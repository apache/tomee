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
package org.apache.webbeans.newtests.disposes;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import junit.framework.Assert;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.disposes.beans.AppScopedBean;
import org.apache.webbeans.newtests.disposes.beans.DependentProducer;
import org.apache.webbeans.newtests.disposes.beans.DisposerMethodBean;
import org.apache.webbeans.newtests.disposes.beans.InjectedIntoBean;
import org.apache.webbeans.newtests.disposes.beans.IntermediateDependentBean;
import org.apache.webbeans.newtests.disposes.beans.RequestBean;
import org.apache.webbeans.newtests.disposes.common.RequestModel;
import org.junit.Test;

public class DependentMethodBeanTest extends AbstractUnitTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void testDisposerMethod()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(AppScopedBean.class);
        beanClasses.add(RequestBean.class);
        
        startContainer(beanClasses, beanXmls);        

        Bean<RequestModel> bean = (Bean<RequestModel>)getBeanManager().getBeans("rproduce").iterator().next();
        CreationalContext<RequestModel> cc = getBeanManager().createCreationalContext(bean);
        RequestModel model = (RequestModel) getBeanManager().getReference(bean, RequestModel.class, cc);
        System.out.println(model.getID());
        
        shutDownContainer();
        
        Assert.assertTrue(AppScopedBean.OK);
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDisposerMethodWithIntermediateDependent()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DependentProducer.class);
        beanClasses.add(InjectedIntoBean.class);
        beanClasses.add(IntermediateDependentBean.class);
        
        startContainer(beanClasses, beanXmls);        
        Bean<InjectedIntoBean> bean = (Bean<InjectedIntoBean>)getBeanManager().getBeans("injectedIntoBean").iterator().next();
         
        CreationalContext<InjectedIntoBean> cc = getBeanManager().createCreationalContext(bean);
        
        InjectedIntoBean model = (InjectedIntoBean) getBeanManager().getReference(bean, InjectedIntoBean.class, cc);
        
        Assert.assertFalse(model.isBeanNull());
        
        shutDownContainer();
        
        //Disposer should only be called once
        Assert.assertEquals(1, DependentProducer.disposerCount); 
    } 

}
