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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLFactory;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.util.Id;

/**
 * Represents the JDBC store.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface JDBCStore {

    /**
     * Current persistence context.
     */
    public StoreContext getContext();

    /**
     * Return the configuration for this runtime.
     */
    public JDBCConfiguration getConfiguration();

    /**
     * Return the dictionary in use.
     */
    public DBDictionary getDBDictionary();

    /**
     * Return the SQL factory for this runtime.
     */
    public SQLFactory getSQLFactory();

    /**
     * If the lock manager in use is a {@link JDBCLockManager}, return it.
     */
    public JDBCLockManager getLockManager();

    /**
     * Return a SQL connection to the database.
     * The <code>close</code> method should always be called on the connection
     * to free any resources it is using. When appropriate, the close
     * method is implemented as a no-op.
     */
    public Connection getConnection();
    
    /**
     * Return a new SQL connection to the database.
     * This is used when the current connection still has a open result set
     * and a new database operation needs to be done. 
     * The <code>close</code> method should always be called on the connection
     * to free any resources it is using. When appropriate, the close
     * method is implemented as a no-op.
     */
    public Connection getNewConnection();

    /**
     * Return the current default fetch configuration.
     */
    public JDBCFetchConfiguration getFetchConfiguration();

    /**
     * Create a new datastore identity object from the given id value and
     * mapping.
     */
    public Id newDataStoreId(long id, ClassMapping mapping, boolean subs);

    /**
     * Find the object with the given oid. Convenience method on top of
     * the store's persistence context.
     *
     * @param vm the mapping holding this oid, or null if not applicable
     */
    public Object find(Object oid, ValueMapping vm,
        JDBCFetchConfiguration fetch);

    /**
     * Makes sure all subclasses of the given type are loaded in the JVM.
     * This is usually done automatically.
     */
    public void loadSubclasses(ClassMapping mapping);
}
