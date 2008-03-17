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
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.util.SafeToolkit;
import org.apache.xbean.finder.ClassFinder;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.jws.WebService;
import static java.lang.reflect.Modifier.isAbstract;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class CheckClasses extends ValidationBase {

    private static final List<Class<? extends Annotation>> beanOnlyAnnotations = new ArrayList<Class<? extends Annotation>>();

    static {
        beanOnlyAnnotations.add(javax.annotation.PostConstruct.class);
        beanOnlyAnnotations.add(javax.annotation.PreDestroy.class);
        beanOnlyAnnotations.add(javax.annotation.Resource.class);
        beanOnlyAnnotations.add(javax.annotation.Resources.class);
        beanOnlyAnnotations.add(javax.annotation.security.DeclareRoles.class);
        beanOnlyAnnotations.add(javax.annotation.security.DenyAll.class);
        beanOnlyAnnotations.add(javax.annotation.security.PermitAll.class);
        beanOnlyAnnotations.add(javax.annotation.security.RolesAllowed.class);
        beanOnlyAnnotations.add(javax.annotation.security.RunAs.class);

        beanOnlyAnnotations.add(javax.ejb.EJB.class);
        beanOnlyAnnotations.add(javax.ejb.EJBs.class);
        beanOnlyAnnotations.add(javax.ejb.Init.class);
        beanOnlyAnnotations.add(javax.ejb.PostActivate.class);
        beanOnlyAnnotations.add(javax.ejb.PrePassivate.class);
        beanOnlyAnnotations.add(javax.ejb.Remove.class);
        beanOnlyAnnotations.add(javax.ejb.Timeout.class);
        beanOnlyAnnotations.add(javax.ejb.TransactionAttribute.class);
        beanOnlyAnnotations.add(javax.ejb.TransactionManagement.class);
    }

    public void validate(EjbModule ejbModule) {
        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            try {
                check_hasEjbClass(bean);

                if (!(bean instanceof RemoteBean)) continue;
                RemoteBean b = (RemoteBean) bean;

                check_isEjbClass(b);
                check_hasDependentClasses(b, b.getEjbClass(), "<ejb-class>");
//                check_hasInterface(b);
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

                if (b instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) b;
                    for (String interfce : sessionBean.getBusinessLocal()) {
                        check_businessInterface(sessionBean, interfce, "<business-local>");
                    }
                    for (String interfce : sessionBean.getBusinessRemote()) {
                        check_businessInterface(sessionBean, interfce, "<business-remote>");
                    }
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(bean.getEjbName(), e);
            }
        }

        for (Interceptor interceptor : ejbModule.getEjbJar().getInterceptors()) {
            check_hasInterceptorClass(interceptor);
        }
    }

    private void check_businessInterface(SessionBean b, String interfaceName, String tagName) {
        String ejbName = b.getEjbName();
        Class interfce = lookForClass(interfaceName, tagName, b.getEjbName());

        if (!interfce.isInterface()){
            fail(b, "notAnInterface", interfce.getName(), tagName);
        }

        ClassFinder finder = new ClassFinder(interfce);

        for (Class<? extends Annotation> annotation : beanOnlyAnnotations) {
            if (interfce.getAnnotation(annotation) != null){
                warn(b, "interface.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), b.getEjbClass());
            }
            for (Method method : finder.findAnnotatedMethods(annotation)) {
                warn(b, "interfaceMethod.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), method.getName(), b.getEjbClass());
            }
        }

        if (EJBHome.class.isAssignableFrom(interfce)){
            fail(ejbName, "xml.remoteOrLocal.ejbHome", tagName, interfce.getName());
        } else if (EJBObject.class.isAssignableFrom(interfce)){
            fail(ejbName, "xml.remoteOrLocal.ejbObject", tagName, interfce.getName());
        } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
            fail(ejbName, "xml.remoteOrLocal.ejbLocalHome", tagName, interfce.getName());
        } else if (EJBLocalObject.class.isAssignableFrom(interfce)){
            fail(ejbName, "xml.remoteOrLocal.ejbLocalObject", tagName, interfce.getName());
        }

    }

    private void check_hasInterface(RemoteBean b) {
        if (b.getRemote() != null) return;
        if (b.getLocal() != null) return;

        Class beanClass = null;
        try {
            beanClass = loadClass(b.getEjbClass());
        } catch (OpenEJBException e) {
        }

        if (b instanceof EntityBean){
            fail(b, "noInterfaceDeclared.entity", beanClass.getSimpleName());
            return;
        }

        if (b.getBusinessLocal().size() > 0) return;
        if (b.getBusinessRemote().size() > 0) return;

        if (((SessionBean) b).getServiceEndpoint() != null) return;

        if (beanClass.getAnnotation(WebService.class) != null) return;

        fail(b, "noInterfaceDeclared.session");
    }

    private void check_hasDependentClasses(RemoteBean b, String className, String type) {
        try {
            ClassLoader cl = module.getClassLoader();
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
            fail(b, "missing.dependent.class", className, e.getMessage(), type, b.getEjbName());
        } catch (NoClassDefFoundError e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */
            fail(b, "missing.dependent.class", className, e.getMessage(), type, b.getEjbName());
        }
    }

    private void check_hasLocalClass(RemoteBean b) {
        lookForClass(b.getLocal(), "<local>", b.getEjbName());
    }

    private void check_hasLocalHomeClass(RemoteBean b) {
        lookForClass(b.getLocalHome(), "<local-home>", b.getEjbName());
    }

    public void check_hasEjbClass(EnterpriseBean b) {

        String ejbName = b.getEjbName();

        Class beanClass = lookForClass(b.getEjbClass(), "<ejb-class>", ejbName);

        if (beanClass.isInterface()){
            fail(ejbName, "interfaceDeclaredAsBean", beanClass.getName());
        }

        if (isCmp(b)) return;

        if (isAbstract(beanClass.getModifiers())){
            fail(ejbName, "abstractDeclaredAsBean", beanClass.getName());
        }
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

    private Class lookForClass(String clazz, String type, String ejbName) {
        try {
            return loadClass(clazz);
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

        return null;
    }

    private void compareTypes(RemoteBean b, String clazz1, Class class2) {
        Class class1 = null;
        try {
            class1 = loadClass(clazz1);
        } catch (OpenEJBException e) {
            return;
        }

        if (class1 != null && !class2.isAssignableFrom(class1)) {
            fail(b, "wrong.class.type", clazz1, class2.getName());
        }
    }

    protected Class loadClass(String clazz) throws OpenEJBException {
        ClassLoader cl = module.getClassLoader();
        try {
            return Class.forName(clazz, false, cl);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", clazz, module.getJarLocation()), cnfe);
        }
    }
}

