package gg.dropbear.bit.modules.database;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.containers.Users;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DBManager {

    private DBDAO dao;
    private Main plugin;
    private DataBaseType DbType;
    int autoSaveInterval;
    private boolean ForceSaveOnLogOut;
    private boolean ForceLoadOnLogIn;
    private static final String fileName = "DataBaseInfo.yml";
    private String username;
    private String password;
    private String hostname;
    private String database;
    private String prefix;
    private boolean autoReconnect;
    private boolean useSSL;
    private boolean verifyServerCertificate;
    private Set<Users> playerListToSave;
    int autosaveBukkitId;
    BukkitTask task;
    Long startedAt;
    private Runnable autoSave;
    HashMap<String, Users> getPlayerId;
    Boolean all;
    Integer oldRapidvalue;
    boolean startingDb;

    public DBManager(final Main plugin) {
        this.DbType = DataBaseType.SqLite;
        this.autoSaveInterval = 15;
        this.ForceSaveOnLogOut = false;
        this.ForceLoadOnLogIn = false;
        this.username = "";
        this.password = "";
        this.hostname = "";
        this.database = "";
        this.prefix = "";
        this.autoReconnect = true;
        this.useSSL = true;
        this.verifyServerCertificate = true;
        this.autosaveBukkitId = -1;
        this.task = null;
        this.startedAt = 0L;
        this.autoSave = new Runnable() {
            @Override
            public void run() {
                if (DBManager.this.task != null && DBManager.this.startedAt + 60000L < System.currentTimeMillis()) {
                    Bukkit.getScheduler().cancelTask(DBManager.this.task.getTaskId());
                    DBManager.this.task = null;
                }
                if (DBManager.this.task == null) {
                    DBManager.this.startedAt = System.currentTimeMillis();
                    DBManager.this.task = Bukkit.getScheduler().runTaskAsynchronously((Plugin)DBManager.this.plugin, (Runnable)new Runnable() {
                        @Override
                        public void run() {
                            DBManager.this.saveBatch(DBManager.this.all);
                            DBManager.this.all = false;
                        }
                    });
                }
            }
        };
        this.all = false;
        this.oldRapidvalue = null;
        this.startingDb = false;
        this.plugin = plugin;
    }

    public void clear() {
        this.getPlayerId.clear();
    }

    public DBDAO getDB() {
        return this.dao;
    }

    public void saveBatch(final boolean b) {
        if (this.startingDb) {
            return;
        }
        try {
            if (!this.playerListToSave.isEmpty()) {
                if (!this.getDB().isConnected() && this.DbType == DataBaseType.MySQL) {

                    if (!this.startingDb) {
                        this.startingDb = true;
                        try {
                            this.dao = this.startMysql(false);
                        }
                        catch (Throwable t) {
                            t.printStackTrace();
                        }
                        finally {
                            this.startingDb = false;
                        }
                        this.startingDb = false;
                    }

                    if (this.getDB() != null && this.getDB().isConnected()) {
                        Main.getInstance().consoleMessage("&2Re-established MySQL connection");
                        return;
                    }
                }
                if ((this.getDB() == null || !this.getDB().isConnected()) && this.DbType == DataBaseType.MySQL) {
                    Main.getInstance().consoleMessage("&cCan't save player data. DataBase connection can't be established");
                    return;
                }
                if (this.getDB().isLocked()) {
                    return;
                }
                this.getDB().prepareTempBatch();
                final HashSet<Users> firstPlayersForSave = this.getFirstPlayersForSave(b);
                if (this.oldRapidvalue != null) {
                    this.plugin.consoleMessage("Saving player data: " + firstPlayersForSave.size() + "/" + this.playerListToSave.size());
                }
                for (final Users value : firstPlayersForSave) {
                    if (value.getUniqueId() == null) {
                        continue;
                    }
                    if (!value.isFakeAccount() && !value.isOnline()) {
                        continue;
                    }
                    if (value.getId() == 0) {
                        this.getPlayerId.put(value.getUniqueId().toString(), value);
                    }
                    boolean updatePlayer = false;
                    try {
                        updatePlayer = this.getDB().updatePlayer(value);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (updatePlayer) {
                        continue;
                    }
                    this.addForSave(value);
                }

                if (this.getDB().isLocked()) {
                    return;
                }
                try {
                    if (this.getDB().executeTempBatch()) {
                        this.playerListToSave.removeAll(firstPlayersForSave);
                    }
                }
                catch (Throwable t2) {
                    t2.printStackTrace();
                }
                if (this.getDB().isLocked()) {
                    return;
                }
                if (!this.getPlayerId.isEmpty()) {
                    this.getDB().getUserIds(this.getPlayerId);
                }
                if (this.getDB().isLocked()) {
                    return;
                }

                this.clear();
                this.task = null;
                return;
            }
            else if (this.oldRapidvalue != null) {
                this.autoSaveInterval = this.oldRapidvalue;
                this.oldRapidvalue = null;
                this.task = null;
                this.start();
                Main.getInstance().consoleMessage("Saved all player data");
            }
        }
        catch (Exception ex2) {
            this.plugin.consoleMessage(" SEVERE SAVE ERROR");
            ex2.printStackTrace();
            this.task = null;
            return;
        }
        this.task = null;
    }

    private synchronized DBMySQL startMysql(final boolean b) {
        final YamlConfiguration loadConfiguration = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "Settings" + File.separator + "DataBaseInfo.yml"));
        final String string = loadConfiguration.getString("mysql.url");
        if (string != null) {
            final String prefix = "jdbc:mysql://";
            if (string.toLowerCase().startsWith(prefix)) {
                final String[] split = string.substring(prefix.length()).split("/");
                if (split.length >= 2) {
                    loadConfiguration.set("mysql.hostname", (Object)split[0]);
                    loadConfiguration.set("mysql.database", (Object)split[1]);
                }
            }
        }
        final String string2 = loadConfiguration.getString("mysql.username");
        if (string2 == null) {
            this.plugin.consoleMessage("&cmysql.username property invalid or missing");
        }
        final String string3 = loadConfiguration.getString("mysql.password");
        final String string4 = loadConfiguration.getString("mysql.hostname");
        final String string5 = loadConfiguration.getString("mysql.database");
        final String string6 = loadConfiguration.getString("mysql.tablePrefix");
        final boolean boolean1 = loadConfiguration.getBoolean("mysql.autoReconnect");
        final boolean boolean2 = loadConfiguration.getBoolean("mysql.useSSL");
        final boolean boolean3 = loadConfiguration.getBoolean("mysql.verifyServerCertificate");
        if (b && this.dao != null) {
            boolean b2 = false;
            if (!this.username.equals(string2)) {
                b2 = true;
            }
            if (!this.password.equals(string3)) {
                b2 = true;
            }
            if (!this.hostname.equals(string4)) {
                b2 = true;
            }
            if (!this.database.equals(string5)) {
                b2 = true;
            }
            if (!this.prefix.equals(string6)) {
                b2 = true;
            }
            if (this.autoReconnect != boolean1) {
                b2 = true;
            }
            if (this.useSSL != boolean2) {
                b2 = true;
            }
            if (this.verifyServerCertificate != boolean3) {
                b2 = true;
            }
            if (this.dao != null && !b2) {
                return (DBMySQL)this.dao;
            }
        }
        this.username = string2;
        this.password = string3;
        this.hostname = string4;
        this.database = string5;
        this.prefix = string6;
        this.autoReconnect = boolean1;
        this.useSSL = boolean2;
        this.verifyServerCertificate = boolean3;
        if (this.plugin.isEnabled()) {
            final DBMySQL dbMySQL = new DBMySQL(this.plugin, string4, string5, string2, string3, string6, boolean1, boolean3, boolean2);
            dbMySQL.initialize();
            return dbMySQL;
        }
        return null;
    }

    public void addForSave(final Users Users) {
        this.playerListToSave.add(Users);
    }

    private synchronized HashSet<Users> getFirstPlayersForSave(final boolean b) {
        final HashSet set = new HashSet<>();
        if (this.playerListToSave.size() < 10) {
            int n = 0;
            for (final Users Users : this.playerListToSave) {
                ++n;
            }
            if (n == 0) {
                this.playerListToSave.clear();
            }
        }
        if (b) {
            set.addAll(this.playerListToSave);
            return (HashSet<Users>)set;
        }
        int n2 = 0;
        final int n3 = (this.oldRapidvalue == null) ? 50 : 250;
        final int n4 = (int)(this.playerListToSave.size() * 0.1);
        final int n5 = (n4 > n3) ? n4 : n3;
        final int n6 = (n5 > 500) ? 500 : n5;
        for (final Users e : this.playerListToSave) {
            if (n2 >= n6) {
                break;
            }
            set.add(e);
            ++n2;
        }
        return (HashSet<Users>)set;
    }



    public enum DataBaseType
    {
        MySQL("MySQL", 0),
        SqLite("SqLite", 1);

        private DataBaseType(final String name, final int ordinal) {
        }
    }
}
