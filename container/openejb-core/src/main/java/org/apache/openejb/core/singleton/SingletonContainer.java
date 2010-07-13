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
package org.apache.openejb.core.singleton;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleApplicationException;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.handleSystemException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.ejb.Asynchronous;
import javax.ejb.ConcurrentAccessTimeoutException;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.interceptor.AroundInvoke;

import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ExceptionType;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.asynch.AsynchMethodRunnable;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.interceptor.InterceptorStack;
import org.apache.openejb.core.timer.EjbTimerService;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.webservices.AddressingSupport;
import org.apache.openejb.core.webservices.NoAddressingSupport;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Duration;
import org.apache.xbean.finder.ClassFinder;

/**
 * @org.apache.xbean.XBean element="statelessContainer"
 */
public class SingletonContainer implements RpcContainer {

    private SingletonInstanceManager instanceManager;

    private HashMap<String,DeploymentInfo> deploymentRegistry = new HashMap<String,DeploymentInfo>();

    private Object containerID = null;
    private SecurityService securityService;
    private long wait = 30;
    private TimeUnit unit = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> asynchQueue = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor asynchPool = new ThreadPoolExecutor(1, 20, 60, TimeUnit.SECONDS, asynchQueue);
    private CompletionService<Object> asynchService = new ExecutorCompletionService<Object>(asynchPool);

    public SingletonContainer(Object id, SecurityService securityService) throws OpenEJBException {
        this.containerID = id;
        this.securityService = securityService;

        instanceManager = new SingletonInstanceManager(securityService);

        for (DeploymentInfo deploymentInfo : deploymentRegistry.values()) {
            org.apache.openejb.core.CoreDeploymentInfo di = (org.apache.openejb.core.CoreDeploymentInfo) deploymentInfo;
            di.setContainer(this);
        }
    }

    public void setAccessTimeout(Duration duration){
        if (duration.getUnit() != null) {
            this.unit = duration.getUnit();
        }
        this.wait = duration.getTime();
    }

    public synchronized DeploymentInfo [] deployments() {
        return deploymentRegistry.values().toArray(new DeploymentInfo[deploymentRegistry.size()]);
    }

    public synchronized DeploymentInfo getDeploymentInfo(Object deploymentID) {
        String id = (String) deploymentID;
        return deploymentRegistry.get(id);
    }

    public ContainerType getContainerType() {
        return ContainerType.SINGLETON;
    }

    public Object getContainerID() {
        return containerID;
    }

