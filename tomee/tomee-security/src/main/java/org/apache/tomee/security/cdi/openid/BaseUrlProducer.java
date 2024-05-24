package org.apache.tomee.security.cdi.openid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Produces <code>baseURL</code> to be used in EL when configuring @OpenIdAuthenticationMechanismDefinition
 */
@ApplicationScoped
public class BaseUrlProducer {
    @Inject
    private HttpServletRequest request;

    @Produces
    @Dependent
    @Named("baseURL")
    public String produce() {
        return request.getRequestURL().substring(0, request.getRequestURL().length() - request.getRequestURI().length()) + request.getContextPath();
    }
}
