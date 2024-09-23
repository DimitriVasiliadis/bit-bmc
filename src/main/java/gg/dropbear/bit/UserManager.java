package gg.dropbear.bit;

import com.google.common.base.Charsets;
import gg.dropbear.bit.containers.Users;
import gg.dropbear.bit.modules.database.DBDAO;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

public class UserManager {

    private int realUserCount;
    private Map<UUID, Users> users;
    private Map<String, Users> usersName;
    private Map<Integer, Users> usersIds;
    private Map<String, List<Users>> duplicateUserNames;
    private Set<UUID> cuffed;
    private Set<UUID> socialSpy;
    private Set<UUID> commandSpy;
    private Set<UUID> signSpy;
    private Set<UUID> alerts;
    protected Player fakeOperator;
    private final UUID emptyUserUUID;
    private final UUID fakeUserUUID;
    private static final String fakeUserName = "DB-Fake-Operator";
    private Main plugin;
    public static HashMap<DBDAO.UserTablesFields, Long> timer;
    public static int timesProcessed;
    List<String> duplicates;
    static DecimalFormat decimalFormat;
    HashMap<UUID, Integer> delaySSTrigger;
    HashMap<UUID, Integer> delayCSTrigger;
    HashMap<UUID, Integer> delaySignSTrigger;

    public UserManager(final Main plugin) {
        this.realUserCount = 0;
        this.users = Collections.synchronizedMap(new HashMap<UUID, Users>());
        this.usersName = Collections.synchronizedMap(new HashMap<String, Users>());
        this.usersIds = Collections.synchronizedMap(new HashMap<Integer, Users>());
        this.duplicateUserNames = Collections.synchronizedMap(new HashMap<String, List<Users>>());
        this.cuffed = new HashSet<UUID>();
        this.socialSpy = new HashSet<UUID>();
        this.commandSpy = new HashSet<UUID>();
        this.signSpy = new HashSet<UUID>();
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


    public Users getUser(final String s) {
        return this.getUser(s, true, false, false, true, true);
    }

    public Users getUser(final String s, final boolean b) {
        return this.getUser(s, b, false, false, true, true);
    }

    @Deprecated
    public Users getUser(final String s, final boolean b, final boolean b2) {
        return this.getUser(s, b, b2, false, true, true);
    }

    public Users getUser(final String s, final boolean b, @Deprecated final boolean b2, final boolean b3, final boolean b4) {
        return this.getUser(s, b, b2, b3, b4, true);
    }

    public Users getUser(String name, final boolean b, @Deprecated final boolean b2, final boolean b3, final boolean b4, final boolean b5) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.length() == 36) {
            try {
                final Users user = this.getUser(UUID.fromString(name));
                if (user != null) {
                    return user;
                }
            }
            catch (Exception ex) {}
            return null;
        }
        final Player player = Bukkit.getPlayer(name);
        if (player != null && player.getName().equalsIgnoreCase(name)) {
            final Users user2 = this.getUser(player);
            if (user2 != null) {
                return user2;
            }
        }
        if (!b4 && b && this.isDuplicatedUserName(name)) {
            final Users duplicatedUser = this.getDuplicatedUser(name);
            if (duplicatedUser != null) {
                return duplicatedUser;
            }
        }
//        if (Config.PrioritizeOnlinePlayers) {
        boolean b6 = false;
        if (name.toLowerCase().endsWith("-exa-")) {
            b6 = true;
            name = name.substring(0, name.length() - "-exa-".length());
        }
        else if (name.toLowerCase().endsWith("-exact-")) {
            b6 = true;
            name = name.substring(0, name.length() - "-exact-".length());
        }
        if (b6) {
            final Users byName = this.getByName(name);
            if (byName != null) {
                if (!byName.isOnline()) {
                    final UUID whoUsesName = this.plugin.getPlayer(name).getUniqueId();
                    if (whoUsesName != null) {
                        final Users user3 = this.getUser(whoUsesName);
                        if (user3 != null && user3.isOnline()) {
                            return user3;
                        }
                    }
                }
                return byName;
            }
//            }
            else if (!b4) {
                final Player player2 = this.plugin.getPlayer(name);
                if (player2 != null) {
                    final Users user4 = this.getUser(player2);
                    if (user4 != null) {
                        return user4;
                    }
                }
            }
        }
        Users user = this.getByName(name);
        if (user != null) {
            if (!user.isOnline()) {
                final UUID whoUsesName2 = this.plugin.getPlayer(name).getUniqueId();
                if (whoUsesName2 != null) {
                    final Users user5 = this.getUser(whoUsesName2);
                    if (user5 != null && user5.isOnline()) {
                        return user5;
                    }
                }
            }
            return user;
        }
        if (b5) {
            final UUID whoUsesName3 = this.plugin.getPlayer(name).getUniqueId();
            if (whoUsesName3 != null) {
                user = this.getUser(whoUsesName3);
                if (user != null) {
                    return user;
                }
            }
        }
        final Player player3 = Bukkit.getPlayer(name);
        if (player3 != null && (!b4 || (b4 && player3.getName().equalsIgnoreCase(name)))) {
            user = this.getUser(player3);
            if (user != null) {
                return user;
            }
        }
        if (!b4) {
            final Player player4 = this.plugin.getPlayer(name);
            if (player4 != null) {
                user = this.getUser(player4);
                if (user != null) {
                    return user;
                }
            }
        }
        if (b3) {
            user = new Users(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
            user.setName(name);
            user.setFakeAccount(true);
            user.addForDelayedSave();
            this.addUser(user);
        }
        return user;
    }

