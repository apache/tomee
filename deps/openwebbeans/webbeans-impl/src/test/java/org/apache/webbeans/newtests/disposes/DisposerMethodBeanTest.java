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
import org.apache.webbeans.newtests.disposes.beans.DisposeModel;
import org.apache.webbeans.newtests.disposes.beans.DisposerMethodBean;
import org.junit.Test;

public class DisposerMethodBeanTest extends AbstractUnitTest
{
    @Test
    @SuppressWarnings("unchecked")
    public void testDisposerMethod()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DisposerMethodBean.class);
        
        startContainer(beanClasses, beanXmls);        
        Bean<DisposeModel> bean = (Bean<DisposeModel>)getBeanManager().getBeans("produce").iterator().next();
         
        CreationalContext<DisposeModel> cc = getBeanManager().createCreationalContext(bean);
        DisposeModel model = (DisposeModel) getBeanManager().getReference(bean, DisposeModel.class, cc);
        bean.destroy(model, cc);
        
        Assert.assertTrue(DisposerMethodBean.OK);

        shutDownContainer();
    }

}
