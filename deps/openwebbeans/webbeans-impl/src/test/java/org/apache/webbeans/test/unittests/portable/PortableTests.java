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
package org.apache.webbeans.test.unittests.portable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptor;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.Binding2;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.inject.parametrized.Dao;
import org.apache.webbeans.test.component.library.Book;
import org.apache.webbeans.test.component.portable.PortableType1;
import org.apache.webbeans.test.xml.annot.BindingType1;
import org.apache.webbeans.test.xml.annot.BindingType2;
import org.junit.Test;

public class PortableTests extends TestContext
{
    public PortableTests()
    {
        super(PortableTests.class.getName());
    }

    @Test
    public void testAnnotatedType()
    {
        AnnotatedType<PortableType1> type = WebBeansContext.getInstance().getAnnotatedElementFactory().newAnnotatedType(PortableType1.class);
        
        Set<Annotation> annotations = type.getAnnotations();
        Set<Class<? extends Annotation>> clazzesAnnots = new HashSet<Class<? extends Annotation>>();
        
        for(Annotation ann : annotations)
        {
            clazzesAnnots.add(ann.annotationType());
        }
        
        Assert.assertTrue(clazzesAnnots.contains(Named.class));
        Assert.assertTrue(clazzesAnnots.contains(Default.class));
        Assert.assertTrue(clazzesAnnots.contains(Binding1.class));
        Assert.assertTrue(clazzesAnnots.contains(Binding2.class));
        Assert.assertTrue(clazzesAnnots.contains(Interceptor.class));
        
        
        
        Set<AnnotatedConstructor<PortableType1>> cs = type.getConstructors();        
        Assert.assertEquals(1, cs.size());        
        AnnotatedConstructor<PortableType1> c = cs.iterator().next();
        
        Assert.assertTrue(c.isAnnotationPresent(Inject.class));        
        Set<AnnotatedField<? super PortableType1>> fields = type.getFields();
        
        Assert.assertEquals(3, fields.size());
        
        for(AnnotatedField<? super PortableType1> field : fields)
        {
            if(field.getJavaMember().getName().equals("payment"))
            {
                Assert.assertTrue(field.isAnnotationPresent(Default.class));
                Assert.assertEquals(IPayment.class, field.getBaseType());
            }
            else if(field.getJavaMember().getName().equals("book"))
            {
                Assert.assertTrue(field.isAnnotationPresent(Default.class));
                Assert.assertTrue(field.isAnnotationPresent(Binding2.class));
                Assert.assertEquals(Book.class, field.getBaseType());                
            }
            else
            {
                Assert.assertTrue(field.isAnnotationPresent(Produces.class));
                Assert.assertTrue(field.isAnnotationPresent(BindingType2.class));
                Assert.assertEquals(CheckWithCheckPayment.class, field.getBaseType());                                
            }
        }
        
        Set<AnnotatedMethod<? super PortableType1>> methods = type.getMethods();
        
        Assert.assertEquals(8, methods.size());
        
        for(AnnotatedMethod<? super PortableType1> method   : methods)
        {
            if(method.getJavaMember().getName().equals("getDao"))
            {
                Assert.assertTrue(method.isAnnotationPresent(Produces.class));
                Assert.assertEquals(Dao.class, ((ParameterizedType)method.getBaseType()).getRawType());
                
                List<?> list = method.getParameters();
                Assert.assertTrue(list.size() == 1);
                AnnotatedParameter<?> param = (AnnotatedParameter<?>)list.iterator().next();
                Assert.assertTrue(param.getAnnotations().size() == 1);
                Assert.assertTrue(param.isAnnotationPresent(BindingType1.class));
            }
            else if(method.getJavaMember().getName().equals("notify"))
            {
                Assert.assertTrue(method.getAnnotations().isEmpty());
                Assert.assertEquals(void.class, method.getBaseType());  
                
                List<?> list = method.getParameters();
                Assert.assertTrue(list.size() == 1);
                AnnotatedParameter<?> param = (AnnotatedParameter<?>)list.iterator().next();
                Assert.assertTrue(param.getAnnotations().size() == 2);
                Assert.assertTrue(param.isAnnotationPresent(Binding2.class));
                Assert.assertTrue(param.isAnnotationPresent(Observes.class));
                
            }
        }
        
        
        
    }
}
