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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class OpenEjbConfigurationValidationTest extends TestCase {

    private List<Class> seen = new ArrayList();

    public void testValidate() throws Exception {
        seen.add(Object.class);
        validate(OpenEjbConfiguration.class);
    }

    private void validate(Class clazz) throws Exception {
        if (clazz == null) return;
        if (seen.contains(clazz)) return;

        validate(clazz.getSuperclass());

        seen.add(clazz);

        String simpleName = clazz.getSimpleName();

        Constructor[] constructors = clazz.getDeclaredConstructors();
        assertEquals("constructors are not allowed: " + simpleName, 1, constructors.length);
        assertEquals("constructors are not allowed: " + simpleName, 0, constructors[0].getParameterTypes().length);

        Method[] methods = clazz.getDeclaredMethods();
        assertEquals("methods are not allowed: " + simpleName, 0, methods.length);

        Annotation[] annotations = clazz.getDeclaredAnnotations();
        assertEquals("annotations are not allowed: " + simpleName, 0, annotations.length);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            assertTrue("Non-public fields are not allowed: " + simpleName + "." + field.getName(), Modifier.isPublic(field.getModifiers()));

            annotations = clazz.getDeclaredAnnotations();
            assertEquals("annotations are not allowed: " + simpleName + "." + field.getName(), 0, annotations.length);

            Class type = field.getType();
            if (type.isArray()) {
                type = type.getComponentType();
            }

            if (List.class.isAssignableFrom(type)) {
                type = getGenericType(field);
                assertNotNull("Lists must have a generic type: " + simpleName + "." + field.getName(), type);
            }

            if (type.isPrimitive()) {
                continue;
            }

            if (String.class.isAssignableFrom(type)) {
                continue;
            }

            if (Properties.class.isAssignableFrom(type)) {
                continue;
            }

            if (InfoObject.class.isAssignableFrom(type)) {
                validate(type);
                continue;
            }

            fail("Field is not of an allowed type: " + simpleName + "." + field.getName());
        }
    }

    private Class getGenericType(Field field) throws Exception {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type firstParamType = parameterizedType.getActualTypeArguments()[0];
            return (Class) firstParamType;
        } else if (genericType instanceof Class) {
            return (Class) genericType;
        } else {
            return null;
        }
    }
}
