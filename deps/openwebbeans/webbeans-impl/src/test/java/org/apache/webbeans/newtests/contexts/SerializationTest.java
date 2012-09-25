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
package org.apache.webbeans.newtests.contexts;


import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.SerializableBean;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.contexts.serialize.AppScopedBean;
import org.apache.webbeans.newtests.contexts.serialize.SessScopedBean;
import org.apache.webbeans.newtests.contexts.session.common.PersonalDataBean;
import org.apache.webbeans.newtests.decorators.multiple.Decorator1;
import org.apache.webbeans.newtests.decorators.multiple.OutputProvider;
import org.apache.webbeans.newtests.decorators.multiple.RequestStringBuilder;
import org.apache.webbeans.newtests.injection.circular.beans.CircularApplicationScopedBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularConstructorOrProducerMethodParameterBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularDependentScopedBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularNormalInConstructor;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves1;
import org.apache.webbeans.test.component.event.normal.ComponentWithObserves2;
import org.apache.webbeans.test.component.event.normal.TransactionalInterceptor;
import org.apache.webbeans.util.WebBeansUtil;

import junit.framework.Assert;
import org.junit.Test;

import javassist.util.proxy.ProxyObject;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


/**
 *  Tests for various serialization issues
 */
public class SerializationTest extends AbstractUnitTest
{

    @SuppressWarnings("unchecked")
    @Test
    public void testCreationalContextSerialization() throws Exception
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();

        // add a few random classes
        classes.add(PersonalDataBean.class);
        classes.add(CircularDependentScopedBean.class);
        classes.add(CircularApplicationScopedBean.class);
        startContainer(classes);

        BeanManager bm = getBeanManager();
        Set<Bean<?>> beans = getBeanManager().getBeans(PersonalDataBean.class);
        Assert.assertNotNull(beans);
        Assert.assertTrue(beans.size() == 1);
        Bean pdbBean = beans.iterator().next();
        CreationalContext<PersonalDataBean> pdbCreational = bm.createCreationalContext(pdbBean);
        Assert.assertNotNull(pdbCreational);

        // oki, now let's serializeBean the CreationalContext
        byte[] serial = serializeObject(pdbCreational);
        CreationalContext<?> cc2 = (CreationalContext<?>) deSerializeObject(serial);
        Assert.assertNotNull(cc2);
    }

    @Test
    public void testPersonalDataBean() throws ClassNotFoundException, IOException
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();

        // add a few random classes
        classes.add(PersonalDataBean.class);
        classes.add(OutputProvider.class);
        classes.add(Decorator1.class);
        classes.add(CircularApplicationScopedBean.class);
        classes.add(CircularDependentScopedBean.class);
        classes.add(RequestStringBuilder.class);
        classes.add(CircularConstructorOrProducerMethodParameterBean.class);
        classes.add(CircularDependentScopedBean.class);
        classes.add(CircularNormalInConstructor.class);
        classes.add(TransactionalInterceptor.class);
        classes.add(ComponentWithObserves1.class);
        classes.add(ComponentWithObserves2.class);
        classes.add(PaymentProcessorComponent.class);
        classes.add(IPayment.class);
        classes.add(CheckWithCheckPayment.class);
        classes.add(CheckWithMoneyPayment.class);

        startContainer(classes);

        Set<Bean<?>> beans = getBeanManager().getBeans(Object.class);
        Assert.assertNotNull(beans);
        Assert.assertTrue(beans.size() > 7);

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        for (Bean<?> bean : beans)
        {
            String id = null;
            if((id = WebBeansUtil.isPassivationCapable(bean)) != null)
            {
                bean = (Bean<?>) webBeansContext.getSerializableBeanVault().getSerializableBean(bean);
                
                byte[] serial = serializeBean(bean);
                Bean<?> b2 = deSerializeBean(serial);

                Assert.assertEquals(((SerializableBean<?>)bean).getBean(), ((SerializableBean<?>)b2).getBean());
                
            }
        }
        
        // and now we are keen and try to serialize the whole passivatable Contexts!
        PersonalDataBean pdb = getInstance(PersonalDataBean.class);
        pdb.business();
        
        // first we need to actually create a few instances

        Context sessionContext = webBeansContext.getContextFactory().getStandardContext(SessionScoped.class);
        Assert.assertNotNull(sessionContext);
        byte[] ba = serializeObject(sessionContext);
        Assert.assertNotNull(ba);
        Context sessContext2 = (Context) deSerializeObject(ba);
        Assert.assertNotNull(sessContext2);
    }
    
    //X TODO this will work after JASSIST-97 got fixed @Test
    public void testProxySerialization() throws Exception
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();

        // add a few random classes
        classes.add(SessScopedBean.class);
        classes.add(AppScopedBean.class);

        startContainer(classes);

        Set<Bean<?>> beans = getBeanManager().getBeans(SessScopedBean.class);
        Assert.assertNotNull(beans);
        Assert.assertTrue(beans.size() == 1);
        
        @SuppressWarnings("unchecked")
        Bean<SessScopedBean> bean = (Bean<SessScopedBean>) beans.iterator().next();
        CreationalContext<SessScopedBean> ssbCreational = getBeanManager().createCreationalContext(bean);
        Assert.assertNotNull(ssbCreational);
        
        SessScopedBean reference = (SessScopedBean) getBeanManager().getReference(bean, SessScopedBean.class, ssbCreational);
        Assert.assertNotNull(reference);
        Assert.assertTrue(reference instanceof ProxyObject);
        
        reference.getApp().setI(4711);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(reference);
        byte[] ba = baos.toByteArray();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        ObjectInputStream ois = new ObjectInputStream(bais);
        SessScopedBean ssb2 =  (SessScopedBean) ois.readObject();
        Assert.assertNotNull(ssb2);
        
        Assert.assertNotNull(ssb2.getApp());
        Assert.assertTrue(ssb2.getApp().getI() == 4711);
    }

    private byte[] serializeBean(Bean<?> bean) throws IOException
    {
        return serializeObject(bean);
    }
    
    private byte[] serializeObject(Object o) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        return baos.toByteArray();
    }

    private Bean<?> deSerializeBean(byte[] serial) throws IOException, ClassNotFoundException
    {
        return (Bean<?>) deSerializeObject(serial);
    }
    
    private Object deSerializeObject(byte[] serial) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(serial);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

}
