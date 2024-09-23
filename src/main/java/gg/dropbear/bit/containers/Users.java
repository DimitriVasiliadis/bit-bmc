package gg.dropbear.bit.containers;

import gg.dropbear.bit.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Users {

    private Player player;
    private boolean collision;
    private String name;

    private String displayName;
    private UUID uuid;
    private int id;
    private int invId;
    private Location logOutLocation;
    private long lastLogin;
    private long lastLogoff;
    private Location TpLoc;
    private long alertUntil;
    private String alertReason;
//    private HashMap<String, Notification> notifications;
//    private CMDCooldown CommandCooldown;
    private boolean isFakeAccount;
//    private DBStats stats;
    private Set<UUID> ignores;
    private boolean silenceMode;
//    private PlayerMeta playerMeta;
//    private List<DBPlayerWarning> warnings;
//    HashMap<updateType, Long> lastInfoUpdate;
//    private DBChatRoom chatRoom;
//    HashMap<PlayerOption, Boolean> options;
    Boolean fakeUser;
    private Integer schedId;
    private Boolean extend;
    Long time;
    private static Statistic statCheck;

    public Users(final int n) {
        this.collision = true;
        this.id = 0;
        this.invId = 0;
        this.lastLogin = 0L;
        this.lastLogoff = 0L;
        this.alertUntil = 0L;
        this.isFakeAccount = false;
        this.silenceMode = false;
//        this.options = null;
        this.fakeUser = null;
        this.time = null;
        this.id = n;
        Main.getInstance().getPlayerManager().addUser(this, n);
    }

    public Users(final UUID uuid) {
        this.collision = true;
        this.id = 0;
        this.invId = 0;
        this.lastLogin = 0L;
        this.lastLogoff = 0L;
        this.alertUntil = 0L;
        this.isFakeAccount = false;
        this.silenceMode = false;
        this.fakeUser = null;
        this.time = null;
        this.uuid = uuid;
        this.player = Bukkit.getPlayer(this.uuid);
        if (this.player != null) {
            this.name = this.player.getName();
        }
    }


    public Users(final OfflinePlayer offlinePlayer) {
        this.collision = true;
        this.id = 0;
        this.invId = 0;
        this.lastLogin = 0L;
        this.lastLogoff = 0L;
        this.alertUntil = 0L;
        this.isFakeAccount = false;
        this.silenceMode = false;
        this.fakeUser = null;
        this.time = null;
        this.name = offlinePlayer.getName();
        this.uuid = offlinePlayer.getUniqueId();
        this.player = Bukkit.getPlayer(this.uuid);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.uuid) != null;
    }

    public String getName() {
        return this.getName(false);
    }

    public int getId() {
        return this.id;
    }


    public String getName(final boolean b) {
        if (this.isOnline()) {
            return this.name = this.getPlayer().getName();
        }
        if (b && this.name == null) {
            final Player player = Bukkit.getPlayer(this.uuid);
            this.player = ((player != null) ? player : this.player);
            if (this.player != null && this.player.isOnline()) {
                this.name = this.player.getName();
                this.addForDelayedSave();
            }
            else {
                this.name = this.getOfflinePlayer().getName();
            }
        }
        return this.name;
    }

    public Player getPlayer() {
        return this.getPlayer(!this.isFakeAccount());
    }

    public Player getPlayer(final boolean b) {
        final Player player = Bukkit.getPlayer(this.uuid);
        if (player == null) {
            if (b) {
                if (this.player == null) {
                    this.player = Main.getInstance().getPlayer(this.displayName);
                    this.ScheduleDataClear();
                }
                else {
                    this.extend = true;
                }
            }
        }
        else {
            this.player = player;
            if (this.schedId != null) {
                Bukkit.getServer().getScheduler().cancelTask((int)this.schedId);
                this.schedId = null;
            }
        }
        try {
            if (this.player != null && this.player.hasMetadata("NPC")) {
                return null;
            }
        }
        catch (Exception ex) {
            return null;
        }
        return this.player;
    }

    public OfflinePlayer getOfflinePlayer() {
        if (this.isOnline()) {
            return (OfflinePlayer)this.getPlayer(false);
        }
        if (this.getUniqueId() == null) {
            return null;
        }
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public long getLastLogoffClean() {
        return this.lastLogoff;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFakeAccount(final boolean isFakeAccount) {
        this.isFakeAccount = isFakeAccount;
    }

    public void setId(final int n) {
        this.id = n;
        Main.getInstance().getPlayerManager().addUser(this, n);
    }


    public void addForDelayedSave() {
        if (this.fakeUser == null) {
            this.fakeUser = (this.getName() != null && this.getName().equalsIgnoreCase(Main.getInstance().getPlayerManager().getFakeUserName()));
        }
        if (this.fakeUser) {
            return;
        }
        Main.getInstance().getDbManager().addForSave(this);
    }

    private void ScheduleDataClear() {
        if (this.schedId != null) {
            Bukkit.getServer().getScheduler().cancelTask((int)this.schedId);
        }
        if (Main.getInstance().isEnabled()) {
            this.schedId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)Main.getInstance(), (Runnable)new Runnable() {
                @Override
                public void run() {
                    if (Users.this.extend != null && Users.this.extend) {
//                        Users.access$1(Users.this, null);
                        Users.this.ScheduleDataClear();
                        return;
                    }
//                    Users.this.unloadData();
//                    Users.access$3(Users.this, null);
                }
            }, 1200L);
        }
    }

    public Boolean isFakeAccount() {
        return this.isFakeAccount;
    }
}
