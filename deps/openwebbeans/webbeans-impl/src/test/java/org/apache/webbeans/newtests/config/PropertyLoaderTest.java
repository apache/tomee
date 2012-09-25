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
package org.apache.webbeans.newtests.config;

import org.apache.webbeans.config.PropertyLoader;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyLoaderTest
{
    private static final String PROPERTY_FILE = "org/apache/webbeans/newtests/config/propertyloadertest.properties";
    private static final String PROPERTY_FILE2 = "org/apache/webbeans/newtests/config/propertyloadertest2.properties";
    private static final String PROPERTY_FILE3 = "org/apache/webbeans/newtests/config/propertyloadertest3.properties";

    @Test
    public void testPropertyLoader() throws Exception
    {
        Properties p = PropertyLoader.getProperties(PROPERTY_FILE);
        Assert.assertNotNull(p);

        String testValue = p.getProperty("testConfig");
        Assert.assertNotNull(testValue);
        Assert.assertEquals("testValue", testValue);
    }

    @Test
    public void testNonExistentProperties()
    {
        Properties p = PropertyLoader.getProperties("notexisting.properties");
        Assert.assertNull(p);
    }
    
    @Test
    public void testOrdinal()
    {
        Properties p15 = PropertyLoader.getProperties(PROPERTY_FILE);
        Properties p16 = PropertyLoader.getProperties(PROPERTY_FILE2);
        Properties p20 = PropertyLoader.getProperties(PROPERTY_FILE3);
        
        List<Properties> properties = new ArrayList<Properties>();
        properties.add(p15);
        properties.add(p16);
        properties.add(p20);
        
        Properties prop = MockPropertyLoader.mergeProperties(MockPropertyLoader.sortProperties(properties));
        Assert.assertEquals("testValue16", prop.get("testConfig"));
        Assert.assertEquals("16", prop.get("test16"));
        Assert.assertEquals("15", prop.get("test15"));
        Assert.assertEquals("20", prop.get("configuration.ordinal"));
        Assert.assertEquals("z", prop.get("override_y"));
        Assert.assertEquals("20", prop.get("override_all"));
        Assert.assertEquals("15", prop.get("unique_1"));
        Assert.assertEquals("16", prop.get("unique_2"));
        Assert.assertEquals("20", prop.get("unique_3"));
        
    }
}
