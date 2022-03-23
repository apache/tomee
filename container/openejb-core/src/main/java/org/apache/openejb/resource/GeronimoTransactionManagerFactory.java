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


package org.apache.openejb.resource;

import org.apache.geronimo.transaction.log.HOWLLog;
import org.apache.geronimo.transaction.manager.ExponentialtIntervalRetryScheduler;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.api.resource.DestroyableResource;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoTransactionManagerFactory {

    private static final byte[] DEFAULT_TM_ID = new byte[]{71, 84, 77, 73, 68};
    private static final int DEFAULT_BUFFER_SIZE = 32;

    public static GeronimoTransactionManager create(Integer defaultTransactionTimeoutSeconds, // Deprecated, use defaultTransactionTimeout
                                                    final Duration defaultTransactionTimeout,
                                                    final boolean txRecovery,
                                                    final byte[] tmId,
                                                    final String bufferClassName,
                                                    final int bufferSizeKb,
                                                    final boolean checksumEnabled,
                                                    final boolean adler32Checksum,
                                                    Integer flushSleepTimeMilliseconds, // Deprecated, use flushSleepTime
                                                    final Duration flushSleepTime,
                                                    final String logFileDir,
                                                    final String logFileExt,
                                                    final String logFileName,
                                                    final int maxBlocksPerFile,
                                                    final int maxBuffers,
                                                    final int maxLogFiles,
                                                    final int minBuffers,
                                                    final int threadsWaitingForceThreshold) throws Exception {

        if (flushSleepTime.getUnit() == null) {
            flushSleepTime.setUnit(TimeUnit.MILLISECONDS);
        }
        if (flushSleepTimeMilliseconds == null) {
            flushSleepTimeMilliseconds = (int) TimeUnit.MILLISECONDS.convert(flushSleepTime.getTime(), flushSleepTime.getUnit());
        }

        if (defaultTransactionTimeout.getUnit() == null) {
            defaultTransactionTimeout.setUnit(TimeUnit.SECONDS);
        }
        if (defaultTransactionTimeoutSeconds == null) {
            defaultTransactionTimeoutSeconds = (int) TimeUnit.SECONDS.convert(defaultTransactionTimeout.getTime(), defaultTransactionTimeout.getUnit());
        }

        XidFactory xidFactory = null;
        TransactionLog txLog = null;
        if (txRecovery) {
            SystemInstance.get().setComponent(XAResourceWrapper.class, new GeronimoXAResourceWrapper());

            xidFactory = new XidFactoryImpl(tmId == null ? DEFAULT_TM_ID : tmId);
            txLog = new HOWLLog(bufferClassName == null ? "org.objectweb.howl.log.BlockLogBuffer" : bufferClassName,
                    bufferSizeKb == 0 ? DEFAULT_BUFFER_SIZE : bufferSizeKb,
                    checksumEnabled,
                    adler32Checksum,
                    flushSleepTimeMilliseconds,
                    logFileDir,
                    logFileExt,
                    logFileName,
                    maxBlocksPerFile,
                    maxBuffers,
                    maxLogFiles,
                    minBuffers,
                    threadsWaitingForceThreshold,
                    xidFactory,
                    SystemInstance.get().getBase().getDirectory("."));
            ((HOWLLog) txLog).doStart();
        }

        final GeronimoTransactionManager geronimoTransactionManager = new DestroyableTransactionManager(defaultTransactionTimeoutSeconds, xidFactory, txLog);
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management")
                .set("j2eeType", "TransactionManager");
        LocalMBeanServer.registerDynamicWrapperSilently(
                new TransactionManagerMBean(geronimoTransactionManager, defaultTransactionTimeout, txLog),
                jmxName.build());

        return geronimoTransactionManager;
    }

    public static class DestroyableTransactionManager extends GeronimoTransactionManager implements DestroyableResource {
        private final TransactionLog txLog;

        public DestroyableTransactionManager(final int defaultTransactionTimeoutSeconds, final XidFactory xidFactory, final TransactionLog transactionLog) throws XAException {
            super(defaultTransactionTimeoutSeconds, xidFactory, transactionLog);
            this.txLog = transactionLog;
        }

        @Override
        public void destroyResource() {
            // try to clean up
            try {
                final Field f = TransactionManagerImpl.class.getDeclaredField("retryScheduler");
                f.setAccessible(true);
                final ExponentialtIntervalRetryScheduler rs = ExponentialtIntervalRetryScheduler.class.cast(f.get(this));

                final Field t = ExponentialtIntervalRetryScheduler.class.getDeclaredField("timer");
                t.setAccessible(true);

                final Timer timer = Timer.class.cast(t.get(rs));
                timer.cancel();
            } catch (final Throwable notImportant) {
                // no-op
            }
            if (txLog != null) {
                try {
                    HOWLLog.class.cast(txLog).doStop();
                } catch (final Throwable /*Exception + NoClassDefFoundError*/ e) {
                    Logger.getInstance(LogCategory.OPENEJB, DestroyableTransactionManager.class).error(e.getMessage(), e);
                }
            }
        }

        @Override
        public void begin() throws NotSupportedException, SystemException {
            try {
                super.begin();
            } catch (final NotSupportedException nse) {
                final RuntimeException re = CoreUserTransaction.error();
                if (re != null) {
                    throw re;
                }
                throw nse;
            }
        }
    }

    public static class GeronimoXAResourceWrapper implements XAResourceWrapper {
        public XAResource wrap(final XAResource xaResource, final String name) {
            return new WrapperNamedXAResource(xaResource, name);
        }
    }

    @MBean
    @Internal
    @Description("Transaction manager statistics")
    public static final class TransactionManagerMBean {

        private final GeronimoTransactionManager transactionManager;
        private final Duration defaultTransactionTimeout;
        private final TransactionLog txLog;

        public TransactionManagerMBean(final GeronimoTransactionManager transactionManager, final Duration defaultTransactionTimeout, final TransactionLog txLog) {
            this.transactionManager = transactionManager;
            this.defaultTransactionTimeout = defaultTransactionTimeout;
            this.txLog = txLog;
        }

        @ManagedAttribute
        @Description("Number of active transactions")
        public long getActive() {
            return transactionManager.getActiveCount();
        }

        @ManagedAttribute
        @Description("Number of committed transactions")
        public long getCommits() {
            return transactionManager.getTotalCommits();
        }

        @ManagedAttribute
        @Description("Number of rolled back transactions")
        public long getRollbacks() {
            return transactionManager.getTotalRollbacks();
        }

        @ManagedOperation
        @Description("Reset statistics counters")
        public void resetStatistics() {
            transactionManager.resetStatistics();
        }

        @ManagedAttribute
        @Description("Display the default transaction timeout")
        public String getDefaultTransactionTimeout() {
            return defaultTransactionTimeout.toString();
        }

    }

}
