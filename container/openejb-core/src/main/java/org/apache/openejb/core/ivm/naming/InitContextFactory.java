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

package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.EnvProps;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.spi.SecurityService;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.security.auth.login.LoginException;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @deprecated Use org.apache.openejb.core.LocalInitialContextFactory
 */
@Deprecated
public class InitContextFactory implements InitialContextFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Context getInitialContext(final Hashtable env) throws javax.naming.NamingException {
        if (!OpenEJB.isInitialized()) {
            initializeOpenEJB(env);
        }

        final String user = (String) env.get(Context.SECURITY_PRINCIPAL);
        final String pass = (String) env.get(Context.SECURITY_CREDENTIALS);
        final String realmName = (String) env.get("openejb.authentication.realmName");

        if (user != null && pass != null) {
            try {
                final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
                final Object identity;
                if (realmName == null) {
                    identity = securityService.login(user, pass);
                } else {
                    identity = securityService.login(realmName, user, pass);
                }
                securityService.associate(identity);
            } catch (final LoginException e) {
                throw (AuthenticationException) new AuthenticationException("User could not be authenticated: " + user).initCause(e);
            }
        }

        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        Context context = containerSystem.getJNDIContext();
        context = (Context) context.lookup("openejb/local");
        return context;

    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    private void initializeOpenEJB(final Hashtable env) throws javax.naming.NamingException {
        try {
            final Properties props = new Properties();

            /* DMB: We should get the defaults from the functionality
            *      Alan is working on.  This is temporary.
            *      When that logic is finished, this block should
            *      probably just be deleted.
            */
            props.put(EnvProps.ASSEMBLER, "org.apache.openejb.assembler.classic.Assembler");
            props.put(EnvProps.CONFIGURATION_FACTORY, "org.apache.openejb.config.ConfigurationFactory");
            props.put(EnvProps.CONFIGURATION, "conf/default.openejb.conf");

            props.putAll(SystemInstance.get().getProperties());

            props.putAll(env);

            OpenEJB.init(props);

        } catch (final Exception e) {
            throw new NamingException("Cannot initailize OpenEJB", e);
        }
    }

}

