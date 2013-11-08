/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.lib.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.openjpa.lib.util.Closeable;

/**
 * Wrapper around an existing data source. Subclasses can override the
 * methods whose behavior they mean to change. The <code>equals</code> and
 * <code>hashCode</code> methods pass through to the base underlying data store.
 *
 * @author Abe White
 */
public class DelegatingDataSource implements DataSource, Closeable {

    private final DataSource _ds;
    private final DelegatingDataSource _del;

    /**
     * Constructor. Supply wrapped data source.
     */
    public DelegatingDataSource(DataSource ds) {
        _ds = ds;

        if (_ds instanceof DelegatingDataSource)
            _del = (DelegatingDataSource) _ds;
        else
            _del = null;
    }

    /**
     * Return the wrapped data source.
     */
    public DataSource getDelegate() {
        return _ds;
    }

    /**
     * Return the inner-most wrapped delegate.
     */
    public DataSource getInnermostDelegate() {
        return (_del == null) ? _ds : _del.getInnermostDelegate();
    }

    public int hashCode() {
        return getInnermostDelegate().hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingDataSource)
            other = ((DelegatingDataSource) other).getInnermostDelegate();
        return getInnermostDelegate().equals(other);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("datasource "). append(hashCode());
        appendInfo(buf);
        return buf.toString();
    }

    protected void appendInfo(StringBuffer buf) {
        if (_del != null)
            _del.appendInfo(buf);
    }

    public PrintWriter getLogWriter() throws SQLException {
        return _ds.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        _ds.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return _ds.getLoginTimeout();
    }

    public void setLoginTimeout(int timeout) throws SQLException {
        _ds.setLoginTimeout(timeout);
    }

    public Connection getConnection() throws SQLException {
        return _ds.getConnection();
    }

    public Connection getConnection(String user, String pass)
        throws SQLException {
        if (user == null && pass == null)
            return _ds.getConnection();
        try {
            return _ds.getConnection(user, pass);
        } catch (UnsupportedOperationException ex) {
            // OPENJPA-1354
            // under some configuration _ds is Commons DBCP Basic/Poolable DataSource
            // that does not support getConnection(user, password)
            // see http://commons.apache.org/dbcp/apidocs/org/apache/commons/dbcp/BasicDataSource.html
            // hence this workaround
            if (setBeanProperty(_ds, "setUsername", user)
             && setBeanProperty(_ds, "setPassword", pass))
                return _ds.getConnection();
        }
        return null;
    }

    public void close() throws Exception {
        if (_ds instanceof Closeable)
            ((Closeable) _ds).close();
    }

    // java.sql.Wrapper implementation (JDBC 4)
    public boolean isWrapperFor(Class iface) {
        return iface.isAssignableFrom(getDelegate().getClass());
    }

    public Object unwrap(Class iface) {
        if (isWrapperFor(iface))
            return getDelegate();
        else
            return null;
    }
    
    private boolean setBeanProperty(Object target, String method, Object val) {
        try {
            Method setter = target.getClass().getMethod(method, new Class[]{String.class});
            setter.invoke(target, val);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
    
    // Java 7 methods follow
    
    public Logger getParentLogger() throws SQLFeatureNotSupportedException{
    	throw new SQLFeatureNotSupportedException();
    }
}
