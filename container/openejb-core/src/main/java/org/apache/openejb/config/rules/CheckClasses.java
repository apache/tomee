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
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.Strings;
import org.apache.openejb.util.proxy.DynamicProxyImplFactory;
import org.apache.xbean.finder.ClassFinder;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EJBs;
import jakarta.ejb.Init;
import jakarta.ejb.Local;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.Remote;
import jakarta.ejb.Remove;
import jakarta.ejb.Timeout;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionManagement;
import jakarta.jws.WebService;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.isAbstract;

/**
 * @version $Rev$ $Date$
 */
public class CheckClasses extends ValidationBase {

    private static final List<Class<? extends Annotation>> beanOnlyAnnotations = new ArrayList<Class<? extends Annotation>>();

    static {
        beanOnlyAnnotations.add(PostConstruct.class);
        beanOnlyAnnotations.add(PreDestroy.class);
        beanOnlyAnnotations.add(Resource.class);
        beanOnlyAnnotations.add(Resources.class);
        beanOnlyAnnotations.add(DeclareRoles.class);
        beanOnlyAnnotations.add(DenyAll.class);
        beanOnlyAnnotations.add(PermitAll.class);
        beanOnlyAnnotations.add(RolesAllowed.class);
        beanOnlyAnnotations.add(RunAs.class);

        beanOnlyAnnotations.add(EJB.class);
        beanOnlyAnnotations.add(EJBs.class);
        beanOnlyAnnotations.add(Init.class);
        beanOnlyAnnotations.add(PostActivate.class);
        beanOnlyAnnotations.add(PrePassivate.class);
        beanOnlyAnnotations.add(Remove.class);
        beanOnlyAnnotations.add(Timeout.class);
        beanOnlyAnnotations.add(TransactionAttribute.class);
        beanOnlyAnnotations.add(TransactionManagement.class);
    }

    public void validate(final EjbModule ejbModule) {
        final ClassLoader loader = ejbModule.getClassLoader();
        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            try {
                final Class<?> beanClass = check_hasEjbClass(loader, bean);

                // All the subsequent checks require the bean class
                if (beanClass == null) {
                    continue;
                }

                if (!(bean instanceof RemoteBean)) {
                    continue;
                }

                if (bean instanceof SessionBean && ((SessionBean) bean).getProxy() != null) {
                    continue;
                }

                final RemoteBean b = (RemoteBean) bean;

                check_isEjbClass(b);
                check_hasDependentClasses(b, b.getEjbClass(), "ejb-class");
                check_hasInterface(b);

                if (b.getRemote() != null) {
                    checkInterface(loader, b, beanClass, "remote", b.getRemote());
                }

                if (b.getHome() != null) {
                    checkInterface(loader, b, beanClass, "home", b.getHome());
                }

                if (b.getLocal() != null) {
                    checkInterface(loader, b, beanClass, "local", b.getLocal());
                }

                if (b.getLocalHome() != null) {
                    checkInterface(loader, b, beanClass, "local-home", b.getLocalHome());
                }

                if (b instanceof SessionBean) {
                    final SessionBean sessionBean = (SessionBean) b;

                    for (final String interfce : sessionBean.getBusinessLocal()) {
                        checkInterface(loader, b, beanClass, "business-local", interfce);
                    }

                    for (final String interfce : sessionBean.getBusinessRemote()) {
                        checkInterface(loader, b, beanClass, "business-remote", interfce);
                    }
                }
            } catch (final RuntimeException e) {
                throw new OpenEJBRuntimeException(bean.getEjbName(), e);
            }
        }

