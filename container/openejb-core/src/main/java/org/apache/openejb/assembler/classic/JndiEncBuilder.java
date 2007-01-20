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

import org.apache.openejb.BeanType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.JndiReference;
import org.apache.openejb.core.ivm.naming.PersistenceUnitReference;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.core.ivm.naming.PersistenceContextReference;
import org.apache.openejb.core.ivm.naming.JndiUrlReference;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.NameNode;
import org.apache.openejb.core.ivm.naming.ParsedName;
import org.apache.xbean.naming.context.WritableContext;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

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
    private final Map<String, EntityManagerFactory> entityManagerFactories;
    private final Map<String, Map<String, EntityManagerFactory>> allFactories;
    private final String jarPath;
    private final JndiEncInfo jndiEnc;


    public JndiEncBuilder(JndiEncInfo jndiEnc) throws OpenEJBException {
        this(jndiEnc, null, null, null,null);
    }

    public JndiEncBuilder(JndiEncInfo jndiEnc, String transactionType, BeanType ejbType, Map<String, Map<String, EntityManagerFactory>> allFactories,String path) throws OpenEJBException {
        if (ejbType == null){
            referenceWrapper = new DefaultReferenceWrapper();
        } else if (ejbType.isEntity()) {
            referenceWrapper = new EntityRefereceWrapper();
        } else if (ejbType == BeanType.STATEFUL) {
            referenceWrapper = new StatefulRefereceWrapper();
        } else if (ejbType == BeanType.STATELESS) {
            referenceWrapper = new StatelessRefereceWrapper();
        } else if (ejbType == BeanType.MESSAGE_DRIVEN) {
            referenceWrapper = new MessageDrivenRefereceWrapper();
        } else {
            throw new org.apache.openejb.OpenEJBException("Unknown component type");
        }

        beanManagedTransactions = transactionType != null && transactionType.equalsIgnoreCase("Bean");

        this.jndiEnc = jndiEnc;

        if(allFactories != null){
            this.allFactories = allFactories;
        } else {
            this.allFactories = new HashMap<String, Map<String, EntityManagerFactory>>();
        }

        this.jarPath = path;

        if(this.allFactories.get(jarPath) != null){
            entityManagerFactories = this.allFactories.get(jarPath);
        } else {
            entityManagerFactories = new HashMap<String, EntityManagerFactory>();
        }
    }

    public Context build() throws OpenEJBException {
        Map<String, Object> bindings = new HashMap<String, Object>();

        // bind TransactionManager
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        bindings.put("java:comp/TransactionManager", transactionManager);

        // bind TransactionSynchronizationRegistry
        TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
        bindings.put("java:comp/TransactionSynchronizationRegistry", synchronizationRegistry);

        // get JtaEntityManagerRegistry
        JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);

        // bind UserTransaction if bean managed transactions
        if (beanManagedTransactions) {
            Object userTransaction = referenceWrapper.wrap(new CoreUserTransaction(transactionManager));
            bindings.put("java:comp/UserTransaction", userTransaction);
        }

        for (EjbReferenceInfo referenceInfo : jndiEnc.ejbReferences) {

            Reference reference = null;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else {
                // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                if (referenceInfo.homeType == null){
                    String jndiName = "java:openejb/ejb/" + referenceInfo.ejbDeploymentId + "BusinessRemote";
                    reference = new IntraVmJndiReference(jndiName);
                } else {
                    // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                    String jndiName = "java:openejb/ejb/" + referenceInfo.ejbDeploymentId;
                    reference = new IntraVmJndiReference(jndiName);
                }
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (EjbLocalReferenceInfo referenceInfo : jndiEnc.ejbLocalReferences) {

            Reference reference = null;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else if (referenceInfo.homeType == null){
                // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                String jndiName = "java:openejb/ejb/" + referenceInfo.ejbDeploymentId + "BusinessLocal";
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String jndiName = "java:openejb/ejb/" + referenceInfo.ejbDeploymentId + "Local";
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (EnvEntryInfo entry : jndiEnc.envEntries) {

            if (entry.location != null) {
                Reference reference = buildReferenceLocation(entry.location);
                bindings.put(normalize(entry.name), reference);
                continue;
            }

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
                } else if (type == Character.class) {
                    StringBuilder sb = new StringBuilder(entry.value + " ");
                    obj = new Character(sb.charAt(0));
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

        for (ResourceReferenceInfo referenceInfo : jndiEnc.resourceRefs) {
            Reference reference = null;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else if (referenceInfo.resourceID != null) {
                String jndiName = "java:openejb/Connector/" + referenceInfo.resourceID;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String jndiName = "java:openejb/Connector/" + referenceInfo.referenceName;
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (ResourceEnvReferenceInfo referenceInfo : jndiEnc.resourceEnvRefs) {
            LinkRef linkRef = null;
            try {
                if (EJBContext.class.isAssignableFrom(Class.forName(referenceInfo.resourceEnvRefType))) {
                    String jndiName = "java:comp/EJBContext";
                    linkRef = new LinkRef(jndiName);
                    bindings.put(normalize(referenceInfo.resourceEnvRefName), linkRef);
                    continue;
                }
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException(e);
            }

            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.resourceEnvRefName), wrapReference(reference));
            }

            //TODO code for handling other resource-env-refs need to be added here.
        }

        for (PersistenceUnitInfo referenceInfo : jndiEnc.persistenceUnitRefs) {
            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
                continue;
            }

            EntityManagerFactory factory = findEntityManagerFactory(referenceInfo.persistenceUnitName);

            Reference reference = new PersistenceUnitReference(factory);
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }

        for (PersistenceContextInfo contextInfo : jndiEnc.persistenceContextRefs) {
            if (contextInfo.location != null){
                Reference reference = buildReferenceLocation(contextInfo.location);
                bindings.put(normalize(contextInfo.referenceName), wrapReference(reference));
                continue;
            }

            EntityManagerFactory factory = findEntityManagerFactory(contextInfo.persistenceUnitName);

            JtaEntityManager jtaEntityManager = new JtaEntityManager(jtaEntityManagerRegistry, factory, contextInfo.properties, contextInfo.extended);
            Reference reference = new PersistenceContextReference(jtaEntityManager);
            bindings.put(normalize(contextInfo.referenceName), wrapReference(reference));
        }

        for (MessageDestinationReferenceInfo referenceInfo : jndiEnc.messageDestinationRefs) {
            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
            }
        }

        for (ServiceReferenceInfo referenceInfo : jndiEnc.serviceRefs) {
            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
            }
        }

        Context context;
        if (System.getProperty("openejb.naming","ivm").equals("xbean")) {
            context = createXBeanWritableContext(bindings);
        } else {
            context = createIvmContext();

            // bind the bindings
            for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                if (value == null) continue;
                try {
                    Name parsedName = context.getNameParser("").parse(name);
                    for (int i = 1; i < parsedName.size(); i++) {
                        Name contextName = parsedName.getPrefix(i);
                        if (!bindingExists(context, contextName)) {
                            context.createSubcontext(contextName);
                        }
                    }
                    context.bind(name, value);
                } catch (NamingException e) {
                    throw new org.apache.openejb.SystemException("Unable to bind '" + name + "' into bean's enc.", e);
                }
            }
        }
        return context;
    }

    private WritableContext createXBeanWritableContext(Map bindings) {
        WritableContext context = null;
        try {
            context = new WritableContext("", bindings);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
        return context;
    }

    private IvmContext createIvmContext() {
        IvmContext context = new IvmContext(new NameNode(null, new ParsedName("comp"), null));
        try {
            context.createSubcontext("comp").createSubcontext("env");
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to create subcontext 'java:comp/env'.  Exception:"+e.getMessage(),e);
        }
        return context;
    }

    public static boolean bindingExists(Context context, Name contextName) {
        try {
            return context.lookup(contextName) != null;
        } catch (NamingException e) {
        }
        return false;
    }

    private Reference buildReferenceLocation(ReferenceLocationInfo location) {
        if (location.jndiProviderId != null){
            String subContextName = "java:openejb/remote_jndi_contexts/" + location.jndiProviderId;
            return new JndiReference(subContextName, location.jndiName);
        } else {
            return new JndiUrlReference(location.jndiName);
        }
    }

    private String normalize(String name) {
        if (name.charAt(0) == '/')
            name = name.substring(1);
        if (!(name.startsWith("java:comp/env") || name.startsWith("comp/env"))) {
            if (name.startsWith("env/"))
                name = "java:comp/" + name;
            else
                name = "java:comp/env/" + name;
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
            return new org.apache.openejb.core.entity.EntityEncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            throw new IllegalStateException("Entity beans cannot have references to UserTransaction instance");
        }
    }

    static class StatelessRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateless.StatelessEncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateless.StatelessEncUserTransaction((CoreUserTransaction) userTransaction);
        }
    }

    static class StatefulRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.stateful.StatefulEncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.stateful.StatefulEncUserTransaction(userTransaction);
        }
    }

    static class MessageDrivenRefereceWrapper extends ReferenceWrapper {
        public Object wrap(Reference reference) {
            return new org.apache.openejb.core.mdb.MdbEncReference(reference);
        }

        public Object wrap(UserTransaction userTransaction) {
            return new org.apache.openejb.core.mdb.MdbEncUserTransaction((CoreUserTransaction) userTransaction);
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

    public EntityManagerFactory findEntityManagerFactory(String persistenceName) throws OpenEJBException {
        EntityManagerFactory factory;
        if (persistenceName != null && !"".equals(persistenceName)) {
            if (persistenceName.indexOf("#") == -1 ) {
                factory = entityManagerFactories.get(persistenceName);
            } else {
                factory = findEntityManagerFactory(allFactories, jarPath, persistenceName);
            }
        } else if (entityManagerFactories.size() == 1) {
            factory = entityManagerFactories.values().toArray(new EntityManagerFactory[1])[0];
        } else {
            throw new OpenEJBException("Deployment failed as the Persistence Unit could not be located. Try adding the 'persistence-unit-name' tag in ejb-jar.xml ");
        }
        return factory;
    }

    /**
     * This method will currently support paths like ../../xyz/ejbmodule.jar#PuName, ././xyz/ejbmodule.jar#PuName
     * and ejbmodule.jar#PuName. For all other types of path it will throw an exception stating an invalid
     * path.The paths are calculated relative to the referencing component jar. See 16.10.2 in ejb core spec.
     */
    private EntityManagerFactory findEntityManagerFactory(Map<String, Map<String, EntityManagerFactory>> allFactories, String path, String puName) throws OpenEJBException{
        int index = puName.indexOf("#");
        String relativePath = puName.substring(0,index);
        String unitName = puName.substring(index+1,puName.length());
        if(new File(path).isFile()){
            path=path.substring(0,path.lastIndexOf(File.separator));
        }
        while(relativePath.startsWith("../")){
            relativePath = relativePath.substring(3,relativePath.length());
            path = path.substring(0,path.lastIndexOf(File.separator));
        }

        while(relativePath.startsWith("./")){
            relativePath = relativePath.substring(2,relativePath.length());
        }
        path = path + File.separator + relativePath;
        path = new File(path).getPath();
        Map factories = allFactories.get(path);
        if (factories != null){
            EntityManagerFactory factory = (EntityManagerFactory)factories.get(unitName);
            if(factory != null){
                return factory;
            }
        }
        throw new OpenEJBException("The persistence unit referred by the persistence-unit-name tag "+puName+" could not be found");
    }
}
