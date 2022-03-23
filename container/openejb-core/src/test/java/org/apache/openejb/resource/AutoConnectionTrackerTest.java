/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.AbstractConnectionManager;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ConnectorReference;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.junit.Ignore;

import jakarta.annotation.Resource;
import jakarta.ejb.Remote;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.lang.SecurityException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * @version $Rev$ $Date$
 */
@Ignore
public class AutoConnectionTrackerTest extends TestCase {

    public static final int LOOP_SIZE = 200;
    public static final int NUM_THREADS = 4;

    @Ignore
    public void test() throws Exception {
        System.setProperty("openejb.log.async", "false");
        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CONNECTOR, AutoConnectionTrackerTest.class);
        logger.info("Starting test");
        final java.util.logging.Logger julLogger = LogManager.getLogManager().getLogger(LogCategory.OPENEJB_CONNECTOR.getName());
        final LogCaptureHandler logCapture = new LogCaptureHandler();
        julLogger.addHandler(logCapture);

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        // System services
        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // JMS persistence datasource
        final ResourceInfo dataSourceInfo = config.configureService("Default Unmanaged JDBC Database", ResourceInfo.class);
        dataSourceInfo.properties.setProperty("JdbcUrl", "jdbc:hsqldb:mem:MdbConfigTest");
        assembler.createResource(dataSourceInfo);

        // FakeRA
        final ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.service = "Resource";
        resourceInfo.className = FakeRA.class.getName();
        resourceInfo.id = "FakeRA";
        resourceInfo.properties = new Properties();
        assembler.createResource(resourceInfo);

        // FakeRA container
        final ContainerInfo containerInfo = config.configureService(MdbContainerInfo.class);
        containerInfo.id = "FakeContainer";
        containerInfo.displayName = "Fake Container";
        containerInfo.properties.setProperty("ResourceAdapter", "FakeRA");
        containerInfo.properties.setProperty("MessageListenerInterface", FakeMessageListener.class.getName());
        containerInfo.properties.setProperty("ActivationSpecClass", FakeActivationSpec.class.getName());
        assembler.createContainer(containerInfo);

        // Fake connection factory
        final ResourceInfo mcfResourceInfo = new ResourceInfo();
        mcfResourceInfo.className = FakeManagedConnectionFactory.class.getName();
        mcfResourceInfo.id = "FakeConnectionFactory";
        mcfResourceInfo.types = Collections.singletonList(FakeConnectionFactory.class.getName());
        mcfResourceInfo.properties = new Properties();
        mcfResourceInfo.properties.setProperty("ResourceAdapter", "FakeRA");
        mcfResourceInfo.properties.setProperty("TransactionSupport", "None");
        mcfResourceInfo.properties.setProperty("allConnectionsEqual", "false");
        assembler.createResource(mcfResourceInfo);

