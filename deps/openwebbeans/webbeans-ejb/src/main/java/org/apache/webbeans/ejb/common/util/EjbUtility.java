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
package org.apache.webbeans.ejb.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.webbeans.component.InjectionTargetWrapper;
import org.apache.webbeans.component.ProducerFieldBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.component.creation.BeanCreator.MetaDataProvider;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.ejb.common.component.BaseEjbBean;
import org.apache.webbeans.ejb.common.component.EjbBeanCreatorImpl;
import org.apache.webbeans.event.ObserverMethodImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.portable.events.ProcessAnnotatedTypeImpl;
import org.apache.webbeans.portable.events.ProcessInjectionTargetImpl;
import org.apache.webbeans.portable.events.ProcessProducerImpl;
import org.apache.webbeans.portable.events.ProcessSessionBeanImpl;
import org.apache.webbeans.portable.events.generics.GProcessSessionBean;
import org.apache.webbeans.util.WebBeansUtil;

@SuppressWarnings("unchecked")
public final class EjbUtility
{
    private EjbUtility()
    {
        
    }
        
    public static <T> void fireEvents(Class<T> clazz, BaseEjbBean<T> ejbBean,ProcessAnnotatedType<T> event)
    {
        WebBeansContext webBeansContext = ejbBean.getWebBeansContext();
        BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();
        AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();

        AnnotatedType<T> annotatedType = annotatedElementFactory.newAnnotatedType(clazz);
        
        //Fires ProcessAnnotatedType
        ProcessAnnotatedTypeImpl<T> processAnnotatedEvent = (ProcessAnnotatedTypeImpl<T>)event;             
        EjbBeanCreatorImpl<T> ejbBeanCreator = new EjbBeanCreatorImpl<T>(ejbBean);
        ejbBeanCreator.checkCreateConditions();
        
        if(processAnnotatedEvent.isVeto())
        {
            return;
        }
        
        if(processAnnotatedEvent.isModifiedAnnotatedType())
        {
            ejbBeanCreator.setMetaDataProvider(MetaDataProvider.THIRDPARTY);
            ejbBeanCreator.setAnnotatedType(annotatedType);
        }
        
        //Define meta-data
        ejbBeanCreator.defineSerializable();
        ejbBeanCreator.defineStereoTypes();
        ejbBeanCreator.defineApiType();
        ejbBeanCreator.defineScopeType("Session Bean implementation class : " + clazz.getName() + " stereotypes must declare same @ScopeType annotations", false);
        ejbBeanCreator.defineQualifier();
        ejbBeanCreator.defineName(WebBeansUtil.getManagedBeanDefaultName(clazz.getSimpleName()));            
        Set<ProducerMethodBean<?>> producerMethodBeans = ejbBeanCreator.defineProducerMethods();        
        checkProducerMethods(producerMethodBeans, ejbBean);
        Set<ProducerFieldBean<?>> producerFieldBeans = ejbBeanCreator.defineProducerFields();           
        ejbBeanCreator.defineInjectedFields();
        ejbBeanCreator.defineInjectedMethods();
        Set<ObserverMethod<?>> observerMethods = ejbBeanCreator.defineObserverMethods();        
        
        //Fires ProcessInjectionTarget
        ProcessInjectionTargetImpl<T> processInjectionTargetEvent =
            webBeansContext.getWebBeansUtil().fireProcessInjectionTargetEvent(ejbBean);
        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by ProcessInjectionTarget event observers. Look at logs for further details");
        //Put final InjectionTarget instance
        manager.putInjectionTargetWrapper(ejbBean, new InjectionTargetWrapper(processInjectionTargetEvent.getInjectionTarget()));
        
        Map<ProducerMethodBean<?>,AnnotatedMethod<?>> annotatedMethods = new HashMap<ProducerMethodBean<?>, AnnotatedMethod<?>>(); 
        for(ProducerMethodBean<?> producerMethod : producerMethodBeans)
        {
            AnnotatedMethod<?> method = annotatedElementFactory.newAnnotatedMethod(producerMethod.getCreatorMethod(), annotatedType);
            ProcessProducerImpl<?, ?> producerEvent =
                webBeansContext.getWebBeansUtil().fireProcessProducerEventForMethod(producerMethod,
                                                                                                   method);
            webBeansContext.getWebBeansUtil().inspectErrorStack(
                "There are errors that are added by ProcessProducer event observers for ProducerMethods. Look at logs for further details");

            annotatedMethods.put(producerMethod, method);
            manager.putInjectionTargetWrapper(producerMethod, new InjectionTargetWrapper(producerEvent.getProducer()));
        }
        
        Map<ProducerFieldBean<?>,AnnotatedField<?>> annotatedFields = new HashMap<ProducerFieldBean<?>, AnnotatedField<?>>();
        for(ProducerFieldBean<?> producerField : producerFieldBeans)
        {
            AnnotatedField<?> field = annotatedElementFactory.newAnnotatedField(producerField.getCreatorField(), annotatedType);
            ProcessProducerImpl<?, ?> producerEvent =
                webBeansContext.getWebBeansUtil().fireProcessProducerEventForField(producerField, field);
            webBeansContext.getWebBeansUtil().inspectErrorStack(
                "There are errors that are added by ProcessProducer event observers for ProducerFields. Look at logs for further details");

            annotatedFields.put(producerField, field);
            manager.putInjectionTargetWrapper(producerField, new InjectionTargetWrapper(producerEvent.getProducer()));
        }
        
        Map<ObserverMethod<?>,AnnotatedMethod<?>> observerMethodsMap = new HashMap<ObserverMethod<?>, AnnotatedMethod<?>>(); 
        for(ObserverMethod<?> observerMethod : observerMethods)
        {
            ObserverMethodImpl<?> impl = (ObserverMethodImpl<?>)observerMethod;
            AnnotatedMethod<?> method = annotatedElementFactory.newAnnotatedMethod(impl.getObserverMethod(), annotatedType);
            
            observerMethodsMap.put(observerMethod, method);
        }        

        //Fires ProcessManagedBean
        ProcessSessionBeanImpl<T> processBeanEvent = new GProcessSessionBean((Bean<Object>)ejbBean,annotatedType,ejbBean.getEjbName(),ejbBean.getEjbType());
        webBeansContext.getBeanManagerImpl().fireEvent(processBeanEvent, new Annotation[0]);
        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by ProcessSessionBean event observers for managed beans. Look at logs for further details");

        //Fires ProcessProducerMethod
        webBeansContext.getWebBeansUtil().fireProcessProducerMethodBeanEvent(annotatedMethods,
                                                                                            annotatedType);
        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by ProcessProducerMethod event observers for producer method beans. Look at logs for further details");

        //Fires ProcessProducerField
        webBeansContext.getWebBeansUtil().fireProcessProducerFieldBeanEvent(annotatedFields);
        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by ProcessProducerField event observers for producer field beans. Look at logs for further details");

        //Fire ObservableMethods
        webBeansContext.getWebBeansUtil().fireProcessObservableMethodBeanEvent(observerMethodsMap);
        webBeansContext.getWebBeansUtil().inspectErrorStack(
            "There are errors that are added by ProcessObserverMethod event observers for observer methods. Look at logs for further details");

        manager.addBean(ejbBean);
        
        // Let the plugin handle adding the new bean instance as it knows more about its EJB Bean
        
        manager.getBeans().addAll(producerMethodBeans);
        ejbBeanCreator.defineDisposalMethods();
        manager.getBeans().addAll(producerFieldBeans);
    }
    
