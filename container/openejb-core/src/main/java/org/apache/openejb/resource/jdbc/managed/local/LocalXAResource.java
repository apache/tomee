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
package org.apache.openejb.resource.jdbc.managed.local;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalXAResource implements XAResource {
    private final Connection connection;
    private Xid currentXid;
    private boolean originalAutoCommit;
    private final Lock lock = new ReentrantLock();

    public LocalXAResource(final Connection localTransaction) {
        connection = localTransaction;
    }

    public Xid getXid() {
        checkLock();
        return currentXid;
    }

    @Override
    public void start(final Xid xid, int flag) throws XAException {
        try {
            if (!lock.tryLock(10, TimeUnit.MINUTES)) {

            }
        } catch (InterruptedException e) {
            throw (XAException) new XAException("can't get lock").initCause(cantGetLock());
        }

        if (flag == XAResource.TMNOFLAGS) {
            if (currentXid != null) {
                throw new XAException("Already enlisted in another transaction with xid " + xid);
            }

            // save off the current auto commit flag so it can be restored after the transaction completes
            try {
                originalAutoCommit = connection.getAutoCommit();
            } catch (SQLException ignored) {
                originalAutoCommit = true;
            }

            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw (XAException) new XAException("Count not turn off auto commit for a XA transaction").initCause(e);
            }

            this.currentXid = xid;
        } else if (flag == XAResource.TMRESUME) {
            if (xid != currentXid) {
                throw new XAException("Attempting to resume in different transaction: expected " + currentXid + ", but was " + xid);
            }
        } else {
            throw new XAException("Unknown start flag " + flag);
        }
    }

    private RuntimeException cantGetLock() {
        return new IllegalStateException("can't get lock on resource with Xid " + currentXid + " from thread " + Thread.currentThread().getName());
    }

    @Override
    public void end(final Xid xid, int flag) throws XAException {
        try {
            if (xid == null) {
                throw new NullPointerException("xid is null");
            }
            if (!this.currentXid.equals(xid)) {
                throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int prepare(final Xid xid) {
        checkLock();

        try {
            if (connection.isReadOnly()) {
                connection.setAutoCommit(originalAutoCommit);
                return XAResource.XA_RDONLY;
            }
        } catch (SQLException ignored) {
            // no-op
        }

        return XAResource.XA_OK;
    }

    @Override
    public void commit(final Xid xid, boolean flag) throws XAException {
        checkLock();

        if (xid == null) {
            throw new NullPointerException("xid is null");
        }
        if (!currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + currentXid + ", but was " + xid);
        }

        try {
            if (connection.isClosed()) {
                throw new XAException("Conection is closed");
            }

            if (!connection.isReadOnly()) {
                connection.commit();
            }
        } catch (SQLException e) {
            throw (XAException) new XAException().initCause(e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                // no-op
            }
            currentXid = null;
        }
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        checkLock();

        if (xid == null) {
            throw new NullPointerException("xid is null");
        }
        if (!currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + currentXid + ", but was " + xid);
        }

        try {
            connection.rollback();
        } catch (SQLException e) {
            throw (XAException) new XAException().initCause(e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                // no-op
            }
            this.currentXid = null;
        }
    }

    @Override
    public boolean isSameRM(final XAResource xaResource) {
        return this == xaResource;
    }

    @Override
    public void forget(final Xid xid) {
        checkLock();
        if (xid != null && currentXid.equals(xid)) {
            currentXid = null;
        }
    }

    @Override
    public Xid[] recover(int flag) {
        return new Xid[0];
    }

    @Override
    public int getTransactionTimeout() {
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(int transactionTimeout) {
        return false;
    }

    private void checkLock() {
        if (!lock.tryLock()) {
            throw cantGetLock();
        }
    }
}
