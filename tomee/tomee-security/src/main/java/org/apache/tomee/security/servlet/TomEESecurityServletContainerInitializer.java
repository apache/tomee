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
package org.apache.tomee.security.servlet;

import org.apache.tomee.security.cdi.TomEESecurityExtension;
import org.apache.tomee.security.provider.TomEESecurityAuthConfigProvider;

import javax.enterprise.inject.spi.CDI;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

public class TomEESecurityServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> c, final ServletContext ctx) throws ServletException {
        final TomEESecurityExtension securityExtension =
                CDI.current().getBeanManager().getExtension(TomEESecurityExtension.class);

        if (securityExtension.hasAuthenticationMechanisms()) {
            AuthConfigFactory.getFactory().registerConfigProvider(
                    new TomEESecurityAuthConfigProvider(),
                    "HttpServlet",                                              // from AuthenticatorBase.java:1245
                    ctx.getVirtualServerName() + " " + ctx.getContextPath(),    // from AuthenticatorBase.java:1178
                    "TomEE Security JSR-375");
        }
    }
}
