/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.timer;

import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.apache.geronimo.timer.WorkInfo;

/**
 * TODO keep track of state so after single-shot firing nothing works.
 *
 * @version $Revision$ $Date$
 */
public class TimerImpl implements Timer {
    private final WorkInfo workInfo;
    private final BasicTimerServiceImpl timerService;
    private final String kernelName;
    private final ObjectName timerSourceName;
    private boolean cancelled = false;

    public TimerImpl(WorkInfo workInfo, BasicTimerServiceImpl timerService, String kernelName, ObjectName timerSourceName) {
        this.workInfo = workInfo;
        this.timerService = timerService;
        this.kernelName = kernelName;
        this.timerSourceName = timerSourceName;
    }

    public void cancel() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        workInfo.getExecutorFeedingTimerTask().cancel();
        cancelled = true;
        try {
            timerService.registerCancelSynchronization(new CancelSynchronization());
        } catch (RollbackException e) {
            throw (IllegalStateException) new IllegalStateException("Transaction is already rolled back").initCause(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public long getTimeRemaining() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        long now = System.currentTimeMillis();
        long then = workInfo.getTime().getTime();
        return then - now;
    }

    public Date getNextTimeout() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return workInfo.getTime();
    }

    public Serializable getInfo() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return (Serializable) workInfo.getUserInfo();
    }

    public TimerHandle getHandle() throws EJBException, IllegalStateException, NoSuchObjectLocalException {
        checkState();
        return new TimerHandleImpl(workInfo.getId(), kernelName, timerSourceName);
    }

    Object getUserId() {
        return workInfo.getUserId();
    }

    private void checkState() throws NoSuchObjectLocalException {
        if (!TimerState.getTimerState()) {
            throw new IllegalStateException("Timer methods not available");
        }
        if (cancelled) {
            throw new NoSuchObjectLocalException("Timer is cancelled");
        }
    }

    private class CancelSynchronization implements Synchronization {

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            if (status == Status.STATUS_COMMITTED) {
            } else if (status == Status.STATUS_ROLLEDBACK) {
                cancelled = false;
            }  //else???
        }

    }

}
