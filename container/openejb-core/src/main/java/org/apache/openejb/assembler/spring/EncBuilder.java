/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2006 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
    private byte ejbType;
    private boolean beanManagedTransaction;
    private TransactionManager transactionManager;
    private EncBuilder.ReferenceWrapper referenceWrapper;

    public EncBuilder() {
    }

    public EncBuilder(EncInfo encInfo, byte ejbType, boolean beanManagedTransaction, TransactionManager transactionManager) throws SystemException {
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

    public byte getEjbType() {
        return ejbType;
    }

    public void setEjbType(byte ejbType) throws SystemException {
        this.ejbType = ejbType;
        if (CoreDeploymentInfo.BMP_ENTITY == ejbType ||
                CoreDeploymentInfo.CMP_ENTITY == ejbType) {
            referenceWrapper = new EntityRefereceWrapper();
        } else if (CoreDeploymentInfo.STATEFUL == ejbType ) {
            referenceWrapper = new StatefulRefereceWrapper();
        } else if (CoreDeploymentInfo.STATELESS == ejbType ) {
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
            bindings.put(normalize(referenceInfo.getName()), wrapReference(reference));
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

                bindings.put(normalize(entry.getName()), obj);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid environment entry type: " + entry.getType().trim() + " for entry: " + entry.getName());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The env-entry-value for entry " + entry.getName() + " was not recognizable as type " + entry.getType() + ". Received Message: " + e.getLocalizedMessage());
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
            bindings.put(normalize(referenceInfo.getName()), wrapReference(reference));
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
