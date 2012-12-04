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
package org.apache.webbeans.annotation;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.OwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.inject.DefinitionException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.ArrayUtil;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.WebBeansUtil;

import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.interceptor.InterceptorBinding;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages annotation usage by classes in this application.
 */
public final class AnnotationManager
{

    private final BeanManagerImpl beanManagerImpl;
    private final WebBeansContext webBeansContext;

    private final static Annotation[] ONLY_DEFAULT_ANNOTATION = new Annotation[1];
    static
    {
        ONLY_DEFAULT_ANNOTATION[0] = new DefaultLiteral();
    }

    // No instantiate

    public AnnotationManager(WebBeansContext context)
    {
        webBeansContext = context;
        beanManagerImpl = context.getBeanManagerImpl();
    }

    /**
     * Returns true if the annotation is defined in xml or annotated with
     * {@link javax.interceptor.InterceptorBinding} or an InterceptorBinding
     * registered via {@link javax.enterprise.inject.spi.BeforeBeanDiscovery}.
     * False otherwise.
     *
     * @param clazz type of the annotation
     * @return true if the annotation is defined in xml or annotated with
     *         {@link javax.interceptor.InterceptorBinding}, false otherwise
     */
    public boolean isInterceptorBindingAnnotation(Class<? extends Annotation> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        return clazz.isAnnotationPresent(InterceptorBinding.class)
               || beanManagerImpl.hasInterceptorBindingType(clazz);
    }



