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

package org.apache.webbeans.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.util.TypeLiteral;
import org.apache.webbeans.annotation.AnyLiteral;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.portable.events.generics.GenericBeanEvent;
import org.apache.webbeans.portable.events.generics.GenericProducerObserverEvent;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ArrayUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.WebBeansUtil;

@SuppressWarnings("unchecked")
public final class NotificationManager
{
    private final static Logger logger = WebBeansLoggerFacade.getLogger(NotificationManager.class);

    private final Map<Type, Set<ObserverMethod<?>>> observers = new ConcurrentHashMap<Type, Set<ObserverMethod<?>>>();
    private final WebBeansContext webBeansContext;

    public NotificationManager(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    public <T> void addObserver(ObserverMethod<T> observer, Type eventType)
    {
        webBeansContext.getAnnotationManager().checkQualifierConditions(observer.getObservedQualifiers());

        Set<ObserverMethod<?>> set = observers.get(eventType);
        if (set == null)
        {
            set = new HashSet<ObserverMethod<?>>();
            observers.put(eventType, set);
        }

        set.add(observer);
    }

    public <T> void addObserver(ObserverMethod<T> observer, TypeLiteral<T> typeLiteral)
    {
        EventUtil.checkEventType(typeLiteral.getRawType());

        addObserver(observer, typeLiteral.getType());
    }

    public <T> Set<ObserverMethod<? super T>> resolveObservers(T event, Annotation... eventQualifiers)
    {
        EventUtil.checkEventBindings(webBeansContext, eventQualifiers);

        Set<Annotation> qualifiers = ArrayUtil.asSet(eventQualifiers);

        Class<T> eventType = (Class<T>) event.getClass();
        Set<ObserverMethod<? super T>> observersMethods = filterByType(event,eventType);

        observersMethods = filterByQualifiers(observersMethods, qualifiers);

        return observersMethods;
    }

    private <T> Set<ObserverMethod<? super T>> filterByType(T event, Class<T> eventType)
    {
        if(WebBeansUtil.isExtensionEventType(eventType))
        {
            return filterByExtensionEventType(event, eventType);
        }
        
        Set<ObserverMethod<? super T>> matching = new HashSet<ObserverMethod<? super T>>();

        Set<Type> types = new HashSet<Type>();
        types.add(eventType);
        
        Type superClazz = eventType.getGenericSuperclass();
        if(superClazz != null)
        {
            types.add(superClazz);    
        }
        
        Type[] genericInts = eventType.getGenericInterfaces();
        
        if(genericInts != null && genericInts.length > 0)
        {
            for(Type genericInt : genericInts)
            {
                types.add(genericInt);
            }            
        }

        Set<Type> keySet = observers.keySet();

        for (Type type : keySet)
        {
            for (Type check : types)
            {
                if (ClassUtil.checkEventTypeAssignability(check, type))
                {
                    Set<ObserverMethod<?>> wrappers = observers.get(type);

                    for (ObserverMethod<?> wrapper : wrappers)
                    {
                        matching.add((ObserverMethod<T>) wrapper);
                    }
                    break;
                }
            }
        }
        return matching;
    }
    
    private <T> Set<ObserverMethod<? super T>> filterByExtensionEventType(T event, Class<T> eventType)
    {
        Set<ObserverMethod<? super T>> matching = new HashSet<ObserverMethod<? super T>>();        
        Set<Type> keySet = observers.keySet();
        for (Type type : keySet)
        {
            Class<?> beanClass = null;
            Class<?> observerClass = ClassUtil.getClazz(type);
            
            if(observerClass != null)
            {
                if(observerClass.isAssignableFrom(eventType))
                {
                    //ProcessBean,ProcessAnnotateType, ProcessInjectionTarget
                    if(WebBeansUtil.isExtensionBeanEventType(eventType))
                    {
                        if(WebBeansUtil.isDefaultExtensionBeanEventType(observerClass))
                        {                
                            GenericBeanEvent genericBeanEvent = (GenericBeanEvent)event;
                            beanClass = genericBeanEvent.getBeanClassFor(observerClass);
                            
                            if(ClassUtil.isParametrizedType(type))
                            {
                                addToMathingWithParametrizedForBeans(type,beanClass,matching);
                            }
                            else
                            {
                                addToMatching(type, matching);
                            }
                        }
                    }
                    //ProcessProducer, ProcessProducerMethod, ProcessProducerField,ProcessObserverMEthod
                    else if(WebBeansUtil.isExtensionProducerOrObserverEventType(eventType))
                    {
                        GenericProducerObserverEvent genericBeanEvent = (GenericProducerObserverEvent)event;
                        beanClass = genericBeanEvent.getBeanClass();
                        Class<?> producerOrObserverReturnClass = genericBeanEvent.getProducerOrObserverType();

                        if(WebBeansUtil.isDefaultExtensionProducerOrObserverEventType(observerClass))
                        {   
                            boolean processProducerEvent = false;
                            if(observerClass.equals(ProcessProducer.class))
                            {
                                processProducerEvent = true;
                            }
                            
                            if(ClassUtil.isParametrizedType(type))
                            {
                                addToMatchingWithParametrizedForProducers(processProducerEvent,type, beanClass, producerOrObserverReturnClass, matching);
                            }
                            else
                            {
                                addToMatching(type, matching);
                            }
                        }
                        else if(observerClass.isAssignableFrom(eventType))
                        {
                            if(ClassUtil.isParametrizedType(type))
                            {
                                addToMathingWithParametrizedForBeans(type,beanClass,matching);
                            }
                            else
                            {
                                addToMatching(type, matching);
                            }                            
                        }
                    }
                    //BeforeBeanDiscovery,AfterBeanDiscovery,AfterDeploymentValidation
                    //BeforeShutDown Events
                    else
                    {
                        if(observerClass.isAssignableFrom(eventType))
                        {                
                            addToMatching(type, matching);
                        }
                    }                
                }                            
            }            
        }            
        
        return matching;        
    }
    
    /**
     * Returns true if fired event class is assignable with 
     * given observer type argument.
     * @param beanClass fired event class.
     * @param observerTypeActualArg actual type argument, 
     * such as in case ProcessProducerField&lt;Book&gt; is Book.class
     * @return true if fired event class is assignable with 
     * given observer type argument.
     */
    private boolean checkEventTypeParameterForExtensions(Class<?> beanClass, Type observerTypeActualArg)
    {
        if(ClassUtil.isTypeVariable(observerTypeActualArg))
        {
            TypeVariable<?> tv = (TypeVariable<?>)observerTypeActualArg;
            Type tvBound = tv.getBounds()[0];
            
            if(tvBound instanceof Class)
            {
                Class<?> clazzTvBound = (Class<?>)tvBound;
                
                if(clazzTvBound.isAssignableFrom(beanClass))
                {
                    return true;
                }                    
            }            

        }
        else if(ClassUtil.isWildCardType(observerTypeActualArg))
        {
            return ClassUtil.checkRequiredTypeIsWildCard(beanClass, observerTypeActualArg);
        }
        else if(observerTypeActualArg instanceof Class)
        {
            Class<?> observerClass = (Class<?>)observerTypeActualArg;
            if(observerClass.isAssignableFrom(beanClass))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private <T> void addToMatching(Type type, Set<ObserverMethod<? super T>> matching)
    {
        Set<ObserverMethod<?>> wrappers = observers.get(type);

        for (ObserverMethod<?> wrapper : wrappers)
        {
            matching.add((ObserverMethod<T>) wrapper);
        }        
    }
    
    private <T> void addToMathingWithParametrizedForBeans(Type type, Class<?> beanClass, Set<ObserverMethod<? super T>> matching )
    {
        ParameterizedType pt = (ParameterizedType)type;
        Type[] actualArgs = pt.getActualTypeArguments();
        
        if(actualArgs.length == 0)
        {
            Class<?> rawType = (Class<?>)pt.getRawType();
            if(rawType.isAssignableFrom(beanClass))
            {
                addToMatching(type, matching);
            }
        }
        else
        {
            if(checkEventTypeParameterForExtensions(beanClass, actualArgs[0]))
            {
                addToMatching(type, matching);   
            }
        }
        
    }
    
    /**
     * Add to matching.
     * @param <T> generic observer method parameter type 
     * fired event because of observer method or not
     * @param type one of observer method parameter base type
     * @param beanClass observer method owner bean class
     * @param producerOrObserverReturnClass observer even normal class
     * @param matching set of observer method that match the given type
     */
    private <T> void addToMatchingWithParametrizedForProducers(boolean processProducer, Type type, Class<?> beanClass,
                                                              Class<?> producerOrObserverReturnClass, Set<ObserverMethod<? super T>> matching )
    {
        ParameterizedType pt = (ParameterizedType)type;
        Type[] actualArgs = pt.getActualTypeArguments();
        
        if(actualArgs.length == 0)
        {
            Class<?> rawType = (Class<?>)pt.getRawType();
            if(rawType.isAssignableFrom(beanClass))
            {
                addToMatching(type, matching);
            }
        }
        else
        {   
            //Bean class argument
            //For observer related event, observer owner bean class.
            Type beanClassArg = actualArgs[1];
            
            //Event payload
            Type returnClassArg = actualArgs[0];
            
            //For ProcessProducer<BeanClass, Event Class>
            if(processProducer)
            {
                beanClassArg = actualArgs[0];
                returnClassArg = actualArgs[1];
            }
                        
            if(checkEventTypeParameterForExtensions(beanClass, beanClassArg) && 
                    checkEventTypeParameterForExtensions(producerOrObserverReturnClass, returnClassArg))
            {
                addToMatching(type, matching);   
            }
        }
        
    }    

    /**
     * filter out all {@code ObserverMethod}s which do not fit the given
     * qualifiers.
     */
    private <T> Set<ObserverMethod<? super T>> filterByQualifiers(Set<ObserverMethod<? super T>> observers, Set<Annotation> eventQualifiers)
    {
        eventQualifiers.add(new AnyLiteral());
                
        Set<ObserverMethod<? super T>> matching = new HashSet<ObserverMethod<? super T>>();

        search: for (ObserverMethod<? super T> ob : observers)
        {
            Set<Annotation> qualifiers = ob.getObservedQualifiers();

            if (qualifiers.size() > eventQualifiers.size())
            {
                continue;
            }
            

            for (Annotation qualifier : qualifiers)
            {
                boolean found = false;
                for(Annotation inList : eventQualifiers)
                {
                    if(AnnotationUtil.isQualifierEqual(inList, qualifier))
                    {
                        found = true;
                        break;
                    }
                }
                
                if(!found)
                {
                    continue search;
                }
            }

            matching.add(ob);
        }

        return matching;
    }

    public void fireEvent(Object event, Annotation... qualifiers)
    {
        Set<ObserverMethod<? super Object>> observerMethods = resolveObservers(event, qualifiers);

        for (ObserverMethod<? super Object> observer : observerMethods)
        {
            try
            {
                TransactionPhase phase = observer.getTransactionPhase();
                
                if(phase != null && !phase.equals(TransactionPhase.IN_PROGRESS))
                {
                    TransactionService transactionService = webBeansContext.getService(TransactionService.class);
                    if(transactionService != null)
                    {
                        transactionService.registerTransactionSynchronization(phase, observer, event);
                    }
                    else
                    {
                        observer.notify(event);
                    }                    
                }
                else
                {
                    observer.notify(event);
                }
            }
            catch (WebBeansException e)
            {
                Throwable exc = e.getCause();
                if(exc instanceof InvocationTargetException)
                {
                    InvocationTargetException invt = (InvocationTargetException)exc;
                    exc = invt.getCause();
                }
                
                if (!RuntimeException.class.isAssignableFrom(exc.getClass()))
                {
                    throw new ObserverException(WebBeansLoggerFacade.getTokenString(OWBLogConst.EXCEPT_0008) + event.getClass().getName(), e);
                }
                else
                {
                    RuntimeException rte = (RuntimeException) exc;
                    throw rte;
                }
            }
            catch (RuntimeException e)
            {
                throw e;
            }

            catch (Exception e)
            {
                throw new WebBeansException(e);
            }
        }
    }

    public <T> Set<ObserverMethod<?>> addObservableComponentMethods(InjectionTargetBean<?> component)
    {
        Asserts.assertNotNull(component, "component parameter can not be null");
        Set<Method> observableMethods = component.getObservableMethods();
        Set<ObserverMethod<?>> observerMethods = new HashSet<ObserverMethod<?>>();
        
        //check for null
        if(observableMethods != null)
        {
            for (Method observableMethod : observableMethods)
            {
                Observes observes = AnnotationUtil.getMethodFirstParameterAnnotation(observableMethod, Observes.class);

                boolean ifExist = false;

                if (observes.notifyObserver().equals(Reception.IF_EXISTS))
                {
                    ifExist = true;
                }

                ObserverMethodImpl<T> observer = new ObserverMethodImpl(component, observableMethod, ifExist);

                Type type = AnnotationUtil.getMethodFirstParameterWithAnnotation(observableMethod, Observes.class);

                addObserver(observer, type);
                
                observerMethods.add(observer);
            }            
        }

        return observerMethods;
    }
    
    /**
     * Gets observer method from given annotated method.
     * @param <T> bean type info
     * @param annotatedMethod annotated method for observer
     * @param bean bean instance 
     * @return ObserverMethod
     */
    public <T> ObserverMethod<?> getObservableMethodForAnnotatedMethod(AnnotatedMethod<?> annotatedMethod, InjectionTargetBean<T> bean)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod parameter can not be null");

        Observes observes = AnnotationUtil.getAnnotatedMethodFirstParameterAnnotation(annotatedMethod, Observes.class);
        boolean ifExist = false;
        if(observes != null)
        {
            if (observes.notifyObserver().equals(Reception.IF_EXISTS))
            {
                ifExist = true;
            }            
        }
        
        //Looking for qualifiers
        Annotation[] observerQualifiers =
            bean.getWebBeansContext().getAnnotationManager().getAnnotatedMethodFirstParameterQualifierWithGivenAnnotation(
                annotatedMethod, Observes.class);
        
        //Getting observer event type
        Type type = AnnotationUtil.getAnnotatedMethodFirstParameterWithAnnotation(annotatedMethod, Observes.class);
        
        //Observer creation from annotated method
        ObserverMethodImpl<T> observer = new ObserverMethodImpl(bean, annotatedMethod.getJavaMember(), ifExist, observerQualifiers, type);
        observer.setAnnotatedMethod((AnnotatedMethod<T>)annotatedMethod);
        
        //Adds this observer
        addObserver(observer, type);
        

        return observer;
    }

}
