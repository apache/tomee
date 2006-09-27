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
package org.apache.openejb.timer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.geronimo.timer.UserTaskFactory;
import org.apache.geronimo.timer.WorkInfo;
import org.apache.openejb.EjbContainer;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.util.TransactionUtils;
import org.apache.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class BasicTimerServiceImpl implements BasicTimerService {
    private static final InterfaceMethodSignature EJB_TIMEOUT_SIGNATURE = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
    private final ExtendedEjbDeployment deployment;
    private final EjbContainer ejbContainer;
    private final PersistentTimer timer;
    private final String kernelName;
    private final String containerId;
    private final ObjectName containerObjectName;
    private final UserTaskFactory userTaskFactory;
    private final int ejbTimeoutIndex;

    public BasicTimerServiceImpl(ExtendedEjbDeployment deployment, EjbContainer ejbContainer, PersistentTimer timer, String kernelName, String containerId) throws MalformedObjectNameException {
        this.deployment = deployment;
        this.ejbContainer = ejbContainer;
        this.timer = timer;
        this.kernelName = kernelName;
        this.containerId = containerId;
        this.containerObjectName = new ObjectName(containerId);
        userTaskFactory = new EJBInvokeTaskFactory(this);

        int ejbTimeoutIndex = -1;
        InterfaceMethodSignature[] signatures = deployment.getSignatures();
        for (int i = 0; i < signatures.length; i++) {
            InterfaceMethodSignature signature = signatures[i];
            if (signature.equals(EJB_TIMEOUT_SIGNATURE)) {
                ejbTimeoutIndex = i;
                break;
            }
        }
        if (ejbTimeoutIndex < 0) {
            throw new IllegalArgumentException("EJB " + deployment.getEjbName() + " does not implement ejbTimeout(javax.ejb.Timer timer)");
        }
        this.ejbTimeoutIndex = ejbTimeoutIndex;
    }

    public void doStart() throws PersistenceException {
        //reconstruct saved timers.
        Collection workInfos = timer.playback(containerId, userTaskFactory);
        for (Iterator iterator = workInfos.iterator(); iterator.hasNext();) {
            WorkInfo workInfo = (WorkInfo) iterator.next();
            newTimer(workInfo);
        }
    }

    public void doStop() throws PersistenceException {
        Collection ids = timer.getIdsByKey(containerId, null);
        timer.cancelTimerTasks(ids);
    }


    public Timer createTimer(Object id, Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = timer.scheduleAtFixedRate(containerId, userTaskFactory, id, info, initialExpiration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = timer.schedule(containerId, userTaskFactory, id, info, expiration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = timer.scheduleAtFixedRate(containerId, userTaskFactory, id, info, initialDuration, intervalDuration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Timer createTimer(Object id, long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException {
        try {
            WorkInfo workInfo = timer.schedule(userTaskFactory, containerId, id, info, duration);
            return newTimer(workInfo);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        } catch (RollbackException e) {
            throw new EJBException(e);
        } catch (SystemException e) {
            throw new EJBException(e);
        }
    }

    public Collection getTimers(Object id) throws IllegalStateException, EJBException {
        Collection ids = null;
        try {
            ids = timer.getIdsByKey(containerId, id);
        } catch (PersistenceException e) {
            throw new EJBException(e);
        }
        Collection timers = new ArrayList();
        for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
            Long timerId = (Long) iterator.next();
            try {
                Timer timer = getTimerById(timerId);
                timers.add(timer);
            } catch (NoSuchObjectLocalException e) {
                System.out.println("could not find timer for timerId " + timerId + "from key " + containerId + " and " + id);
            }
        }
        return timers;
    }

    //TODO HACK SEE GERONIMO-623
    private boolean notified = false;

    public Timer getTimerById(Long id) {
        WorkInfo workInfo = null;
        workInfo = timer.getWorkInfo(id);
        if (workInfo != null) {
            TimerImpl timer = (TimerImpl) workInfo.getClientHandle();
            return timer;
        } else {
            throw new NoSuchObjectLocalException("No timer");
        }
    }

    void registerCancelSynchronization(Synchronization cancelSynchronization) throws RollbackException, SystemException {
        Transaction transaction = TransactionUtils.getTransactionIfActive(ejbContainer.getTransactionManager());
        if (transaction != null) {
            transaction.registerSynchronization(cancelSynchronization);
        } else {
            cancelSynchronization.afterCompletion(Status.STATUS_COMMITTED);
        }
    }

    private Timer newTimer(WorkInfo workInfo) {
        Timer timer = new TimerImpl(workInfo, this, kernelName, containerObjectName);
        workInfo.setClientHandle(timer);
        return timer;
    }

    private static class EJBInvokeTask implements Runnable {
        private final BasicTimerServiceImpl timerService;
        private final long timerId;

        public EJBInvokeTask(BasicTimerServiceImpl timerService, long timerId) {
            this.timerService = timerService;
            this.timerId = timerId;
        }

        public void run() {
            TimerImpl timerImpl = (TimerImpl) timerService.getTimerById(new Long(timerId));
            //TODO HACK SEE GERONIMO-623
            if (timerImpl == null) {
                return;
            }
            timerService.ejbContainer.timeout(timerService.deployment, timerImpl.getUserId(), timerImpl, timerService.ejbTimeoutIndex);
        }

    }

    private static class EJBInvokeTaskFactory implements UserTaskFactory {
        private final BasicTimerServiceImpl timerService;

        public EJBInvokeTaskFactory(BasicTimerServiceImpl timerService) {
            this.timerService = timerService;
        }

        public Runnable newTask(long id) {
            return new EJBInvokeTask(timerService, id);
        }

    }

}
