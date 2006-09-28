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
package org.apache.openejb;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.naming.Context;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.PersistentTimer;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.SystemMethodIndices;
import org.apache.openejb.security.PermissionManager;
import org.apache.openejb.timer.BasicTimerServiceImpl;
import org.apache.openejb.transaction.TransactionPolicyManager;
import org.apache.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractEjbDeployment implements EjbDeployment, ExtendedEjbDeployment {
    protected final Log log;
    protected final String containerId;
    protected final String ejbName;
    protected final Class beanClass;
    protected final ClassLoader classLoader;
    protected final String policyContextId;
    protected final Subject runAs;
    protected final Subject defaultSubject;
    protected final InterfaceMethodSignature[] signatures;
    protected final TransactionPolicyManager transactionPolicyManager;
    protected final PermissionManager permissionManager;
    protected final Context componentContext;
    protected final EjbContainer ejbContainer;
    protected final TransactionManager transactionManager;
    protected final boolean beanManagedTransactions;
    protected final SortedMap transactionPolicies;
    protected final SystemMethodIndices systemMethodIndices;
    protected final BasicTimerServiceImpl timerService;
    protected final boolean securityEnabled;

    // connector stuff
    // todo find a new home for this data
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;

    public AbstractEjbDeployment(String containerId,
            String ejbName,

            Class beanClass,
            ClassLoader classLoader,
            SignatureIndexBuilder signatureIndexBuilder,

            EjbContainer ejbContainer,

            boolean securityEnabled,
            String policyContextId,
            Subject defaultSubject,
            Subject runAs,

            boolean beanManagedTransactions,
            SortedMap transactionPolicies,

            Map componentContext,

            Set unshareableResources,
            Set applicationManagedSecurityResources) throws Exception {

        assert (containerId != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (classLoader != null);

        log = LogFactory.getLog("org.apache.openejb.EjbContainer." + ejbName);

        this.containerId = containerId;
        this.ejbName = ejbName;

        // load the bean classes
        this.classLoader = classLoader;
        this.beanClass = beanClass;

        this.ejbContainer = ejbContainer;

        this.securityEnabled = securityEnabled;
        this.policyContextId = policyContextId;
        this.runAs = runAs;

        this.beanManagedTransactions = beanManagedTransactions;
        this.transactionPolicies = transactionPolicies;

        signatures = signatureIndexBuilder.createSignatureIndex();

        permissionManager = new PermissionManager(ejbName, signatures);
        transactionPolicyManager = new TransactionPolicyManager(beanManagedTransactions, transactionPolicies, signatures);

        this.defaultSubject = defaultSubject;

        UserTransaction userTransaction;
        if (beanManagedTransactions) {
            userTransaction = ejbContainer.getUserTransaction();
        } else {
            userTransaction = null;
        }

        // TODO: Kernel reference
        this.componentContext = EnterpriseNamingContext.createEnterpriseNamingContext(componentContext, userTransaction, null, classLoader);

        transactionManager = ejbContainer.getTransactionManager();

        if (EntityBean.class.isAssignableFrom(beanClass)) {
            systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setEntityContext", EntityContext.class.getName(), "unsetEntityContext");
        } else if (SessionBean.class.isAssignableFrom(beanClass)) {
            systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setSessionContext", SessionContext.class.getName(), null);
        } else {
            systemMethodIndices = SystemMethodIndices.createSystemMethodIndices(signatures, "setMessageDrivenContext", MessageDrivenContext.class.getName(), null);
        }

        if (TimedObject.class.isAssignableFrom(getBeanClass())) {
            PersistentTimer timer = getTimer();
            if (timer != null) {
                String kernelName = null; //TODO: kernel.getKernelName();
                timerService = new BasicTimerServiceImpl(this, ejbContainer, timer, kernelName, containerId);
            } else {
                timerService = null;
            }
        } else {
            timerService = null;
        }

        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    private PersistentTimer getTimer() {
        if (beanManagedTransactions) {
            return ejbContainer.getTransactedTimer();
        }

        InterfaceMethodSignature signature = new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false);
        TransactionPolicyType transactionPolicy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "timeout", signature);
        boolean isTransacted = transactionPolicy == TransactionPolicyType.Required || transactionPolicy == TransactionPolicyType.RequiresNew;
        if (isTransacted) {
            return ejbContainer.getTransactedTimer();
        }

        return ejbContainer.getNontransactedTimer();
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        ejbInvocation.setEjbDeployment(this);
        try {
            return ejbContainer.invoke(invocation);
        } finally {
            ejbInvocation.setEjbDeployment(null);
        }
    }

    public void logSystemException(Throwable t) {
        log.warn(ejbName, t);
    }

    public String getContainerId() {
        return containerId;
    }

    public String getEjbName() {
        return ejbName;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public TransactionPolicyManager getTransactionPolicyManager() {
        return transactionPolicyManager;
    }

    public Context getComponentContext() {
        return componentContext;
    }

    public Subject getDefaultSubject() {
        return defaultSubject;
    }

    public Subject getRunAsSubject() {
        return runAs;
    }

    public BasicTimerServiceImpl getTimerService() {
        return timerService;
    }

    public Timer getTimerById(Long id) {
        assert timerService != null;
        return timerService.getTimerById(id);
    }

    public abstract int getMethodIndex(Method method);

    public InterfaceMethodSignature[] getSignatures() {
        // return a copy just to be safe... this method should not be called often
        InterfaceMethodSignature[] copy = new InterfaceMethodSignature[signatures.length];
        System.arraycopy(signatures, 0, copy, 0, signatures.length);
        return copy;
    }

    public EjbDeployment getUnmanagedReference() {
        return this;
    }

    public String getObjectName() {
        return containerId;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return false;
    }

    public void doStart() throws Exception {
        if (defaultSubject != null) {
            ContextManager.registerSubject(defaultSubject);
        }
        if (runAs != null) {
            ContextManager.registerSubject(runAs);
        }

        if (timerService != null) {
            timerService.doStart();
        }

        log.debug("Started " + containerId);
    }

    public final void doStop() throws Exception {
        destroy();

        log.debug("Stopped '" + containerId);
    }

    public final void doFail() {
        try {
            destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.error("Failed '" + containerId);
    }

    protected void destroy() throws PersistenceException {
        if (defaultSubject != null) {
            ContextManager.unregisterSubject(defaultSubject);
        }
        if (runAs != null) {
            ContextManager.unregisterSubject(runAs);
        }

        if (timerService != null) {
            timerService.doStop();
        }
    }

    public static Class loadClass(String className, ClassLoader classLoader, String description) throws ClassNotFoundException {
        if (className == null) {
            return null;
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Unable to load " + description + " " + className);
        }
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }
}
