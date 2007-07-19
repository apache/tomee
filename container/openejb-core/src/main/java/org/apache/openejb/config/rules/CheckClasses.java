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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.config.EjbSet;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.util.SafeToolkit;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;

public class CheckClasses extends ValidationBase {

    private EjbSet set;
    private ClassLoader classLoader;

    public void validate(EjbSet set) {
        this.set = set;

        for (EnterpriseBean bean : set.getJar().getEnterpriseBeans()) {
            try {
                check_hasEjbClass(bean);

                if (!(bean instanceof RemoteBean)) continue;
                RemoteBean b = (RemoteBean) bean;

                check_isEjbClass(b);
                check_hasDependentClasses(b, b.getEjbClass(), "<ejb-class>");
                if (b.getHome() != null) {
                    check_hasHomeClass(b);
                    check_hasRemoteClass(b);
                    check_isHomeInterface(b);
                    check_isRemoteInterface(b);
                    check_hasDependentClasses(b, b.getHome(), "<home>");
                    check_hasDependentClasses(b, b.getRemote(), "<remote>");
                }
                if (b.getLocalHome() != null) {
                    check_hasLocalHomeClass(b);
                    check_hasLocalClass(b);
                    check_isLocalHomeInterface(b);
                    check_isLocalInterface(b);
                    check_hasDependentClasses(b, b.getLocalHome(), "<local-home>");
                    check_hasDependentClasses(b, b.getLocal(), "<local>");
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(bean.getEjbName(), e);
            }
        }

        for (Interceptor interceptor : set.getEjbJar().getInterceptors()) {
            check_hasInterceptorClass(interceptor);
        }
    }

    private void check_hasDependentClasses(RemoteBean b, String className, String type) {
        try {
            ClassLoader cl = set.getClassLoader();
            Class clazz = cl.loadClass(className);
            for (Object item : clazz.getFields()) { item.toString(); }
            for (Object item : clazz.getMethods()) { item.toString(); }
            for (Object item : clazz.getConstructors()) { item.toString(); }
            for (Object item : clazz.getAnnotations()) { item.toString(); }
            for (Object item : clazz.getEnumConstants()) { item.toString(); }
        } catch (NullPointerException e) {
            // Don't know why I get these from clazz.getEnumConstants() 
        } catch (ClassNotFoundException e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */
            ValidationFailure failure = new ValidationFailure("missing.dependent.class");
            failure.setDetails(className, e.getMessage(), type, b.getEjbName());
            failure.setComponentName(b.getEjbName());

            set.addFailure(failure);
        } catch (NoClassDefFoundError e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */

            ValidationFailure failure = new ValidationFailure("missing.dependent.class");
            failure.setDetails(className, e.getMessage(), type, b.getEjbName());
            failure.setComponentName(b.getEjbName());

            set.addFailure(failure);
        }
    }

    private void check_hasLocalClass(RemoteBean b) {
        lookForClass(b.getLocal(), "<local>", b.getEjbName());
    }

    private void check_hasLocalHomeClass(RemoteBean b) {
        lookForClass(b.getLocalHome(), "<local-home>", b.getEjbName());
    }

    public void check_hasEjbClass(EnterpriseBean b) {

        lookForClass(b.getEjbClass(), "<ejb-class>", b.getEjbName());

    }

    public void check_hasInterceptorClass(Interceptor i) {

        lookForClass(i.getInterceptorClass(), "<interceptor-class>", "Interceptor");

    }

    public void check_hasHomeClass(RemoteBean b) {

        lookForClass(b.getHome(), "<home>", b.getEjbName());

    }

    public void check_hasRemoteClass(RemoteBean b) {

        lookForClass(b.getRemote(), "<remote>", b.getEjbName());

    }

    public void check_isEjbClass(RemoteBean b) {

        if (b instanceof SessionBean) {

            // DMB: Beans in ejb 3 are not required to implement javax.ejb.SessionBean
            // but it would still be nice to think of some sort of check to do here.
            // compareTypes(b, b.getEjbClass(), javax.ejb.SessionBean.class);

        } else if (b instanceof EntityBean) {

            compareTypes(b, b.getEjbClass(), javax.ejb.EntityBean.class);

        }

    }

    private void check_isLocalInterface(RemoteBean b) {
        compareTypes(b, b.getLocal(), EJBLocalObject.class);
    }

    private void check_isLocalHomeInterface(RemoteBean b) {
        compareTypes(b, b.getLocalHome(), EJBLocalHome.class);
    }

    public void check_isHomeInterface(RemoteBean b) {

        compareTypes(b, b.getHome(), javax.ejb.EJBHome.class);

    }

    public void check_isRemoteInterface(RemoteBean b) {

        compareTypes(b, b.getRemote(), javax.ejb.EJBObject.class);

    }

    private void lookForClass(String clazz, String type, String ejbName) {
        try {
            loadClass(clazz);
        } catch (OpenEJBException e) {
            /*
            # 0 - Class name
            # 1 - Element (home, ejb-class, remote)
            # 2 - Bean name
            */

            fail(ejbName, "missing.class", clazz, type, ejbName);

        } catch (NoClassDefFoundError e) {
            /*
             # 0 - Class name
             # 1 - Element (home, ejb-class, remote)
             # 2 - Bean name
             # 3 - Misslocated Class name
             */
            fail(ejbName, "misslocated.class", clazz, type, ejbName, e.getMessage());

            throw e;
        }

    }

    private void compareTypes(RemoteBean b, String clazz1, Class class2) {
        Class class1 = null;
        try {
            class1 = loadClass(clazz1);
        } catch (OpenEJBException e) {
            return;
        }

        if (class1 != null && !class2.isAssignableFrom(class1)) {
            ValidationFailure failure = new ValidationFailure("wrong.class.type");
            failure.setDetails(clazz1, class2.getName());
            failure.setComponentName(b.getEjbName());

            set.addFailure(failure);

        }
    }

    protected Class loadClass(String clazz) throws OpenEJBException {
        ClassLoader cl = set.getClassLoader();
        try {
            return Class.forName(clazz, true, cl);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", clazz, set.getJarPath()), cnfe);
        }
    }
}

