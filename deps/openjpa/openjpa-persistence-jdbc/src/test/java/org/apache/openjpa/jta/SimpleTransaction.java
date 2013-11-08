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
package org.apache.openjpa.jta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * A transaction for testing.
 * 
 * @author Pinaki Poddar
 *
 */
public class SimpleTransaction implements Transaction {
    private final Set<Synchronization> synchs = new HashSet<Synchronization>();
    private Throwable rollbackCause;
    private volatile int status = Status.STATUS_UNKNOWN;
    
    /**
     * Commits this transaction.
     */
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException,
            SecurityException, SystemException {
        if (status == Status.STATUS_MARKED_ROLLBACK)
            throw new RuntimeException(this + " can not commit. Marked for rollback");
        status = Status.STATUS_COMMITTING;
        List<Throwable> errors = new ArrayList<Throwable>();
        for (Synchronization synch : synchs) {
            try {
                synch.beforeCompletion();
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        // do nothing
        status = errors.isEmpty() ? Status.STATUS_COMMITTED : Status.STATUS_ROLLEDBACK;
        for (Synchronization synch : synchs) {
            try {
                synch.afterCompletion(status);
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        status = errors.isEmpty() ? Status.STATUS_COMMITTED : Status.STATUS_UNKNOWN;
    }

    /**
     * Not implemented.
     * Raises UnsupportedOperationException.
     */
    public boolean delistResource(XAResource arg0, int arg1) throws IllegalStateException, SystemException {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented.
     * Raises UnsupportedOperationException.
     */
    public boolean enlistResource(XAResource arg0) throws IllegalStateException, RollbackException, SystemException {
        return false;
    }

    /**
     * Gets the status of this transaction.
     * Raises UnsupportedOperationException.
     */
    public int getStatus() throws SystemException {
        return status;
    }
    
    void setStatus(int newStatus) throws SystemException {
        status = newStatus;
    }
    

    /**
     * Registers the given synchronization element.
     */
    public void registerSynchronization(Synchronization synch) throws IllegalStateException, RollbackException,
            SystemException {
        synchs.add(synch);
    }

    /**
     * Rolls back this transaction.
     */
    public void rollback() throws IllegalStateException, SystemException {
        status = Status.STATUS_ROLLING_BACK;
        List<Throwable> errors = new ArrayList<Throwable>();
        for (Synchronization synch : synchs) {
            try {
                synch.beforeCompletion();
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        
        for (Synchronization synch : synchs) {
            try {
                synch.afterCompletion(Status.STATUS_ROLLEDBACK);
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        status = errors.isEmpty() ? Status.STATUS_ROLLEDBACK : Status.STATUS_UNKNOWN;
        if (!errors.isEmpty()) 
            throw new RuntimeException(errors.get(0));
    }

    /**
     * Marks this transaction for rollback only.
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        setRollbackOnly(null);
    }
    
    /**
     * Marks this transaction for rollback only with the given underlying cause.
     */
    void setRollbackOnly(Throwable cause) throws IllegalStateException, SystemException {
        rollbackCause = cause;
        status = Status.STATUS_MARKED_ROLLBACK;
    }
    
    Throwable getRollbackCause() {
        return rollbackCause;
    }
    
    public String toString() {
        return "TXN:"+hashCode() +"["+statusCode()+"]";
    }
    
    String statusCode() {
        switch (status) {
        case Status.STATUS_ACTIVE          : return "active";
        case Status.STATUS_COMMITTED       : return "committed";
        case Status.STATUS_COMMITTING      : return "committing";
        case Status.STATUS_MARKED_ROLLBACK : return "marked rollback";
        case Status.STATUS_NO_TRANSACTION  : return "none";
        case Status.STATUS_PREPARED        : return "prepared";
        case Status.STATUS_PREPARING       : return "preparing";
        case Status.STATUS_ROLLEDBACK      : return "rolled back";
        case Status.STATUS_ROLLING_BACK    : return "rolling back";
        case Status.STATUS_UNKNOWN         : return "unknown";
        default                            : return "error";
        }
    }

}
