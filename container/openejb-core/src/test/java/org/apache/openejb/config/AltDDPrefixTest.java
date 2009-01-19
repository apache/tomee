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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;

import java.net.URL;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class AltDDPrefixTest extends TestCase {

    public void test() throws Exception {
        Assembler assmbler = new Assembler();
        SystemInstance.get().setProperty("openejb.altdd.prefix", "test");
        ConfigurationFactory factory = new ConfigurationFactory();

        URL resource = AltDDPrefixTest.class.getClassLoader().getResource("altddapp");
        File file = URLs.toFile(resource);
        AppInfo appInfo = factory.configureApplication(file);
        assertNotNull(appInfo);
        assertEquals(1, appInfo.ejbJars.size());
    }
}
