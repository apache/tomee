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
package org.apache.openejb.config.scanning;

import org.apache.openejb.config.ConfigurableClasspathArchive;
import org.apache.openejb.config.ScanConstants;
import org.apache.openejb.config.scanning.bean.MyAnnotation;
import org.apache.openejb.config.scanning.bean.MyBean1;
import org.apache.openejb.config.scanning.bean.MyBean2;
import org.apache.openejb.config.scanning.bean.MyBean3;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class AbstractXmlAnnotationFinderTest implements ScanConstants {
    protected IAnnotationFinder finder;

    @Before
    public void initFinder() throws Exception {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        System.setProperty(SCAN_XML_PROPERTY, scanXml());
        finder = new AnnotationFinder(new ConfigurableClasspathArchive(loader,
                Arrays.asList(
                        new URL(loader.getResource(scanXml()).toExternalForm().replace(scanXml(), ""))
                )
        ));
        System.clearProperty("openejb.scan.xml.name");
    }

    protected abstract String scanXml();

    @Test
    public void findClass() {
        final List<Class<?>> myClassAnnotated = finder.findAnnotatedClasses(MyAnnotation.class);
        assertEquals(1, myClassAnnotated.size());
        assertEquals(MyBean1.class, myClassAnnotated.iterator().next());
    }

    @Test
    public void findMethod() {
        final List<Method> myMethodAnnotated = finder.findAnnotatedMethods(MyAnnotation.class);
        assertEquals(1, myMethodAnnotated.size());
        final Method method = myMethodAnnotated.iterator().next();
        assertEquals(MyBean2.class, method.getDeclaringClass());
        assertEquals("aMethod", method.getName());
    }

    @Test
    public void findField() {
        final List<Field> myFieldAnnotated = finder.findAnnotatedFields(MyAnnotation.class);
        assertEquals(1, myFieldAnnotated.size());
        final Field field = myFieldAnnotated.iterator().next();
        assertEquals(MyBean3.class, field.getDeclaringClass());
        assertEquals("aField", field.getName());
    }
}
