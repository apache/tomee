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

import org.apache.openejb.config.EjbModule;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServlet;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @version $Rev$ $Date$
 */
public class CheckInjectionPointUsage extends ValidationBase {

    @Override
    public void validate(final EjbModule ejbModule) {
        if (ejbModule.getBeans() == null) {
            return;
        }

        try {
            for (final Field field : ejbModule.getFinder().findAnnotatedFields(Inject.class)) {
                if (!field.getType().equals(InjectionPoint.class) || !HttpServlet.class.isAssignableFrom(field.getDeclaringClass())) {
                    continue;
                }

                final Annotation[] annotations = field.getAnnotations();
                if (annotations.length == 1 || (annotations.length == 2 && field.getAnnotation(Default.class) != null)) {
                    throw new DefinitionException("Can't inject InjectionPoint in " + field.getDeclaringClass());
                } // else we should check is there is no other qualifier than @Default but too early
            }
        } catch (final NoClassDefFoundError noClassDefFoundError) {
            // ignored: can't check but maybe it is because of an optional dep so ignore it
            // not important to skip it since the failure will be reported elsewhere
            // this validator doesn't check it
        }
    }
}
