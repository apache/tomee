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

package org.apache.openejb.core.cmp;

import org.apache.openejb.OpenEJBException;

import jakarta.ejb.EJBException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class AbstractKeyGenerator implements KeyGenerator {
    public static boolean isValidPkField(final Field field) {
        final int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

    public static Field getField(final Class clazz, final String fieldName) throws OpenEJBException {
        try {
            return clazz.getField(fieldName);
        } catch (final NoSuchFieldException e) {
            throw new OpenEJBException("Unable to get primary key field from entity bean class: " + clazz.getName(), e);
        }
    }

    public static Object getFieldValue(final Field field, final Object object) throws EJBException {
        if (field == null) {
            throw new NullPointerException("field is null");
        }
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        try {
            return field.get(object);
        } catch (final Exception e) {
            throw new EJBException("Could not get field value for field " + field, e);
        }
    }

    public static void setFieldValue(final Field field, final Object object, final Object value) throws EJBException {
        if (field == null) {
            throw new NullPointerException("field is null");
        }
        if (object == null) {
            throw new NullPointerException("object is null");
        }
        try {
            field.set(object, value);
        } catch (final Exception e) {
            throw new EJBException("Could not set field value for field " + field, e);
        }
    }
}
