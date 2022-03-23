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
package org.apache.openejb.test;

import org.apache.xbean.finder.ClassFinder;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;
import javax.naming.InitialContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class TestClient extends NamedTestCase {

    protected InitialContext initialContext;
    protected EJBMetaData ejbMetaData;
    protected HomeHandle ejbHomeHandle;
    protected Handle ejbHandle;
    protected Integer ejbPrimaryKey;

    public TestClient(final String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected abstract void setUp() throws Exception;

    protected Object cast(final Object object, final Class type) {
        return object;
    }

    protected final void processFieldInjections() {
        Object home = null;
        ClassFinder finder = null;
        List<Field> fieldList = null;

        finder = new ClassFinder(getClassPath());
        fieldList = finder.findAnnotatedFields(EJB.class);
        for (final Iterator fields = fieldList.iterator(); fields.hasNext(); ) {
            final Field field = (Field) fields.next();
            final EJB ejbAnnotation = field.getAnnotation(EJB.class);
            if ((ejbAnnotation.name() != null) && (ejbAnnotation.name() != "") && (ejbAnnotation.beanInterface() != null)) {
                try {
                    home = initialContext.lookup(ejbAnnotation.name());
                    // home = ejbAnnotation.beanInterface().cast(home;
                    home = cast(home, ejbAnnotation.beanInterface());
                    field.setAccessible(true);
                    field.set(this, home);
                } catch (final Exception ex) {
                    // TODO - MNour : Needs better exception handling
                    ex.printStackTrace();
                }
            }
        }
    }

    protected final void processSetterInjections() {
        Object home = null;
        ClassFinder finder = null;
        List<Method> methodList = null;

        finder = new ClassFinder(getClassPath());
        methodList = finder.findAnnotatedMethods(EJB.class);
        for (final Iterator methods = methodList.iterator(); methods.hasNext(); ) {
            final Method method = (Method) methods.next();
            final EJB ejbAnnotation = method.getAnnotation(EJB.class);
            if ((ejbAnnotation.name() != null) && (ejbAnnotation.name() != "") && (ejbAnnotation.beanInterface() != null)) {
                try {
                    home = initialContext.lookup(ejbAnnotation.name());
                    // home = ejbAnnotation.beanInterface().cast(home;
                    home = cast(home, ejbAnnotation.beanInterface());
                    method.setAccessible(true);
                    method.invoke(this, new Object[]{home});
                } catch (final Exception ex) {
                    // TODO - MNour : Needs better exception handling
                    ex.printStackTrace();
                }
            }
        }
    }

    private List<Class<?>> getClassPath() {
        Class<?> superClass = null;
        final List<Class<?>> classPath = new ArrayList<Class<?>>();

        classPath.add(getClass());
        superClass = getClass().getSuperclass();
        while (!superClass.equals(Object.class)) {
            classPath.add(superClass);
            superClass = superClass.getSuperclass();
        }
        return classPath;
    }

}
