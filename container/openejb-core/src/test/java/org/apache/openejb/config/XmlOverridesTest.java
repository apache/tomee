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
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectableInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResSharingScope;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.jpa.unit.Persistence;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Stateful;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class XmlOverridesTest extends TestCase {

    public void test() throws Exception {
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        final StatefulBean bean = ejbJar.addEnterpriseBean(new StatefulBean(AnnotatedBean.class));

        bean.getEjbLocalRef().add(new EjbLocalRef(name("annotatedLocal"), "BarBean"));

        bean.getEnvEntry().add(new EnvEntry(name("striing"), "java.lang.Integer", "2"));
        bean.getEnvEntry().add(new EnvEntry(name("doouble"), "java.lang.String", "two"));
        bean.getEnvEntry().add(new EnvEntry(name("loong"), "java.lang.String", "three"));
        bean.getEnvEntry().add(new EnvEntry(name("flooat"), "java.lang.String", "four"));
        bean.getEnvEntry().add(new EnvEntry(name("inteeger"), "java.lang.String", "five"));
        bean.getEnvEntry().add(new EnvEntry(name("shoort"), "java.lang.String", "six"));
        bean.getEnvEntry().add(new EnvEntry(name("booolean"), "java.lang.String", "seven"));
        bean.getEnvEntry().add(new EnvEntry(name("byyte"), "java.lang.String", "eight"));
        bean.getEnvEntry().add(new EnvEntry(name("chaaracter"), "java.lang.String", "nine"));

        final EnvEntry lookupEntry = new EnvEntry(name("lookup"), "java.lang.String", null);
        lookupEntry.setLookupName("java:app/AppName");
        bean.getEnvEntry().add(lookupEntry);

        bean.getResourceRef().add(new ResourceRef(name("daataSource"), DataSource.class.getName(), ResAuth.CONTAINER, ResSharingScope.SHAREABLE));

        bean.getPersistenceUnitRef().add(new PersistenceUnitRef(name("emf"), "yellow"));

        bean.getPersistenceContextRef().add(new PersistenceContextRef(name("em"), "yellow", PersistenceContextType.TRANSACTION, new ArrayList(Arrays.asList(new Property("zzzz", "AAAA")))));

        final org.apache.openejb.jee.jpa.unit.PersistenceUnit persistenceUnit = new org.apache.openejb.jee.jpa.unit.PersistenceUnit("yellow");

        final AppModule app = new AppModule(this.getClass().getClassLoader(), "app");
        app.getEjbModules().add(new EjbModule(ejbJar));
        app.addPersistenceModule(new PersistenceModule("root", new Persistence(persistenceUnit)));

        final AppInfo appInfo = config.configureApplication(app);

        final EjbJarInfo ejbJarInfo = appInfo.ejbJars.get(0);
        final EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);
        final JndiEncInfo enc = beanInfo.jndiEnc;

        assertEquals("Enc.ejbLocalReferences.size()", 1, enc.ejbLocalReferences.size());
        assertEquals("Enc.ejbLocalReferences.get(0).link", "BarBean", enc.ejbLocalReferences.get(0).link);
        assertEquals("Enc.ejbReferences.size()", 0, enc.ejbReferences.size());

        assertEquals("Enc.envEntries.size()", 11, enc.envEntries.size()); // 10 + ComponentName
        final Map<String, EnvEntryInfo> entries = map(enc.envEntries);

        assertEnvEntry(entries, name("striing"), "java.lang.Integer", "2");
        assertEnvEntry(entries, name("doouble"), "java.lang.String", "two");
        assertEnvEntry(entries, name("loong"), "java.lang.String", "three");
        assertEnvEntry(entries, name("flooat"), "java.lang.String", "four");
        assertEnvEntry(entries, name("inteeger"), "java.lang.String", "five");
        assertEnvEntry(entries, name("shoort"), "java.lang.String", "six");
        assertEnvEntry(entries, name("booolean"), "java.lang.String", "seven");
        assertEnvEntry(entries, name("byyte"), "java.lang.String", "eight");
        assertEnvEntry(entries, name("chaaracter"), "java.lang.String", "nine");
        assertEnvEntryLookup(entries, name("lookup"), "java.lang.String", "java:app/AppName");

        assertEquals("Enc.persistenceContextRefs.size()", 1, enc.persistenceContextRefs.size());
        final PersistenceContextReferenceInfo context = enc.persistenceContextRefs.get(0);
        assertEquals("Context.extended", false, context.extended);
        assertEquals("Context.persistenceUnitName", "yellow", context.persistenceUnitName);
        assertEquals("Context.properties.size()", 1, context.properties.size());
        assertEquals("Context.properties.getProperty(\"zzzz\")", "AAAA", context.properties.getProperty("zzzz"));

        assertEquals("Enc.persistenceUnitRefs.size()", 1, enc.persistenceUnitRefs.size());
        final PersistenceUnitReferenceInfo unit = enc.persistenceUnitRefs.get(0);
        assertEquals("Unit.persistenceUnitName", "yellow", unit.persistenceUnitName);

        assertEquals("Enc.resourceRefs.size()", 1, enc.resourceRefs.size());
        final ResourceReferenceInfo resource = enc.resourceRefs.get(0);
        assertEquals("Resource.referenceAuth", "CONTAINER", resource.referenceAuth);

    }

    private void assertEnvEntry(final Map<String, EnvEntryInfo> entries, final String name, final String type, final String value) {
        final EnvEntryInfo entryInfo = entries.get(name);
        assertNotNull(name, entryInfo);
        assertEquals(name + ".type", type, entryInfo.type);
        assertEquals(name + ".value", value, entryInfo.value);
    }

    private void assertEnvEntryLookup(final Map<String, EnvEntryInfo> entries, final String name, final String type, final String lookup) {
        final EnvEntryInfo entryInfo = entries.get(name);
        assertNotNull(name, entryInfo);
        assertEquals(name + ".type", type, entryInfo.type);
        assertNull(name + ".value", entryInfo.value);
        assertNotNull(name + ".location", entryInfo.location);
        assertEquals(name + ".location.jndiName", lookup, entryInfo.location.jndiName);
    }

    private <T extends InjectableInfo> Map<String, T> map(final List<T> list) {
        try {
            final Map<String, T> entries = new HashMap<>();
            for (final T envEntry : list) {
                entries.put("java:" + envEntry.referenceName, envEntry);
            }
            return entries;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String name(final String name) {
//        return AnnotatedBean.class.getName() + "/" + name;
        return "java:comp/env/" + AnnotatedBean.class.getName() + "/" + name;
    }

    @Local
    public static interface AnnotatedLocal {

    }

    @Stateful
    public static class AnnotatedBean implements AnnotatedLocal {

        @EJB(beanName = "FooBean")
        private AnnotatedLocal annotatedLocal;

        @Resource
        private final String striing = "1";

        @Resource
        private final Double doouble = 1.0D;

        @Resource
        private final Long loong = 1L;

        @Resource
        private final Float flooat = 1.0F;

        @Resource
        private final Integer inteeger = 1;

        @Resource
        private final Short shoort = (short) 1;

        @Resource
        private final Boolean booolean = true;

        @Resource
        private final Byte byyte = (byte) 1;

        @Resource
        private final Character chaaracter = 'D';

        @Resource
        private String lookup;

        @Resource(authenticationType = Resource.AuthenticationType.APPLICATION, shareable = false)
        private DataSource daataSource;

        @PersistenceUnit(unitName = "orange")
        private EntityManagerFactory emf;

        @PersistenceContext(unitName = "orange", type = jakarta.persistence.PersistenceContextType.EXTENDED, properties = {@jakarta.persistence.PersistenceProperty(name = "zzzz", value = "ZZZZ")})
        private EntityManager em;
    }
}
