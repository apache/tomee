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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CheckMethods extends ValidationBase {

    public void validate(final EjbModule ejbModule) {

        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            if (!(bean instanceof RemoteBean)) {
                continue;
            }
            final RemoteBean b = (RemoteBean) bean;

            if (b.getHome() != null) {
                check_remoteInterfaceMethods(b);
                check_homeInterfaceMethods(b);
            }
            if (b.getLocalHome() != null) {
                check_localInterfaceMethods(b);
                check_localHomeInterfaceMethods(b);
            }

            check_unusedCreateMethods(b);
            check_unusedPostCreateMethods(b);

        }
    }

    private void check_localHomeInterfaceMethods(final RemoteBean b) {
        Class home = null;
        Class bean = null;
        try {
            home = loadClass(b.getLocalHome());
            bean = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        if (!EJBLocalHome.class.isAssignableFrom(home)) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
//            check_postCreateMethodsAreImplemented(b, bean, home);
        }
    }

    private void check_localInterfaceMethods(final RemoteBean b) {
        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = loadClass(b.getLocal());
            beanClass = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        if (!EJBLocalObject.class.isAssignableFrom(intrface)) {
            return;
        }

        final Method[] interfaceMethods = intrface.getMethods();

        for (Method interfaceMethod : interfaceMethods) {
            if (interfaceMethod.getDeclaringClass() == EJBLocalObject.class) {
                continue;
            }
            final String name = interfaceMethod.getName();
            try {
                final Class[] params = interfaceMethod.getParameterTypes();
                beanClass.getMethod(name, params);
            } catch (final NoSuchMethodException nsme) {
                final List<Method> differentArgs = new ArrayList<>();
                final List<Method> differentCase = new ArrayList<>();

                for (final Method method : beanClass.getMethods()) {
                    if (method.getName().equals(name)) {
                        differentArgs.add(method);
                    } else if (method.getName().equalsIgnoreCase(name)) {
                        differentCase.add(method);
                    }
                }

                if (differentArgs.size() > 0) {
                    fail(b, "no.busines.method.args", interfaceMethod.getName(), interfaceMethod.toString(), "local", intrface.getName(), beanClass.getName(), differentArgs.size());
                }
                if (differentCase.size() > 0) {
                    fail(b, "no.busines.method.case", interfaceMethod.getName(), interfaceMethod.toString(), "local", intrface.getName(), beanClass.getName(), differentCase.size());
                }
                if (differentArgs.size() == 0 && differentCase.size() == 0) {
                    fail(b, "no.busines.method", interfaceMethod.getName(), interfaceMethod.toString(), "local", intrface.getName(), beanClass.getName());
                }
            }
        }

    }

    private void check_remoteInterfaceMethods(final RemoteBean b) {

        Class intrface = null;
        Class beanClass = null;
        try {
            intrface = loadClass(b.getRemote());
            beanClass = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        if (!EJBObject.class.isAssignableFrom(intrface)) {
            return;
        }

        final Method[] interfaceMethods = intrface.getMethods();

        for (Method interfaceMethod : interfaceMethods) {
            if (interfaceMethod.getDeclaringClass() == EJBObject.class) {
                continue;
            }
            final String name = interfaceMethod.getName();
            try {
                final Class[] params = interfaceMethod.getParameterTypes();
                beanClass.getMethod(name, params);
            } catch (final NoSuchMethodException nsme) {
                final List<Method> differentArgs = new ArrayList<>();
                final List<Method> differentCase = new ArrayList<>();

                for (final Method method : beanClass.getMethods()) {
                    if (method.getName().equals(name)) {
                        differentArgs.add(method);
                    } else if (method.getName().equalsIgnoreCase(name)) {
                        differentCase.add(method);
                    }
                }

                if (differentArgs.size() > 0) {
                    fail(b, "no.busines.method.args", interfaceMethod.getName(), interfaceMethod.toString(), "remote", intrface.getName(), beanClass.getName(), differentArgs.size());
                }
                if (differentCase.size() > 0) {
                    fail(b, "no.busines.method.case", interfaceMethod.getName(), interfaceMethod.toString(), "remote", intrface.getName(), beanClass.getName(), differentCase.size());
                }
                if (differentArgs.size() == 0 && differentCase.size() == 0) {
                    fail(b, "no.busines.method", interfaceMethod.getName(), interfaceMethod.toString(), "remote", intrface.getName(), beanClass.getName());
                }
            }
        }
    }


    private void check_homeInterfaceMethods(final RemoteBean b) {
        Class home = null;
        Class bean = null;
        try {
            home = loadClass(b.getHome());
            bean = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        if (!EJBHome.class.isAssignableFrom(home)) {
            return;
        }

        if (check_hasCreateMethod(b, bean, home)) {
            check_createMethodsAreImplemented(b, bean, home);
            // ejbPostCreate methods are now automatically generated
//            check_postCreateMethodsAreImplemented(b, bean, home);
        }
    }

    public boolean check_hasCreateMethod(final RemoteBean b, final Class bean, final Class home) {

        if (b instanceof SessionBean && !jakarta.ejb.SessionBean.class.isAssignableFrom(bean)) {
            // This is a pojo-style bean
            return false;
        }

        final Method[] homeMethods = home.getMethods();

        boolean hasCreateMethod = false;

        for (int i = 0; i < homeMethods.length && !hasCreateMethod; i++) {
            hasCreateMethod = homeMethods[i].getName().startsWith("create");
        }

        if (!hasCreateMethod && !(b instanceof EntityBean)) {

            fail(b, "no.home.create", b.getHome(), b.getRemote());

        }

        return hasCreateMethod;
    }

    public boolean check_createMethodsAreImplemented(final RemoteBean b, final Class bean, final Class home) {
        boolean result = true;

        final Method[] homeMethods = home.getMethods();

        for (Method homeMethod : homeMethods) {
            if (!homeMethod.getName().startsWith("create")) {
                continue;
            }

            final Method create = homeMethod;

            final StringBuilder ejbCreateName = new StringBuilder(create.getName());
            ejbCreateName.replace(0, 1, "ejbC");

            try {
                if (jakarta.ejb.EnterpriseBean.class.isAssignableFrom(bean)) {
                    bean.getMethod(ejbCreateName.toString(), create.getParameterTypes());
                }
                // TODO: else { /* Check for Init method in pojo session bean class */ }
            } catch (final NoSuchMethodException e) {
                result = false;

                final String paramString = getParameters(create);

                if (b instanceof EntityBean) {
                    final EntityBean entity = (EntityBean) b;

                    fail(b, "entity.no.ejb.create", b.getEjbClass(), entity.getPrimKeyClass(), ejbCreateName.toString(), paramString);

                } else {
                    if (b instanceof SessionBean) {
                        final SessionBean sb = (SessionBean) b;
                        // Under EJB 3.1, it is not required that a stateless session bean have an ejbCreate method, even when it has a home interface
                        if (!sb.getSessionType().equals(SessionType.STATELESS)) {
                            fail(b, "session.no.ejb.create", b.getEjbClass(), ejbCreateName.toString(), paramString);
                        }
                    }
                }
            }
        }

        return result;
    }

    public boolean check_postCreateMethodsAreImplemented(final RemoteBean b, final Class bean, final Class home) {
        boolean result = true;

        if (b instanceof SessionBean) {
            return true;
        }

        final Method[] homeMethods = home.getMethods();
        final Method[] beanMethods = bean.getMethods();

        for (Method homeMethod : homeMethods) {
            if (!homeMethod.getName().startsWith("create")) {
                continue;
            }
            final Method create = homeMethod;
            final StringBuilder ejbPostCreateName = new StringBuilder(create.getName());
            ejbPostCreateName.replace(0, 1, "ejbPostC");
            try {
                bean.getMethod(ejbPostCreateName.toString(), create.getParameterTypes());
            } catch (final NoSuchMethodException e) {
                result = false;

                final String paramString = getParameters(create);

                fail(b, "no.ejb.post.create", b.getEjbClass(), ejbPostCreateName.toString(), paramString);

            }
        }

        return result;
    }

    public void check_unusedCreateMethods(final RemoteBean b) {

        Class home = null;
        Class localHome = null;
        Class bean = null;
        try {
            if (b.getLocalHome() != null) {
                localHome = loadClass(b.getLocalHome());
            }

            if (b.getHome() != null) {
                home = loadClass(b.getHome());
            }

            bean = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        for (final Method ejbCreate : bean.getMethods()) {

            if (!ejbCreate.getName().startsWith("ejbCreate")) {
                continue;
            }

            final StringBuilder create = new StringBuilder(ejbCreate.getName());
            create.replace(0, "ejbC".length(), "c");


            boolean inLocalHome = false;
            boolean inHome = false;

            try {
                if (localHome != null) {
                    localHome.getMethod(create.toString(), ejbCreate.getParameterTypes());
                    inLocalHome = true;
                }
            } catch (final NoSuchMethodException e) {
                // no-op
            }

            try {
                if (home != null) {
                    home.getMethod(create.toString(), ejbCreate.getParameterTypes());
                    inHome = true;
                }
            } catch (final NoSuchMethodException e) {
                // no-op
            }

            if (!inLocalHome && !inHome) {
                final String paramString = getParameters(ejbCreate);

                warn(b, "unused.ejb.create", b.getEjbClass(), ejbCreate.getName(), paramString, create.toString());
            }
        }
    }

    public void check_unusedPostCreateMethods(final RemoteBean b) {

        Class bean = null;
        try {
            bean = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            return;
        }

        for (final Method postCreate : bean.getMethods()) {

            if (!postCreate.getName().startsWith("ejbPostCreate")) {
                continue;
            }

            final StringBuilder ejbCreate = new StringBuilder(postCreate.getName());
            ejbCreate.replace(0, "ejbPostCreate".length(), "ejbCreate");

            try {
                bean.getMethod(ejbCreate.toString(), postCreate.getParameterTypes());
            } catch (final NoSuchMethodException e) {

                final String paramString = getParameters(postCreate);

                warn(b, "unused.ejbPostCreate", b.getEjbClass(), postCreate.getName(), paramString, ejbCreate.toString());

            }
        }
    }

/// public void check_findMethods(){
///     if(this.componentType == this.BMP_ENTITY ){
///
///         String beanMethodName = "ejbF"+method.getName().substring(1);
///         beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
///     }
/// }
///
/// public void check_homeMethods(){
///     String beanMethodName = "ejbHome"+method.getName().substring(0,1).toUpperCase()+method.getName().substring(1);
///     beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
/// }

}

