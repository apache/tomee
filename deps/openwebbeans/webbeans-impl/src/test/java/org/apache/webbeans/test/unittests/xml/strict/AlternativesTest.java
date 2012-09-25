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
package org.apache.webbeans.test.unittests.xml.strict;

import java.io.InputStream;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.inject.AlternativesManager;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.xml.strict.Alternative1;
import org.apache.webbeans.test.xml.strict.Alternative2;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;
import org.junit.Test;

public class AlternativesTest extends TestContext
{
    public AlternativesTest()
    {
        super(AlternativesTest.class.getName());
    }

    @Test
    public void testAlternativeCorrect()
    {
        Bean<Alternative1> alternative1 = defineManagedBean(Alternative1.class);
        Bean<Alternative2> alternative2 = defineManagedBean(Alternative2.class);        
        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_correct.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_correct.xml");

        AlternativesManager manager = WebBeansContext.getInstance().getAlternativesManager();
        
        Assert.assertTrue(manager.isBeanHasAlternative(alternative1));
        Assert.assertTrue(manager.isBeanHasAlternative(alternative2));
        
        manager.clear();
        
    }
    
    @Test(expected=WebBeansConfigurationException.class)
    public void testDoubleAlternativeClass()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed.xml");        
        
    }
    
    @Test(expected=WebBeansConfigurationException.class)
    public void testDoubleAlternativeStereotype()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed2.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed2.xml");        
        
    }
    
    @Test(expected=WebBeansConfigurationException.class)
    public void testNoClass()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed3.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed3.xml");        
        
    }
    
    @Test(expected=WebBeansConfigurationException.class)
    public void testNoStereotype()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed4.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed4.xml");        
        
    }

    @Test(expected=WebBeansConfigurationException.class)
    public void testNotAnnotationClass()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed5.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed5.xml");        
        
    }

    @Test(expected=WebBeansConfigurationException.class)
    public void testNotStereotype()
    {        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/alternatives_failed6.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "alternatives_failed6.xml");        
        
    }
    
}
