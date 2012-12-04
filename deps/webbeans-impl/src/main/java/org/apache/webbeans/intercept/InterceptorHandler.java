/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.intercept;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.interceptor.InvocationContext;
import org.apache.webbeans.proxy.MethodHandler;

import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.decorator.DelegateHandler;
import org.apache.webbeans.decorator.WebBeansDecoratorConfig;
import org.apache.webbeans.decorator.WebBeansDecoratorInterceptor;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.proxy.ProxyFactory;
import org.apache.webbeans.util.ClassUtil;

/**
 * Logic for how interceptors & decorators work in OWB.
 * 
 * <ul>
 * <li><b>1- Configuration of decorators and interceptors</b>
 * <p>
 * Decorators and Interceptors are configured from {@link org.apache.webbeans.config.BeansDeployer}
 * class via methods <code>defineManagedBean(class)</code> and Those methods further call
 * <code>defineInterceptor(interceptor class)</code> and <code>defineDecorator(decorator class)</code>
 * methods. Those methods finally call
 * {@link org.apache.webbeans.util.WebBeansUtil#defineInterceptor(org.apache.webbeans.component.creation.ManagedBeanCreatorImpl,
 *        javax.enterprise.inject.spi.ProcessInjectionTarget)} and
 * {@link org.apache.webbeans.util.WebBeansUtil#defineDecorator(org.apache.webbeans.component.creation.ManagedBeanCreatorImpl,
 *        javax.enterprise.inject.spi.ProcessInjectionTarget)}
 * methods for actual configuration.
 * <p>
 * Let's look at the "WebBeansUtil's" methods; 
 * </p>
 * <ul>
 * <li>
 * <code>defineInterceptor</code> : This method firstly
 * creates a "Managed Bean" for the given interceptor with
 * "WebBeansType.INTERCEPTOR" as a type. After checking some controls, it calls
 * "WebBeansInterceptorConfig#configureInterceptorClass".
 * "configureInterceptorClass" method creates a "WebBeansInterceptor" instance
 * that wraps the given managed bean instance and configuring interceptor's
 * *Interceptor Binding* annotations. If everything goes well, it adds
 * interceptor instance into the "BeanManager" interceptor list.
 * </li>
 * <li><code>defineDecorator</code> : Exactly doing same thing as "defineInterceptor". If
 * everything goes well, it adds decorator instance into the "BeanManager"
 * decorator list.</li>
 * </p>
 * </li></ul>
 * <li><b>2* Configuring ManagedBean Instance Interceptor and Decorator Stack</b>
 * <p>
 * Currently interceptors and decorators are supported for the "Managed Beans".
 * OWB delegates calling of "EJB Beans" interceptors to the EJB container. It
 * does not provide built-in interceptor and decorator support for EJB beans.
 * Current implementation supports configuration of the interceptors on the
 * "Managed Beans" with 2 different scenarios, i.e. it supports
 * "EJB related interceptors ( defined by EJB specification)" and
 * "JSR-299 related interceptors (defined by interceptor bindings)". Managed
 * Beans interceptor and decorator stacks are configured after they are
 * instantiated by the container first time. This method can be found in the
 * AbstractInjectionTargetBean" class "afterConstructor()" method. Actual
 * configuration is done by the
 * {@link org.apache.webbeans.config.DefinitionUtil#defineBeanInterceptorStack
 *        (org.apache.webbeans.component.AbstractInjectionTargetBean)} and
 * {@link org.apache.webbeans.config.DefinitionUtil#defineDecoratorStack}. In
 * "DefinitionUtil.defineBeanInterceptorStack", firstly it configures
 * "EJB spec. interceptors" after that configures "JSR-299 spec. interceptors."
 * In "DefinitionUtil.defineDecoratorStack", it configures
 * decorator stack. "EJBInterceptorConfig" class is responsible for finding all
 * interceptors for given managed bean class according to the EJB Specification.
 * (But as you said, it may not include AroundInvoke/PostConstruct etc.
 * disablement scenario!). "WebBeansInterceptorConfig" class is responsible for
 * finding all interceptors for a given managed bean class according to the
 * "JSR-299, spec." It adds all interceptors into the bean's interceptor stack.
 * It first adds "EJB" related interceptors, after that adds "JSR-299" related
 * interceptors. For "JSR-299" related interceptors, it orders the interceptors
 * according to the "InterceptorComparator". Basically, it puts interceptors in
 * order according to how they are ordered in a "beans.xml" configuration file.
 * Similarly, it configures managed bean's decorator stack according to the
 * decorator resolution rules. Also, it orders decorators according to the
 * "beans.xml" configuration file that contains decorator declarations.
 * </p>
 * </li>
 * <li><b>3* Invocation of Interceptors and Decorators</b>
 * <p>
 * Invocation is handled by the "InterceptorHandler" class (It has an absurd
 * name, it can be changed to a more meaningful name :)). It works nearly same
 * as what you have explained. First of all, it checks that calling method is a
 * business method of a managed bean or not. After that it filters interceptor
 * stack for calling method (Current design of filtering may not be optimal!).
 * Firstly it adds EJB interceptor to the list and then adds JSR-299
 * interceptors. After that, it starts to call all interceptors in order. After
 * consuming all interceptors it calls decorators. (as you explained, seems that
 * the logic may not be correct here. Currently, interceptors and decorators are
 * not related with each other. They are called independently).This must be changed!.
 * </p>
 * </li>
 * </ul>
 * 
 * @version $Rev: 1410305 $ $Date: 2012-11-16 12:10:00 +0100 (ven., 16 nov. 2012) $
 * 
 * @see WebBeansInterceptorConfig
 * @see WebBeansDecoratorConfig
 * @see org.apache.webbeans.intercept.webbeans.WebBeansInterceptor
 * @see org.apache.webbeans.decorator.WebBeansDecorator
 * @see org.apache.webbeans.intercept.ejb.EJBInterceptorConfig
 */
