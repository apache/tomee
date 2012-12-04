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
package org.apache.webbeans.proxy;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;

import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.decorator.DelegateHandler;
import org.apache.webbeans.decorator.WebBeansDecorator;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.intercept.DependentScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.intercept.InterceptorHandler;
import org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.webbeans.WebBeansInterceptor;
import org.apache.webbeans.proxy.javassist.JavassistFactory;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

public final class ProxyFactory
{
    private ConcurrentMap<OwbBean<?>, Class<?>> buildInBeanProxyClasses = new ConcurrentHashMap<OwbBean<?>, Class<?>>();
    private ConcurrentMap<OwbBean<?>, Class<?>> normalScopedBeanProxyClasses = new ConcurrentHashMap<OwbBean<?>, Class<?>>();    
    private ConcurrentMap<OwbBean<?>, Class<?>> dependentScopedBeanProxyClasses = new ConcurrentHashMap<OwbBean<?>, Class<?>>();    
    private ConcurrentMap<OwbBean<?>, Class<?>> interceptorProxyClasses = new ConcurrentHashMap<OwbBean<?>, Class<?>>();
    private ConcurrentMap<ResourceBean<?, ?>, Class<?>> resourceBeanProxyClasses = new ConcurrentHashMap<ResourceBean<?,?>, Class<?>>();
    // second level map is indexed on local interface
    private ConcurrentMap<OwbBean<?>, ConcurrentMap<Class<?>, Class<?>>> ejbProxyClasses = new ConcurrentHashMap<OwbBean<?>, ConcurrentMap<Class<?>, Class<?>>>();
    private Factory factory;

    public ProxyFactory()
    {
        this(new JavassistFactory());
    }

    public ProxyFactory(Factory factory)
    {
        this.factory = factory;
    }

    /**
     * This map contains all configured special Scope->InterceptorHandler mappings.
     * If no mapping is configured, a {@link org.apache.webbeans.intercept.NormalScopedBeanInterceptorHandler} will get created.
     */
    private Map<String, Class<? extends InterceptorHandler>> interceptorHandlerClasses =
            new ConcurrentHashMap<String, Class<? extends InterceptorHandler>>();

    public void setHandler(Object proxy, MethodHandler handler)
    {
        factory.setHandler(proxy, handler);
    }


    public   Map<OwbBean<?>, Class<?>> getInterceptorProxyClasses()
    {
        return interceptorProxyClasses;
    }

    public void clear()
    {
        normalScopedBeanProxyClasses.clear();
        dependentScopedBeanProxyClasses.clear();
        interceptorProxyClasses.clear();
        ejbProxyClasses.clear();
    }
    /**
     * Provides the proxy for the given bean and interface, if defined
     * 
     * @param bean the contextual representing the EJB
     * @param iface the injected business local interface
     * @return the proxy Class if one has been defined, else null
     */
    public Class<?> getEjbBeanProxyClass(OwbBean<?> bean, Class<?> iface)
    {
        Class<?> proxyClass = null;

        ConcurrentMap<Class<?>, Class<?>> typeToProxyClassMap = ejbProxyClasses.get(bean);
        if (typeToProxyClassMap == null)
        {
            typeToProxyClassMap = new ConcurrentHashMap<Class<?>, Class<?>>();
            ConcurrentMap<Class<?>, Class<?>> existingMap = ejbProxyClasses.putIfAbsent(bean, typeToProxyClassMap);
            
            // use the map that beat us, because our new one definitely had no classes in it.
            typeToProxyClassMap = (existingMap != null) ? existingMap : typeToProxyClassMap; 
        }

        proxyClass = typeToProxyClassMap.get(iface);

        if (proxyClass == null)
        {
            Class<?> superClazz = null;
            List<Class<?>> list = new ArrayList<Class<?>>();
            Class<?>[] interfaces = null;
            
            if (iface.isInterface())
            {
                list.add(iface);
            }
            else 
            {
                // @LocalBean no-interface local view requested
                superClazz = iface;
                //Stateless beans with no interface
                //To failover bean instance
                Class<?>[] ifaces = iface.getInterfaces();
                if(ifaces != null && ifaces.length > 0)
                {
                    //check for serializable
                    for(Class<?> temp : ifaces)
                    {
                        if(temp == Serializable.class)
                        {
                            list.add(Serializable.class);
                            break;
                        }
                    }
                }
            }            
            
            interfaces = new Class<?>[list.size()];
            interfaces = list.toArray(interfaces);
            proxyClass = factory.getProxyClass(superClazz, interfaces);
            
            typeToProxyClassMap.putIfAbsent(iface, proxyClass);
            // don't care if we were beaten in updating the iface->proxyclass map
        }

        return proxyClass;
    }
    
