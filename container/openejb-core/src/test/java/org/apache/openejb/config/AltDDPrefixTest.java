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
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class AltDDPrefixTest extends TestCase {

    /**
     * This module has only a "test.ejb-jar.xml" file and no equivalent
     * non-test ejb-jar.xml file.  It should still get discovered even though
     * there is not an ejb-jar.xml file and only a test.ejb-jar.xml file
     *
     * @throws Exception
     */
    public void testTestOnlyModule() throws Exception {
        System.out.println("*** testTestOnlyModule ***");
        final Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "test");
        DeploymentLoader.reloadAltDD();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddapp1");
        final File file = URLs.toFile(resource);
        final AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());
    }

    public void testMultitplePrefixes() throws Exception {
        System.out.println("*** testMultitplePrefixes ***");
        final Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "footest, test");
        DeploymentLoader.reloadAltDD();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddapp2");
        final File file = URLs.toFile(resource);
        final AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());

        final EjbJarInfo ejbJar = appInfo.ejbJars.get(0);

        // was the footest.ejb-jar.xml picked up
        assertEquals("EjbJar.enterpriseBeans", 1, ejbJar.enterpriseBeans.size());
        assertEquals("EjbJar.interceptors.size()", 1, ejbJar.interceptors.size());

        // was the test.env-entries.properties picked up
        // 4 + ComponentName
        assertEquals("EjbJar.enterpriseBeans.get(0).jndiEnc.envEntries.size()", 5, ejbJar.enterpriseBeans.get(0).jndiEnc.envEntries.size());


    }

    /**
     * OPENEJB-1059: altdd does not work with a persistence.xml when no embedded
     * in EjbModule/Client module.
     *
     * @throws Exception if something wrong happen
     */
    public void testPersistenceUnit() throws Exception {
        System.out.println("*** testPersistenceUnit ***");
        final Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "footest, test");
        DeploymentLoader.reloadAltDD();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddPU1");
        final File file = URLs.toFile(resource);
        final AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(0, appInfo.ejbJars.size());
        assertEquals(1, appInfo.persistenceUnits.size());

        final PersistenceUnitInfo info = appInfo.persistenceUnits.get(0);
        assertTrue(info.id.startsWith("footest-unit ")); // a space must be present before hashcode

//        appInfo = factory.configureApplication(file);
    }

    /**
     * OPENEJB-1059: altdd does not work with a persistence.xml when no embedded
     * in EjbModule/Client module.
     *
     * @throws Exception if something wrong happen
     */
    public void testPersistenceUnitWithAllDD() throws Exception {
        System.out.println("*** testPersistenceUnitWithAllDD ***");
        final Assembler assmbler = new Assembler();
        // TODO should be better to add a remove property method
        SystemInstance.get().getProperties().remove("openejb.altdd.prefix");
        DeploymentLoader.reloadAltDD();
        final ConfigurationFactory factory = new ConfigurationFactory();

        final URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddPU1");
        final File file = URLs.toFile(resource);
        final AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(0, appInfo.ejbJars.size());
        assertEquals(1, appInfo.persistenceUnits.size());

        final PersistenceUnitInfo info = appInfo.persistenceUnits.get(0);
        assertTrue(info.id.startsWith("unit ")); // a space must be present before hashcode
    }

    public class OrangeBean implements OrangeLocal {

        public int echo(final int i) {
            return i;
        }
    }

    public interface OrangeLocal {
        int echo(int i);
    }

    public static class FooTestInterceptor {
        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            return context.proceed();
        }
    }
}
