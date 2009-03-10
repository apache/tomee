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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
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
        Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "test");
        ConfigurationFactory factory = new ConfigurationFactory();

        URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddapp1");
        File file = URLs.toFile(resource);
        AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());
    }

    public void testMultitplePrefixes() throws Exception {
        Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "footest, test");
        ConfigurationFactory factory = new ConfigurationFactory();

        URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddapp2");
        File file = URLs.toFile(resource);
        AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());

        EjbJarInfo ejbJar = appInfo.ejbJars.get(0);

        // was the footest.ejb-jar.xml picked up
        assertEquals("EjbJar.enterpriseBeans", 1, ejbJar.enterpriseBeans.size());
        assertEquals("EjbJar.interceptors.size()", 1, ejbJar.interceptors.size());

        // was the test.env-entries.properties picked up
        assertEquals("EjbJar.enterpriseBeans.get(0).jndiEnc.envEntries.size()", 4, ejbJar.enterpriseBeans.get(0).jndiEnc.envEntries.size());


    }

    public class OrangeBean implements OrangeLocal {

        public int echo(int i) {
            return i;
        }
    }

    public interface OrangeLocal {
        int echo(int i);
    }

    public static class FooTestInterceptor {
        @AroundInvoke
        public Object invoke(InvocationContext context) throws Exception {
            return context.proceed();
        }
    }
}
