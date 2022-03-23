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
package org.apache.openejb.config.sys;

import junit.framework.TestCase;
import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.junit.AfterClass;

/**
 * @version $Rev$ $Date$
 */
public class NoServiceJarTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final ConfigurationFactory factory = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        final Resource orange = new Resource("Orange");
        orange.setClassName(Color.class.getName());
        orange.getProperties().setProperty("red", "255");
        orange.getProperties().setProperty("green", "200");
        orange.getProperties().setProperty("blue", "0");

        final ResourceInfo resourceInfo = factory.configureService(orange, ResourceInfo.class);
        assembler.createResource(resourceInfo);

        assembler.createSecurityService(factory.configureService(SecurityServiceInfo.class));
        assembler.createTransactionManager(factory.configureService(TransactionServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(MyBean.class));
        final AppContext application = assembler.createApplication(factory.configureApplication(new EjbModule(ejbJar)));

        final MyBean myBean = (MyBean) application.getBeanContexts().get(0).getBusinessLocalBeanHome().create();

        final Color color = myBean.getColor();
        assertNotNull(color);
        assertEquals(255, color.getRed());
        assertEquals(200, color.getGreen());
        assertEquals(0, color.getBlue());
    }

    public void testInvalid() throws Exception {
        final ConfigurationFactory factory = new ConfigurationFactory();

        final Resource orange = new Resource("Orange");
        orange.getProperties().setProperty("red", "255");
        orange.getProperties().setProperty("green", "200");
        orange.getProperties().setProperty("blue", "0");

        try {
            factory.configureService(orange, ResourceInfo.class);
            fail("OpenEJBException should have been thrown");
        } catch (final OpenEJBException e) {
            // pass
        }
    }

    public void testInvalidJustType() throws Exception {
        final ConfigurationFactory factory = new ConfigurationFactory();

        final Resource orange = new Resource("Orange");
        orange.setType(Color.class.getName());
        orange.getProperties().setProperty("red", "255");
        orange.getProperties().setProperty("green", "200");
        orange.getProperties().setProperty("blue", "0");

        try {
            factory.configureService(orange, ResourceInfo.class);
            fail("OpenEJBException should have been thrown");
        } catch (final OpenEJBException e) {
            // pass
        }
    }


    public static class Color {
        private int red;
        private int green;
        private int blue;

        public Color() {
        }

        public int getRed() {
            return red;
        }

        public void setRed(final int red) {
            this.red = red;
        }

        public int getGreen() {
            return green;
        }

        public void setGreen(final int green) {
            this.green = green;
        }

        public int getBlue() {
            return blue;
        }

        public void setBlue(final int blue) {
            this.blue = blue;
        }
    }

    public static class MyBean {

        @jakarta.annotation.Resource
        private Color color;

        public Color getColor() {
            return color;
        }
    }
}
