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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InterceptionType;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.apache.webbeans.annotation.AnnotationManager;
import org.apache.webbeans.component.InjectionTargetBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.plugins.OpenWebBeansEjbLCAPlugin;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;


public final class InterceptorUtil
{
    private static final Logger logger = WebBeansLoggerFacade.getLogger(InterceptorUtil.class);

    private final OpenWebBeansEjbLCAPlugin ejbPlugin;
    private final Class<? extends Annotation> prePassivateClass;
    private final Class<? extends Annotation> postActivateClass;

    /**
     * all the bit flags of private static and final modifiers
     */
    private final int MODIFIER_STATIC_FINAL_PRIVATE = Modifier.STATIC | Modifier.FINAL | Modifier.PRIVATE;

    private final WebBeansContext webBeansContext;

    public InterceptorUtil(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
        ejbPlugin = webBeansContext.getPluginLoader().getEjbLCAPlugin();
        if (ejbPlugin != null)
        {
            prePassivateClass = ejbPlugin.getPrePassivateClass();
            postActivateClass = ejbPlugin.getPostActivateClass();
        }
        else
        {
            prePassivateClass = null;
            postActivateClass = null;
        }
    }

    /**
     * Check if the given method is a 'business method'
     * in the sense of the Interceptor specification
     * @param method
     * @return <code>true</code> if the given method is an interceptable business method
     */
    public boolean isWebBeansBusinessMethod(Method method)
    {
        int modifiers = method.getModifiers();

        if ((modifiers & MODIFIER_STATIC_FINAL_PRIVATE) != 0)
        {
            // static, final and private methods are NO business methods!
            return false;
        }

        Annotation[] anns = method.getDeclaredAnnotations();

        // filter out all container 'special' methods
        for (Annotation ann : anns)
        {
            Class <? extends Annotation> annCls = ann.annotationType();
            if (annCls.equals(Inject.class)        ||
                annCls.equals(PreDestroy.class)    ||
                annCls.equals(PostConstruct.class) ||
                annCls.equals(AroundInvoke.class)  ||
                annCls.equals(AroundTimeout.class) ||    // JSR-299 7.2
                ((ejbPlugin != null)              &&
                 (annCls.equals(prePassivateClass)   ||  // JSR-299 7.2
                  annCls.equals(postActivateClass))))    // JSR-299 7.2
            {
                return false;
            }
        }

        return true;
    }

