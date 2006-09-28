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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.proxy.ProxyInfo;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractRpcDeployment extends AbstractEjbDeployment implements RpcEjbDeployment {

    private final ProxyInfo proxyInfo;

    private final String[] jndiNames;
    private final String[] localJndiNames;

    protected final EJBProxyFactory proxyFactory;


    public AbstractRpcDeployment(String containerId,
            String ejbName,

            ProxyInfo proxyInfo,
            Class beanClass,
            ClassLoader classLoader,
            SignatureIndexBuilder signatureIndexBuilder,

            EjbContainer ejbContainer,

            String[] jndiNames,
            String[] localJndiNames,

            boolean securityEnabled,
            String policyContextId,
            Subject subject,
            Subject runAs,

            boolean beanManagedTransactions,
            SortedMap transactionPolicies,

            Map componentContext,

            Set unshareableResources,
            Set applicationManagedSecurityResources) throws Exception {

        super(containerId,
                ejbName,
                beanClass,
                classLoader,
                signatureIndexBuilder,
                ejbContainer,
                securityEnabled,
                policyContextId,
                subject,
                runAs,
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                unshareableResources,
                applicationManagedSecurityResources);

        assert (containerId != null);
        assert (ejbName != null && ejbName.length() > 0);
        assert (classLoader != null);

        // load the bean classes
        this.proxyInfo = proxyInfo;

        if (jndiNames == null) {
            jndiNames = new String[0];
        }
        this.jndiNames = jndiNames;
        if (localJndiNames == null) {
            localJndiNames = new String[0];
        }
        this.localJndiNames = localJndiNames;

        // create the proxy factory
        // NOTE: this can't be called until the signaure array has been built and the proxy info has been set
        proxyFactory = new EJBProxyFactory(this);

        // TODO maybe there is a more suitable place to do this.  Maybe not.
        setupJndi();
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public String[] getJndiNames() {
        return copyNames(jndiNames);
    }

    public String[] getLocalJndiNames() {
        return copyNames(localJndiNames);
    }

    public EJBHome getEjbHome() {
        return proxyFactory.getEJBHome();
    }

    public EJBObject getEjbObject(Object primaryKey) {
        return proxyFactory.getEJBObject(primaryKey);
    }

    public EJBLocalHome getEjbLocalHome() {
        return proxyFactory.getEJBLocalHome();
    }

    public EJBLocalObject getEjbLocalObject(Object primaryKey) {
        return proxyFactory.getEJBLocalObject(primaryKey);
    }

    public int getMethodIndex(Method method) {
        return proxyFactory.getMethodIndex(method);
    }

    public Object invoke(Method method, Object[] args, Object primKey) throws Throwable {
        EJBInterfaceType invocationType = null;
        int index = getMethodIndex(method);

        Class serviceEndpointInterface = getProxyInfo().getServiceEndpointInterface();

        Class clazz = method.getDeclaringClass();
        if (EJBHome.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.HOME;
        } else if (EJBObject.class.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.REMOTE;
        } else if (serviceEndpointInterface != null && serviceEndpointInterface.isAssignableFrom(clazz)) {
            invocationType = EJBInterfaceType.WEB_SERVICE;
        } else {
            throw new IllegalArgumentException("Legacy invoke interface only supports remote and service-endpoint interfaces: " + clazz);
        }

        // extract the primary key from home ejb remove invocations
        if (invocationType == EJBInterfaceType.HOME && method.getName().equals("remove")) {
            primKey = args[0];
            if (primKey instanceof Handle) {
//                Handle handle = (Handle) primKey;
                //TODO: This naughty code uses org.apache.openejb.client
//                EJBObjectProxy ejbObject = (EJBObjectProxy) handle.getEJBObject();
//                EJBObjectHandler handler = ejbObject.getEJBObjectHandler();
//                primKey = handler.getRegistryId();
            }
        }

        EjbInvocation invocation = new EjbInvocationImpl(invocationType, primKey, index, args);

        InvocationResult result = null;
        try {
            result = invoke(invocation);
        } catch (Throwable t) {
            RemoteException re;
            if (t instanceof RemoteException) {
                re = (RemoteException) t;
            } else {
                re = new RemoteException("The bean encountered a non-application exception. method", t);
            }
            throw new InvalidateReferenceException(re);
        }

        if (result.isException()) {
            throw new ApplicationException(result.getException());
        } else {
            return result.getResult();
        }
    }

    public void doStart() throws Exception {
        super.doStart();
    }

    protected void destroy() throws PersistenceException {
        super.destroy();
    }

    private static String[] copyNames(String[] names) {
        if (names == null) {
            return null;
        }
        int length = names.length;
        String[] copy = new String[length];
        System.arraycopy(names, 0, copy, 0, length);
        return copy;
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(Context.URL_PKG_PREFIXES);
        if (str == null) {
            str = ":org.apache.geronimo.naming";
        } else {
            if (str.indexOf(":org.apache.geronimo.naming") < 0) {
                str = str + ":org.apache.geronimo.naming";
            }
        }
        System.setProperty(Context.URL_PKG_PREFIXES, str);
    }
}
