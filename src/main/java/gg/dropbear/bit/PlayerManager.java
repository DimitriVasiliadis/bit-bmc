package gg.dropbear.bit;

import gg.dropbear.bit.containers.Users;
import gg.dropbear.bit.modules.database.DBDAO;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class PlayerManager {
    private int realUserCount;
    private Map<UUID, Users> users;
    private Map<String, Users> usersName;
    private Map<Integer, Users> usersIds;
    private Map<String, List<Users>> duplicateUserNames;
    private Set<UUID> alerts;
    protected Player fakeOperator;
    private final UUID emptyUserUUID;
    private final UUID fakeUserUUID;
    private static final String fakeUserName = "DB-Fake-Operator";
    private Main plugin;
    public static HashMap<DBDAO.UserTablesFields, Long> timer;
    public static int timesProcessed;
    public static final String mapKeySeparator = "%%";
    private static final String mapKeySeparatorForsave = "T9C";
    private static final String sectionSeparator = ":";
    private static final String sectionSeparatorForSave = "T8C";
    public static final String lineSeparator = ";";
    private static final String lineSeparatorForSave = "T7C";
    public static final String multiSeparator = "-X-";
    List<String> duplicates;
    static DecimalFormat decimalFormat;
    HashMap<UUID, Integer> delaySSTrigger;
    HashMap<UUID, Integer> delayCSTrigger;
    HashMap<UUID, Integer> delaySignSTrigger;

    static {
        PlayerManager.timer = new HashMap<DBDAO.UserTablesFields, Long>();
        PlayerManager.timesProcessed = 0;
        PlayerManager.decimalFormat = new DecimalFormat("0.00");
    }

    public PlayerManager(final Main plugin) {
        this.realUserCount = 0;
        this.users = Collections.synchronizedMap(new HashMap<UUID, Users>());
        this.usersName = Collections.synchronizedMap(new HashMap<String, Users>());
        this.usersIds = Collections.synchronizedMap(new HashMap<Integer, Users>());
        this.duplicateUserNames = Collections.synchronizedMap(new HashMap<String, List<Users>>());
        this.alerts = new HashSet<UUID>();
        this.fakeOperator = null;
        this.emptyUserUUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        this.fakeUserUUID = UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff0");
        this.duplicates = new ArrayList<String>();
        this.delaySSTrigger = new HashMap<UUID, Integer>();
        this.delayCSTrigger = new HashMap<UUID, Integer>();
        this.delaySignSTrigger = new HashMap<UUID, Integer>();
        this.plugin = plugin;
    }

    public void addUser(final Users Users) {
        if (Users == null) {
            return;
        }
        if (Users.getUniqueId() == null) {
            return;
        }
        final Users byName = this.getByName(Users.getName());
        if (Users.getName() != null && byName != null && byName.getUniqueId() != null && !byName.getUniqueId().equals(Users.getUniqueId())) {
            this.addDuplicatedUser(Users);
            this.addDuplicatedUser(byName);
        }
        this.users.put(Users.getUniqueId(), Users);
        if (!Users.isFakeAccount()) {
            ++this.realUserCount;
        }
        if (Users.getName() != null) {
            if (!this.isDuplicatedUserName(Users.getName())) {
                this.usersName.put(Users.getName().toLowerCase(), Users);
            }
            else if (byName != null && System.currentTimeMillis() - byName.getLastLogoffClean() > System.currentTimeMillis() - Users.getLastLogoffClean()) {
                this.usersName.put(Users.getName().toLowerCase(), Users);
            }
        }
    }

    public void addUser(final Users Users, final Integer n) {
        if (n > 0) {
            this.usersIds.put(n, Users);
        }
    }

    public boolean addDuplicatedUser(final Users Users) {
        if (Users.getName() == null) {
            return false;
        }
        List<Users> list = new ArrayList<Users>();
        if (this.duplicateUserNames.containsKey(Users.getName().toLowerCase())) {
            list = this.duplicateUserNames.get(Users.getName().toLowerCase());
        }
        boolean b = false;
        final Iterator<Users> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUniqueId().equals(Users.getUniqueId())) {
                b = true;
            }
        }
        if (!b) {
            list.add(Users);
        }
        this.duplicateUserNames.put(Users.getName().toLowerCase(), list);
        return true;
    }

    public boolean isDuplicatedUserName(final String s) {
        return this.duplicateUserNames.containsKey(s.toLowerCase());
    }

    public String getFakeUserName() {
        return "DB-Fake-Operator";
    }

    private Users getByName(final String s) {
        if (s == null) {
            return null;
        }
        return this.usersName.get(s.toLowerCase());
    }

    public PreparedStatement savePlayerToDB(final Users Users, final PreparedStatement preparedStatement, final boolean b) throws SQLException {
        int n = 1;
        ++PlayerManager.timesProcessed;
        DBDAO.UserTablesFields[] values;
        for (int length = (values = DBDAO.UserTablesFields.values()).length, i = 0; i < length; ++i) {
            final DBDAO.UserTablesFields obj = values[i];
            try {
                Object o = null;
                switch (obj) {
                    case DisplayName: {
                        o = Users.getDisplayNameClean(false);
                        if (o != null && o.equals(Users.getName(false))) {
                            o = null;
                            break;
                        }
                        break;
                    }
                    case LastLoginTime: {
                        o = Users.getLastLogin();
                        break;
                    }
                    case LastLogoffTime: {
                        o = Users.getLastLogoff();
                        break;
                    }
                    case LogOutLocation: {
                        o = Users.getLogOutLocation();
                        break;
                    }
                    case TeleportLocation: {
                        o = Users.getLastTeleportLocation();
                        break;
                    }
                    case FakeAccount: {
                        o = Users.isFakeAccount();
                        break;
                    }
                    case username: {
                        o = Users.getName(false);
                        break;
                    }
                    case Ignores: {
                        o = Users.getIgnoresString();
                        break;
                    }
                    case player_uuid: {
                        if (!b) {
                            o = Users.getUniqueId().toString();
                            break;
                        }
                        continue;
                    }
                }
                proccessForSaveField(preparedStatement, n, o, obj);
                ++n;
            }
            catch (Throwable t) {
                ++n;
                this.plugin.consoleMessage(" SEVERE SAVE ERROR WHILE SAVING " + Users.getName() + " PLAYER DATA (" + obj + ")");
                t.printStackTrace();
                throw t;
            }
        }
        if (b) {
            try {
                preparedStatement.setInt(n, Users.getId());
                ++n;
            }
            catch (Throwable t2) {
                t2.printStackTrace();
            }
        }
        else if (DBDAO.Format != DBDAO.mysqltypes.old) {
            try {
                preparedStatement.setString(n, Users.getUniqueId().toString());
                ++n;
            }
            catch (Throwable t3) {}
        }
        return preparedStatement;
    }
}
