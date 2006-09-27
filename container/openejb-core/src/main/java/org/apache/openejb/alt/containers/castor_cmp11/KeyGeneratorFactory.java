package org.apache.openejb.alt.containers.castor_cmp11;

import java.util.HashMap;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.util.Logger;

public abstract class KeyGeneratorFactory {
    protected final static Logger logger = Logger.getInstance("CastorCMP", "org.apache.openejb.alt.util.resources");

    private static class PrimitiveKey implements org.apache.openejb.alt.containers.castor_cmp11.KeyGenerator {
        private final java.lang.reflect.Field field;

        PrimitiveKey(CoreDeploymentInfo di) throws org.apache.openejb.OpenEJBException {
            field = di.getPrimaryKeyField();
        }

        public java.lang.Object getPrimaryKey(javax.ejb.EntityBean bean) {
            try {
                return field.get(bean);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Don't have access to field " + field + ": received " + e);
            }
        }

        public org.exolab.castor.persist.spi.Complex getJdoComplex(java.lang.Object primaryKey) {
            throw new IllegalStateException("This method must not be called on primitive primary keys");
        }

        public boolean isKeyComplex() {
            return false;
        }
    }

    private static class ComplexKey implements org.apache.openejb.alt.containers.castor_cmp11.KeyGenerator {
        private final java.lang.Class pkClass;
        private final Object[] cmFields;
        private final HashMap pkFieldMap;
        private final HashMap beanFieldMap;

        ComplexKey(CoreDeploymentInfo di) throws org.apache.openejb.OpenEJBException {
            pkClass = di.getPrimaryKeyClass();
            Class beanClass = di.getBeanClass();
            java.util.List v = new java.util.ArrayList();
            java.lang.reflect.Field[] fields = pkClass.getFields();
            pkFieldMap = new HashMap();
            beanFieldMap = new HashMap();
            for (int i = 0; i < fields.length; ++i) {
                String fieldName = fields[i].getName();
                try {
                    beanFieldMap.put(fieldName, beanClass.getField(fieldName));
                    pkFieldMap.put(fieldName, fields[i]);
                    v.add(fieldName);
                } catch (java.lang.NoSuchFieldException e) {

                }
            }
            cmFields = new Object[v.size()];
            v.toArray(cmFields);
        }

        public java.lang.Object getPrimaryKey(javax.ejb.EntityBean bean) {
            try {
                Object pk = pkClass.newInstance();
                for (int i = 0; i < cmFields.length; ++i) {
                    Object fieldName = cmFields[i];
                    Object value = ((java.lang.reflect.Field) beanFieldMap.get(fieldName)).get(bean);
                    ((java.lang.reflect.Field) pkFieldMap.get(fieldName)).set(pk, value);
                }
                return pk;
            } catch (Exception e) {
                logger.error("getPrimaryKey() received exception " + e, e);
                throw new IllegalStateException();
            }
        }

        public org.exolab.castor.persist.spi.Complex getJdoComplex(java.lang.Object primaryKey) {
            int len = cmFields.length;
            java.lang.Object [] args = new java.lang.Object[len];
            try {
                for (int i = 0; i < len; ++i) {
                    args[i] = ((java.lang.reflect.Field) pkFieldMap.get(cmFields[i])).get(primaryKey);
                }
                return new org.exolab.castor.persist.spi.Complex(len, args);
            } catch (Exception e) {
                logger.error("getJdoComplex() received exception " + e, e);
                throw new IllegalStateException();
            }
        }

        public boolean isKeyComplex() {
            return true;
        }
    }

    public static KeyGenerator createKeyGenerator(CoreDeploymentInfo di)
            throws org.apache.openejb.OpenEJBException {

        if (di.getPrimaryKeyField() != null) {
            return new PrimitiveKey(di);
        } else {
            return new ComplexKey(di);
        }
    }
}

