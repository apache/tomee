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
package org.apache.webbeans.test.unittests.inject.alternative;

import java.io.InputStream;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.inject.alternative.AlternativeComponent;
import org.apache.webbeans.test.component.inject.alternative.AlternativeInjector;
import org.apache.webbeans.test.component.inject.alternative.IAlternative;
import org.apache.webbeans.test.component.inject.alternative.NotAlternativeComponent;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;
import org.junit.Before;
import org.junit.Test;

public class AlternativeTest extends TestContext
{
    public AlternativeTest()
    {
        super(AlternativeTest.class.getName());
    }
    
    @Before
    public void setUp()
    {
        super.init();
    }
    
    @Test
    public void testInjectAlternative()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/alternative/alternatives.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternative.xml");
        
        defineManagedBean(AlternativeComponent.class);
        defineManagedBean(NotAlternativeComponent.class);
        
        Bean<AlternativeInjector> injector = defineManagedBean(AlternativeInjector.class);
        AlternativeInjector instance = (AlternativeInjector) getManager().getReference(injector, AlternativeInjector.class, getManager().createCreationalContext(injector));
        
        Assert.assertNotNull(instance);
        
        IAlternative alternative = instance.getAlternative();
        
        Assert.assertTrue(alternative instanceof AlternativeComponent);

        WebBeansContext.getInstance().getPluginLoader().shutDown();
        
    }
    
    @Test
    public void testInjectNotAlternative()
    {
        WebBeansContext.getInstance().getAlternativesManager().clear();
        
        defineManagedBean(AlternativeComponent.class);
        defineManagedBean(NotAlternativeComponent.class);
        
        Bean<AlternativeInjector> injector = defineManagedBean(AlternativeInjector.class);
        AlternativeInjector instance = (AlternativeInjector) getManager().getReference(injector, AlternativeInjector.class, getManager().createCreationalContext(injector));
        
        Assert.assertNotNull(instance);
        
        IAlternative alternative = instance.getAlternative();
        
        Assert.assertTrue(alternative instanceof NotAlternativeComponent);

        WebBeansContext.getInstance().getPluginLoader().shutDown();
        
    }
    
}
