/*
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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.api.configuration.PersistenceUnitDefinitions;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.xbean.finder.IAnnotationFinder;

import java.util.ArrayList;
import jakarta.persistence.Entity;

public class ConfigurationDeployer implements DynamicDeployer {
    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        for (final EjbModule module : new ArrayList<>(appModule.getEjbModules())) {
            if (module.getFinder() == null) {
                continue;
            }

            EjbModule m = null;
            for (final Class<?> configClass : module.getFinder().findAnnotatedClasses(PersistenceUnitDefinition.class)) {
                m = m == null ? findModule(appModule, module) : m;
                configureJpa(appModule, configClass.getAnnotation(PersistenceUnitDefinition.class), m.getFinder());
            }
            for (final Class<?> configClass : module.getFinder().findAnnotatedClasses(PersistenceUnitDefinitions.class)) {
                for (final PersistenceUnitDefinition persistenceUnitDefinition : configClass.getAnnotation(PersistenceUnitDefinitions.class).value()) {
                    m = m == null ? findModule(appModule, module) : m;
                    configureJpa(appModule, persistenceUnitDefinition, m.getFinder());
                }
            }
        }
        return appModule;
    }

    private EjbModule findModule(final AppModule appModule, final EjbModule module) {
        EjbModule m = module;
        if (m.getFinder().findAnnotatedClasses(Entity.class).isEmpty()) {
            // switch to another module
            for (final EjbModule other : appModule.getEjbModules()) {
                if (other == module || other.getFinder() == null) {
                    continue;
                }
                m = other;
                boolean done = false;
                for (final WebModule web : appModule.getWebModules()) {
                    if (web.getModuleId().equals(other.getModuleId())) { // the biggest module is found, use it
                        done = true;
                        break;
                    }
                }
                if (done) {
                    break;
                }
            }
        }
        return m;
    }

    private void configureJpa(final AppModule appModule, final PersistenceUnitDefinition annotation, final IAnnotationFinder finder) {
        if (annotation == null) {
            return;
        }

        final String unitName = PropertyPlaceHolderHelper.simpleValue(annotation.unitName());
        for (final PersistenceModule module : appModule.getPersistenceModules()) {
            for (final PersistenceUnit unit : module.getPersistence().getPersistenceUnit()) {
                if (unitName.equals(unit.getName())) {
                    Logger.getInstance(LogCategory.OPENEJB_STARTUP, ConfigurationDeployer.class).info("Unit[" + unitName + "] overriden by a persistence.xml with root url: " + module.getRootUrl());
                    return;
                }
            }
        }

        final PersistenceUnit unit = new PersistenceUnit();
        unit.setName(unitName);
        if (!"auto".equals(annotation.jtaDataSource())) {
            unit.setJtaDataSource(PropertyPlaceHolderHelper.simpleValue(annotation.jtaDataSource()));
        }
        if (!"auto".equals(annotation.nonJtaDataSource())) {
            unit.setNonJtaDataSource(PropertyPlaceHolderHelper.simpleValue(annotation.nonJtaDataSource()));
        }
        for (final String prop : annotation.properties()) {
            final int equalIndex = prop.indexOf('=');
            unit.setProperty(PropertyPlaceHolderHelper.simpleValue(prop.substring(0, equalIndex)), PropertyPlaceHolderHelper.simpleValue(prop.substring(equalIndex + 1, prop.length())));
        }
        unit.setProperty("openejb.jpa.auto-scan", "true");
        if (!"auto".equals(annotation.entitiesPackage())) {
            unit.setProperty("openejb.jpa.auto-scan.package", PropertyPlaceHolderHelper.simpleValue(annotation.entitiesPackage()));
        }
        if (annotation.ddlAuto()) {
            unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            unit.setProperty("jakarta.persistence.schema-generation.database.action", "create");
        }
        if (annotation.jta()) {
            unit.setTransactionType(TransactionType.JTA);
        } else {
            unit.setTransactionType(TransactionType.RESOURCE_LOCAL);
            unit.setNonJtaDataSource("autoNonJtaDb"); // otherwise type is forced to JTA
        }
        if (!"auto".equals(annotation.provider())) {
            unit.setProvider(annotation.provider());
        }
        unit.setValidationMode(annotation.validationMode());
        unit.setSharedCacheMode(annotation.cacheMode());

        AnnotationDeployer.doAutoJpa(finder, unit); // we pass after annotation deployer so need to fill it ourself

        final Persistence persistence = new Persistence();
        persistence.addPersistenceUnit(unit);
        appModule.addPersistenceModule(new PersistenceModule(appModule, "@Configuration#" + unitName, persistence));
    }
}
