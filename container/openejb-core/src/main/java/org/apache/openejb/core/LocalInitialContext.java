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
package org.apache.openejb.core;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.ServiceManagerProxy;
import org.apache.openejb.core.ivm.ClientSecurity;
import org.apache.openejb.core.ivm.naming.ContextWrapper;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.security.auth.login.LoginException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class LocalInitialContext extends ContextWrapper {

    public static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";
    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, LocalInitialContext.class);

    private final LocalInitialContextFactory factory;
    private Properties properties;
    private Object clientIdentity;

    public static final String ON_CLOSE = "openejb.embedded.initialcontext.close";
    private Close onClose;
    private Options options;
    private ServiceManagerProxy serviceManager;

    public static enum Close {
        LOGOUT, DESTROY;
    }

    public LocalInitialContext(Hashtable env, LocalInitialContextFactory factory) throws NamingException {
        super(getContainerSystemEjbContext());
        properties = new Properties();
        properties.putAll(env);

        options = new Options(properties);
        onClose = options.get(ON_CLOSE, Close.LOGOUT);

        this.factory = factory;

        login();

        startNetworkServices();
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
        if (serviceManager != null) {
            serviceManager.stop();
        }
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
        try {
            SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            if (clientIdentity != null) {
                logger.info("Logging out");
                securityService.logout(clientIdentity);
                ClientSecurity.setIdentity(null);
            }
        } catch (LoginException e) {
            throw new RuntimeException("User could not be logged out.", e);
        }
    }

    private void startNetworkServices() {
        if (!options.get(OPENEJB_EMBEDDED_REMOTABLE, false)) {
            return;
        }

        try {
            serviceManager = new ServiceManagerProxy();
            serviceManager.start();
        } catch (ServiceManagerProxy.AlreadyStartedException e) {
            logger.debug("Network services already started.  Ignoring option " + OPENEJB_EMBEDDED_REMOTABLE);            
        }
    }


    private static Context getContainerSystemEjbContext() throws NamingException {
        Context context = getRoot();
        context = (Context) context.lookup("openejb/local");
        return context;
    }

    private static Context getRoot() {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        Context context = containerSystem.getJNDIContext();
        return context;
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        if ("inject".equalsIgnoreCase(name)) {
            inject(obj);
        } else {
            super.bind(name, obj);
        }
    }

    private void inject(Object obj) throws NamingException {
        if (obj == null) throw new NullPointerException("Object supplied to 'inject' operation is null");
        Class clazz = obj.getClass();

        Context clients = (Context) getRoot().lookup("openejb/client/");

        Context context = null;
        List<Injection> injections = null;

        while (clazz != null && !clazz.equals(Object.class)) {
            try {
                String moduleId = (String) clients.lookup(clazz.getName());
                context = (Context) clients.lookup(moduleId);
                injections = (List<Injection>) context.lookup("info/injections");
                break;
            } catch (NamingException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (injections == null) throw new NamingException("Unable to find injection meta-data for "+obj.getClass().getName()+".  Ensure that class was annotated with @"+ LocalClient.class.getName()+" and was successfully discovered and deployed.  See http://openejb.apache.org/3.0/local-client-injection.html");

        try {
            InjectionProcessor processor = new InjectionProcessor(obj, injections, context);

            processor.createInstance();
        } catch (OpenEJBException e) {
            throw (NamingException) new NamingException("Injection failed").initCause(e);
        }
    }
}
