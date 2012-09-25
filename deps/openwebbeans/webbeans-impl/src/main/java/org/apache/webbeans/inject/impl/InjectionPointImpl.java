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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.AnnotatedElementFactory;
import org.apache.webbeans.util.WebBeansUtil;

class InjectionPointImpl implements InjectionPoint, Serializable
{
    private static final long serialVersionUID = 1047233127758068484L;

    private Set<Annotation> qualifierAnnotations = new HashSet<Annotation>();
    
    private Bean<?> ownerBean;
    
    private Member injectionMember;
    
    private Type injectionType;
    
    private Annotated annotated;
    
    private boolean transientt;
    
    private boolean delegate;

    InjectionPointImpl(Bean<?> ownerBean, Type type, Member member, Annotated annotated)
    {
        this.ownerBean = ownerBean;
        injectionMember = member;
        injectionType = type;
        this.annotated = annotated;
    }
    
    void addBindingAnnotation(Annotation qualifierAnnotations)
    {
        this.qualifierAnnotations.add(qualifierAnnotations);
    }
    
    public Bean<?> getBean()
    {
        
        return ownerBean;
    }

    public Set<Annotation> getQualifiers()
    {
        
        return qualifierAnnotations;
    }

    public Member getMember()
    {
        return injectionMember;
    }

    public Type getType()
    {
        
        return injectionType;
    }

    
    public Annotated getAnnotated()
    {
        return annotated;
    }

    public boolean isDelegate()
    {
        return delegate;
    }

    public boolean isTransient()
    {
        return transientt;
    }
    
    void setDelegate(boolean delegate)
    {
        this.delegate = delegate;
    }
    
    void setTransient(boolean transientt)
    {
        this.transientt = transientt;
    }
    
    private void writeObject(java.io.ObjectOutputStream op) throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(op);

        //Write the owning bean class
        out.writeObject(ownerBean.getBeanClass());

        Set<Annotation> qualifiers = ownerBean.getQualifiers();
        for(Annotation qualifier : qualifiers)
        {
            out.writeObject(Character.valueOf('-')); // throw-away delimiter so alternating annotations don't get swallowed in the read.
            out.writeObject(qualifier);
            
        }
        
        out.writeObject(Character.valueOf('~'));
        
        if(injectionMember instanceof Field)
        {
            out.writeByte(0);
            out.writeUTF(injectionMember.getName());
        }
        
        if(injectionMember instanceof Method)
        {
            out.writeByte(1);
            out.writeUTF(injectionMember.getName());
            Method method = (Method) injectionMember;
            Class<?>[] parameters = method.getParameterTypes();
            out.writeObject(parameters);
            
            AnnotatedParameter<?> ap = (AnnotatedParameter<?>) annotated;
            out.writeByte(ap.getPosition());
            
        }
        
        if(injectionMember instanceof Constructor)
        {
            out.writeByte(2);
            Constructor<?> constr = (Constructor<?>) injectionMember;
            Class<?>[] parameters = constr.getParameterTypes();
            out.writeObject(parameters);
            
            AnnotatedParameter<?> ap = (AnnotatedParameter<?>) annotated;
            out.writeByte(ap.getPosition());
            
        }
        
        out.writeBoolean(delegate);
        out.writeBoolean(transientt);
        out.flush();
        
    }
    
    public class CustomObjectInputStream extends ObjectInputStream
    {
        private ClassLoader classLoader;

        public CustomObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException
        {
            super(in);
            this.classLoader = classLoader;
        }
        
        protected Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException
        {
            return Class.forName(desc.getName(), false, classLoader);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream inp) throws IOException, ClassNotFoundException
    {

        ObjectInputStream in = new CustomObjectInputStream(inp, WebBeansUtil.getCurrentClassLoader());

        Class<?> beanClass = (Class<?>)in.readObject();
        Set<Annotation> anns = new HashSet<Annotation>();
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        AnnotatedElementFactory annotatedElementFactory = webBeansContext.getAnnotatedElementFactory();

        while(!in.readObject().equals('~'))   // read throw-away '-' or '~' terminal delimiter.
        {
            Annotation ann = (Annotation) in.readObject();  // now read the annotation.
            anns.add(ann);
        }
        
        //process annotations
        ownerBean = webBeansContext.getBeanManagerImpl().getBeans(beanClass, anns.toArray(new Annotation[anns.size()])).iterator().next();
        qualifierAnnotations = anns;
        
        // determine type of injection point member (0=field, 1=method, 2=constructor) and read...
        int c = in.readByte();
        if(c == 0)
        {
            String fieldName = in.readUTF();
            Field field = webBeansContext.getSecurityService().doPrivilegedGetDeclaredField(beanClass, fieldName);

            injectionMember = field;
            
            AnnotatedType<?> annotatedType = annotatedElementFactory.newAnnotatedType(beanClass);
            annotated = annotatedElementFactory.newAnnotatedField(field, annotatedType);
            injectionType = field.getGenericType();
            
        }
        else if(c == 1)
        {
            String methodName = in.readUTF();
            Class<?>[] parameters = (Class<?>[])in.readObject();
            
            Method method = webBeansContext.getSecurityService().doPrivilegedGetDeclaredMethod(beanClass, methodName, parameters);
            injectionMember = method;
            
            AnnotatedType<?> annotatedType = annotatedElementFactory.newAnnotatedType(beanClass);
            AnnotatedMethod<Object> am =  (AnnotatedMethod<Object>)annotatedElementFactory.
                                    newAnnotatedMethod((Method) injectionMember,annotatedType);
            List<AnnotatedParameter<Object>> annParameters = am.getParameters();

            annotated = annParameters.get(in.readByte());
            injectionType = annotated.getBaseType();
            
        }
        else if(c == 2)
        {
            Class<?>[] parameters = (Class<?>[])in.readObject();            
            try
            {
                injectionMember = beanClass.getConstructor(parameters);

            }
            catch(NoSuchMethodException e)
            {
                injectionMember = null;
            }

            AnnotatedType<Object> annotatedType = (AnnotatedType<Object>)annotatedElementFactory.newAnnotatedType(beanClass);
            AnnotatedConstructor<Object> am =  annotatedElementFactory
                                            .newAnnotatedConstructor((Constructor<Object>) injectionMember,annotatedType);
            List<AnnotatedParameter<Object>> annParameters = am.getParameters();

            annotated = annParameters.get(in.readByte());
            injectionType = annotated.getBaseType();
        }

        delegate = in.readBoolean();
        transientt = in.readBoolean();
         
    }


    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        if(injectionMember instanceof Constructor)
        {
            Constructor<?> constructor = (Constructor<?>) injectionMember;
            buffer.append("Constructor Injection Point, constructor name :  " + constructor.getName() + ", Bean Owner : ["+ ownerBean.toString() + "]");
        }
        else if(injectionMember instanceof Method)
        {
            Method method = (Method) injectionMember;
            buffer.append("Method Injection Point, method name :  " + method.getName() + ", Bean Owner : ["+ ownerBean.toString() + "]");
            
        }
        else if(injectionMember instanceof Field)
        {
            Field field = (Field) injectionMember;
            buffer.append("Field Injection Point, field name :  " + field.getName() + ", Bean Owner : ["+ ownerBean.toString() + "]");            
        }
        
        return buffer.toString();
    }
}
