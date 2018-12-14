package org.apache.tomee.microprofile.jwt.jaxrs;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class MPJWTSecurityAnnotationsInterceptor implements ContainerRequestFilter {

    private final javax.ws.rs.container.ResourceInfo resourceInfo;
    private final ConcurrentMap<Method, Set<String>> rolesAllowed;
    private final Set<Method> denyAll;
    private final Set<Method> permitAll;

    public MPJWTSecurityAnnotationsInterceptor(final javax.ws.rs.container.ResourceInfo resourceInfo,
                                               final ConcurrentMap<Method, Set<String>> rolesAllowed,
                                               final Set<Method> denyAll,
                                               final Set<Method> permitAll) {
        this.resourceInfo = resourceInfo;
        this.rolesAllowed = rolesAllowed;
        this.denyAll = denyAll;
        this.permitAll = permitAll;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (permitAll.contains(resourceInfo.getResourceMethod())) {
            return;
        }

        if (denyAll.contains(resourceInfo.getResourceMethod())) {
            forbidden(requestContext);
            return;
        }

        final Set<String> roles = rolesAllowed.get(resourceInfo.getResourceMethod());

        if (roles != null && !roles.isEmpty()) {
            final SecurityContext securityContext = requestContext.getSecurityContext();
            boolean hasAtLeasOneValidRole = false;
            for (String role : roles) {
                if (securityContext.isUserInRole(role)) {
                    hasAtLeasOneValidRole = true;
                    break;
                }
            }
            if (!hasAtLeasOneValidRole) {
                forbidden(requestContext);
            }
        }

    }

    private void forbidden(final ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(HttpURLConnection.HTTP_FORBIDDEN).build());
    }
}