/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;


import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.junit.Assert;
import org.junit.Test;

public class ReadDescriptorsTest {

    @Test
    public void testClassNotAvailable() {

        final Resource resource = new Resource();
        resource.setClassName("not.a.real.Class");

        final Resources resources = new Resources();
        resources.add(resource);

        final Resources checkedResources = ReadDescriptors.check(resources);
        final Resource res = checkedResources.getResource().get(0);

        Assert.assertEquals("true", res.getProperties().getProperty("Lazy"));
        Assert.assertEquals("true", res.getProperties().getProperty("UseAppClassLoader"));
        Assert.assertEquals("true", res.getProperties().getProperty("InitializeAfterDeployment"));
    }

    @Test
    public void testClassAndTypeAvailable() {

        final Resource resource = new Resource();
        resource.setClassName("org.apache.openejb.config.ReadDescriptorsTest");

        final Resources resources = new Resources();
        resources.add(resource);

        final Resources checkedResources = ReadDescriptors.check(resources);
        final Resource res = checkedResources.getResource().get(0);

        Assert.assertNull(res.getProperties().getProperty("Lazy"));
        Assert.assertNull(res.getProperties().getProperty("UseAppClassLoader"));
        Assert.assertNull(res.getProperties().getProperty("InitializeAfterDeployment"));
    }

    @Test
    public void testClassAvailable() {

        final Resource resource = new Resource();
        resource.setClassName("org.apache.openejb.config.ReadDescriptorsTest");

        final Resources resources = new Resources();
        resources.add(resource);

        final Resources checkedResources = ReadDescriptors.check(resources);
        final Resource res = checkedResources.getResource().get(0);

        Assert.assertNull(res.getProperties().getProperty("Lazy"));
        Assert.assertNull(res.getProperties().getProperty("UseAppClassLoader"));
        Assert.assertNull(res.getProperties().getProperty("InitializeAfterDeployment"));
    }

    @Test
    public void testLazyResource() {
        final Resource resource = new Resource();
        resource.setClassName("not.a.real.Class");
        resource.getProperties().setProperty("Lazy", "true");

        final Resources resources = new Resources();
        resources.add(resource);

        final Resources checkedResources = ReadDescriptors.check(resources);
        final Resource res = checkedResources.getResource().get(0);

        Assert.assertEquals("true", res.getProperties().getProperty("Lazy"));
        Assert.assertEquals("true", res.getProperties().getProperty("UseAppClassLoader"));
        Assert.assertNull(res.getProperties().getProperty("InitializeAfterDeployment"));
    }

    @Test
    public void testReadCmpOrmDescriptor() throws Exception {
        final EjbModule ejbModule = new EjbModule(new EjbJar());
        ejbModule.getAltDDs().put("openejb-cmp-orm.xml", getClass().getResource("test-openejb-cmp-orm.xml"));
        new ReadDescriptors().readCmpOrm(ejbModule);
        Assert.assertNotNull(ejbModule.getAltDDs().get("openejb-cmp-orm.xml"));
        Assert.assertTrue(EntityMappings.class.isInstance(ejbModule.getAltDDs().get("openejb-cmp-orm.xml")));
    }
}
