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
package org.openejb.assembler.spring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;
import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.openejb.SystemException;
import org.openejb.loader.SystemInstance;
import org.openejb.core.DeploymentContext;
import org.openejb.core.CoreDeploymentInfo;
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
        try {
            File file = SystemInstance.get().getHome().getFile(jarPath, false).getCanonicalFile();
            return file.getAbsolutePath();
        } catch (IOException e) {
            return jarPath;
        }
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

    protected abstract byte getComponentType();

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
                null, null, loadClass(getPkClass(), classLoader),
                getComponentType(),
                null);
        deploymentInfo.setJarPath(getJarPath());
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
