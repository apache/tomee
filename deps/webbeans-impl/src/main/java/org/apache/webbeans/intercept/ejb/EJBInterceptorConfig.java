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
package org.apache.webbeans.intercept.ejb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptors;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

/**
 * Configures the EJB related interceptors.
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public final class EJBInterceptorConfig
{

    private final WebBeansContext webBeansContext;

    public EJBInterceptorConfig(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    /**
     * Configures the given class for applicable interceptors.
     *
     * @param annotatedType to configure interceptors for
     * @param interceptorStack to fill
     */
    public void configure(AnnotatedType<?> annotatedType, List<InterceptorData> interceptorStack)
    {
        Asserts.assertNotNull(annotatedType);


        Interceptors incs = annotatedType.getAnnotation(Interceptors.class);
        if (incs != null)
        {
            Class<?>[] interceptorClasses = incs.value();

            for (Class<?> intClass : interceptorClasses)
            {
                configureInterceptorAnnots(intClass, interceptorStack, false, null);
            }

        }
        configureBeanAnnots(annotatedType, interceptorStack);

        Class clazz = annotatedType.getJavaClass();
        webBeansContext.getInterceptorUtil().filterOverridenLifecycleInterceptor(clazz, interceptorStack);
    }

    /**
     * Configure {@link Interceptors} on bean class.
     * @param clazz bean class
     * @param stack interceptor stack of bean
     * @param isMethod if interceptor definition is on the bean
     * @param m if isMethod true, then it is intercepted method
     */
    private void configureInterceptorAnnots(Class<?> clazz, List<InterceptorData> stack, boolean isMethod, Method m)
    {
        // 1- Look interceptor class super class
        // 2- Look interceptor class
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class))
        {
            configureInterceptorAnnots(superClass, stack, false, null);
        }

        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, AroundInvoke.class,
                                                                      true, isMethod, stack, m, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, AroundTimeout.class,
                                                                      true, isMethod, stack, m, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, PostConstruct.class,
                                                                      true, isMethod, stack, m, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, PreDestroy.class,
                                                                      true, isMethod, stack, m, false);

    }

    /**
     * Configure bean class defined interceptors.
     * @param annotatedType bean class
     * @param stack interceptor stack
     */
    private void configureBeanAnnots(AnnotatedType annotatedType, List<InterceptorData> stack)
    {
        // 1- Look method intercepor class annotations
        // 2- Look super class around invoke
        // 3- Look bean around invoke

        // 1-
        Set<AnnotatedMethod<?>> annotatedMethods = annotatedType.getMethods();

        for (AnnotatedMethod<?> annotatedMethod : annotatedMethods)
        {
            Interceptors incs = annotatedMethod.getAnnotation(Interceptors.class);
            if (incs != null)
            {
                Method method = annotatedMethod.getJavaMember();
                Class<?>[] intClasses = incs.value();

                for (Class<?> intClass : intClasses)
                {
                    configureInterceptorAnnots(intClass, stack, true, method);
                }

            }
        }

        Class clazz = annotatedType.getJavaClass();
        // 2- Super clazz
        List<Class<?>> listSuperClazz = ClassUtil.getSuperClasses(clazz, new ArrayList<Class<?>>());
        configureBeanSuperClassAnnots(listSuperClazz, stack);

        // 3- Bean itself
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, AroundInvoke.class,
                                                                      false, false, stack, null, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, AroundTimeout.class,
                                                                      false, false, stack, null, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, PostConstruct.class,
                                                                      false, false, stack, null, false);
        webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz, PreDestroy.class,
                                                                      false, false, stack, null, false);

    }

    /**
     * Configures super classes interceptors.
     * @param list super classes
     * @param stack interceptor stack
     */
    private void configureBeanSuperClassAnnots(List<Class<?>> list, List<InterceptorData> stack)
    {
        int i = list.size();

        for (int j = i - 1; j >= 0; j--)
        {
            Class<?> clazz = list.get(j);
            if (!clazz.equals(Object.class))
            {
                webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz,
                                                                              AroundInvoke.class, false,
                                                                              false, stack, null, false);
                webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz,
                                                                              AroundTimeout.class, false,
                                                                              false, stack, null, false);
                webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz,
                                                                              PostConstruct.class, false,
                                                                              false, stack, null, false);
                webBeansContext.getWebBeansUtil().configureInterceptorMethods(null, clazz,
                                                                              PreDestroy.class, false,
                                                                              false, stack, null, false);
            }
        }
    }

}
