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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;
import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.apache.openejb.SystemException;
import org.apache.openejb.BeanType;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.springframework.beans.factory.FactoryBean;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractDeploymentFactory implements FactoryBean {
    protected String id;
    protected String homeInterface;
    protected String remoteInterface;
    protected String localHomeInterface;
    protected String localInterface;
    protected String businessLocalInterface;
    protected String businessRemoteInterface;
    protected String beanClass;
    protected ClassLoader classLoader;
    protected EncInfo jndiContext;
    private String jarPath;
    protected TransactionManager transactionManager;
    protected AssemblyInfo assembly;
    protected final Map<String, String> roleReferences = new LinkedHashMap<String, String>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessLocalInterface() {
        return businessLocalInterface;
    }

    public void setBusinessLocalInterface(String businessLocalInterface) {
        this.businessLocalInterface = businessLocalInterface;
    }

    public String getBusinessRemoteInterface() {
        return businessRemoteInterface;
    }

    public void setBusinessRemoteInterface(String businessRemoteInterface) {
        this.businessRemoteInterface = businessRemoteInterface;
    }

    public String getHomeInterface() {
        return homeInterface;
    }

    public void setHomeInterface(String homeInterface) {
        this.homeInterface = homeInterface;
    }

    public String getRemoteInterface() {
        return remoteInterface;
    }

    public void setRemoteInterface(String remoteInterface) {
        this.remoteInterface = remoteInterface;
    }

    public String getLocalHomeInterface() {
        return localHomeInterface;
    }

    public void setLocalHomeInterface(String localHomeInterface) {
        this.localHomeInterface = localHomeInterface;
    }

    public String getLocalInterface() {
        return localInterface;
    }

    public void setLocalInterface(String localInterface) {
        this.localInterface = localInterface;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public EncInfo getJndiContext() {
        return jndiContext;
    }

    public void setJndiContext(EncInfo jndiContext) {
        this.jndiContext = jndiContext;
    }

    public String getJarPath() {
//        try {
//            File file = SystemInstance.get().getHome().getFile(jarPath, false).getCanonicalFile();
//            return file.getAbsolutePath();
//        } catch (IOException e) {
            return jarPath;
//        }
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public AssemblyInfo getAssembly() {
        return assembly;
    }

    public void setAssembly(AssemblyInfo assembly) {
        this.assembly = assembly;
    }

    public Class getObjectType() {
        return CoreDeploymentInfo.class;
    }

    public boolean isSingleton() {
        return true;
    }

    protected static Class loadClass(String name, ClassLoader classLoader) throws SystemException {
        if (name != null) {
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new SystemException(e);
            }
        }
        return null;
    }

    public Object getObject() throws Exception {
        CoreDeploymentInfo deploymentInfo = createDeploymentInfo();
        return deploymentInfo;
    }

    protected abstract BeanType getComponentType();

    protected abstract boolean isBeanManagedTransaction();

    protected abstract String getPkClass();

    protected CoreDeploymentInfo createDeploymentInfo() throws SystemException {
        EncBuilder encBuilder = new EncBuilder(jndiContext, getComponentType(), isBeanManagedTransaction(), transactionManager);
        Context context = encBuilder.createContext();

        DeploymentContext deploymentContext = new DeploymentContext(id, classLoader, context);
        CoreDeploymentInfo deploymentInfo = new CoreDeploymentInfo(deploymentContext,
                loadClass(beanClass, classLoader), loadClass(homeInterface, classLoader),
                loadClass(remoteInterface, classLoader),
                loadClass(localHomeInterface, classLoader),
                loadClass(localInterface, classLoader),
                loadClass(businessLocalInterface, classLoader),
                loadClass(businessRemoteInterface, classLoader),
                loadClass(getPkClass(), classLoader),
                getComponentType(),
                null);
        //deploymentInfo.setJarPath(getJarPath());
        if (assembly != null) {
            applySecurityRoleReference(deploymentInfo);
            // todo we should be able to apply tx attributes and security permissions here
            // but the code in the deployment tries to access the container which isn't available here
            // when we change the container to not need the deployments in the constructor, we can
            // move the code from assembler to here
        }
        return deploymentInfo;
    }

    private void applySecurityRoleReference(CoreDeploymentInfo deployment) {
        Map<String, String[]> roleMappings = new TreeMap<String, String[]>();
        for (RoleMapping roleMapping : AssemblerUtil.asList(assembly.roleMappings)) {
            roleMappings.put(roleMapping.logical, roleMapping.physical);
        }

        for (Map.Entry<String, String> roleRef : roleReferences.entrySet()) {
            String roleLink = roleRef.getKey();
            String roleName = roleRef.getValue();


            String[] physicalRoles = roleMappings.get(roleLink);
            deployment.addSecurityRoleReference(roleName, physicalRoles);
        }
    }
}
