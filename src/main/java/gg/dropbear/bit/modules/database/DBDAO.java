package gg.dropbear.bit.modules.database;

import gg.dropbear.bit.Main;
import gg.dropbear.bit.containers.Users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public abstract class DBDAO {

    private DBConnectionPool pool;
    private static String prefix;
    protected Main plugin;
    private static DBManager.DataBaseType dbType;
    public static mysqltypes Format;
    private PreparedStatement updateBatch;
    private boolean updateBatchExecuted;
    private PreparedStatement insertBatch;
    private boolean insertBatchExecuted;
    private PreparedStatement inventoryUpdateBatch;
    private boolean inventoryUpdateBatchExecuted;
    private PreparedStatement inventoryInsertBatch;
    private boolean inventoryInsertBatchExecuted;
    private PreparedStatement playtimerewardUpdateBatch;
    private boolean playtimerewardUpdateBatchExecuted;
    private PreparedStatement playtimerewardInsertBatch;
    private boolean playtimerewardInsertBatchExecuted;
    private PreparedStatement playtimeUpdateBatch;
    private boolean playtimeUpdateBatchExecuted;
    private PreparedStatement playtimeInsertBatch;
    private boolean playtimeInsertBatchExecuted;
    private boolean autoCommit;
    private boolean locked;
    boolean ignoredFirst;

    static {
        DBDAO.dbType = DBManager.DataBaseType.SqLite;
        DBDAO.Format = mysqltypes.MySQL;
    }


    protected DBDAO(final Main plugin, final String s, final String s2, final String s3, final String s4, final String prefix) {
        this.updateBatch = null;
        this.updateBatchExecuted = true;
        this.insertBatch = null;
        this.insertBatchExecuted = true;
        this.inventoryUpdateBatch = null;
        this.inventoryUpdateBatchExecuted = true;
        this.inventoryInsertBatch = null;
        this.inventoryInsertBatchExecuted = true;
        this.playtimerewardUpdateBatch = null;
        this.playtimerewardUpdateBatchExecuted = true;
        this.playtimerewardInsertBatch = null;
        this.playtimerewardInsertBatchExecuted = true;
        this.playtimeUpdateBatch = null;
        this.playtimeUpdateBatchExecuted = true;
        this.playtimeInsertBatch = null;
        this.playtimeInsertBatchExecuted = true;
        this.autoCommit = true;
        this.locked = false;
        this.ignoredFirst = false;
        this.plugin = plugin;
        DBDAO.prefix = prefix;
        try {
            this.pool = new DBConnectionPool(s, s2, s3, s4);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            this.pool.getConnection();
        }
        catch (Throwable t2) {
            try {
                this.pool = new DBConnectionPool(s, s2.replace("utf8mb4_unicode_ci", "utf-8"), s3, s4);
            }
            catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        try {
            Main.getInstance().consoleMessage(String.valueOf(this.pool.getConnection().getMetaData().getDatabaseProductVersion()) + " data base type detected");
            if (this.pool.getConnection().getMetaData().getDatabaseProductVersion().toLowerCase().contains("mariadb")) {
                DBDAO.Format = mysqltypes.MariaDB;
            }
        }
        catch (Error | Exception error) {
            final Throwable t = null;
            t.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return this.pool.getConnection() != null && !this.pool.getConnection().isClosed();
        }
        catch (Error | Exception error) {
            return false;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setDbType(final DBManager.DataBaseType dbType) {
        DBDAO.dbType = dbType;
    }


    public final synchronized void setUp() {
        if (this.plugin.isFullyLoaded()) {
            return;
        }
        this.createDefaultTable(DBTables.UserTable);
        final String tableCharSet = this.getTableCharSet(DBTables.UserTable);
        if (tableCharSet != null && !tableCharSet.contains("utf8")) {
            this.convertTableToUTF8(DBTables.UserTable);
        }
        final String tableRowFormat = this.getTableRowFormat(DBTables.UserTable);
        if (tableRowFormat != null && !tableRowFormat.equalsIgnoreCase("dynamic")) {
            this.convertTableRowFormat(DBTables.UserTable);
        }
        this.checkDefaultUserCollumns();
    }

    public void close(final ResultSet set) {
        if (set != null) {
            try {
                set.close();
            }
            catch (Error | Exception error) {
                final Throwable t = null;
                t.printStackTrace();
            }
        }
    }

    public void close(final Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (Error | Exception error) {
                final Throwable t = null;
                t.printStackTrace();
            }
        }
    }

    private boolean checkDefaultUserCollumns() {
        UserTablesFields[] values;
        for (int length = (values = UserTablesFields.values()).length, i = 0; i < length; ++i) {
            final UserTablesFields userTablesFields = values[i];
            if (!this.isCollumn(DBTables.UserTable.getTableName(), userTablesFields.getCollumn())) {
                this.addCollumn(DBTables.UserTable.getTableName(), userTablesFields.getCollumn(), userTablesFields.getType());
            }
        }
        return true;
    }

    private boolean createDefaultTable(final DBTables dbTables) {
        if (this.isTable(dbTables.getTableName())) {
            return true;
        }
        try {
            Main.getInstance().consoleMessage(String.valueOf(dbTables.toString()) + " creating " + dbTables.getQuery());
            this.createTable(dbTables.getQuery());
            return true;
        }
        catch (Error | Exception error) {
            final Throwable t = null;
            t.printStackTrace();
            return false;
        }
    }

    protected DBConnection getConnection() {
        try {
            return this.pool.getConnection();
        }
        catch (Error | Exception error) {
            final Object o = null;
            this.plugin.consoleMessage("&cUnable to connect to the database: " + ((Throwable)o).getMessage());
            return null;
        }
    }

    public void setAutoCommit(final boolean b) {
        try {
            this.getConnection().setAutoCommit(b);
            this.autoCommit = b;
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    public boolean executeTempBatch() {
        if (this.locked) {
            return false;
        }
        this.setAutoCommit(false);
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            if (!this.updateBatchExecuted) {
                this.updateBatch.executeBatch();
                this.updateBatch.close();
                this.updateBatch = null;
                this.updateBatchExecuted = true;
            }
        }
        catch (Throwable t) {
            if (this.updateBatch != null) {
                try {
                    this.updateBatch.close();
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            this.updateBatch = null;
            t.printStackTrace();
        }
        try {
            if (!this.insertBatchExecuted) {
                this.insertBatch.executeBatch();
                this.insertBatch.close();
                this.insertBatch = null;
                this.insertBatchExecuted = true;
            }
        }
        catch (Throwable t2) {
            if (this.ignoredFirst) {
                t2.printStackTrace();
            }
            this.ignoredFirst = true;
            DBDAO.Format = mysqltypes.old;
            if (this.insertBatch != null) {
                try {
                    this.insertBatch.close();
                }
                catch (SQLException ex2) {
                    ex2.printStackTrace();
                }
            }
            this.insertBatch = null;
            this.insertBatchExecuted = true;
        }
        try {
            if (!this.playtimeUpdateBatchExecuted) {
                this.playtimeUpdateBatch.executeBatch();
                this.playtimeUpdateBatch.close();
                this.playtimeUpdateBatch = null;
                this.playtimeUpdateBatchExecuted = true;
            }
        }
        catch (Throwable t3) {
            t3.printStackTrace();
        }
        try {
            if (!this.playtimeInsertBatchExecuted) {
                this.playtimeInsertBatch.executeBatch();
                this.playtimeInsertBatch.close();
                this.playtimeInsertBatch = null;
                this.playtimeInsertBatchExecuted = true;
            }
        }
        catch (Throwable t4) {
            t4.printStackTrace();
        }
        try {
            if (!this.inventoryUpdateBatchExecuted) {
                this.inventoryUpdateBatch.executeBatch();
                this.inventoryUpdateBatch.close();
                this.inventoryUpdateBatch = null;
                this.inventoryUpdateBatchExecuted = true;
            }
        }
        catch (Throwable t5) {
            t5.printStackTrace();
        }
        try {
            if (!this.inventoryInsertBatchExecuted) {
                this.inventoryInsertBatch.executeBatch();
                this.inventoryInsertBatch.close();
                this.inventoryInsertBatch = null;
                this.inventoryInsertBatchExecuted = true;
            }
        }
        catch (Throwable t6) {
            t6.printStackTrace();
        }
        try {
            if (!this.playtimerewardUpdateBatchExecuted) {
                this.playtimerewardUpdateBatch.executeBatch();
                this.playtimerewardUpdateBatch.close();
                this.playtimerewardUpdateBatch = null;
                this.playtimerewardUpdateBatchExecuted = true;
            }
        }
        catch (Throwable t7) {
            t7.printStackTrace();
        }
        try {
            if (!this.playtimerewardInsertBatchExecuted) {
                this.playtimerewardInsertBatch.executeBatch();
                this.playtimerewardInsertBatch.close();
                this.playtimerewardInsertBatch = null;
                this.playtimerewardInsertBatchExecuted = true;
            }
        }
        catch (Throwable t8) {
            t8.printStackTrace();
        }
        this.setAutoCommit(false);
        if (this.locked) {
            return false;
        }
        try {
            if (!this.autoCommit) {
                connection.commit();
            }
        }
        catch (Error | Exception error) {
            final Throwable t9 = null;
            t9.printStackTrace();
        }
        return true;
    }


    public void prepareTempBatch() {
        if (this.locked) {
            return;
        }
        this.setAutoCommit(false);
    }

    public boolean updatePlayer(final Users Users) {
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        if (connection.isClosed()) {
            return false;
        }
        if (Users.getId() == 0) {
            try {
                if (this.insertBatch == null) {
                    this.insertBatch = connection.prepareStatement(DBTables.UserTable.getInsertQuery());
                }
                try {
                    this.plugin.getPlayerManager().savePlayerToDB(Users, this.insertBatch, false);
                }
                catch (Throwable t) {
                    if (this.insertBatch != null) {
                        try {
                            this.insertBatch.close();
                        }
                        catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                    this.insertBatch = null;
                    t.printStackTrace();
                    return false;
                }
                if (this.insertBatch != null) {
                    this.insertBatch.addBatch();
                    this.insertBatchExecuted = false;
                    return true;
                }
                return true;
            }
            catch (Throwable t2) {
                if (this.insertBatch != null) {
                    try {
                        this.insertBatch.close();
                    }
                    catch (SQLException ex2) {
                        ex2.printStackTrace();
                        return false;
                    }
                }
                this.insertBatch = null;
                t2.printStackTrace();
                return false;
            }
        }
        try {
            if (this.updateBatch == null) {
                this.updateBatch = connection.prepareStatement(DBTables.UserTable.getUpdateQuery());
            }
            try {
                this.plugin.getPlayerManager().savePlayerToDB(Users, this.updateBatch, true);
            }
            catch (Throwable t3) {
                if (this.updateBatch != null) {
                    try {
                        this.updateBatch.close();
                    }
                    catch (SQLException ex3) {
                        ex3.printStackTrace();
                        return false;
                    }
                }
                this.updateBatch = null;
                t3.printStackTrace();
                return false;
            }
            if (this.updateBatch != null) {
                this.updateBatch.addBatch();
                this.updateBatchExecuted = false;
            }
        }
        catch (Throwable t4) {
            if (this.updateBatch != null) {
                try {
                    this.updateBatch.close();
                }
                catch (SQLException ex4) {
                    ex4.printStackTrace();
                    return false;
                }
            }
            this.updateBatch = null;
            t4.printStackTrace();
            return false;
        }
        return true;
    }

    public void getUserIds(final HashMap<String, Users> hashMap) {
        if (this.locked) {
            return;
        }
        this.setAutoCommit(true);
        if (hashMap.size() < 4) {
            for (final Map.Entry<String, Users> entry : hashMap.entrySet()) {
                entry.getValue().setId(this.plugin.getDbManager().getDB().getId(entry.getValue().getUniqueId().toString()));
            }
            return;
        }
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return;
        }
        PreparedStatement prepareStatement = null;
        ResultSet executeQuery = null;
        try {
            prepareStatement = connection.prepareStatement("SELECT * FROM `" + DBTables.UserTable.getTableName() + "`;");
            executeQuery = prepareStatement.executeQuery();
            while (executeQuery.next()) {
                if (this.locked) {
                    this.close(executeQuery);
                    this.close(prepareStatement);
                    return;
                }
                if (!hashMap.containsKey(executeQuery.getString(UserTablesFields.player_uuid.getCollumn()))) {
                    continue;
                }
                hashMap.get(executeQuery.getString(UserTablesFields.player_uuid.getCollumn())).setId(executeQuery.getInt("id"));
            }
        }
        catch (Error error) {}
        catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        finally {
            this.close(executeQuery);
            this.close(prepareStatement);
        }
        this.close(executeQuery);
        this.close(prepareStatement);
    }

    private int getId(final String s) {
        this.setAutoCommit(true);
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return 0;
        }
        PreparedStatement prepareStatement = null;
        ResultSet executeQuery = null;
        int int1 = 0;
        try {
            prepareStatement = connection.prepareStatement("SELECT `id` FROM `" + DBTables.UserTable.getTableName() + "` WHERE `player_uuid` = ? LIMIT 1;");
            prepareStatement.setString(1, s);
            executeQuery = prepareStatement.executeQuery();
            if (executeQuery.next()) {
                int1 = executeQuery.getInt("id");
            }
        }
        catch (Error error) {}
        catch (Exception ex) {
            ex.printStackTrace();
            return int1;
        }
        finally {
            this.close(executeQuery);
            this.close(prepareStatement);
        }
        this.close(executeQuery);
        this.close(prepareStatement);
        return int1;
    }

    public abstract Statement prepareStatement(final String p0);

    public abstract boolean createTable(final String p0);

    public abstract boolean isTable(final String p0);

    public abstract boolean isCollumn(final String p0, final String p1);

    public abstract boolean truncate(final String p0);

    public abstract boolean addCollumn(final String p0, final String p1, final String p2);

    public abstract String getTableCharSet(final DBTables p0);

    public abstract String getTableRowFormat(final DBTables p0);

    public abstract boolean convertTableRowFormat(final DBTables p0);

    public abstract boolean convertTableToUTF8(final DBTables p0);




    public enum TablesFieldsType
    {
        decimal("decimal", 0, "double"),
        number("number", 1, "int"),
        longtext("longtext", 2, "longtext"),
        text("text", 3, "text"),
        stringList("stringList", 4, "text"),
        stringLongMap("stringLongMap", 5, "text"),
        stringIntMap("stringIntMap", 6, "text"),
        stringDoubleMap("stringDoubleMap", 7, "text"),
        stringStringMap("stringStringMap", 8, "text"),
        locationMap("locationMap", 9, "text"),
        state("state", 10, "boolean"),
        location("location", 11, "text"),
        longNumber("longNumber", 12, "bigint");

        private String type;

        private TablesFieldsType(final String name, final int ordinal, final String type) {
            this.type = type;
        }

        public static TablesFieldsType getByname(final String anotherString) {
            TablesFieldsType[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final TablesFieldsType tablesFieldsType = values[i];
                if (tablesFieldsType.name().equalsIgnoreCase(anotherString)) {
                    return tablesFieldsType;
                }
            }
            return null;
        }

        public String getType() {
            return this.type;
        }
    }

    public enum UserTablesFields
    {
        player_uuid("player_uuid", 0, "text", TablesFieldsType.text),
        username("username", 1, "text", TablesFieldsType.text),
        nickname("nickname", 2, "text", TablesFieldsType.text),
        LogOutLocation("LogOutLocation", 3, "text", TablesFieldsType.location),
        DeathLocation("DeathLocation", 4, "text", TablesFieldsType.location),
        TeleportLocation("TeleportLocation", 5, "text", TablesFieldsType.location),
        Homes("Homes", 6, "text", TablesFieldsType.locationMap),
        LastLoginTime("LastLoginTime", 7, "bigint", TablesFieldsType.longNumber),
        LastLogoffTime("LastLogoffTime", 8, "bigint", TablesFieldsType.longNumber),
        TotalPlayTime("TotalPlayTime", 9, "bigint", TablesFieldsType.longNumber),
        tFly("tFly", 10, "bigint", TablesFieldsType.longNumber),
        tGod("tGod", 11, "bigint", TablesFieldsType.longNumber),
        Glow("Glow", 12, "text", TablesFieldsType.text),
        Ips("Ips", 13, "text", TablesFieldsType.stringIntMap),
        Cuffed("Cuffed", 14, "boolean", TablesFieldsType.state),
        AlertUntil("AlertUntil", 15, "bigint", TablesFieldsType.longNumber),
        AlertReason("AlertReason", 16, "text", TablesFieldsType.text),
        JoinedCounter("JoinedCounter", 17, "boolean", TablesFieldsType.state),
        LockedIps("LockedIps", 18, "text", TablesFieldsType.stringList),
        pTime("pTime", 19, "bigint", TablesFieldsType.longNumber),
        Kits("Kits", 20, "text", TablesFieldsType.stringLongMap),
        Charges("Charges", 21, "text", TablesFieldsType.text),
        Cooldowns("Cooldowns", 22, "longtext", TablesFieldsType.text),
        Balance("Balance", 23, "double", TablesFieldsType.decimal),
        Notes("Notes", 24, "text", TablesFieldsType.stringList),
        Rank("Rank", 25, "text", TablesFieldsType.text),
        BannedUntil("BannedUntil", 26, "bigint", TablesFieldsType.longNumber),
        BannedAt("BannedAt", 27, "bigint", TablesFieldsType.longNumber),
        BannedBy("BannedBy", 28, "text", TablesFieldsType.text),
        BanReason("BanReason", 29, "text", TablesFieldsType.text),
        Ignores("Ignores", 30, "text", TablesFieldsType.text),
        Vanish("Vanish", 31, "text", TablesFieldsType.text),
        Economy("Economy", 32, "text", TablesFieldsType.stringDoubleMap),
        Mail("Mail", 33, "text", TablesFieldsType.stringList),
        FlightCharge("FlightCharge", 34, "double", TablesFieldsType.decimal),
        UserMeta("UserMeta", 35, "text", TablesFieldsType.stringStringMap),
        Flying("Flying", 36, "boolean", TablesFieldsType.state),
        Votifier("Votifier", 37, "int", TablesFieldsType.number),
        Jail("Jail", 38, "text", TablesFieldsType.text),
        JailedUntil("JailedUntil", 39, "bigint", TablesFieldsType.longNumber),
        FakeAccount("FakeAccount", 40, "boolean", TablesFieldsType.state),
        PlaytimeOptimized("PlaytimeOptimized", 41, "bigint", TablesFieldsType.longNumber),
        flightChargeEnabled("flightChargeEnabled", 42, "boolean", TablesFieldsType.state),
        JailReason("JailReason", 43, "text", TablesFieldsType.text),
        Skin("Skin", 44, "text", TablesFieldsType.text),
        Collision("Collision", 45, "boolean", TablesFieldsType.state),
        NamePrefix("NamePrefix", 46, "text", TablesFieldsType.text),
        NameSuffix("NameSuffix", 47, "text", TablesFieldsType.text),
        Warnings("Warnings", 48, "text", TablesFieldsType.stringLongMap),
        NameColor("NameColor", 49, "text", TablesFieldsType.text),
        Muted("Muted", 50, "text", TablesFieldsType.text),
        AFRecharge("AFRecharge", 51, "text", TablesFieldsType.text),
        DisplayName("DisplayName", 52, "text", TablesFieldsType.text),
        Options("Options", 53, "text", TablesFieldsType.text);

        private String type;
        private TablesFieldsType fieldType;

        private UserTablesFields(final String name, final int ordinal, final String type, final TablesFieldsType fieldType) {
            this.type = type;
            this.fieldType = fieldType;
        }

        public String getCollumn() {
            return this.name();
        }

        public String getType() {
            return this.type;
        }

        public TablesFieldsType getFieldType() {
            return this.fieldType;
        }
    }

    public enum mysqltypes
    {
        old("old", 0),
        MySQL("MySQL", 1),
        MariaDB("MariaDB", 2);

        private mysqltypes(final String name, final int ordinal) {
        }
    }

    public enum DBTables
    {
        UserTable("UserTable", 0, "users", "CREATE TABLE `[tableName]` (`id` int NOT NULL AUTO_INCREMENT PRIMARY KEY[fields]);", "CREATE TABLE `[tableName]` (`id` INTEGER PRIMARY KEY AUTOINCREMENT[fields]);"),
        InvTable("InvTable", 1, "inventories", "CREATE TABLE `[tableName]` (`id` int NOT NULL AUTO_INCREMENT PRIMARY KEY[fields]);", "CREATE TABLE `[tableName]` (`id` INTEGER PRIMARY KEY AUTOINCREMENT[fields]);"),
        PlayTime("PlayTime", 2, "playtime", "CREATE TABLE `[tableName]` (`id` int NOT NULL AUTO_INCREMENT PRIMARY KEY[fields]);", "CREATE TABLE `[tableName]` (`id` INTEGER PRIMARY KEY AUTOINCREMENT[fields]);"),
        PlayTimeReward("PlayTimeReward", 3, "playtimereward", "CREATE TABLE `[tableName]` (`id` int NOT NULL AUTO_INCREMENT PRIMARY KEY[fields]);", "CREATE TABLE `[tableName]` (`id` INTEGER PRIMARY KEY AUTOINCREMENT[fields]);");

        private String mySQL;
        private String sQlite;
        private String tableName;

        private DBTables(final String name, final int ordinal, final String tableName, final String mySQL, final String sQlite) {
            this.tableName = tableName;
            this.mySQL = mySQL;
            this.sQlite = sQlite;
        }

        private String getQR() {
            switch (DBDAO.dbType) {
                case MySQL: {
                    return this.mySQL.replace("[tableName]", String.valueOf(DBDAO.prefix) + this.tableName);
                }
                case SqLite: {
                    return this.sQlite.replace("[tableName]", this.tableName);
                }
                default: {
                    return "";
                }
            }
        }

        public String getQuery() {
            String replacement = "";
            switch (this) {
                case UserTable: {
                    UserTablesFields[] values2;
                    for (int length2 = (values2 = UserTablesFields.values()).length, j = 0; j < length2; ++j) {
                        final UserTablesFields userTablesFields = values2[j];
                        replacement = String.valueOf(replacement) + ", `" + userTablesFields.getCollumn() + "` " + userTablesFields.getType();
                    }
                    break;
                }
            }
            return this.getQR().replace("[fields]", replacement);
        }

        public String getUpdateQuery() {
            switch (this) {
                case UserTable: {
                    String str2 = "";
                    UserTablesFields[] values2;
                    for (int length2 = (values2 = UserTablesFields.values()).length, j = 0; j < length2; ++j) {
                        final UserTablesFields userTablesFields = values2[j];
                        if (userTablesFields != UserTablesFields.player_uuid) {
                            if (!str2.isEmpty()) {
                                str2 = String.valueOf(str2) + ", ";
                            }
                            str2 = String.valueOf(str2) + "`" + userTablesFields.getCollumn() + "` = ?";
                        }
                    }
                    return "UPDATE `" + this.getTableName() + "` SET " + str2 + " WHERE `id` = ?;";
                }
                default: {
                    return "";
                }
            }
        }

        public String getInsertQuery() {
            String str = "";
            String str2 = "";
            switch (this) {
                case UserTable: {
                    UserTablesFields[] values;
                    for (int length = (values = UserTablesFields.values()).length, i = 0; i < length; ++i) {
                        final UserTablesFields userTablesFields = values[i];
                        if (!str.isEmpty()) {
                            str = String.valueOf(str) + ", ";
                        }
                        str = String.valueOf(str) + "`" + userTablesFields.getCollumn() + "`";
                        if (!str2.isEmpty()) {
                            str2 = String.valueOf(str2) + ", ";
                        }
                        str2 = String.valueOf(str2) + "?";
                    }
                    break;
                }

            }
            switch (this) {
                case UserTable: {
                    if (DBDAO.dbType.equals(DBManager.DataBaseType.SqLite)) {
                        str = "INSERT INTO `" + this.getTableName() + "` (" + str + ") VALUES (" + str2 + ");";
                        break;
                    }
                    switch (DBDAO.Format) {
                        case MariaDB: {
                            str = "INSERT INTO `" + this.getTableName() + "` (" + str + ") SELECT " + str2 + " FROM dual WHERE NOT EXISTS (SELECT " + UserTablesFields.player_uuid.getCollumn() + " FROM " + this.getTableName() + " WHERE " + UserTablesFields.player_uuid.getCollumn() + " = ?) LIMIT 1;";
                            break;
                        }
                        case MySQL: {
                            str = "INSERT INTO `" + this.getTableName() + "` (" + str + ") SELECT " + str2 + " WHERE NOT EXISTS (SELECT " + UserTablesFields.player_uuid.getCollumn() + " FROM " + this.getTableName() + " WHERE " + UserTablesFields.player_uuid.getCollumn() + " = ?) LIMIT 1;";
                            break;
                        }
                        case old: {
                            str = "INSERT INTO `" + this.getTableName() + "` (" + str + ") VALUES (" + str2 + ");";
                            break;
                        }
                    }
                    break;
                }
                default: {
                    str = "INSERT INTO `" + this.getTableName() + "` (" + str + ") VALUES (" + str2 + ");";
                    break;
                }
            }
            return str;
        }

        public String getTableName() {
            return String.valueOf(DBDAO.prefix) + this.tableName;
        }
    }


}
