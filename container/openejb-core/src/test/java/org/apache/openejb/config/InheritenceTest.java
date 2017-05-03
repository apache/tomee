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

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.CallbackInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.HandlerChainInfo;
import org.apache.openejb.assembler.classic.HandlerInfo;
import org.apache.openejb.assembler.classic.InitMethodInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.NamedMethodInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PortRefInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ReferenceLocationInfo;
import org.apache.openejb.assembler.classic.RemoveMethodInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatefulBean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.Topic;
import javax.sql.DataSource;
import java.rmi.RemoteException;
import java.util.List;
import java.util.ListIterator;

/**
 * @version $Rev$ $Date$
 */
public class InheritenceTest extends TestCase {

    public void test() throws Exception {
        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createContainer(config.configureService(StatefulSessionContainerInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatefulBean(Color.class));
        ejbJar.addEnterpriseBean(new StatefulBean(Red.class));
        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        final StatefulBeanInfo expected = (StatefulBeanInfo) ejbJarInfo.enterpriseBeans.get(0);
        final StatefulBeanInfo actual = (StatefulBeanInfo) ejbJarInfo.enterpriseBeans.get(1);


        assertEquals("transactionType", expected.transactionType, actual.transactionType);
        assertEquals("runAs", expected.runAs, actual.runAs);

        assertEquals("businessLocal", expected.businessLocal, actual.businessLocal);
        assertEquals("businessRemote", expected.businessRemote, actual.businessRemote);
        assertEquals("local", expected.local, actual.local);
        assertEquals("localHome", expected.localHome, actual.localHome);
        assertEquals("remote", expected.remote, actual.remote);
        assertEquals("home", expected.home, actual.home);

        assertEquals("timeout", expected.timeoutMethod, actual.timeoutMethod);

        assertCallbackInfos("postActivate", expected.postActivate, actual.postActivate);
        assertCallbackInfos("prePassivate", expected.prePassivate, actual.prePassivate);

        assertCallbackInfos("postConstruct", expected.postConstruct, actual.postConstruct);
        assertCallbackInfos("preDestroy", expected.preDestroy, actual.preDestroy);
        assertCallbackInfos("preDestroy", expected.aroundInvoke, actual.aroundInvoke);

        assertRemoveMethodInfos("removeMethods", expected.removeMethods, actual.removeMethods);
        assertInitMethodInfos("initMethods", expected.initMethods, actual.initMethods);

        assertSecurityRoleReferenceInfos("securityRoleReferences", expected.securityRoleReferences, actual.securityRoleReferences);

        // comp/ComponentName is different
        assertEquals(1, expected.jndiEnc.envEntries.size());
        assertEquals(1, actual.jndiEnc.envEntries.size());
        assertEquals("comp/ComponentName", expected.jndiEnc.envEntries.get(0).referenceName);
        assertEquals("Color", expected.jndiEnc.envEntries.get(0).value);
        assertEquals("comp/ComponentName", actual.jndiEnc.envEntries.get(0).referenceName);
        assertEquals("Red", actual.jndiEnc.envEntries.get(0).value);
        expected.jndiEnc.envEntries.clear();
        actual.jndiEnc.envEntries.clear();

        assertEquals("jndiEnc", expected.jndiEnc, actual.jndiEnc);
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    public static interface ColorLocal2 {
    }

    public static interface ColorRemote2 {
    }

    public static interface ColorEjbHome extends EJBHome {
        ColorEjbObject create() throws CreateException, RemoteException;
    }

    public static interface ColorEjbObject extends EJBObject {
    }

    public static interface ColorEjbLocalHome extends EJBLocalHome {
        ColorEjbLocalObject create() throws CreateException;
    }

    public static interface ColorEjbLocalObject extends EJBLocalObject {
    }


    @EJB(name = "colorClassEjb", beanInterface = ColorLocal.class)
    @Resource(name = "colorClassResource", type = DataSource.class)
    @RolesAllowed({"ColorManager"})
    @DeclareRoles({"ColorGuy", "ColorGal"})
    @RunAs("ColorManager")
    @TransactionManagement(TransactionManagementType.BEAN)
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @Local({ColorLocal2.class})
    @Remote({ColorRemote2.class})
    @LocalHome(ColorEjbLocalHome.class)
    @RemoteHome(ColorEjbHome.class)
    public static class Color implements ColorLocal, ColorRemote {

        @EJB
        private ColorRemote colorFieldEjb;

        @Resource
        private DataSource colorFieldResource;

        @Resource
        private Topic colorFieldResourceEnv;

        public void methodOne() {
        }

        public void methodTwo() {
        }

        public void methodThree() {
        }

        public void methodFour() {
        }

        @Timeout
        public void colorTimeout(final Timer timer) {
        }

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            return null;
        }

        @PostConstruct
        private void colorPostConstruct() {

        }

        @PreDestroy
        private void colorPreDestroy() {
        }

        @Init
        public void colorInit() {
        }

        @Remove
        public void colorRemove() {

        }

        @PrePassivate
        public void colorPrePassivate() {
        }

        @PostActivate
        public void colorPostActivate() {

        }
    }

    public static class Red extends Color {
    }


    // ------------------------------------------------------------------------------------------------------
    //
    //     Assert methods
    //
    // ------------------------------------------------------------------------------------------------------

    public static void assertEquals(final String m, final JndiEncInfo a, final JndiEncInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final JndiEncInfo a, final JndiEncInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (!equalsEnvEntryInfos(a.envEntries, b.envEntries)) return false;
        if (!equalsEjbReferenceInfos(a.ejbReferences, b.ejbReferences)) return false;
        if (!equalsEjbLocalReferenceInfos(a.ejbLocalReferences, b.ejbLocalReferences)) return false;
        if (!equalsResourceReferenceInfos(a.resourceRefs, b.resourceRefs)) return false;
        if (!equalsPersistenceUnitReferenceInfos(a.persistenceUnitRefs, b.persistenceUnitRefs)) return false;
        if (!equalsPersistenceContextReferenceInfos(a.persistenceContextRefs, b.persistenceContextRefs)) return false;
        if (!equalsResourceEnvReferenceInfos(a.resourceEnvRefs, b.resourceEnvRefs)) return false;
        if (!equalsServiceReferenceInfos(a.serviceRefs, b.serviceRefs)) return false;

        return true;
    }

    // -- ReferenceLocationInfo --------------------------------//

    public static void assertReferenceLocationInfos(final String s, final List<ReferenceLocationInfo> expected, final List<ReferenceLocationInfo> actual) {
        assertTrue(s, equalsReferenceLocationInfos(expected, actual));
    }

    public static boolean equalsReferenceLocationInfos(final List<ReferenceLocationInfo> expected, final List<ReferenceLocationInfo> actual) {
        return new ReferenceLocationInfoComparator().compare(expected, actual);
    }

    public static class ReferenceLocationInfoComparator extends ListComparator<ReferenceLocationInfo> {
        public boolean compare(final ReferenceLocationInfo a, final ReferenceLocationInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final ReferenceLocationInfo a, final ReferenceLocationInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final ReferenceLocationInfo a, final ReferenceLocationInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.jndiName != null ? !a.jndiName.equals(b.jndiName) : b.jndiName != null) return false;
        if (a.jndiProviderId != null ? !a.jndiProviderId.equals(b.jndiProviderId) : b.jndiProviderId != null)
            return false;

        return true;
    }

    // -- EnvEntryInfo --------------------------------//

    public static void assertEnvEntryInfos(final String s, final List<EnvEntryInfo> expected, final List<EnvEntryInfo> actual) {
        assertTrue(s, equalsEnvEntryInfos(expected, actual));
    }

    public static boolean equalsEnvEntryInfos(final List<EnvEntryInfo> expected, final List<EnvEntryInfo> actual) {
        return new EnvEntryInfoComparator().compare(expected, actual);
    }

    public static class EnvEntryInfoComparator extends ListComparator<EnvEntryInfo> {
        public boolean compare(final EnvEntryInfo a, final EnvEntryInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final EnvEntryInfo a, final EnvEntryInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final EnvEntryInfo a, final EnvEntryInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.type != null ? !a.type.equals(b.type) : b.type != null) return false;
        if (a.value != null ? !a.value.equals(b.value) : b.value != null) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- InjectionInfo --------------------------------//

    public static void assertInjectionInfos(final String s, final List<InjectionInfo> expected, final List<InjectionInfo> actual) {
        assertTrue(s, equalsInjectionInfos(expected, actual));
    }

    public static boolean equalsInjectionInfos(final List<InjectionInfo> expected, final List<InjectionInfo> actual) {
        return new InjectionInfoComparator().compare(expected, actual);
    }

    public static class InjectionInfoComparator extends ListComparator<InjectionInfo> {
        public boolean compare(final InjectionInfo a, final InjectionInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String s, final InjectionInfo a, final InjectionInfo b) {
        assertTrue(s, equals(a, b));
    }

    public static boolean equals(final InjectionInfo a, final InjectionInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.propertyName != null ? !a.propertyName.equals(b.propertyName) : b.propertyName != null) return false;
        if (a.className != null ? !a.className.equals(b.className) : b.className != null) return false;

        return true;
    }

    // -- EjbReferenceInfo --------------------------------//

    public static void assertEjbReferenceInfos(final String s, final List<EjbReferenceInfo> expected, final List<EjbReferenceInfo> actual) {
        assertTrue(s, equalsEjbReferenceInfos(expected, actual));
    }

    public static boolean equalsEjbReferenceInfos(final List<EjbReferenceInfo> expected, final List<EjbReferenceInfo> actual) {
        return new EjbReferenceInfoComparator().compare(expected, actual);
    }

    public static class EjbReferenceInfoComparator extends ListComparator<EjbReferenceInfo> {
        public boolean compare(final EjbReferenceInfo a, final EjbReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final EjbReferenceInfo a, final EjbReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final EjbReferenceInfo a, final EjbReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.homeClassName != null ? !a.homeClassName.equals(b.homeClassName) : b.homeClassName != null) return false;
        if (a.interfaceClassName != null ? !a.interfaceClassName.equals(b.interfaceClassName) : b.interfaceClassName != null)
            return false;
        if (a.ejbDeploymentId != null ? !a.ejbDeploymentId.equals(b.ejbDeploymentId) : b.ejbDeploymentId != null)
            return false;
        if (a.link != null ? !a.link.equals(b.link) : b.link != null) return false;
        if (a.externalReference != b.externalReference) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- EjbLocalReferenceInfo --------------------------------//

    public static void assertEjbLocalReferenceInfos(final String s, final List<EjbLocalReferenceInfo> expected, final List<EjbLocalReferenceInfo> actual) {
        assertTrue(s, equalsEjbLocalReferenceInfos(expected, actual));
    }

    public static boolean equalsEjbLocalReferenceInfos(final List<EjbLocalReferenceInfo> expected, final List<EjbLocalReferenceInfo> actual) {
        return new EjbLocalReferenceInfoComparator().compare(expected, actual);
    }

    public static class EjbLocalReferenceInfoComparator extends ListComparator<EjbLocalReferenceInfo> {
        public boolean compare(final EjbLocalReferenceInfo a, final EjbLocalReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    // -- ResourceReferenceInfo --------------------------------//

    public static void assertResourceReferenceInfos(final String s, final List<ResourceReferenceInfo> expected, final List<ResourceReferenceInfo> actual) {
        assertTrue(s, equalsResourceReferenceInfos(expected, actual));
    }

    public static boolean equalsResourceReferenceInfos(final List<ResourceReferenceInfo> expected, final List<ResourceReferenceInfo> actual) {
        return new ResourceReferenceInfoComparator().compare(expected, actual);
    }

    public static class ResourceReferenceInfoComparator extends ListComparator<ResourceReferenceInfo> {
        public boolean compare(final ResourceReferenceInfo a, final ResourceReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final ResourceReferenceInfo a, final ResourceReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final ResourceReferenceInfo a, final ResourceReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.referenceType != null ? !a.referenceType.equals(b.referenceType) : b.referenceType != null) return false;
        if (a.referenceAuth != null ? !a.referenceAuth.equals(b.referenceAuth) : b.referenceAuth != null) return false;
        if (a.resourceID != null ? !a.resourceID.equals(b.resourceID) : b.resourceID != null) return false;
        if (a.properties != null ? !a.properties.equals(b.properties) : b.properties != null) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- PersistenceUnitReferenceInfo --------------------------------//

    public static void assertPersistenceUnitReferenceInfos(final String s, final List<PersistenceUnitReferenceInfo> expected, final List<PersistenceUnitReferenceInfo> actual) {
        assertTrue(s, equalsPersistenceUnitReferenceInfos(expected, actual));
    }

    public static boolean equalsPersistenceUnitReferenceInfos(final List<PersistenceUnitReferenceInfo> expected, final List<PersistenceUnitReferenceInfo> actual) {
        return new PersistenceUnitReferenceInfoComparator().compare(expected, actual);
    }

    public static class PersistenceUnitReferenceInfoComparator extends ListComparator<PersistenceUnitReferenceInfo> {
        public boolean compare(final PersistenceUnitReferenceInfo a, final PersistenceUnitReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final PersistenceUnitReferenceInfo a, final PersistenceUnitReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final PersistenceUnitReferenceInfo a, final PersistenceUnitReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.persistenceUnitName != null ? !a.persistenceUnitName.equals(b.persistenceUnitName) : b.persistenceUnitName != null)
            return false;
        if (a.unitId != null ? !a.unitId.equals(b.unitId) : b.unitId != null) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- PersistenceContextReferenceInfo --------------------------------//

    public static void assertPersistenceContextReferenceInfos(final String s, final List<PersistenceContextReferenceInfo> expected, final List<PersistenceContextReferenceInfo> actual) {
        assertTrue(s, equalsPersistenceContextReferenceInfos(expected, actual));
    }

    public static boolean equalsPersistenceContextReferenceInfos(final List<PersistenceContextReferenceInfo> expected, final List<PersistenceContextReferenceInfo> actual) {
        return new PersistenceContextReferenceInfoComparator().compare(expected, actual);
    }

    public static class PersistenceContextReferenceInfoComparator extends ListComparator<PersistenceContextReferenceInfo> {
        public boolean compare(final PersistenceContextReferenceInfo a, final PersistenceContextReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final PersistenceContextReferenceInfo a, final PersistenceContextReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final PersistenceContextReferenceInfo a, final PersistenceContextReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.persistenceUnitName != null ? !a.persistenceUnitName.equals(b.persistenceUnitName) : b.persistenceUnitName != null)
            return false;
        if (a.unitId != null ? !a.unitId.equals(b.unitId) : b.unitId != null) return false;
        if (a.extended != b.extended) return false;
        if (a.properties != null ? !a.properties.equals(b.properties) : b.properties != null) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- ResourceEnvReferenceInfo --------------------------------//

    public static void assertResourceEnvReferenceInfos(final String s, final List<ResourceEnvReferenceInfo> expected, final List<ResourceEnvReferenceInfo> actual) {
        assertTrue(s, equalsResourceEnvReferenceInfos(expected, actual));
    }

    public static boolean equalsResourceEnvReferenceInfos(final List<ResourceEnvReferenceInfo> expected, final List<ResourceEnvReferenceInfo> actual) {
        return new ResourceEnvReferenceInfoComparator().compare(expected, actual);
    }

    public static class ResourceEnvReferenceInfoComparator extends ListComparator<ResourceEnvReferenceInfo> {
        public boolean compare(final ResourceEnvReferenceInfo a, final ResourceEnvReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final ResourceEnvReferenceInfo a, final ResourceEnvReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final ResourceEnvReferenceInfo a, final ResourceEnvReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null)
            return false;
        if (a.resourceEnvRefType != null ? !a.resourceEnvRefType.equals(b.resourceEnvRefType) : b.resourceEnvRefType != null)
            return false;
        if (a.mappedName != null ? !a.mappedName.equals(b.mappedName) : b.mappedName != null) return false;
        if (a.resourceID != null ? !a.resourceID.equals(b.resourceID) : b.resourceID != null) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- ServiceReferenceInfo --------------------------------//

    public static void assertServiceReferenceInfos(final String s, final List<ServiceReferenceInfo> expected, final List<ServiceReferenceInfo> actual) {
        assertTrue(s, equalsServiceReferenceInfos(expected, actual));
    }

    public static boolean equalsServiceReferenceInfos(final List<ServiceReferenceInfo> expected, final List<ServiceReferenceInfo> actual) {
        return new ServiceReferenceInfoComparator().compare(expected, actual);
    }

    public static class ServiceReferenceInfoComparator extends ListComparator<ServiceReferenceInfo> {
        public boolean compare(final ServiceReferenceInfo a, final ServiceReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }


    public static void assertEquals(final String m, final ServiceReferenceInfo a, final ServiceReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final ServiceReferenceInfo a, final ServiceReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.referenceName != null ? !a.referenceName.equals(b.referenceName) : b.referenceName != null) return false;
        if (a.serviceQName != null ? !a.serviceQName.equals(b.serviceQName) : b.serviceQName != null) return false;
        if (a.serviceType != null ? !a.serviceType.equals(b.serviceType) : b.serviceType != null) return false;
        if (a.portQName != null ? !a.portQName.equals(b.portQName) : b.portQName != null) return false;
        if (a.referenceType != null ? !a.referenceType.equals(b.referenceType) : b.referenceType != null) return false;
        if (a.wsdlFile != null ? !a.wsdlFile.equals(b.wsdlFile) : b.wsdlFile != null) return false;
        if (a.jaxrpcMappingFile != null ? !a.jaxrpcMappingFile.equals(b.jaxrpcMappingFile) : b.jaxrpcMappingFile != null)
            return false;
        if (a.id != null ? !a.id.equals(b.id) : b.id != null) return false;
        if (!equalsHandlerChainInfos(a.handlerChains, b.handlerChains)) return false;
        if (!equalsPortRefInfos(a.portRefs, b.portRefs)) return false;
        if (!equals(a.location, b.location)) return false;
        if (!equalsInjectionInfos(a.targets, b.targets)) return false;

        return true;
    }

    // -- HandlerChainInfo --------------------------------//

    public static void assertHandlerChainInfos(final String s, final List<HandlerChainInfo> expected, final List<HandlerChainInfo> actual) {
        assertTrue(s, equalsHandlerChainInfos(expected, actual));
    }

    public static boolean equalsHandlerChainInfos(final List<HandlerChainInfo> expected, final List<HandlerChainInfo> actual) {
        return new HandlerChainInfoComparator().compare(expected, actual);
    }

    public static class HandlerChainInfoComparator extends ListComparator<HandlerChainInfo> {
        public boolean compare(final HandlerChainInfo a, final HandlerChainInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String s, final HandlerChainInfo a, final HandlerChainInfo b) {
        assertTrue(s, equals(a, b));
    }

    public static boolean equals(final HandlerChainInfo a, final HandlerChainInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.serviceNamePattern != null ? !a.serviceNamePattern.equals(b.serviceNamePattern) : b.serviceNamePattern != null)
            return false;
        if (a.portNamePattern != null ? !a.portNamePattern.equals(b.portNamePattern) : b.portNamePattern != null)
            return false;
        if (a.protocolBindings != null ? !a.protocolBindings.equals(b.protocolBindings) : b.protocolBindings != null)
            return false;
        if (!equalsHandlerInfos(a.handlers, b.handlers)) return false;

        return true;
    }

    // -- HandlerInfo --------------------------------//

    public static void assertHandlerInfos(final String s, final List<HandlerInfo> expected, final List<HandlerInfo> actual) {
        assertTrue(s, equalsHandlerInfos(expected, actual));
    }

    public static boolean equalsHandlerInfos(final List<HandlerInfo> expected, final List<HandlerInfo> actual) {
        return new HandlerInfoComparator().compare(expected, actual);
    }

    public static class HandlerInfoComparator extends ListComparator<HandlerInfo> {
        public boolean compare(final HandlerInfo a, final HandlerInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String s, final HandlerInfo a, final HandlerInfo b) {
        assertTrue(s, equals(a, b));
    }

    public static boolean equals(final HandlerInfo a, final HandlerInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.handlerClass != null ? !a.handlerClass.equals(b.handlerClass) : b.handlerClass != null) return false;
        if (a.handlerName != null ? !a.handlerName.equals(b.handlerName) : b.handlerName != null) return false;
        if (a.initParams != null ? !a.initParams.equals(b.initParams) : b.initParams != null) return false;
        if (a.soapHeaders != null ? !a.soapHeaders.equals(b.soapHeaders) : b.soapHeaders != null) return false;
        if (a.soapRoles != null ? !a.soapRoles.equals(b.soapRoles) : b.soapRoles != null) return false;

        return true;
    }

    // -- PortRefInfo --------------------------------//

    public static void assertPortRefInfos(final String s, final List<PortRefInfo> expected, final List<PortRefInfo> actual) {
        assertTrue(s, equalsPortRefInfos(expected, actual));
    }

    public static boolean equalsPortRefInfos(final List<PortRefInfo> expected, final List<PortRefInfo> actual) {
        return new PortRefInfoComparator().compare(expected, actual);
    }

    public static class PortRefInfoComparator extends ListComparator<PortRefInfo> {
        public boolean compare(final PortRefInfo a, final PortRefInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String s, final PortRefInfo a, final PortRefInfo b) {
        assertTrue(s, equals(a, b));
    }

    public static boolean equals(final PortRefInfo a, final PortRefInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.qname != null ? !a.qname.equals(b.qname) : b.qname != null) return false;
        if (a.serviceEndpointInterface != null ? !a.serviceEndpointInterface.equals(b.serviceEndpointInterface) : b.serviceEndpointInterface != null)
            return false;
        if (a.properties != null ? !a.properties.equals(b.properties) : b.properties != null) return false;
        if (a.enableMtom != b.enableMtom) return false;

        return true;
    }

    // -- CallbackInfo --------------------------------//

    public static void assertCallbackInfos(final String s, final List<CallbackInfo> expected, final List<CallbackInfo> actual) {
        assertTrue(s, equalsCallbackInfos(expected, actual));
    }

    public static boolean equalsCallbackInfos(final List<CallbackInfo> expected, final List<CallbackInfo> actual) {
        return new CallbackInfoComparator().compare(expected, actual);
    }

    public static class CallbackInfoComparator extends ListComparator<CallbackInfo> {
        public boolean compare(final CallbackInfo a, final CallbackInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }


    public static void assertEquals(final String m, final CallbackInfo a, final CallbackInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final CallbackInfo a, final CallbackInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.className != null ? !a.className.equals(b.className) : b.className != null) return false;
        if (a.method != null ? !a.method.equals(b.method) : b.method != null) return false;
        return true;
    }

    // -- SecurityRoleReferenceInfo --------------------------------//

    public static void assertSecurityRoleReferenceInfos(final String s, final List<SecurityRoleReferenceInfo> expected, final List<SecurityRoleReferenceInfo> actual) {
        assertTrue(s, equalsSecurityRoleReferenceInfos(expected, actual));
    }

    public static boolean equalsSecurityRoleReferenceInfos(final List<SecurityRoleReferenceInfo> expected, final List<SecurityRoleReferenceInfo> actual) {
        return new SecurityRoleReferenceInfoComparator().compare(expected, actual);
    }

    public static class SecurityRoleReferenceInfoComparator extends ListComparator<SecurityRoleReferenceInfo> {
        public boolean compare(final SecurityRoleReferenceInfo a, final SecurityRoleReferenceInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final SecurityRoleReferenceInfo a, final SecurityRoleReferenceInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final SecurityRoleReferenceInfo a, final SecurityRoleReferenceInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.roleName != null ? !a.roleName.equals(b.roleName) : b.roleName != null) return false;
        if (a.roleLink != null ? !a.roleLink.equals(b.roleLink) : b.roleLink != null) return false;
        return true;
    }

    // -- RemoveMethodInfo --------------------------------//

    public static void assertRemoveMethodInfos(final String s, final List<RemoveMethodInfo> expected, final List<RemoveMethodInfo> actual) {
        assertTrue(s, equalsRemoveMethodInfos(expected, actual));
    }

    public static boolean equalsRemoveMethodInfos(final List<RemoveMethodInfo> expected, final List<RemoveMethodInfo> actual) {
        return new RemoveMethodInfoComparator().compare(expected, actual);
    }

    public static class RemoveMethodInfoComparator extends ListComparator<RemoveMethodInfo> {
        public boolean compare(final RemoveMethodInfo a, final RemoveMethodInfo b) {
            return InheritenceTest.equals(a, b);
        }
    }

    public static void assertEquals(final String m, final RemoveMethodInfo a, final RemoveMethodInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final RemoveMethodInfo a, final RemoveMethodInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        return a.retainIfException == b.retainIfException && equals(a.beanMethod, b.beanMethod);

    }

    public static void assertInitMethodInfos(final String s, final List<InitMethodInfo> expected, final List<InitMethodInfo> actual) {
        assertTrue(s, equalsInitMethodInfos(expected, actual));
    }

    public static boolean equalsInitMethodInfos(final List<InitMethodInfo> expected, final List<InitMethodInfo> actual) {
        return new InitMethodInfoComparator().compare(expected, actual);
    }

    public static class InitMethodInfoComparator extends ListComparator<InitMethodInfo> {
        public boolean compare(final InitMethodInfo o1, final InitMethodInfo o2) {
            return InheritenceTest.equals(o1, o2);
        }
    }

    public static void assertEquals(final String m, final InitMethodInfo a, final InitMethodInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final InitMethodInfo a, final InitMethodInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (!equals(a.beanMethod, b.beanMethod)) return false;
        if (!equals(a.createMethod, b.createMethod)) return false;
        return true;
    }

    public static void assertEquals(final String m, final NamedMethodInfo a, final NamedMethodInfo b) {
        assertTrue(m, equals(a, b));
    }

    public static boolean equals(final NamedMethodInfo a, final NamedMethodInfo b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        if (a.id != null ? !a.id.equals(b.id) : b.id != null) return false;
        if (a.methodName != null ? !a.methodName.equals(b.methodName) : b.methodName != null) return false;
        if (a.methodParams != null ? !a.methodParams.equals(b.methodParams) : b.methodParams != null) return false;
        return true;
    }

    public static void assertList(final String s, final List expected, final List actual, final ListComparator comparator) {
        assertTrue(s, comparator.compare(expected, actual));
    }

    public static abstract class ListComparator<T> {

        public boolean compare(final List<T> listA, final List<T> listB) {
            if (listA == listB) return true;

            final ListIterator iA = listA.listIterator();
            final ListIterator iB = listB.listIterator();
            while (iA.hasNext() && iB.hasNext()) {
                final T a = (T) iA.next();
                final T b = (T) iB.next();
                if (!(a == null ? b == null : compare(a, b))) {
                    return false;
                }
            }
            return !(iA.hasNext() || iB.hasNext());
        }

        public abstract boolean compare(T o1, T o2);

    }
}
