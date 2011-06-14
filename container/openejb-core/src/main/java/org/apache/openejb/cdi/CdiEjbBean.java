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
package org.apache.openejb.cdi;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.assembler.classic.ProxyInterfaceResolver;
import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.inject.InjectableConstructor;

import javax.ejb.Remove;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CdiEjbBean<T> extends BaseEjbBean<T> {
    private final BeanContext beanContext;
    private final CreationalContext<T> creationalContext;
    private final Constructor<T> constructor;

    public CdiEjbBean(BeanContext beanContext, WebBeansContext webBeansContext) {
        super(beanContext.getBeanClass(), toSessionType(beanContext.getComponentType()), webBeansContext);
        this.beanContext = beanContext;


        if (beanContext.isLocalbean()) addApiType(beanContext.getBeanClass());

        addApiType(beanContext.getHomeInterface());
        addApiType(beanContext.getLocalHomeInterface());

        for (Class clazz : beanContext.getBusinessLocalInterfaces()) addApiType(clazz);

        beanContext.set(Bean.class, this);

        //TODO correct scope not clear
        setImplScopeType(new DependentScopeLiteral());
        BeanManagerImpl beanManagerImpl = webBeansContext.getBeanManagerImpl();
        creationalContext = beanManagerImpl.createCreationalContext(this);
        constructor = webBeansContext.getWebBeansUtil().defineConstructor(getReturnType());
        webBeansContext.getDefinitionUtil().addConstructorInjectionPointMetaData(this, constructor);
    }

    @Override
    public void addApiType(Class<?> apiType) {
        if (apiType == null) return;

        super.addApiType(apiType);
    }

    public BeanContext getBeanContext() {
        return this.beanContext;
    }

    public T create() {
        InjectableConstructor<T> ic = new InjectableConstructor<T>(constructor, this, creationalContext);

        return ic.doInjection();
    }

    private static SessionBeanType toSessionType(BeanType beanType) {
        switch (beanType) {
        case SINGLETON:
            return SessionBeanType.SINGLETON;
        case STATELESS:
            return SessionBeanType.STATELESS;
        case STATEFUL:
        case MANAGED:
            return SessionBeanType.STATEFUL;
        default:
            throw new IllegalStateException("Unknown Session BeanType " + beanType);
        }
    }

    @Override
    public String getId() {
        return (String) beanContext.getDeploymentID();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getInstance(CreationalContext<T> creationalContext) {

        final List<Class> classes = beanContext.getBusinessLocalInterfaces();
        final Class mainInterface = classes.get(0);

        List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanContext.getBeanClass(), mainInterface, classes);
        BeanContext.BusinessLocalHome home = beanContext.getBusinessLocalHome(interfaces, mainInterface);

        return (T) home.create();
    }

    public String getEjbName() {
        return this.beanContext.getEjbName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Class<?>> getBusinessLocalInterfaces() {
        List<Class<?>> clazzes = new ArrayList<Class<?>>();
        List<Class> cl = this.beanContext.getBusinessLocalInterfaces();

        if (cl != null && !cl.isEmpty()) {
            for (Class<?> c : cl) {
                clazzes.add(c);
            }
        }

        return clazzes;
    }

    @Override
    public List<Method> getRemoveMethods() {
        // Should we delegate to super and merge both?
        return findRemove(beanContext.getBeanClass(), beanContext.getBusinessLocalInterface());
    }

    @SuppressWarnings("unchecked")
    private List<Method> findRemove(Class beanClass, Class beanInterface) {
        List<Method> toReturn = new ArrayList<Method>();

        // Get all the public methods of the bean class and super class
        Method[] methods = beanClass.getMethods();

        // Search for methods annotated with @Remove
        for (Method method : methods) {
            Remove annotation = method.getAnnotation(Remove.class);
            if (annotation != null) {
                // Get the corresponding method into the bean interface
                Method interfaceMethod;
                try {
                    interfaceMethod = beanInterface.getMethod(method.getName(), method
                            .getParameterTypes());

                    toReturn.add(interfaceMethod);
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    // The method can not be into the interface in which case we
                    // don't wonder of
                }
            }
        }

        return toReturn;
    }

}