    public static <T> void defineSpecializedData(Class<T> clazz, BaseEjbBean<T> ejbBean)
    {
        final String message = "There are errors that are added by %s event observers for %s. Look at logs for further details";

        final WebBeansContext webBeansContext = ejbBean.getWebBeansContext();
        final BeanManagerImpl manager = webBeansContext.getBeanManagerImpl();

        final AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();

        final AnnotatedType<T> annotatedType = annotatedElementFactory.newAnnotatedType(clazz);

        final DefinitionUtil util = webBeansContext.getDefinitionUtil();

        final Set<ProducerMethodBean<?>> producerMethodBeans = util.defineProducerMethods(ejbBean, clazz);

        final Set<ProducerFieldBean<?>> producerFieldBeans = util.defineProducerFields(ejbBean, clazz);

        checkProducerMethods(producerMethodBeans, ejbBean);

        // PRODUCER METHODS
        Map<ProducerMethodBean<?>, AnnotatedMethod<?>> annotatedMethods = new HashMap<ProducerMethodBean<?>, AnnotatedMethod<?>>();
        for(ProducerMethodBean<?> producerMethod : producerMethodBeans)
        {
            AnnotatedMethod<?> method = annotatedElementFactory.newAnnotatedMethod(producerMethod.getCreatorMethod(), annotatedType);

            ProcessProducerImpl<?, ?> producerEvent = webBeansContext.getWebBeansUtil().fireProcessProducerEventForMethod(producerMethod, method);

            webBeansContext.getWebBeansUtil().inspectErrorStack(String.format(message, "ProcessProducer", "ProducerMethods"));

            annotatedMethods.put(producerMethod, method);
            manager.putInjectionTargetWrapper(producerMethod, new InjectionTargetWrapper(producerEvent.getProducer()));
        }

        // PRODUCER FIELDS
        Map<ProducerFieldBean<?>, AnnotatedField<?>> annotatedFields = new HashMap<ProducerFieldBean<?>, AnnotatedField<?>>();
        for(ProducerFieldBean<?> producerField : producerFieldBeans)
        {
            AnnotatedField<?> field = annotatedElementFactory.newAnnotatedField(producerField.getCreatorField(), annotatedType);

            ProcessProducerImpl<?, ?> producerEvent = webBeansContext.getWebBeansUtil().fireProcessProducerEventForField(producerField, field);

            webBeansContext.getWebBeansUtil().inspectErrorStack(String.format(message, "ProcessProducer", "ProducerFields"));

            annotatedFields.put(producerField, field);
            manager.putInjectionTargetWrapper(producerField, new InjectionTargetWrapper(producerEvent.getProducer()));
        }

        //Fires ProcessProducerMethod
        webBeansContext.getWebBeansUtil().fireProcessProducerMethodBeanEvent(annotatedMethods, annotatedType);
        webBeansContext.getWebBeansUtil().inspectErrorStack(String.format(message, "ProcessProducerMethod", "producer method beans"));

        //Fires ProcessProducerField
        webBeansContext.getWebBeansUtil().fireProcessProducerFieldBeanEvent(annotatedFields);
        webBeansContext.getWebBeansUtil().inspectErrorStack(String.format(message, "ProcessProducerField", "producer field beans"));

        // Let the plugin handle adding the new bean instance as it knows more about its EJB Bean

        manager.getBeans().addAll(producerMethodBeans);
        manager.getBeans().addAll(producerFieldBeans);

        util.defineDisposalMethods(ejbBean, clazz);

    }

