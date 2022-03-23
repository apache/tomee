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

package org.apache.openejb.core.security.jaas;

import org.apache.openejb.AppContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.OWBInjector;

import jakarta.enterprise.inject.spi.Bean;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;
import java.util.Set;

/**
 * Usage:
 *
 * CDI {
 * org.apache.openejb.core.security.jaas.CDILoginModule required
 * delegate="org.apache.openejb.core.security.CDILoginModuleTest$Delegate"
 * loginModuleAsCdiBean=false
 * cdiName="xxx";
 * };
 *
 * Note: you can use instead of delegate &lt;appid&gt; to define a delegate by app.
 * Note 2: loginModuleAsCdiBean=true is recommanded only for @Dependent beans
 * Note 3: delegate and cdiName can be used alone
 */
public class CDILoginModule implements LoginModule {
    private CreationalContextImpl<?> cc;
    private LoginModule loginModule;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler,
                           final Map<String, ?> sharedState, final Map<String, ?> options) {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        final BeanManagerImpl bm = webBeansContext.getBeanManagerImpl();
        if (!bm.isInUse()) {
            throw new OpenEJBRuntimeException("CDI not activated");
        }

        String delegate = String.valueOf(options.get("delegate"));
        if ("null".equals(delegate)) {
            final String app = findAppName(webBeansContext);
            delegate = String.valueOf(options.get(app));
            if ("null".equals(delegate)) {
                throw new OpenEJBRuntimeException("Please specify a delegate class");
            }
        }

        final Class<?> clazz;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(delegate);
        } catch (final ClassNotFoundException e) {
            throw new OpenEJBRuntimeException(e.getMessage(), e);
        }

        cc = bm.createCreationalContext(null);
        final String cdiName = String.valueOf(options.get("cdiName"));
        if ("true".equals(String.valueOf(options.get("loginModuleAsCdiBean")))) {
            final Set<Bean<?>> beans;
            if ("null".equals(cdiName)) {
                beans = bm.getBeans(clazz);
            } else {
                beans = bm.getBeans(cdiName);
            }
            loginModule = LoginModule.class.cast(bm.getReference(bm.resolve(beans), clazz, cc));
        } else {
            try {
                loginModule = LoginModule.class.cast(clazz.newInstance());
                OWBInjector.inject(bm, loginModule, cc);
            } catch (final Exception e) {
                throw new OpenEJBRuntimeException("Can't inject into delegate class " + loginModule, e);
            }
        }

        loginModule.initialize(subject, callbackHandler, sharedState, options);
    }

    private static String findAppName(final WebBeansContext webBeansContext) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (final AppContext appContext : containerSystem.getAppContexts()) {
            if (appContext.getWebBeansContext() == webBeansContext) {
                return appContext.getId();
            }
            for (final WebContext web : appContext.getWebContexts()) {
                if (web.getWebbeansContext() == webBeansContext) { // ear
                    return web.getId();
                }
            }
        }
        return "defaultDelegate";
    }

    @Override
    public boolean login() throws LoginException {
        return loginModule != null && loginModule.login();
    }

    @Override
    public boolean commit() throws LoginException {
        return loginModule == null || loginModule.commit(); // cleanUp is called on logout
    }

    @Override
    public boolean abort() throws LoginException {
        try {
            return loginModule == null || loginModule.abort();
        } finally {
            cleanUp();
        }
    }

    @Override
    public boolean logout() throws LoginException {
        try {
            return loginModule == null || loginModule.logout();
        } finally {
            cleanUp();
        }
    }

    private void cleanUp() {
        if (cc != null) {
            cc.release();
            cc = null;
        }
    }
}
