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
package org.apache.webbeans.util;

import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.component.AbstractInjectionTargetBean;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.component.ProducerFieldBean;
import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.component.ResourceBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.component.creation.AnnotatedTypeBeanCreatorImpl;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.impl.InjectionPointFactory;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.spi.api.ResourceReference;

import javax.decorator.Delegate;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.webbeans.util.InjectionExceptionUtils.throwUnsatisfiedResolutionException;

public final class WebBeansAnnotatedTypeUtil
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(WebBeansAnnotatedTypeUtil.class);

    private final WebBeansContext webBeansContext;

    public WebBeansAnnotatedTypeUtil(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }
    
    public static <T> AnnotatedConstructor<T> getBeanConstructor(AnnotatedType<T> type)
    {
        Asserts.assertNotNull(type,"Type is null");
        AnnotatedConstructor<T> result = null;
        
        Set<AnnotatedConstructor<T>> annConsts = type.getConstructors();
        if(annConsts != null)
        {
            boolean found = false;
            boolean noParamConsIsDefined = false;
            for(AnnotatedConstructor<T> annConst : annConsts)
            {
                if(annConst.isAnnotationPresent(Inject.class))
                {
                    if (found)
                    {
                        throw new WebBeansConfigurationException("There are more than one constructor with @Inject annotation in annotation type : "
                                                                 + type);
                    }
                    
                    found = true;
                    result = annConst;
                }
                else
                {
                    if(!found && !noParamConsIsDefined)
                    {
                        List<AnnotatedParameter<T>> parameters = annConst.getParameters();
                        if(parameters != null && parameters.isEmpty())
                        {
                            result = annConst;
                            noParamConsIsDefined = true;
                        }                        
                    }
                }
            }
        }
        
        if (result == null)
        {
            throw new WebBeansConfigurationException("No constructor is found for the annotated type : " + type);
        }
        
        List<AnnotatedParameter<T>> parameters = result.getParameters();
        for(AnnotatedParameter<T> parameter : parameters)
        {
            if (parameter.isAnnotationPresent(Disposes.class))
            {
                throw new WebBeansConfigurationException("Constructor parameter annotations can not contain @Disposes annotation in annotated constructor : "
                                                         + result);
            }
            
            if(parameter.isAnnotationPresent(Observes.class))
            {
                throw new WebBeansConfigurationException("Constructor parameter annotations can not contain @Observes annotation in annotated constructor : " + result);
            }
            
        }

        
        return result;
    }
    
    public <T> void addConstructorInjectionPointMetaData(AbstractOwbBean<T> owner, AnnotatedConstructor<T> constructor)
    {
        InjectionPointFactory injectionPointFactory = owner.getWebBeansContext().getInjectionPointFactory();
        List<InjectionPoint> injectionPoints = injectionPointFactory.getConstructorInjectionPointData(owner, constructor);
        for (InjectionPoint injectionPoint : injectionPoints)
        {
            webBeansContext.getDefinitionUtil().addImplicitComponentForInjectionPoint(injectionPoint);
            owner.addInjectionPoint(injectionPoint);
        }
    }
    
    public <T,X> void addMethodInjectionPointMetaData(OwbBean<T> owner, AnnotatedMethod<X> method)
    {
        List<InjectionPoint> injectionPoints = owner.getWebBeansContext().getInjectionPointFactory().getMethodInjectionPointData(owner, method);
        for (InjectionPoint injectionPoint : injectionPoints)
        {
            webBeansContext.getDefinitionUtil().addImplicitComponentForInjectionPoint(injectionPoint);
            owner.addInjectionPoint(injectionPoint);
        }
    }
    
    public static <T,X> void addFieldInjectionPointMetaData(AbstractOwbBean<T> owner, AnnotatedField<X> annotField)
    {
        owner.addInjectionPoint(owner.getWebBeansContext().getInjectionPointFactory().getFieldInjectionPointData(owner, annotField));
    }
    
    @SuppressWarnings("unchecked")
    public <X> Set<ObserverMethod<?>> defineObserverMethods(AbstractInjectionTargetBean<X> bean,AnnotatedType<X> annotatedType)
    {
        WebBeansContext webBeansContext = bean.getWebBeansContext();
        Set<ObserverMethod<?>> definedObservers = new HashSet<ObserverMethod<?>>();
        Set<AnnotatedMethod<? super X>> annotatedMethods = annotatedType.getMethods();    
        for (AnnotatedMethod<? super X> annotatedMethod : annotatedMethods)
        {
            AnnotatedMethod<X> annt = (AnnotatedMethod<X>)annotatedMethod;
            List<AnnotatedParameter<X>> parameters = annt.getParameters();
            boolean found = false;
            for(AnnotatedParameter<X> parameter : parameters)
            {
                if(parameter.isAnnotationPresent(Observes.class))
                {
                    found = true;
                    break;
                }
            }
            
            if(found)
            {
                checkObserverMethodConditions(annotatedMethod, annotatedMethod.getDeclaringType().getJavaClass());
                if(bean.getScope().equals(Dependent.class))
                {
                    //Check Reception
                     AnnotationUtil.getAnnotatedMethodFirstParameterWithAnnotation(annotatedMethod, Observes.class);
                    
                     Observes observes = AnnotationUtil.getAnnotatedMethodFirstParameterAnnotation(annotatedMethod, Observes.class);
                     Reception reception = observes.notifyObserver();
                     if(reception.equals(Reception.IF_EXISTS))
                     {
                         throw new WebBeansConfigurationException("Dependent Bean : " + bean + " can not define observer method with @Receiver = IF_EXIST");
                     }
                }
                
                //Add method
                bean.addObservableMethod(annotatedMethod.getJavaMember());

                //Add injection point data
                addMethodInjectionPointMetaData(bean, annotatedMethod);
                
                //Looking for ObserverMethod
                ObserverMethod<?> definedObserver = webBeansContext.getBeanManagerImpl().getNotificationManager().getObservableMethodForAnnotatedMethod(annotatedMethod, bean);
                if(definedObserver != null)
                {
                    definedObservers.add(definedObserver);
                }
            }
        }
        
        return definedObservers;
    }
    
    @SuppressWarnings("unchecked")
    public <X> void defineDisposalMethods(AbstractInjectionTargetBean<X> bean,AnnotatedType<X> annotatedType)
    {
        final AnnotationManager annotationManager = bean.getWebBeansContext().getAnnotationManager();
        Set<AnnotatedMethod<? super X>> annotatedMethods = annotatedType.getMethods();    
        ProducerMethodBean<?> previous = null;
        for (AnnotatedMethod<? super X> annotatedMethod : annotatedMethods)
        {
            Method declaredMethod = annotatedMethod.getJavaMember();
            AnnotatedMethod<X> annt = (AnnotatedMethod<X>)annotatedMethod;
            List<AnnotatedParameter<X>> parameters = annt.getParameters();
            boolean found = false;
            for(AnnotatedParameter<X> parameter : parameters)
            {
                if(parameter.isAnnotationPresent(Disposes.class))
                {
                    found = true;
                    break;
                }
            }
            
            if(found)
            {
                checkProducerMethodDisposal(annotatedMethod);
                Type type = AnnotationUtil.getAnnotatedMethodFirstParameterWithAnnotation(annotatedMethod, Disposes.class);
                Annotation[] annot = annotationManager.getAnnotatedMethodFirstParameterQualifierWithGivenAnnotation(annotatedMethod, Disposes.class);

                InjectionResolver injectionResolver = bean.getWebBeansContext().getBeanManagerImpl().getInjectionResolver();

                Set<Bean<?>> set = injectionResolver.implResolveByType(type, annot);
                if (set.isEmpty())
                {
                    throwUnsatisfiedResolutionException(type, declaredMethod, annot);
                }
                
                Bean<?> foundBean = set.iterator().next();
                ProducerMethodBean<?> pr = null;

                if (foundBean == null || !(foundBean instanceof ProducerMethodBean))
                {
                    throwUnsatisfiedResolutionException(annotatedMethod.getDeclaringType().getJavaClass(), declaredMethod, annot);
                }

                pr = (ProducerMethodBean<?>) foundBean;

                if (previous == null)
                {
                    previous = pr;
                }
                else
                {
                    // multiple same producer
                    if (previous.equals(pr))
                    {
                        throw new WebBeansConfigurationException("There are multiple disposal method for the producer method : " + pr.getCreatorMethod().getName() + " in class : "
                                                                 + annotatedMethod.getDeclaringType().getJavaClass());
                    }
                }

                Method producerMethod = pr.getCreatorMethod();
                //Disposer methods and producer methods must be in the same class
                if(!producerMethod.getDeclaringClass().getName().equals(declaredMethod.getDeclaringClass().getName()))
                {
                    throw new WebBeansConfigurationException("Producer method component of the disposal method : " + declaredMethod.getName() + " in class : "
                                                             + annotatedMethod.getDeclaringType().getJavaClass() + " must be in the same class!");
                }
                
                pr.setDisposalMethod(declaredMethod);

                addMethodInjectionPointMetaData(bean, annotatedMethod);
                
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public <X> void defineInjectedMethods(AbstractInjectionTargetBean<X> bean,AnnotatedType<X> annotatedType)
    {
        Set<AnnotatedMethod<? super X>> annotatedMethods = annotatedType.getMethods();
        
        for (AnnotatedMethod<? super X> annotatedMethod : annotatedMethods)
        {            
            boolean isInitializer = annotatedMethod.isAnnotationPresent(Inject.class);            

            if (isInitializer)
            {
                //Do not support static
                if(annotatedMethod.isStatic())
                {
                    continue;
                }
                
                checkForInjectedInitializerMethod(bean, (AnnotatedMethod<X>)annotatedMethod);
            }
            else
            {
                continue;
            }

            Method method = annotatedMethod.getJavaMember();
            
            if (!Modifier.isStatic(method.getModifiers()))
            {
                bean.addInjectedMethod(method);
                addMethodInjectionPointMetaData(bean, annotatedMethod);
            }
        }
        
        webBeansContext.getDefinitionUtil().defineInternalInjectedMethodsRecursively(bean, annotatedType.getJavaClass());
    }
    
    public static <X> void defineInjectedFields(AbstractInjectionTargetBean<X> bean,AnnotatedType<X> annotatedType)
    {
        AnnotationManager annotationManager = bean.getWebBeansContext().getAnnotationManager();

        Set<AnnotatedField<? super X>> annotatedFields = annotatedType.getFields();   
        for(AnnotatedField<? super X> annotatedField: annotatedFields)
        {
            if(!annotatedField.isAnnotationPresent(Inject.class))
            {
                continue;
            }

            if (annotatedField.isAnnotationPresent(Produces.class) || annotatedField.isAnnotationPresent(Delegate.class))
            {
                continue;
            }
            
            
            Field field = annotatedField.getJavaMember();
            Annotation[] anns = AnnotationUtil.getAnnotationsFromSet(annotatedField.getAnnotations());
            if(Modifier.isPublic(field.getModifiers()))
            {
                if(!bean.getScope().equals(Dependent.class))
                {
                    throw new WebBeansConfigurationException("Error in annotated field : " + annotatedField
                                                    +" while definining injected field. If bean has a public modifier injection point, bean scope must be defined as @Dependent");
                }
            }

            Annotation[] qualifierAnns = annotationManager.getQualifierAnnotations(anns);

            if (qualifierAnns.length > 0)
            {
                if (qualifierAnns.length > 0)
                {
                    annotationManager.checkForNewQualifierForDeployment(annotatedField.getBaseType(), annotatedField.getDeclaringType().getJavaClass(), field.getName(), anns);
                }

                int mod = field.getModifiers();
                
                if (!Modifier.isStatic(mod) && !Modifier.isFinal(mod))
                {
                    bean.addInjectedField(field);
                    addFieldInjectionPointMetaData(bean, annotatedField);                                
                }
            }                                    
        }
        
        bean.getWebBeansContext().getDefinitionUtil().defineInternalInjectedFieldsRecursively(bean, annotatedType.getJavaClass());
    }
    
    
    @SuppressWarnings("unchecked")
    public <X> Set<ProducerFieldBean<?>> defineProducerFields(InjectionTargetBean<X> bean, AnnotatedType<X> annotatedType)
    {
        DefinitionUtil definitionUtil = webBeansContext.getDefinitionUtil();
        Set<ProducerFieldBean<?>> producerBeans = new HashSet<ProducerFieldBean<?>>();
        Set<AnnotatedField<? super X>> annotatedFields = annotatedType.getFields();        
        for(AnnotatedField<? super X> annotatedField: annotatedFields)
        {
            if(annotatedField.isAnnotationPresent(Produces.class))
            {
                Type genericType = annotatedField.getBaseType();
                
                if(ClassUtil.isParametrizedType(genericType))
                {
                    if(!ClassUtil.checkParametrizedType((ParameterizedType)genericType))
                    {
                        throw new WebBeansConfigurationException("Producer annotated field : " + annotatedField + " can not be Wildcard type or Type variable");
                    }
                }
                
                Annotation[] anns = AnnotationUtil.getAnnotationsFromSet(annotatedField.getAnnotations());
                Field field = annotatedField.getJavaMember();
                
                //Producer field for resource
                Annotation resourceAnnotation = AnnotationUtil.hasOwbInjectableResource(anns);                
                //Producer field for resource
                if(resourceAnnotation != null)
                {                    
                    //Check for valid resource annotation
                    //WebBeansUtil.checkForValidResources(annotatedField.getDeclaringType().getJavaClass(), field.getType(), field.getName(), anns);
                    if(!Modifier.isStatic(field.getModifiers()))
                    {
                        ResourceReference<X,Annotation> resourceRef = new ResourceReference<X, Annotation>(bean.getBeanClass(), field.getName(),
                                                                                                           (Class<X>)field.getType(), resourceAnnotation);
                        
                        //Can not define EL name
                        if(annotatedField.isAnnotationPresent(Named.class))
                        {
                            throw new WebBeansConfigurationException("Resource producer annotated field : " + annotatedField + " can not define EL name");
                        }
                        
                        ResourceBean<X,Annotation> resourceBean = new ResourceBean((Class<X>)field.getType(),bean, resourceRef);
                        
                        resourceBean.getTypes().addAll(annotatedField.getTypeClosure());
                        definitionUtil.defineQualifiers(resourceBean, anns);
                        resourceBean.setImplScopeType(new DependentScopeLiteral());
                        resourceBean.setProducerField(field);
                        
                        producerBeans.add(resourceBean);                                            
                    }
                }
                else
                {
                    ProducerFieldBean<X> producerFieldBean = new ProducerFieldBean<X>(bean, (Class<X>)ClassUtil.getClass(annotatedField.getBaseType()));
                    producerFieldBean.setProducerField(field);
                    
                    if (ClassUtil.getClass(annotatedField.getBaseType()).isPrimitive())
                    {
                        producerFieldBean.setNullable(false);
                    }                    

                    definitionUtil.defineSerializable(producerFieldBean);
                    definitionUtil.defineStereoTypes(producerFieldBean, anns);
                    webBeansContext.getWebBeansUtil().setBeanEnableFlagForProducerBean(bean,
                                                                                                      producerFieldBean,
                                                                                                      anns);
                    Set<Type> types = annotatedField.getTypeClosure();
                    producerFieldBean.getTypes().addAll(types);
                    definitionUtil.defineScopeType(producerFieldBean, anns, "Annotated producer field: " + annotatedField +  "must declare default @Scope annotation", false);
                    webBeansContext.getWebBeansUtil().checkUnproxiableApiType(producerFieldBean,
                                                                                             producerFieldBean.getScope());
                    WebBeansUtil.checkProducerGenericType(producerFieldBean,annotatedField.getJavaMember());
                    definitionUtil.defineQualifiers(producerFieldBean, anns);
                    definitionUtil.defineName(producerFieldBean, anns, WebBeansUtil.getProducerDefaultName(annotatedField.getJavaMember().getName()));
                    
                    producerBeans.add(producerFieldBean);
                }
            }
        }
        
        return producerBeans;
    }
    
    
    @SuppressWarnings("unchecked")
    public <X> Set<ProducerMethodBean<?>> defineProducerMethods(InjectionTargetBean<X> bean, AnnotatedType<X> annotatedType)
    {
        DefinitionUtil definitionUtil = webBeansContext.getDefinitionUtil();
        Set<ProducerMethodBean<?>> producerBeans = new HashSet<ProducerMethodBean<?>>();
        Set<AnnotatedMethod<? super X>> annotatedMethods = annotatedType.getMethods();
        
        for(AnnotatedMethod<? super X> annotatedMethod: annotatedMethods)
        {
            if(annotatedMethod.isAnnotationPresent(Produces.class))
            {
                checkProducerMethodForDeployment(annotatedMethod);
                boolean specialize = false;
                if(annotatedMethod.isAnnotationPresent(Specializes.class))
                {
                    if (annotatedMethod.isStatic())
                    {
                        throw new WebBeansConfigurationException("Specializing annotated producer method : " + annotatedMethod + " can not be static");
                    }
                    
                    specialize = true;
                }
                
                ProducerMethodBean<X> producerMethodBean = new ProducerMethodBean<X>(bean, (Class<X>)ClassUtil.getClass(annotatedMethod.getBaseType()));
                producerMethodBean.setCreatorMethod(annotatedMethod.getJavaMember());
                
                if(specialize)
                {
                    configureProducerSpecialization(producerMethodBean, (AnnotatedMethod<X>)annotatedMethod);
                }
                
                if (ClassUtil.getClass(annotatedMethod.getBaseType()).isPrimitive())
                {
                    producerMethodBean.setNullable(false);
                }
                
                definitionUtil.defineSerializable(producerMethodBean);
                definitionUtil.defineStereoTypes(producerMethodBean, AnnotationUtil.getAnnotationsFromSet(annotatedMethod.getAnnotations()));
                webBeansContext.getWebBeansUtil().setBeanEnableFlagForProducerBean(bean,
                                                                                   producerMethodBean,
                                                                                   AnnotationUtil.getAnnotationsFromSet(annotatedMethod.getAnnotations()));

                Set<Type> types = annotatedMethod.getTypeClosure();
                producerMethodBean.getTypes().addAll(types);
                definitionUtil.defineScopeType(producerMethodBean,
                                               AnnotationUtil.getAnnotationsFromSet(annotatedMethod.getAnnotations()),
                                                                                    "Annotated producer method : " + annotatedMethod +  "must declare default @Scope annotation",
                                                                                    false);
                webBeansContext.getWebBeansUtil().checkUnproxiableApiType(producerMethodBean,
                                                                                         producerMethodBean.getScope());
                WebBeansUtil.checkProducerGenericType(producerMethodBean,annotatedMethod.getJavaMember());
                definitionUtil.defineName(producerMethodBean,
                                          AnnotationUtil.getAnnotationsFromSet(annotatedMethod.getAnnotations()),
                                                                               WebBeansUtil.getProducerDefaultName(annotatedMethod.getJavaMember().getName()));
                definitionUtil.defineQualifiers(producerMethodBean, AnnotationUtil.getAnnotationsFromSet(annotatedMethod.getAnnotations()));
                
                addMethodInjectionPointMetaData(producerMethodBean, annotatedMethod);
                producerBeans.add(producerMethodBean);
                
            }
            
        }
        
        return producerBeans;
    }
    
    
    /**
     * Check producer method is ok for deployment.
     * 
     * @param annotatedMethod producer method
     */
    public static <X> void checkProducerMethodForDeployment(AnnotatedMethod<X> annotatedMethod)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");

        if (annotatedMethod.isAnnotationPresent(Inject.class) || 
                annotatedMethod.isAnnotationPresent(Disposes.class) ||  
                annotatedMethod.isAnnotationPresent(Observes.class))
        {
            throw new WebBeansConfigurationException("Producer annotated method : " + annotatedMethod + " can not be annotated with"
                                                     + " @Initializer/@Destructor annotation or has a parameter annotated with @Disposes/@Observes");
        }
    }
    
    
    public static <X> void configureProducerSpecialization(AbstractOwbBean<X> bean,AnnotatedMethod<X> annotatedMethod)
    {
        List<AnnotatedParameter<X>> annotatedParameters = annotatedMethod.getParameters();
        List<Class<?>> parameters = new ArrayList<Class<?>>();
        for(AnnotatedParameter<X> annotatedParam : annotatedParameters)
        {
            parameters.add(ClassUtil.getClass(annotatedParam.getBaseType()));
        }
        
        Method superMethod = ClassUtil.getClassMethodWithTypes(annotatedMethod.getDeclaringType().getJavaClass().getSuperclass(), 
                annotatedMethod.getJavaMember().getName(), parameters);
        if (superMethod == null)
        {
            throw new WebBeansConfigurationException("Anontated producer method specialization is failed : " + annotatedMethod.getJavaMember().getName()
                                                     + " not found in super class : " + annotatedMethod.getDeclaringType().getJavaClass().getSuperclass().getName()
                                                     + " for annotated method : " + annotatedMethod);
        }
        
        if (!AnnotationUtil.hasAnnotation(superMethod.getAnnotations(), Produces.class))
        {
            throw new WebBeansConfigurationException("Anontated producer method specialization is failed : " + annotatedMethod.getJavaMember().getName()
                                                     + " found in super class : " + annotatedMethod.getDeclaringType().getJavaClass().getSuperclass().getName()
                                                     + " is not annotated with @Produces" + " for annotated method : " + annotatedMethod);
        }

        /* To avoid multiple invocations of setBeanName(), following code is delayed to
         * configSpecializedProducerMethodBeans() when checkSpecializations.
        Annotation[] anns = AnnotationUtil.getQualifierAnnotations(superMethod.getAnnotations());

        for (Annotation ann : anns)
        {
            bean.addQualifier(ann);
        }
        
        WebBeansUtil.configuredProducerSpecializedName(bean, annotatedMethod.getJavaMember(), superMethod);
        */
        
        bean.setSpecializedBean(true);        
    }
    
    /**
     * add the definitions for a &#x0040;Initializer method.
     */
    private static <X> void checkForInjectedInitializerMethod(AbstractInjectionTargetBean<X> component, AnnotatedMethod<X> annotatedMethod)
    {
        Method method = annotatedMethod.getJavaMember();
        
        TypeVariable<?>[] args = method.getTypeParameters();
        if(args.length > 0)
        {
            throw new WebBeansConfigurationException("Error in defining injected methods in annotated method : " + annotatedMethod+ 
                    ". Reason : Initializer methods must not be generic.");
        }
        
        if (annotatedMethod.isAnnotationPresent(Produces.class))
        {
            throw new WebBeansConfigurationException("Error in defining injected methods in annotated method : " + annotatedMethod+ 
            ". Reason : Initializer method can not be annotated with @Produces.");
        
        }

        AnnotationManager annotationManager = component.getWebBeansContext().getAnnotationManager();

        List<AnnotatedParameter<X>> annotatedParameters = annotatedMethod.getParameters();
        for (AnnotatedParameter<X> annotatedParameter : annotatedParameters)
        {
            annotationManager.checkForNewQualifierForDeployment(annotatedParameter.getBaseType(), annotatedMethod.getDeclaringType().getJavaClass(),
                    method.getName(), AnnotationUtil.getAnnotationsFromSet(annotatedParameter.getAnnotations()));

            if(annotatedParameter.isAnnotationPresent(Disposes.class) ||
                    annotatedParameter.isAnnotationPresent(Observes.class))
            {
                throw new WebBeansConfigurationException("Error in defining injected methods in annotated method : " + annotatedMethod+ 
                ". Reason : Initializer method parameters does not contain @Observes or @Dispose annotations.");
                
            }
        }
    }
    
    /**
     * CheckProducerMethodDisposal.
     * @param annotatedMethod disposal method
     */
    public static <X> void checkProducerMethodDisposal(AnnotatedMethod<X> annotatedMethod)
    {
        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        boolean found = false;
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(Disposes.class))
            {
                if(found)
                {
                    throw new WebBeansConfigurationException("Error in definining disposal method of annotated method : " + annotatedMethod
                            + ". Multiple disposes annotation.");
                }
                found = true;
            }
        }
        
        if(annotatedMethod.isAnnotationPresent(Inject.class) 
                || AnnotationUtil.hasAnnotatedMethodParameterAnnotation(annotatedMethod, Observes.class) 
                || annotatedMethod.isAnnotationPresent(Produces.class))
        {
            throw new WebBeansConfigurationException("Error in definining disposal method of annotated method : " + annotatedMethod
                    + ". Disposal methods  can not be annotated with" + " @Initializer/@Destructor/@Produces annotation or has a parameter annotated with @Observes.");
        }        
    }
    
    public static <X> void checkObserverMethodConditions(AnnotatedMethod<X> annotatedMethod, Class<?> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod parameter can not be null");
        Asserts.nullCheckForClass(clazz);
        
        Method candidateObserverMethod = annotatedMethod.getJavaMember();
        
        if (AnnotationUtil.hasAnnotatedMethodMultipleParameterAnnotation(annotatedMethod, Observes.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : " + clazz.getName()
                                                     + " can not define two parameters with annotated @Observes");
        }

        if (annotatedMethod.isAnnotationPresent(Produces.class) 
                || annotatedMethod.isAnnotationPresent(Inject.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : " + clazz.getName()
                                                     + " can not annotated with annotation in the list {@Produces, @Initializer, @Destructor}");

        }

        if (AnnotationUtil.hasAnnotatedMethodParameterAnnotation(annotatedMethod, Disposes.class))
        {
            throw new WebBeansConfigurationException("Observer method : " + candidateObserverMethod.getName() + " in class : "
                                                     + clazz.getName() + " can not annotated with annotation @Disposes");
        }                
    }

    /**
     * Gets injection points for the given javaee component annotated type.
     * @param webBeansContext
     *@param type annotated type for the class  @return injection points of the java ee component class
     * @throws IllegalArgumentException if any exception occurs
     */
    public static <T> Set<InjectionPoint> getJavaEeComponentInstanceInjectionPoints(WebBeansContext webBeansContext,
                                                                                    AnnotatedType<T> type) throws IllegalArgumentException
    {
        try
        {
            if(type == null)
            {
                return Collections.emptySet();
            }
            else
            {
                //Class of the component
                Class<T> clazz = type.getJavaClass();
                
                //Just creating temporary for getting injected fields
                ManagedBean<T> managedBean = new ManagedBean<T>(clazz,WebBeansType.MANAGED, webBeansContext);
                managedBean.setImplScopeType(new DependentScopeLiteral());
                managedBean.setAnnotatedType(type);
                            
                AnnotatedTypeBeanCreatorImpl<T> managedBeanCreator = new AnnotatedTypeBeanCreatorImpl<T>(managedBean);            
                managedBeanCreator.setAnnotatedType(type);
                
                //Just define injections
                managedBeanCreator.defineInjectedFields();
                managedBeanCreator.defineInjectedMethods();
                
                return managedBean.getInjectionPoints();
                
            }            
        }
        catch(Exception e)
        {
            String message = "Error is occured while getting injection points for the Java EE component instance class, " + type.getJavaClass(); 
            logger.log(Level.SEVERE, message, e);
            throw new IllegalArgumentException(message, e);
        }
    }
}
