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

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.interceptor.AroundInvoke;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class InvalidInterfacesTest {
    @Keys
    public EjbJar testCorrectInterfaces() throws Exception {
        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        bean.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        bean.addBusinessLocal(FooLocal.class.getName());
        bean.addBusinessRemote(FooRemote.class.getName());
        return ejbJar;
    }

    @Keys( { @Key(value = "xml.remote.businessLocal", type = KeyType.FAILURE), 
             @Key(value = "xml.home.businessLocal", type = KeyType.FAILURE), 
             @Key(value = "xml.local.businessLocal", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.businessLocal", type = KeyType.FAILURE) })
    public EjbJar testBusinessLocal() throws Exception {
        return validate(FooLocal.class);
    }

    @Keys( { @Key(value = "xml.remote.businessRemote", type = KeyType.FAILURE), 
             @Key(value = "xml.home.businessRemote", type = KeyType.FAILURE), 
             @Key(value = "xml.local.businessRemote", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.businessRemote", type = KeyType.FAILURE) })
    public EjbJar testBusinessRemote() throws Exception {
        return validate(FooRemote.class);
    }

    @Keys( { @Key(value = "xml.home.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "xml.local.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "xml.businessRemote.ejbObject", type = KeyType.FAILURE) })
    public EjbJar testEJBObject() throws Exception {
        return validate(FooEJBObject.class);
    }

    @Keys( { @Key(value = "xml.remote.ejbHome", type = KeyType.FAILURE), 
             @Key(value = "xml.local.ejbHome", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.ejbHome", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbHome", type = KeyType.FAILURE), 
             @Key(value = "xml.businessRemote.ejbHome", type = KeyType.FAILURE) })
    public EjbJar testEJBHome() throws Exception {
        return validate(FooEJBHome.class);
    }

    @Keys( { @Key(value = "xml.remote.ejbLocalHome", type = KeyType.FAILURE), 
             @Key(value = "xml.home.ejbLocalHome", type = KeyType.FAILURE), 
             @Key(value = "xml.local.ejbLocalHome", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbLocalHome", type = KeyType.FAILURE),
             @Key(value = "xml.businessRemote.ejbLocalHome", type = KeyType.FAILURE) })
    public EjbJar testEJBLocalHome() throws Exception {
        return validate(FooEJBLocalHome.class);
    }

    @Keys( { @Key(value = "xml.remote.ejbLocalObject", type = KeyType.FAILURE), 
             @Key(value = "xml.home.ejbLocalObject", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.ejbLocalObject", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbLocalObject", type = KeyType.FAILURE),
             @Key(value = "xml.businessRemote.ejbLocalObject", type = KeyType.FAILURE) })
    public EjbJar testEJBLocalObject() throws Exception {
        return validate(FooEJBLocalObject.class);
    }

    @Keys( { @Key(value = "xml.remote.unknown", type = KeyType.FAILURE), 
             @Key(value = "xml.home.unknown", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.unknown", type = KeyType.FAILURE), 
             @Key(value = "xml.local.unknown", type = KeyType.FAILURE), 
             @Key(value = "xml.localRemote.conflict", type = KeyType.FAILURE) })
    public EjbJar testUnkown() throws Exception {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        return validate(FooUnknown.class);
    }

    @Keys( { @Key(value = "xml.remote.beanClass", type = KeyType.FAILURE), 
             @Key(value = "xml.home.beanClass", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.beanClass", type = KeyType.FAILURE), 
             @Key(value = "xml.local.beanClass", type = KeyType.FAILURE), 
             @Key(value = "xml.businessRemote.beanClass", type = KeyType.FAILURE),
             @Key(value = "xml.businessLocal.beanClass", type = KeyType.FAILURE) })
    public EjbJar testBeanClass() throws Exception {
        return validate(FooBean.class);
    }

    @Keys( { @Key(value = "xml.remote.notInterface", type = KeyType.FAILURE), 
             @Key(value = "xml.home.notInterface", type = KeyType.FAILURE), 
             @Key(value = "xml.localHome.notInterface", type = KeyType.FAILURE), 
             @Key(value = "xml.local.notInterface", type = KeyType.FAILURE),
             @Key(value = "xml.businessRemote.notInterface", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.notInterface", type = KeyType.FAILURE) })
    public EjbJar testNotInterface() throws Exception {
        return validate(FooClass.class);
    }

    @Keys( { @Key(value = "ann.notAnInterface", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.notInterface", type = KeyType.FAILURE), 
             @Key(value = "ann.localRemote.conflict", type = KeyType.FAILURE), 
             @Key(value = "ann.remoteOrLocal.ejbHome", type = KeyType.FAILURE),
             @Key(value = "xml.businessRemote.ejbHome", type = KeyType.FAILURE), 
             @Key(value = "ann.remoteOrLocal.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "xml.businessRemote.ejbObject", type = KeyType.FAILURE), 
             @Key(value = "ann.remoteOrLocal.ejbLocalHome", type = KeyType.FAILURE),
             @Key(value = "ann.remoteOrLocal.ejbLocalObject", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbLocalHome", type = KeyType.FAILURE), 
             @Key(value = "xml.businessLocal.ejbLocalObject", type = KeyType.FAILURE) })
    public EjbJar test() throws OpenEJBException {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(BBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(CBean.class));
        StatelessBean mybean1 = ejbJar.addEnterpriseBean(new StatelessBean("MyBean1", MyBean.class));
        return ejbJar;
    }
    @Keys({@Key(value="interface.beanOnlyAnnotation",type=KeyType.WARNING),
          @Key(value="interfaceMethod.beanOnlyAnnotation",type=KeyType.WARNING),
          @Key(value="aroundInvoke.invalidArguments", type = KeyType.FAILURE)})
    public EjbJar test1(){
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(DBean.class));
        return ejbJar;
    
    }
    @Keys({@Key(value="ann.remoteOrLocal.converse.parent",count=2, type = KeyType.FAILURE)})
    public EjbJar test2(){
      SystemInstance.get().setProperty("openejb.strict.interface.declaration", "true");
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(EBean.class));
        ejbJar.addEnterpriseBean(new StatelessBean(FBean.class));
        return ejbJar;
    
    }
    @After
    public void after() {
        SystemInstance.get().setProperty("openejb.strict.interface.declaration", "false");
    }

    private EjbJar validate(Class interfaceClass) throws OpenEJBException {
        EjbJar ejbJar = new EjbJar();
        StatelessBean bean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        bean.setHomeAndLocal(interfaceClass, interfaceClass);
        bean.setHomeAndRemote(interfaceClass, interfaceClass);
        bean.addBusinessLocal(interfaceClass);
        bean.addBusinessRemote(interfaceClass);
        return ejbJar;
    }

    public static class FooBean {}

    public static interface FooEJBHome extends EJBHome {}

    public static interface FooEJBObject extends EJBObject {}

    public static interface FooEJBLocalHome extends EJBLocalHome {}

    public static interface FooEJBLocalObject extends EJBLocalObject {}

    @Remote
    public static interface FooRemote {}

    @Local
    public static interface FooLocal {}

    public static interface FooUnknown {}

    public static class FooClass {}

    public static interface MyRemoteHome extends EJBHome {}

    public static interface MyLocalHome extends EJBLocalHome {}

    public static interface MyRemote extends EJBObject {}

    public static interface MyLocal extends EJBLocalObject {}

    @Remote( { MyRemoteHome.class, MyRemote.class })
    @Local( { MyLocalHome.class, MyLocal.class })
    public static class MyBean {}

    public static class ABean {}

    @Local(ABean.class)
    public static class BBean extends ABean {}

    public static interface C {}

    @Local(C.class)
    @Remote(C.class)
    public static class CBean {}
    @EJB
    public static interface D{
        @PostConstruct
        public void foo();
    }
    @Local(D.class)
    public static class DBean{
        public void foo(){}
        @AroundInvoke public Object bar(){return null;}
    }
    @Local
    public static interface E{}
    @Remote
    public static interface E1 extends E{}
    public static class EBean implements E1{}
    @Remote
    public static interface F{}
    @Local
    public static interface F1 extends F{}
    public static class FBean implements F1{}
}
