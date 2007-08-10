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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.PersistenceBuilder;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.javaagent.AgentExtention;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.Property;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.xbean.finder.ResourceFinder;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
            // read the persistence.xml files
            ReadDescriptors readDescriptors = new ReadDescriptors();
            readDescriptors.deploy(appModule);

            // convert the xml to info objects
            Collection<PersistenceUnitInfo> infos = createPersistenceUnitInfos(appModule);


            // create the factories
            PersistenceBuilder persistenceBuilder = new PersistenceBuilder(new PersistenceClassLoaderHandlerImpl());
            for (PersistenceUnitInfo info : infos) {
                try {
                    // For OpenJPA we only need to create the EMF to cause the enhancer to
                    // be added.  This may not work for other JPA implementations.
                    persistenceBuilder.createEntityManagerFactory(info, classLoader);
                } catch (Exception e) {
                    throw new OpenEJBException(e);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Enhancement failed: "+ e.getMessage(), e);
        }
    }

    private Collection<PersistenceUnitInfo> createPersistenceUnitInfos(AppModule appModule) {
        Collection<PersistenceUnitInfo> persistenceUnits = new ArrayList<PersistenceUnitInfo>();
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            String rootUrl = persistenceModule.getRootUrl();
            Persistence persistence = persistenceModule.getPersistence();
            for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
                PersistenceUnitInfo info = new PersistenceUnitInfo();
                info.name = persistenceUnit.getName();
                info.persistenceUnitRootUrl = rootUrl;
                info.provider = persistenceUnit.getProvider();
                info.transactionType = persistenceUnit.getTransactionType().toString();

                Boolean excludeUnlistedClasses = persistenceUnit.isExcludeUnlistedClasses();
                info.excludeUnlistedClasses = excludeUnlistedClasses != null && excludeUnlistedClasses;

                info.jarFiles.addAll(persistenceUnit.getJarFile());
                info.classes.addAll(persistenceUnit.getClazz());
                info.mappingFiles.addAll(persistenceUnit.getMappingFile());

                // Handle Properties
                // todo Do we really want the properties?  This could cause the engine to do bad things
                org.apache.openejb.jee.jpa.unit.Properties puiProperties = persistenceUnit.getProperties();
                if (puiProperties != null) {
                    for (Property property : puiProperties.getProperty()) {
                        info.properties.put(property.getName(), property.getValue());
                    }
                }

                // Persistence Unit Root Url
                persistenceUnits.add(info);
            }
        }
        return persistenceUnits;
    }

    private static class PersistenceClassLoaderHandlerImpl implements PersistenceClassLoaderHandler {
        public void addTransformer(ClassLoader classLoader, ClassFileTransformer classFileTransformer) {
            Instrumentation instrumentation = Agent.getInstrumentation();
            if (instrumentation != null) {
                instrumentation.addTransformer(classFileTransformer);
            }
        }

        public ClassLoader getNewTempClassLoader(ClassLoader classLoader) {
            return new TemporaryClassLoader(classLoader);
        }
    }
}
