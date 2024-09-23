package gg.dropbear.bit.modules.database;

import java.sql.*;

public class DBConnection {

    private Connection conn;

    public DBConnection(final Connection conn) {
        this.conn = conn;
    }

    public synchronized boolean isClosed() {
        try {
            return this.conn.isClosed();
        }
        catch (SQLException ex) {
            return true;
        }
    }

    public synchronized boolean isValid(final int n) {
        try {
            return this.conn.isValid(n);
        }
        catch (AbstractMethodError | SQLException abstractMethodError) {
            return true;
        }
    }

    public synchronized void closeConnection() throws SQLException {
        this.conn.close();
    }

    public synchronized Statement createStatement() throws SQLException {
        return this.conn.createStatement();
    }

    public synchronized PreparedStatement prepareStatement(final String s) throws SQLException {
        return this.conn.prepareStatement(s);
    }

    public synchronized PreparedStatement prepareStatement(final String s, final int n) throws SQLException {
        return this.conn.prepareStatement(s, n);
    }

    public synchronized void setAutoCommit(final Boolean b) throws SQLException {
        this.conn.setAutoCommit(b);
    }

    public synchronized void commit() throws SQLException {
        this.conn.commit();
    }

    public synchronized DatabaseMetaData getMetaData() throws SQLException {
        return this.conn.getMetaData();
    }
}
