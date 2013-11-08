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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.Collection;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPASavepoint;
import org.apache.openjpa.kernel.RestoreState;
import org.apache.openjpa.kernel.SavepointManager;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Abstract {@link SavepointManager} implementation that
 * delegates to the subclass for savepoint operations on the active
 * {@link Connection}. This implementation issues a flush and relies
 * on the driver/db to restore back to the flushed state.
 *
 * @author Steve Kim
 * @since 0.3.4
 */
public abstract class AbstractJDBCSavepointManager
    implements SavepointManager, Configurable {

    private boolean _restore = false;

    public void startConfiguration() {
    }

    public void setConfiguration(Configuration conf) {
        _restore = ((OpenJPAConfiguration) conf).getRestoreStateConstant()
            != RestoreState.RESTORE_NONE;
    }

    public void endConfiguration() {
    }

    /**
     * Return whether to retain in-memory copies of field values for restore
     * on rollback. Defaults to {@link OpenJPAConfiguration#getRestoreState).
     */
    public boolean getRestoreFieldState() {
        return _restore;
    }

    /**
     * Set whether to retain in-memory copies of field values for restore
     * on rollback. Defaults to {@link OpenJPAConfiguration#getRestoreState}.
     */
    public void setRestoreFieldState(boolean restore) {
        _restore = restore;
    }

    public OpenJPASavepoint newSavepoint(String name, Broker broker) {
        // flush after creating savepoint b/c flush may add/change states
        OpenJPASavepoint save = new ConnectionSavepoint(broker, name, _restore);
        broker.flush();
        return save;
    }

    public boolean supportsIncrementalFlush() {
        return true;
    }

    /**
     * Rollback the datastore savepoint.
     */
    protected abstract void rollbackDataStore(ConnectionSavepoint savepoint);

    /**
     * Set the datastore savepoint.
     */
    protected abstract void setDataStore(ConnectionSavepoint savepoint);

    /**
     * A savepoint which provides access to the current transactional
     * connection.
     */
    protected class ConnectionSavepoint extends OpenJPASavepoint {

        private Object _savepoint;

        public ConnectionSavepoint(Broker broker, String name, boolean copy) {
            super(broker, name, copy);
        }

        /**
         * Return the stored savepoint object
         */
        public Object getDataStoreSavepoint() {
            return _savepoint;
        }

        /**
         * Set the implementation specific savepoint object
         */
        public void setDataStoreSavepoint(Object savepoint) {
            _savepoint = savepoint;
        }

        /**
         * Return the current {@link Connection} for this savepoint.
         */
        public Connection getConnection() {
            return ((JDBCStoreManager) getBroker().getStoreManager().
                getInnermostDelegate()).getConnection();
        }

        public Collection rollback(Collection previous) {
            AbstractJDBCSavepointManager.this.rollbackDataStore(this);
            return super.rollback(previous);
        }

        public void save(Collection states) {
            AbstractJDBCSavepointManager.this.setDataStore(this);
            super.save(states);
        }

        private void writeObject(ObjectOutputStream out)
            throws IOException {
            throw new NotSerializableException();
        }
    }
}
