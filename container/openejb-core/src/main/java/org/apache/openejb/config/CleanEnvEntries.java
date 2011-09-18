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
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class CleanEnvEntries implements DynamicDeployer {

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        // Any EnvEntry types missing the <value> and having no <lookup> should be removed
        appModule = removeUnsetEnvEntries(appModule);

        // Any EnvEntry types having the <value> but missing the type should have the type implied
        // based on the injection points
        appModule = fillInMissingType(appModule);

        return appModule;
    }

    public AppModule removeUnsetEnvEntries(AppModule appModule) throws OpenEJBException {

        for (ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) continue;

            removeUnsetEnvEntries(consumer);
        }

        for (WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) continue;

            removeUnsetEnvEntries(consumer);
        }

        for (EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) continue;

            for (EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                removeUnsetEnvEntries(consumer);
            }
        }

        return appModule;
    }

    private void removeUnsetEnvEntries(JndiConsumer consumer) {
        final Iterator<EnvEntry> entries = consumer.getEnvEntry().iterator();
        while (entries.hasNext()) {
            final EnvEntry entry = entries.next();
            if (entry.getEnvEntryValue() != null) continue;
            if (entry.getLookupName() != null) continue;

            entries.remove();
        }
    }


    public AppModule fillInMissingType(AppModule appModule) throws OpenEJBException {

        for (ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) continue;

            fillInMissingType(consumer, module);
        }

        for (WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) continue;

            fillInMissingType(consumer, module);
        }

        for (EjbModule module : appModule.getEjbModules()) {
            final EjbJar ejbJar = module.getEjbJar();
            if (ejbJar == null) continue;

            for (EnterpriseBean consumer : ejbJar.getEnterpriseBeans()) {
                fillInMissingType(consumer, module);
            }
        }

        return appModule;
    }

    private void fillInMissingType(JndiConsumer consumer, DeploymentModule module) {
        final ClassLoader loader = module.getClassLoader();

        for (EnvEntry entry : consumer.getEnvEntry()) {
            fillInMissingType(loader, entry);
        }

    }

    private void fillInMissingType(ClassLoader loader, EnvEntry entry) {
        if (entry.getType() != null) return;

        // If it has the lookup supplied we don't care if there is no type
        if (entry.getLookupName() != null) return;

        // We can't imply type without at least one injection point
        if (entry.getInjectionTarget().size() == 0) return;

        final Set<Class> types = new HashSet<Class>();

        for (InjectionTarget target : entry.getInjectionTarget()) {
            if (target.getInjectionTargetClass() == null) continue;
            if (target.getInjectionTargetName() == null) continue;

            types.add(getType(loader, target));
        }

        normalize(types);

        final Class<?> type = (types.size() == 1) ? types.iterator().next() : String.class;

        entry.setType(type.getName());
    }

    private void normalize(Set<Class> types) {
        types.remove(Object.class);

        if (types.contains(int.class)) {
            types.remove(int.class);
            types.add(Integer.class);
        }

        if (types.contains(char.class)) {
            types.remove(char.class);
            types.add(Character.class);
        }

        if (types.contains(short.class)) {
            types.remove(short.class);
            types.add(Short.class);
        }

        if (types.contains(long.class)) {
            types.remove(long.class);
            types.add(Long.class);
        }

        if (types.contains(float.class)) {
            types.remove(float.class);
            types.add(Float.class);
        }

        if (types.contains(double.class)) {
            types.remove(double.class);
            types.add(Double.class);
        }

        if (types.contains(boolean.class)) {
            types.remove(boolean.class);
            types.add(Boolean.class);
        }

        if (types.contains(byte.class)) {
            types.remove(byte.class);
            types.add(Byte.class);
        }

    }

    private Class<?> getType(ClassLoader loader, InjectionTarget target) {
        try {
            final Class<?> clazz = loader.loadClass(target.getInjectionTargetClass());

            try {
                final Field field = clazz.getDeclaredField(target.getInjectionTargetName());
                return field.getType();
            } catch (NoSuchFieldException e) {
            }

            // TODO Technically we should match by case
            final String name = "set" + target.getInjectionTargetName().toLowerCase();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getParameterTypes().length == 1 && method.getName().toLowerCase().equals(name)) {
                    return method.getParameterTypes()[0];
                }
            }

        } catch (Throwable e) {
        }

        return Object.class;
    }


}
