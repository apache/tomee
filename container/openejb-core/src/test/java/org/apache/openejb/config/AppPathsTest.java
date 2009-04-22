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
import org.apache.openejb.util.URLs;

import java.io.File;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class AppPathsTest extends TestCase {

    public void test() {}

    /**
     * Seems like this may not be a feature that can be supported on
     * all platforms.  Seems to work on the mac VM, but not the linux vm.
     * @throws Exception
     */
    public void _testMixedCaseMetaInf() throws Exception {
        Assembler assmbler = new Assembler();
        ConfigurationFactory factory = new ConfigurationFactory();

        URL resource = AppPathsTest.class.getClassLoader().getResource("mixedcase");
        File file = URLs.toFile(resource);

        AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());

        EjbJarInfo ejbJar = appInfo.ejbJars.get(0);

        // was the footest.ejb-jar.xml picked up
        assertEquals("EjbJar.enterpriseBeans", 1, ejbJar.enterpriseBeans.size());
    }

    public class OrangeBean implements OrangeLocal {

        public int echo(int i) {
            return i;
        }
    }

    public interface OrangeLocal {
        int echo(int i);
    }

}