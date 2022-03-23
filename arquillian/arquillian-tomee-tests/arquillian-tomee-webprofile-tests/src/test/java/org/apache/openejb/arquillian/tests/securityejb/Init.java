/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.securityejb;

import jakarta.security.auth.message.config.AuthConfigFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class Init implements ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        final ServletContext context = sce.getServletContext();
        AuthConfigFactory.getFactory().registerConfigProvider(
                new TheAuthConfigProvider(new TheServerAuthModule()),
                "HttpServlet", context.getVirtualServerName() + " " + context.getContextPath(),
                "Test authentication config provider");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        // no-op
    }
}
