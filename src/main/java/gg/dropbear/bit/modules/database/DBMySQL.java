package gg.dropbear.bit.modules.database;

import gg.dropbear.bit.Main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMySQL extends DBDAO {

    private Main plugin;
    private String database;
    private static String path;

    static {
        DBMySQL.path = "";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DBMySQL.path = "com.mysql.cj.jdbc.Driver";
        }
        catch (Throwable t) {
            DBMySQL.path = "com.mysql.jdbc.Driver";
        }
        final String[] split = System.getProperty("java.runtime.version").split("\\.|_|-b");
        String str;
        try {
            str = split[0];
        }
        catch (Throwable t2) {
            str = System.getProperty("java.version").split("_")[0].split(".")[1];
        }
        Main.getInstance().consoleMessage("Detected Java" + str);
    }

    public DBMySQL(final Main plugin, final String str, final String s, final String s2, final String s3, final String s4, final boolean b, final boolean b2, final boolean b3) {
        super(plugin, DBMySQL.path, "jdbc:mysql://" + str + "/" + s + "?autoReconnect=" + b + "&useSSL=" + b3 + "&verifyServerCertificate=" + b2 + "&useUnicode=true&characterEncoding=utf8mb4_unicode_ci", s2, s3, s4);
        this.plugin = plugin;
        this.database = s;
        this.setDbType(DBManager.DataBaseType.MySQL);
    }

    public void initialize() {
        this.setUp();
    }

    public DBMySQL initialize(final Main plugin, final String s, final String s2, final String s3, final String s4, final String s5, final boolean b, final boolean b2, final boolean b3) {
        this.plugin = plugin;
        final DBMySQL dbMySQL = new DBMySQL(plugin, s, s2, s3, s4, s5, b, b2, b3);
        dbMySQL.setUp();
        return dbMySQL;
    }

    @Override
    public Statement prepareStatement(final String s) {
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return null;
        }
        Statement prepareStatement = null;
        try {
            prepareStatement = connection.prepareStatement(s);
        }
        catch (SQLException | NumberFormatException ex) {
            final Throwable t = null;
            t.printStackTrace();
        }
        return prepareStatement;
    }

    @Override
    public boolean createTable(final String s) {
        this.plugin.consoleMessage(s);
        Statement statement = null;
        if (s == null || s.equals("")) {
            this.plugin.consoleMessage("&cCould not create table: query is empty or null.");
            return false;
        }
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            statement = connection.createStatement();
            statement.execute(s);
            statement.close();
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not create table, SQLException: " + ex.getMessage());
            this.close(statement);
            return false;
        }
        finally {
            this.close(statement);
        }
        this.close(statement);
        return true;
    }

    @Override
    public boolean isTable(final String str) {
        Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            statement = connection.createStatement();
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not check if its table, SQLException: " + ex.getMessage());
            return false;
        }
        finally {
            this.close(statement);
        }
        this.close(statement);
        try {
            return connection.getMetaData().getTables(this.database, null, str, new String[] { "TABLE" }).next();
        }
        catch (SQLException ex2) {
            this.plugin.consoleMessage("Not a table |SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ='" + str + "';" + "|");
            PreparedStatement prepareStatement = null;
            ResultSet executeQuery = null;
            try {
                prepareStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ='" + str + "';");
                executeQuery = prepareStatement.executeQuery();
                if (executeQuery.next()) {
                    executeQuery.close();
                    prepareStatement.close();
                    return true;
                }
                return false;
            }
            catch (SQLException ex3) {
                this.plugin.consoleMessage("Not a table |SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME ='" + str + "';" + "|");
                return false;
            }
            finally {
                this.close(statement);
                this.close(prepareStatement);
                this.close(executeQuery);
            }
        }
    }

    @Override
    public boolean isCollumn(final String s, final String s2) {
        Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            statement = connection.createStatement();
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not check if its collumn, SQLException: " + ex.getMessage());
            return false;
        }
        finally {
            this.close(statement);
        }
        this.close(statement);
        ResultSet columns = null;
        try {
            columns = connection.getMetaData().getColumns(this.database, null, s, s2);
            if (columns.next()) {
                columns.close();
                return true;
            }
            return false;
        }
        catch (SQLException ex2) {}
        finally {
            this.close(columns);
        }
        return false;
    }

    @Override
    public boolean addCollumn(final String s, final String s2, final String s3) {
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        Statement statement;
        try {
            statement = connection.createStatement();
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not add new collumn, SQLException: " + ex.getMessage());
            return false;
        }
        try {
            this.plugin.consoleMessage("Creating culumn |" + s + " " + s2 + " " + s3 + "|");
            statement.executeUpdate("ALTER TABLE `" + s + "` ADD COLUMN `" + s2 + "` " + s3 + ";");
            statement.close();
            return true;
        }
        catch (SQLException ex2) {
            this.close(statement);
            ex2.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean truncate(final String s) {
        Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            if (!this.isTable(s)) {
                this.plugin.consoleMessage("&cTable \"" + s + "\" does not exist.");
                return false;
            }
            statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM " + s + ";");
            statement.close();
            return true;
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not wipe table, SQLException: " + ex.getMessage());
            this.close(statement);
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public String getTableCharSet(final DBTables dbTables) {
        final Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return null;
        }
        try {
            if (!this.isTable(dbTables.getTableName())) {
                this.plugin.consoleMessage("&cTable \"" + dbTables.getTableName() + "\" does not exist.");
                return null;
            }
            final PreparedStatement prepareStatement = connection.prepareStatement("SELECT TABLE_SCHEMA, TABLE_NAME, TABLE_COLLATION, ROW_FORMAT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='" + dbTables.getTableName() + "';");
            final ResultSet executeQuery = prepareStatement.executeQuery();
            String string = null;
            if (executeQuery.next()) {
                string = executeQuery.getString(3);
            }
            executeQuery.close();
            prepareStatement.close();
            return string;
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not check table chatser, SQLException: " + ex.getMessage());
            this.close(statement);
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getTableRowFormat(final DBTables dbTables) {
        final Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return null;
        }
        try {
            if (!this.isTable(dbTables.getTableName())) {
                this.plugin.consoleMessage("&cTable \"" + dbTables.getTableName() + "\" does not exist.");
                return null;
            }
            final PreparedStatement prepareStatement = connection.prepareStatement("SELECT ROW_FORMAT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME ='" + dbTables.getTableName() + "';");
            final ResultSet executeQuery = prepareStatement.executeQuery();
            String string = null;
            if (executeQuery.next()) {
                string = executeQuery.getString(1);
            }
            executeQuery.close();
            prepareStatement.close();
            return string;
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not check table chatser, SQLException: " + ex.getMessage());
            this.close(statement);
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean convertTableToUTF8(final DBTables dbTables) {
        Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            if (!this.isTable(dbTables.getTableName())) {
                this.plugin.consoleMessage("&cTable \"" + dbTables.getTableName() + "\" does not exist.");
                return false;
            }
            statement = connection.createStatement();
            statement.executeUpdate("ALTER TABLE `" + dbTables.getTableName() + "` CONVERT TO CHARSET utf8;");
        }
        catch (SQLException ex) {
            this.plugin.consoleMessage("&cCould not convert table, SQLException: " + ex.getMessage());
            ex.printStackTrace();
            return true;
        }
        finally {
            this.close(statement);
        }
        this.close(statement);
        return true;
    }

    @Override
    public boolean convertTableRowFormat(final DBTables dbTables) {
        Statement statement = null;
        final DBConnection connection = this.getConnection();
        if (connection == null) {
            return false;
        }
        try {
            if (!this.isTable(dbTables.getTableName())) {
                this.plugin.consoleMessage("&cTable \"" + dbTables.getTableName() + "\" does not exist.");
                return false;
            }
            statement = connection.createStatement();
            statement.executeUpdate("ALTER TABLE " + dbTables.getTableName() + " ROW_FORMAT=DYNAMIC;");
        }
        catch (SQLException ex) {
            return true;
        }
        finally {
            this.close(statement);
        }
        this.close(statement);
        return true;
    }
}