        // generate ejb jar application
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean("TestBean", FakeStatelessBean.class));
        final EjbModule ejbModule = new EjbModule(getClass().getClassLoader(), "FakeEjbJar", "fake.jar", ejbJar, null);

        // configure and deploy it
        final EjbJarInfo info = config.configureApplication(ejbModule);
        assembler.createEjbJar(info);

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final FakeConnectionFactory cf = (FakeConnectionFactory) containerSystem.getJNDIContext().lookup("openejb:Resource/FakeConnectionFactory");
        final FakeRemote bean = (FakeRemote) containerSystem.getJNDIContext().lookup("java:global/FakeEjbJar/FakeEjbJar/TestBean!org.apache.openejb.resource.AutoConnectionTrackerTest$FakeRemote");


        {
            logCapture.clear();
            runTest(new Runnable() {
                @Override
                public void run() {
                    try {
                        bean.nonLeakyTxMethod();
                        System.gc();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            System.gc();
            cf.getConnection().close();
            assertLogs(logCapture, 0, "Transaction complete, but connection still has handles associated");
            assertLogs(logCapture, 0, "Detected abandoned connection");
            assertTrue(getConnectionCount((FakeConnectionFactoryImpl) cf) > 0);
        }
        {
            logCapture.clear();
            runTest(new Runnable() {
                @Override
                public void run() {
                    try {
                        bean.nonleakyNonTxMethod();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            System.gc();
            cf.getConnection().close();
            assertLogs(logCapture, 0, "Transaction complete, but connection still has handles associated");
            assertLogs(logCapture, 0, "Detected abandoned connection");
            assertTrue(getConnectionCount((FakeConnectionFactoryImpl) cf) > 0);
        }
        {
            logCapture.clear();
            bean.leakyTxMethod();
            System.gc();

            final AutoConnectionTracker tracker = getAutoConnectionTracker((FakeConnectionFactoryImpl) cf);
            tracker.setEnvironment(null, null);
            assertLogs(logCapture, 1, "Transaction complete, but connection still has handles associated");
            assertLogs(logCapture, 1, "Detected abandoned connection");
        }
        {
            logCapture.clear();
            bean.leakyNonTxMethod();
            System.gc();

            final AutoConnectionTracker tracker = getAutoConnectionTracker((FakeConnectionFactoryImpl) cf);
            tracker.setEnvironment(null, null);
            assertLogs(logCapture, 1, "Detected abandoned connection");
        }
    }

    // this is a very quick and dirty hack for debugging purpose
    private void assertLogs(final LogCaptureHandler logCapture, final int times, final String message) {
        final int iteration = 5;
        final int waitSeconds = 2;

        AssertionFailedError failure = null;

        for (int i = 0 ; i < iteration ; i++) {
            try {
                assertEquals(message, times, logCapture.find(message).size());
                return;

            } catch (final AssertionFailedError e) {
                if (failure == null) { // keep the first issue
                    failure = e;
                }

                try {
                    Thread.sleep(waitSeconds * 1000);
                } catch (final InterruptedException e1) {
                    // no-op
                }
            }
        }

        throw failure;
    }

    private AutoConnectionTracker getAutoConnectionTracker(final FakeConnectionFactoryImpl cf) throws Exception {
        final Field field = AbstractConnectionManager.class.getDeclaredField("interceptors");
        field.setAccessible(true);
        final Object o = field.get(cf.connectionManager);
        final Field stackField = o.getClass().getDeclaredField("stack");
        stackField.setAccessible(true);
        final ConnectionTrackingInterceptor cti = (ConnectionTrackingInterceptor) stackField.get(o);
        final Field connectionTrackerField = ConnectionTrackingInterceptor.class.getDeclaredField("connectionTracker");
        connectionTrackerField.setAccessible(true);
        AutoConnectionTracker tracker = (AutoConnectionTracker) connectionTrackerField.get(cti);
        return tracker;
    }

    private int getConnectionCount(FakeConnectionFactoryImpl cf) {
        final PoolingSupport pooling = ((GenericConnectionManager) cf.connectionManager).getPooling();
        return pooling.getConnectionCount();
    }

    private void runTest(final Runnable testCode) throws InterruptedException {
        final CountDownLatch startingLine = new CountDownLatch(NUM_THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(NUM_THREADS * LOOP_SIZE);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    startingLine.countDown();
                    start.await();
                    for (int i = 0; i < LOOP_SIZE; i++) {
                        testCode.run();
                        end.countDown();
                    }
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        };

        for (int i = 0; i < NUM_THREADS; i++) {
            new Thread(runnable).start();
        }

        startingLine.await(1, TimeUnit.MINUTES);
        start.countDown();
        end.await(1, TimeUnit.MINUTES);
    }

    public interface FakeRemote {
        void leakyTxMethod() throws Exception;
        void nonLeakyTxMethod() throws Exception;
        void leakyNonTxMethod() throws Exception;
        void nonleakyNonTxMethod() throws Exception;
    }

    @Remote
    public static class FakeStatelessBean implements FakeRemote {

        @Resource(name="FakeConnectionFactory")
        private FakeConnectionFactory cf;

        @Override
        public void leakyTxMethod() throws Exception {
            final FakeConnection connection = cf.getConnection();// this leaks!
            connection.sendMessage("Test message");
        }

        @Override
        public void nonLeakyTxMethod() throws Exception {
            final FakeConnection connection = cf.getConnection();
            connection.sendMessage("Test message");
            connection.close();
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public void leakyNonTxMethod() throws Exception {
            final FakeConnection connection = cf.getConnection();// this leaks!
            connection.sendMessage("Test message");
        }

        @Override
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public void nonleakyNonTxMethod() throws Exception {
            final FakeConnection connection = cf.getConnection();
            connection.sendMessage("Test message");
            connection.close();
        }
    }

    public static class FakeMdb implements FakeMessageListener {
        public void doIt(final Properties properties) {
        }
    }

    public interface FakeMessageListener {
        void doIt(Properties properties);
    }

    public static class FakeRA implements ResourceAdapter {
        public boolean started;

        public void start(final BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
        }

        public void stop() {
            assertTrue("RA was not started", started);
        }

        public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
        }

        public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
        }

        public XAResource[] getXAResources(final ActivationSpec[] activationSpecs) throws ResourceException {
            return new XAResource[0];
        }
    }

    public static class FakeActivationSpec implements ActivationSpec {
        private FakeRA fakeRA;
        protected boolean validated;

        public void validate() throws InvalidPropertyException {
            validated = true;
        }

        public FakeRA getResourceAdapter() {
            return fakeRA;
        }

        public void setResourceAdapter(final ResourceAdapter resourceAdapter) {
            assertNotNull("resourceAdapter is null", resourceAdapter);
            assertTrue("resourceAdapter should be an instance of FakeRA", resourceAdapter instanceof FakeRA);
            this.fakeRA = (FakeRA) resourceAdapter;
            assertTrue("ActivationSpec has not been validated", validated);
        }
    }

    public interface FakeConnection {
        void sendMessage(final String message);
        void close();
    }

    public static class FakeConnectionImpl implements FakeConnection {
        private final FakeManagedConnection mc;
        private final FakeManagedConnectionFactory mcf;

        public FakeConnectionImpl(FakeManagedConnection mc, FakeManagedConnectionFactory mcf) {
            this.mc = mc;
            this.mcf = mcf;
        }

        @Override
        public void sendMessage(final String message) {
            mc.sendMessage(message);
        }

        @Override
        public void close() {
            mc.closeHandle(this);
        }
    }

    public static class FakeManagedConnection implements ManagedConnection {
        private final Logger logger = Logger.getInstance(LogCategory.OPENEJB_CONNECTOR, FakeManagedConnection.class);
        private final FakeManagedConnectionFactory mcf;
        private final List<ConnectionEventListener> listeners = new ArrayList<>();
        private FakeConnection connection;
        private PrintWriter writer;

        public FakeManagedConnection(FakeManagedConnectionFactory mcf) {
            this.mcf = mcf;
        }

        @Override
        public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            connection = new FakeConnectionImpl(this, mcf);
            return connection;
        }

        @Override
        public void destroy() throws ResourceException {
        }

        @Override
        public void cleanup() throws ResourceException {
        }

        @Override
        public void associateConnection(Object connection) throws ResourceException {
            if (connection == null)
                throw new ResourceException("Null connection handle");

            if (!(connection instanceof FakeConnectionImpl))
                throw new ResourceException("Wrong connection handle");

            this.connection = (FakeConnectionImpl) connection;
        }

        @Override
        public void addConnectionEventListener(final ConnectionEventListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener is null");
            }

            listeners.add(listener);
        }

        @Override
        public void removeConnectionEventListener(final ConnectionEventListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("Listener is null");
            }

            listeners.remove(listener);
        }

        @Override
        public XAResource getXAResource() throws ResourceException {
            throw new NotSupportedException("getXAResource() not supported");
        }

        @Override
        public LocalTransaction getLocalTransaction() throws ResourceException {
            throw new NotSupportedException("getLocalTransaction() not supported");
        }

        @Override
        public ManagedConnectionMetaData getMetaData() throws ResourceException {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter writer) throws ResourceException {
            this.writer = writer;
        }

        @Override
        public PrintWriter getLogWriter() throws ResourceException {
            return writer;
        }

        void closeHandle(final FakeConnection handle) {
            final ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            event.setConnectionHandle(handle);
            for (ConnectionEventListener cel : listeners) {
                cel.connectionClosed(event);
            }
        }

        public void sendMessage(final String message) {
            logger.info(message);
        }
    }

    public interface FakeConnectionFactory {
        FakeConnection getConnection() throws ResourceException;
    }

    public static class FakeConnectionFactoryImpl implements FakeConnectionFactory {

        private final FakeManagedConnectionFactory mcf;
        private final ConnectionManager connectionManager;

        public FakeConnectionFactoryImpl(final FakeManagedConnectionFactory mcf, final ConnectionManager connectionManager) {
            this.mcf = mcf;
            this.connectionManager = connectionManager;
        }

        @Override
        public FakeConnection getConnection() throws ResourceException {
            return (FakeConnection) connectionManager.allocateConnection(mcf, null);
        }
    }

    public static class FakeManagedConnectionFactory implements ManagedConnectionFactory {

        private PrintWriter writer;

        @Override
        public Object createConnectionFactory(final ConnectionManager cxManager) throws ResourceException {
            return new FakeConnectionFactoryImpl(this, cxManager);
        }

        @Override
        public Object createConnectionFactory() throws ResourceException {
            throw new ResourceException("This resource adapter doesn't support non-managed environments");
        }

        @Override
        public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            return new FakeManagedConnection(this);
        }

        @Override
        public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
            ManagedConnection result = null;
            Iterator it = connectionSet.iterator();
            while (result == null && it.hasNext()) {
                ManagedConnection mc = (ManagedConnection) it.next();
                if (mc instanceof FakeManagedConnection) {
                    result = mc;
                }

            }
            return result;
        }

        @Override
        public void setLogWriter(PrintWriter writer) throws ResourceException {
            this.writer = writer;
        }

        @Override
        public PrintWriter getLogWriter() throws ResourceException {
            return writer;
        }
    }

    public static class LogCaptureHandler extends Handler {

        private List<LogRecord> recordList = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void publish(final LogRecord record) {
            recordList.add(record);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

        public List<LogRecord> find(final String message) {
            final List<LogRecord> allRecords = new ArrayList<>(recordList);
            final List<LogRecord> matchingRecords = new ArrayList<>();

            for (final LogRecord record : allRecords) {
                if (record.getMessage().contains(message)) {
                    matchingRecords.add(record);
                }
            }

            return matchingRecords;
        }

        public void clear() {
            recordList.clear();
        }
    }
}
