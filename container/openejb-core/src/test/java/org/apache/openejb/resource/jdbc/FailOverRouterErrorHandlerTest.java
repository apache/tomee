/*
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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.resource.jdbc.router.FailOverRouter;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Classes
@RunWith(ApplicationComposer.class)
public class FailOverRouterErrorHandlerTest {
    @Resource(name = "routedDs")
    private DataSource failover;

    @Resource(name = "router")
    private FailOverRouter router;

    @Resource(name = "errorHandler")
    private SimpleHandler handler;

    @Test
    public void test() throws SQLException {
        { // all is fine
            ControllableDriver.actives.add("1");
            ControllableDriver.actives.add("2");
            try (final Connection c = failover.getConnection()) {
                c.createStatement();
            }
            assertEquals(0, handler.errors.size());
        }

        { // failover
            ControllableDriver.actives.remove("1");
            try (final Connection c = failover.getConnection()) {
                c.createStatement();
            }

            assertEquals(1, handler.errors.size());
            final Map<String, Throwable> errors = handler.errors.iterator().next();
            assertTrue(errors.containsKey("delegate1"));
            assertTrue(SQLException.class.isInstance(errors.get("delegate1")));
            assertEquals("failed", errors.get("delegate1").getMessage());
        }
    }

    @Configuration
    public Properties configuration() {
        // datasources
        return new PropertiesBuilder()

            // router
            .property("router", "new://Resource?class-name=" + FailOverRouter.class.getName())
            .property("router.datasourceNames", "delegate1,delegate2")
            .property("router.errorHandlerInstance", "@errorHandler")

            // routed DS
            .property("routedDs", "new://Resource?provider=RoutedDataSource&type=DataSource")
            .property("routedDs.router", "router")

            // delegates
            .property("delegate1", "new://Resource?type=DataSource")
            .property("delegate1.JtaManaged", "false")
            .property("delegate1.JdbcDriver", ControllableDriver.class.getName())
            .property("delegate1.JdbcUrl", "1")
            .property("delegate1.TestOnBorrow", "true") // otherwise connections can be created without being valid depending the pool
            .property("delegate1.validationQuery", "select 1")

            .property("delegate2", "new://Resource?type=DataSource")
            .property("delegate2.JtaManaged", "false")
            .property("delegate2.JdbcDriver", ControllableDriver.class.getName())
            .property("delegate2.JdbcUrl", "2")
            .property("delegate2.TestOnBorrow", "true")
            .property("delegate2.validationQuery", "select 1")

            // error handler
            .property("errorHandler", "new://Resource?class-name=" + SimpleHandler.class.getName())

            .build();
    }

    public static class SimpleHandler implements FailOverRouter.ErrorHandler {
        private final Collection<Map<String, Throwable>> errors = new ArrayList<>();

        @Override
        public void onError(final Map<String, Throwable> errorByFailingDataSource, final FailOverRouter.DataSourceHolder finallyUsedOrNull) {
            errors.add(errorByFailingDataSource);
        }
    }

    public static class ControllableDriver implements Driver {
        static final Collection<String> actives = new CopyOnWriteArraySet<>();

        @Override
        public Connection connect(final String url, final Properties info) throws SQLException {
            checkActive(url);
            return new Connection() {
                @Override
                public Statement createStatement() throws SQLException {
                    checkActive(url);
                    return new Statement() {
                        @Override
                        public ResultSet executeQuery(final String sql) throws SQLException {
                            return newResultSet(sql);
                        }

                        @Override
                        public int executeUpdate(final String sql) throws SQLException {
                            return 0;
                        }

                        @Override
                        public void close() throws SQLException {

                        }

                        @Override
                        public int getMaxFieldSize() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setMaxFieldSize(final int max) throws SQLException {

                        }

                        @Override
                        public int getMaxRows() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setMaxRows(final int max) throws SQLException {

                        }

                        @Override
                        public void setEscapeProcessing(final boolean enable) throws SQLException {

                        }

                        @Override
                        public int getQueryTimeout() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setQueryTimeout(final int seconds) throws SQLException {

                        }

                        @Override
                        public void cancel() throws SQLException {

                        }

                        @Override
                        public SQLWarning getWarnings() throws SQLException {
                            return null;
                        }

                        @Override
                        public void clearWarnings() throws SQLException {

                        }

                        @Override
                        public void setCursorName(final String name) throws SQLException {

                        }

                        @Override
                        public boolean execute(final String sql) throws SQLException {
                            return false;
                        }

                        @Override
                        public ResultSet getResultSet() throws SQLException {
                            return null;
                        }

                        @Override
                        public int getUpdateCount() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean getMoreResults() throws SQLException {
                            return false;
                        }

                        @Override
                        public void setFetchDirection(final int direction) throws SQLException {

                        }

                        @Override
                        public int getFetchDirection() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setFetchSize(final int rows) throws SQLException {

                        }

                        @Override
                        public int getFetchSize() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getResultSetConcurrency() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getResultSetType() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void addBatch(final String sql) throws SQLException {

                        }

                        @Override
                        public void clearBatch() throws SQLException {

                        }

                        @Override
                        public int[] executeBatch() throws SQLException {
                            return new int[0];
                        }

                        @Override
                        public Connection getConnection() throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean getMoreResults(final int current) throws SQLException {
                            return false;
                        }

                        @Override
                        public ResultSet getGeneratedKeys() throws SQLException {
                            return null;
                        }

                        @Override
                        public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean execute(final String sql, final String[] columnNames) throws SQLException {
                            return false;
                        }

                        @Override
                        public int getResultSetHoldability() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean isClosed() throws SQLException {
                            return false;
                        }

                        @Override
                        public void setPoolable(final boolean poolable) throws SQLException {

                        }

                        @Override
                        public boolean isPoolable() throws SQLException {
                            return false;
                        }

                        @Override
                        public void closeOnCompletion() throws SQLException {

                        }

                        @Override
                        public boolean isCloseOnCompletion() throws SQLException {
                            return false;
                        }

                        @Override
                        public <T> T unwrap(final Class<T> iface) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
                            return false;
                        }
                    };
                }

                @Override
                public PreparedStatement prepareStatement(final String sql) throws SQLException {
                    checkActive(url);
                    return new PreparedStatement() {
                        @Override
                        public ResultSet executeQuery() throws SQLException {
                            checkActive(url);
                            return newResultSet(sql);
                        }

                        @Override
                        public int executeUpdate() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setNull(int parameterIndex, int sqlType) throws SQLException {

                        }

                        @Override
                        public void setBoolean(int parameterIndex, boolean x) throws SQLException {

                        }

                        @Override
                        public void setByte(int parameterIndex, byte x) throws SQLException {

                        }

                        @Override
                        public void setShort(int parameterIndex, short x) throws SQLException {

                        }

                        @Override
                        public void setInt(int parameterIndex, int x) throws SQLException {

                        }

                        @Override
                        public void setLong(int parameterIndex, long x) throws SQLException {

                        }

                        @Override
                        public void setFloat(int parameterIndex, float x) throws SQLException {

                        }

                        @Override
                        public void setDouble(int parameterIndex, double x) throws SQLException {

                        }

                        @Override
                        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {

                        }

                        @Override
                        public void setString(int parameterIndex, String x) throws SQLException {

                        }

                        @Override
                        public void setBytes(int parameterIndex, byte[] x) throws SQLException {

                        }

                        @Override
                        public void setDate(int parameterIndex, Date x) throws SQLException {

                        }

                        @Override
                        public void setTime(int parameterIndex, Time x) throws SQLException {

                        }

                        @Override
                        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {

                        }

                        @Override
                        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void clearParameters() throws SQLException {

                        }

                        @Override
                        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {

                        }

                        @Override
                        public void setObject(int parameterIndex, Object x) throws SQLException {

                        }

                        @Override
                        public boolean execute() throws SQLException {
                            return false;
                        }

                        @Override
                        public void addBatch() throws SQLException {

                        }

                        @Override
                        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {

                        }

                        @Override
                        public void setRef(int parameterIndex, Ref x) throws SQLException {

                        }

                        @Override
                        public void setBlob(int parameterIndex, Blob x) throws SQLException {

                        }

                        @Override
                        public void setClob(int parameterIndex, Clob x) throws SQLException {

                        }

                        @Override
                        public void setArray(int parameterIndex, Array x) throws SQLException {

                        }

                        @Override
                        public ResultSetMetaData getMetaData() throws SQLException {
                            return null;
                        }

                        @Override
                        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {

                        }

                        @Override
                        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {

                        }

                        @Override
                        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {

                        }

                        @Override
                        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {

                        }

                        @Override
                        public void setURL(int parameterIndex, URL x) throws SQLException {

                        }

                        @Override
                        public ParameterMetaData getParameterMetaData() throws SQLException {
                            return null;
                        }

                        @Override
                        public void setRowId(int parameterIndex, RowId x) throws SQLException {

                        }

                        @Override
                        public void setNString(int parameterIndex, String value) throws SQLException {

                        }

                        @Override
                        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

                        }

                        @Override
                        public void setNClob(int parameterIndex, NClob value) throws SQLException {

                        }

                        @Override
                        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

                        }

                        @Override
                        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

                        }

                        @Override
                        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {

                        }

                        @Override
                        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

                        }

                        @Override
                        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

                        }

                        @Override
                        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

                        }

                        @Override
                        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

                        }

                        @Override
                        public void setClob(int parameterIndex, Reader reader) throws SQLException {

                        }

                        @Override
                        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

                        }

                        @Override
                        public void setNClob(int parameterIndex, Reader reader) throws SQLException {

                        }

                        @Override
                        public ResultSet executeQuery(String sql) throws SQLException {
                            return null;
                        }

                        @Override
                        public int executeUpdate(String sql) throws SQLException {
                            return 0;
                        }

                        @Override
                        public void close() throws SQLException {

                        }

                        @Override
                        public int getMaxFieldSize() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setMaxFieldSize(int max) throws SQLException {

                        }

                        @Override
                        public int getMaxRows() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setMaxRows(int max) throws SQLException {

                        }

                        @Override
                        public void setEscapeProcessing(boolean enable) throws SQLException {

                        }

                        @Override
                        public int getQueryTimeout() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setQueryTimeout(int seconds) throws SQLException {

                        }

                        @Override
                        public void cancel() throws SQLException {

                        }

                        @Override
                        public SQLWarning getWarnings() throws SQLException {
                            return null;
                        }

                        @Override
                        public void clearWarnings() throws SQLException {

                        }

                        @Override
                        public void setCursorName(String name) throws SQLException {

                        }

                        @Override
                        public boolean execute(String sql) throws SQLException {
                            return false;
                        }

                        @Override
                        public ResultSet getResultSet() throws SQLException {
                            return null;
                        }

                        @Override
                        public int getUpdateCount() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean getMoreResults() throws SQLException {
                            return false;
                        }

                        @Override
                        public void setFetchDirection(int direction) throws SQLException {

                        }

                        @Override
                        public int getFetchDirection() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setFetchSize(int rows) throws SQLException {

                        }

                        @Override
                        public int getFetchSize() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getResultSetConcurrency() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getResultSetType() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void addBatch(String sql) throws SQLException {

                        }

                        @Override
                        public void clearBatch() throws SQLException {

                        }

                        @Override
                        public int[] executeBatch() throws SQLException {
                            return new int[0];
                        }

                        @Override
                        public Connection getConnection() throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean getMoreResults(int current) throws SQLException {
                            return false;
                        }

                        @Override
                        public ResultSet getGeneratedKeys() throws SQLException {
                            return null;
                        }

                        @Override
                        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean execute(String sql, String[] columnNames) throws SQLException {
                            return false;
                        }

                        @Override
                        public int getResultSetHoldability() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean isClosed() throws SQLException {
                            return false;
                        }

                        @Override
                        public void setPoolable(boolean poolable) throws SQLException {

                        }

                        @Override
                        public boolean isPoolable() throws SQLException {
                            return false;
                        }

                        @Override
                        public void closeOnCompletion() throws SQLException {

                        }

                        @Override
                        public boolean isCloseOnCompletion() throws SQLException {
                            return false;
                        }

                        @Override
                        public <T> T unwrap(Class<T> iface) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean isWrapperFor(Class<?> iface) throws SQLException {
                            return false;
                        }
                    };
                }

                private ResultSet newResultSet(final String sql) {
                    return "select 1".equals(sql) ? new ResultSet() {
                        private final AtomicBoolean val = new AtomicBoolean(true);

                        @Override
                        public boolean next() throws SQLException {
                            return val.getAndSet(false);
                        }

                        @Override
                        public void close() throws SQLException {

                        }

                        @Override
                        public boolean wasNull() throws SQLException {
                            return false;
                        }

                        @Override
                        public String getString(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean getBoolean(int columnIndex) throws SQLException {
                            return false;
                        }

                        @Override
                        public byte getByte(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public short getShort(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getInt(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public long getLong(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public float getFloat(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public double getDouble(int columnIndex) throws SQLException {
                            return 0;
                        }

                        @Override
                        public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
                            return null;
                        }

                        @Override
                        public byte[] getBytes(int columnIndex) throws SQLException {
                            return new byte[0];
                        }

                        @Override
                        public Date getDate(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Time getTime(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Timestamp getTimestamp(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getAsciiStream(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getUnicodeStream(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getBinaryStream(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public String getString(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean getBoolean(String columnLabel) throws SQLException {
                            return false;
                        }

                        @Override
                        public byte getByte(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public short getShort(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getInt(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public long getLong(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public float getFloat(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public double getDouble(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
                            return null;
                        }

                        @Override
                        public byte[] getBytes(String columnLabel) throws SQLException {
                            return new byte[0];
                        }

                        @Override
                        public Date getDate(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Time getTime(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Timestamp getTimestamp(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getAsciiStream(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getUnicodeStream(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public InputStream getBinaryStream(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public SQLWarning getWarnings() throws SQLException {
                            return null;
                        }

                        @Override
                        public void clearWarnings() throws SQLException {

                        }

                        @Override
                        public String getCursorName() throws SQLException {
                            return null;
                        }

                        @Override
                        public ResultSetMetaData getMetaData() throws SQLException {
                            return null;
                        }

                        @Override
                        public Object getObject(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Object getObject(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public int findColumn(String columnLabel) throws SQLException {
                            return 0;
                        }

                        @Override
                        public Reader getCharacterStream(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Reader getCharacterStream(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean isBeforeFirst() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean isAfterLast() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean isFirst() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean isLast() throws SQLException {
                            return false;
                        }

                        @Override
                        public void beforeFirst() throws SQLException {

                        }

                        @Override
                        public void afterLast() throws SQLException {

                        }

                        @Override
                        public boolean first() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean last() throws SQLException {
                            return false;
                        }

                        @Override
                        public int getRow() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean absolute(int row) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean relative(int rows) throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean previous() throws SQLException {
                            return false;
                        }

                        @Override
                        public void setFetchDirection(int direction) throws SQLException {

                        }

                        @Override
                        public int getFetchDirection() throws SQLException {
                            return 0;
                        }

                        @Override
                        public void setFetchSize(int rows) throws SQLException {

                        }

                        @Override
                        public int getFetchSize() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getType() throws SQLException {
                            return 0;
                        }

                        @Override
                        public int getConcurrency() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean rowUpdated() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean rowInserted() throws SQLException {
                            return false;
                        }

                        @Override
                        public boolean rowDeleted() throws SQLException {
                            return false;
                        }

                        @Override
                        public void updateNull(int columnIndex) throws SQLException {

                        }

                        @Override
                        public void updateBoolean(int columnIndex, boolean x) throws SQLException {

                        }

                        @Override
                        public void updateByte(int columnIndex, byte x) throws SQLException {

                        }

                        @Override
                        public void updateShort(int columnIndex, short x) throws SQLException {

                        }

                        @Override
                        public void updateInt(int columnIndex, int x) throws SQLException {

                        }

                        @Override
                        public void updateLong(int columnIndex, long x) throws SQLException {

                        }

                        @Override
                        public void updateFloat(int columnIndex, float x) throws SQLException {

                        }

                        @Override
                        public void updateDouble(int columnIndex, double x) throws SQLException {

                        }

                        @Override
                        public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

                        }

                        @Override
                        public void updateString(int columnIndex, String x) throws SQLException {

                        }

                        @Override
                        public void updateBytes(int columnIndex, byte[] x) throws SQLException {

                        }

                        @Override
                        public void updateDate(int columnIndex, Date x) throws SQLException {

                        }

                        @Override
                        public void updateTime(int columnIndex, Time x) throws SQLException {

                        }

                        @Override
                        public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

                        }

                        @Override
                        public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {

                        }

                        @Override
                        public void updateObject(int columnIndex, Object x) throws SQLException {

                        }

                        @Override
                        public void updateNull(String columnLabel) throws SQLException {

                        }

                        @Override
                        public void updateBoolean(String columnLabel, boolean x) throws SQLException {

                        }

                        @Override
                        public void updateByte(String columnLabel, byte x) throws SQLException {

                        }

                        @Override
                        public void updateShort(String columnLabel, short x) throws SQLException {

                        }

                        @Override
                        public void updateInt(String columnLabel, int x) throws SQLException {

                        }

                        @Override
                        public void updateLong(String columnLabel, long x) throws SQLException {

                        }

                        @Override
                        public void updateFloat(String columnLabel, float x) throws SQLException {

                        }

                        @Override
                        public void updateDouble(String columnLabel, double x) throws SQLException {

                        }

                        @Override
                        public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {

                        }

                        @Override
                        public void updateString(String columnLabel, String x) throws SQLException {

                        }

                        @Override
                        public void updateBytes(String columnLabel, byte[] x) throws SQLException {

                        }

                        @Override
                        public void updateDate(String columnLabel, Date x) throws SQLException {

                        }

                        @Override
                        public void updateTime(String columnLabel, Time x) throws SQLException {

                        }

                        @Override
                        public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {

                        }

                        @Override
                        public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {

                        }

                        @Override
                        public void updateObject(String columnLabel, Object x) throws SQLException {

                        }

                        @Override
                        public void insertRow() throws SQLException {

                        }

                        @Override
                        public void updateRow() throws SQLException {

                        }

                        @Override
                        public void deleteRow() throws SQLException {

                        }

                        @Override
                        public void refreshRow() throws SQLException {

                        }

                        @Override
                        public void cancelRowUpdates() throws SQLException {

                        }

                        @Override
                        public void moveToInsertRow() throws SQLException {

                        }

                        @Override
                        public void moveToCurrentRow() throws SQLException {

                        }

                        @Override
                        public Statement getStatement() throws SQLException {
                            return null;
                        }

                        @Override
                        public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
                            return null;
                        }

                        @Override
                        public Ref getRef(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Blob getBlob(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Clob getClob(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Array getArray(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
                            return null;
                        }

                        @Override
                        public Ref getRef(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Blob getBlob(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Clob getClob(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Array getArray(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Date getDate(int columnIndex, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public Date getDate(String columnLabel, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public Time getTime(int columnIndex, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public Time getTime(String columnLabel, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
                            return null;
                        }

                        @Override
                        public URL getURL(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public URL getURL(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public void updateRef(int columnIndex, Ref x) throws SQLException {

                        }

                        @Override
                        public void updateRef(String columnLabel, Ref x) throws SQLException {

                        }

                        @Override
                        public void updateBlob(int columnIndex, Blob x) throws SQLException {

                        }

                        @Override
                        public void updateBlob(String columnLabel, Blob x) throws SQLException {

                        }

                        @Override
                        public void updateClob(int columnIndex, Clob x) throws SQLException {

                        }

                        @Override
                        public void updateClob(String columnLabel, Clob x) throws SQLException {

                        }

                        @Override
                        public void updateArray(int columnIndex, Array x) throws SQLException {

                        }

                        @Override
                        public void updateArray(String columnLabel, Array x) throws SQLException {

                        }

                        @Override
                        public RowId getRowId(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public RowId getRowId(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public void updateRowId(int columnIndex, RowId x) throws SQLException {

                        }

                        @Override
                        public void updateRowId(String columnLabel, RowId x) throws SQLException {

                        }

                        @Override
                        public int getHoldability() throws SQLException {
                            return 0;
                        }

                        @Override
                        public boolean isClosed() throws SQLException {
                            return false;
                        }

                        @Override
                        public void updateNString(int columnIndex, String nString) throws SQLException {

                        }

                        @Override
                        public void updateNString(String columnLabel, String nString) throws SQLException {

                        }

                        @Override
                        public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

                        }

                        @Override
                        public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

                        }

                        @Override
                        public NClob getNClob(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public NClob getNClob(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public SQLXML getSQLXML(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public SQLXML getSQLXML(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

                        }

                        @Override
                        public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

                        }

                        @Override
                        public String getNString(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public String getNString(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public Reader getNCharacterStream(int columnIndex) throws SQLException {
                            return null;
                        }

                        @Override
                        public Reader getNCharacterStream(String columnLabel) throws SQLException {
                            return null;
                        }

                        @Override
                        public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

                        }

                        @Override
                        public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

                        }

                        @Override
                        public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

                        }

                        @Override
                        public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

                        }

                        @Override
                        public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

                        }

                        @Override
                        public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

                        }

                        @Override
                        public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

                        }

                        @Override
                        public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

                        }

                        @Override
                        public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

                        }

                        @Override
                        public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

                        }

                        @Override
                        public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

                        }

                        @Override
                        public void updateClob(int columnIndex, Reader reader) throws SQLException {

                        }

                        @Override
                        public void updateClob(String columnLabel, Reader reader) throws SQLException {

                        }

                        @Override
                        public void updateNClob(int columnIndex, Reader reader) throws SQLException {

                        }

                        @Override
                        public void updateNClob(String columnLabel, Reader reader) throws SQLException {

                        }

                        @Override
                        public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
                            return null;
                        }

                        @Override
                        public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
                            return null;
                        }

                        @Override
                        public <T> T unwrap(Class<T> iface) throws SQLException {
                            return null;
                        }

                        @Override
                        public boolean isWrapperFor(Class<?> iface) throws SQLException {
                            return false;
                        }
                    } : null;
                }

                @Override
                public CallableStatement prepareCall(final String sql) throws SQLException {
                    return null;
                }

                @Override
                public String nativeSQL(final String sql) throws SQLException {
                    return null;
                }

                @Override
                public void setAutoCommit(final boolean autoCommit) throws SQLException {

                }

                @Override
                public boolean getAutoCommit() throws SQLException {
                    return false;
                }

                @Override
                public void commit() throws SQLException {

                }

                @Override
                public void rollback() throws SQLException {

                }

                @Override
                public void close() throws SQLException {

                }

                @Override
                public boolean isClosed() throws SQLException {
                    return false;
                }

                @Override
                public DatabaseMetaData getMetaData() throws SQLException {
                    return null;
                }

                @Override
                public void setReadOnly(final boolean readOnly) throws SQLException {

                }

                @Override
                public boolean isReadOnly() throws SQLException {
                    return false;
                }

                @Override
                public void setCatalog(final String catalog) throws SQLException {

                }

                @Override
                public String getCatalog() throws SQLException {
                    return null;
                }

                @Override
                public void setTransactionIsolation(final int level) throws SQLException {

                }

                @Override
                public int getTransactionIsolation() throws SQLException {
                    return 0;
                }

                @Override
                public SQLWarning getWarnings() throws SQLException {
                    return null;
                }

                @Override
                public void clearWarnings() throws SQLException {

                }

                @Override
                public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
                    return null;
                }

                @Override
                public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
                    return null;
                }

                @Override
                public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
                    return null;
                }

                @Override
                public Map<String, Class<?>> getTypeMap() throws SQLException {
                    return null;
                }

                @Override
                public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {

                }

                @Override
                public void setHoldability(final int holdability) throws SQLException {

                }

                @Override
                public int getHoldability() throws SQLException {
                    return 0;
                }

                @Override
                public Savepoint setSavepoint() throws SQLException {
                    return null;
                }

                @Override
                public Savepoint setSavepoint(final String name) throws SQLException {
                    return null;
                }

                @Override
                public void rollback(final Savepoint savepoint) throws SQLException {

                }

                @Override
                public void releaseSavepoint(final Savepoint savepoint) throws SQLException {

                }

                @Override
                public Statement createStatement(final int resultSetType, final int resultSetConcurrency,
                                                 final int resultSetHoldability) throws SQLException {
                    return null;
                }

                @Override
                public PreparedStatement prepareStatement(final String sql, final int resultSetType,
                                                          final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
                    return null;
                }

                @Override
                public CallableStatement prepareCall(final String sql, final int resultSetType,
                                                     final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
                    return null;
                }

                @Override
                public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
                    return null;
                }

                @Override
                public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
                    return null;
                }

                @Override
                public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
                    return null;
                }

                @Override
                public Clob createClob() throws SQLException {
                    return null;
                }

                @Override
                public Blob createBlob() throws SQLException {
                    return null;
                }

                @Override
                public NClob createNClob() throws SQLException {
                    return null;
                }

                @Override
                public SQLXML createSQLXML() throws SQLException {
                    return null;
                }

                @Override
                public boolean isValid(int timeout) throws SQLException {
                    return false;
                }

                @Override
                public void setClientInfo(final String name, final String value) throws SQLClientInfoException {

                }

                @Override
                public void setClientInfo(final Properties properties) throws SQLClientInfoException {

                }

                @Override
                public String getClientInfo(final String name) throws SQLException {
                    return null;
                }

                @Override
                public Properties getClientInfo() throws SQLException {
                    return null;
                }

                @Override
                public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
                    return null;
                }

                @Override
                public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
                    return null;
                }

                @Override
                public void setSchema(final String schema) throws SQLException {

                }

                @Override
                public String getSchema() throws SQLException {
                    return null;
                }

                @Override
                public void abort(final Executor executor) throws SQLException {

                }

                @Override
                public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {

                }

                @Override
                public int getNetworkTimeout() throws SQLException {
                    return 0;
                }

                @Override
                public <T> T unwrap(final Class<T> iface) throws SQLException {
                    return null;
                }

                @Override
                public boolean isWrapperFor(final Class<?> iface) throws SQLException {
                    return false;
                }
            };
        }

        private void checkActive(String url) throws SQLException {
            if (!actives.contains(url)) {
                throw new SQLException("failed");
            }
        }

        @Override
        public boolean acceptsURL(final String url) throws SQLException {
            try {
                Integer.parseInt(url);
                return true;
            } catch (final NumberFormatException nfe) {
                return false;
            }
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 4;
        }

        @Override
        public int getMinorVersion() {
            return 2;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }
}
