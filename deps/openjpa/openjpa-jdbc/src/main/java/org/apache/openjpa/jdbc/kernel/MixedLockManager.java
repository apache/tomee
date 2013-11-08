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

import java.sql.SQLException;
import java.util.List;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.jdbc.sql.SQLFactory;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.MixedLockLevels;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.OptimisticException;

/**
 * Mixed lock manager implements both optimistic and pessimistic locking
 * semantics in parallel to the JPA 2.0 specification.
 *
 * @author Albert Lee
 * @since 2.0.0
 */
public class MixedLockManager extends PessimisticLockManager {

    private static final Localizer _loc = Localizer
        .forPackage(MixedLockManager.class);

    /*
     * (non-Javadoc)
     * @see org.apache.openjpa.jdbc.kernel.PessimisticLockManager
     *  #selectForUpdate(org.apache.openjpa.jdbc.sql.Select,int)
     */
    public boolean selectForUpdate(Select sel, int lockLevel) {
        return (lockLevel >= MixedLockLevels.LOCK_PESSIMISTIC_READ) 
            ? super.selectForUpdate(sel, lockLevel) : false;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.openjpa.jdbc.kernel.PessimisticLockManager#
     *  lockInternal(org.apache.openjpa.kernel.OpenJPAStateManager, int, int,
     *               java.lang.Object)
     */
    protected void lockInternal(OpenJPAStateManager sm, int level, int timeout,
        Object sdata, boolean postLockVersionCheck) {
        if (level >= MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT) {
            setVersionCheckOnReadLock(true);
            setVersionUpdateOnWriteLock(true);
            super.lockInternal(sm, level, timeout, sdata, postLockVersionCheck);
        } else if (level >= MixedLockLevels.LOCK_PESSIMISTIC_READ) {
            setVersionCheckOnReadLock(true);
            setVersionUpdateOnWriteLock(false);
            super.lockInternal(sm, level, timeout, sdata, postLockVersionCheck);
        } else if (level >= MixedLockLevels.LOCK_READ) {
            setVersionCheckOnReadLock(true);
            setVersionUpdateOnWriteLock(true);
            optimisticLockInternal(sm, level, timeout, sdata,
                postLockVersionCheck);
        }
    }

    protected List<SQLBuffer> getLockRows(DBDictionary dict, Object id, ClassMapping mapping,
            JDBCFetchConfiguration fetch, SQLFactory factory) {
        List<SQLBuffer> sqls = super.getLockRows(dict, id, mapping, fetch, factory);
        // 
        if(!dict.supportsLockingWithMultipleTables) {
            // look for columns mapped to secondary tables which need to be locked
            FieldMapping fms[] = mapping.getFieldMappings();
            for( FieldMapping fm : fms) {
                DBIdentifier secTableName = fm.getMappingInfo().getTableIdentifier();
                if (!DBIdentifier.isNull(secTableName)) {
                    // select only the PK columns, since we just want to lock
                    Select select = factory.newSelect();
                    select.select(fm.getColumns());
                    select.whereForeignKey(fm.getJoinForeignKey(), id, mapping, _store);
                    sqls.add(select.toSelect(true, fetch));
                }
            }
        }
        return sqls;
    }

    protected void optimisticLockInternal(OpenJPAStateManager sm, int level,
        int timeout, Object sdata, boolean postLockVersionCheck) {
        super.optimisticLockInternal(sm, level, timeout, sdata,
            postLockVersionCheck);
        if (postLockVersionCheck) {
            if (level >= MixedLockLevels.LOCK_PESSIMISTIC_READ) {
                ClassMapping mapping = (ClassMapping) sm.getMetaData();
                try {
                    if (!mapping.getVersion().checkVersion(sm, this.getStore(),
                        false)) {
                        throw (new OptimisticException(_loc.get(
                            "optimistic-violation-lock").getMessage()))
                            .setFailedObject(sm.getObjectId());
                    }
                } catch (SQLException se) {
                    throw SQLExceptions.getStore(se, sm.getObjectId(),
                        getStore().getDBDictionary());
                }
            }
        }
    }

    public boolean skipRelationFieldLock() {
        return true;
    }
}
