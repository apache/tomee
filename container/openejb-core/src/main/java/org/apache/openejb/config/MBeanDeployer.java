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
package org.apache.openejb.config;

import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.management.MBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.util.AnnotationUtil;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ClassFinder;

/**
 * @author Romain Manni-Bucau
 */
public class MBeanDeployer implements DynamicDeployer {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, MBeanDeployer.class);
    private static final MBeanServer server = LocalMBeanServer.get();

    private static final String OPENEJB_MBEAN_CLASSES_PROPERTY = "openejb.user.mbeans.list";
    private static final String OPENEJB_MBEAN_CLASSES_SPLIT = ",";
    private static final String OPENEJB_MBEAN_FORCE_FINDER = "*";

    @Override public AppModule deploy(AppModule appModule) throws OpenEJBException {
        logger.debug("looking for annotated MBeans in " + appModule.getModuleId());
        Set<String> mbeans = new TreeSet<String>();

        deploy(mbeans, appModule.getClassLoader(), appModule.getModuleId());
        List<String> done = new ArrayList<String>();
        for (WebModule webModule : appModule.getWebModules()) {
            deploy(mbeans, webModule.getClassLoader(), webModule.getModuleId());
            done.add(webModule.getJarLocation());
        }
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            if (!done.contains(ejbModule.getJarLocation())) {
                deploy(mbeans, ejbModule.getClassLoader(), ejbModule.getModuleId());
                done.add(ejbModule.getJarLocation());
            }
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            if (!done.contains(clientModule.getJarLocation())) {
                deploy(mbeans, clientModule.getClassLoader(), clientModule.getModuleId());
                done.add(clientModule.getJarLocation());
            }
        }

        List<URL> libs = appModule.getAdditionalLibraries();
        Iterator<URL> it = libs.iterator();
        while (it.hasNext()) {
            URL url = it.next();
            for (String location : done) {
                if (url.getFile().equals(location)) {
                    it.remove();
                }
            }
        }
        if (libs.size() > 0) {
            ClassLoader additionnalLibCl = new URLClassLoader(libs.toArray(new URL[libs.size()]));
            deploy(mbeans, additionnalLibCl, appModule.getModuleId() + ".add-lib");
        }

        appModule.setMBeans(mbeans);
        logger.debug("registered " + mbeans.size() + " annotated MBeans in " + appModule.getModuleId());
        return appModule;
    }

    private static void deploy(Set<String> mbeans, ClassLoader cl, String id) {
        if (cl == null) {
            return;
        }

        for (Map.Entry<Class<?>, ObjectName> mbean : getMbeanClasses(cl, id).entrySet()) {
            ObjectName objectName = mbean.getValue();
            try {
                server.registerMBean(new DynamicMBeanWrapper(mbean.getKey()), objectName);
                mbeans.add(objectName.getCanonicalName());
                logger.info("MBean " + objectName.getCanonicalName() + " registered.");
            } catch (Exception e) {
                logger.error("the mbean " + mbean.getKey().getName() + " can't be registered", e);
            }
        }
    }

    /**
     * if OPENEJB_MBEAN_FORCE_FINDER system property is set mbeans will be searched from the class loader
     * otherwise the OPENEJB_MBEAN_CLASSES_PROPERTY system property will be used.
     *
     * @param cl the classloader used
     * @param id application id
     * @return the list of mbean classes
     */
    private static Map<Class<?>, ObjectName> getMbeanClasses(ClassLoader cl, String id) {
        ClassLoader classLoader = cl;
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = OpenEJB.class.getClassLoader();
            }
        }

        Map<Class<?>, ObjectName> mbeans = new HashMap<Class<?>, ObjectName>();

        String listProp = SystemInstance.get().getProperty(OPENEJB_MBEAN_CLASSES_PROPERTY);
        if (OPENEJB_MBEAN_FORCE_FINDER.equals(listProp)) { // the classfinder costs too much to be used by default
            logger.debug("loading mbeans using an annotation finder, you should maybe adjust {} system property",
                                            OPENEJB_MBEAN_CLASSES_PROPERTY);
            List<Class<?>> list = Collections.emptyList();
            try {
                ClassFinder mbeanFinder = new ClassFinder(classLoader, true);
                list = mbeanFinder.findAnnotatedClasses(MBean.class);
            } catch (Exception e) {
                logger.error("can't find annotated MBean", e);
            }

            for (Class<?> clazz : list) {
                if (AnnotationUtil.getAnnotation(Internal.class, clazz) == null) {
                    mbeans.put(clazz, getObjectName(clazz, id));
                }
            }
        } else if (listProp != null) {
            for (String name : listProp.replace(" ", "").split(OPENEJB_MBEAN_CLASSES_SPLIT)) {
                name = name.trim();
                try {
                    Class<?> clazz = classLoader.loadClass(name);
                    ObjectName objectName = getObjectName(clazz, id);
                    if (!server.isRegistered(objectName)) {
                        mbeans.put(clazz, objectName);
                    }
                } catch (ClassNotFoundException ignore) { // it is maybe in another classloader
                    logger.debug("mbean not found in classloader " + classLoader.toString()
                        + ", we will try in the next app", ignore);
                } catch (NoClassDefFoundError ignore) {
                    logger.debug("mbean not found in the current app", ignore);
                }
            }
        }
        return mbeans;
    }

    private static ObjectName getObjectName(Class<?> mBean, String id) {
        ObjectNameBuilder builder = new ObjectNameBuilder("openejb.user.mbeans");
        builder.set("group", mBean.getPackage().getName());
        builder.set("application", id);
        builder.set("name", mBean.getSimpleName());
        return builder.build();
    }
}
