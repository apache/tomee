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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class MemoryTimerStore implements TimerStore {
    private static final Logger log = Logger.getInstance(LogCategory.TIMER, "org.apache.openejb.util.resources");
    private final Map<Long,TimerData> taskStore = new ConcurrentHashMap<Long,TimerData>();
    private final Map<Transaction,TimerDataView> tasksByTransaction = new ConcurrentHashMap<Transaction, TimerDataView>();
    private final AtomicLong counter = new AtomicLong(0);

    private final TransactionManager transactionManager;

    public MemoryTimerStore(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TimerData getTimer(String deploymentId, long timerId) {
        try {
            TimerDataView tasks = getTasks();
            TimerData timerData = tasks.getTasks().get(new Long(timerId));
            return timerData;
        } catch (TimerStoreException e) {
            return null;
        }
    }

    public Collection<TimerData> getTimers(String deploymentId) {
        try {
            TimerDataView tasks = getTasks();
            Collection<TimerData> timerDatas = new ArrayList<TimerData>(tasks.getTasks().values());
            return timerDatas;
        } catch (TimerStoreException e) {
            return Collections.emptySet();
        }
    }

    public Collection<TimerData> loadTimers(EjbTimerServiceImpl timerService, String deploymentId) throws TimerStoreException {
        TimerDataView tasks = getTasks();
        Collection<TimerData> timerDatas = new ArrayList<TimerData>(tasks.getTasks().values());
        return timerDatas;
    }

    // used to re-register a TimerData, if a cancel() is rolledback...
    public void addTimerData(TimerData timerData) throws TimerStoreException {
        getTasks().addTimerData(timerData);
    }

    @Override
    public TimerData createCalendarTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, ScheduleExpression scheduleExpression, TimerConfig timerConfig)
            throws TimerStoreException {
        long id = counter.incrementAndGet();
        TimerData timerData = new CalendarTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, scheduleExpression);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    @Override
    public TimerData createIntervalTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, Date initialExpiration, long intervalDuration, TimerConfig timerConfig)
            throws TimerStoreException {
        long id = counter.incrementAndGet();
        TimerData timerData = new IntervalTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, initialExpiration, intervalDuration);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    @Override
    public TimerData createSingleActionTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Method timeoutMethod, Date expiration, TimerConfig timerConfig) throws TimerStoreException {
        long id = counter.incrementAndGet();
        TimerData timerData = new SingleActionTimerData(id, timerService, deploymentId, primaryKey, timeoutMethod, timerConfig, expiration);
        getTasks().addTimerData(timerData);
        return timerData;
    }

    public void removeTimer(long id) {
        try {
            getTasks().removeTimerData(new Long(id));
        } catch (TimerStoreException e) {
            log.warning("Unable to remove timer data from memory store", e);
        }
    }

    public void updateIntervalTimer(TimerData timerData) {
    }

    private TimerDataView getTasks() throws TimerStoreException {
        Transaction transaction = null;
        int status = Status.STATUS_NO_TRANSACTION;
        try {
            transaction = transactionManager.getTransaction();
            if (transaction != null) {
                status = transaction.getStatus();
            }
        } catch (SystemException e) {
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
        Map<Long,TimerData> getTasks();

        void addTimerData(TimerData timerData);

        void removeTimerData(Long timerId);
    }

    private class LiveTimerDataView implements TimerDataView {
        public Map<Long,TimerData> getTasks() {
            return new TreeMap<Long,TimerData>(taskStore);
        }

        public void addTimerData(TimerData timerData) {
            taskStore.put(new Long(timerData.getId()), timerData);
        }

        public void removeTimerData(Long timerId) {
            taskStore.remove(timerId);
        }
    }

    private class TxTimerDataView implements Synchronization, TimerDataView {
        private final Map<Long,TimerData> add = new TreeMap<Long,TimerData>();
        private final Set<Long> remove = new TreeSet<Long>();
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
        public TxTimerDataView(Transaction transaction) throws TimerStoreException {
            // We're going to lock this timer inside this transaction and essentially
            // never let it go.  Any other threads attempting to invoke this object
            // will immediately throw an exception.
            lock.lock();
            concurentException = new IllegalThreadStateException("Object can only be invoked by Thread[" + Thread.currentThread().getName() + "] in Transaction[" + transaction + "]");
            concurentException.fillInStackTrace();
            try {
                transaction.registerSynchronization(this);
                tansactionReference = new WeakReference<Transaction>(transaction);
            } catch (RollbackException e) {
                throw new TimerStoreException("Transaction has been rolled back");
            } catch (SystemException e) {
                throw new TimerStoreException("Error registering transaction synchronization callback");
            }
        }

        private void checkThread() {
            if (!lock.tryLock()) throw new IllegalStateException("Illegal access by Thread[" + Thread.currentThread().getName() + "]", concurentException);
        }

        public Map<Long,TimerData> getTasks() {
            checkThread();
            TreeMap<Long, TimerData> allTasks = new TreeMap<Long, TimerData>();
            allTasks.putAll(taskStore);
            for (Long key : remove) allTasks.remove(key);
            allTasks.putAll(add);
            return Collections.unmodifiableMap(allTasks);
        }

        public void addTimerData(TimerData timerData) {
            checkThread();
            Long timerId = new Long(timerData.getId());

            // remove it from the remove set, if it is there
            remove.remove(timerId);

            // put it in the add set
            add.put(timerId, timerData);
        }

        public void removeTimerData(Long timerId) {
            checkThread();

            // remove it from the add set, if it is there
            add.remove(timerId);

            // add it in the remove set
            remove.add(timerId);
        }

        public void beforeCompletion() {
            checkThread();
        }

        public void afterCompletion(int status) {
            checkThread();

            // if the tx was not committed, there is nothign to update
            if (status != Status.STATUS_COMMITTED) return;

            // add the new work
            taskStore.putAll(add);

            // remove work
            taskStore.keySet().removeAll(remove);

            tasksByTransaction.remove(tansactionReference.get());
        }
    }
}
