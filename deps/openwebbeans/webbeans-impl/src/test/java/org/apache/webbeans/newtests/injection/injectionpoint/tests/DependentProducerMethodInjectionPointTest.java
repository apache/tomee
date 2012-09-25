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
package org.apache.webbeans.newtests.injection.injectionpoint.tests;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.injection.injectionpoint.beans.InjectionPointMetaDataOwner;
import org.apache.webbeans.newtests.injection.injectionpoint.beans.LoggerInjectedBean;
import org.junit.Test;

public class DependentProducerMethodInjectionPointTest extends AbstractUnitTest
{
    @SuppressWarnings("unchecked")
    @Test
    public void testDependentProducerMethodInjectionPoint() throws Exception
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(InjectionPointMetaDataOwner.class);
        beanClasses.add(LoggerInjectedBean.class);
        
        startContainer(beanClasses, beanXmls);    
        
        Bean<LoggerInjectedBean> bean = (Bean<LoggerInjectedBean>)getBeanManager().getBeans(LoggerInjectedBean.class.getName()).iterator().next();
        
        CreationalContext<LoggerInjectedBean> cc = getBeanManager().createCreationalContext(bean);
        LoggerInjectedBean model = (LoggerInjectedBean) getBeanManager().getReference(bean, LoggerInjectedBean.class, cc);
        
        Assert.assertNotNull(model.getLogger());

        Bean<InjectionPointMetaDataOwner> bean2 = (Bean<InjectionPointMetaDataOwner>)getBeanManager().getBeans(InjectionPointMetaDataOwner.class.getName()).iterator().next();
        
        CreationalContext<InjectionPointMetaDataOwner> cc2 = getBeanManager().createCreationalContext(bean2);
        InjectionPointMetaDataOwner model2 = (InjectionPointMetaDataOwner) getBeanManager().getReference(bean2, InjectionPointMetaDataOwner.class, cc2);
        
        InjectionPoint point = model2.getInjectionPoint();
        Assert.assertTrue(point.getBean().equals(bean));
        Assert.assertTrue(point.getMember() instanceof Field);

        point = (InjectionPoint) deserialize(serialize(point));

        Assert.assertTrue(point.getBean().equals(bean));
        Assert.assertTrue(point.getMember() instanceof Field);
        
        shutDownContainer();

    }

    private byte[] serialize(Object o) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    private Object deserialize(byte[] serial) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(serial);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }


}
