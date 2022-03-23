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

package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ValidationContext;
import org.apache.openejb.config.ValidationRule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class CheckRestMethodArePublic implements ValidationRule {
    @Override
    public void validate(final AppModule appModule) {
        // valid standalone classes
        final Collection<String> standAloneClasses = new ArrayList<>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final EjbModule ejb : appModule.getEjbModules()) {
                Thread.currentThread().setContextClassLoader(ejb.getClassLoader());

                for (final EnterpriseBean bean : ejb.getEjbJar().getEnterpriseBeans()) {
                    if (bean instanceof SessionBean && ((SessionBean) bean).isRestService()) {
                        standAloneClasses.add(bean.getEjbClass());
                        valid(ejb.getValidation(), ejb.getClassLoader(), bean.getEjbClass());
                    }
                }
            }

            for (final WebModule web : appModule.getWebModules()) {
                Thread.currentThread().setContextClassLoader(web.getClassLoader());

                // build the list of classes to validate
                final Collection<String> classes = new ArrayList<>();
                classes.addAll(web.getRestClasses());
                classes.addAll(web.getEjbRestServices());

                for (final String app : web.getRestApplications()) {
                    final Class<?> clazz;
                    try {
                        clazz = web.getClassLoader().loadClass(app);
                    } catch (final ClassNotFoundException e) {
                        continue; // managed elsewhere, here we just check methods
                    }

                    final Application appInstance;
                    try {
                        appInstance = (Application) clazz.newInstance();
                    } catch (final Exception e) {
                        continue; // managed elsewhere
                    }

                    try {
                        for (final Class<?> rsClass : appInstance.getClasses()) {
                            classes.add(rsClass.getName());
                        }
                        /* don't do it or ensure you have cdi activated! + CXF will catch it later
                        for (final Object rsSingleton : appInstance.getSingletons()) {
                            classes.add(rsSingleton.getClass().getName());
                        }
                        */
                    } catch (final RuntimeException npe) {
                        if (appInstance == null) {
                            throw npe;
                        }
                        // if app relies on cdi it is null here
                    }
                }

                // try to avoid to valid twice the same classes
                classes.removeIf(standAloneClasses::contains);

                // valid
                for (final String classname : classes) {
                    valid(web.getValidation(), web.getClassLoader(), classname);
                }

                classes.clear();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }

        standAloneClasses.clear();
    }

    private void valid(final ValidationContext validation, final ClassLoader classLoader, final String classname) {
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(classname);
        } catch (final ClassNotFoundException e) {
            return; // managed elsewhere
        }

        int publicMethodNumber = 0;
        int nonPublicMethods = 0;
        while (!Object.class.equals(clazz) && clazz != null) {
            for (final Method mtd : clazz.getDeclaredMethods()) {
                final boolean isPublic = Modifier.isPublic(mtd.getModifiers());
                if (mtd.getAnnotation(Path.class) != null && !isPublic) {
                    final String name = mtd.toGenericString();
                    validation.warn(name, "rest.method.visibility", name);
                }
                if (isPublic) {
                    publicMethodNumber++;
                } else {
                    nonPublicMethods++;
                }
            }
            clazz = clazz.getSuperclass();
        }

        if (publicMethodNumber == 0 && nonPublicMethods > 0) {
            validation.warn(classname, "no.method.in.rest.class", classname);
        } else if (publicMethodNumber == 0 && nonPublicMethods == 0) {
            validation.warn(classname, "no.rest.resource.method", classname);
        }
    }
}
