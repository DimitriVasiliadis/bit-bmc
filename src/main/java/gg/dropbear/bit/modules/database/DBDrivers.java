package gg.dropbear.bit.modules.database;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DBDrivers implements Driver {
    private Driver driver;

    public DBDrivers(final Driver driver) {
        this.driver = driver;
    }

    @Override
    public Connection connect(final String s, final Properties properties) throws SQLException {
        return this.driver.connect(s, properties);
    }

    @Override
    public boolean acceptsURL(final String s) throws SQLException {
        return this.driver.acceptsURL(s);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String s, final Properties properties) throws SQLException {
        return this.driver.getPropertyInfo(s, properties);
    }

    @Override
    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.driver.getParentLogger();
    }
}
