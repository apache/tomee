/**
 *
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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;

import java.util.List;
import java.util.ArrayList;

public class InjectionBuilder {
    private final ClassLoader classLoader;

    public InjectionBuilder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<Injection> buildInjections(JndiEncInfo jndiEnc) throws OpenEJBException {
        List<Injection> injections = new ArrayList<Injection>();
        for (EnvEntryInfo info : jndiEnc.envEntries) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.name, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (EjbReferenceInfo info : jndiEnc.ejbReferences) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (EjbReferenceInfo info : jndiEnc.ejbLocalReferences) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (PersistenceUnitReferenceInfo info : jndiEnc.persistenceUnitRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (PersistenceContextReferenceInfo info : jndiEnc.persistenceContextRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (ResourceReferenceInfo info : jndiEnc.resourceRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (ResourceEnvReferenceInfo info : jndiEnc.resourceEnvRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.resourceEnvRefName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }

        for (ServiceReferenceInfo info : jndiEnc.serviceRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className);
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                injections.add(injection);
            }
        }
        return injections;
    }

    private Class loadClass(String className) throws OpenEJBException {
        try {
            Class clazz = Class.forName(className, true, classLoader);
//            clazz.getDeclaredMethods();
//            clazz.getDeclaredFields();
//            clazz.getDeclaredConstructors();
//            clazz.getInterfaces();
            return clazz;
        } catch (Throwable e) {
            throw new OpenEJBException("Unable to load class " + className);
        }
    }
}
