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
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.FinderFactory;
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
import org.apache.openejb.util.Archives;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.apache.openejb.config.DeploymentFilterable.DEPLOYMENTS_CLASSPATH_PROPERTY;

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

        public $(final FrameworkMethod testMethod, final Object target) {
            this.testMethod = testMethod;
        }

        @Override
        public void evaluate() throws Throwable {
            final MetaTest annotation = testMethod.getAnnotation(MetaTest.class);

            if (isSpecificBeanType(annotation)) {

                testOnce(annotation);

            } else {

                testAsAllBeanTypes(annotation);
            }
        }

        private void testAsAllBeanTypes(final MetaTest annotation) throws Throwable {
            Class<? extends EnterpriseBean>[] classes = annotation.types();

            // for some reason these defaults do not work in the annotation
            if (classes.length == 0)
                classes = new Class[]{SingletonBean.class, StatelessBean.class, StatefulBean.class, ManagedBean.class, MessageDrivenBean.class};

            for (final Class<? extends EnterpriseBean> beanType : classes) {


                try {
                    final ConfigurationFactory factory = factory();

                    final EjbJar expected = new EjbJar("expected");
                    final EnterpriseBean bean = expected.addEnterpriseBean(newBean(beanType, annotation.expected()));

                    final EjbJar actual = new EjbJar("actual");
                    actual.addEnterpriseBean(newBean(beanType, annotation.actual()));


                    final AppModule app = new AppModule(this.getClass().getClassLoader(), "test");
                    app.getEjbModules().add(module(expected));
                    app.getEjbModules().add(module(actual));

                    final AppInfo appInfo = factory.configureApplication(app);

                    final List<ContainerTransaction> expectedList = expected.getAssemblyDescriptor().getContainerTransaction();
                    final List<ContainerTransaction> actualList = actual.getAssemblyDescriptor().getContainerTransaction();

                    assertEquals(expectedList.size(), actualList.size());
                    final String expectedXml = toString(expected);
                    final String actualXml = toString(actual).replaceAll("Actual", "Expected").replaceAll("actual", "expected");
                    assertEquals(expectedXml, actualXml);
                } catch (final Exception e) {
                    throw new AssertionError(beanType.getSimpleName()).initCause(e);
                }
            }
        }

        private void testOnce(final MetaTest annotation) throws Throwable {

            try {

                SystemInstance.reset();
                SystemInstance.get().setProperty(DEPLOYMENTS_CLASSPATH_PROPERTY, "false");

                final Assembler assembler = new Assembler();
                final ConfigurationFactory factory = new ConfigurationFactory();
                assembler.createTransactionManager(factory.configureService(TransactionServiceInfo.class));
                assembler.createSecurityService(factory.configureService(SecurityServiceInfo.class));

                final ArrayList<File> files = new ArrayList<>();

                { // expected archive
                    final HashMap map = new HashMap();
                    map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"expected\"/>");
                    files.add(Archives.jarArchive(map, "expected", annotation.expected()));
                }

                { // actual archive
                    final HashMap map = new HashMap();
                    map.put("META-INF/ejb-jar.xml", "<ejb-jar id=\"actual\"/>");
                    files.add(Archives.jarArchive(map, "actual", getClasses(annotation)));
                }

                final AppModule app = factory.loadApplication(this.getClass().getClassLoader(), "test", files);
                for (final EjbModule ejbModule : app.getEjbModules()) {
                    ejbModule.getProperties().setProperty(FinderFactory.FORCE_LINK, Boolean.TRUE.toString());
                }

                final OpenEjbConfiguration conf = factory.getOpenEjbConfiguration();

                factory.configureApplication(app);

                final EjbJar expected = getDescriptor(app, "expected");
                final EjbJar actual = getDescriptor(app, "actual");
                final String expectedXml = toString(expected);
                final String actualXml = toString(actual).replaceAll("Actual", "Expected").replaceAll("actual", "expected");
                assertEquals(expectedXml, actualXml);
            } catch (final Exception e) {
                throw new AssertionError().initCause(e);
            }
        }

        private Class[] getClasses(final MetaTest annotation) {
            final Class clazz = annotation.actual();

            final List<Class> classes = new ArrayList<>();

            getClasses(clazz, classes);

            return classes.toArray(new Class[]{});
        }

        private void getClasses(final Class clazz, final List<Class> classes) {
            if (classes.contains(clazz)) return;
            classes.add(clazz);

            final Annotation[] annotations = clazz.getAnnotations();
            for (final Annotation ann : annotations) {
                final Class<? extends Annotation> type = ann.annotationType();
                final String name = type.getName();
                if (name.startsWith("javax.")) continue;
                if (name.startsWith("java.")) continue;

                getClasses(type, classes);
            }
        }

        private EjbJar getDescriptor(final AppModule app, final String name) {
            for (final EjbModule ejbModule : app.getEjbModules()) {
                if (name.equals(ejbModule.getModuleId())) {
                    return ejbModule.getEjbJar();
                }
            }
            throw new RuntimeException("Test setup failed, no such module: " + name);
        }

        private boolean isSpecificBeanType(final MetaTest annotation) {
            final Class<? extends Annotation>[] annotations = new Class[]{jakarta.ejb.Singleton.class, jakarta.ejb.Stateless.class, jakarta.ejb.Stateful.class, jakarta.ejb.MessageDriven.class};
            for (final Class<? extends Annotation> compDef : annotations) {
                if (annotation.expected().isAnnotationPresent(compDef)) {
                    return true;
                }
            }

            return false;
        }

        private ConfigurationFactory factory() {
            try {
                SystemInstance.reset();
                SystemInstance.get().setProperty(DEPLOYMENTS_CLASSPATH_PROPERTY, "false");

                final Assembler assembler = new Assembler();
                final ConfigurationFactory factory = new ConfigurationFactory();

                // Configure the system but don't actually build it
                final OpenEjbConfiguration conf = factory.getOpenEjbConfiguration();
                final ContainerInfo container = new ContainerInfo();
                container.id = "foo";
                conf.containerSystem.containers.add(container);

                SystemInstance.get().setComponent(OpenEjbConfiguration.class, conf);

                return factory;
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private EjbModule module(final EjbJar ejbJar) {
            final OpenejbJar openejbJar = new OpenejbJar();
            openejbJar.addEjbDeployment(ejbJar.getEnterpriseBeans()[0]).setContainerId("foo");

            final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
            ejbModule.getProperties().setProperty(FinderFactory.FORCE_LINK, Boolean.TRUE.toString());
            return ejbModule;
        }

        private <T> T newBean(final Class<T> beanType, final Class ejbClass) {
            try {
                final Constructor<T> constructor = beanType.getConstructor(Class.class);
                return constructor.newInstance(ejbClass);
            } catch (final Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private static String toString(final EjbJar ejbjar) throws JAXBException {
            return JaxbJavaee.marshal(EjbJar.class, ejbjar);
        }

    }
}
