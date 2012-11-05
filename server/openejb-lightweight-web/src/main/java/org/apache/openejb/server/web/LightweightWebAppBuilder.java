/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.web;

import org.apache.openejb.AppContext;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.InitialContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LightweightWebAppBuilder implements WebAppBuilder {
    @Override
    public void deployWebApps(final AppInfo appInfo, final ClassLoader classLoader) throws Exception {
        final CoreContainerSystem cs = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        final AppContext appContext = cs.getAppContext(appInfo.appId);
        if (appContext == null) {
            throw new OpenEJBRuntimeException("Can't find app context for " + appInfo.appId);
        }

        for (WebAppInfo webAppInfo : appInfo.webApps) {
            final Collection<Injection> injections = appContext.getInjections();

            final Map<String, Object> bindings = new HashMap<String, Object>();
            bindings.putAll(appContext.getBindings());
            bindings.putAll(new JndiEncBuilder(webAppInfo.jndiEnc, injections, webAppInfo.moduleId, "Bean", null, webAppInfo.uniqueId, classLoader).buildBindings(JndiEncBuilder.JndiScope.comp));

            final WebContext webContext = new WebContext(appContext);
            webContext.setJndiEnc(new InitialContext());
            webContext.setClassLoader(classLoader);
            webContext.setId(webAppInfo.moduleId);
            webContext.setContextRoot(webAppInfo.contextRoot);
            webContext.setBindings(bindings);
            webContext.getInjections().addAll(injections);
            appContext.getWebContexts().add(webContext);
            cs.addWebContext(webContext);
        }
    }

    @Override
    public void undeployWebApps(final AppInfo appInfo) throws Exception {
        // no-op
    }

    @Override
    public Map<ClassLoader, Map<String, Set<String>>> getJsfClasses() {
        return Collections.emptyMap(); // while we don't manage servlet in embedded mode we don't need it
    }
}
