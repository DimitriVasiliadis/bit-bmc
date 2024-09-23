package gg.dropbear.bit.modules.database;

import gg.dropbear.bit.Main;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionPool {
    private DBConnection connection;
    private String url;
    private String username;
    private String password;

    public DBConnectionPool(final String name, final String url, final String username, final String password) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        DriverManager.registerDriver(new DBDrivers((Driver)Class.forName(name, true, Main.getInstance().getDBClassloader()).newInstance()));
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public synchronized DBConnection getConnection() throws SQLException {
        Label_0044: {
            if (this.connection != null) {
                if (!this.connection.isClosed()) {
                    if (this.connection.isValid(1)) {
                        break Label_0044;
                    }
                }
                try {
                    this.connection.closeConnection();
                }
                catch (SQLException ex) {}
                this.connection = null;
            }
        }
        if (this.connection == null) {
            this.connection = new DBConnection(DriverManager.getConnection(this.url, this.username, this.password));
        }
        return this.connection;
    }

    public synchronized void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.closeConnection();
            }
            catch (SQLException ex) {}
        }
    }
}
