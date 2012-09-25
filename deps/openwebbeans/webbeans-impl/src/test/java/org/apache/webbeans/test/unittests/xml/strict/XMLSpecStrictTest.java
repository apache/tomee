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

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.xml.strict.DummyDecorator;
import org.apache.webbeans.test.xml.strict.DummyInterceptor;
import org.apache.webbeans.xml.WebBeansXMLConfigurator;
import org.junit.Test;

public class XMLSpecStrictTest extends TestContext
{
    public XMLSpecStrictTest()
    {
        super(XMLSpecStrictTest.class.getName());
    }

    @Test
    public void testXMLSpecStrictDecorators()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/decorators.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "decorators.xml");

        boolean enable = WebBeansContext.getInstance().getDecoratorsManager().isDecoratorEnabled(DummyDecorator.class);
        Assert.assertTrue(enable);
    }
    
    @Test
    public void testXMLSpecStrictInterceptors()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("org/apache/webbeans/test/xml/strict/interceptors.xml");
        Assert.assertNotNull(stream);

        WebBeansXMLConfigurator configurator = new WebBeansXMLConfigurator();
        configurator.configureSpecSpecific(stream, "interceptors.xml");

        boolean enable = WebBeansContext.getInstance().getInterceptorsManager().isInterceptorEnabled(DummyInterceptor.class);
        Assert.assertTrue(enable);
    }
    
}
