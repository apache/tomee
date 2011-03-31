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
package org.apache.openejb.meta;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.xml.bind.JAXBException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * @version $Rev$ $Date$
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaTest {

    Class expected();

    Class actual();

    Class<? extends EnterpriseBean>[] types() default {};


    public static class $ extends Statement {

        // The test method
        private final FrameworkMethod testMethod;

        public $(FrameworkMethod testMethod, Object target) {
            this.testMethod = testMethod;
        }

        @Override
        public void evaluate() throws Throwable {
            MetaTest annotation = testMethod.getAnnotation(MetaTest.class);


            Class<? extends EnterpriseBean>[] classes = annotation.types();

            // for some reason these defaults do not work in the annotation
            if (classes.length == 0) classes = new Class[]{SingletonBean.class, StatelessBean.class, StatefulBean.class, ManagedBean.class, MessageDrivenBean.class};

            for (Class<? extends EnterpriseBean> beanType : classes) {


                try {
                    ConfigurationFactory factory = factory();

                    EjbJar expected = new EjbJar("expected");
                    EnterpriseBean bean = expected.addEnterpriseBean(newBean(beanType, annotation.expected()));

                    EjbJar actual = new EjbJar("actual");
                    actual.addEnterpriseBean(newBean(beanType, annotation.actual()));


                    AppModule app = new AppModule(this.getClass().getClassLoader(), "test");
                    app.getEjbModules().add(module(expected));
                    app.getEjbModules().add(module(actual));

                    AppInfo appInfo = factory.configureApplication(app);

                    List<ContainerTransaction> expectedList = expected.getAssemblyDescriptor().getContainerTransaction();
                    List<ContainerTransaction> actualList = actual.getAssemblyDescriptor().getContainerTransaction();

                    assertEquals(expectedList.size(), actualList.size());
                    String expectedXml = toString(expected);
                    String actualXml = toString(actual).replaceAll("Actual", "Expected").replaceAll("actual", "expected");
                    assertEquals(expectedXml, actualXml);
                } catch (Exception e) {
                    throw new AssertionError(beanType.getSimpleName()).initCause(e);
                }
            }
        }

        private ConfigurationFactory factory() {
            try {
                SystemInstance.reset();

                Assembler assembler = new Assembler();
                ConfigurationFactory factory = new ConfigurationFactory();

                // Configure the system but don't actually build it
                OpenEjbConfiguration conf = factory.getOpenEjbConfiguration();
                ContainerInfo container = new ContainerInfo();
                container.id = "foo";
                conf.containerSystem.containers.add(container);

                SystemInstance.get().setComponent(OpenEjbConfiguration.class, conf);

                return factory;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private EjbModule module(EjbJar ejbJar) {
            OpenejbJar openejbJar = new OpenejbJar();
            openejbJar.addEjbDeployment(ejbJar.getEnterpriseBeans()[0]).setContainerId("foo");

            return new EjbModule(ejbJar, openejbJar);
        }

        private <T> T newBean(Class<T> beanType, Class ejbClass) {
            try {
                Constructor<T> constructor = beanType.getConstructor(Class.class);
                return constructor.newInstance(ejbClass);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private static String toString(EjbJar ejbjar) throws JAXBException {
            return JaxbJavaee.marshal(EjbJar.class, ejbjar);
        }

    }
}
