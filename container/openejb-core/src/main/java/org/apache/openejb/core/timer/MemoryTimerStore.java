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

package org.apache.openejb.core.timer;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.TimerConfig;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryTimerStore implements TimerStore {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");
    private final Map<Long, TimerData> taskStore = new ConcurrentHashMap<>();
    private final Map<Transaction, TimerDataView> tasksByTransaction = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    private final TransactionManager transactionManager;

    public MemoryTimerStore(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public TimerData getTimer(final String deploymentId, final long timerId) {
        try {
            final TimerDataView tasks = getTasks();
            return tasks.getTasks().get(timerId);
        } catch (final TimerStoreException e) {
            return null;
        }
    }

    @Override
    public Collection<TimerData> getTimers(final String deploymentId) {
        try {
            final TimerDataView tasks = getTasks();
            return new ArrayList<>(tasks.getTasks().values());
        } catch (final TimerStoreException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public Collection<TimerData> loadTimers(final EjbTimerServiceImpl timerService, final String deploymentId) throws TimerStoreException {
        final TimerDataView tasks = getTasks();
        final Collection<TimerData> out = new LinkedList<>();
        for (final TimerData data : tasks.getTasks().values()) {
            if (deploymentId == null || deploymentId.equals(data.getDeploymentId())) {
                out.add(data);
            }
        }
        return out;
    }

    // used to re-register a TimerData, if a cancel() is rolledback...
    @Override
    public void addTimerData(final TimerData timerData) throws TimerStoreException {
        getTasks().addTimerData(timerData);
    }

    @Override
    public TimerData createCalendarTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final ScheduleExpression scheduleExpression, final TimerConfig timerConfig, final boolean auto)
        throws TimerStoreException {
        final long id = counter.incrementAndGet();
        final TimerData timerData = new CalendarTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, scheduleExpression, auto);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    @Override
    public TimerData createIntervalTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final Date initialExpiration, final long intervalDuration, final TimerConfig timerConfig)
        throws TimerStoreException {
        final long id = counter.incrementAndGet();
        final TimerData timerData = new IntervalTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, initialExpiration, intervalDuration);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    @Override
    public TimerData createSingleActionTimer(final EjbTimerServiceImpl timerService, final String deploymentId, final Object primaryKey, final Method timeoutMethod, final Date expiration, final TimerConfig timerConfig) throws TimerStoreException {
        final long id = counter.incrementAndGet();
        final TimerData timerData = new SingleActionTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, expiration);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    @Override
    public void removeTimer(final long id) {
        try {
            getTasks().removeTimerData(id);
        } catch (final TimerStoreException e) {
            log.warning("Unable to remove timer data from memory store", e);
        }
    }

    @Override
    public void updateIntervalTimer(final TimerData timerData) {
    }

    private TimerDataView getTasks() throws TimerStoreException {
        Transaction transaction = null;
        int status = Status.STATUS_NO_TRANSACTION;
        try {
            transaction = transactionManager.getTransaction();
            if (transaction != null) {
                status = transaction.getStatus();
            }
        } catch (final SystemException e) {
            // no-op
        }

        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK) {
            return new LiveTimerDataView();
        }

        TxTimerDataView tasks = (TxTimerDataView) tasksByTransaction.get(transaction);
        if (tasks == null) {
            tasks = new TxTimerDataView(transaction);
            tasksByTransaction.put(transaction, tasks);
        }
        return tasks;
    }

    private interface TimerDataView {
        Map<Long, TimerData> getTasks();

        void addTimerData(TimerData timerData);

        void removeTimerData(Long timerId);
    }

    private class LiveTimerDataView implements TimerDataView {
        @Override
        public Map<Long, TimerData> getTasks() {
            return new TreeMap<>(taskStore);
        }

        @Override
        public void addTimerData(final TimerData timerData) {
            taskStore.put(timerData.getId(), timerData);
        }

        @Override
        public void removeTimerData(final Long timerId) {
            taskStore.remove(timerId);
        }
    }

    private class TxTimerDataView implements Synchronization, TimerDataView {
        private final Map<Long, TimerData> add = new TreeMap<>();
        private final Set<Long> remove = new TreeSet<>();
        private final Lock lock = new ReentrantLock();
        private final RuntimeException concurentException;
        private final WeakReference<Transaction> tansactionReference;

        /**
         * This class is not designed to be multi-treaded under the assumption
         * that transactions are single-threaded and this view is only supposed
         * to be used within the transaction for which it was created.
         *
         * @param transaction
         * @throws TimerStoreException
         */
        @SuppressWarnings("LockAcquiredButNotSafelyReleased")
        public TxTimerDataView(final Transaction transaction) throws TimerStoreException {
            // We're going to lock this timer inside this transaction and essentially
            // never let it go.  Any other threads attempting to invoke this object
            // will immediately throw an exception.
            lock.lock();
            concurentException = new IllegalThreadStateException("Object can only be invoked by Thread[" + Thread.currentThread().getName() + "] in Transaction[" + transaction + "]");
            concurentException.fillInStackTrace();
            try {
                transaction.registerSynchronization(this);
                tansactionReference = new WeakReference<>(transaction);
            } catch (final RollbackException e) {
                throw new TimerStoreException("Transaction has been rolled back");
            } catch (final SystemException e) {
                throw new TimerStoreException("Error registering transaction synchronization callback");
            }
        }

        private void checkThread() {
            if (!lock.tryLock()) {
                throw new IllegalStateException("Illegal access by Thread[" + Thread.currentThread().getName() + "]", concurentException);
            }
        }

        @Override
        public Map<Long, TimerData> getTasks() {
            checkThread();
            final TreeMap<Long, TimerData> allTasks = new TreeMap<>(taskStore);
            for (final Long key : remove) {
                allTasks.remove(key);
            }
            allTasks.putAll(add);
            return Collections.unmodifiableMap(allTasks);
        }

        @Override
        public void addTimerData(final TimerData timerData) {
            checkThread();
            final Long timerId = timerData.getId();

            // remove it from the remove set, if it is there
            remove.remove(timerId);

            // put it in the add set
            add.put(timerId, timerData);
        }

        @Override
        public void removeTimerData(final Long timerId) {
            checkThread();

            // remove it from the add set, if it is there
            add.remove(timerId);

            // add it in the remove set
            remove.add(timerId);
        }

        @Override
        public void beforeCompletion() {
            checkThread();
        }

        @Override
        public void afterCompletion(final int status) {
            checkThread();

            // if the tx was not committed, there is nothign to update
            if (status != Status.STATUS_COMMITTED) {
                return;
            }

            // add the new work
            taskStore.putAll(add);

            // remove work
            taskStore.keySet().removeAll(remove);

            tasksByTransaction.remove(tansactionReference.get());
        }
    }
}
