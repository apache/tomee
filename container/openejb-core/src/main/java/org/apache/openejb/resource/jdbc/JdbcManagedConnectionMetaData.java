package org.apache.openejb.resource.jdbc;

public class JdbcManagedConnectionMetaData
        implements javax.resource.spi.ManagedConnectionMetaData {

    private java.sql.DatabaseMetaData sqlMetaData;

    public JdbcManagedConnectionMetaData(java.sql.DatabaseMetaData sqlMetaData) {
        this.sqlMetaData = sqlMetaData;
    }

    public java.lang.String getEISProductName()
            throws javax.resource.spi.ResourceAdapterInternalException {
        try {
            return "OpenEJB JDBC Connector (over " + sqlMetaData.getDriverName() + ")";
        } catch (java.sql.SQLException sqlE) {
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }

    public java.lang.String getEISProductVersion()
            throws javax.resource.spi.ResourceAdapterInternalException {
        try {
            return "Beta 1.0 (over " + sqlMetaData.getDriverVersion() + ")";
        } catch (java.sql.SQLException sqlE) {
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }

    public int getMaxConnections()
            throws javax.resource.spi.ResourceAdapterInternalException {
        try {
            return sqlMetaData.getMaxConnections();
        } catch (java.sql.SQLException sqlE) {
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }
    }

    public java.lang.String getUserName()
            throws javax.resource.spi.ResourceAdapterInternalException {
        try {
            return sqlMetaData.getUserName();
        } catch (java.sql.SQLException sqlE) {
            throw new javax.resource.spi.ResourceAdapterInternalException("MetaData is not available. Connection may be lost", ErrorCode.JDBC_0004);
        }

    }

}