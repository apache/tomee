/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.core.timer;

import org.apache.openejb.util.Logger;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryTimerStore implements TimerStore {
    private static final Logger log = Logger.getInstance("Timer", "org.apache.openejb.util.resources");
    private final Map<Long,TimerData> taskStore = new ConcurrentHashMap<Long,TimerData>();
    private final Map<Transaction,TimerDataView> tasksByTransaction = new HashMap<Transaction, TimerDataView>();
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

    public TimerData createTimer(EjbTimerServiceImpl timerService, String deploymentId, Object primaryKey, Object info, Date expiration, long intervalDuration) throws TimerStoreException {
        long id = counter.incrementAndGet();
        TimerData timerData = new TimerData(id, timerService, deploymentId, primaryKey, info, expiration, intervalDuration);
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
        private final Map<Long,TimerData> tasks;
        private final Map<Long,TimerData> add = new TreeMap<Long,TimerData>();
        private final Set<Long> remove = new TreeSet<Long>();

        public TxTimerDataView(Transaction transaction) throws TimerStoreException {
            try {
                transaction.registerSynchronization(this);
            } catch (RollbackException e) {
                throw new TimerStoreException("Transaction has been rolled back");
            } catch (SystemException e) {
                throw new TimerStoreException("Error registering transaction synchronization callback");
            }
            this.tasks = new TreeMap<Long,TimerData>(taskStore);
        }

        public Map<Long,TimerData> getTasks() {
            return Collections.unmodifiableMap(tasks);
        }

        public void addTimerData(TimerData timerData) {
            Long timerId = new Long(timerData.getId());

            // if it was previously removed...
            if (remove.contains(timerId)) {
                // remove it from the remove set
                remove.remove(timerId);
                // put the work back into the current tasks set
                tasks.put(timerId, timerData);

            } else {
                // if it is not in the current tasks
                if (!tasks.containsKey(timerId)) {
                    // put it in the add set
                    add.put(timerId, timerData);

                    // put the work into the current tasks set
                    tasks.put(timerId, timerData);
                }
            }
        }

        public void removeTimerData(Long timerId) {
            // if it was previously added...
            if (add.containsKey(timerId)) {
                // remove it from the add set
                add.remove(timerId);
                // re-remove the work from the current tasks set
                tasks.remove(timerId);

            } else {
                // if it is in the current tasks
                if (tasks.containsKey(timerId)) {
                    // add it in the remove set
                    remove.add(timerId);

                    // remove the work from the current tasks set
                    tasks.remove(timerId);
                }
            }
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int status) {
            // if the tx was not committed, there is nothign to update
            if (status != Status.STATUS_COMMITTED) return;

            // add the new work
            taskStore.putAll(add);

            // remove work
            taskStore.keySet().removeAll(remove);

        }
    }
}
