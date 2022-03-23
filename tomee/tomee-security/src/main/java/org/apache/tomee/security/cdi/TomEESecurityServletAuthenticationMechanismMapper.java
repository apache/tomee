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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@ApplicationScoped
public class TomEESecurityServletAuthenticationMechanismMapper {
    private final Map<String, HttpAuthenticationMechanism> servletAuthenticationMapper = new ConcurrentHashMap<>();

    @Inject
    private Instance<HttpAuthenticationMechanism> authenticationMechanisms;
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

                if (servletClass.isAnnotationPresent(FormAuthenticationMechanismDefinition.class)) {
                    servletAuthenticationMapper.put(servletName,
                                                    CDI.current().select(FormAuthenticationMechanism.class).get());
                }

                if (servletClass.isAnnotationPresent(CustomFormAuthenticationMechanismDefinition.class)) {
                    servletAuthenticationMapper.put(servletName,
                                                    CDI.current().select(CustomFormAuthenticationMechanism.class).get());
                }

            } catch (final ClassNotFoundException e) {
                // Ignore
            }
        });

        final Set<HttpAuthenticationMechanism> availableBeans = authenticationMechanisms.stream().collect(Collectors.toSet());
        availableBeans.removeAll(servletAuthenticationMapper.values());
        availableBeans.remove(defaultAuthenticationMechanism); // this our wrapper

        if (availableBeans.size() == 1) {
            defaultAuthenticationMechanism.setDelegate(availableBeans.iterator().next());

        } else if (availableBeans.size() > 1) {
            throw new IllegalStateException(
                    "Multiple HttpAuthenticationMechanism found " +
                    availableBeans.stream()
                                  .map(b -> substringBefore(b.getClass().getSimpleName(), "$$"))
                                  .collect(toList()) + " " +
                    "without a @WebServlet association. " +
                    "Deploy a single one for the application, or associate it with a @WebServlet.");

        } else if (servletAuthenticationMapper.size() == 1) {
            // don't think it's covered by the spec but sotera seems to support such a case
            defaultAuthenticationMechanism.setDelegate(servletAuthenticationMapper.values().iterator().next());

        }
    }

    public HttpAuthenticationMechanism getCurrentAuthenticationMechanism(final HttpMessageContext httpMessageContext) {
        final HttpServletRequest request = httpMessageContext.getRequest();

        if (request.getRequestURI().endsWith("j_security_check")) {
            return CDI.current().select(FormAuthenticationMechanism.class).get();
        }

        final String servletName = request.getHttpServletMapping().getServletName();
        return servletAuthenticationMapper.getOrDefault(servletName, defaultAuthenticationMechanism);
    }
}
