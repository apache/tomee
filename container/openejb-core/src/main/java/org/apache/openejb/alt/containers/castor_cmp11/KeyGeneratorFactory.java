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
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.exolab.castor.persist.spi.Complex;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public abstract class KeyGeneratorFactory {
    public static KeyGenerator createKeyGenerator(CoreDeploymentInfo deploymentInfo) throws OpenEJBException {
        Field primaryKeyField = deploymentInfo.getPrimaryKeyField();
        if (primaryKeyField != null) {
            return new SimpleKeyGenerator(primaryKeyField);
        } else {
            return new ComplexKeyGenerator(deploymentInfo.getBeanClass(), deploymentInfo.getPrimaryKeyClass());
        }
    }

    public static boolean isValidPkField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
    }

    public static Field getField(Class clazz, String fieldName) throws OpenEJBException {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new OpenEJBException("Unable to get primary key field from entity bean class: " + clazz.getName(), e);
        }
    }

    public static Object getFieldValue(Field field, Object object) throws EJBException {
        if (field == null)  throw new NullPointerException("field is null");
        if (object == null)  throw new NullPointerException("object is null");
        try {
            return field.get(object);
        } catch (Exception e) {
            throw new EJBException("Could not get field value for field " + field, e);
        }
    }

    public static void setFieldValue(Field field, Object object, Object value) throws EJBException {
        if (field == null)  throw new NullPointerException("field is null");
        if (object == null)  throw new NullPointerException("object is null");
        try {
            field.set(object, value);
        } catch (Exception e) {
            throw new EJBException("Could not set field value for field " + field, e);
        }
    }

    public static class SimpleKeyGenerator implements KeyGenerator {
        private final Field pkField;

        public SimpleKeyGenerator(Field pkField) throws OpenEJBException {
            this.pkField = pkField;
            if (!KeyGeneratorFactory.isValidPkField(pkField)) {
                throw new OpenEJBException("Invalid primray key field: " + pkField);
            }
        }

        public Object getPrimaryKey(EntityBean bean) {
            Object value = KeyGeneratorFactory.getFieldValue(pkField, bean);
            return value;
        }

        public Complex getJdoComplex(Object primaryKey) {
            Complex complex = new Complex(primaryKey);
            return complex;
        }

        public boolean isKeyComplex() {
            return false;
        }
    }

    public static class ComplexKeyGenerator implements KeyGenerator {
        private final List<PkField> fields;
        private final Class pkClass;

        public ComplexKeyGenerator(Class entityBeanClass, Class pkClass) throws OpenEJBException {
            this.pkClass = pkClass;
            List<PkField> fields = new ArrayList<PkField>();
            for (Field pkObjectField : pkClass.getFields()) {
                if (!KeyGeneratorFactory.isValidPkField(pkObjectField)) {
                    Field entityBeanField = KeyGeneratorFactory.getField(entityBeanClass, pkObjectField.getName());
                    if (!KeyGeneratorFactory.isValidPkField(entityBeanField)) {
                        throw new OpenEJBException("Invalid primray key field on entity bean class: " + entityBeanClass.getName());
                    }
                    PkField pkField = new PkField(entityBeanField, pkObjectField);
                    fields.add(pkField);
                }
            }
            this.fields = Collections.unmodifiableList(fields);
        }

        public Object getPrimaryKey(EntityBean bean) {
            Object pkObject = null;
            try {
                pkObject = pkClass.newInstance();
            } catch (Exception e) {
                throw new EJBException("Unable to create complex primary key instance: " + pkClass.getName(), e);
            }
            for (PkField pkField : fields) {
                pkField.copyToPkObject(bean, pkObject);
            }
            return pkObject;
        }

        public Complex getJdoComplex(Object primaryKey) {
            Object[] pkValues = new Object[fields.size()];
            for (int i = 0; i < fields.size(); i++) {
                PkField pkField = fields.get(i);
                Object pkValue = pkField.getPkFieldValue(primaryKey);
                pkValues[i] = pkValue;
            }
            Complex complex = new Complex(pkValues);
            return complex;
        }

        public boolean isKeyComplex() {
            return true;
        }
    }

    private static class PkField {
        private final Field entityBeanField;
        private final Field pkObjectField;

        public PkField(Field entityBeanField, Field pkObjectField) {
            this.entityBeanField = entityBeanField;
            this.pkObjectField = pkObjectField;
        }

        public void copyToPkObject(EntityBean bean, Object pkObject) {
            Object value = KeyGeneratorFactory.getFieldValue(entityBeanField, bean);
            KeyGeneratorFactory.setFieldValue(pkObjectField, bean, value);
        }

        public Object getPkFieldValue(Object pkObject) {
            Object value = KeyGeneratorFactory.getFieldValue(pkObjectField, pkObject);
            return value;
        }
    }
}

