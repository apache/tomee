/**
 *
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
package org.apache.openejb.client;

import org.apache.openejb.loader.OpenEJBInstance;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    private static OpenEJBInstance openejb;
    private static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";

    private boolean bootedOpenEJB;
    private Object serviceManager;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        init(env);
        return getLocalInitialContext(env);
    }

    private void init(Hashtable env) throws javax.naming.NamingException {
        if (openejb != null) {
            return;
        }
        try {
            Properties properties = new Properties();
            properties.putAll(env);
            init(properties);
        } catch (Exception e) {
            throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
        }
    }

    boolean bootedOpenEJB() {
        return bootedOpenEJB;
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        bootedOpenEJB = true;
        SystemInstance.init(properties);
        SystemInstance.get().setProperty("openejb.embedded", "true");
        openejb.init(properties);
        if (properties.getProperty(OPENEJB_EMBEDDED_REMOTABLE, "false").equalsIgnoreCase("true")) {
            bootServerServices();
        }
    }

    public void close(){
        openejb = null;
        if (serviceManager != null){
            try {
                Method stop = serviceManager.getClass().getMethod("stop");
                stop.invoke(serviceManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void bootServerServices() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Class serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.ServiceManager");
            Method init = serviceManagerClass.getMethod("init");
            Method start = serviceManagerClass.getMethod("start", boolean.class);

            serviceManager = serviceManagerClass.newInstance();
            try {
                init.invoke(serviceManager);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, unable to initialize ServiceManager.  Cause: " + cause.getClass().getName() + ": " + cause.getMessage();
                throw new IllegalStateException(msg, cause);
            }
            try {
                start.invoke(serviceManager, false);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, unable to start ServiceManager.  Cause: " + cause.getClass().getName() + ": " + cause.getMessage();
                throw new IllegalStateException(msg, cause);
            }
        } catch (ClassNotFoundException e) {
            String msg = "Enabling option '" + OPENEJB_EMBEDDED_REMOTABLE + "' requires class 'org.apache.openejb.server.ServiceManager' to be available.  Make sure you have the openejb-server-*.jar in your classpath and at least one protocol implementation such as openejb-ejbd-*.jar.";
            throw new IllegalStateException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, 'init' and 'start' methods not found on as expected on class 'org.apache.openejb.server.ServiceManager'.  This should never happen.";
            throw new IllegalStateException(msg, e);
        } catch (InstantiationException e) {
            String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, unable to instantiate ServiceManager class 'org.apache.openejb.server.ServiceManager'.";
            throw new IllegalStateException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, 'init' and 'start' methods cannot be accessed on class 'org.apache.openejb.server.ServiceManager'.  The VM SecurityManager settings must be adjusted.";
            throw new IllegalStateException(msg, e);
        }
    }

    private Context getLocalInitialContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            InitialContextFactory factory = null;
            ClassLoader cl = SystemInstance.get().getClassLoader();
            Class ivmFactoryClass = Class.forName("org.apache.openejb.core.ivm.naming.InitContextFactory", true, cl);

            factory = (InitialContextFactory) ivmFactoryClass.newInstance();
            context = factory.getInitialContext(env);

            Class clientWrapper = Class.forName("org.apache.openejb.client.LocalInitialContext", true, cl);
            Constructor constructor = clientWrapper.getConstructor(Context.class, this.getClass());
            context = (Context) constructor.newInstance(context, this);
        } catch (Exception e) {
            throw (NamingException) new javax.naming.NamingException("Cannot instantiate a LocalInitialContext. Exception: "
                    + e.getClass().getName() + " " + e.getMessage()).initCause(e);
        }

        return context;
    }

}