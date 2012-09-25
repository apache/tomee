/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.interceptors.lifecycle;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.Interceptor;

import org.apache.webbeans.config.WebBeansContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InterceptorExtension implements Extension
{
    /**
     * we add the InterceptorBinding via Extension to test OWB-593
     * @param event
     */
    public void registerInterceptorBinding(@Observes BeforeBeanDiscovery event)
    {
        event.addInterceptorBinding(LifecycleBinding.class);
    }

    public void observeNotAnnotatedBean(@Observes ProcessAnnotatedType<NotAnnotatedBean> process)
    {
        process.getAnnotatedType().getAnnotations().add(new AnnotationLiteral<LifecycleBinding>(){});
        process.setAnnotatedType(process.getAnnotatedType());
    }

    public void observeLifecycleInterceptorPat(@Observes ProcessAnnotatedType<LifecycleInterceptorPat> process)
    {
        process.getAnnotatedType().getAnnotations().add(new AnnotationLiteral<LifecycleBinding>(){});
        process.getAnnotatedType().getAnnotations().add(new AnnotationLiteral<Interceptor>(){});
        process.setAnnotatedType(process.getAnnotatedType());
    }

    // manually add the correct LifecycleInterceptorBbd
    public void observeLiveCycleInterceptorBbd(@Observes BeforeBeanDiscovery bbd)
    {
        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        AnnotatedTypeImpl<LifecycleInterceptorBbd> annotatedType =
                new AnnotatedTypeImpl<LifecycleInterceptorBbd>(LifecycleInterceptorBbd.class );

        Set<Annotation> anns = new HashSet<Annotation>();
        anns.add(new AnnotationLiteral<LifecycleBinding>(){});
        anns.add(new AnnotationLiteral<Interceptor>(){});
        annotatedType.setAnnotations(anns);

        bbd.addAnnotatedType(annotatedType);
    }

    public static class AnnotatedTypeImpl<X> implements AnnotatedType<X>
    {
        private Class<X> javaClass;
        private Set<AnnotatedConstructor<X>>    annotatedConstructors = Collections.EMPTY_SET;
        private Set<AnnotatedMethod<? super X>> annotatedMethods = Collections.EMPTY_SET;
        private Set<AnnotatedField<? super X>>  annotatedFields = Collections.EMPTY_SET;
        private Set<Type>                       typeClosures  = Collections.EMPTY_SET;
        private Set<Annotation>                 annotations   = Collections.EMPTY_SET;


        public AnnotatedTypeImpl(Class<X> javaClass)
        {
            this.javaClass = javaClass;
        }

        public Set<AnnotatedConstructor<X>> getConstructors()
        {
            return annotatedConstructors;
        }

        public Class<X> getJavaClass()
        {
            return javaClass;
        }

        public Set<AnnotatedMethod<? super X>> getMethods()
        {
            return annotatedMethods;
        }

        public Set<AnnotatedField<? super X>> getFields()
        {
            return annotatedFields;
        }

        public Type getBaseType()
        {
            return javaClass;
        }

        public Set<Type> getTypeClosure()
        {
            return typeClosures;
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationType)
        {
            for (Annotation a: annotations)
            {
                if (a.annotationType().equals(annotationType))
                {
                    return (T) a;
                }
            }

            return null;
        }

        public Set<Annotation> getAnnotations()
        {
            return annotations;
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
        {
            return getAnnotation(annotationType) != null;
        }

        public void setAnnotatedConstructors(Set<AnnotatedConstructor<X>> annotatedConstructors)
        {
            this.annotatedConstructors = annotatedConstructors;
        }

        public void setAnnotatedFields(Set<AnnotatedField<? super X>> annotatedFields)
        {
            this.annotatedFields = annotatedFields;
        }

        public void setAnnotatedMethods(Set<AnnotatedMethod<? super X>> annotatedMethods)
        {
            this.annotatedMethods = annotatedMethods;
        }

        public void setAnnotations(Set<Annotation> annotations)
        {
            this.annotations = annotations;
        }

        public void setJavaClass(Class<X> javaClass)
        {
            this.javaClass = javaClass;
        }

        public void setTypeClosures(Set<Type> typeClosures)
        {
            this.typeClosures = typeClosures;
        }
    }

}
