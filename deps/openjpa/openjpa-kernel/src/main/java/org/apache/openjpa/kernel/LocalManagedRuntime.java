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
package org.apache.openjpa.kernel;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.openjpa.ee.AbstractManagedRuntime;
import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.StoreException;
import org.apache.openjpa.util.UserException;

/**
 * Uses a local implementation of the {@link TransactionManager} interface.
 * This manager is valid only for a single {@link Broker}.
 * It duplicates non-managed transaction control.
 *
 * @author Abe White
 */
class LocalManagedRuntime extends AbstractManagedRuntime
    implements ManagedRuntime, TransactionManager, Transaction {

    private static final Localizer _loc = Localizer.forPackage
        (LocalManagedRuntime.class);

    private Synchronization _broker = null;
    private Synchronization _factorySync = null;
    private boolean _active = false;
    private Throwable _rollbackOnly = null;

    /**
     * Constructor. Provide broker that will be requesting managed
     * transaction info.
     */
    public LocalManagedRuntime(Broker broker) {
        _broker = broker;
    }

    public TransactionManager getTransactionManager() {
        return this;
    }

    public synchronized void begin() {
        if (_active)
            throw new InvalidStateException(_loc.get("active"));
        _active = true;
    }

    public synchronized void commit() {
        if (!_active)
            throw new InvalidStateException(_loc.get("not-active"));

        // try to invoke before completion in preparation for commit
        RuntimeException err = null;
        if (_rollbackOnly == null) {
            try {
                _broker.beforeCompletion();
                if (_factorySync != null)
                    _factorySync.beforeCompletion();
            } catch (RuntimeException re) {
                _rollbackOnly = re;
                err = re;
            }
        } else // previously marked rollback only
            err = new StoreException(_loc.get("marked-rollback")).
                setCause(_rollbackOnly).setFatal(true);

        if (_rollbackOnly == null) {
            try {
                _broker.afterCompletion(Status.STATUS_COMMITTED);
                notifyAfterCompletion(Status.STATUS_COMMITTED);
            } catch (RuntimeException re) {
                if (err == null)
                    err = re;
            }
        }

        // if we haven't managed to commit, rollback
        if (_active) {
            try {
                rollback();
            } catch (RuntimeException re) {
                if (err == null)
                    err = re;
            }
        }

        // throw the first exception we encountered, if any
        if (err != null)
            throw err;
    }

    public synchronized void rollback() {
        if (!_active)
            throw new InvalidStateException(_loc.get("not-active"));

        // rollback broker
        RuntimeException err = null;
        try {
            _broker.afterCompletion(Status.STATUS_ROLLEDBACK);
        } catch (RuntimeException re) {
            err = re;
        }

        // rollback synch, even if broker throws exception
        try {
            notifyAfterCompletion(Status.STATUS_ROLLEDBACK);
        } catch (RuntimeException re) {
            if (err == null)
                err = re;
        }

        if (err != null)
            throw err;
    }

    /**
     * Notifies the factory sync that the transaction has ended with
     * the given status. Clears all transaction state regardless
     * of any exceptions during the callback.
     */
    private void notifyAfterCompletion(int status) {
        _active = false;

        try {
            if (_factorySync != null)
                _factorySync.afterCompletion(status);
        } finally {
            _rollbackOnly = null;
            _factorySync = null;
        }
    }

    public synchronized void setRollbackOnly() {
        setRollbackOnly(new UserException());
    }

    public void setRollbackOnly(Throwable cause) {
        _rollbackOnly = cause;
    }

    public Throwable getRollbackCause() {
        return _rollbackOnly;
    }

    public synchronized int getStatus() {
        if (_rollbackOnly != null)
            return Status.STATUS_MARKED_ROLLBACK;
        if (_active)
            return Status.STATUS_ACTIVE;
        return Status.STATUS_NO_TRANSACTION;
    }

    public Transaction getTransaction() {
        return this;
    }

    public void resume(Transaction tobj)
        throws SystemException {
        throw new SystemException(NotSupportedException.class.getName());
    }

    public void setTransactionTimeout(int sec)
        throws SystemException {
        throw new SystemException(NotSupportedException.class.getName());
    }

    public Transaction suspend()
        throws SystemException {
        throw new SystemException(NotSupportedException.class.getName());
    }

    public boolean delistResource(XAResource xaRes, int flag)
        throws SystemException {
        throw new SystemException(NotSupportedException.class.getName());
    }

    public boolean enlistResource(XAResource xaRes)
        throws SystemException {
        throw new SystemException(NotSupportedException.class.getName());
    }

    public synchronized void registerSynchronization(Synchronization sync) {
        if (sync == _broker)
            return;
        if (_factorySync != null)
            throw new InternalException();
        _factorySync = sync;
    }
}

