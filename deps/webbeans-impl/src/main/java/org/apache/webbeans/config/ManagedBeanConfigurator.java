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
package org.apache.webbeans.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Set;

import javax.decorator.Decorator;
import javax.interceptor.Interceptor;

import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.ProducerFieldBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.portable.creation.InjectionTargetProducer;
import org.apache.webbeans.portable.creation.ProducerBeansProducer;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Configures Simple WebBeans Component.
 * <p>
 * Contains useful static methods for creating Simple WebBeans Components.
 * </p>
 *
 * @version $Rev: 1410305 $ $Date: 2012-11-16 12:10:00 +0100 (ven., 16 nov. 2012) $
 */
public final class ManagedBeanConfigurator
{

    private final WebBeansContext webBeansContext;

    public ManagedBeanConfigurator(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    public void checkManagedBeanCondition(Class<?> clazz) throws WebBeansConfigurationException
    {
        int modifier = clazz.getModifiers();

        if (AnnotationUtil.hasClassAnnotation(clazz, Decorator.class) && AnnotationUtil.hasClassAnnotation(clazz, Interceptor.class))
        {
            throw new WebBeansConfigurationException("ManagedBean implementation class : " + clazz.getName()
                                                     + " may not annotated with both @Interceptor and @Decorator annotation");
        }

        if (!AnnotationUtil.hasClassAnnotation(clazz, Decorator.class) && !AnnotationUtil.hasClassAnnotation(clazz, Interceptor.class))
        {
            webBeansContext.getInterceptorUtil().checkSimpleWebBeansInterceptorConditions(clazz);
        }

        if (ClassUtil.isInterface(modifier))
        {
            throw new WebBeansConfigurationException("ManagedBean implementation class : " + clazz.getName() + " may not _defined as interface");
        }
    }

    /**
     * Returns true if this class can be candidate for simple web bean, false otherwise.
     *
     * @param clazz implementation class
     * @return true if this class can be candidate for simple web bean
     * @throws WebBeansConfigurationException if any configuration exception occurs
     */
    public boolean isManagedBean(Class<?> clazz) throws WebBeansConfigurationException
    {
        try
        {
            webBeansContext.getWebBeansUtil().isManagedBeanClass(clazz);

        }
        catch (WebBeansConfigurationException e)
        {
            return false;
        }

        return true;
    }

    /**
     * Returns the newly created Simple WebBean Component.
     *
     * @param clazz Simple WebBean Component implementation class
     * @return the newly created Simple WebBean Component
     * @throws WebBeansConfigurationException if any configuration exception occurs
     * @deprecated
     */
    @SuppressWarnings("unchecked")
    public <T> ManagedBean<T> define(Class<T> clazz, WebBeansType type) throws WebBeansConfigurationException
    {
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        DefinitionUtil definitionUtil = webBeansContext.getDefinitionUtil();


        int modifier = clazz.getModifiers();

        if (AnnotationUtil.hasClassAnnotation(clazz, Decorator.class) && AnnotationUtil.hasClassAnnotation(clazz, Interceptor.class))
        {
            throw new WebBeansConfigurationException("ManagedBean implementation class : " + clazz.getName()
                                                     + " may not annotated with both @Interceptor and @Decorator annotation");
        }

        if (!AnnotationUtil.hasClassAnnotation(clazz, Decorator.class) && !AnnotationUtil.hasClassAnnotation(clazz, Interceptor.class))
        {
            webBeansContext.getInterceptorUtil().checkSimpleWebBeansInterceptorConditions(clazz);
        }

        if (ClassUtil.isInterface(modifier))
        {
            throw new WebBeansConfigurationException("ManagedBean implementation class : " + clazz.getName() + " may not _defined as interface");
        }

        ManagedBean<T> component = new ManagedBean<T>(clazz, type, webBeansContext);
        manager.putInjectionTargetWrapper(component, new InjectionTargetWrapper(new InjectionTargetProducer(component)));

        webBeansContext.getWebBeansUtil().setInjectionTargetBeanEnableFlag(component);

        definitionUtil.defineSerializable(component);
        definitionUtil.defineStereoTypes(component, clazz.getDeclaredAnnotations());

        Annotation[] clazzAnns = clazz.getDeclaredAnnotations();

        definitionUtil.defineApiTypes(component, clazz);
        definitionUtil.defineScopeType(component, clazzAnns, "Simple WebBean Component implementation class : " + clazz.getName()
                                                             + " stereotypes must declare same @Scope annotations", false);
        // we fully initialize the bean in this case.
        component.setFullInit(true);

        WebBeansUtil.checkGenericType(component);
        definitionUtil.defineName(component, clazzAnns, WebBeansUtil.getManagedBeanDefaultName(clazz.getSimpleName()));
        definitionUtil.defineQualifiers(component, clazzAnns);

        Constructor<T> constructor = webBeansContext.getWebBeansUtil().defineConstructor(clazz);
        component.setConstructor(constructor);
        definitionUtil.addConstructorInjectionPointMetaData(component, constructor);

        //Dropped from the speicification
        //WebBeansUtil.checkSteroTypeRequirements(component, clazz.getDeclaredAnnotations(), "Simple WebBean Component implementation class : " + clazz.getName());

        Set<ProducerMethodBean<?>> producerMethods = definitionUtil.defineProducerMethods(component);
        for (ProducerMethodBean<?> producerMethod : producerMethods)
        {
            // add them one after the other to enable serialization handling et al
            manager.addBean(producerMethod);
            manager.putInjectionTargetWrapper(producerMethod, new InjectionTargetWrapper(new ProducerBeansProducer(producerMethod)));
        }

        Set<ProducerFieldBean<?>> producerFields = definitionUtil.defineProducerFields(component);
        for (ProducerFieldBean<?> producerField : producerFields)
        {
            // add them one after the other to enable serialization handling et al
            manager.addBean(producerField);
            manager.putInjectionTargetWrapper(producerField, new InjectionTargetWrapper(new ProducerBeansProducer(producerField)));
        }


        definitionUtil.defineDisposalMethods(component);
        definitionUtil.defineInjectedFields(component);
        definitionUtil.defineInjectedMethods(component);
        definitionUtil.defineObserverMethods(component, clazz);

        return component;
    }
}