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
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.BasicAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanismHandler;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import java.lang.annotation.Annotation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.tomee.security.http.LoginToContinueMechanism;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@ApplicationScoped
public class TomEESecurityServletAuthenticationMechanismMapper {
    private final Map<String, HttpAuthenticationMechanism> servletAuthenticationMapper = new ConcurrentHashMap<>();

    @Inject
    @Any
    private Instance<HttpAuthenticationMechanism> authenticationMechanisms;
    @Inject
    @Any
    private Instance<HttpAuthenticationMechanismHandler> authenticationMechanismHandlers;
    @Inject
    private DefaultAuthenticationMechanism defaultAuthenticationMechanism;

    public void init(@Observes @Initialized(ApplicationScoped.class) final ServletContext context) {
        final Map<String, ? extends ServletRegistration> servletRegistrations = context.getServletRegistrations();
        servletRegistrations.forEach((servletName, servletRegistration) -> {
            try {
                final Class<?> servletClass = Thread.currentThread().getContextClassLoader().loadClass(servletName);
                final BasicAuthenticationMechanismDefinition[] basicDefinitions =
                        servletClass.getAnnotationsByType(BasicAuthenticationMechanismDefinition.class);
                if (basicDefinitions.length > 0) {
                    servletAuthenticationMapper.put(servletName, selectBasicMechanism(basicDefinitions[0].qualifiers()));
                }

                final FormAuthenticationMechanismDefinition[] formDefinitions =
                        servletClass.getAnnotationsByType(FormAuthenticationMechanismDefinition.class);
                if (formDefinitions.length > 0) {
                    servletAuthenticationMapper.put(servletName, selectFormMechanism(formDefinitions[0].qualifiers()));
                }

                final CustomFormAuthenticationMechanismDefinition[] customFormDefinitions =
                        servletClass.getAnnotationsByType(CustomFormAuthenticationMechanismDefinition.class);
                if (customFormDefinitions.length > 0) {
                    servletAuthenticationMapper.put(servletName, selectCustomFormMechanism(customFormDefinitions[0].qualifiers()));
                }

                final OpenIdAuthenticationMechanismDefinition[] openIdDefinitions =
                        servletClass.getAnnotationsByType(OpenIdAuthenticationMechanismDefinition.class);
                if (openIdDefinitions.length > 0) {
                    servletAuthenticationMapper.put(servletName, selectOpenIdMechanism(openIdDefinitions[0].qualifiers()));
                }

            } catch (final ClassNotFoundException e) {
                // Ignore
            }
        });

        final boolean hasCustomAuthenticationMechanismHandler = authenticationMechanismHandlers.stream()
                .anyMatch(handler -> !(handler instanceof DefaultAuthenticationMechanismHandler));
        if (hasCustomAuthenticationMechanismHandler) {
            return;
        }

        final Set<HttpAuthenticationMechanism> availableBeans = authenticationMechanisms.stream().collect(Collectors.toSet());
        availableBeans.removeAll(servletAuthenticationMapper.values());
        availableBeans.remove(defaultAuthenticationMechanism); // this is our wrapper

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
            // don't think it's covered by the spec but soteria seems to support such a case
            defaultAuthenticationMechanism.setDelegate(servletAuthenticationMapper.values().iterator().next());

        }
    }

    public HttpAuthenticationMechanism getCurrentAuthenticationMechanism(final HttpMessageContext httpMessageContext) {
        final HttpServletRequest request = httpMessageContext.getRequest();

        if (request.getRequestURI().endsWith("j_security_check")) {
            final Set<HttpAuthenticationMechanism> loginToContinueMechanisms = authenticationMechanisms.stream()
                    .filter(LoginToContinueMechanism.class::isInstance)
                    .collect(Collectors.toSet());
            loginToContinueMechanisms.remove(defaultAuthenticationMechanism);
            if (loginToContinueMechanisms.size() == 1) {
                return loginToContinueMechanisms.iterator().next();
            }
        }

        final String servletName = request.getHttpServletMapping().getServletName();
        return servletAuthenticationMapper.getOrDefault(servletName, defaultAuthenticationMechanism);
    }

    private HttpAuthenticationMechanism selectBasicMechanism(final Class<?>[] qualifierTypes) {
        final Annotation[] qualifiers = QualifierInstances.literals(qualifierTypes);
        return CDI.current().select(BasicAuthenticationMechanism.class, qualifiers).get();
    }

    private HttpAuthenticationMechanism selectFormMechanism(final Class<?>[] qualifierTypes) {
        final Annotation[] qualifiers = QualifierInstances.literals(qualifierTypes);
        return CDI.current().select(FormAuthenticationMechanism.class, qualifiers).get();
    }

    private HttpAuthenticationMechanism selectCustomFormMechanism(final Class<?>[] qualifierTypes) {
        final Annotation[] qualifiers = QualifierInstances.literals(qualifierTypes);
        return CDI.current().select(CustomFormAuthenticationMechanism.class, qualifiers).get();
    }

    private HttpAuthenticationMechanism selectOpenIdMechanism(final Class<?>[] qualifierTypes) {
        final Annotation[] qualifiers = QualifierInstances.literals(qualifierTypes);
        return CDI.current().select(OpenIdAuthenticationMechanism.class, qualifiers).get();
    }
}
