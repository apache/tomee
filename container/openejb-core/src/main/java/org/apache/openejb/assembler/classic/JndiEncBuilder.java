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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.JndiReference;
import org.apache.openejb.core.ivm.naming.NameNode;
import org.apache.openejb.core.ivm.naming.ParsedName;
import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO: This class is essentially an over glorified sym-linker.  The names
 * we were linking to are no longer guaranteed to be what we assume them to
 * be.  We need to come up with a different internal naming structure for
 * the global JNDI and finally create the default which will be the default
 * symlinked version of all the components.
 */
public class JndiEncBuilder {

    private final ReferenceWrapper referenceWrapper;
    private final boolean beanManagedTransactions;
    private final EjbReferenceInfo[] ejbReferences;
    private final EjbLocalReferenceInfo[] ejbLocalReferences;
    private final EnvEntryInfo[] envEntries;
    private final ResourceReferenceInfo[] resourceRefs;

    public JndiEncBuilder(JndiEncInfo jndiEnc) throws OpenEJBException {
        this(jndiEnc, null, null);
    }

    public JndiEncBuilder(JndiEncInfo jndiEnc, String transactionType, BeanType ejbType) throws OpenEJBException {
        if (ejbType == null){
            referenceWrapper = new DefaultReferenceWrapper();
        } else if (ejbType.isEntity()) {
            referenceWrapper = new EntityRefereceWrapper();
        } else if (ejbType == BeanType.STATEFUL) {
            referenceWrapper = new StatefulRefereceWrapper();
        } else if (ejbType == BeanType.STATELESS) {
            referenceWrapper = new StatelessRefereceWrapper();
        } else {
            throw new org.apache.openejb.OpenEJBException("Unknown component type");
        }

        beanManagedTransactions = transactionType != null && transactionType.equalsIgnoreCase("Bean");

        ejbReferences = (jndiEnc != null && jndiEnc.ejbReferences != null) ? jndiEnc.ejbReferences : new EjbReferenceInfo[]{};
        ejbLocalReferences = (jndiEnc != null && jndiEnc.ejbLocalReferences != null) ? jndiEnc.ejbLocalReferences : new EjbLocalReferenceInfo[]{};
        envEntries = (jndiEnc != null && jndiEnc.envEntries != null) ? jndiEnc.envEntries : new EnvEntryInfo[]{};
        resourceRefs = (jndiEnc != null && jndiEnc.resourceRefs != null) ? jndiEnc.resourceRefs : new ResourceReferenceInfo[]{};
    }

    public Context build() throws OpenEJBException {
        HashMap bindings = new HashMap();

        if (beanManagedTransactions) {
            Object obj = Assembler.getContext().get(TransactionManager.class.getName());
            TransactionManager transactionManager = (TransactionManager) obj;

            Object userTransaction = referenceWrapper.wrap(new CoreUserTransaction(transactionManager));
            bindings.put("java:comp/UserTransaction", userTransaction);
        }

        for (int i = 0; i < ejbReferences.length; i++) {
            EjbReferenceInfo referenceInfo = ejbReferences[i];
            EjbReferenceLocationInfo location = referenceInfo.location;

            Reference reference = null;

            if (!location.remote) {
                // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                if (referenceInfo.homeType == null){
                    String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId + "BusinessRemote";
                    reference = new IntraVmJndiReference(jndiName);
                }else {
                    // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                    String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId;
                    reference = new IntraVmJndiReference(jndiName);
                }
            } else {
                String openEjbSubContextName = "java:openejb/remote_jndi_contexts/" + location.jndiContextId;
                reference = new JndiReference(openEjbSubContextName, location.remoteRefName);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (int i = 0; i < ejbLocalReferences.length; i++) {
            EjbLocalReferenceInfo referenceInfo = ejbLocalReferences[i];

            EjbReferenceLocationInfo location = referenceInfo.location;
            if (location != null && !location.remote) {
                // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId + "Local";
                Reference reference = new IntraVmJndiReference(jndiName);
                bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
            }
        }

        for (int i = 0; i < envEntries.length; i++) {
            EnvEntryInfo entry = envEntries[i];
            try {
                Class type = Class.forName(entry.type.trim());
                Object obj = null;
                if (type == String.class)
                    obj = new String(entry.value);
                else if (type == Double.class) {
                    obj = new Double(entry.value);
                } else if (type == Integer.class) {
                    obj = new Integer(entry.value);
                } else if (type == Long.class) {
                    obj = new Long(entry.value);
                } else if (type == Float.class) {
                    obj = new Float(entry.value);
                } else if (type == Short.class) {
                    obj = new Short(entry.value);
                } else if (type == Boolean.class) {
                    obj = new Boolean(entry.value);
                } else if (type == Byte.class) {
                    obj = new Byte(entry.value);
                } else {
                    throw new IllegalArgumentException("Invalid env-ref-type " + type);
                }

                bindings.put(normalize(entry.name), obj);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Invalid environment entry type: " + entry.type.trim() + " for entry: " + entry.name);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The env-entry-value for entry " + entry.name + " was not recognizable as type " + entry.type + ". Received Message: " + e.getLocalizedMessage());
            }
        }

        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceReferenceInfo referenceInfo = resourceRefs[i];
            Reference reference = null;

            if (referenceInfo.resourceID != null) {
                String jndiName = "java:openejb/connector/" + referenceInfo.resourceID;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String openEjbSubContextName1 = "java:openejb/remote_jndi_contexts/" + referenceInfo.location.jndiContextId;
                String jndiName2 = referenceInfo.location.remoteRefName;
                reference = new JndiReference(openEjbSubContextName1, jndiName2);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        IvmContext enc = new IvmContext(new NameNode(null, new ParsedName("comp"), null));

        for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            try {
                enc.bind(name, value);
            } catch (NamingException e) {
                throw new org.apache.openejb.SystemException("Unable to bind '" + name + "' into bean's enc.", e);
            }
        }
        return enc;
    }

    private String normalize(String name) {
        if (name.charAt(0) == '/')
            name = name.substring(1);
        if (!(name.startsWith("java:comp/env") || name.startsWith("comp/env"))) {
            if (name.startsWith("env/"))
                name = "comp/" + name;
            else
                name = "comp/env/" + name;
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

    static class EntityRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.entity.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            throw new IllegalStateException("Entity beans cannot have references to UserTransaction instance");
        }
    }

    static class StatelessRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateless.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateless.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }

    static class StatefulRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateful.EncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateful.EncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }

    private static class DefaultReferenceWrapper extends ReferenceWrapper {
        Object wrap(Reference reference) {
            return reference;
        }

        Object wrap(UserTransaction reference) {
            return reference;
        }
    }
}