    public Object createDecoratorDelegate(OwbBean<?> bean, DelegateHandler newDelegateHandler)
        throws Exception
    {

        Class<?> proxyClass = this.getInterceptorProxyClasses().get(bean);
        if (proxyClass == null)
        {
            proxyClass = createProxyClass(bean);
            this.getInterceptorProxyClasses().put(bean, proxyClass);
        }

        final Object delegate = createProxy(proxyClass);
        setHandler(delegate, newDelegateHandler);
        return delegate;
    }

    public Class<?> getResourceBeanProxyClass(ResourceBean<?, ?> resourceBean)
    {
        try
        {
            Class<?> proxyClass = resourceBeanProxyClasses.get(resourceBean);
            if (proxyClass == null)
            {
                proxyClass = createProxyClass(resourceBean);

                Class<?> oldClazz = resourceBeanProxyClasses.putIfAbsent(resourceBean, proxyClass);
                if (oldClazz != null)
                {
                    return oldClazz;
                }                
            }
            return proxyClass;
        }
        catch (Exception e)
        {
            WebBeansUtil.throwRuntimeExceptions(e);
        }

        return null;
    }


    public  Object createNormalScopedBeanProxy(OwbBean<?> bean, CreationalContext<?> creationalContext)
    {
        Object result = null;
        try
        {
            Class<?> proxyClass = normalScopedBeanProxyClasses.get(bean);
            if (proxyClass == null)
            {
                proxyClass = createProxyClass(bean);
                normalScopedBeanProxyClasses.putIfAbsent(bean, proxyClass);
            }


            result = createProxy(proxyClass);
            
            if (!(bean instanceof WebBeansDecorator<?>) && !(bean instanceof WebBeansInterceptor<?>))
            {
                InterceptorHandler interceptorHandler = createInterceptorHandler(bean, creationalContext);

                setHandler(result, interceptorHandler);
            }
        }
        catch (Exception e)
        {
            WebBeansUtil.throwRuntimeExceptions(e);
        }

        return result;
    }

    private Object createProxy(Class<?> proxyClass)
        throws InstantiationException, IllegalAccessException
    {
        return factory.createProxy(proxyClass);
    }

    private InterceptorHandler createInterceptorHandler(OwbBean<?> bean, CreationalContext<?> creationalContext)
    {
        String scopeClassName = bean.getScope().getName();
        Class<? extends InterceptorHandler> interceptorHandlerClass = null;
        if (!interceptorHandlerClasses.containsKey(scopeClassName))
        {
            String proxyMappingConfigKey = OpenWebBeansConfiguration.PROXY_MAPPING_PREFIX + scopeClassName;
            String className = bean.getWebBeansContext().getOpenWebBeansConfiguration().getProperty(proxyMappingConfigKey);
            if (className != null)
            {
                try
                {
                    interceptorHandlerClass = (Class<? extends InterceptorHandler>) Class.forName(className, true, WebBeansUtil.getCurrentClassLoader());
                }
                catch (ClassNotFoundException e)
                {
                    throw new WebBeansConfigurationException("Configured InterceptorHandler "
                                                             + className
                                                             +" cannot be found",
                                                             e);
                }
            }
            else
            {
                // we need to explicitely store a class because ConcurrentHashMap will throw a NPE if value == null
                interceptorHandlerClass = NormalScopedBeanInterceptorHandler.class;
            }

            interceptorHandlerClasses.put(scopeClassName, interceptorHandlerClass);
        }
        else
        {
            interceptorHandlerClass = interceptorHandlerClasses.get(scopeClassName);
        }

        if (interceptorHandlerClass.equals(NormalScopedBeanInterceptorHandler.class))
        {
            // this is faster that way...
            return new NormalScopedBeanInterceptorHandler(bean, creationalContext);
        }
        else
        {
            try
            {
                Constructor ct = interceptorHandlerClass.getConstructor(OwbBean.class, CreationalContext.class);
                return (InterceptorHandler) ct.newInstance(bean, creationalContext);
            }
            catch (NoSuchMethodException e)
            {
                throw new WebBeansConfigurationException("Configured InterceptorHandler "
                                                         + interceptorHandlerClass.getName()
                                                         +" has the wrong contructor",
                                                         e);
            }
            catch (InvocationTargetException e)
            {
                throw new WebBeansConfigurationException("Configured InterceptorHandler "
                                                         + interceptorHandlerClass.getName()
                                                         +" has the wrong contructor",
                                                         e);
            }
            catch (InstantiationException e)
            {
                throw new WebBeansConfigurationException("Configured InterceptorHandler "
                                                         + interceptorHandlerClass.getName()
                                                         +" has the wrong contructor",
                                                         e);
            }
            catch (IllegalAccessException e)
            {
                throw new WebBeansConfigurationException("Configured InterceptorHandler "
                                                         + interceptorHandlerClass.getName()
                                                         +" has the wrong contructor",
                                                         e);
            }
        }
    }

