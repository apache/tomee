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
package org.apache.tomee.catalina.realm;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.ietf.jgss.GSSContext;

import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LowTypedRealm implements Realm {
    private static final Class<?>[] GET_ROLES_ARGS = new Class<?>[]{Principal.class};
    private static final Class<?>[] AUTHENTICATE_STRING_ARGS = new Class<?>[]{String.class};
    private static final Class<?>[] SIMPLE_AUTHENTICATE_ARGS = new Class<?>[]{String.class, String.class};
    private static final Class<?>[] AUTHENTICATE_ARGS = new Class<?>[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class};
    private static final Class<?>[] GSCONTEXT_AUTHENTICATE = new Class<?>[]{GSSContext.class, Boolean.class};
    private static final Class<?>[] X509CERT_AUTHENTICATE = new Class<?>[]{X509Certificate[].class};
    private static final Class<?>[] FIND_SECURITY_CONSTRAINTS_CONSTRAINT = new Class<?>[]{HttpServletRequest.class, String.class};
    private static final Class<?>[] HAS_RESOURCE_PERMISSION_CONSTRAINT = new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, Object[].class, String.class};
    private static final Class<?>[] HAS_ROLE_CONSTRAINT = new Class<?>[]{Principal.class, String.class};
    private static final Class<?>[] HAS_USER_DATA_PERMISSION_CONSTRAINT = new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class, Object[].class};

    private final Object delegate;

    private final Method simpleAuthenticateMethod;
    private final Method authenticateStringMethod;
    private final Method authenticateMethod;
    private final Method gsMethod;
    private final Method findSecurityConstraintsMethod;
    private final Method x509Method;
    private final Method hasResourcePermissionMethod;
    private final Method hasRoleConstraintMethod;
    private final Method hasUserDataMethod;
    private final Method getRoles;

    private Container container;

    public LowTypedRealm(final Object delegate) {
        this.delegate = delegate;

        final Class<?> clazz = delegate.getClass();

        authenticateStringMethod = findMethod(clazz, AUTHENTICATE_STRING_ARGS);
        simpleAuthenticateMethod = findMethod(clazz, SIMPLE_AUTHENTICATE_ARGS);
        authenticateMethod = findMethod(clazz, AUTHENTICATE_ARGS);
        gsMethod = findMethod(clazz, GSCONTEXT_AUTHENTICATE);
        findSecurityConstraintsMethod = findMethod(clazz, FIND_SECURITY_CONSTRAINTS_CONSTRAINT);
        x509Method = findMethod(clazz, X509CERT_AUTHENTICATE);
        hasResourcePermissionMethod = findMethod(clazz, HAS_RESOURCE_PERMISSION_CONSTRAINT);
        hasRoleConstraintMethod = findMethod(clazz, HAS_ROLE_CONSTRAINT);
        hasUserDataMethod = findMethod(clazz, HAS_USER_DATA_PERMISSION_CONSTRAINT);
        getRoles = findMethod(clazz, GET_ROLES_ARGS);
    }

    private Method findMethod(final Class<?> clazz, final Class<?>[] argTypes) {
        for (final Method mtd : clazz.getMethods()) {
            if (Modifier.isAbstract(mtd.getModifiers())) {
                continue;
            }

            boolean match = true;
            for (int i = 0; i < argTypes.length; i++) {
                final Class<?>[] params = mtd.getParameterTypes();
                if (params.length != argTypes.length) {
                    match = false;
                    break;
                }

                if (!argTypes[i].isAssignableFrom(params[i])) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return mtd;
            }
        }

        return null;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(final Container container) {
        this.container = container;
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public String[] getRoles(final Principal principal) {
        return (String[]) invoke(getRoles, principal);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Principal authenticate(final String s) {
        return (Principal) invoke(authenticateStringMethod, s);
    }

    @Override
    public Principal authenticate(final String username, final String credentials) {
        return (Principal) invoke(simpleAuthenticateMethod, username, credentials);
    }

    @Override
    public Principal authenticate(final String username, final String digest, final String nonce,
                                  final String nc, final String cnonce, final String qop,
                                  final String realm, final String md5a2) {
        return (Principal) invoke(authenticateMethod, username, digest, nonce, nc, cnonce, qop, realm, md5a2);
    }

    @Override
    public Principal authenticate(final GSSContext gssContext, final boolean storeCreds) {
        return (Principal) invoke(gsMethod, gssContext, storeCreds);
    }

    @Override
    public Principal authenticate(final X509Certificate[] certs) {
        return (Principal) invoke(x509Method, certs);
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        return (Boolean) invoke(hasRoleConstraintMethod, principal, role);
    }

    @Override
    public void backgroundProcess() {
        // no-op
    }

    //
    // next patterns can be fully reworked
    //

    @Override
    public SecurityConstraint[] findSecurityConstraints(final Request request, final Context context) {
        final Map<String, ServletSecurityElement> map = (Map<String, ServletSecurityElement>) invoke(findSecurityConstraintsMethod, request.getRequest(), context.getPath());
        final List<SecurityConstraint> constraints = new ArrayList<SecurityConstraint>();
        for (final Map.Entry<String, ServletSecurityElement> entry : map.entrySet()) {
            constraints.addAll(Arrays.asList(SecurityConstraint.createConstraints(entry.getValue(), entry.getKey())));
        }
        return constraints.toArray(new SecurityConstraint[constraints.size()]);
    }

    @Override
    public boolean hasResourcePermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraint,
                                         final Context context) throws IOException {
        return (Boolean) invoke(hasResourcePermissionMethod, request.getRequest(), response.getResponse(), constraint, context.getPath());
    }

    @Override
    public boolean hasUserDataPermission(final Request request, final Response response, final SecurityConstraint[] constraint) throws IOException {
        return (Boolean) invoke(hasUserDataMethod, request.getRequest(), response.getResponse(), constraint);
    }

    @Override
    public CredentialHandler getCredentialHandler() {
        return null;
    }

    @Override
    public void setCredentialHandler(final CredentialHandler credentialHandler) {
        // no-op: ignored, impl should handle it
    }

    private Object invoke(final Method method, final Object... args) {
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(delegate, args);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new TomEERuntimeException(e.getCause());
        } catch (final IllegalAccessException e) {
            throw new TomEERuntimeException(e);
        }
    }

}
