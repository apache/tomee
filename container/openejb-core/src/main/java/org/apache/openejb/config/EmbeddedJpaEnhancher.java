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

import org.apache.openejb.javaagent.AgentExtention;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.xbean.finder.ResourceFinder;

import javax.naming.NamingException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class EmbeddedJpaEnhancher implements AgentExtention {
    public void premain(String agentArgs, Instrumentation instrumentation) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        ClassLoader appClassLoader = new TemporaryClassLoader(classLoader);
        AppModule appModule = new AppModule(appClassLoader, classLoader.toString());

        // Persistence Units via META-INF/persistence.xml
        try {
            ResourceFinder finder = new ResourceFinder("", appClassLoader);
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            appModule.getAltDDs().put("persistence.xml", persistenceUrls);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load persistence-units from 'META-INF/persistence.xml' : " + e.getMessage(), e);
        }

        try {
            ConfigurationFactory configFactory = new ConfigurationFactory();
            AppInfo appInfo = configFactory.configureApplication(appModule);

            Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
            if (assembler == null) {
                assembler = new Assembler();
            }

            assembler.createApplication(appInfo);
            assembler.destroyApplication(appInfo.jarPath);
        } catch (Exception e) {
            throw new IllegalStateException("Enhancement failed: "+ e.getMessage(), e);
        }
    }
}
