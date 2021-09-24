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
package org.apache.openejb.junit5.jee;

import org.apache.openejb.Injector;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.injection.FallbackPropertyInjector;
import org.apache.openejb.junit.jee.config.Properties;
import org.apache.openejb.junit.jee.config.Property;
import org.apache.openejb.junit.jee.config.PropertyFile;
import org.apache.openejb.junit.jee.resources.TestResource;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.osgi.client.LocalInitialContextFactory;
import org.apache.openejb.testing.TestInstance;
import org.apache.openejb.util.Classes;
import org.junit.jupiter.api.extension.*;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Optional;

public class EjbContainerExtension implements AfterAllCallback, BeforeAllCallback, BeforeEachCallback {

    private java.util.Properties properties;
    private EJBContainer container;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        Class<?> clazz = extensionContext.getTestClass()
                .orElseThrow(() -> new OpenEJBRuntimeException("Could not get class from extension context"));

        properties = new java.util.Properties();

        { // set caller first to let it be overridable by @Property
            final StringBuilder b = new StringBuilder();
            for (final Class<?> c : Classes.ancestors(clazz)) {
                if (c != Object.class) {
                    b.append(c.getName()).append(",");
                }
            }
            b.setLength(b.length() - 1);
            properties.put(OpenEjbContainer.Provider.OPENEJB_ADDITIONNAL_CALLERS_KEY, b.toString());
        }

        // default implicit config
        {
            try (final InputStream is = clazz.getClassLoader().getResourceAsStream("openejb-junit.properties")) {
                if (is != null) {
                    properties.load(is);
                }
            }
        }

        final PropertyFile propertyFile = clazz.getAnnotation(PropertyFile.class);
        if (propertyFile != null) {
            final String path = propertyFile.value();
            if (!path.isEmpty()) {
                InputStream is = null;
                try {
                    is = clazz.getClassLoader().getResourceAsStream(path);
                    if (is == null) {
                        final File file = new File(path);
                        if (file.exists()) {
                            is = new FileInputStream(file);
                        } else {
                            throw new OpenEJBException("properties resource '" + path + "' not found");
                        }
                    }

                    properties.load(is);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }

            }
        }

        final Properties annotationConfig = clazz.getAnnotation(Properties.class);
        if (annotationConfig != null) {
            for (final Property property : annotationConfig.value()) {
                properties.put(property.key(), property.value());
            }
        }

        if (!properties.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
            properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        }

        container = EJBContainer.createEJBContainer(properties);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Class<?> clazz = extensionContext.getTestClass().orElse(null);
        Object test =  extensionContext.getTestInstance().orElse(null);

        if (clazz != null){

            while (!Object.class.equals(clazz)) {
                for (final Field field : clazz.getDeclaredFields()) {
                    final TestResource resource = field.getAnnotation(TestResource.class);
                    if (resource != null) {
                        if (Context.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, getContainer().getContext());
                        } else if (Hashtable.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, getProperties());
                        } else if (EJBContainer.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            field.set(Modifier.isStatic(field.getModifiers()) ? null : test, getContainer());
                        } else {
                            throw new OpenEJBException("can't inject field '" + field.getName() + "'");
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }

        if (test != null) {
            SystemInstance.get().setComponent(TestInstance.class, new TestInstance(test.getClass(), test));
            SystemInstance.get().getComponent(FallbackPropertyInjector.class); // force eager init (MockitoInjector initialize everything in its constructor)
            Injector.inject(test);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (container != null) {
            container.close();
        }
    }

    public java.util.Properties getProperties() {
        return properties;
    }

    public EJBContainer getContainer() {
        return container;
    }
}
