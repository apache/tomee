/**
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
