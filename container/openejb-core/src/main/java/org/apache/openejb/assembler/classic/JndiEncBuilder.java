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
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.core.ivm.naming.CrossClassLoaderJndiReference;
import org.apache.xbean.naming.context.WritableContext;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.omg.CORBA.ORB;

import javax.ejb.EJBContext;
import javax.ejb.spi.HandleDelegate;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * TODO: This class is essentially an over glorified sym-linker.  The names
 * we were linking to are no longer guaranteed to be what we assume them to
 * be.  We need to come up with a different internal naming structure for
 * the global JNDI and finally create the default which will be the default
 * symlinked version of all the components.
 */
public class JndiEncBuilder {

    private final boolean beanManagedTransactions;
    private final String moduleId;
    private final JndiEncInfo jndiEnc;
    private final URI moduleUri;

    // JPA factory indexes
    private final Map<String, EntityManagerFactory> localFactories;
    private final Map<String, EntityManagerFactory> absoluteFactories = new TreeMap<String,EntityManagerFactory>();
    private final Map<String, SortedSet<String>> factoryPaths = new TreeMap<String,SortedSet<String>>();

    private boolean useCrossClassLoaderRef = true;

    public JndiEncBuilder(JndiEncInfo jndiEnc, String moduleId) throws OpenEJBException {
        this(jndiEnc, null, null, null, moduleId);
    }

    public JndiEncBuilder(JndiEncInfo jndiEnc, String transactionType, BeanType ejbType, Map<String, Map<String, EntityManagerFactory>> allFactories, String moduleId) throws OpenEJBException {
        beanManagedTransactions = transactionType != null && transactionType.equalsIgnoreCase("Bean");

        this.moduleId = moduleId;
        try {
            moduleUri = new URI(moduleId);
        } catch (URISyntaxException e) {
            throw new OpenEJBException(e);
        }
        this.jndiEnc = jndiEnc;

        // build map of path#untiName --> EMF
        // and map of unitName --> TreeSet(path#untiName)
        if (allFactories == null) allFactories = new HashMap<String, Map<String, EntityManagerFactory>>();
        for (Map.Entry<String, Map<String, EntityManagerFactory>> entry : allFactories.entrySet()) {
            String path = entry.getKey();
            Map<String, EntityManagerFactory> entityManagers = entry.getValue();
            for (Map.Entry<String, EntityManagerFactory> entityManagersEntry : entityManagers.entrySet()) {
                String unitName = entityManagersEntry.getKey();
                EntityManagerFactory entityManagerFactory = entityManagersEntry.getValue();
                String absolutePath = path + "#" + unitName;
                absoluteFactories.put(absolutePath, entityManagerFactory);

                SortedSet<String> absolutePaths = factoryPaths.get(unitName);
                if (absolutePaths == null) {
                    absolutePaths = new TreeSet<String>();
                    factoryPaths.put(unitName, absolutePaths);
                }
                absolutePaths.add(absolutePath);
            }
        }

        Map<String, EntityManagerFactory> factories = allFactories.get(moduleId);
        if (factories == null) factories = new HashMap<String, EntityManagerFactory>();
        localFactories = factories;
    }

    public boolean isUseCrossClassLoaderRef() {
        return useCrossClassLoaderRef;
    }

