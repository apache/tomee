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

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class InvalidInterfacesTest  {
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
    @Keys( { @Key("xml.remote.businessLocal"), @Key("xml.home.businessLocal"),@Key("xml.local.businessLocal"), @Key("xml.localHome.businessLocal") })
    public EjbJar testBusinessLocal() throws Exception {
        return validate(FooLocal.class);
    }
    @Keys( { @Key("xml.remote.businessRemote"), @Key("xml.home.businessRemote"),@Key("xml.local.businessRemote"), @Key("xml.localHome.businessRemote") })
    public EjbJar testBusinessRemote() throws Exception {
        return validate(FooRemote.class);
    }
    @Keys( { @Key("xml.home.ejbObject"), @Key("xml.local.ejbObject"),@Key("xml.localHome.ejbObject"), @Key("xml.businessLocal.ejbObject"), @Key("xml.businessRemote.ejbObject") })
    public EjbJar testEJBObject() throws Exception {
        return validate(FooEJBObject.class);
    }
    @Keys( { @Key("xml.remote.ejbHome"), @Key("xml.local.ejbHome"),@Key("xml.localHome.ejbHome"), @Key("xml.businessLocal.ejbHome"), @Key("xml.businessRemote.ejbHome") })
    public EjbJar testEJBHome() throws Exception {
        return validate(FooEJBHome.class);
    }
    @Keys( { @Key("xml.remote.ejbLocalHome"), @Key("xml.home.ejbLocalHome"),@Key("xml.local.ejbLocalHome"), @Key("xml.businessLocal.ejbLocalHome"), @Key("xml.businessRemote.ejbLocalHome") })
    public EjbJar testEJBLocalHome() throws Exception {
        return validate(FooEJBLocalHome.class);
    }
    @Keys( { @Key("xml.remote.ejbLocalObject"), @Key("xml.home.ejbLocalObject"),@Key("xml.localHome.ejbLocalObject"), @Key("xml.businessLocal.ejbLocalObject"), @Key("xml.businessRemote.ejbLocalObject") })
    public EjbJar testEJBLocalObject() throws Exception {
        return validate(FooEJBLocalObject.class);
    }
    @Keys( { @Key("xml.remote.unknown"), @Key("xml.home.unknown"),@Key("xml.localHome.unknown"), @Key("xml.local.unknown")})
    public EjbJar testUnkown() throws Exception {
        return validate(FooUnknown.class);
    }
    @Keys( { @Key("xml.remote.beanClass"), @Key("xml.home.beanClass"),@Key("xml.localHome.beanClass"), @Key("xml.local.beanClass"), @Key("xml.businessRemote.beanClass"), @Key("xml.businessLocal.beanClass") })
    public EjbJar testBeanClass() throws Exception {
        return validate(FooBean.class);
    }
    @Keys( { @Key("xml.remote.notInterface"), @Key("xml.home.notInterface"),@Key("xml.localHome.notInterface"), @Key("xml.local.notInterface"), @Key("xml.businessRemote.notInterface"), @Key("xml.businessLocal.notInterface") })
    public EjbJar testNotInterface() throws Exception {
        return validate(FooClass.class);
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
}
