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
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.provider.ID;
import org.apache.openejb.config.provider.ProviderLoader;
import org.apache.openejb.config.provider.ProviderManager;
import org.apache.openejb.config.provider.ServiceJarXmlLoader;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.AfterClass;

import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ServiceProviderInheritanceTest extends TestCase {

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {

        SystemInstance.get().setComponent(ProviderManager.class, new ProviderManager(new ProviderLoader() {
            final ProviderLoader loader = new ServiceJarXmlLoader();

            @Override
            public ServiceProvider load(final ID id) {

                { // try the regular loader
                    final ServiceProvider provider = loader.load(id);
                    if (provider != null) return provider;
                }

                if ("color".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider color = new ServiceProvider(Color.class, "Color", "Resource");
                    color.getProperties().setProperty("red", "0");
                    color.getProperties().setProperty("green", "0");
                    color.getProperties().setProperty("blue", "0");
                    color.getTypes().add(Color.class.getName());
                    return color;
                }

                if ("red".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider red = new ServiceProvider();
                    red.setId("Red");
                    red.setParent("Color");
                    red.getProperties().setProperty("red", "255");
                    return red;
                }

                if ("orange".equalsIgnoreCase(id.getName())) {
                    final ServiceProvider orange = new ServiceProvider();
                    orange.setId("Orange");
                    orange.setParent("Red");
                    orange.getProperties().setProperty("green", "200");
                    return orange;
                }

                throw new IllegalStateException(id.toString());
            }

            @Override
            public List<ServiceProvider> load(final String namespace) {
                final List<ServiceProvider> list = loader.load(namespace);

                list.add(load(new ID(namespace, "color")));
                list.add(load(new ID(namespace, "red")));
                list.add(load(new ID(namespace, "orange")));
                return list;
            }
        }));

        final ConfigurationFactory factory = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        assembler.createSecurityService(factory.configureService(SecurityServiceInfo.class));
        assembler.createTransactionManager(factory.configureService(TransactionServiceInfo.class));

        {
            final Resource orange = new Resource("Orange", Color.class.getName(), "Orange");
            final ResourceInfo resourceInfo = factory.configureService(orange, ResourceInfo.class);

            assertEquals(Color.class.getName(), resourceInfo.className);
            assertEquals("Orange", resourceInfo.id);
            assertEquals(3, resourceInfo.properties.size());
            assertEquals("255", resourceInfo.properties.get("red"));
            assertEquals("200", resourceInfo.properties.get("green"));
            assertEquals("0", resourceInfo.properties.get("blue"));

            assembler.createResource(resourceInfo);
        }


        {
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