public abstract class InterceptorHandler implements InvocationHandler, MethodHandler, Serializable
{
    /**Default serial id*/
    private static final long serialVersionUID = 1L;
    
    private final static Logger logger = WebBeansLoggerFacade.getLogger(InterceptorHandler.class);
    
    /**Proxied bean*/
    protected OwbBean<?> bean = null;
    
    /**Intercepted methods*/
    protected transient volatile Map<Method, List<InterceptorData>> interceptedMethodMap = null;

    protected WebBeansContext webBeansContext;

    private volatile DelegateHandler decoratorDelegateHandler = null;

    /**
     * Creates a new handler.
     * @param bean proxied bean
     */
    protected InterceptorHandler(OwbBean<?> bean)
    {
        this.bean = bean;
        webBeansContext = bean.getWebBeansContext();
//        new Exception().fillInStackTrace().printStackTrace();
    }

    /**
     * This method provides a way to implement a negative cache for methods
     * which are known to become intercepted or decorated.
     * This is useful since the calculation actually takes a lot of time.
     * @param method which should get invoked
     * @return <code>true</code> if the method is known to not get intercepted,
     *         <code>false</code> we dont know or it gets intercepted
     * @see #setNotInterceptedOrDecoratedMethod(java.lang.reflect.Method)
     */
    protected boolean isNotInterceptedOrDecoratedMethod(Method method)
    {
        return false;
    }

    /**
     * This method will get called after the interceptorStack got evaluated and we
     * found out that it isnt intercepted nor decorated.
     * The information might get cache to skip the evaluation in a later invocation.
     * @param method
     *
     * @see #isNotInterceptedOrDecoratedMethod(java.lang.reflect.Method)
     */
    protected void setNotInterceptedOrDecoratedMethod(Method method)
    {
        // do nothing by default
    }


    /**
     * Calls decorators and interceptors and actual
     * bean method.
     * @param instance actual bean instance
     * @param method business method
     * @param proceed proceed method
     * @param arguments method arguments
     * @param ownerCreationalContext bean creational context
     * @return method result
     * @throws Exception for exception
     */
    public Object invoke(Object instance, Method method, Method proceed, Object[] arguments, CreationalContextImpl<?> ownerCreationalContext) throws Exception
    {
        return invoke(instance, method, arguments, ownerCreationalContext);
    }