        for (final Interceptor interceptor : ejbModule.getEjbJar().getInterceptors()) {
            check_hasInterceptorClass(loader, interceptor);
        }
    }

    private void checkInterface(final ClassLoader loader, final RemoteBean b, final Class<?> beanClass, String tag, final String className) {
        final Class<?> interfce = lookForClass(loader, className, tag, b.getEjbName());

        if (interfce == null) {
            return;
        }

        check_hasDependentClasses(b, className, tag);

        tag = Strings.lcfirst(Strings.camelCase(tag));

        isValidInterface(b, interfce, beanClass, tag);

        final ClassFinder finder = new ClassFinder(interfce);

        for (final Class<? extends Annotation> annotation : beanOnlyAnnotations) {

            if (interfce.isAnnotationPresent(annotation)) {
                warn(b, "interface.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), b.getEjbClass());
            }

            for (final Method method : finder.findAnnotatedMethods(annotation)) {
                warn(b, "interfaceMethod.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), method.getName(), b.getEjbClass());
            }
        }

    }

    private void check_hasInterface(final RemoteBean b) {
        if (b.getRemote() != null) {
            return;
        }
        if (b.getLocal() != null) {
            return;
        }

        Class<?> beanClass = null;
        try {
            beanClass = loadClass(b.getEjbClass());
        } catch (final OpenEJBException e) {
            // no-op
        }

        if (b instanceof EntityBean) {
            fail(b, "noInterfaceDeclared.entity", beanClass.getSimpleName());
            return;
        }

        if (b.getBusinessLocal().size() > 0) {
            return;
        }
        if (b.getBusinessRemote().size() > 0) {
            return;
        }

        if (((SessionBean) b).getServiceEndpoint() != null) {
            return;
        }

        if (beanClass.isAnnotationPresent(WebService.class)) {
            return;
        }

        //fail(b, "noInterfaceDeclared.session");
    }

    private void check_hasDependentClasses(final RemoteBean b, final String className, final String type) {
        try {
            final ClassLoader cl = module.getClassLoader();
            final Class<?> clazz = cl.loadClass(className);
            for (final Object item : clazz.getFields()) {
                item.toString();
            }
            for (final Object item : clazz.getMethods()) {
                item.toString();
            }
            for (final Object item : clazz.getConstructors()) {
                item.toString();
            }
            for (final Object item : clazz.getAnnotations()) {
                item.toString();
            }
            // checking for any declared enum constants
            for (final Class klass : clazz.getClasses()) {
                if (klass.isEnum()) {
                    klass.toString();
                }
            }
        } catch (final ClassNotFoundException | NoClassDefFoundError e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */
            final String missingClass = e.getMessage();
            fail(b, "missing.dependent.class", className, missingClass, type, b.getEjbName());
        }
    }

    public Class<?> check_hasEjbClass(final ClassLoader loader, final EnterpriseBean b) {

        final String ejbName = b.getEjbName();

        final Class<?> beanClass = lookForClass(loader, b.getEjbClass(), "ejb-class", ejbName);
        final boolean isDynamicProxyImpl = DynamicProxyImplFactory.isKnownDynamicallyImplemented(beanClass);

        if (beanClass == null) {
            return null;
        }

        if (beanClass.isInterface() && !isDynamicProxyImpl) {
            fail(ejbName, "interfaceDeclaredAsBean", beanClass.getName());
        }

        if (isCmp(b)) {
            return beanClass;
        }

        if (isAbstract(beanClass.getModifiers()) && !isAbstractAllowed(beanClass)) {
            fail(ejbName, "abstractDeclaredAsBean", beanClass.getName());
        }

        return beanClass;
    }

    public static boolean isAbstractAllowed(final Class clazz) {
        if (DynamicProxyImplFactory.isKnownDynamicallyImplemented(clazz)) {
            return true;
        }
        if (DynamicSubclass.isDynamic(clazz)) {
            return true;
        }
        return false;
    }

    private void check_hasInterceptorClass(final ClassLoader loader, final Interceptor i) {

        lookForClass(loader, i.getInterceptorClass(), "interceptor-class", "Interceptor");

    }

    private void check_isEjbClass(final RemoteBean b) {
        if (b instanceof SessionBean) { //NOPMD
            // DMB: Beans in ejb 3 are not required to implement jakarta.ejb.SessionBean
            // but it would still be nice to think of some sort of check to do here.
            // compareTypes(b, b.getEjbClass(), jakarta.ejb.SessionBean.class);

        } else if (b instanceof EntityBean) {
            compareTypes(b, b.getEjbClass(), jakarta.ejb.EntityBean.class);
        }
    }

    private Class<?> lookForClass(final ClassLoader loader, final String clazz, final String type, final String ejbName) {
        try {
            return loadClass(loader, clazz);
        } catch (final OpenEJBException e) {
            /*
            # 0 - Class name
            # 1 - Element (home, ejb-class, remote)
            # 2 - Bean name
            */

            fail(ejbName, "missing.class", clazz, type, ejbName);

        } catch (final NoClassDefFoundError e) {
            /*
             # 0 - Class name
             # 1 - Element (home, ejb-class, remote)
             # 2 - Bean name
             # 3 - Misslocated Class name
             */
            fail(ejbName, "misslocated.class", clazz, type, ejbName, e.getMessage());

            throw e;
        }

        return null;
    }

    private boolean isValidInterface(final RemoteBean b, final Class clazz, final Class beanClass, final String tag) {

        if (clazz.equals(beanClass)) {

            fail(b, "xml." + tag + ".beanClass", clazz.getName());

        } else if (!clazz.isInterface()) {

            fail(b, "xml." + tag + ".notInterface", clazz.getName());

        } else if (EJBHome.class.isAssignableFrom(clazz)) {

            if (tag.equals("home")) {
                return true;
            }

            fail(b, "xml." + tag + ".ejbHome", clazz.getName());

        } else if (EJBLocalHome.class.isAssignableFrom(clazz)) {

            if (tag.equals("localHome")) {
                return true;
            }

            fail(b, "xml." + tag + ".ejbLocalHome", clazz.getName());

        } else if (EJBObject.class.isAssignableFrom(clazz)) {

            if (tag.equals("remote")) {
                return true;
            }

            fail(b, "xml." + tag + ".ejbObject", clazz.getName());

        } else if (EJBLocalObject.class.isAssignableFrom(clazz)) {

            if (tag.equals("local")) {
                return true;
            }

            fail(b, "xml." + tag + ".ejbLocalObject", clazz.getName());

        } else {
            if (tag.equals("businessLocal") || tag.equals("businessRemote")) {

                return true;

            } else if (clazz.isAnnotationPresent(Local.class)) {

                fail(b, "xml." + tag + ".businessLocal", clazz.getName());

            } else if (clazz.isAnnotationPresent(Remote.class)) {

                fail(b, "xml." + tag + ".businessRemote", clazz.getName());

            } else {

                fail(b, "xml." + tag + ".unknown", clazz.getName());

            }

        }

        // must be tagged as <home>, <local-home>, <remote>, or <local>

        return false;
    }

    private void compareTypes(final RemoteBean b, final String clazz1, final Class<?> class2) {
        Class<?> class1 = null;
        try {
            class1 = loadClass(clazz1);
        } catch (final OpenEJBException e) {
            return;
        }

        if (class1 != null && !class2.isAssignableFrom(class1)) {
            fail(b, "wrong.class.type", clazz1, class2.getName());
        }
    }

    protected Class<?> loadClass(final ClassLoader cl, final String clazz) throws OpenEJBException {
        try {
            return Class.forName(clazz, false, cl == null ? module.getClassLoader() : cl);
        } catch (final ClassNotFoundException cnfe) {
            throw new OpenEJBException(
                    new Messages("org.apache.openejb.util.resources").format("cl0007", clazz, module.getJarLocation()), cnfe);
        }
    }
}

