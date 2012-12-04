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
package org.apache.webbeans.inject.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.decorator.Delegate;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

import org.apache.webbeans.annotation.NamedLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.Asserts;

public class InjectionPointFactory
{
    private final WebBeansContext webBeansContext;

    public InjectionPointFactory(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    public InjectionPoint getFieldInjectionPointData(Bean<?> owner, Field member)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(member, "member parameter can not be null");

        Annotation[] annots = null;
        annots = member.getAnnotations();

        AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();

        AnnotatedType<?> annotated = annotatedElementFactory.newAnnotatedType(member.getDeclaringClass());
        return getGenericInjectionPoint(owner, annots, member.getGenericType(), member, annotatedElementFactory.newAnnotatedField(member, annotated));
    }

    public <X> InjectionPoint getFieldInjectionPointData(Bean<?> owner, AnnotatedField<X> annotField)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(annotField, "annotField parameter can not be null");
        Field member = annotField.getJavaMember();

        Annotation[] annots = AnnotationUtil.getAnnotationsFromSet(annotField.getAnnotations());

        return getGenericInjectionPoint(owner, annots, annotField.getBaseType(), member, annotField);
    }

    /**
     * Gets injected point instance.
     * @param owner owner of the injection point
     * @param annots annotations of the injection point
     * @param type type of the injection point
     * @param member member of the injection point
     * @param annotated annotated instance of injection point
     * @return injection point instance
     */
    private InjectionPoint getGenericInjectionPoint(Bean<?> owner, Annotation[] annots, Type type, Member member,Annotated annotated)
    {
        InjectionPointImpl injectionPoint;

        Annotation[] qualifierAnnots = webBeansContext.getAnnotationManager().getQualifierAnnotations(annots);

        //@Named update for injection fields!
        if(member instanceof Field)
        {
            for(int i=0; i < qualifierAnnots.length; i++)
            {
                Annotation qualifier = qualifierAnnots[i];
                if(qualifier.annotationType().equals(Named.class))
                {
                    Named named = (Named)qualifier;
                    String value = named.value();

                    if(value == null || value.equals(""))
                    {
                        NamedLiteral namedLiteral = new NamedLiteral();
                        namedLiteral.setValue(member.getName());
                        qualifierAnnots[i] = namedLiteral;
                    }

                    break;
                }
            }
        }


        injectionPoint = new InjectionPointImpl(owner, type, member, annotated);

        if(AnnotationUtil.hasAnnotation(annots, Delegate.class))
        {
            injectionPoint.setDelegate(true);
        }

        if(Modifier.isTransient(member.getModifiers()))
        {
            injectionPoint.setTransient(true);
        }

        addAnnotation(injectionPoint, qualifierAnnots, true);

        return injectionPoint;

    }

    @SuppressWarnings("unchecked")
    public List<InjectionPoint> getMethodInjectionPointData(Bean<?> owner, Method member)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(member, "member parameter can not be null");

        List<InjectionPoint> lists = new ArrayList<InjectionPoint>();

        AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();
        AnnotatedType<?> annotated = annotatedElementFactory.newAnnotatedType(member.getDeclaringClass());
        AnnotatedMethod method = annotatedElementFactory.newAnnotatedMethod(member, annotated);
        List<AnnotatedParameter<?>> parameters = method.getParameters();

        InjectionPoint point = null;

        for(AnnotatedParameter<?> parameter : parameters)
        {
            //@Observes is not injection point type for method parameters
            if(parameter.getAnnotation(Observes.class) == null)
            {
                point = getGenericInjectionPoint(owner, parameter.getAnnotations().toArray(new Annotation[parameter.getAnnotations().size()]),
                                                 parameter.getBaseType(), member , parameter);
                lists.add(point);
            }
        }

        return lists;
    }

    public <X> List<InjectionPoint> getMethodInjectionPointData(Bean<?> owner, AnnotatedMethod<X> method)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(method, "method parameter can not be null");

        List<InjectionPoint> lists = new ArrayList<InjectionPoint>();

        List<AnnotatedParameter<X>> parameters = method.getParameters();

        InjectionPoint point = null;

        for(AnnotatedParameter<?> parameter : parameters)
        {
            //@Observes is not injection point type for method parameters
            if(parameter.getAnnotation(Observes.class) == null)
            {
                point = getGenericInjectionPoint(owner, parameter.getAnnotations().toArray(new Annotation[parameter.getAnnotations().size()]),
                                                 parameter.getBaseType(), method.getJavaMember() , parameter);
                lists.add(point);
            }
        }

        return lists;
    }

    public static InjectionPoint getPartialInjectionPoint(Bean<?> owner,Type type, Member member, Annotated annotated, Annotation...bindings)
    {
        InjectionPointImpl impl = new InjectionPointImpl(owner,type,member,annotated);


        for(Annotation annot : bindings)
        {
            impl.addBindingAnnotation(annot);
        }

        return impl;

    }

    public <T> List<InjectionPoint> getConstructorInjectionPointData(Bean<T> owner, AnnotatedConstructor<T> constructor)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(constructor, "constructor parameter can not be null");

        List<InjectionPoint> lists = new ArrayList<InjectionPoint>();

        List<AnnotatedParameter<T>> parameters = constructor.getParameters();

        InjectionPoint point = null;

        for(AnnotatedParameter<?> parameter : parameters)
        {
            point = getGenericInjectionPoint(owner, parameter.getAnnotations().toArray(new Annotation[parameter.getAnnotations().size()]),
                                             parameter.getBaseType(), constructor.getJavaMember() , parameter);
            lists.add(point);
        }

        return lists;
    }


    @SuppressWarnings("unchecked")
    public List<InjectionPoint> getConstructorInjectionPointData(Bean<?> owner, Constructor<?> member)
    {
        Asserts.assertNotNull(owner, "owner parameter can not be null");
        Asserts.assertNotNull(member, "member parameter can not be null");

        List<InjectionPoint> lists = new ArrayList<InjectionPoint>();

        AnnotatedType<Object> annotated = (AnnotatedType<Object>) webBeansContext.getAnnotatedElementFactory().newAnnotatedType(member.getDeclaringClass());
        AnnotatedConstructor constructor = webBeansContext.getAnnotatedElementFactory().newAnnotatedConstructor((Constructor<Object>)member,annotated);
        List<AnnotatedParameter<?>> parameters = constructor.getParameters();

        InjectionPoint point = null;

        for(AnnotatedParameter<?> parameter : parameters)
        {
            point = getGenericInjectionPoint(owner, parameter.getAnnotations().toArray(new Annotation[parameter.getAnnotations().size()]),
                                             parameter.getBaseType(), member , parameter);
            lists.add(point);
        }

        return lists;
    }

    private static void addAnnotation(InjectionPointImpl impl, Annotation[] annots, boolean isBinding)
    {
        for (Annotation ann : annots)
        {
            if (isBinding)
            {
                impl.addBindingAnnotation(ann);
            }
        }
    }

}
