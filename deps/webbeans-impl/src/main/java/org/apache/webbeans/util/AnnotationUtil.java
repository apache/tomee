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

import org.apache.webbeans.exception.WebBeansException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility class related with {@link Annotation} operations.
 * 
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @since 1.0
 */
public final class AnnotationUtil
{
    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    // No instantiate
    private AnnotationUtil()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Check given annotation exist on the method.
     * 
     * @param method method
     * @param clazz annotation class
     * @return true or false
     */
    public static boolean hasMethodAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        final AnnotatedElement element = method;
        Annotation[] anns = getDeclaredAnnotations(element);
        for (Annotation annotation : anns)
        {
            if (annotation.annotationType().equals(clazz))
            {
                return true;
            }
        }

        return false;

    }

    /**
     * Utility method to get around some errors caused by
     * interactions between the Equinox class loaders and
     * the OpenJPA transformation process.  There is a window
     * where the OpenJPA transformation process can cause
     * an annotation being processed to get defined in a
     * classloader during the actual defineClass call for
     * that very class (e.g., recursively).  This results in
     * a LinkageError exception.  If we see one of these,
     * retry the request.  Since the annotation will be
     * defined on the second pass, this should succeed.  If
     * we get a second exception, then it's likely some
     * other problem.
     *
     * @param element The AnnotatedElement we need information for.
     *
     * @return An array of the Annotations defined on the element.
     */
    private static Annotation[] getDeclaredAnnotations(AnnotatedElement element)
    {
        try
        {
            return element.getDeclaredAnnotations();
        }
        catch (LinkageError e)
        {
            return element.getDeclaredAnnotations();
        }
    }


    /**
     * Check given annotation exist in the any parameter of the given method.
     * Return true if exist false otherwise.
     * 
     * @param method method
     * @param clazz checking annotation
     * @return true or false
     */
    public static boolean hasMethodParameterAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();

        for (Annotation[] parameters : parameterAnns)
        {
            for (Annotation param : parameters)
            {
                Class<? extends Annotation> btype = param.annotationType();
                if (btype.equals(clazz))
                {
                    return true;
                }
            }

        }
        return false;
    }
    
    public static <X> boolean hasAnnotatedMethodParameterAnnotation(AnnotatedMethod<X> annotatedMethod, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");
        Asserts.nullCheckForClass(clazz);

        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(clazz))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check given annotation exist in the multiple parameter of the given
     * method. Return true if exist false otherwise.
     * 
     * @param method method
     * @param clazz checking annotation
     * @return true or false
     */
    public static boolean hasMethodMultipleParameterAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();

        boolean found = false;

        for (Annotation[] parameters : parameterAnns)
        {
            for (Annotation param : parameters)
            {

                if (param.annotationType().equals(clazz))
                {
                    if (!found)
                    {
                        found = true;
                    }
                    else
                    {
                        return true;
                    }
                }
            }

        }
        return false;
    }
    
    public static <X> boolean hasAnnotatedMethodMultipleParameterAnnotation(AnnotatedMethod<X> annotatedMethod, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");
        Asserts.nullCheckForClass(clazz);

        boolean found = false;
        
        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(clazz))
            {
                if(!found)
                {
                    found = true;
                }
                else
                {
                    return true;   
                }                
            }
        }
        
        
        return false;
    }
    

    /**
     * Gets the method first found parameter type that is annotated with the
     * given annotation.
     * 
     * @param method method
     * @param clazz checking annotation
     * @return type
     */
    public static Type getMethodFirstParameterWithAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();
        Type[] params = method.getGenericParameterTypes();

        int index = 0;
        for (Annotation[] parameters : parameterAnns)
        {
            for (Annotation param : parameters)
            {
                Class<? extends Annotation> btype = param.annotationType();
                if (btype.equals(clazz))
                {
                    return params[index];
                }
            }

            index++;

        }
        return null;
    }
    
    public static <X> Type getAnnotatedMethodFirstParameterWithAnnotation(AnnotatedMethod<X> annotatedMethod, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");
        Asserts.nullCheckForClass(clazz);

        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(clazz))
            {
                return parameter.getBaseType();
            }
        }
        
        return null;
    }

    /**
     * Get the Type of the method parameter which has the given annotation
     * @param method which need to be scanned
     * @param clazz the annotation to scan the method parameters for
     * @return the Type of the method parameter which has the given annotation, or <code>null</code> if not found.
     */
    public static Type getTypeOfParameterWithGivenAnnotation(Method method, Class<? extends Annotation> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();
        Type result = null;

        int index = 0;
        for (Annotation[] parameters : parameterAnns)
        {
            boolean found = false;
            for (Annotation param : parameters)
            {
                Class<? extends Annotation> btype = param.annotationType();
                if (btype.equals(clazz))
                {
                    found = true;
                    //Adding Break instead of continue
                    break;
                }
            }

            if (found)
            {
                result = method.getGenericParameterTypes()[index];
                break;
            }

            index++;

        }
        return result;
    }

    /**
     * Gets the method first found parameter annotation with given type.
     * 
     * @param method method
     * @param clazz checking annotation
     * @return annotation
     */
    public static <T extends Annotation> T getMethodFirstParameterAnnotation(Method method, Class<T> clazz)
    {
        Asserts.assertNotNull(method, "Method argument can not be null");
        Asserts.nullCheckForClass(clazz);

        Annotation[][] parameterAnns = method.getParameterAnnotations();

        for (Annotation[] parameters : parameterAnns)
        {
            for (Annotation param : parameters)
            {
                Class<? extends Annotation> btype = param.annotationType();
                if (btype.equals(clazz))
                {
                    return clazz.cast(param);
                }

            }

        }

        return null;
    }    
    
    public static <X,T extends Annotation> T getAnnotatedMethodFirstParameterAnnotation(AnnotatedMethod<X> annotatedMethod, Class<T> clazz)
    {
        Asserts.assertNotNull(annotatedMethod, "annotatedMethod argument can not be null");
        Asserts.nullCheckForClass(clazz);
        
        
        List<AnnotatedParameter<X>> parameters = annotatedMethod.getParameters();
        for(AnnotatedParameter<X> parameter : parameters)
        {
            if(parameter.isAnnotationPresent(clazz))
            {
                return clazz.cast(parameter.getAnnotation(clazz));
            }
        }
        
        return null;
    }    

    /**
     * Checks if the given qualifiers are equal.
     *
     * Qualifiers are equal if they have the same annotationType and all their
     * methods, except those annotated with @Nonbinding, return the same value.
     *
     * @param qualifier1
     * @param qualifier2
     * @return 
     */
    public static boolean isQualifierEqual(Annotation qualifier1, Annotation qualifier2)
    {
        Asserts.assertNotNull(qualifier1, "qualifier1 argument can not be null");
        Asserts.assertNotNull(qualifier2, "qualifier2 argument can not be null");

        Class<? extends Annotation> qualifier1AnnotationType
                = qualifier1.annotationType();

        // check if the annotationTypes are equal
        if (qualifier1AnnotationType == null
            || !qualifier1AnnotationType.equals(qualifier2.annotationType()))
        {
            return false;
        }

        // check the values of all qualifier-methods
        // except those annotated with @Nonbinding
        List<Method> bindingQualifierMethods
                = getBindingQualifierMethods(qualifier1AnnotationType);

        for (int i = 0, size = bindingQualifierMethods.size(); i < size; i++)
        {
            Method method = bindingQualifierMethods.get(i);
            Object value1 = callMethod(qualifier1, method);
            Object value2 = callMethod(qualifier2, method);

            if (!checkEquality(value1, value2))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Quecks if the two values are equal.
     *
     * @param value1
     * @param value2
     * @return
     */
    private static boolean checkEquality(Object value1, Object value2)
    {
        if ((value1 == null && value2 != null) ||
            (value1 != null && value2 == null))
        {
            return false;
        }

        if (value1 == null && value2 == null)
        {
            return true;
        }

        // now both values are != null

        Class<?> valueClass = value1.getClass();

        if (!valueClass.equals(value2.getClass()))
        {
            return false;
        }

        if (valueClass.isPrimitive())
        {
            // primitive types can be checked with ==
            return value1 == value2;
        }
        else if (valueClass.isArray())
        {
            Class<?> arrayType = valueClass.getComponentType();

            if (arrayType.isPrimitive())
            {
                if (Long.TYPE == arrayType)
                {
                    return Arrays.equals(((long[]) value1), (long[]) value2);
                }
                else if (Integer.TYPE == arrayType)
                {
                    return Arrays.equals(((int[]) value1), (int[]) value2);
                }
                else if (Short.TYPE == arrayType)
                {
                    return Arrays.equals(((short[]) value1), (short[]) value2);
                }
                else if (Double.TYPE == arrayType)
                {
                    return Arrays.equals(((double[]) value1), (double[]) value2);
                }
                else if (Float.TYPE == arrayType)
                {
                    return Arrays.equals(((float[]) value1), (float[]) value2);
                }
                else if (Boolean.TYPE == arrayType)
                {
                    return Arrays.equals(((boolean[]) value1), (boolean[]) value2);
                }
                else if (Byte.TYPE == arrayType)
                {
                    return Arrays.equals(((byte[]) value1), (byte[]) value2);
                }
                else if (Character.TYPE == arrayType)
                {
                    return Arrays.equals(((char[]) value1), (char[]) value2);
                }
                return false;
            }
            else
            {
                return Arrays.equals(((Object[]) value1), (Object[]) value2);
            }
        }
        else
        {
            return value1.equals(value2);
        }
    }

    /**
     * Calls the given method on the given instance.
     * Used to determine the values of annotation instances.
     *
     * @param instance
     * @param method
     * @return
     */
    private static Object callMethod(Object instance, Method method)
    {
        try
        {
            if (!method.isAccessible())
            {
                method.setAccessible(true);
            }

            return method.invoke(instance, EMPTY_OBJECT_ARRAY);
        }
        catch (Exception e)
        {
            throw new WebBeansException("Exception in method call : " + method.getName(), e);
        }
    }

    /**
     * Return a List of all methods of the qualifier,
     * which are not annotated with @Nonbinding.
     *
     * @param qualifierAnnotationType
     */
    private static List<Method> getBindingQualifierMethods(
            Class<? extends Annotation> qualifierAnnotationType)
    {
        Method[] qualifierMethods = qualifierAnnotationType.getDeclaredMethods();

        if (qualifierMethods.length > 0)
        {
            List<Method> bindingMethods = new ArrayList<Method>();

            for (Method qualifierMethod : qualifierMethods)
            {
                Annotation[] qualifierMethodAnnotations = getDeclaredAnnotations(qualifierMethod);

                if (qualifierMethodAnnotations.length > 0)
                {
                    // look for @Nonbinding
                    boolean nonbinding = false;

                    for (Annotation qualifierMethodAnnotation : qualifierMethodAnnotations)
                    {
                        if (Nonbinding.class.equals(
                                qualifierMethodAnnotation.annotationType()))
                        {
                            nonbinding = true;
                            break;
                        }
                    }

                    if (!nonbinding)
                    {
                        // no @Nonbinding found - add to list
                        bindingMethods.add(qualifierMethod);
                    }
                }
                else
                {
                    // no method-annotations - add to list
                    bindingMethods.add(qualifierMethod);
                }
            }

            return bindingMethods;
        }

        // annotation has no methods
        return Collections.emptyList();
    }


    /**
     * Gets array of methods that has parameter with given annotation type.
     * 
     * @param clazz class for check
     * @param annotation for check
     * @return array of methods
     */
    public static Method[] getMethodsWithParameterAnnotation(Class<?> clazz, Class<? extends Annotation> annotation)
    {
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(annotation, "Annotation argument can not be null");
        List<Method> list = new ArrayList<Method>();

        do
        {
            Method[] methods = SecurityUtil.doPrivilegedGetDeclaredMethods(clazz);

            for (Method m : methods)
            {
                if (hasMethodParameterAnnotation(m, annotation))
                {
                    list.add(m);
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null && clazz != Object.class);

        Method[] rMethod = list.toArray(new Method[list.size()]);

        return rMethod;
    }


    /**
     * Check whether or not class contains the given annotation.
     * 
     * @param clazz class instance
     * @param annotation annotation class
     * @return return true or false
     */
    public static boolean hasClassAnnotation(Class<?> clazz, Class<? extends Annotation> annotation)
    {
        Asserts.nullCheckForClass(clazz);
        Asserts.assertNotNull(annotation, "Annotation argument can not be null");

        try
        {
            Annotation a = clazz.getAnnotation(annotation);

            if (a != null)
            {
                return true;
            }
        }
        catch (ArrayStoreException e)
        {
            //log this?  It is probably already logged in AnnotatedElementFactory
        }

        return false;
    }

    public static boolean hasMetaAnnotation(Annotation[] anns, Class<? extends Annotation> metaAnnotation)
    {
        Asserts.assertNotNull(anns, "Anns argument can not be null");
        Asserts.assertNotNull(metaAnnotation, "MetaAnnotation argument can not be null");

        for (Annotation annot : anns)
        {
            if (annot.annotationType().isAnnotationPresent(metaAnnotation))
            {
                return true;
            }
        }

        return false;

    }

    public static boolean hasAnnotation(Annotation[] anns, Class<? extends Annotation> annotation)
    {
        return getAnnotation(anns, annotation) != null;
    }

    /**
     * get the annotation of the given type from the array. 
     * @param anns
     * @param annotation
     * @return the Annotation with the given type or <code>null</code> if no such found.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotation(Annotation[] anns, Class<T> annotation)
    {
        Asserts.assertNotNull(anns, "anns argument can not be null");
        Asserts.assertNotNull(annotation, "annotation argument can not be null");
        for (Annotation annot : anns)
        {
            if (annot.annotationType().equals(annotation))
            {
                return (T)annot;
            }
        }

        return null;
    }

    /**
     * Returns a subset of annotations that are annotated with the specified meta-annotation
     * 
     * @param anns
     * @param metaAnnotation
     * @return
     */
    public static Annotation[] getMetaAnnotations(Annotation[] anns, Class<? extends Annotation> metaAnnotation)
    {
        List<Annotation> annots = new ArrayList<Annotation>();
        Annotation[] result;
        Asserts.assertNotNull(anns, "Anns argument can not be null");
        Asserts.assertNotNull(metaAnnotation, "MetaAnnotation argument can not be null");

        for (Annotation annot : anns)
        {
            if (annot.annotationType().isAnnotationPresent(metaAnnotation))
            {
                annots.add(annot);
            }
        }

        result = new Annotation[annots.size()];
        result = annots.toArray(result);

        return result;
    }


    /**
     * Returns true if any binding exist
     * 
     * @param bean bean
     * @return true if any binding exist
     */
    public static boolean hasAnyQualifier(Bean<?> bean)
    {
        Asserts.assertNotNull(bean, "bean parameter can not be null");
        Set<Annotation> qualifiers = bean.getQualifiers();

        return getAnnotation(qualifiers, Any.class) != null;
    }

    /**
     * Search in the given Set of Annotations for the one with the given AnnotationClass.  
     * @param annotations to scan
     * @param annotationClass to search for
     * @return the annotation with the given annotationClass or <code>null</code> if not found.
     */
    public static <T extends Annotation> T getAnnotation(Set<Annotation> annotations, Class<T> annotationClass)
    {
        if (annotations == null) 
        {
            return null;
        }
            
        for(Annotation ann : annotations)
        {
            if(ann.annotationType().equals(annotationClass))
            {
                return (T) ann;
            }
        }
        
        return null;
    }

    
    public static Annotation hasOwbInjectableResource(Annotation[] annotations)
    {
        for (Annotation anno : annotations)
        {
            for(String name : WebBeansConstants.OWB_INJECTABLE_RESOURCE_ANNOTATIONS)
            {
                if(anno.annotationType().getName().equals(name))
                {
                    return anno;
                }
            }
        }        
        
        return null;        
    }

    public static Annotation[] getAnnotationsFromSet(Set<Annotation> set)
    {
        if(set != null)
        {
            Annotation[] anns = new Annotation[set.size()];
            anns = set.toArray(anns);
            
            return anns;
        }
        
        return EMPTY_ANNOTATION_ARRAY;
    }
}