    private static void checkProducerMethods(Set<ProducerMethodBean<?>> producerMethodBeans, BaseEjbBean<?> bean)
    {
        for(ProducerMethodBean<?> producerMethodBean : producerMethodBeans)
        {
            Method producerMethod = producerMethodBean.getCreatorMethod();
            if(!Modifier.isStatic(producerMethod.getModifiers()))
            {
                if(!isBusinessMethod(producerMethod, bean))
                {
                    throw new WebBeansConfigurationException("Producer Method Bean must be business method of session bean : " + bean);
                }
            }
        }        
    }
    
    /**
     * Check if a given Method is a business method of the given enterprise bean.
     * 
     * FIXME: While remote interfaces aren't part of a 299 beans types, method 
     * invocations of non-contextual references (@EJB) to remote interfaces are 
     * business method invocations nonetheless.  Not presently accounted for here.
     * 
     * @param method the method of interest
     * @param bean the Contextual
     * @return true if the Method could be a business method invocation
     */
    public static boolean isBusinessMethod(Method method, BaseEjbBean<?> bean)
    {
        List<Class<?>> businessLocals = bean.getBusinessLocalInterfaces();
        for(Class<?> clz : businessLocals)
        {
            try
            {
                clz.getMethod(method.getName(), method.getParameterTypes());
                
                return true;
            }
            catch (SecurityException e)
            {
                throw new WebBeansException("Security exception",e);
            }
            catch (NoSuchMethodException e)
            {
                continue;
            }
        }
        
        return false;
    }
}
