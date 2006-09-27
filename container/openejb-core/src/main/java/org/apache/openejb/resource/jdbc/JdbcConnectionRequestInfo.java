package org.apache.openejb.resource.jdbc;

import javax.resource.spi.ConnectionRequestInfo;

public class JdbcConnectionRequestInfo implements ConnectionRequestInfo {

    private String userName;
    private String password;
    private String jdbcDriver;
    private String jdbcUrl;
    private int hashCode;

    public JdbcConnectionRequestInfo(String userName, String password, String jdbcDriver, String jdbcUrl) {
        this.userName = userName;
        this.password = password;
        this.jdbcDriver = jdbcDriver;
        this.jdbcUrl = jdbcUrl;
    }

    protected String getUserName() {
        return userName;
    }

    protected String getPassword() {
        return password;
    }

    protected String getJdbcDriver() {
        return jdbcDriver;
    }

    protected String getJdbcUrl() {
        return jdbcUrl;
    }

    public boolean equals(java.lang.Object other) {
        if (other instanceof JdbcConnectionRequestInfo &&
                ((JdbcConnectionRequestInfo) other).getUserName().equals(userName) &&
                ((JdbcConnectionRequestInfo) other).getPassword().equals(password) &&
                ((JdbcConnectionRequestInfo) other).getJdbcDriver().equals(jdbcDriver) &&
                ((JdbcConnectionRequestInfo) other).getJdbcUrl().equals(jdbcUrl))
            return true;
        else
            return false;
    }

    public int hashCode() {
        if (hashCode != 0) return hashCode;
        hashCode = jdbcDriver.hashCode() ^ jdbcUrl.hashCode() ^ userName.hashCode() ^ password.hashCode();
        return hashCode;
    }

}