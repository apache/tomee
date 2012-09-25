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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A small helper class to create a Annotation instance of the given annotation class
 * via {@link java.lang.reflect.Proxy}. 
 * The annotation literal gets filled with the default values.
 * TODO implement class caching!
 */
public class DefaultAnnotation implements InvocationHandler, Annotation
{
    
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static Annotation of(Class<? extends Annotation> annotation) 
    {
        return (Annotation) Proxy.newProxyInstance(annotation.getClassLoader(),
                new Class[] {annotation}, new DefaultAnnotation(annotation));
    }

    private final Class<? extends Annotation> annotationClass;

    private DefaultAnnotation(Class<? extends Annotation> annotationClass)
    {
        this.annotationClass = annotationClass;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if ("hashCode".equals(method.getName()))
        {
            return hashCode();
        }
        else if ("equals".equals(method.getName()))
        {
            return equals(args[0]);
        }
        else if ("annotationType".equals(method.getName()))
        {
            return annotationType();
        }
        else if ("toString".equals(method.getName()))
        {
            return toString();
        }

        return method.getDefaultValue();
    }

    public Class<? extends Annotation> annotationType()
    {
        return annotationClass;
    }

    /**
     * Copied from javax.enterprise.util.AnnotationLiteral#toString()
     * with minor changes.
     *
     * @return
     */
    @Override
    public String toString()
    {
        Method[] methods = annotationClass.getDeclaredMethods();

        StringBuilder sb = new StringBuilder("@" + annotationType().getName() + "(");
        int lenght = methods.length;

        for (int i = 0; i < lenght; i++)
        {
            // Member name
            sb.append(methods[i].getName()).append("=");

            // Member value
            Object memberValue;
            try
            {
                memberValue = invoke(this, methods[i], EMPTY_OBJECT_ARRAY);
            }
            catch (Throwable throwable)
            {
                memberValue = "";
            }
            sb.append(memberValue);

            if (i < lenght - 1)
            {
                sb.append(",");
            }
        }

        sb.append(")");

        return sb.toString();
    }
    
}
