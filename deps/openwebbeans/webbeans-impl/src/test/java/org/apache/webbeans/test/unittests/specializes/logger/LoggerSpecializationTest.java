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
package org.apache.webbeans.test.unittests.specializes.logger;

import java.io.InputStream;
import java.util.ArrayList;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.specializes.logger.ISomeLogger;
import org.apache.webbeans.test.component.specializes.logger.MockNotSpecializedLogger;
import org.apache.webbeans.test.component.specializes.logger.MockSpecializedLogger;
import org.apache.webbeans.test.component.specializes.logger.SpecializedInjector;
import org.apache.webbeans.test.component.specializes.logger.SystemLogger;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;
import org.junit.Test;

public class LoggerSpecializationTest extends TestContext
{
    public LoggerSpecializationTest()
    {
        super(LoggerSpecializationTest.class.getName());
    }

    @Test
    public void testNotSpecializedVersion()
    {
        clear();

        WebBeansContext.getInstance().getPluginLoader().startUp();
        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/specializes/alternatives.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternative.xml");
        
        defineManagedBean(SystemLogger.class);
        defineManagedBean(MockNotSpecializedLogger.class);        
        
        Bean<SpecializedInjector> bean = defineManagedBean(SpecializedInjector.class);
        Object instance = getManager().getReference(bean, SpecializedInjector.class, getManager().createCreationalContext(bean));
        
        Assert.assertTrue(instance instanceof SpecializedInjector);
        SpecializedInjector injector = (SpecializedInjector)instance;
        
        ISomeLogger logger = injector.logger();
        
        Assert.assertTrue(logger instanceof SystemLogger);
        
        logger.printError("Hello World");
        
        SystemLogger sysLogger = (SystemLogger)logger;
        
        Assert.assertEquals("Hello World", sysLogger.getMessage());

        WebBeansContext.getInstance().getPluginLoader().shutDown();
        WebBeansContext.getInstance().getAlternativesManager().clear();
    }
    
    @Test
    public void testSpecializedVersion()
    {
        clear();

        WebBeansContext.getInstance().getPluginLoader().startUp();
        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/specializes/alternatives.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives.xml");
        
        defineManagedBean(SystemLogger.class);
        defineManagedBean(MockSpecializedLogger.class);
        
        ArrayList<Class<?>> specialClassList = new ArrayList<Class<?>>();
        specialClassList.add(MockSpecializedLogger.class);
        WebBeansContext.getInstance().getWebBeansUtil().configureSpecializations(specialClassList);

        Bean<SpecializedInjector> bean = defineManagedBean(SpecializedInjector.class);
        Object instance = getManager().getReference(bean, SpecializedInjector.class, getManager().createCreationalContext(bean));
        
        Assert.assertTrue(instance instanceof SpecializedInjector);
        SpecializedInjector injector = (SpecializedInjector)instance;
        
        ISomeLogger logger = injector.logger();
        
        Assert.assertTrue(logger instanceof MockSpecializedLogger);
        
        logger.printError("Hello World");
        
        MockSpecializedLogger sysLogger = (MockSpecializedLogger)logger;
        
        Assert.assertEquals("Hello World", sysLogger.getMessage());

        WebBeansContext.getInstance().getPluginLoader().shutDown();
    }
}
