package org.apache.openejb.resource.jdbc.managed.local;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.Connection;
import java.sql.SQLException;

public class LocalXAResource implements XAResource {
    private final Connection connection;
    private Xid currentXid;
    private boolean originalAutoCommit;

    public LocalXAResource(final Connection localTransaction) {
        this.connection = localTransaction;
    }

    public synchronized Xid getXid() {
        return currentXid;
    }

    @Override
    public synchronized void start(final Xid xid, int flag) throws XAException {
        if (flag == XAResource.TMNOFLAGS) {
            if (this.currentXid != null) {
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
            if (xid != this.currentXid) {
                throw new XAException("Attempting to resume in different transaction: expected " + this.currentXid + ", but was " + xid);
            }
        } else {
            throw new XAException("Unknown start flag " + flag);
        }
    }

    @Override
    public synchronized void end(final Xid xid, int flag) throws XAException {
        if (xid == null) {
            throw new NullPointerException("xid is null");
        }
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
        }
    }

    @Override
    public synchronized int prepare(final Xid xid) {
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
    public synchronized void commit(final Xid xid, boolean flag) throws XAException {
        if (xid == null) {
            throw new NullPointerException("xid is null");
        }
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
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
            this.currentXid = null;
        }
    }

    @Override
    public synchronized void rollback(final Xid xid) throws XAException {
        if (xid == null) {
            throw new NullPointerException("xid is null");
        }
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
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
    public synchronized void forget(final Xid xid) {
        if (xid != null && this.currentXid.equals(xid)) {
            this.currentXid = null;
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
}
