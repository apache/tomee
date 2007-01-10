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
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.ivm.naming.JndiReference;
import org.apache.openejb.core.ivm.naming.NameNode;
import org.apache.openejb.core.ivm.naming.ParsedName;
import org.apache.openejb.core.ivm.naming.PersistenceUnitReference;
import org.apache.openejb.core.ivm.naming.Reference;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.io.File;
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
    private final PersistenceUnitInfo[] persistenceUnitRefs;
    private final PersistenceContextInfo[] persistenceContextRefs;
    private final Map<String, EntityManagerFactory> entityManagerFactories;
    private final Map<String, Map<String, EntityManagerFactory>> allFactories;
    private final String jarPath;
    

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

        if ((jndiEnc != null && jndiEnc.ejbReferences != null)) {
            ejbReferences = jndiEnc.ejbReferences.toArray(new EjbReferenceInfo[0]);
        } else {
            ejbReferences = new EjbReferenceInfo[]{};
        }

        if ((jndiEnc != null && jndiEnc.ejbLocalReferences != null)) {
            ejbLocalReferences = jndiEnc.ejbLocalReferences.toArray(new EjbLocalReferenceInfo[0]);
        } else {
            ejbLocalReferences = new EjbLocalReferenceInfo[]{};
        }

        if ((jndiEnc != null && jndiEnc.envEntries != null)) {
            envEntries = jndiEnc.envEntries.toArray(new EnvEntryInfo[0]);
        } else {
            envEntries = new EnvEntryInfo[]{};
        }

        if ((jndiEnc != null && jndiEnc.resourceRefs != null)) {
            resourceRefs = jndiEnc.resourceRefs.toArray(new ResourceReferenceInfo[0]);
        } else {
            resourceRefs = new ResourceReferenceInfo[]{};
        }

        if ((jndiEnc != null && jndiEnc.persistenceUnitRefs != null)) {
        	persistenceUnitRefs = jndiEnc.persistenceUnitRefs.toArray(new PersistenceUnitInfo[0]);
        } else {
        	persistenceUnitRefs = new PersistenceUnitInfo[]{};
        }      
        
        if ((jndiEnc != null && jndiEnc.persistenceContextRefs != null)) {
        	persistenceContextRefs = jndiEnc.persistenceContextRefs.toArray(new PersistenceContextInfo[0]);
        } else {
        	persistenceContextRefs = new PersistenceContextInfo[]{};
        }

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
                } else {
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

            Reference reference = null;

            if (location != null && !location.remote) {
                if (referenceInfo.homeType == null){
                    // TODO: Before JndiNameStrategy can be used, this assumption has to be updated
                    String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId + "BusinessLocal";
                    reference = new IntraVmJndiReference(jndiName);
                } else {
                    String jndiName = "java:openejb/ejb/" + location.ejbDeploymentId + "Local";
                    reference = new IntraVmJndiReference(jndiName);
                }
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

        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceReferenceInfo referenceInfo = resourceRefs[i];
            Reference reference = null;

            if (referenceInfo.resourceID != null) {
                String jndiName = "java:openejb/Connector/" + referenceInfo.resourceID;
                reference = new IntraVmJndiReference(jndiName);
            } else if (referenceInfo.location != null) {
                String openEjbSubContextName1 = "java:openejb/remote_jndi_contexts/" + referenceInfo.location.jndiContextId;
                String jndiName2 = referenceInfo.location.remoteRefName;
                reference = new JndiReference(openEjbSubContextName1, jndiName2);
            } else {
                String jndiName = "java:openejb/Connector/" + referenceInfo.referenceName;
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), wrapReference(reference));
        }
        
        for (int i = 0; i < persistenceUnitRefs.length; i++){
        	PersistenceUnitInfo puRefInfo = persistenceUnitRefs[i];
            Reference reference = null;
            EntityManagerFactory factory = null;
            if (puRefInfo.persistenceUnitName != null) {    
            	if(puRefInfo.persistenceUnitName.indexOf("#") == -1){
            		factory = entityManagerFactories.get(puRefInfo.persistenceUnitName);
            	}else{
            		factory = findEntityManagerFactory(allFactories,jarPath,puRefInfo.persistenceUnitName);
            	}
            }else if(entityManagerFactories.size() == 1){
            	factory = entityManagerFactories.values().toArray(new EntityManagerFactory[1])[0];
            }else{
            	throw new OpenEJBException("Deployment failed as the Persistence Unit could not be located. Try adding the 'persistence-unit-name' tag in ejb-jar.xml ");
            }
            
            reference = new PersistenceUnitReference(factory);
            bindings.put(normalize(puRefInfo.referenceName), wrapReference(reference));
        }

        for (int i = 0; i < persistenceContextRefs.length; i++) {
            PersistenceContextInfo contextInfo = persistenceContextRefs[i];

            EntityManagerFactory factory;
            if (contextInfo.persistenceUnitName != null) {
                if (contextInfo.persistenceUnitName.indexOf("#") == -1) {
                    factory = entityManagerFactories.get(contextInfo.persistenceUnitName);
                } else {
                    factory = findEntityManagerFactory(allFactories, jarPath, contextInfo.persistenceUnitName);
                }
            } else if (entityManagerFactories.size() == 1) {
                factory = entityManagerFactories.values().toArray(new EntityManagerFactory[1])[0];
            } else {
                throw new OpenEJBException("Deployment failed as the Persistence Unit could not be located. Try adding the 'persistence-unit-name' tag in ejb-jar.xml ");
            }

// TODO create persistence context reference here
//            Reference reference = new PersistenceUnitReference(factory);
//            bindings.put(normalize(contextInfo.referenceName), wrapReference(reference));
        }

        IvmContext enc = new IvmContext(new NameNode(null, new ParsedName("comp"), null));
        try {
            enc.createSubcontext("comp/env");
            enc.lookup("env");
        } catch (NamingException e) {
            throw new IllegalStateException("Unable to create subcontext 'java:comp/env'.  Exception:"+e.getMessage(),e);
        }

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
            return new org.apache.openejb.core.stateful.StatefulEncUserTransaction((CoreUserTransaction) userTransaction);
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