    public Class<? extends Annotation> getInterceptorAnnotationClazz(InterceptionType type)
    {
        if (type.equals(InterceptionType.AROUND_INVOKE))
        {
            return AroundInvoke.class;
        }
        else if (type.equals(InterceptionType.POST_ACTIVATE))
        {
            return postActivateClass;
        }
        else if (type.equals(InterceptionType.POST_CONSTRUCT))
        {
            return PostConstruct.class;
        }
        else if (type.equals(InterceptionType.PRE_DESTROY))
        {
            return PreDestroy.class;
        }
        else if (type.equals(InterceptionType.PRE_PASSIVATE))
        {
            return prePassivateClass;
        }
        else if (type.equals(InterceptionType.AROUND_TIMEOUT))
        {
            return AroundTimeout.class;
        }
        else
        {
            throw new WebBeansException("Undefined interceotion type");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> boolean isBusinessMethodInterceptor(AnnotatedType<T> annotatedType)
    {
        Set<AnnotatedMethod<? super T>> methods = annotatedType.getMethods();
        for(AnnotatedMethod<? super T> methodA : methods)
        {
            AnnotatedMethod<T> method = (AnnotatedMethod<T>)methodA;
            if(method.isAnnotationPresent(AroundInvoke.class))
            {
                    if (!methodA.getParameters().isEmpty())
                    {
                        List<AnnotatedParameter<T>> parameters = method.getParameters();
                        List<Class<?>> clazzParameters = new ArrayList<Class<?>>();
                        for(AnnotatedParameter<T> parameter : parameters)
                        {
                            clazzParameters.add(ClassUtil.getClazz(parameter.getBaseType()));
                        }

                        Class<?>[] params = clazzParameters.toArray(new Class<?>[clazzParameters.size()]);
                        if (params.length == 1 && params[0].equals(InvocationContext.class))
                        {
                            if (ClassUtil.getReturnType(method.getJavaMember()).equals(Object.class))
                            {
                                if (!ClassUtil.isMethodHasCheckedException(method.getJavaMember()))
                                {
                                    if (!Modifier.isStatic(method.getJavaMember().getModifiers()) && !ClassUtil.isFinal(method.getJavaMember().getModifiers()))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
            }
        }

        return false;
    }


    public boolean isBusinessMethodInterceptor(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        Method[] methods = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethods(clazz);
        for (Method method : methods)
        {
            if (AnnotationUtil.hasMethodAnnotation(method, AroundInvoke.class))
            {
                if (ClassUtil.isMethodHasParameter(method))
                {
                    Class<?>[] params = ClassUtil.getMethodParameterTypes(method);
                    if (params.length == 1 && params[0].equals(InvocationContext.class))
                    {
                        if (ClassUtil.getReturnType(method).equals(Object.class))
                        {
                            if (!Modifier.isStatic(method.getModifiers()) && !ClassUtil.isFinal(method.getModifiers()))
                            {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    public boolean isLifecycleMethodInterceptor(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        Method[] methods = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethods(clazz);
        for (Method method : methods)
        {
            if (AnnotationUtil.hasMethodAnnotation(method, PostConstruct.class) || AnnotationUtil.hasMethodAnnotation(method, PreDestroy.class)
                || AnnotationUtil.hasMethodAnnotation(method, postActivateClass)
                || AnnotationUtil.hasMethodAnnotation(method, prePassivateClass)
               )
            {
                if (ClassUtil.isMethodHasParameter(method))
                {
                    Class<?>[] params = ClassUtil.getMethodParameterTypes(method);
                    if (params.length == 1 && params[0].equals(InvocationContext.class))
                    {
                        if (ClassUtil.getReturnType(method).equals(Void.TYPE))
                        {
                            if (!ClassUtil.isMethodHasCheckedException(method))
                            {
                                if (!Modifier.isStatic(method.getModifiers()))
                                {
                                    return true;
                                }
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> boolean isLifecycleMethodInterceptor(AnnotatedType<T> annotatedType)
    {
        Set<AnnotatedMethod<? super T>> methods = annotatedType.getMethods();
        for(AnnotatedMethod<? super T> methodA : methods)
        {
            AnnotatedMethod<T> method = (AnnotatedMethod<T>)methodA;
            if(method.isAnnotationPresent(PostConstruct.class)
                    || method.isAnnotationPresent(PreDestroy.class)
                    || method.isAnnotationPresent(postActivateClass)
                    || method.isAnnotationPresent(prePassivateClass))
            {
                    if (!methodA.getParameters().isEmpty())
                    {
                        List<AnnotatedParameter<T>> parameters = method.getParameters();
                        List<Class<?>> clazzParameters = new ArrayList<Class<?>>();
                        for(AnnotatedParameter<T> parameter : parameters)
                        {
                            clazzParameters.add(ClassUtil.getClazz(parameter.getBaseType()));
                        }

                        Class<?>[] params = clazzParameters.toArray(new Class<?>[clazzParameters.size()]);
                        if (params.length == 1 && params[0].equals(InvocationContext.class))
                        {
                            if (ClassUtil.getReturnType(method.getJavaMember()).equals(Void.TYPE))
                            {
                                if (!ClassUtil.isMethodHasCheckedException(method.getJavaMember()))
                                {
                                    if (!Modifier.isStatic(method.getJavaMember().getModifiers()))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
            }
        }

        return false;
    }


    public <T> void checkAnnotatedTypeInterceptorConditions(AnnotatedType<T> annotatedType)
    {
        Set<AnnotatedMethod<? super T>> methods = annotatedType.getMethods();
        for(AnnotatedMethod<? super T> methodA : methods)
        {
            if(methodA.isAnnotationPresent(Produces.class))
            {
                throw new WebBeansConfigurationException("Interceptor class : " + annotatedType.getJavaClass().getName()
                                                         + " can not have producer methods but it has one with name : "
                                                         + methodA.getJavaMember().getName());
            }

        }

        Set<Annotation> annSet = annotatedType.getAnnotations();
        Annotation[] anns = annSet.toArray(new Annotation[annSet.size()]);
        if (!webBeansContext.getAnnotationManager().hasInterceptorBindingMetaAnnotation(anns))
        {
            throw new WebBeansConfigurationException("Interceptor class : " + annotatedType.getJavaClass().getName()
                                                     + " must have at least one @InterceptorBinding annotation");
        }

        checkLifecycleConditions(annotatedType, anns, "Lifecycle interceptor : " + annotatedType.getJavaClass().getName()
                                                      + " interceptor binding type must be defined as @Target{TYPE}");
    }


    public void checkInterceptorConditions(AnnotatedType annotatedType)
    {
        Asserts.assertNotNull(annotatedType);

        Set<AnnotatedMethod> methods = annotatedType.getMethods();
        for(AnnotatedMethod method : methods)
        {
            List<AnnotatedParameter> parms = method.getParameters();
            for (AnnotatedParameter parameter : parms)
            {
                if (parameter.isAnnotationPresent(Produces.class))
                {
                    throw new WebBeansConfigurationException("Interceptor class : " + annotatedType.getJavaClass()
                            + " can not have producer methods but it has one with name : "
                            + method.getJavaMember().getName());
                }
            }
        }


        Annotation[] anns = annotatedType.getAnnotations().toArray(new Annotation[annotatedType.getAnnotations().size()]);
        if (!webBeansContext.getAnnotationManager().hasInterceptorBindingMetaAnnotation(anns))
        {
            throw new WebBeansConfigurationException("WebBeans Interceptor class : " + annotatedType.getJavaClass()
                                                     + " must have at least one @InterceptorBinding annotation");
        }

        checkLifecycleConditions(annotatedType.getJavaClass(), anns, "Lifecycle interceptor : " + annotatedType.getJavaClass()
                                              + " interceptor binding type must be defined as @Target{TYPE}");
    }

    /**
     * @param clazz AUTSCH! we should use the AnnotatedType for all that stuff!
     */
    public <T> void checkLifecycleConditions(Class<T> clazz, Annotation[] annots, String errorMessage)
    {
        Asserts.nullCheckForClass(clazz);

        if (isLifecycleMethodInterceptor(clazz) && !isBusinessMethodInterceptor(clazz))
        {
            Annotation[] anns = webBeansContext.getAnnotationManager().getInterceptorBindingMetaAnnotations(annots);

            for (Annotation annotation : anns)
            {
                Target target = annotation.annotationType().getAnnotation(Target.class);
                ElementType[] elementTypes = target.value();

                if (!(elementTypes.length == 1 && elementTypes[0].equals(ElementType.TYPE)))
                {
                    throw new WebBeansConfigurationException(errorMessage);
                }
            }
        }

    }

    public <T> void checkLifecycleConditions(AnnotatedType<T> annotatedType, Annotation[] annots, String errorMessage)
    {
        if (isLifecycleMethodInterceptor(annotatedType) && !isBusinessMethodInterceptor(annotatedType))
        {
            Annotation[] anns = webBeansContext.getAnnotationManager().getInterceptorBindingMetaAnnotations(annots);

            for (Annotation annotation : anns)
            {
                Target target = annotation.annotationType().getAnnotation(Target.class);
                ElementType[] elementTypes = target.value();

                if (!(elementTypes.length == 1 && elementTypes[0].equals(ElementType.TYPE)))
                {
                    throw new WebBeansConfigurationException(errorMessage);
                }
            }
        }

    }


    public void checkSimpleWebBeansInterceptorConditions(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        Annotation[] anns = clazz.getDeclaredAnnotations();

        boolean hasClassInterceptors = false;
        AnnotationManager annotationManager = webBeansContext.getAnnotationManager();
        if (annotationManager.getInterceptorBindingMetaAnnotations(anns).length > 0)
        {
            hasClassInterceptors = true;
        }
        else
        {
            Annotation[] stereoTypes = annotationManager.getStereotypeMetaAnnotations(clazz.getDeclaredAnnotations());
            for (Annotation stero : stereoTypes)
            {
                if (annotationManager.hasInterceptorBindingMetaAnnotation(stero.annotationType().getDeclaredAnnotations()))
                {
                    hasClassInterceptors = true;
                    break;
                }
            }
        }

        //Simple webbeans
        if(ClassUtil.isFinal(clazz.getModifiers()) && hasClassInterceptors)
        {
            throw new WebBeansConfigurationException("Final Simple class with name : " + clazz.getName() + " can not define any InterceptorBindings");
        }

        Method[] methods = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethods(clazz);

        for (Method method : methods)
        {
            int modifiers = method.getModifiers();
            if (!method.isSynthetic() && !method.isBridge() && !Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers) && ClassUtil.isFinal(modifiers))
            {
                if (hasClassInterceptors)
                {
                    throw new WebBeansConfigurationException("Simple web bean class : " + clazz.getName()
                                                             + " can not define non-static, non-private final methods. "
                                                             + "Because it is annotated with at least one @InterceptorBinding");
                }
                else
                {
                    if (annotationManager.hasInterceptorBindingMetaAnnotation(
                        method.getDeclaredAnnotations()))
                    {
                        throw new WebBeansConfigurationException("Method : " + method.getName() + "in simple web bean class : "
                                                                 + clazz.getName()
                                                                 + " can not be defined as non-static, non-private and final. "
                                                                 + "Because it is annotated with at least one @InterceptorBinding");
                    }
                }
            }
        }

    }

    /**
     * Gets list of interceptors with the given type.
     *
     * @param stack interceptor stack
     * @param type interceptor type
     * @return list of interceptor
     */
    @SuppressWarnings("unchecked")
    public List<InterceptorData> getInterceptorMethods(List<InterceptorData> stack, InterceptionType type)
    {
        List<InterceptorData> interceptors = new ArrayList<InterceptorData>();

        Iterator<InterceptorData> it = stack.iterator();
        while (it.hasNext())
        {
            Method m = null;
            InterceptorData data = it.next();

            if (type.equals(InterceptionType.AROUND_INVOKE))
            {
                m = data.getAroundInvoke();
            }
            else if (type.equals(InterceptionType.AROUND_TIMEOUT))
            {
                m = data.getAroundTimeout();
            }
            else if (type.equals(InterceptionType.POST_ACTIVATE))
            {
                m = data.getPostActivate();
            }
            else if (type.equals(InterceptionType.POST_CONSTRUCT))
            {
                m = data.getPostConstruct();
            }
            else if (type.equals(InterceptionType.PRE_DESTROY))
            {
                m = data.getPreDestroy();
            }
            else if (type.equals(InterceptionType.PRE_PASSIVATE))
            {
                m = data.getPrePassivate();
            }
            if (m != null)
            {
                interceptors.add(data);
            }
        }

        return interceptors;
    }

    /**
     * Returns true if this interceptor data is not related
     * false othwewise.
     * @param id interceptor data
     * @param method called method
     * @return true if this interceptor data is not related
     */
    private boolean shouldRemoveInterceptorCommon(InterceptorData id, Method method)
    {
        boolean isMethodAnnotatedWithExcludeInterceptorClass = false;
        if (AnnotationUtil.hasMethodAnnotation(method, ExcludeClassInterceptors.class))
        {
            isMethodAnnotatedWithExcludeInterceptorClass = true;
        }

        if (isMethodAnnotatedWithExcludeInterceptorClass)
        {
            // If the interceptor is defined at the class level it should be
            // removed due to ExcludeClassInterceptors method annotation
            if (!id.isDefinedInMethod() && id.isDefinedInInterceptorClass())
            {
                return true;
            }
        }

        // If the interceptor is defined in a different method, remove it
        if (id.isDefinedInMethod() && !id.getInterceptorBindingMethod().equals(method))
        {
            return true;
        }

        return false;
    }

    /**
     * Filter bean interceptor stack.
     * @param stack interceptor stack
     * @param method called method on proxy
     */
    public void filterCommonInterceptorStackList(List<InterceptorData> stack, Method method)
    {
        if (stack.size() > 0)
        {
            Iterator<InterceptorData> it = stack.iterator();
            while (it.hasNext())
            {
                InterceptorData data = it.next();

                if (shouldRemoveInterceptorCommon(data, method))
                {
                    it.remove();
                }
            }
        }
    }

    public Object callAroundInvokes(WebBeansContext webBeansContext, InjectionTargetBean<?> bean,Object instance, CreationalContextImpl<?> creationalContext,
            Method proceed, Object[] arguments, List<InterceptorData> stack, InvocationContext ejbInvocationContext, Object altKey) throws Exception
    {
        InvocationContextImpl impl = new InvocationContextImpl(webBeansContext, bean, instance,
                                                               proceed, arguments, stack, InterceptionType.AROUND_INVOKE);
        if (ejbInvocationContext != null)
        {
            impl.setEJBInvocationContext(ejbInvocationContext);
        }

        if (altKey != null)
        {
            impl.setCcKey(altKey);
        }

        impl.setCreationalContext(creationalContext);

        return impl.proceed();
    }



    /**
     * Return true if candidate class is a super class of given interceptor
     * class.
     *
     * @param interceptorClass interceptor class
     * @param candidateClass candaite class
     * @return true if candidate class is a super class of given interceptor
     *         class
     */
    public boolean checkInInterceptorHierarchy(Class<?> interceptorClass, Class<?> candidateClass)
    {
        Class<?> superClassInterceptor = interceptorClass.getSuperclass();
        if (superClassInterceptor != null && !superClassInterceptor.equals(Object.class))
        {
            if (superClassInterceptor.equals(candidateClass))
            {
                return true;
            }

            else
            {
                return checkInInterceptorHierarchy(superClassInterceptor, candidateClass);
            }
        }

        return false;
    }

    /**
     * Remove bean inherited and overriden lifecycle interceptor method from its
     * stack list.
     *
     * @param beanClass bean class
     * @param stack bean interceptor stack
     */
    public void filterOverridenLifecycleInterceptor(Class<?> beanClass, List<InterceptorData> stack)
    {
        List<InterceptorData> overridenInterceptors = new ArrayList<InterceptorData>();
        Iterator<InterceptorData> it = stack.iterator();
        while (it.hasNext())
        {
            InterceptorData interceptorData = it.next();
            if (interceptorData.isLifecycleInterceptor())
            {
                InterceptorData overridenInterceptor = getOverridenInterceptor(beanClass, interceptorData, stack);
                if (null != overridenInterceptor)
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine("REMOVING parent " + overridenInterceptor);
                    }

                    it.remove();
                }
            }
        }
        stack.removeAll(overridenInterceptors);
    }

    /**
     * If an AroundInvoke method is overridden by another method (regardless of
     * whether that method is itself an AroundInvoke method), it will not be
     * invoked. Remove bean inherited but overriden around invoke interceptor
     * method from its stack list.
     *
     * @param beanClass bean class
     * @param stack bean interceptor stack
     */
    public void filterOverridenAroundInvokeInterceptor(Class<?> beanClass, List<InterceptorData> stack)
    {

        List<InterceptorData> overridenInterceptors = null;
        if (stack.size() > 0)
        {
            Iterator<InterceptorData> it = stack.iterator();
            while (it.hasNext())
            {
                InterceptorData interceptorData = it.next();
                if (interceptorData.getAroundInvoke() != null)
                {
                    InterceptorData overridenInterceptor = getOverridenInterceptor(beanClass, interceptorData, stack);
                    if (null != overridenInterceptor)
                    {
                        if (overridenInterceptors == null)
                        {
                            overridenInterceptors = new ArrayList<InterceptorData>();
                        }
                        overridenInterceptors.add(overridenInterceptor);
                        if (logger.isLoggable(Level.FINE))
                        {
                            logger.fine("REMOVING parent " + overridenInterceptor);
                        }

                    }
                }
            }
        }

        if (overridenInterceptors != null)
        {
            stack.removeAll(overridenInterceptors);
        }
    }

    /**
     * Check to see if any parent class in the hierarchy is in this interceptor
     * stack If any method in the current interceptor has the same name and
     * signature as the parent's interceptor method remove the parent
     * interceptor from the stack
     *
     * @param interceptorData
     * @param stack
     * @return the overriden InterceptorData that represents the parent
     */
    private InterceptorData getOverridenInterceptor(Class<?> clazz, InterceptorData interceptorData, List<InterceptorData> stack)
    {
        Method interceptor = interceptorData.getInterceptorMethod();
        Class<?> interceptorClass = interceptor.getDeclaringClass();

        for (InterceptorData superInterceptorData : stack)
        {

            if (interceptorClass.equals(superInterceptorData.getInterceptorClass()))
            {
                continue; // we are looking at ourself
            }

            // parent interceptor in the interceptor stack
            if (checkInInterceptorHierarchy(interceptorClass, superInterceptorData.getInterceptorClass()))
            {

                // get the interceptor method of the parent
                Method superInterceptorMethod = superInterceptorData.getInterceptorMethod();
                Method childInterceptorMethod = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethod(interceptorClass,
                                              superInterceptorMethod.getName(), superInterceptorMethod.getParameterTypes());

                if (null != childInterceptorMethod && ClassUtil.isOverridden(childInterceptorMethod, superInterceptorMethod))
                {
                    return superInterceptorData;
                }
            }
            else
            { // the class may be overriding the interceptor method
                return removeInheritedButOverridenInterceptor(clazz, interceptorData);

            }
        }

        return null;
    }

    /**
     * This returns the Interceptor that is defined in a super class of the bean
     * and has the same method as the bean. i.e. the bean method overrides the
     * Interceptor method defined in the super class.
     *
     * @param clazz
     * @param interceptorData
     * @return
     */
    private InterceptorData removeInheritedButOverridenInterceptor(Class<?> clazz, InterceptorData interceptorData)
    {
        Method interceptor = interceptorData.getInterceptorMethod();
        Class<?> declaringClass = interceptor.getDeclaringClass();

        // Not look for Interceptor classes
        if (checkGivenClassIsInInterceptorList(clazz, declaringClass))
        {
            return null;
        }

        if (!declaringClass.equals(clazz) && checkInInterceptorHierarchy(clazz, declaringClass))
        {
            Method found = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethod(clazz, interceptor.getName(), interceptor.getParameterTypes());

            if (found == null)
            {
                Class<?> superClass = clazz.getSuperclass();
                if (superClass != null && !superClass.equals(Object.class))
                {
                    return removeInheritedButOverridenInterceptor(superClass, interceptorData);
                }
            }

            return interceptorData;
        }

        return null;
    }

    /**
     * Return true if given candidate is listed in interceptors list.
     *
     * @param mainClass bean class
     * @param candidateClass interceptor candidate class
     * @return true if given candidate is listed in interceptors list
     */
    public boolean checkGivenClassIsInInterceptorList(Class<?> mainClass, Class<?> candidateClass)
    {
        if (AnnotationUtil.hasClassAnnotation(mainClass, Interceptors.class))
        {
            Interceptors incs = mainClass.getAnnotation(Interceptors.class);
            Class<?>[] intClasses = incs.value();

            for (Class<?> intClass : intClasses)
            {
                if (intClass.equals(candidateClass))
                {
                    return true;
                }
                else
                {
                    if (checkInInterceptorHierarchy(intClass, candidateClass))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
