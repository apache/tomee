/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.apache.openejb.config.ConfigurationFactory;

/**
 * @version $Rev$ $Date$
 */
public class ResourceInfoComparatorTest extends TestCase {

    public void test() throws Exception {
        List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

        resources.add(new ResourceInfo());
        resources.get(0).id = "Red";
        resources.get(0).properties = new Properties();
        resources.get(0).properties.put("someValue", "Blue");

        resources.add(new ResourceInfo());
        resources.get(1).id = "Blue";
        resources.get(1).properties = new Properties();
        resources.get(1).properties.put("foo", "Green");

        resources.add(new ResourceInfo());
        resources.get(2).properties = new Properties();
        resources.get(2).id = "Green";

        resources.add(new ResourceInfo());
        resources.get(3).id = "Yellow";
        resources.get(3).properties = new Properties();
        resources.get(3).properties.put("foo", "Green");

        Collections.sort(resources, new ConfigurationFactory.ResourceInfoComparator(resources));

        assertEquals("Green", resources.get(0).id);
        assertEquals("Blue", resources.get(1).id);
        assertEquals("Red", resources.get(2).id);
        assertEquals("Yellow", resources.get(3).id);

    }

    public void testRealWorld() throws Exception {
        List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

        resources.add(new ResourceInfo());
        resources.get(0).id = "My DataSource";
        resources.get(0).properties = new Properties();

        resources.add(new ResourceInfo());
        resources.get(1).id = "My Unmanaged DataSource";
        resources.get(1).properties = new Properties();

        resources.add(new ResourceInfo());
        resources.get(2).id = "My JMS Resource Adapter";
        resources.get(2).properties = new Properties();
        resources.get(2).properties.put("DataSource", "My Unmanaged DataSource");

        resources.add(new ResourceInfo());
        resources.get(3).id = "My JMS Connection Factory";
        resources.get(3).properties = new Properties();
        resources.get(3).properties.put("ResourceAdapter", "My JMS Resource Adapter");

        Collections.sort(resources, new ConfigurationFactory.ResourceInfoComparator(resources));

        assertEquals("My DataSource", resources.get(0).id);
        assertEquals("My Unmanaged DataSource", resources.get(1).id);
        assertEquals("My JMS Resource Adapter", resources.get(2).id);
        assertEquals("My JMS Connection Factory", resources.get(3).id);

    }

    public void testRealWorld2() throws Exception {
        List<ResourceInfo> resources = new ArrayList<ResourceInfo>();

        resources.add(new ResourceInfo());
        resources.get(0).id = "My JMS Connection Factory";
        resources.get(0).properties = new Properties();
        resources.get(0).properties.put("ResourceAdapter", "My JMS Resource Adapter");

        resources.add(new ResourceInfo());
        resources.get(1).id = "My JMS Resource Adapter";
        resources.get(1).properties = new Properties();
        resources.get(1).properties.put("DataSource", "My Unmanaged DataSource");

        resources.add(new ResourceInfo());
        resources.get(2).id = "My Unmanaged DataSource";
        resources.get(2).properties = new Properties();

        resources.add(new ResourceInfo());
        resources.get(3).id = "My DataSource";
        resources.get(3).properties = new Properties();


        Collections.sort(resources, new ConfigurationFactory.ResourceInfoComparator(resources));

        // since unmanaged is first in the "file" it should be first here
        assertEquals("My Unmanaged DataSource", resources.get(0).id);
        assertEquals("My DataSource", resources.get(1).id);
        assertEquals("My JMS Resource Adapter", resources.get(2).id);
        assertEquals("My JMS Connection Factory", resources.get(3).id);

    }
}
