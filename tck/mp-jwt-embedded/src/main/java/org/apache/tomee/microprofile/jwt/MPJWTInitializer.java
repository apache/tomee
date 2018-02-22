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
package org.apache.tomee.microprofile.jwt;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.inject.Inject;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Responsible for adding the filter into the chain and doing all other initialization
 */
@HandlesTypes(LoginConfig.class)
public class MPJWTInitializer implements ServletContainerInitializer {

    @Inject
    private MPJWTContext context;

    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {

        if (classes == null || classes.isEmpty()) {
            return; // to REST application having @LoginConfig on it
        }

        for (Class<?> clazz : classes) {
            final LoginConfig loginConfig = clazz.getAnnotation(LoginConfig.class);

            if (loginConfig.authMethod() == null && !"MP-JWT".equals(loginConfig.authMethod())) {
                continue;
            }

            if (!Application.class.isAssignableFrom(clazz)) {
                continue; // do we really want Application?
            }

            final ApplicationPath applicationPath = clazz.getAnnotation(ApplicationPath.class);

            final FilterRegistration.Dynamic mpJwtFilter = ctx.addFilter("mp-jwt-filter", MPJWTFilter.class);
            mpJwtFilter.setAsyncSupported(true);

            context.addMapping(
                    new MPJWTContext.MPJWTConfigKey(
                            ctx.getContextPath(),
                            applicationPath == null ? "" : applicationPath.value()),
                    new MPJWTContext.MPJWTConfigValue(
                            loginConfig.authMethod(),
                            loginConfig.realmName())
            );

        }

    }

}