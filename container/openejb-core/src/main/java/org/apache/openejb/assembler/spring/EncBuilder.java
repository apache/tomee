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
package org.apache.openejb.assembler.spring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.openejb.SystemException;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.JndiReference;
import org.apache.openejb.core.ivm.naming.NameNode;
import org.apache.openejb.core.ivm.naming.ParsedName;
import org.apache.openejb.core.ivm.naming.Reference;

public class EncBuilder {
    private EncInfo encInfo;
    private BeanType ejbType;
    private boolean beanManagedTransaction;
    private TransactionManager transactionManager;
    private EncBuilder.ReferenceWrapper referenceWrapper;

    public EncBuilder() {
    }

    public EncBuilder(EncInfo encInfo, BeanType ejbType, boolean beanManagedTransaction, TransactionManager transactionManager) throws SystemException {
        this.encInfo = encInfo;
        setEjbType(ejbType);
        this.beanManagedTransaction = beanManagedTransaction;
        this.transactionManager = transactionManager;
    }

    public EncInfo getEncInfo() {
        return encInfo;
    }

    public void setEncInfo(EncInfo encInfo) {
        this.encInfo = encInfo;
    }

    public BeanType getEjbType() {
        return ejbType;
    }

    public void setEjbType(BeanType ejbType) throws SystemException {
        this.ejbType = ejbType;
        if (BeanType.BMP_ENTITY == ejbType || BeanType.CMP_ENTITY == ejbType) {
            referenceWrapper = new EntityRefereceWrapper();
        } else if (BeanType.STATEFUL == ejbType ) {
            referenceWrapper = new StatefulRefereceWrapper();
        } else if (BeanType.STATELESS == ejbType ) {
            referenceWrapper = new StatelessRefereceWrapper();
        } else {
            throw new SystemException("Unknown component type: " + ejbType);
        }
    }

    public boolean isBeanManagedTransaction() {
        return beanManagedTransaction;
    }

    public void setBeanManagedTransaction(boolean beanManagedTransaction) {
        this.beanManagedTransaction = beanManagedTransaction;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Context createContext() throws SystemException {
        HashMap<String, Object> bindings = new HashMap<String, Object>();
        if (beanManagedTransaction) {
            Object userTransaction = referenceWrapper.wrap(new CoreUserTransaction(transactionManager));
            bindings.put("java:comp/UserTransaction", userTransaction);
        }

        for (EjbReferenceInfo referenceInfo : encInfo.ejbRefs) {
            Reference reference;
            if (referenceInfo.isLocal()) {
                String jndiName = "java:openejb/ejb/" + referenceInfo.getEjbId() + "Local";
                reference = new IntraVmJndiReference(jndiName);
            } else if (referenceInfo.getEjbId() != null) {
                String jndiName = "java:openejb/ejb/" + referenceInfo.getEjbId();
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String openEjbSubContextName = "java:openejb/remote_jndi_contexts/" + referenceInfo.getRemoteContextId();
                reference = new JndiReference(openEjbSubContextName, referenceInfo.getRemoteName());
            }
            bindings.put(normalize(referenceInfo.getJndiName()), wrapReference(reference));
        }

        for (EnvEntryInfo entry : encInfo.envEntries) {
            try {
                Class type = Class.forName(entry.getType().trim());
                Object obj = null;
                if (type == String.class) {
                    obj = new String(entry.getValue());
                } else if (type == Double.class) {
                    obj = new Double(entry.getValue());
                } else if (type == Integer.class) {
                    obj = new Integer(entry.getValue());
                } else if (type == Long.class) {
                    obj = new Long(entry.getValue());
                } else if (type == Float.class) {
                    obj = new Float(entry.getValue());
                } else if (type == Short.class) {
                    obj = new Short(entry.getValue());
                } else if (type == Boolean.class) {
                    obj = new Boolean(entry.getValue());
                } else if (type == Byte.class) {
                    obj = new Byte(entry.getValue());
                } else {
                    throw new IllegalArgumentException("Invalid env-ref-type " + type);
                }

                bindings.put(normalize(entry.getJndiName()), obj);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid environment entry type: " + entry.getType().trim() + " for entry: " + entry.getJndiName());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The env-entry-value for entry " + entry.getJndiName() + " was not recognizable as type " + entry.getType() + ". Received Message: " + e.getLocalizedMessage());
            }
        }

        for (ResourceReferenceInfo referenceInfo : encInfo.resourceRefs) {
            Reference reference = null;
            if (referenceInfo.getResourceId() != null) {
                String jndiName = "java:openejb/connector/" + referenceInfo.getResourceId();
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String openEjbSubContextName1 = "java:openejb/remote_jndi_contexts/" + referenceInfo.getRemoteContextId();
                String jndiName2 = referenceInfo.getRemoteName();
                reference = new JndiReference(openEjbSubContextName1, jndiName2);
            }
            bindings.put(normalize(referenceInfo.getJndiName()), wrapReference(reference));
        }

        IvmContext enc = new IvmContext(new NameNode(null, new ParsedName("comp"), null));

        for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            try {
                enc.bind(name, value);
            } catch (NamingException e) {
                throw new SystemException("Unable to bind '" + name + "' into bean's enc.", e);
            }
        }
        return enc;
    }

    private String normalize(String name) {
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        if (!(name.startsWith("java:comp/env") || name.startsWith("comp/env"))) {
            if (name.startsWith("env/")) {
                name = "comp/" + name;
            } else {
                name = "comp/env/" + name;
            }
        }
        return name;
    }

    private Object wrapReference(Reference reference) {
        return referenceWrapper.wrap(reference);
    }

    static abstract class ReferenceWrapper {
        abstract Object wrap(Reference reference);

        abstract Object wrap(UserTransaction reference);
    }

    static class EntityRefereceWrapper extends EncBuilder.ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.entity.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            throw new IllegalStateException("Entity beans cannot have references to UserTransaction instance");
        }
    }

    static class StatelessRefereceWrapper extends EncBuilder.ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateless.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateless.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }

    static class StatefulRefereceWrapper extends EncBuilder.ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateful.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateful.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }
}
