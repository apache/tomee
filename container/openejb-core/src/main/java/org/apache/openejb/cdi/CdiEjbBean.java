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
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;

import javax.ejb.Remove;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.SessionBeanType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CdiEjbBean<T> extends BaseEjbBean<T> {
    private final BeanContext beanContext;

    public CdiEjbBean(BeanContext beanContext) {
        super(beanContext.getBeanClass(), toSessionType(beanContext.getComponentType()), WebBeansContext.getInstance());
        this.beanContext = beanContext;
    }

    public BeanContext getBeanContext() {
        return this.beanContext;
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
    private final List<Method> findRemove(Class beanClass, Class beanInterface) {
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

    @Override
    public String getName() {
        return beanContext.getEjbName();
    }
}