    public void deploy(DeploymentInfo info) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) info;
        instanceManager.deploy(deploymentInfo);
        String id = (String) deploymentInfo.getDeploymentID();
        synchronized (this) {
            deploymentRegistry.put(id, deploymentInfo);
            deploymentInfo.setContainer(this);
        }

        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.start();
        }

        if (deploymentInfo.isLoadOnStartup()){
            try {
                ThreadContext callContext = new ThreadContext(deploymentInfo, null);
                ThreadContext old = ThreadContext.enter(callContext);
                try {
                    instanceManager.getInstance(callContext);
                } finally{
                    ThreadContext.exit(old);
                }
            } catch (OpenEJBException e) {
                throw new OpenEJBException("Singleton startup failed: "+deploymentInfo.getDeploymentID(), e);
            }
        }
    }

    public void undeploy(DeploymentInfo info) {
        undeploy((CoreDeploymentInfo)info);
    }

    private void undeploy(CoreDeploymentInfo deploymentInfo) {
        ThreadContext threadContext = new ThreadContext(deploymentInfo, null);
        ThreadContext old = ThreadContext.enter(threadContext);
        try {
            instanceManager.freeInstance(threadContext);
        } finally{
            ThreadContext.exit(old);
        }
        instanceManager.undeploy(deploymentInfo);
        EjbTimerService timerService = deploymentInfo.getEjbTimerService();
        if (timerService != null) {
            timerService.stop();
        }

        synchronized (this) {
            String id = (String) deploymentInfo.getDeploymentID();
            deploymentInfo.setContainer(null);
            deploymentInfo.setContainerData(null);
            deploymentRegistry.remove(id);
        }
    }

    /**
     * @deprecated use invoke signature without 'securityIdentity' argument.
     */
    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        return invoke(deployID, null, callMethod.getDeclaringClass(), callMethod, args, primKey);
    }

    public Object invoke(Object deployID, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        return invoke(deployID, null, callInterface, callMethod, args, primKey);
    }

    public Object invoke(Object deployID, InterfaceType type, Class callInterface, Method callMethod, Object[] args, Object primKey) throws OpenEJBException {
        CoreDeploymentInfo deployInfo = (CoreDeploymentInfo) this.getDeploymentInfo(deployID);

        if (deployInfo == null) throw new OpenEJBException("Deployment does not exist in this container. Deployment(id='"+deployID+"'), Container(id='"+containerID+"')");

        // Use the backup way to determine call type if null was supplied.
        if (type == null) type = deployInfo.getInterfaceType(callInterface);

        Method runMethod = deployInfo.getMatchingBeanMethod(callMethod);
        if(runMethod.getAnnotation(Asynchronous.class) != null){
        	// Need to invoke this bean asynchronously
        	AsynchMethodRunnable asynch = new SingletonMethodRunnable(callMethod, runMethod, args, type, deployInfo, primKey);
        	Future<Object> future = this.asynchService.submit(asynch);
        	return future;
        }

        ThreadContext callContext = new ThreadContext(deployInfo, primKey);
        ThreadContext oldCallContext = ThreadContext.enter(callContext);
        try {
            boolean authorized = type == InterfaceType.TIMEOUT || getSecurityService().isCallerAuthorized(callMethod, type);
            if (!authorized)
                throw new org.apache.openejb.ApplicationException(new EJBAccessException("Unauthorized Access by Principal Denied"));

            Class declaringClass = callMethod.getDeclaringClass();
            if (EJBHome.class.isAssignableFrom(declaringClass) || EJBLocalHome.class.isAssignableFrom(declaringClass)) {
                if (callMethod.getName().startsWith("create")) {
                    return createEJBObject(deployInfo, callMethod);
                } else
                    return null;// EJBHome.remove( ) and other EJBHome methods are not process by the container
            } else if (EJBObject.class == declaringClass || EJBLocalObject.class == declaringClass) {
                return null;// EJBObject.remove( ) and other EJBObject methods are not process by the container
            }

            Instance instance = instanceManager.getInstance(callContext);

            callContext.setCurrentOperation(type == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS);
            callContext.setCurrentAllowedStates(null);
            callContext.set(Method.class, runMethod);
            callContext.setInvokedInterface(callInterface);

            Object retValue = _invoke(callMethod, runMethod, args, instance, callContext, type);

            return retValue;

        } finally {
            ThreadContext.exit(oldCallContext);
        }
    }

    private SecurityService getSecurityService() {
        return securityService;
    }

    public SingletonInstanceManager getInstanceManager() {
        return instanceManager;
    }

    protected Object _invoke(Method callMethod, Method runMethod, Object[] args, Instance instance, ThreadContext callContext, InterfaceType callType) throws OpenEJBException {
        CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();

        boolean read = deploymentInfo.getConcurrencyAttribute(runMethod) == javax.ejb.LockType.READ;

        final Lock lock = aquireLock(read, instance);

        Object returnValue;
        try {
            TransactionPolicy txPolicy = createTransactionPolicy(deploymentInfo.getTransactionType(callMethod), callContext);

            returnValue = null;
            try {
                if (callType == InterfaceType.SERVICE_ENDPOINT) {
                    callContext.setCurrentOperation(Operation.BUSINESS_WS);
                    returnValue = invokeWebService(args, deploymentInfo, runMethod, instance);
                } else {
                    List<InterceptorData> interceptors = deploymentInfo.getMethodInterceptors(runMethod);
                    InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, callType == InterfaceType.TIMEOUT ? Operation.TIMEOUT : Operation.BUSINESS, interceptors,
                            instance.interceptors);
                    returnValue = interceptorStack.invoke(args);
                }
            } catch (Throwable e) {// handle reflection exception
                ExceptionType type = deploymentInfo.getExceptionType(e);
                if (type == ExceptionType.SYSTEM) {
                    /* System Exception ****************************/

                    // The bean instance is not put into the pool via instanceManager.poolInstance
                    // and therefore the instance will be garbage collected and destroyed.
                    // For this reason the discardInstance method of the StatelessInstanceManager
                    // does nothing.
                    handleSystemException(txPolicy, e, callContext);
                } else {
                    /* Application Exception ***********************/

                    handleApplicationException(txPolicy, e, type == ExceptionType.APPLICATION_ROLLBACK);
                }
            } finally {
                afterInvoke(txPolicy, callContext);
            }
        } finally {
            lock.unlock();
        }

        return returnValue;
    }

    private Lock aquireLock(boolean read, Instance instance) {
        final Lock lock;
        if (read) {
            lock = instance.lock.readLock();
        } else {
            lock = instance.lock.writeLock();
        }

        try {
            if (!lock.tryLock(wait, unit)){
                throw new ConcurrentAccessTimeoutException();
            }
        } catch (InterruptedException e) {
            throw (ConcurrentAccessTimeoutException) new ConcurrentAccessTimeoutException().initCause(e);
        }
        return lock;
    }

    private Object invokeWebService(Object[] args, CoreDeploymentInfo deploymentInfo, Method runMethod, Instance instance) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("WebService calls must follow format {messageContext, interceptor, [arg...]}.");
        }

        Object messageContext = args[0];

        if (messageContext == null) throw new IllegalArgumentException("MessageContext is null.");

        // This object will be used as an interceptor in the stack and will be responsible
        // for unmarshalling the soap message parts into an argument list that will be
        // used for the actual method invocation.
        //
        // We just need to make it an interceptor in the OpenEJB sense and tack it on the end
        // of our stack.
        Object interceptor = args[1];

        if (interceptor == null) throw new IllegalArgumentException("Interceptor instance is null.");

        //  Add the webservice interceptor to the list of interceptor instances
        Map<String, Object> interceptors = new HashMap<String, Object>(instance.interceptors);
        {
            interceptors.put(interceptor.getClass().getName(), interceptor);
        }

        //  Create an InterceptorData for the webservice interceptor to the list of interceptorDatas for this method
        List<InterceptorData> interceptorDatas = new ArrayList<InterceptorData>(deploymentInfo.getMethodInterceptors(runMethod));
        {
            InterceptorData providerData = new InterceptorData(interceptor.getClass());
            ClassFinder finder = new ClassFinder(interceptor.getClass());
            providerData.getAroundInvoke().addAll(finder.findAnnotatedMethods(AroundInvoke.class));
            interceptorDatas.add(providerData);
        }

        InterceptorStack interceptorStack = new InterceptorStack(instance.bean, runMethod, Operation.BUSINESS_WS, interceptorDatas, interceptors);
        Object[] params = new Object[runMethod.getParameterTypes().length];
        if (messageContext instanceof javax.xml.rpc.handler.MessageContext) {
            ThreadContext.getThreadContext().set(javax.xml.rpc.handler.MessageContext.class, (javax.xml.rpc.handler.MessageContext) messageContext);
            return interceptorStack.invoke((javax.xml.rpc.handler.MessageContext) messageContext, params);
        } else if (messageContext instanceof javax.xml.ws.handler.MessageContext) {
            AddressingSupport wsaSupport = NoAddressingSupport.INSTANCE;
            for (int i = 2; i < args.length; i++) {
                if (args[i] instanceof AddressingSupport) {
                    wsaSupport = (AddressingSupport)args[i];
                }
            }
            ThreadContext.getThreadContext().set(AddressingSupport.class, wsaSupport);
            ThreadContext.getThreadContext().set(javax.xml.ws.handler.MessageContext.class, (javax.xml.ws.handler.MessageContext) messageContext);
            return interceptorStack.invoke((javax.xml.ws.handler.MessageContext) messageContext, params);
        }
        throw new IllegalArgumentException("Uknown MessageContext type: " + messageContext.getClass().getName());
    }

    protected ProxyInfo createEJBObject(org.apache.openejb.core.CoreDeploymentInfo deploymentInfo, Method callMethod) {
        return new ProxyInfo(deploymentInfo, null);
    }

    public class SingletonMethodRunnable extends AsynchMethodRunnable{

    	public SingletonMethodRunnable(
				Method callMethod,
				Method runMethod, Object[] args, InterfaceType type,
				CoreDeploymentInfo deployInfo, Object primKey) {
			super(callMethod, runMethod, args, type, deployInfo, primKey);
		}

		protected Object performInvoke(Object bean, ThreadContext callContext) throws OpenEJBException{
    		return _invoke(this.callMethod, this.runMethod, this.args, (Instance)bean, callContext, this.type);
    	}

		@Override
		protected Object createBean(ThreadContext callContext) throws OpenEJBException{
			return instanceManager.getInstance(callContext);
		}

		@Override
		protected void releaseBean(Object bean, ThreadContext callContext) throws OpenEJBException{
			// Singleton doesn't have to do anything here
		}

    }
}