    public Object createBuildInBeanProxy(OwbBean<?> bean) 
    {
        Object result = null;
        try
        {
            Class<?> proxyClass = buildInBeanProxyClasses.get(bean);
            if (proxyClass == null)
            {
                proxyClass = createProxyClass(bean);
                buildInBeanProxyClasses.putIfAbsent(bean, proxyClass);
            }
            result = createProxy(proxyClass);
        }
        catch (Exception e)
        {
            WebBeansUtil.throwRuntimeExceptions(e);
        }
        return result;
    }

    
    public  Object createDependentScopedBeanProxy(OwbBean<?> bean, Object actualInstance, CreationalContext<?> creastionalContext)
    {

        List<InterceptorData> interceptors =  null;
        List<Decorator<?>> decorators = null;
        InjectionTargetBean<?> injectionTargetBean = null;
        if(bean instanceof InjectionTargetBean<?>)
        {
            injectionTargetBean = (InjectionTargetBean<?>)bean;
            interceptors = injectionTargetBean.getInterceptorStack();
            decorators = injectionTargetBean.getDecoratorStack();
        }
        
        if(interceptors == null && decorators == null)
        {
            return actualInstance;
        }
        
        boolean notInInterceptorClassAndLifecycle = false;
        if(interceptors != null && interceptors.size() > 0)
        {
            Iterator<InterceptorData> its = interceptors.iterator();
            while(its.hasNext())
            {
                InterceptorData id = its.next();
                if(!id.isDefinedInInterceptorClass() && id.isLifecycleInterceptor())
                {
                    continue;
                }
                notInInterceptorClassAndLifecycle = true;
                break;
            }
        }
        
        //No need to return proxy
        if(!notInInterceptorClassAndLifecycle && decorators.isEmpty())
        {
            //Adding this dependent instance into creational context
            //This occurs when no owner of this dependent instance
            if(creastionalContext instanceof CreationalContextImpl)
            {
                //If this creational context is owned by itself, add it
                //For example, getting it directly BeanManager#getReference(bean,creational context)
                CreationalContextImpl<?> ccImpl = (CreationalContextImpl<?>)creastionalContext;
                
                //Non contextual instance --> Bean --> Null
                //See OWBInjector
                if(ccImpl.getBean() != null)
                {
                    if(ccImpl.getBean().equals(bean))
                    {
                        //Owner of the dependent is itself
                        ccImpl.addDependent(actualInstance, bean, actualInstance);
                    }                                
                }
            }
            
            return actualInstance;
        }
        
        try
        {
            Class<?> proxyClass = dependentScopedBeanProxyClasses.get(bean);
            if (proxyClass == null)
            {
                proxyClass = createProxyClass(bean);
                dependentScopedBeanProxyClasses.putIfAbsent(bean, proxyClass);
            }

            Object result = createProxy(proxyClass);
            if (!(bean instanceof WebBeansDecorator<?>) && !(bean instanceof WebBeansInterceptor<?>))
            {
                setHandler(result, new DependentScopedBeanInterceptorHandler(bean, actualInstance, creastionalContext));
            }

            return result;
        }
        catch (Exception e)
        {
            WebBeansUtil.throwRuntimeExceptions(e);
        }

        return null;
    }

    public Class<?> createProxyClass(OwbBean<?> bean)
    {
        final ProxyInfo info = getProxyInfo(bean);
        return factory.getProxyClass(info.getSuperClass(), info.getInterfaces());
    }

    public Class<?> createAbstractDecoratorProxyClass(OwbBean<?> bean)
    {
        return createProxyClass(bean);
    }

    public boolean isProxyInstance(Object o)
    {
        return factory.isProxyInstance(o);
    }

    public Object createProxy(MethodHandler handler, Class<?>[] interfaces)
        throws IllegalAccessException, InstantiationException
    {
        return factory.createProxy(handler, interfaces);
    }

    private static class ProxyInfo
    {
        private final Class<?> superClass;
        private final Class<?>[] interfaces;

        private ProxyInfo(Class<?> superClass, Class<?>[] interfaces)
        {
            this.superClass = superClass;
            this.interfaces = interfaces;
        }

        public Class<?> getSuperClass()
        {
            return superClass;
        }

        public Class<?>[] getInterfaces()
        {
            return interfaces;
        }
    }

    private static ProxyInfo getProxyInfo(Bean<?> bean)
    {
        final Set<Class<?>> interfaceList = new HashSet<Class<?>>();
        Class<?> superClass = null;
        for (Type generic : bean.getTypes())
        {
            Class<?> type = ClassUtil.getClazz(generic);

            if (type.isInterface())
            {
                interfaceList.add(type);
            }

            else if ((superClass == null) || (superClass.isAssignableFrom(type) && type != Object.class))
            {
                superClass = type;
            }

        }
        if (!interfaceList.contains(Serializable.class))
        {
            interfaceList.add(Serializable.class);
        }

        Class<?>[] interfaceArray = new Class<?>[interfaceList.size()];
        interfaceArray = interfaceList.toArray(interfaceArray);

        return new ProxyInfo(superClass, interfaceArray);
    }
}