    public Users getUser(final Player player) {
        if (player == null) {
            return null;
        }
        final Users user = this.users.get(player.getUniqueId());
        if (user != null) {
            user.setFakeAccount(false);
            return user;
        }
        final Users user2 = new Users((OfflinePlayer)player);
        if (!player.hasMetadata("NPC")) {
            this.addUser(user2);
            user2.addForDelayedSave();
            return user2;
        }
        return null;
    }

    public Users getUser(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        final Users user = this.users.get(uuid);
        if (user == null) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                final Users user2 = new Users((OfflinePlayer)player);
                if (!player.hasMetadata("NPC")) {
                    this.addUser(user2);
                    return user2;
                }
                return null;
            }
        }
        return user;
    }

    public void addUser(final Users user) {
        if (user == null) {
            return;
        }
        if (user.getUniqueId() == null) {
            return;
        }
        final Users byName = this.getByName(user.getName());
        if (user.getName() != null && byName != null && byName.getUniqueId() != null && !byName.getUniqueId().equals(user.getUniqueId())) {
            this.addDuplicatedUser(user);
            this.addDuplicatedUser(byName);
        }
        this.users.put(user.getUniqueId(), user);
        if (!user.isFakeAccount()) {
            ++this.realUserCount;
        }
        if (user.getName() != null) {
            if (!this.isDuplicatedUserName(user.getName())) {
                this.usersName.put(user.getName().toLowerCase(), user);
            }
            else if (byName != null && System.currentTimeMillis() - byName.getLastLogoffClean() > System.currentTimeMillis() - user.getLastLogoffClean()) {
                this.usersName.put(user.getName().toLowerCase(), user);
            }
        }
    }

    public void addUser(final Users user, final Integer n) {
        if (n > 0) {
            this.usersIds.put(n, user);
        }
    }

    public boolean addDuplicatedUser(final Users user) {
        if (user.getName() == null) {
            return false;
        }
        List<Users> list = new ArrayList<Users>();
        if (this.duplicateUserNames.containsKey(user.getName().toLowerCase())) {
            list = this.duplicateUserNames.get(user.getName().toLowerCase());
        }
        boolean b = false;
        final Iterator<Users> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUniqueId().equals(user.getUniqueId())) {
                b = true;
            }
        }
        if (!b) {
            list.add(user);
        }
        this.duplicateUserNames.put(user.getName().toLowerCase(), list);
        return true;
    }

    public boolean isDuplicatedUserName(final String s) {
        return this.duplicateUserNames.containsKey(s.toLowerCase());
    }

    public Users getDuplicatedUser(final String s) {
        if (!this.duplicateUserNames.containsKey(s.toLowerCase())) {
            return null;
        }
        Users user = null;
        for (final Users user2 : this.duplicateUserNames.get(s.toLowerCase())) {
            if (user2.isOnline()) {
                return user2;
            }
        }
        for (final Users user3 : this.duplicateUserNames.get(s.toLowerCase())) {
            if (user == null) {
                user = user3;
            }
            if (user3.getName() == null) {
                continue;
            }
            if (System.currentTimeMillis() - user.getLastLogoffClean() <= System.currentTimeMillis() - user3.getLastLogoffClean()) {
                continue;
            }
            user = user3;
        }
        return user;
    }

    private Users getByName(final String s) {
        if (s == null) {
            return null;
        }
        return this.usersName.get(s.toLowerCase());
    }
}
