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
import org.apache.openejb.util.Strings;
import org.apache.xbean.finder.ClassFinder;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.jws.WebService;
import static java.lang.reflect.Modifier.isAbstract;
import java.lang.reflect.Method;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
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
                Class<?> beanClass = check_hasEjbClass(bean);

                if (!(bean instanceof RemoteBean)) continue;
                RemoteBean b = (RemoteBean) bean;

                check_isEjbClass(b);
                check_hasDependentClasses(b, b.getEjbClass(), "ejb-class");
                check_hasInterface(b);

                if (b.getRemote() != null){
                    checkInterface(b, beanClass, "remote", b.getRemote());
                }

                if (b.getHome() != null) {
                    checkInterface(b, beanClass, "home", b.getHome());
                }

                if (b.getLocal() != null) {
                    checkInterface(b, beanClass, "local", b.getLocal());
                }

                if (b.getLocalHome() != null) {
                    checkInterface(b, beanClass, "local-home", b.getLocalHome());
                }

                if (b instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) b;

                    for (String interfce : sessionBean.getBusinessLocal()) {
                        checkInterface(b, beanClass, "business-local", interfce);
                    }

                    for (String interfce : sessionBean.getBusinessRemote()) {
                        checkInterface(b, beanClass, "business-remote", interfce);
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

    private void checkInterface(RemoteBean b, Class<?> beanClass, String tag, String className) {
        Class<?> interfce = lookForClass(className, tag, b.getEjbName());

        if (interfce == null) return;

        check_hasDependentClasses(b, className, tag);

        tag = Strings.lcfirst(Strings.camelCase(tag));

        if (isValidInterface(b, interfce, beanClass, tag));

        ClassFinder finder = new ClassFinder(interfce);

        for (Class<? extends Annotation> annotation : beanOnlyAnnotations) {

            if (interfce.isAnnotationPresent(annotation)){
                warn(b, "interface.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), b.getEjbClass());
            }

            for (Method method : finder.findAnnotatedMethods(annotation)) {
                warn(b, "interfaceMethod.beanOnlyAnnotation", annotation.getSimpleName(), interfce.getName(), method.getName(), b.getEjbClass());
            }
        }

    }

    private void check_hasInterface(RemoteBean b) {
        if (b.getRemote() != null) return;
        if (b.getLocal() != null) return;

        Class<?> beanClass = null;
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

        if (beanClass.isAnnotationPresent(WebService.class)) return;

        //fail(b, "noInterfaceDeclared.session");
    }

    private void check_hasDependentClasses(RemoteBean b, String className, String type) {
        try {
            ClassLoader cl = module.getClassLoader();
            Class<?> clazz = cl.loadClass(className);
            for (Object item : clazz.getFields()) { item.toString(); }
            for (Object item : clazz.getMethods()) { item.toString(); }
            for (Object item : clazz.getConstructors()) { item.toString(); }
            for (Object item : clazz.getAnnotations()) { item.toString(); }
            // checking for any declared enum constants
            for(Class klass: clazz.getClasses()){
            	if(klass.isEnum()){
            		klass.toString();
            	}
            }
        } catch (ClassNotFoundException e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */
            String missingClass = e.getMessage();
            fail(b, "missing.dependent.class", className, missingClass, type, b.getEjbName());
        } catch (NoClassDefFoundError e) {
            /*
            # 0 - Referring Class name
            # 1 - Dependent Class name
            # 2 - Element (home, ejb-class, remote)
            # 3 - Bean name
            */
            String missingClass = e.getMessage();
            fail(b, "missing.dependent.class", className, missingClass, type, b.getEjbName());
        }
    }

    public Class<?> check_hasEjbClass(EnterpriseBean b) {

        String ejbName = b.getEjbName();

        Class<?> beanClass = lookForClass(b.getEjbClass(), "<ejb-class>", ejbName);

        if (beanClass.isInterface()){
            fail(ejbName, "interfaceDeclaredAsBean", beanClass.getName());
        }

        if (isCmp(b)) return beanClass;

        if (isAbstract(beanClass.getModifiers())){
            fail(ejbName, "abstractDeclaredAsBean", beanClass.getName());
        }

        return beanClass;
    }

    private void check_hasInterceptorClass(Interceptor i) {

        lookForClass(i.getInterceptorClass(), "interceptor-class", "Interceptor");

    }

    private void check_isEjbClass(RemoteBean b) {

        if (b instanceof SessionBean) {

            // DMB: Beans in ejb 3 are not required to implement javax.ejb.SessionBean
            // but it would still be nice to think of some sort of check to do here.
            // compareTypes(b, b.getEjbClass(), javax.ejb.SessionBean.class);

        } else if (b instanceof EntityBean) {

            compareTypes(b, b.getEjbClass(), javax.ejb.EntityBean.class);

        }

    }

    private Class<?> lookForClass(String clazz, String type, String ejbName) {
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

    private boolean isValidInterface(RemoteBean b, Class clazz, Class beanClass, String tag) {

        if (clazz.equals(beanClass)) {

            fail(b, "xml." + tag + ".beanClass", clazz.getName());

        } else if (!clazz.isInterface()) {

            fail(b, "xml." + tag + ".notInterface", clazz.getName());

        } else if (EJBHome.class.isAssignableFrom(clazz)) {

            if (tag.equals("home")) return true;

            fail(b, "xml." + tag + ".ejbHome", clazz.getName());

        } else if (EJBLocalHome.class.isAssignableFrom(clazz)) {

            if (tag.equals("localHome")) return true;

            fail(b, "xml." + tag + ".ejbLocalHome", clazz.getName());

        } else if (EJBObject.class.isAssignableFrom(clazz)) {

            if (tag.equals("remote")) return true;

            fail(b, "xml." + tag + ".ejbObject", clazz.getName());

        } else if (EJBLocalObject.class.isAssignableFrom(clazz)) {

            if (tag.equals("local")) return true;

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

    private void compareTypes(RemoteBean b, String clazz1, Class<?> class2) {
        Class<?> class1 = null;
        try {
            class1 = loadClass(clazz1);
        } catch (OpenEJBException e) {
            return;
        }

        if (class1 != null && !class2.isAssignableFrom(class1)) {
            fail(b, "wrong.class.type", clazz1, class2.getName());
        }
    }

    protected Class<?> loadClass(String clazz) throws OpenEJBException {
        ClassLoader cl = module.getClassLoader();
        try {
            return Class.forName(clazz, false, cl);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", clazz, module.getJarLocation()), cnfe);
        }
    }
}

