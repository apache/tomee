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
package org.apache.openjpa.persistence;

import java.util.EnumSet;

import junit.framework.TestCase;
import org.apache.openjpa.kernel.ConnectionRetainModes;
import org.apache.openjpa.kernel.DetachState;
import org.apache.openjpa.kernel.RestoreState;
import org.apache.openjpa.kernel.AutoClear;
import org.apache.openjpa.kernel.AutoDetach;
import org.apache.openjpa.kernel.QueryOperations;
import org.apache.openjpa.event.CallbackModes;

public class TestEnumToKernelConstantMappings
    extends TestCase {

    public void testConnectionRetainModes() {
        assertEquals(ConnectionRetainModes.CONN_RETAIN_ALWAYS,
            ConnectionRetainMode.ALWAYS.toKernelConstant());
        assertEquals(ConnectionRetainMode.ALWAYS,
            ConnectionRetainMode.fromKernelConstant(
                ConnectionRetainModes.CONN_RETAIN_ALWAYS));
        assertEquals(ConnectionRetainMode.ALWAYS.toKernelConstant(),
            ConnectionRetainMode.ALWAYS.ordinal());

        assertEquals(ConnectionRetainModes.CONN_RETAIN_DEMAND,
            ConnectionRetainMode.ON_DEMAND.toKernelConstant());
        assertEquals(ConnectionRetainMode.ON_DEMAND,
            ConnectionRetainMode.fromKernelConstant(
                ConnectionRetainModes.CONN_RETAIN_DEMAND));
        assertEquals(ConnectionRetainMode.ON_DEMAND.toKernelConstant(),
            ConnectionRetainMode.ON_DEMAND.ordinal());

        assertEquals(ConnectionRetainModes.CONN_RETAIN_TRANS,
            ConnectionRetainMode.TRANSACTION.toKernelConstant());
        assertEquals(ConnectionRetainMode.TRANSACTION,
            ConnectionRetainMode.fromKernelConstant(
                ConnectionRetainModes.CONN_RETAIN_TRANS));
        assertEquals(ConnectionRetainMode.TRANSACTION.toKernelConstant(),
            ConnectionRetainMode.TRANSACTION.ordinal());

        assertEquals(getConstantCount(ConnectionRetainModes.class),
            ConnectionRetainMode.values().length);
    }

    public void testDetachState() {
        assertEquals(DetachState.DETACH_ALL,
            DetachStateType.ALL.toKernelConstant());
        assertEquals(DetachStateType.ALL,
            DetachStateType.fromKernelConstant(DetachState.DETACH_ALL));
        assertEquals(DetachStateType.ALL.toKernelConstant(),
            DetachStateType.ALL.ordinal());

        assertEquals(DetachState.DETACH_FETCH_GROUPS,
            DetachStateType.FETCH_GROUPS.toKernelConstant());
        assertEquals(DetachStateType.FETCH_GROUPS,
            DetachStateType.fromKernelConstant(
                DetachState.DETACH_FETCH_GROUPS));
        assertEquals(DetachStateType.FETCH_GROUPS.toKernelConstant(),
            DetachStateType.FETCH_GROUPS.ordinal());

        assertEquals(DetachState.DETACH_LOADED,
            DetachStateType.LOADED.toKernelConstant());
        assertEquals(DetachStateType.LOADED,
            DetachStateType.fromKernelConstant(DetachState.DETACH_LOADED));
        assertEquals(DetachStateType.LOADED.toKernelConstant(),
            DetachStateType.LOADED.ordinal());

        // subtract 1 for DetachState.DETACH_FGS, which is deprecated
        assertEquals(getConstantCount(DetachState.class) - 1,
            DetachStateType.values().length);
    }

    public void testRestoreState() {
        assertEquals(RestoreState.RESTORE_ALL,
            RestoreStateType.ALL.toKernelConstant());
        assertEquals(RestoreStateType.ALL,
            RestoreStateType.fromKernelConstant(RestoreState.RESTORE_ALL));
        assertEquals(RestoreStateType.ALL.toKernelConstant(),
            RestoreStateType.ALL.ordinal());

        assertEquals(RestoreState.RESTORE_IMMUTABLE,
            RestoreStateType.IMMUTABLE.toKernelConstant());
        assertEquals(RestoreStateType.IMMUTABLE,
            RestoreStateType.fromKernelConstant(
                RestoreState.RESTORE_IMMUTABLE));
        assertEquals(RestoreStateType.IMMUTABLE.toKernelConstant(),
            RestoreStateType.IMMUTABLE.ordinal());

        assertEquals(RestoreState.RESTORE_NONE,
            RestoreStateType.NONE.toKernelConstant());
        assertEquals(RestoreStateType.NONE,
            RestoreStateType.fromKernelConstant(RestoreState.RESTORE_NONE));
        assertEquals(RestoreStateType.NONE.toKernelConstant(),
            RestoreStateType.NONE.ordinal());

        assertEquals(getConstantCount(RestoreState.class),
            RestoreStateType.values().length);
    }

    public void testAutoClear() {
        assertEquals(AutoClear.CLEAR_ALL, AutoClearType.ALL.toKernelConstant());
        assertEquals(AutoClearType.ALL,
            AutoClearType.fromKernelConstant(AutoClear.CLEAR_ALL));
        assertEquals(AutoClearType.ALL.toKernelConstant(),
            AutoClearType.ALL.ordinal());

        assertEquals(AutoClear.CLEAR_DATASTORE,
            AutoClearType.DATASTORE.toKernelConstant());
        assertEquals(AutoClearType.DATASTORE,
            AutoClearType.fromKernelConstant(AutoClear.CLEAR_DATASTORE));
        assertEquals(AutoClearType.DATASTORE.toKernelConstant(),
            AutoClearType.DATASTORE.ordinal());

        assertEquals(getConstantCount(AutoClear.class),
            AutoClearType.values().length);
    }

    public void testAutoDetach() {
        // Commenting out constant count test for now. Subtracting 2 is brittle.
        // assertEquals(getConstantCount(AutoDetach.class) - 2,
        //    AutoDetachType.values().length);

        assertEquals(EnumSet.of(AutoDetachType.CLOSE),
            AutoDetachType.toEnumSet(AutoDetach.DETACH_CLOSE));
        assertEquals(AutoDetach.DETACH_CLOSE,
            AutoDetachType.fromEnumSet(EnumSet.of(AutoDetachType.CLOSE)));

        assertEquals(EnumSet.of(AutoDetachType.COMMIT),
            AutoDetachType.toEnumSet(AutoDetach.DETACH_COMMIT));
        assertEquals(AutoDetach.DETACH_COMMIT,
            AutoDetachType.fromEnumSet(EnumSet.of(AutoDetachType.COMMIT)));

        assertEquals(EnumSet.of(AutoDetachType.NON_TRANSACTIONAL_READ),
            AutoDetachType.toEnumSet(AutoDetach.DETACH_NONTXREAD));
        assertEquals(AutoDetach.DETACH_NONTXREAD,
            AutoDetachType.fromEnumSet(
                EnumSet.of(AutoDetachType.NON_TRANSACTIONAL_READ)));

        assertEquals(EnumSet.of(AutoDetachType.ROLLBACK),
            AutoDetachType.toEnumSet(AutoDetach.DETACH_ROLLBACK));
        assertEquals(AutoDetach.DETACH_ROLLBACK,
            AutoDetachType.fromEnumSet(EnumSet.of(AutoDetachType.ROLLBACK)));


        assertEquals(EnumSet.of(AutoDetachType.CLOSE, AutoDetachType.COMMIT),
            AutoDetachType.toEnumSet(
                AutoDetach.DETACH_CLOSE | AutoDetach.DETACH_COMMIT));
        assertEquals(AutoDetach.DETACH_ROLLBACK | AutoDetach.DETACH_CLOSE,
            AutoDetachType.fromEnumSet(
                EnumSet.of(AutoDetachType.ROLLBACK, AutoDetachType.CLOSE)));


        assertEquals(EnumSet.allOf(AutoDetachType.class),
            AutoDetachType.toEnumSet(
            		  AutoDetach.DETACH_NONE
                    | AutoDetach.DETACH_CLOSE
                    | AutoDetach.DETACH_COMMIT
                    | AutoDetach.DETACH_NONTXREAD
                    | AutoDetach.DETACH_ROLLBACK));
        assertEquals( AutoDetach.DETACH_NONE
        		    | AutoDetach.DETACH_CLOSE
                    | AutoDetach.DETACH_COMMIT
                    | AutoDetach.DETACH_NONTXREAD
                    | AutoDetach.DETACH_ROLLBACK,
            AutoDetachType.fromEnumSet(EnumSet.allOf(AutoDetachType.class)));
    }

    public void testCallbackMode() {
        assertEquals(getConstantCount(CallbackModes.class),
            CallbackMode.values().length);

        assertEquals(EnumSet.of(CallbackMode.FAIL_FAST),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_FAIL_FAST));
        assertEquals(CallbackModes.CALLBACK_FAIL_FAST,
            CallbackMode.fromEnumSet(EnumSet.of(CallbackMode.FAIL_FAST)));

        assertEquals(EnumSet.of(CallbackMode.IGNORE),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_IGNORE));
        assertEquals(CallbackModes.CALLBACK_IGNORE,
            CallbackMode.fromEnumSet(EnumSet.of(CallbackMode.IGNORE)));

        assertEquals(EnumSet.of(CallbackMode.LOG),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_LOG));
        assertEquals(CallbackModes.CALLBACK_LOG,
            CallbackMode.fromEnumSet(EnumSet.of(CallbackMode.LOG)));

        assertEquals(EnumSet.of(CallbackMode.RETHROW),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_RETHROW));
        assertEquals(CallbackModes.CALLBACK_RETHROW,
            CallbackMode.fromEnumSet(EnumSet.of(CallbackMode.RETHROW)));

        assertEquals(EnumSet.of(CallbackMode.ROLLBACK),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_ROLLBACK));
        assertEquals(CallbackModes.CALLBACK_ROLLBACK,
            CallbackMode.fromEnumSet(EnumSet.of(CallbackMode.ROLLBACK)));


        assertEquals(EnumSet.of(CallbackMode.ROLLBACK, CallbackMode.IGNORE),
            CallbackMode.toEnumSet(CallbackModes.CALLBACK_ROLLBACK
                | CallbackModes.CALLBACK_IGNORE));
        assertEquals(
            CallbackModes.CALLBACK_ROLLBACK | CallbackModes.CALLBACK_IGNORE,
            CallbackMode.fromEnumSet(
                EnumSet.of(CallbackMode.ROLLBACK, CallbackMode.IGNORE)));


        assertEquals(EnumSet.allOf(CallbackMode.class),
            CallbackMode.toEnumSet(
                CallbackModes.CALLBACK_FAIL_FAST
                    | CallbackModes.CALLBACK_IGNORE
                    | CallbackModes.CALLBACK_LOG
                    | CallbackModes.CALLBACK_RETHROW
                    | CallbackModes.CALLBACK_ROLLBACK));
        assertEquals(CallbackModes.CALLBACK_FAIL_FAST
                    | CallbackModes.CALLBACK_IGNORE
                    | CallbackModes.CALLBACK_LOG
                    | CallbackModes.CALLBACK_RETHROW
                    | CallbackModes.CALLBACK_ROLLBACK,
            CallbackMode.fromEnumSet(EnumSet.allOf(CallbackMode.class)));
    }

    public void testQueryOperationTypes() {
        assertEquals(QueryOperations.OP_SELECT,
            QueryOperationType.SELECT.toKernelConstant());
        assertEquals(QueryOperationType.SELECT,
            QueryOperationType.fromKernelConstant(
                QueryOperations.OP_SELECT));
        assertEquals(QueryOperationType.SELECT.toKernelConstant(),
            QueryOperationType.SELECT.ordinal() + 1);

        assertEquals(QueryOperations.OP_UPDATE,
            QueryOperationType.UPDATE.toKernelConstant());
        assertEquals(QueryOperationType.UPDATE,
            QueryOperationType.fromKernelConstant(
                QueryOperations.OP_UPDATE));
        assertEquals(QueryOperationType.UPDATE.toKernelConstant(),
            QueryOperationType.UPDATE.ordinal() + 1);

        assertEquals(QueryOperations.OP_DELETE,
            QueryOperationType.DELETE.toKernelConstant());
        assertEquals(QueryOperationType.DELETE,
            QueryOperationType.fromKernelConstant(
                QueryOperations.OP_DELETE));
        assertEquals(QueryOperationType.DELETE.toKernelConstant(),
            QueryOperationType.DELETE.ordinal() + 1);

        assertEquals(getConstantCount(QueryOperations.class),
            QueryOperationType.values().length);
    }

    private int getConstantCount(Class cls) {
        return cls.getDeclaredFields().length;
    }
}
