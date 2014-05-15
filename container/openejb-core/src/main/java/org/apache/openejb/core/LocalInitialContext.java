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

import org.apache.openejb.ClientInjections;
import org.apache.openejb.Core;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ivm.ClientSecurity;
import org.apache.openejb.core.ivm.naming.ContextWrapper;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.ServiceManagerProxy;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class LocalInitialContext extends ContextWrapper {

    static {
        Core.warmup();
    }

    public static final String OPENEJB_EMBEDDED_REMOTABLE = "openejb.embedded.remotable";
    static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("local"), LocalInitialContext.class);

    private final LocalInitialContextFactory factory;
    private final Properties properties;
    private Object clientIdentity;

    public static final String ON_CLOSE = "openejb.embedded.initialcontext.close";
    private final Close onClose;
    private final Options options;
    private ServiceManagerProxy serviceManager;

    public static enum Close {
        LOGOUT,
        DESTROY
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public LocalInitialContext(final Hashtable env, final LocalInitialContextFactory factory) throws NamingException {
        super(getContainerSystemEjbContext());
        properties = new Properties();
        properties.putAll(env);

        options = new Options(properties);
        onClose = options.get(ON_CLOSE, Close.LOGOUT);

        this.factory = factory;

        login();

        startNetworkServices();
    }

    @Override
    public void close() throws NamingException {
        logger.debug("LocalIntialContext.close()");

        switch (onClose) {
            case LOGOUT: {
                logout();
            }
            break;
            case DESTROY: {
                logout();
                destroy();
            }
            break;

        }
    }

    private void destroy() throws NamingException {
        if (serviceManager != null) {
            serviceManager.stop();
        }
        tearDownOpenEJB();
    }

    private void tearDownOpenEJB() throws NamingException {
        if (factory.bootedOpenEJB()) {
            logger.info("Destroying container system");
            factory.close();
            context.close();
            OpenEJB.destroy();
        }
    }

    private void login() throws AuthenticationException {
        final String user = (String) properties.get(Context.SECURITY_PRINCIPAL);
        final String pass = (String) properties.get(Context.SECURITY_CREDENTIALS);
        final String realmName = (String) properties.get("openejb.authentication.realmName");

        if (user != null && pass != null) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Logging in: " + user);
                }
                final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
                if (realmName == null) {
                    clientIdentity = securityService.login(user, pass);
                } else {
                    clientIdentity = securityService.login(realmName, user, pass);
                }
                ClientSecurity.setIdentity(clientIdentity);
            } catch (final LoginException e) {
                throw (AuthenticationException) new AuthenticationException("User could not be authenticated: " + user).initCause(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void logout() {
        try {
            final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            if (clientIdentity != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Logging out: " + clientIdentity);
                }
                securityService.logout(clientIdentity);
                ClientSecurity.setIdentity(null);
            }
        } catch (final LoginException e) {
            throw new OpenEJBRuntimeException("User could not be logged out.", e);
        }
    }

    private void startNetworkServices() {
        if (!options.get(OPENEJB_EMBEDDED_REMOTABLE, false)) {
            return;
        }

        try {
            serviceManager = new ServiceManagerProxy();
            serviceManager.start();
        } catch (final ServiceManagerProxy.AlreadyStartedException e) {
            logger.debug("Network services already started.  Ignoring option " + OPENEJB_EMBEDDED_REMOTABLE);
        }
    }

    private static Context getContainerSystemEjbContext() throws NamingException {
        Context context = getRoot();
        context = (Context) context.lookup("openejb/local");
        return context;
    }

    private static Context getRoot() {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        return containerSystem.getJNDIContext();
    }

    @Override
    public void bind(final String name, final Object obj) throws NamingException {
        if ("inject".equalsIgnoreCase(name)) {
            inject(obj);
        } else {
            super.bind(name, obj);
        }
    }

    private void inject(final Object obj) throws NamingException {
        try {
            ClientInjections.clientInjector(obj).createInstance();
        } catch (final OpenEJBException e) {
            throw (NamingException) new NamingException("Injection failed").initCause(e);
        }
    }
}
