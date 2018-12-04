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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;

import java.util.ArrayList;
import java.util.List;

public class InjectionBuilder {
    private final ClassLoader classLoader;

    public InjectionBuilder(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    // TODO: check we can really skip the loadClass exception (TCKs)
    public List<Injection> buildInjections(final JndiEncInfo jndiEnc) throws OpenEJBException {
        final List<Injection> injections = new ArrayList<>();
        for (final EnvEntryInfo info : jndiEnc.envEntries) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final EjbReferenceInfo info : jndiEnc.ejbReferences) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final EjbReferenceInfo info : jndiEnc.ejbLocalReferences) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final PersistenceUnitReferenceInfo info : jndiEnc.persistenceUnitRefs) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final PersistenceContextReferenceInfo info : jndiEnc.persistenceContextRefs) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final ResourceReferenceInfo info : jndiEnc.resourceRefs) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final ResourceEnvReferenceInfo info : jndiEnc.resourceEnvRefs) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        for (final ServiceReferenceInfo info : jndiEnc.serviceRefs) {
            for (final InjectionInfo target : info.targets) {
                final Injection injection = injection(info.referenceName, target.propertyName, target.className);
                injections.add(injection);
            }
        }

        return injections;
    }

    private Injection injection(final String referenceName, final String propertyName, final String className) {
        Class<?> targetClass;
        try {
            targetClass = loadClass(className);
        } catch (final OpenEJBException ex) {
            targetClass = null;
        }

        if (targetClass == null) {
            return new Injection(referenceName, propertyName, className);
        }
        return new Injection(referenceName, propertyName, targetClass);
    }

    private Class loadClass(final String className) throws OpenEJBException {
        try {
            final Class clazz = Class.forName(className, true, classLoader);
//            clazz.getDeclaredMethods();
//            clazz.getDeclaredFields();
//            clazz.getDeclaredConstructors();
//            clazz.getInterfaces();
            return clazz;
        } catch (final Throwable e) {
            throw new OpenEJBException("Unable to load class " + className);
        }
    }
}
