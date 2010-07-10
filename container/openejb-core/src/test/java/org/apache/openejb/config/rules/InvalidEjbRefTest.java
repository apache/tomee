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

import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;

import junit.framework.TestCase;

import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.junit.runner.RunWith;

/**
 * @version $Rev$ $Date$
 */
@RunWith(ValidationRunner.class)
public class InvalidEjbRefTest extends TestCase {
    @Keys({@Key("ann.ejb.ejbObject"),@Key("ann.ejb.ejbLocalObject"),@Key("ann.ejb.beanClass"),@Key("ann.ejb.notInterface")})
    public EjbJar test() throws Exception {

        EjbJar ejbJar = new EjbJar();

        ejbJar.addEnterpriseBean(new StatelessBean(EjbRefBean.class));

        StatelessBean fooBean = ejbJar.addEnterpriseBean(new StatelessBean(FooBean.class));
        fooBean.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        fooBean.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        fooBean.addBusinessLocal(FooLocal.class.getName());
        fooBean.addBusinessRemote(FooRemote.class.getName());

        StatelessBean fooImpl = ejbJar.addEnterpriseBean(new StatelessBean(FooImpl.class));
        fooImpl.setHomeAndRemote(FooEJBHome.class, FooEJBObject.class);
        fooImpl.setHomeAndLocal(FooEJBLocalHome.class, FooEJBLocalObject.class);
        fooImpl.addBusinessLocal(FooLocal.class.getName());
        fooImpl.addBusinessRemote(FooRemote.class.getName());

        return ejbJar;
    }

    public static class EjbRefBean implements EjbRefBeanLocal {
        // valid because fooBean will be a LocalBean (because it has no interfaces)
        @EJB
        private FooBean fooBean;

        // valid
        @EJB
        private FooEJBHome fooEJBHome;

        // invalid
        @EJB
        private FooEJBObject fooEJBObject;

        // valid
        @EJB
        private FooEJBLocalHome fooEJBLocalHome;

        // invalid
        @EJB
        private FooEJBLocalObject fooEJBLocalObject;

        // valid
        @EJB
        private FooRemote fooRemote;

        // valid
        @EJB
        private FooLocal fooLocal;

        // valid
        @EJB
        private FooUnknown fooUnknown;

        // invalid
        @EJB
        private FooClass fooClass;


        // invalid because we refer to the bean class, but this bean is not a LocalBeanm
        @EJB
        private FooImpl fooImpl;
    }

    public static interface EjbRefBeanLocal {

    }

    public static class FooBean {

    }

    public static interface FooEJBHome extends EJBHome {
        FooEJBObject create();
    }

    public static interface FooEJBObject extends EJBObject {
    }

    public static interface FooEJBLocalHome extends EJBLocalHome {
        FooEJBLocalObject create();
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
    
    public static class FooImpl implements FooLocal {
    }
}