    /**
     * If any Annotations in the input is an interceptor binding annotation type then return
     * true, false otherwise.
     *
     * @param anns array of Annotations to check
     * @return true if one or moe of the input annotations are an interceptor binding annotation
     *         type false otherwise
     */
    public boolean hasInterceptorBindingMetaAnnotation(Annotation[] anns)
    {
        Asserts.assertNotNull(anns, "anns parameter can not be null");

        for (Annotation ann : anns)
        {
            if (isInterceptorBindingAnnotation(ann.annotationType()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Collect the interceptor bindings from an array of annotations, including
     * transitively defined interceptor bindings.
     * @param anns An array of annotations
     * @return an array of interceptor binding annotations, including the input and any transitively declared annotations
     */
    public Annotation[] getInterceptorBindingMetaAnnotations(Annotation[] anns)
    {
        Asserts.assertNotNull(anns, "anns parameter can not be null");
        List<Annotation> interAnns = new ArrayList<Annotation>();

        for (Annotation ann : anns)
        {
            if (isInterceptorBindingAnnotation(ann.annotationType()))
            {
                interAnns.add(ann);

                //check for transitive
                Annotation[] transitives = getTransitiveInterceptorBindings(ann.annotationType().getDeclaredAnnotations());

                for(Annotation transitive : transitives)
                {
                    interAnns.add(transitive);
                }

            }
        }

        Annotation[] ret = new Annotation[interAnns.size()];
        ret = interAnns.toArray(ret);

        return ret;
    }

    private Annotation[] getTransitiveInterceptorBindings(Annotation[] anns)
    {
        return getInterceptorBindingMetaAnnotations(anns);
    }

    /**
     * Returns true if the annotation is defined in xml or annotated with
     * {@link javax.inject.Qualifier} false otherwise.
     *
     * @param clazz type of the annotation
     * @return true if the annotation is defined in xml or annotated with
     *         {@link javax.inject.Qualifier} false otherwise
     */
    public boolean isQualifierAnnotation(Class<? extends Annotation> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        if (clazz.isAnnotationPresent(Qualifier.class))
        {
            return true;
        }
        else if(beanManagerImpl.getAdditionalQualifiers().contains(clazz))
        {
            return true;
        }

        return false;
    }

    public <X> Annotation[] getAnnotatedMethodFirstParameterQualifierWithGivenAnnotation(
            AnnotatedMethod<X> annotatedMethod, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");
        Asserts.nullCheckForClass(clazz);

        List<Annotation> list = new ArrayList<Annotation>();
        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(clazz))
            {
                Annotation[] anns = AnnotationUtil.getAnnotationsFromSet(parameter.getAnnotations());
                for(Annotation ann : anns)
                {
                    if(isQualifierAnnotation(ann.annotationType()))
                    {
                        list.add(ann);
                    }
                }
            }
        }

        Annotation[] finalAnns = new Annotation[list.size()];
        finalAnns = list.toArray(finalAnns);

        return finalAnns;
    }


    /**
     * Gets the method first found parameter qualifiers.
     *
     * @param method method
     * @param clazz checking annotation
     * @return annotation array
     */
    public Annotation[] getMethodFirstParameterQualifierWithGivenAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();
        List<Annotation> list = new ArrayList<Annotation>();
        Annotation[] result;

        for (Annotation[] parameters : parameterAnns)
        {
            boolean found = false;
            for (Annotation param : parameters)
            {
                Class<? extends Annotation> btype = param.annotationType();
                if (btype.equals(clazz))
                {
                    found = true;
                    continue;
                }

                if (isQualifierAnnotation(btype))
                {
                    list.add(param);
                }

            }

            if (found)
            {
                result = new Annotation[list.size()];
                result = list.toArray(result);
                return result;
            }
        }
        result = AnnotationUtil.EMPTY_ANNOTATION_ARRAY;
        return result;
    }

    /**
     * Gets the array of qualifier annotations on the given array.
     *
     * @param annotations annotation array
     * @return array containing qualifier anns
     */
    public Annotation[] getQualifierAnnotations(Annotation... annotations)
    {
        Asserts.assertNotNull(annotations, "Annotations argument can not be null");

        if (annotations.length == 0)
        {
            return ONLY_DEFAULT_ANNOTATION;
        }

        Set<Annotation> set = new HashSet<Annotation>();

        for (Annotation annot : annotations)
        {
            if (isQualifierAnnotation(annot.annotationType()))
            {
                set.add(annot);
            }
        }

        //Add the default qualifier if no others exist.  Section 3.10, OWB-142///
        if(set.size() == 0)
        {
            return ONLY_DEFAULT_ANNOTATION;
        }
        ////////////////////////////////////////////////////////////////////////

        Annotation[] a = new Annotation[set.size()];
        a = set.toArray(a);

        return a;
    }

    public void checkQualifierConditions(Annotation... qualifierAnnots)
    {
        Set<Annotation> annSet = ArrayUtil.asSet(qualifierAnnots);

        //check for duplicate annotations
        if (qualifierAnnots.length != annSet.size())
        {
            throw new IllegalArgumentException("Qualifier annotations can not contain duplicate qualifiers:"
                                               + Arrays.toString(qualifierAnnots));
        }

        checkQualifierConditions(annSet);
    }

    /**
     * This function obviously cannot check for duplicate annotations.
     * So this must have been done before!
     * @param qualifierAnnots
     */
    public void checkQualifierConditions(Set<Annotation> qualifierAnnots)
    {
        for (Annotation ann : qualifierAnnots)
        {
            checkQualifierConditions(ann);
        }
    }

    private void checkQualifierConditions(Annotation ann)
    {
        Method[] methods = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethods(ann.annotationType());

        for (Method method : methods)
        {
            Class<?> clazz = method.getReturnType();
            if (clazz.isArray() || clazz.isAnnotation())
            {
                if (!AnnotationUtil.hasAnnotation(method.getDeclaredAnnotations(), Nonbinding.class))
                {
                    throw new WebBeansConfigurationException("@Qualifier : " + ann.annotationType().getName()
                                                             + " must have @NonBinding valued members for its array-valued and annotation valued members");
                }
            }
        }

        if (!isQualifierAnnotation(ann.annotationType()))
        {
            throw new IllegalArgumentException("Qualifier annotations must be annotated with @Qualifier");
        }
    }

    /**
     * Returns true if the annotation is defined in xml or annotated with
     * {@link javax.enterprise.inject.Stereotype} false otherwise.
     *
     * @param clazz type of the annotation
     * @return true if the annotation is defined in xml or annotated with
     *         {@link javax.enterprise.inject.Stereotype} false otherwise
     */
    public boolean isStereoTypeAnnotation(Class<? extends Annotation> clazz)
    {
        Asserts.nullCheckForClass(clazz);

        return clazz.isAnnotationPresent(Stereotype.class);
    }

    public boolean hasStereoTypeMetaAnnotation(Annotation[] anns)
    {
        Asserts.assertNotNull(anns, "anns parameter can not be null");

        for (Annotation ann : anns)
        {
            if (isStereoTypeAnnotation(ann.annotationType()))
            {
                return true;
            }
        }

        return false;
    }

    public Annotation[] getStereotypeMetaAnnotations(Annotation[] anns)
    {
        Asserts.assertNotNull(anns, "anns parameter can not be null");
        List<Annotation> interAnns = new ArrayList<Annotation>();

        for (Annotation ann : anns)
        {
            if (isStereoTypeAnnotation(ann.annotationType()))
            {
                interAnns.add(ann);

                //check for transitive
                Annotation[] transitives = getTransitiveStereoTypes(ann.annotationType().getDeclaredAnnotations());

                for(Annotation transitive : transitives)
                {
                    interAnns.add(transitive);
                }
            }
        }

        Annotation[] ret = new Annotation[interAnns.size()];
        ret = interAnns.toArray(ret);

        return ret;
    }

    private Annotation[] getTransitiveStereoTypes(Annotation[] anns)
    {
        return getStereotypeMetaAnnotations(anns);
    }

    /**
     * Returns true if array contains the StereoType meta annotation
     *
     * @return true if array contains the StereoType meta annotation
     */
    public boolean isComponentHasStereoType(OwbBean<?> component)
    {
        Asserts.assertNotNull(component, "component parameter can not be null");

        Set<Annotation> set = component.getOwbStereotypes();
        Annotation[] anns = new Annotation[set.size()];
        anns = set.toArray(anns);
        return hasStereoTypeMetaAnnotation(anns);
    }

    /**
     * Returns bean stereotypes.
     * @param bean bean instance
     * @return bean stereotypes
     */
    public Annotation[] getComponentStereoTypes(OwbBean<?> bean)
    {
        Asserts.assertNotNull(bean, "bean parameter can not be null");
        if (isComponentHasStereoType(bean))
        {
            Set<Annotation> set = bean.getOwbStereotypes();
            Annotation[] anns = new Annotation[set.size()];
            anns = set.toArray(anns);

            return getStereotypeMetaAnnotations(anns);
        }

        return new Annotation[] {};
    }

    /**
     * Returns true if name exists,false otherwise.
     * @param bean bean instance
     * @return true if name exists
     */
    public boolean hasNamedOnStereoTypes(OwbBean<?> bean)
    {
        Annotation[] types = getComponentStereoTypes(bean);

        for (Annotation ann : types)
        {
            if (AnnotationUtil.hasClassAnnotation(ann.annotationType(), Named.class))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates that given class obeys stereotype model
     * defined by the specification.
     * @param clazz stereotype class
     */
    public void checkStereoTypeClass(Class<? extends Annotation> clazz, Annotation...annotations)
    {
        Asserts.nullCheckForClass(clazz);

        boolean scopeTypeFound = false;
        for (Annotation annotation : annotations)
        {
            Class<? extends Annotation> annotType = annotation.annotationType();

            if (annotType.isAnnotationPresent(NormalScope.class) || annotType.isAnnotationPresent(Scope.class))
            {
                if (scopeTypeFound)
                {
                    throw new WebBeansConfigurationException("@StereoType annotation can not contain more " +
                            "than one @Scope/@NormalScope annotation");
                }
                else
                {
                    scopeTypeFound = true;
                }
            }
            else if (annotType.equals(Named.class))
            {
                Named name = (Named) annotation;
                if (!name.value().equals(""))
                {
                    throw new WebBeansConfigurationException("@StereoType annotation can not define @Named " +
                            "annotation with value");
                }
            }
        }
    }

    public void checkInterceptorResolverParams(Annotation... interceptorBindings)
    {
        if (interceptorBindings == null || interceptorBindings.length == 0)
        {
            throw new IllegalArgumentException("Manager.resolveInterceptors() method parameter interceptor bindings " +
                    "array argument can not be empty");
        }

        Annotation old = null;
        for (Annotation interceptorBinding : interceptorBindings)
        {
            if (!isInterceptorBindingAnnotation(interceptorBinding.annotationType()))
            {
                throw new IllegalArgumentException("Manager.resolveInterceptors() method parameter interceptor" +
                        " bindings array can not contain other annotation that is not @InterceptorBinding");
            }

            if (old == null)
            {
                old = interceptorBinding;
            }
            else
            {
                if (old.equals(interceptorBinding))
                {
                    throw new IllegalArgumentException("Manager.resolveInterceptors() method parameter interceptor " +
                            "bindings array argument can not define duplicate binding annotation with name : @" +
                            old.getClass().getName());
                }

                old = interceptorBinding;
            }
        }
    }

    public void checkDecoratorResolverParams(Set<Type> apiTypes, Annotation... qualifiers)
    {
        if (apiTypes == null || apiTypes.size() == 0)
        {
            throw new IllegalArgumentException("Manager.resolveDecorators() method parameter api types argument " +
                    "can not be empty");
        }

        Annotation old = null;
        for (Annotation qualifier : qualifiers)
        {
            if (!isQualifierAnnotation(qualifier.annotationType()))
            {
                throw new IllegalArgumentException("Manager.resolveDecorators() method parameter qualifiers array " +
                        "can not contain other annotation that is not @Qualifier");
            }
            if (old == null)
            {
                old = qualifier;
            }
            else
            {
                if (old.annotationType().equals(qualifier.annotationType()))
                {
                    throw new IllegalArgumentException("Manager.resolveDecorators() method parameter qualifiers " +
                            "array argument can not define duplicate qualifier annotation with name : @" +
                            old.annotationType().getName());
                }

                old = qualifier;
            }
        }

    }


    /**
     * Check conditions for the new binding.
     * @param annotations annotations
     * @return Annotation[] with all binding annotations
     * @throws WebBeansConfigurationException if New plus any other binding annotation is set
     */
    public Annotation[] checkForNewQualifierForDeployment(Type type, Class<?> clazz, String name,
                                                                 Annotation[] annotations)
    {
        Asserts.assertNotNull(type, "Type argument can not be null");
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(annotations, "Annotations argument can not be null");

        Annotation[] as = getQualifierAnnotations(annotations);
        for (Annotation a : annotations)
        {
            if (a.annotationType().equals(New.class))
            {
                if (as.length > 1)
                {
                    throw new WebBeansConfigurationException("@New binding annotation can not have any binding "
                                                             + "annotation in class : " + clazz.getName()
                                                             + " in field/method : " + name);
                }
            }
        }

        return as;
    }

    /**
     * Configures the name of the producer method for specializing the parent.
     *
     * @param component producer method component
     * @param method specialized producer method
     * @param superMethod overriden super producer method
     */
    public boolean configuredProducerSpecializedName(AbstractOwbBean<?> component,
                                                            Method method,
                                                            Method superMethod)
    {
        Asserts.assertNotNull(component,"component parameter can not be null");
        Asserts.assertNotNull(method,"method parameter can not be null");
        Asserts.assertNotNull(superMethod,"superMethod parameter can not be null");

        String name = null;
        boolean hasName = false;
        if(AnnotationUtil.hasMethodAnnotation(superMethod, Named.class))
        {
            Named named =  superMethod.getAnnotation(Named.class);
            hasName = true;
            if(!named.value().equals(""))
            {
                name = named.value();
            }
            else
            {
                name = WebBeansUtil.getProducerDefaultName(superMethod.getName());
            }
        }
        else
        {
            Annotation[] anns = getStereotypeMetaAnnotations(superMethod.getAnnotations());
            for(Annotation ann : anns)
            {
                if(ann.annotationType().isAnnotationPresent(Stereotype.class))
                {
                    hasName = true;
                    name = WebBeansUtil.getProducerDefaultName(superMethod.getName());
                    break;
                }
            }
        }

        if(hasName)
        {
            if(AnnotationUtil.hasMethodAnnotation(method, Named.class))
            {
                throw new DefinitionException("Specialized method : " + method.getName() + " in class : "
                        + component.getReturnType().getName() + " may not define @Named annotation");
            }

            component.setName(name);
        }

        return hasName;
//        else
//        {
//            component.setName(name);
//        }

    }

    @SuppressWarnings("unchecked")
    public <X> Method getDisposalWithGivenAnnotatedMethod(AnnotatedType<X> annotatedType, Type beanType, Annotation[] qualifiers)
    {
        Set<AnnotatedMethod<? super X>> annotatedMethods = annotatedType.getMethods();

        if(annotatedMethods != null)
        {
            for (AnnotatedMethod<? super X> annotatedMethod : annotatedMethods)
            {
                AnnotatedMethod<X> annt = (AnnotatedMethod<X>)annotatedMethod;
                List<AnnotatedParameter<X>> parameters = annt.getParameters();
                if(parameters != null)
                {
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
                        Type type = AnnotationUtil.getAnnotatedMethodFirstParameterWithAnnotation(annotatedMethod, Disposes.class);
                        Annotation[] annots = getAnnotatedMethodFirstParameterQualifierWithGivenAnnotation(annotatedMethod, Disposes.class);

                        if(type.equals(beanType))
                        {
                            for(Annotation qualifier : qualifiers)
                            {
                                if(qualifier.annotationType() != Default.class)
                                {
                                    for(Annotation ann :annots)
                                    {
                                        if(!AnnotationUtil.isQualifierEqual(qualifier, ann))
                                        {
                                            return null;
                                        }
                                    }
                                }
                            }

                            return annotatedMethod.getJavaMember();
                        }
                    }
                }
            }
        }
        return null;

    }

    /**
     * JavaEE components can not inject {@link javax.enterprise.inject.spi.InjectionPoint}.
     * @param clazz javaee component class info
     * @throws WebBeansConfigurationException exception if condition is not applied
     */
    public void checkInjectionPointForInjectInjectionPoint(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        Field[] fields = webBeansContext.getSecurityService().doPrivilegedGetDeclaredFields(clazz);
        for(Field field : fields)
        {
            if(field.getAnnotation(Inject.class) != null)
            {
                if(field.getType() == InjectionPoint.class)
                {
                    Annotation[] anns = getQualifierAnnotations(field.getDeclaredAnnotations());
                    if (AnnotationUtil.hasAnnotation(anns, Default.class))
                    {
                        throw new WebBeansConfigurationException("Java EE Component class :  " + clazz + " can not inject InjectionPoint");
                    }
                }
            }
        }
    }

    /**
     * Returns trur for serializable types.
     * @param clazz class info
     * @return true if class is serializable
     */
    public boolean checkInjectionPointForInterceptorPassivation(Class<?> clazz)
    {
        Asserts.nullCheckForClass(clazz);
        Field[] fields = webBeansContext.getSecurityService().doPrivilegedGetDeclaredFields(clazz);
        for(Field field : fields)
        {
            if(field.getAnnotation(Inject.class) != null)
            {
                Class<?> type = field.getType();
                if(!Serializable.class.isAssignableFrom(type))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
