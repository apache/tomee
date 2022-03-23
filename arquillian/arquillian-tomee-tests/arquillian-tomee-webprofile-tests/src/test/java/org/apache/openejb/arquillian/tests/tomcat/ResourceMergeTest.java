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
package org.apache.openejb.arquillian.tests.tomcat;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.naming.NamingException;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ResourceMergeTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "resource.war")
                .addClasses(SomeResource.class, SomeResourceFactory.class)
                .add(new StringAsset("<Context>\n" +
                        "  <Resource name=\"some-resource\"\n" +
                        "            type=\"" + SomeResource.class.getName() + "\"\n" +
                        "            factory=\"" + SomeResourceFactory.class.getName() + "\"/>\n" +
                        "</Context>"), "META-INF/context.xml");
    }

    @Resource(name = "some-resource")
    private SomeResource sr;

    @Test
    public void checkResource() throws NamingException {
        assertNotNull(sr);
    }
}
