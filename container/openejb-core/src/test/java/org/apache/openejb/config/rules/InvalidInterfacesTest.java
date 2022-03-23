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
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.interceptor.AroundInvoke;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class InvalidInterfacesTest {
    @Keys
    public EjbJar testCorrectInterfaces() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        bean.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        bean.addBusinessLocal(FooLocal.class.getName());
        bean.addBusinessRemote(FooRemote.class.getName());
        return ejbJar;
    }

    @Keys({@Key("xml.remote.businessLocal"), @Key("xml.home.businessLocal"), @Key("xml.local.businessLocal"), @Key("xml.localHome.businessLocal")})
    public EjbJar testBusinessLocal() throws Exception {
        return validate(FooLocal.class);
    }

    @Keys({@Key("xml.remote.businessRemote"), @Key("xml.home.businessRemote"), @Key("xml.local.businessRemote"), @Key("xml.localHome.businessRemote")})
    public EjbJar testBusinessRemote() throws Exception {
        return validate(FooRemote.class);
    }

    @Keys({@Key("xml.home.ejbObject"), @Key("xml.local.ejbObject"), @Key("xml.localHome.ejbObject"), @Key("xml.businessLocal.ejbObject"), @Key("xml.businessRemote.ejbObject")})
    public EjbJar testEJBObject() throws Exception {
        return validate(FooEJBObject.class);
    }

    @Keys({@Key("xml.remote.ejbHome"), @Key("xml.local.ejbHome"), @Key("xml.localHome.ejbHome"), @Key("xml.businessLocal.ejbHome"), @Key("xml.businessRemote.ejbHome")})
    public EjbJar testEJBHome() throws Exception {
        return validate(FooEJBHome.class);
    }

    @Keys({@Key("xml.remote.ejbLocalHome"), @Key("xml.home.ejbLocalHome"), @Key("xml.local.ejbLocalHome"), @Key("xml.businessLocal.ejbLocalHome"),
        @Key("xml.businessRemote.ejbLocalHome")})
    public EjbJar testEJBLocalHome() throws Exception {
        return validate(FooEJBLocalHome.class);
    }

    @Keys({@Key("xml.remote.ejbLocalObject"), @Key("xml.home.ejbLocalObject"), @Key("xml.localHome.ejbLocalObject"), @Key("xml.businessLocal.ejbLocalObject"),
        @Key("xml.businessRemote.ejbLocalObject")})
    public EjbJar testEJBLocalObject() throws Exception {
        return validate(FooEJBLocalObject.class);
    }

    @Keys({@Key("xml.remote.unknown"), @Key("xml.home.unknown"), @Key("xml.localHome.unknown"), @Key("xml.local.unknown"), @Key("xml.localRemote.conflict")})
    public EjbJar testUnkown() throws Exception {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        return validate(FooUnknown.class);
    }

    @Keys({@Key("xml.remote.beanClass"), @Key("xml.home.beanClass"), @Key("xml.localHome.beanClass"), @Key("xml.local.beanClass"), @Key("xml.businessRemote.beanClass"),
        @Key("xml.businessLocal.beanClass")})
    public EjbJar testBeanClass() throws Exception {
        return validate(FooBean.class);
    }

    @Keys({@Key("xml.remote.notInterface"), @Key("xml.home.notInterface"), @Key("xml.localHome.notInterface"), @Key("xml.local.notInterface"),
        @Key("xml.businessRemote.notInterface"), @Key("xml.businessLocal.notInterface")})
    public EjbJar testNotInterface() throws Exception {
        return validate(FooClass.class);
    }

    @Keys({@Key("ann.notAnInterface"), @Key("xml.businessLocal.notInterface"), @Key("ann.localRemote.conflict"), @Key("ann.remoteOrLocal.ejbHome"),
        @Key("xml.businessRemote.ejbHome"), @Key("ann.remoteOrLocal.ejbObject"), @Key("xml.businessRemote.ejbObject"), @Key(value = "ann.remoteOrLocal.ejbLocalHome"),
        @Key(value = "ann.remoteOrLocal.ejbLocalObject"), @Key("xml.businessLocal.ejbLocalHome"), @Key("xml.businessLocal.ejbLocalObject")})
    public EjbJar test() throws OpenEJBException {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(BBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(CBean.class));
        final StatelessBean mybean1 = ejbJar.addEnterpriseBean(new StatelessBean("MyBean1", MyBean.class));
        return ejbJar;
    }

    @Keys({@Key(value = "interface.beanOnlyAnnotation", type = KeyType.WARNING), @Key(value = "interfaceMethod.beanOnlyAnnotation", type = KeyType.WARNING), @Key("aroundInvoke.invalidArguments")})
    public EjbJar test1() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(DBean.class));
        return ejbJar;

    }

    @Keys({@Key(value = "ann.remoteOrLocal.converse.parent", count = 2)})
    public EjbJar test2() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(FBean.class));
        return ejbJar;

    }

    @After
    public void after() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "false");
    }

    private EjbJar validate(final Class interfaceClass) throws OpenEJBException {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndLocal(interfaceClass, interfaceClass);
        bean.setHomeAndRemote(interfaceClass, interfaceClass);
        bean.addBusinessLocal(interfaceClass);
        bean.addBusinessRemote(interfaceClass);
        return ejbJar;
    }

    public static class FooBean {
    }

    public static interface FooEJBHome extends EJBHome {
    }

    public static interface FooEJBObject extends EJBObject {
    }

    public static interface FooEJBLocalHome extends EJBLocalHome {
    }

    public static interface FooEJBLocalObject extends EJBLocalObject {
    }

    @Remote
    public static interface FooRemote {
    }

    @Local
    public static interface FooLocal {
    }

    public static interface FooUnknown {
    }

    public static class FooClass {
    }

    public static interface MyRemoteHome extends EJBHome {
    }

    public static interface MyLocalHome extends EJBLocalHome {
    }

    public static interface MyRemote extends EJBObject {
    }

    public static interface MyLocal extends EJBLocalObject {
    }

    @Remote({MyRemoteHome.class, MyRemote.class})
    @Local({MyLocalHome.class, MyLocal.class})
    public static class MyBean {
    }

    public static class ABean {
    }

    @Local(ABean.class)
    public static class BBean extends ABean {
    }

    public static interface C {
    }

    @Local(C.class)
    @Remote(C.class)
    public static class CBean {
    }

    @EJB
    public static interface D {
        @PostConstruct
        public void foo();
    }

    @Local(D.class)
    public static class DBean {
        public void foo() {
        }

        @AroundInvoke
        public Object bar() {
            return null;
        }
    }

    @Local
    public static interface E {
    }

    @Remote
    public static interface E1 extends E {
    }

    public static class EBean implements E1 {
    }

    @Remote
    public static interface F {
    }

    @Local
    public static interface F1 extends F {
    }

    public static class FBean implements F1 {
    }
}
