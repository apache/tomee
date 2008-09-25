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
package org.apache.openejb.client;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Options;
import org.apache.openejb.core.ivm.ClientSecurity;
import org.apache.openejb.core.ivm.naming.ContextWrapper;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.security.auth.login.LoginException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Rev$ $Date$
 */
public class LocalInitialContext extends ContextWrapper {

    private static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";
    private static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, LocalInitialContext.class);

    private final LocalInitialContextFactory factory;
    private Properties properties;
    private Object clientIdentity;
    private Object serviceManager;

    private static final String ON_CLOSE = "openejb.embedded.initialcontext.close";
    private Close onClose;

    public static enum Close {
        LOGOUT, RESTART, DESTROY;
    }


    public LocalInitialContext(Hashtable env, LocalInitialContextFactory factory) throws NamingException {
        super(getContainerSystemEjbContext());
        properties = new Properties();
        properties.putAll(env);

        onClose = Options.getEnum(properties, ON_CLOSE, Close.LOGOUT);

        this.factory = factory;

        login();

        startNetworkServices();

        Properties properties = new Properties();

        // set standard and vendor properties
        createEJBContainer(properties);
    }

    public void createEJBContainer(Map<?,?> properties, String... modules){

    }

    public void close() throws NamingException {
        logger.debug("LocalIntialContext.close()");

        switch(onClose){
            case LOGOUT: {
                logout();
            } break;
            case DESTROY: {
                logout();
                destroy();
            } break;

        }
    }

    private void destroy() throws NamingException {
        stopNetworkServices();
        tearDownOpenEJB();
    }

    private void tearDownOpenEJB() throws NamingException {
        if (factory.bootedOpenEJB()){
            logger.info("Destroying container system");
            factory.close();
            context.close();
            OpenEJB.destroy();
        }
    }

    private void login() throws AuthenticationException {
        String user = (String) properties.get(Context.SECURITY_PRINCIPAL);
        String pass = (String) properties.get(Context.SECURITY_CREDENTIALS);
        String realmName = (String) properties.get("openejb.authentication.realmName");

        if (user != null && pass != null){
            try {
                logger.info("Logging in");
                SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
                if (realmName == null) {
                    clientIdentity = securityService.login(user, pass);
                } else {
                    clientIdentity = securityService.login(realmName, user, pass);
                }
                ClientSecurity.setIdentity(clientIdentity);
            } catch (LoginException e) {
                throw (AuthenticationException) new AuthenticationException("User could not be authenticated: "+user).initCause(e);
            }
        }
    }

    private void logout() {
        if (clientIdentity != null) {
            logger.info("Logging out");
            ClientSecurity.setIdentity(null);
        }
    }

    private void startNetworkServices() {
        if (!properties.getProperty(OPENEJB_EMBEDDED_REMOTABLE, "false").equalsIgnoreCase("true")) {
            return;
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            Class serviceManagerClass = classLoader.loadClass("org.apache.openejb.server.ServiceManager");

            Method get = serviceManagerClass.getMethod("get");

            try {
                if (get.invoke(null) != null) return;
            } catch (InvocationTargetException e) {
                return;
            }

            logger.info("Starting network services");

            Method getManager = serviceManagerClass.getMethod("getManager");
            Method init = serviceManagerClass.getMethod("init");
            Method start = serviceManagerClass.getMethod("start", boolean.class);

            try {
                serviceManager = getManager.invoke(null);
            } catch (InvocationTargetException e) {
                String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, unable to instantiate ServiceManager class 'org.apache.openejb.server.ServiceManager'.";
                throw new IllegalStateException(msg, e);
            }

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
        } catch (IllegalAccessException e) {
            String msg = "Option Enabled '" + OPENEJB_EMBEDDED_REMOTABLE + "'.  Error, 'init' and 'start' methods cannot be accessed on class 'org.apache.openejb.server.ServiceManager'.  The VM SecurityManager settings must be adjusted.";
            throw new IllegalStateException(msg, e);
        }
    }


    private void stopNetworkServices() {
        if (serviceManager != null){
            logger.info("Stopping network services");
            try {
                Method stop = serviceManager.getClass().getMethod("stop");
                stop.invoke(serviceManager);
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static Context getContainerSystemEjbContext() throws NamingException {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        Context context = containerSystem.getJNDIContext();
        context = (Context) context.lookup("java:openejb/ejb");
        return context;
    }
}
