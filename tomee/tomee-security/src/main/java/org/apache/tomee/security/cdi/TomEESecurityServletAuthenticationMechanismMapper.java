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
package org.apache.tomee.security.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TomEESecurityServletAuthenticationMechanismMapper {
    private final Map<String, HttpAuthenticationMechanism> servletAuthenticationMapper = new ConcurrentHashMap<>();

    @Inject
    private DefaultAuthenticationMechanism defaultAuthenticationMechanism;

    public void init(@Observes @Initialized(ApplicationScoped.class) final ServletContext context) {
        final Map<String, ? extends ServletRegistration> servletRegistrations = context.getServletRegistrations();
        servletRegistrations.forEach((servletName, servletRegistration) -> {
            try {
                final Class<?> servletClass = Thread.currentThread().getContextClassLoader().loadClass(servletName);
                if (servletClass.isAnnotationPresent(BasicAuthenticationMechanismDefinition.class)) {
                    servletAuthenticationMapper.put(servletName,
                                                    CDI.current().select(BasicAuthenticationMechanism.class).get());
                }
            } catch (final ClassNotFoundException e) {
                // Ignore
            }
        });
    }

    public HttpAuthenticationMechanism getCurrentAuthenticationMechanism(final String servletName) {
        return servletAuthenticationMapper.getOrDefault(servletName, defaultAuthenticationMechanism);
    }
}
