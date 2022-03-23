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
import org.apache.openejb.config.WebModule;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.IAnnotationFinder;

import jakarta.jws.WebService;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

public class CheckAnnotations extends ValidationBase {

    Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_VALIDATION, CheckAnnotations.class);

    @Override
    public void validate(final AppModule appModule) {
        try {

            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                module = ejbModule;
                findClassesAnnotatedWithWebService(ejbModule);
            }

            for (final WebModule webModule : appModule.getWebModules()) {
                module = webModule;
                findClassesAnnotatedWithWebService(webModule);
            }

        } catch (final Exception e) {
            logger.error("Error while validating @WebService annotation", e);
        }


    }

    private void findClassesAnnotatedWithWebService(final EjbModule ejbModule) {

        final IAnnotationFinder finder = ejbModule.getFinder();
        if (finder != null) {
            findIncorrectAnnotationAndWarn(finder, ejbModule.toString());
        }
    }


    private void findClassesAnnotatedWithWebService(final WebModule webModule) {
        final IAnnotationFinder finder = webModule.getFinder();
        if (finder != null) {
            findIncorrectAnnotationAndWarn(finder, webModule.toString());
        }

    }

    private void findIncorrectAnnotationAndWarn(final IAnnotationFinder finder, final String component) {
        final List<Class<?>> webserviceAnnotatedClasses = finder.findAnnotatedClasses(WebService.class);
        for (final Class clazz : webserviceAnnotatedClasses) {
            final Annotation[] annotations = clazz.getDeclaredAnnotations();

            final List<Annotation> declaredAnnotations = Arrays.asList(annotations);
            for (final Annotation declaredAnn : declaredAnnotations) {
                if (declaredAnn.annotationType().getName().equals("jakarta.ejb.Stateful")) {
                    warn(component, "annotation.invalid.stateful.webservice", clazz.getName());
                }
                if (declaredAnn.annotationType().getName().equals("jakarta.annotation.ManagedBean")) {
                    warn(component, "annotation.invalid.managedbean.webservice", clazz.getName());
                }
                if (declaredAnn.annotationType().getName().equals("jakarta.ejb.MessageDriven")) {
                    warn(component, "annotation.invalid.messagedriven.webservice", clazz.getName());
                }

            }
        }
    }


}