    public void setUseCrossClassLoaderRef(boolean useCrossClassLoaderRef) {
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public Context build() throws OpenEJBException {
        Map<String, Object> bindings = buildMap();

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
                    context.bind(name, value);
                } catch (NamingException e) {
                    throw new org.apache.openejb.SystemException("Unable to bind '" + name + "' into bean's enc.", e);
                }
            }
        }
        return context;
    }

    public Map<String, Object> buildMap() throws OpenEJBException {
        Map<String, Object> bindings = new HashMap<String, Object>();

        // bind TransactionManager
        TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        bindings.put("java:comp/TransactionManager", transactionManager);

        // bind TransactionSynchronizationRegistry
        TransactionSynchronizationRegistry synchronizationRegistry = SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class);
        bindings.put("java:comp/TransactionSynchronizationRegistry", synchronizationRegistry);

        bindings.put("java:comp/ORB", new SystemComponentReference(ORB.class));
        bindings.put("java:comp/HandleDelegate", new SystemComponentReference(HandleDelegate.class));

        // get JtaEntityManagerRegistry
        JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);

        // bind UserTransaction if bean managed transactions
        if (beanManagedTransactions) {
            Object userTransaction = new CoreUserTransaction(transactionManager);
            bindings.put("java:comp/UserTransaction", userTransaction);
        }

        for (EjbReferenceInfo referenceInfo : jndiEnc.ejbReferences) {

            Reference reference = null;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else {
                String jndiName = "java:openejb/Deployment/" + referenceInfo.ejbDeploymentId + "/" + referenceInfo.remoteType;
                if (useCrossClassLoaderRef && referenceInfo.externalReference) {
                    reference = new CrossClassLoaderJndiReference(jndiName);
                } else {
                    reference = new IntraVmJndiReference(jndiName);
                }
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (EjbLocalReferenceInfo referenceInfo : jndiEnc.ejbLocalReferences) {

            Reference reference = null;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else {
                String jndiName = "java:openejb/Deployment/" + referenceInfo.ejbDeploymentId + "/" + referenceInfo.localType;
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
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
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (ResourceEnvReferenceInfo referenceInfo : jndiEnc.resourceEnvRefs) {
            LinkRef linkRef = null;
            try {
                Class<?> type = Class.forName(referenceInfo.resourceEnvRefType, true, EJBContext.class.getClassLoader());
                if (EJBContext.class.isAssignableFrom(type)) {
                    String jndiName = "java:comp/EJBContext";
                    linkRef = new LinkRef(jndiName);
                    bindings.put(normalize(referenceInfo.resourceEnvRefName), linkRef);
                    continue;
                }
            } catch (ClassNotFoundException e) {
//                throw new OpenEJBException(e);
            }

            Object reference = null;
            if (referenceInfo.location != null){
                reference = buildReferenceLocation(referenceInfo.location);
            } else {
                String destination = referenceInfo.resourceID;
                if (destination == null) destination = referenceInfo.resourceEnvRefName;

                String destinationType = referenceInfo.resourceEnvRefType;
                if (Queue.class.getName().equals(destinationType)) {
                    reference = new ActiveMQQueue(destination);
                } else if (Topic.class.getName().equals(destinationType)) {
                    reference = new ActiveMQTopic(destination);
                }
            }
            if (reference != null) {
                bindings.put(normalize(referenceInfo.resourceEnvRefName), reference);
            }

            //TODO code for handling other resource-env-refs need to be added here.
        }

        for (PersistenceUnitReferenceInfo referenceInfo : jndiEnc.persistenceUnitRefs) {
            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), reference);
                continue;
            }

            EntityManagerFactory factory = findEntityManagerFactory(referenceInfo.persistenceUnitName);
            if (factory == null) {
                throw new IllegalArgumentException("Persistence unit " + referenceInfo.persistenceUnitName + " for persistence-unit-ref " +
                        referenceInfo.referenceName + " not found");
            }

            Reference reference = new PersistenceUnitReference(factory);
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (PersistenceContextReferenceInfo contextInfo : jndiEnc.persistenceContextRefs) {
            if (contextInfo.location != null){
                Reference reference = buildReferenceLocation(contextInfo.location);
                bindings.put(normalize(contextInfo.referenceName), reference);
                continue;
            }

            EntityManagerFactory factory = findEntityManagerFactory(contextInfo.persistenceUnitName);
            if (factory == null) {
                throw new IllegalArgumentException("Persistence unit " + contextInfo.persistenceUnitName + " for persistence-context-ref " +
                        contextInfo.referenceName + " not found");
            }

            JtaEntityManager jtaEntityManager = new JtaEntityManager(jtaEntityManagerRegistry, factory, contextInfo.properties, contextInfo.extended);
            Reference reference = new PersistenceContextReference(jtaEntityManager);
            bindings.put(normalize(contextInfo.referenceName), reference);
        }

        for (MessageDestinationReferenceInfo referenceInfo : jndiEnc.messageDestinationRefs) {
            Reference reference;
            if (referenceInfo.location != null){
                reference = buildReferenceLocation(referenceInfo.location);
            } else if (referenceInfo.messageDestinationLink != null) {
                String jndiName = "java:openejb/ejb/" + referenceInfo.messageDestinationLink;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                String jndiName = "java:openejb/ejb/" + referenceInfo.referenceName;
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (ServiceReferenceInfo referenceInfo : jndiEnc.serviceRefs) {
            if (referenceInfo.location != null){
                Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), reference);
            }
        }
        return bindings;
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
            // todo remove this... IvmContext seems to drop empty nodes
            context.bind("java:comp/env/dummy", "dummy");
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

    public EntityManagerFactory findEntityManagerFactory(String persistenceName) throws OpenEJBException {
        if (persistenceName != null && !"".equals(persistenceName)) {
            if (persistenceName.indexOf("#") == -1 ) {
                EntityManagerFactory factory = localFactories.get(persistenceName);
                if (factory != null) return factory;

                // search for a unique match in allFactories;
                SortedSet<String> absolutePaths = factoryPaths.get(persistenceName);
                if (absolutePaths == null || absolutePaths.size() != 1) {
                    // todo warn with valid names
                    return null;
                }

                String absolutePath = absolutePaths.iterator().next();
                factory = absoluteFactories.get(absolutePath);
                return factory;
            } else {
                String absoluteName = moduleUri.resolve(persistenceName).toString();
                EntityManagerFactory factory = absoluteFactories.get(absoluteName);
                return factory;
            }
        } else if (localFactories.size() == 1) {
            return localFactories.values().toArray(new EntityManagerFactory[1])[0];
        } else {
            throw new OpenEJBException("Deployment failed as the Persistence Unit could not be located. Try adding the 'persistence-unit-name' tag in ejb-jar.xml ");
        }
    }
}
