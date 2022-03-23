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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.TransactionSupportType;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.ApplicationException;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import javax.naming.NamingException;
import javax.naming.Reference;
import jakarta.resource.Referenceable;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MessageListener;
import jakarta.resource.cci.Record;
import jakarta.resource.spi.Activation;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.AdministeredObject;
import jakarta.resource.spi.AuthenticationMechanism;
import jakarta.resource.spi.AuthenticationMechanism.CredentialInterface;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.SecurityPermission;
import jakarta.resource.spi.TransactionSupport.TransactionSupportLevel;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import jakarta.resource.spi.work.WorkContext;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployerTest {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    /**
     *  For http://issues.apache.org/jira/browse/OPENEJB-980
     */
    public void applicationExceptionInheritanceTest() throws Exception {
        EjbModule ejbModule = testModule();
        final AnnotationDeployer.DiscoverAnnotatedBeans discvrAnnBeans = new AnnotationDeployer.DiscoverAnnotatedBeans();
        ejbModule = discvrAnnBeans.deploy(ejbModule);

        final AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();
        org.apache.openejb.jee.ApplicationException appEx =
            assemblyDescriptor.getApplicationException(BusinessException.class);
        assertThat(appEx, notNullValue());
        assertThat(appEx.getExceptionClass(), is(BusinessException.class.getName()));
        assertThat(appEx.isRollback(), is(true));

        //inheritance is now handled at runtime, only explicitly mentioned exceptions are in the assembly descriptor
        appEx = assemblyDescriptor.getApplicationException(ValueRequiredException.class);
        assertThat(appEx, nullValue());
    }

    private EjbModule testModule() {
        final EjbJar ejbJar = new EjbJar("test-classes");
        final EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.setFinder(new ClassFinder(AnnotationDeployerTest.class,
            BusinessException.class,
            Exception.class,
            GenericInterface.class,
            InterceptedSLSBean.class,
            MyMainClass.class,
            TestLocalBean.class,
            ValueRequiredException.class
        ));
        return ejbModule;
    }


    @Test
    public void testSortClasses() throws Exception {
        final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Emerald.class)).link();

        final List<Annotated<Class<?>>> classes = finder.findMetaAnnotatedClasses(Resource.class);
        assertTrue(classes.size() >= 3);

        final List<Annotated<Class<?>>> sorted = AnnotationDeployer.sortClasses(classes);

        assertTrue(sorted.size() >= 3);

        assertEquals(Emerald.class, sorted.get(0).get());
        assertEquals(Green.class, sorted.get(1).get());
        assertEquals(Color.class, sorted.get(2).get());
    }

    @Test
    public void testSortMethods() throws Exception {
        final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Emerald.class)).link();

        final List<Annotated<Method>> classes = finder.findMetaAnnotatedMethods(Resource.class);
        assertTrue(classes.size() >= 3);

        final List<Annotated<Method>> sorted = AnnotationDeployer.sortMethods(classes);

        assertTrue(sorted.size() >= 3);

        assertEquals(Emerald.class, sorted.get(0).get().getDeclaringClass());
        assertEquals(Green.class, sorted.get(1).get().getDeclaringClass());
        assertEquals(Color.class, sorted.get(2).get().getDeclaringClass());
    }

    @Test
    /**
     *  For https://issues.apache.org/jira/browse/OPENEJB-1063
     */
    public void badMainClassFormatTest() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        final AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        final ClientModule clientModule = new ClientModule(null, app.getClassLoader(), app.getJarLocation(), null, null);

        // change "." --> "/" to check that main class is changed by the AnnotationDeployer
        final String mainClass = MyMainClass.class.getName().replaceAll("\\.", "/");
        clientModule.setMainClass(mainClass);

        app.getClientModules().add(clientModule);

        final AppInfo appInfo = config.configureApplication(app);

        assembler.createApplication(appInfo);

        final ClientInfo clientInfo = appInfo.clients.get(0);
        Assert.assertNotNull(clientInfo);
        Assert.assertEquals(MyMainClass.class.getName(), clientInfo.mainClass);
    }

    /**
     * For https://issues.apache.org/jira/browse/OPENEJB-1128
     */
    @Test
    public void interceptingGenericBusinessMethodCalls() throws Exception {
        EjbModule ejbModule = testModule();
        final EjbJar ejbJar = ejbModule.getEjbJar();

        final AnnotationDeployer.DiscoverAnnotatedBeans discvrAnnBeans = new AnnotationDeployer.DiscoverAnnotatedBeans();
        ejbModule = discvrAnnBeans.deploy(ejbModule);

        final EnterpriseBean bean = ejbJar.getEnterpriseBean("InterceptedSLSBean");
        assert bean != null;
    }

    /**
     * For https://issues.apache.org/jira/browse/OPENEJB-1188
     *
     * @throws Exception
     */
    @Test
    public void testLocalBean() throws Exception {
        final EjbModule ejbModule = testModule();
        final EjbJar ejbJar = ejbModule.getEjbJar();

        AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), "myapp");
        appModule.getEjbModules().add(ejbModule);

        final AnnotationDeployer annotationDeployer = new AnnotationDeployer();
        appModule = annotationDeployer.deploy(appModule);

        EnterpriseBean bean = ejbJar.getEnterpriseBean("TestLocalBean");
        assert bean != null;
        assert (((SessionBean) bean).getLocalBean() != null);

        bean = ejbJar.getEnterpriseBean("InterceptedSLSBean");
        assert bean != null;
        assert (((SessionBean) bean).getLocalBean() == null);
    }

    @Test
    public void testResourceAdapter() throws Exception {
        final ConnectorModule connectorModule = testConnectorModule();
        final AnnotationDeployer.DiscoverAnnotatedBeans discvrAnnBeans = new AnnotationDeployer.DiscoverAnnotatedBeans();
        discvrAnnBeans.deploy(connectorModule);

        final Connector connector = connectorModule.getConnector();
        Assert.assertEquals("displayName", connector.getDisplayName());
        Assert.assertEquals("description", connector.getDescription());
        Assert.assertEquals("eisType", connector.getEisType());
        Assert.assertEquals("vendorName", connector.getVendorName());
        Assert.assertEquals("version", connector.getResourceAdapterVersion());
        Assert.assertEquals("smallIcon", connector.getIcon().getSmallIcon());
        Assert.assertEquals("largeIcon", connector.getIcon().getLargeIcon());
        Assert.assertEquals("licenseDescription", connector.getLicense().getDescription());
        Assert.assertEquals(true, connector.getLicense().isLicenseRequired());

        final List<org.apache.openejb.jee.SecurityPermission> securityPermission = connector.getResourceAdapter().getSecurityPermission();
        Assert.assertEquals("description", securityPermission.get(0).getDescription());
        Assert.assertEquals("permissionSpec", securityPermission.get(0).getSecurityPermissionSpec());

        final List<String> requiredWorkContext = connector.getRequiredWorkContext();
        Assert.assertEquals(TestWorkContext.class.getName(), requiredWorkContext.get(0));

        final List<org.apache.openejb.jee.AuthenticationMechanism> authenticationMechanism = connector.getResourceAdapter().getOutboundResourceAdapter().getAuthenticationMechanism();
        Assert.assertEquals("authMechanism", authenticationMechanism.get(0).getAuthenticationMechanismType());
        Assert.assertEquals(CredentialInterface.GenericCredential.toString(), authenticationMechanism.get(0).getCredentialInterface());
        Assert.assertEquals("description", authenticationMechanism.get(0).getDescription());

        Assert.assertEquals(TransactionSupportType.NO_TRANSACTION, connector.getResourceAdapter().getOutboundResourceAdapter().getTransactionSupport());
        Assert.assertEquals(true, connector.getResourceAdapter().getOutboundResourceAdapter().isReauthenticationSupport());

        Assert.assertEquals(Connection.class.getName(), connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition().get(0).getConnectionInterface());
        Assert.assertEquals(ConnectionImpl.class.getName(), connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition().get(0).getConnectionImplClass());
        Assert.assertEquals(ConnectionFactory.class.getName(), connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition().get(0).getConnectionFactoryInterface());
        Assert.assertEquals(ConnectionFactoryImpl.class.getName(), connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition().get(0).getConnectionFactoryImplClass());

        Assert.assertEquals(TestActivation.class.getName(), connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter().getMessageListener().get(0).getActivationSpec().getActivationSpecClass());
        Assert.assertEquals(TestMessageListener.class.getName(), connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter().getMessageListener().get(0).getMessageListenerType());

        Assert.assertEquals(TestAdminObject.class.getName(), connector.getResourceAdapter().getAdminObject().get(0).getAdminObjectClass());
        Assert.assertEquals(TestAdminObjectInterface.class.getName(), connector.getResourceAdapter().getAdminObject().get(0).getAdminObjectInterface());
    }

    private ConnectorModule testConnectorModule() {
        final Connector connector = new Connector();
        final ConnectorModule connectorModule = new ConnectorModule(connector);
        connectorModule.setFinder(new ClassFinder(TestConnector.class, TestManagedConnectionFactory.class, TestActivation.class, TestAdminObject.class));
        return connectorModule;
    }

    @Test
    public void testConfigProperties() throws Exception {
        final ClassFinder finder = new ClassFinder(TestAdminObject.class);

        final List<ConfigProperty> configProperty = new ArrayList<>();

        final Object object = new Object() {
            public List<ConfigProperty> getConfigProperty() {
                return configProperty;
            }
        };

        new AnnotationDeployer.DiscoverAnnotatedBeans().process(null, TestAdminObject.class.getName(), object);
        Assert.assertEquals(2, configProperty.size());
        Assert.assertEquals("myNumber", configProperty.get(0).getConfigPropertyName());
        Assert.assertEquals("java.lang.Integer", configProperty.get(0).getConfigPropertyType());
        Assert.assertEquals("myProperty", configProperty.get(1).getConfigPropertyName());
        Assert.assertEquals("java.lang.String", configProperty.get(1).getConfigPropertyType());
        Assert.assertEquals("This is a test", configProperty.get(1).getConfigPropertyValue());
    }

    @ApplicationException(rollback = true)
    public abstract class BusinessException extends Exception {
    }

    public class ValueRequiredException extends BusinessException {
    }

    public static final class MyMainClass {
        public static void main(final String[] args) {
        }
    }

    public static interface GenericInterface<T> {
        T genericMethod(T t);
    }

    @Stateless
    @Local(GenericInterface.class)
    public static class InterceptedSLSBean implements GenericInterface<String> {
        public String genericMethod(final String s) {
            return s;
        }
    }

    @Stateless
    @LocalBean
    public static class TestLocalBean {
        public String echo(final String input) {
            return input;
        }
    }

    @Resource
    public static class Color {
        @Resource
        public void color() {
        }
    }

    @Resource
    public static class Green extends Color {
        @Resource
        public void green() {
        }
    }

    @Resource
    public static class Emerald extends Green {
        @Resource
        public void emerald() {
        }
    }

    @jakarta.resource.spi.Connector(description = "description",
        displayName = "displayName", smallIcon = "smallIcon",
        largeIcon = "largeIcon", vendorName = "vendorName",
        eisType = "eisType",
        version = "version",
        licenseDescription = {"licenseDescription"},
        licenseRequired = true,
        authMechanisms = {@AuthenticationMechanism(authMechanism = "authMechanism",
            credentialInterface = CredentialInterface.GenericCredential, description = {"description"})},
        reauthenticationSupport = true,
        securityPermissions = {@SecurityPermission(permissionSpec = "permissionSpec", description = "description")},
        transactionSupport = TransactionSupportLevel.NoTransaction,
        requiredWorkContexts = {TestWorkContext.class}
    )
    public static class TestConnector implements ResourceAdapter {

        public void endpointActivation(final MessageEndpointFactory mef, final ActivationSpec spec) throws ResourceException {
        }

        public void endpointDeactivation(final MessageEndpointFactory mef, final ActivationSpec spec) {
        }

        public XAResource[] getXAResources(final ActivationSpec[] specs) throws ResourceException {
            return null;
        }

        public void start(final BootstrapContext ctx) throws ResourceAdapterInternalException {
        }

        public void stop() {
        }
    }

    @ConnectionDefinition(connection = Connection.class, connectionFactory = ConnectionFactory.class, connectionImpl = ConnectionImpl.class, connectionFactoryImpl = ConnectionFactoryImpl.class)
    public static class TestManagedConnectionFactory implements ManagedConnectionFactory {

        public Object createConnectionFactory() throws ResourceException {
            return null;
        }

        public Object createConnectionFactory(final ConnectionManager connectionManager) throws ResourceException {
            return null;
        }

        public ManagedConnection createManagedConnection(final Subject subject, final ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
            return null;
        }

        public PrintWriter getLogWriter() throws ResourceException {
            return null;
        }

        public ManagedConnection matchManagedConnections(final Set managedConnections, final Subject subject, final ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
            return null;
        }

        public void setLogWriter(final PrintWriter writer) throws ResourceException {
        }

    }

    public static class TestWorkContext implements WorkContext {
        public String getDescription() {
            return "Description";
        }

        public String getName() {
            return "Name";
        }
    }

    public static interface Connection {
    }

    public static class ConnectionImpl implements Connection {
    }

    public static interface ConnectionFactory extends Serializable, Referenceable {
    }

    public static class ConnectionFactoryImpl implements ConnectionFactory {

        public void setReference(final Reference reference) {
        }

        public Reference getReference() throws NamingException {
            return null;
        }
    }

    @Activation(messageListeners = {TestMessageListener.class})
    public static class TestActivation implements ActivationSpec, Serializable {

        public ResourceAdapter getResourceAdapter() {
            return null;
        }

        public void setResourceAdapter(final ResourceAdapter arg0) throws ResourceException {
        }

        public void validate() throws InvalidPropertyException {
        }
    }

    public static class TestMessageListener implements MessageListener {
        public Record onMessage(final Record arg0) throws ResourceException {
            return null;
        }
    }

    public static interface TestAdminObjectInterface {
    }

    public static interface SomeOtherInterface {
    }

    @AdministeredObject(adminObjectInterfaces = {TestAdminObjectInterface.class})
    public static class TestAdminObject implements TestAdminObjectInterface, SomeOtherInterface {
        private String myProperty = "This is a test";

        @jakarta.resource.spi.ConfigProperty(ignore = true)
        private int myNumber;

        public String getMyProperty() {
            return myProperty;
        }

        public void setMyProperty(final String myProperty) {
            this.myProperty = myProperty;
        }

        public int getMyNumber() {
            return myNumber;
        }

        public void setMyNumber(final int myNumber) {
            this.myNumber = myNumber;
        }
    }

    @Test
    public void findRestClasses() throws Exception {
        final WebApp webApp = new WebApp();
        webApp.setContextRoot("/");
        webApp.setId("web");
        webApp.setVersion("2.5");
        WebModule webModule = new WebModule(webApp, webApp.getContextRoot(), Thread.currentThread().getContextClassLoader(), "myapp", webApp.getId());
        webModule.setFinder(new AnnotationFinder(new ClassesArchive(RESTClass.class, RESTMethod.class, RESTApp.class)).link());

        final AnnotationDeployer annotationDeployer = new AnnotationDeployer();
        webModule = annotationDeployer.deploy(webModule);

        final Set<String> classes = webModule.getRestClasses();
        final Set<String> applications = webModule.getRestApplications();

        assertEquals(1, classes.size());
        assertTrue(classes.contains(RESTClass.class.getName()));
        // assertTrue(classes.contains(RESTMethod.class.getName()));

        assertEquals(1, applications.size());
        assertEquals(RESTApp.class.getName(), applications.iterator().next());
    }

    @Path("/")
    public static class RESTClass {

    }

    public static class RESTMethod extends RESTClass {
        @Path("/method")
        public void noop() {
            // no-op
        }
    }

    @ApplicationPath("/")
    public static class RESTApp extends Application {
        public java.util.Set<java.lang.Class<?>> getClasses() {
            return new HashSet<Class<?>>() {{
                add(RESTClass.class);
                add(RESTMethod.class);
            }};
        }

        public java.util.Set<java.lang.Object> getSingletons() {
            return new HashSet<Object>() {{
                add(new RESTMethod());
                add(new RESTMethod());
            }};
        }
    }
}
