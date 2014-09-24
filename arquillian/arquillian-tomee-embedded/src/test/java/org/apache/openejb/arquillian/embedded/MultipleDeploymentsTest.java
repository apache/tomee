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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.tomee.catalina.environment.Hosts;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;

@RunWith(Arquillian.class)
public class MultipleDeploymentsTest extends Assert {

    public static final String MSG = "HelloWorld";

    @Deployment(name = "orange")
    public static WebArchive orange() {
        return ShrinkWrap.create(WebArchive.class, "orange.war")
            .addClasses(
                TestMe.class,
                Hosts.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(name = "green")
    public static WebArchive green() {
        return ShrinkWrap.create(WebArchive.class, "green.war");
    }

    @Deployment(name = "blue")
    public static WebArchive blue() {
        return ShrinkWrap.create(WebArchive.class, "blue.war");
    }

    @Deployment(name = "yellow")
    public static WebArchive yellow() {
        return ShrinkWrap.create(WebArchive.class, "yellow.war");
    }

    @Inject
    private TestMe testMe;

    @Test
    @OperateOnDeployment("orange")
    public void testOrange() throws Exception {

        final String className = TestMe.class.getName();
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        final List<AppContext> appContexts = containerSystem.getAppContexts();
        final ClassLoader loader = this.getClass().getClassLoader();

        int size = appContexts.size();
        assertEquals("Unexpected app count", 4, size);

        for (final AppContext app : appContexts) {
            final BeanContext context = containerSystem.getBeanContext(app.getId() + "_" + className);
            if (context != null) {
                if (context.getBeanClass().getClassLoader() == loader) {
                    System.out.println("Found '" + className + "' in app: " + app.getId());
                    size--;
                }
            }
        }

        assertEquals("Found " + (4 - size) + " matching contexts", 3, size);

        assertNotNull(testMe);
        assertEquals("Unexpected message", MSG, testMe.getMessage());


    }

    @Test
    @OperateOnDeployment("green")
    public void testGreen() throws Exception {
        assertNull("Value should be null", testMe);
    }

    @Test
    @OperateOnDeployment("blue")
    public void testBlue() throws Exception {
        assertNull("Value should be null", testMe);
    }

    @Test
    @OperateOnDeployment("yellow")
    public void testYellow() throws Exception {
        assertNull("Value should be null", testMe);
    }

    public static class TestMe {
        public TestMe() {
        }

        public String getMessage() {
            return MSG;
        }
    }
}
