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
package org.apache.webbeans.newtests.portable.events.extensions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;

import org.apache.webbeans.annotation.DependentScopeLiteral;

public class AddBeanExtension implements Extension
{
    public static class MyBean
    {
        
    }
    
    public static class MyBeanExtension implements Extension
    {
        public static Bean<MyBean> myBean;
        
        public void observer(@Observes ProcessBean<MyBean> event)
        {
            myBean = event.getBean();
            
        }
    }
    
    public void observer(@Observes AfterBeanDiscovery event)
    {
        event.addBean(new Bean<MyBean>(){

            @Override
            public Class<?> getBeanClass()
            {
                return MyBean.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getName()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<Annotation> getQualifiers()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Class<? extends Annotation> getScope()
            {
                return new DependentScopeLiteral().annotationType();
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<Type> getTypes()
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isAlternative()
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isNullable()
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public MyBean create(CreationalContext<MyBean> context)
            {
                return new MyBean();
            }

            @Override
            public void destroy(MyBean instance, CreationalContext<MyBean> context)
            {
                // TODO Auto-generated method stub
                
            }
            
        });
    }

}
