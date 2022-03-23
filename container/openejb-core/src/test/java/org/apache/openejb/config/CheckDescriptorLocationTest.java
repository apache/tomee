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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.config.rules.Key;
import org.apache.openejb.config.rules.KeyType;
import org.apache.openejb.config.rules.Keys;
import org.apache.openejb.config.rules.ValidationRunner;
import org.apache.openejb.util.Archives;
import org.apache.openejb.util.WebArchives;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RunWith(ValidationRunner.class)
public class CheckDescriptorLocationTest {

    public static final String FILENAME_PREFIX = "ValTest";

    @Keys({@Key(value = "descriptor.incorrectLocation", type = KeyType.WARNING)})
    public AppModule testJar() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("ejb-jar.xml", "<ejb-jar/>"); // Place the descriptor in
        // incorrect location (directly
        // under root)

        final File jarFile = Archives.jarArchive(map, FILENAME_PREFIX, FooBean.class);
        final DeploymentLoader loader = new DeploymentLoader();
        final AppModule appModule = loader.load(jarFile, null);

        return appModule;
    }


    @Keys({@Key(value = "descriptor.incorrectLocation", type = KeyType.WARNING, count = 1)})
    public AppModule testWarWithDescriptorInMetaInf() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("META-INF/ejb-jar.xml", "<ejb-jar/>");
        map.put("WEB-INF/web.xml", "<web-app/>");

        // Place the descriptor in
        // incorrect location (directly
        // under root)
        final File warFile = WebArchives.warArchive(map, FILENAME_PREFIX, FooBean.class);

        final DeploymentLoader loader = new DeploymentLoader();
        final AppModule appModule = loader.load(warFile, null);

        return appModule;
    }


    @Keys({@Key(value = "descriptor.incorrectLocation", type = KeyType.WARNING, count = 1)})
    public AppModule testWarWithDescriptorInRoot() throws Exception {
        final Map<String, String> map = new HashMap<>();
        map.put("ejb-jar.xml", "<ejb-jar/>");
        map.put("WEB-INF/web.xml", "<web-app/>");

        // Place the descriptor in
        // incorrect location (directly
        // under root)
        final File warFile = WebArchives.warArchive(map, FILENAME_PREFIX, FooBean.class);

        final DeploymentLoader loader = new DeploymentLoader();
        final AppModule appModule = loader.load(warFile, null);

        return appModule;
    }


    @Stateless
    class FooBean {
        // need to add this @AroundInvoke to cause validation to fail. Validation does not
        // fail on warnings, which causes this framework to not work properly
        @AroundInvoke
        public void sayCheese() {
        }
    }
}