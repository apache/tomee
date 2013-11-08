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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

/**
 * Delegating data source that maintains a list of {@link ConnectionDecorator}s.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DecoratingDataSource extends DelegatingDataSource {
    
    private List<ConnectionDecorator> _decorators =
        new CopyOnWriteArrayList<ConnectionDecorator>();

    /**
     * Constructor. Supply wrapped data source.
     */
    public DecoratingDataSource(DataSource ds) {
        super(ds);
    }
    
    /**
     * Return a read-only list of connection decorators in the order they were
     * added.
     */
    public Collection<ConnectionDecorator> getDecorators() {
        return Collections.unmodifiableCollection(_decorators);
    }

    /**
     * Add a connection decorator.
     */
    public void addDecorator(ConnectionDecorator decorator) {
        if (decorator != null)
            _decorators.add(decorator);
    }

    /**
     * Add multiple connection decorators efficiently.
     */
    public void addDecorators(Collection<ConnectionDecorator> decorators) {
        if (decorators != null)
            _decorators.addAll(decorators);
    }

    /**
     * Remove a connection decorator.
     */
    public boolean removeDecorator(ConnectionDecorator decorator) {
        return _decorators.remove(decorator);
    }

    /**
     * Clear all decorators.
     */
    public void clearDecorators() {
        _decorators.clear();
    }

    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        return decorate(conn);
    }

    public Connection getConnection(String user, String pass)
        throws SQLException {
        Connection conn = super.getConnection(user, pass);
        return decorate(conn);
    }

    private Connection decorate(Connection conn) throws SQLException {
        for(ConnectionDecorator decorator : _decorators) { 
            conn = decorator.decorate(conn);
        }
        return conn;
    }
}
