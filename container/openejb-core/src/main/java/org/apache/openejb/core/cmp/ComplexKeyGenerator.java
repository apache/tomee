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
import jakarta.ejb.EntityBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplexKeyGenerator extends AbstractKeyGenerator {
    protected final List<PkField> fields;
    private final Class pkClass;

    public ComplexKeyGenerator(final Class entityBeanClass, final Class pkClass) throws OpenEJBException {
        this.pkClass = pkClass;
        final List<ComplexKeyGenerator.PkField> fields = new ArrayList<>();
        for (final Field pkObjectField : pkClass.getFields()) {
            if (isValidPkField(pkObjectField)) {
                final Field entityBeanField = getField(entityBeanClass, pkObjectField.getName());
                if (!isValidPkField(entityBeanField)) {
                    throw new OpenEJBException("Invalid primray key field: " + entityBeanField);
                }
                final PkField pkField = new PkField(entityBeanField, pkObjectField);
                fields.add(pkField);
            }
        }
        this.fields = Collections.unmodifiableList(fields);
    }

    public Object getPrimaryKey(final EntityBean bean) {
        Object pkObject = null;
        try {
            pkObject = pkClass.newInstance();
        } catch (final Exception e) {
            throw new EJBException("Unable to create complex primary key instance: " + pkClass.getName(), e);
        }

        for (final PkField pkField : fields) {
            pkField.copyToPkObject(bean, pkObject);
        }
        return pkObject;
    }

    protected static class PkField {
        private final Field entityBeanField;
        private final Field pkObjectField;

        public PkField(final Field entityBeanField, final Field pkObjectField) {
            entityBeanField.setAccessible(true);
            pkObjectField.setAccessible(true);

            this.entityBeanField = entityBeanField;
            this.pkObjectField = pkObjectField;
        }

        public void copyToPkObject(final EntityBean bean, final Object pkObject) {
            final Object value = getFieldValue(entityBeanField, bean);
            setFieldValue(pkObjectField, pkObject, value);
        }

        public Object getPkFieldValue(final Object pkObject) {
            final Object value = getFieldValue(pkObjectField, pkObject);
            return value;
        }
    }
}
