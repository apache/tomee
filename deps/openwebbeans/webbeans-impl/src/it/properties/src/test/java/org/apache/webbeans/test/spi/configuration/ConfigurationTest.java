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
package org.apache.webbeans.test.spi.configuration;

import junit.framework.Assert;

import org.apache.webbeans.config.OpenWebBeansConfiguration;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Test;

public class ConfigurationTest
{
    @Test
    public void testConfigurationParser() 
    {
        OpenWebBeansConfiguration cfg = WebBeansContext.getInstance().getOpenWebBeansConfiguration();
        
        // not overloaded
        String ts = cfg.getProperty("org.apache.webbeans.spi.TransactionService");
        Assert.assertNotNull(ts);
        Assert.assertEquals("org.apache.webbeans.corespi.ee.TransactionServiceJndiImpl", ts);
        
        // overloaded version 1
        String wbf = cfg.getProperty("org.apache.webbeans.spi.JNDIService");
        Assert.assertNotNull(wbf);
        Assert.assertEquals("org.apache.webbeans.corespi.ee.JNDIServiceEnterpriseImpl", wbf);
        
        // property which is only in the specialised openwebbeans.properties
        String testProperty = cfg.getProperty("test.property");
        Assert.assertNotNull(testProperty);
        Assert.assertEquals("true", testProperty);
    }
    
}