    public Object invoke(Object instance, Method method, Object[] arguments, CreationalContextImpl<?> ownerCreationalContext) throws Exception
    {
        if (instance == null)
        {
            return null;
        }
        
        //Result of invocation
        Object result = null;
        
        try
        {
            boolean isNotInterceptedOrDecoratedMethod = isNotInterceptedOrDecoratedMethod(method);

            //Calling method name on Proxy
            String methodName = method.getName();
            if (!isNotInterceptedOrDecoratedMethod &&
                !ClassUtil.isObjectMethod(methodName) && bean instanceof InjectionTargetBean<?>)
            {
                InjectionTargetBean<?> injectionTarget = (InjectionTargetBean<?>) bean;
                InterceptorDataImpl decoratorInterceptorDataImpl = null;
                
                //Check method is business method
                if (webBeansContext.getInterceptorUtil().isWebBeansBusinessMethod(method))
                {
                    if (!injectionTarget.getDecoratorStack().isEmpty())
                    {
                        resolveDecoratorDelegateHandler(instance, ownerCreationalContext, injectionTarget);
                    }

                    // Run around invoke chain
                    List<InterceptorData> interceptorStack = injectionTarget.getInterceptorStack();
                    if (!interceptorStack.isEmpty())
                    {
                        if (interceptedMethodMap == null)
                        {
                            // lazy initialisation, because creating a WeakHashMap is expensive!
                            interceptedMethodMap = new ConcurrentHashMap<Method, List<InterceptorData>>();
                        }
                        
                        if (decoratorDelegateHandler != null)
                        {
                            // We have interceptors and decorators, Our delegateHandler will need to be wrapped in an interceptor
                            WebBeansDecoratorInterceptor lastInterceptor = new WebBeansDecoratorInterceptor(decoratorDelegateHandler, instance);
                            decoratorInterceptorDataImpl = new InterceptorDataImpl(true, lastInterceptor, webBeansContext);
                            decoratorInterceptorDataImpl.setDefinedInInterceptorClass(true);
                            decoratorInterceptorDataImpl.setAroundInvoke(
                                    webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethod(lastInterceptor.getClass(),
                                            "invokeDecorators",
                                            new Class[] {InvocationContext.class}));
                        }

                        List<InterceptorData> interceptorMethods = interceptedMethodMap.get(method);
                        if (interceptorMethods == null)
                        {
                            //Holds filtered interceptor stack
                            List<InterceptorData> filteredInterceptorStack = new ArrayList<InterceptorData>();
                            for (InterceptorData interceptData : interceptorStack)
                            {
                                if (interceptData.getAroundInvoke() !=null)
                                {
                                    filteredInterceptorStack.add(interceptData);
                                }
                            }
        
                            // Filter both EJB and WebBeans interceptors
                            InterceptorUtil interceptorUtil = webBeansContext.getInterceptorUtil();
                            interceptorUtil.filterCommonInterceptorStackList(filteredInterceptorStack, method);
                            interceptorUtil.filterOverridenAroundInvokeInterceptor(bean.getBeanClass(), filteredInterceptorStack);
                            interceptedMethodMap.put(method, filteredInterceptorStack);
                            interceptorMethods = filteredInterceptorStack;
                        }
                        
                        if (decoratorInterceptorDataImpl != null)
                        {
                            // created an intereceptor to run our decorators, add it to the calculated stack
                            interceptorMethods = new ArrayList<InterceptorData>(interceptorMethods);
                            interceptorMethods.add(decoratorInterceptorDataImpl);
                        }

                        // Call Around Invokes
                        if (!interceptorMethods.isEmpty())
                        {
                            return callAroundInvokes(method, arguments, interceptorMethods);
                        }
                    }
                    
                    // If there are Decorators, allow the delegate handler to
                    // manage the stack
                    if (decoratorDelegateHandler != null)
                    {
                        return decoratorDelegateHandler.invoke(instance, method, arguments);
                    }
                }

                setNotInterceptedOrDecoratedMethod(method);
            }

            
            //If here call actual method            
            //If not interceptor or decorator calls
            //Do normal calling
            if (!method.isAccessible())
            {
                webBeansContext.getSecurityService().doPrivilegedSetAccessible(method, true);
            }

            result = method.invoke(instance, arguments);
        }
        catch (InvocationTargetException e)
        {
            Throwable target = e.getCause();
            
            //Look for target exception
            if (target instanceof Exception)
            {
                throw (Exception) target;
            }
            else
            {
                throw e;
            }
        }

        return result;
    }

    private synchronized void resolveDecoratorDelegateHandler(Object instance, CreationalContextImpl<?> ownerCreationalContext,
                                                              InjectionTargetBean<?> injectionTarget)
            throws Exception
    {
        if (decoratorDelegateHandler == null)
        {
            final DelegateHandler newDelegateHandler = new DelegateHandler(bean);
            final ProxyFactory proxyFactory = webBeansContext.getProxyFactory();

            final Object delegate = proxyFactory.createDecoratorDelegate(bean, newDelegateHandler);

            // Gets component decorator stack
            List<Object> decorators = WebBeansDecoratorConfig.getDecoratorStack(injectionTarget, instance, delegate, ownerCreationalContext);
            //Sets decorator stack of delegate
            newDelegateHandler.setDecorators(decorators);
            decoratorDelegateHandler = newDelegateHandler;
        }
    }

    /**
     * Call around invoke method of the given bean on
     * calling interceptedMethod.
     * @param interceptedMethod intercepted bean method
     * @param arguments method actual arguments
     * @param stack interceptor stack
     * @return return of method
     * @throws Exception for any exception
     */
    protected abstract Object callAroundInvokes(Method interceptedMethod, Object[] arguments, List<InterceptorData> stack) throws Exception;
    
    /**
     * 
     * @return bean manager
     */
    protected BeanManagerImpl getBeanManager()
    {
        return webBeansContext.getBeanManagerImpl();
    }
                
    /**
     * Write to stream.
     * @param s stream
     * @throws IOException
     */
    private  void writeObject(ObjectOutputStream s) throws IOException
    {
        s.writeLong(serialVersionUID);
        // we have to write the ids for all beans, not only PassivationCapable
        // since this gets serialized along with the Bean proxy.
        String passivationId = bean.getId();
        if (passivationId!= null)
        {
            s.writeObject(passivationId);
        }
        else
        {
            s.writeObject(null);
            logger.log(Level.WARNING, OWBLogConst.WARN_0010, bean);
        }
    }
    
    /**
     * Read from stream.
     * @param s stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private  void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
    {
        if(s.readLong() == serialVersionUID)
        {
            webBeansContext = WebBeansContext.currentInstance();
            String passivationId = (String) s.readObject();
            if (passivationId != null)
            {
                bean = (OwbBean<?>) webBeansContext.getBeanManagerImpl().getPassivationCapableBean(passivationId);
            }
        }
        else
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0011, bean);
        }
    }

}
