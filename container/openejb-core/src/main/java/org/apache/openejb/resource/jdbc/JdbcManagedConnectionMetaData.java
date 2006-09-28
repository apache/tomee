/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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