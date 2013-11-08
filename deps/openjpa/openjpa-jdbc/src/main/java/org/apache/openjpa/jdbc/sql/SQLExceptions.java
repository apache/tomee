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
package org.apache.openjpa.jdbc.sql;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.util.LockException;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.StoreException;

/**
 * Helper class for converting a {@link SQLException} into
 * the appropriate OpenJPA type.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class SQLExceptions {

    private static final SQLException[] EMPTY_EXCEPS = new SQLException[0];

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(SQLException se) {
        return getStore(se, null, null);
    }

    /**
     * Convert the specified exception into a {@link OpenJPAException}.
     */
    public static OpenJPAException getStore(SQLException se, Object failed) {
        return getStore(se, failed, null);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(SQLException se,
        DBDictionary dict) {
        return getStore(se.getMessage(), se, dict);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(SQLException se,
        DBDictionary dict, int level) {
        return getStore(se.getMessage(), se, dict, level);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(SQLException se, Object failed,
        DBDictionary dict) {
        return getStore(se.getMessage(), se, failed, dict, -1);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(SQLException se, Object failed,
        DBDictionary dict, int level) {
        return getStore(se.getMessage(), se, failed, dict, level);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(Message msg, SQLException se,
        DBDictionary dict) {
        return getStore(msg.getMessage(), se, null, dict, -1);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(Message msg, SQLException se,
        DBDictionary dict, int level) {
        return getStore(msg.getMessage(), se, null, dict, level);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(String msg, SQLException se,
        DBDictionary dict) {
        return getStore(msg, se, null, dict, -1);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(String msg, SQLException se,
        DBDictionary dict, int level) {
        return getStore(msg, se, null, dict, level);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(String msg, SQLException se,
        Object failed, DBDictionary dict) {
        return getStore(msg, se, failed, dict, -1);
    }

    /**
     * Convert the specified exception into a {@link StoreException}.
     */
    public static OpenJPAException getStore(String msg, SQLException se, Object failed, DBDictionary dict, int level) {
        if (msg == null)
            msg = se.getClass().getName();
        SQLException[] ses = getSQLExceptions(se);
        OpenJPAException storeEx = (dict == null) ? new StoreException(msg).setFailedObject(failed)
                .setNestedThrowables(ses) : dict.newStoreException(msg, ses, failed);
        if (level != -1 && storeEx.getSubtype() == StoreException.LOCK) {
            LockException lockEx = (LockException) storeEx;
            lockEx.setLockLevel(level);
        }
        return storeEx;
    }
    
    /**
     * Returns an array of {@link SQLException} instances for the specified
     * exception.
     */
    private static SQLException[] getSQLExceptions(SQLException se) {
        if (se == null)
            return EMPTY_EXCEPS;

        List<SQLException> errs = new LinkedList<SQLException>();
        while (se != null && !errs.contains(se)) {
            errs.add(se);
            se = se.getNextException();
        }
        return (SQLException[]) errs.toArray(new SQLException[errs.size()]);
    }
}
