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
package org.apache.openejb.config;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ClientInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import javax.ejb.ApplicationException;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployerTest {

    @Test
    /**
     *  For http://issues.apache.org/jira/browse/OPENEJB-980
     */
    public void applicationExceptionInheritanceTest() throws Exception {
        EjbModule ejbModule = testModule();
        AnnotationDeployer.DiscoverAnnotatedBeans discvrAnnBeans = new AnnotationDeployer.DiscoverAnnotatedBeans();
        ejbModule = discvrAnnBeans.deploy(ejbModule);

        AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();
        org.apache.openejb.jee.ApplicationException appEx =
                assemblyDescriptor.getApplicationException(BusinessException.class);
        assertThat(appEx, notNullValue());
        assertThat(appEx.getExceptionClass(), is(BusinessException.class.getName()));
        assertThat(appEx.isRollback(), is(true));

        //inheritance is now handled at runtime, only explicitly mentioned exceptions are in the assembly descriptor
        appEx = assemblyDescriptor.getApplicationException(ValueRequiredException.class);
        assertThat(appEx, nullValue());
    }

    private EjbModule testModule() {
        EjbJar ejbJar = new EjbJar("test-classes");
        EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.setFinder(new ClassFinder(AnnotationDeployerTest.class,
                BusinessException.class,
                Exception.class,
                GenericInterface.class,
                InterceptedSLSBean.class,
                MyMainClass.class,
                TestLocalBean.class,
                ValueRequiredException.class
        ));
        return ejbModule;
    }


    @Test
    public void testSortClasses() throws Exception {
        AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Emerald.class)).link();

        List<Annotated<Class<?>>> classes = finder.findMetaAnnotatedClasses(Resource.class);
        assertTrue(classes.size() >= 3);

        List<Annotated<Class<?>>> sorted = AnnotationDeployer.sortClasses(classes);

        assertTrue(sorted.size() >= 3);

        assertEquals(Emerald.class, sorted.get(0).get());
        assertEquals(Green.class, sorted.get(1).get());
        assertEquals(Color.class, sorted.get(2).get());
    }

    @Test
    public void testSortMethods() throws Exception {
        AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(Emerald.class)).link();

        List<Annotated<Method>> classes = finder.findMetaAnnotatedMethods(Resource.class);
        assertTrue(classes.size() >= 3);

        List<Annotated<Method>> sorted = AnnotationDeployer.sortMethods(classes);

        assertTrue(sorted.size() >= 3);

        assertEquals(Emerald.class, sorted.get(0).get().getDeclaringClass());
        assertEquals(Green.class, sorted.get(1).get().getDeclaringClass());
        assertEquals(Color.class, sorted.get(2).get().getDeclaringClass());
    }

    @Test
    /**
     *  For https://issues.apache.org/jira/browse/OPENEJB-1063
     */
    public void badMainClassFormatTest() throws Exception {
	ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");

        ClientModule clientModule = new ClientModule(null, app.getClassLoader(), app.getJarLocation(), null, null);
        
        // change "." --> "/" to check that main class is changed by the AnnotationDeployer
        String mainClass = MyMainClass.class.getName().replaceAll("\\.", "/");
        clientModule.setMainClass(mainClass);

        app.getClientModules().add(clientModule);
        
        AppInfo appInfo = config.configureApplication(app);
        
        assembler.createApplication(appInfo);
        
        ClientInfo clientInfo = appInfo.clients.get(0);
        Assert.assertNotNull(clientInfo);
        Assert.assertEquals(MyMainClass.class.getName(), clientInfo.mainClass);
    }

    /**
     *  For https://issues.apache.org/jira/browse/OPENEJB-1128
     */
    @Test
    public void interceptingGenericBusinessMethodCalls() throws Exception {
        EjbModule ejbModule = testModule();
        EjbJar ejbJar = ejbModule.getEjbJar();
        
        AnnotationDeployer.DiscoverAnnotatedBeans discvrAnnBeans = new AnnotationDeployer.DiscoverAnnotatedBeans();
        ejbModule = discvrAnnBeans.deploy(ejbModule);

        final EnterpriseBean bean = ejbJar.getEnterpriseBean("InterceptedSLSBean");
        assert bean != null;        
    }

    /**
     * For https://issues.apache.org/jira/browse/OPENEJB-1188
     * @throws Exception
     */
    @Test
    public void testLocalBean() throws Exception {
        EjbModule ejbModule = testModule();
        EjbJar ejbJar = ejbModule.getEjbJar();

        AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), "myapp");
        appModule.getEjbModules().add(ejbModule);

        AnnotationDeployer annotationDeployer = new AnnotationDeployer();
        appModule = annotationDeployer.deploy(appModule);

        EnterpriseBean bean = ejbJar.getEnterpriseBean("TestLocalBean");
        assert bean != null;
        assert (((SessionBean)bean).getLocalBean() != null);

        bean = ejbJar.getEnterpriseBean("InterceptedSLSBean");
        assert bean != null;
        assert (((SessionBean)bean).getLocalBean() == null);        
    }

    @ApplicationException(rollback = true)
    public abstract class BusinessException extends Exception {
    }

    public class ValueRequiredException extends BusinessException {
    }

    public static final class MyMainClass {
        public static void main(String[] args) {
        }
    }

    public static interface GenericInterface<T> {
        T genericMethod(T t);
    }

    @Stateless
    @Local(GenericInterface.class)
    public static class InterceptedSLSBean implements GenericInterface<String> {
        public String genericMethod(String s) {
            return s;
        }
    }

    @Stateless
    @LocalBean
    public static class TestLocalBean {
        public String echo(String input) {
            return input;
        }
    }

    @Resource
    public static class Color {
        @Resource
        public void color(){}
    }

    @Resource
    public static class Green extends Color {
        @Resource
        public void green(){}
    }

    @Resource
    public static class Emerald extends Green {
        @Resource
        public void emerald(){}
    }

}
