/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.spring;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.DeploymentLoader;
import org.apache.openejb.alt.config.DeploymentModule;
import org.apache.openejb.alt.config.EjbJarInfoBuilder;
import org.apache.openejb.alt.config.EjbModule;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.assembler.classic.EjbJarBuilder;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.persistence.GlobalJndiDataSourceResolver;
import org.apache.openejb.persistence.PersistenceDeployer;
import org.apache.openejb.persistence.PersistenceDeployerException;
import org.apache.xbean.finder.ResourceFinder;
import org.springframework.beans.factory.FactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @org.apache.xbean.XBean element="deployments"
 */
public class DeploymentsFactory implements FactoryBean {

    private AssemblyInfo assembly;
    private TransactionManager transactionManager;
    private Object value;
    private DeploymentLoader.Type type;

    public AssemblyInfo getAssembly() {
        return assembly;
    }

    public void setAssembly(AssemblyInfo assembly) {
        this.assembly = assembly;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getJar() {
        return (String) value;
    }

    public void setJar(String jar) {
        this.type = DeploymentLoader.Type.JAR;
        this.value = jar;
    }

    public String getDir() {
        return (String) value;
    }

    public void setDir(String dir) {
        this.type = DeploymentLoader.Type.DIR;
        this.value = dir;
    }

    public ClassLoader getClasspath() {
        return (ClassLoader) value;
    }

    public void setClasspath(ClassLoader classpath) {
        this.type = DeploymentLoader.Type.CLASSPATH;
        this.value = classpath;
    }

    // Singletons don't work
    private HashMap<String, DeploymentInfo> deployments;
    public Object getObject() throws Exception {
        if (deployments != null){
            return deployments;
        }
        HashMap context = new HashMap();
        context.put(TransactionManager.class.getName(), transactionManager);
        org.apache.openejb.assembler.classic.Assembler.setContext(context);

        DeploymentLoader loader = new DeploymentLoader();
        List<DeploymentModule> deployedJars = loader.load(type, value);

        EjbJarInfoBuilder infoBuilder = new EjbJarInfoBuilder();

        ClassLoader classLoader = (value instanceof ClassLoader) ? (ClassLoader) value : Thread.currentThread().getContextClassLoader();
        EjbJarBuilder builder = new EjbJarBuilder(classLoader);

        deployments = new HashMap();
        HashMap<String, Map> allFactories = new HashMap();
        for (DeploymentModule module : deployedJars) {
            if (!(module instanceof EjbModule)) {
                continue;
            }
            EjbModule jar = (EjbModule) module;
        	PersistenceDeployer pm = null;        
            Map<String, EntityManagerFactory> factories = null;
            try {
            	pm = new PersistenceDeployer(new GlobalJndiDataSourceResolver(null));
            	URL url = new File(jar.getJarURI()).toURL();
            	ClassLoader tmpClassLoader = new URLClassLoader(new URL[]{url}, classLoader);
            	ResourceFinder resourceFinder = new ResourceFinder("",tmpClassLoader,url);        	
            	factories = pm.deploy(resourceFinder.findAll("META-INF/persistence.xml"),classLoader);
    		} catch (PersistenceDeployerException e1) {
    			throw new OpenEJBException(e1);			
    		} catch (IOException e) {
    			throw new OpenEJBException(e);
    		}
    		allFactories.put(jar.getJarURI(),factories);                
        }            

        for (DeploymentModule module : deployedJars) {
            if (!(module instanceof EjbModule)) {
                continue;
            }
            EjbModule jar = (EjbModule) module;

            EjbJarInfo jarInfo = infoBuilder.buildInfo(jar);
            if (jarInfo == null){
                // This means the jar failed validation or otherwise could not be deployed
                // a message was already logged to the appropriate place.
                continue;
            }

            transferMethodTransactionInfos(infoBuilder);
            transferMethodPermissionInfos(infoBuilder);

            HashMap<String, DeploymentInfo> ejbs = builder.build(jarInfo,allFactories);

            for (EjbDeployment data : jar.getOpenejbJar().getEjbDeployment()) {
                ((CoreDeploymentInfo)ejbs.get(data.getDeploymentId())).setContainer(new ContainerPointer(data.getContainerId()));
            }

            deployments.putAll(ejbs);
        }

        return deployments;
    }

    private void transferMethodTransactionInfos(EjbJarInfoBuilder infoBuilder) {
        List<MethodTransactionInfo> infos = new ArrayList();
        if (assembly.getMethodTransactions() != null){
            infos.addAll(Arrays.asList(assembly.getMethodTransactions()));
        }
        for (org.apache.openejb.assembler.classic.MethodTransactionInfo info : infoBuilder.getMethodTransactionInfos()) {
            infos.add(new MethodTransactionInfo(info));
        }
        assembly.setMethodTransactions(infos.toArray(new MethodTransactionInfo[]{}));
    }

    private void transferMethodPermissionInfos(EjbJarInfoBuilder infoBuilder) {
        List<MethodPermissionInfo> infos = new ArrayList();
        if (assembly.getMethodPermissions() != null){
            infos.addAll(Arrays.asList(assembly.getMethodPermissions()));
        }
        for (org.apache.openejb.assembler.classic.MethodPermissionInfo info : infoBuilder.getMethodPermissionInfos()) {
            infos.add(new MethodPermissionInfo(info));
        }
        assembly.setMethodPermissions(infos.toArray(new MethodPermissionInfo[]{}));
    }

    public Class getObjectType() {
        return Map.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
