package org.superbiz.injection.jpa;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

@Singleton
public class MoviesXA {
    private static int XA_STATE_INITIAL = 0;
    private static int XA_STATE_STARTED = 1;
    private static int XA_STATE_ENDED = 2;
    private static int XA_STATE_PREPARED = 3;
    private static int XA_STATE_DISPOSED = 4;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private TransactionManager transactionManager;

    private volatile boolean fail = false;
    private volatile boolean before = false;

    public void run(Movie movie) {
        if (before) {
            addXaResource();
        }

        em.persist(movie);

        if (!before) {
            addXaResource();
        }
    }

    private void addXaResource() {
        try {
            transactionManager.getTransaction().enlistResource(new XAResource() {
                private int state = XA_STATE_INITIAL;
                private Xid xid = null;

                private void validateXid(final Xid xid) throws XAException {
                    if (xid == null) {
                        throw new XAException("Null Xid");
                    }
                    if (this.xid == null) {
                        throw new XAException("There is no live transaction for this XAResource");
                    }
                    if (!xid.equals(this.xid)) {
                        throw new XAException("Given Xid is not that associated with this XAResource object");
                    }
                }

                @Override public void commit(final Xid xid, final boolean onePhase) throws XAException {
                    if (onePhase && state == XA_STATE_PREPARED) {
                        throw new XAException("Transaction is in a 2-phase state when 1-phase is requested");
                    }

                    if ((!onePhase) && state != XA_STATE_PREPARED) {
                        throw new XAException("Attempt to do a 2-phase commit when " + "transaction is not prepared");
                    }

                    dispose();
                }

                private void dispose() throws XAException {
                    state = XA_STATE_DISPOSED;
                    xid = null;
                }

                @Override public void end(final Xid xid, final int flags) throws XAException {

                    validateXid(xid);

                    if (state != XA_STATE_STARTED) {
                        throw new XAException("Invalid XAResource state");
                    }

                    state = XA_STATE_ENDED;
                }

                @Override public void forget(Xid xid) throws XAException {
                    validateXid(xid);

                    if (state != XA_STATE_PREPARED) {
                        throw new XAException("Attempted to forget a XAResource that " + "is not in a heuristically completed state");
                    }

                    dispose();

                    state = XA_STATE_INITIAL;
                }

                @Override public int getTransactionTimeout() throws XAException {
                    throw new XAException("Transaction timeouts not implemented yet");
                }

                @Override public boolean isSameRM(final XAResource xares) throws XAException {
                    return xares == this;
                }

                @Override public int prepare(final Xid xid) throws XAException {
                    if (state != XA_STATE_ENDED) {
                        throw new XAException("Invalid XAResource state");
                    }

                    state = XA_STATE_PREPARED;

                    if (fail) {
                        throw new XAException("oops");
                    }

                    return XA_OK;
                }

                @Override public Xid[] recover(final int flag) throws XAException {
                    return new Xid[0];
                }

                @Override public void rollback(Xid xid) throws XAException {
                    if (state != XA_STATE_PREPARED && state != XA_STATE_ENDED) {
                        throw new XAException("Invalid XAResource state");
                    }
                    dispose();
                }

                @Override public boolean setTransactionTimeout(final int seconds) throws XAException {
                    return false;
                }

                @Override public void start(final Xid xid, final int flags) throws XAException {
                    if (state != XA_STATE_INITIAL && state != XA_STATE_DISPOSED) {
                        throw new XAException("Invalid XAResource state");
                    }

                    if (xid == null) {
                        throw new XAException("Null Xid");
                    }

                    this.xid = xid;
                    state = XA_STATE_STARTED;
                }
            });
        } catch (final RollbackException | SystemException e) {
            throw new IllegalStateException(e);
        }
    }

    public Movie find() {
        return em.createQuery("select e from Movie e", Movie.class).getResultList().iterator().next();
    }

    public void fail() {
        fail = true;
    }

    public void reset() {
        fail = false;
        before = false;
    }

    public void before() {
        before = true;
    }
}
