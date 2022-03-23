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
package org.apache.tomee.catalina;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.openejb.BeanContext;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.CallerPrincipal;
import org.apache.tomee.loader.TomcatHelper;

import javax.security.auth.Subject;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;
import jakarta.security.jacc.PolicyContextException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

public class TomcatSecurityService extends AbstractSecurityService {
    private static final boolean ONLY_DEFAULT_REALM = "true".equals(SystemInstance.get().getProperty("tomee.realm.only-default", "false"));
    protected static final ThreadLocal<LinkedList<Subject>> RUN_AS_STACK = new ThreadLocal<LinkedList<Subject>>() {
        protected LinkedList<Subject> initialValue() {
            return new LinkedList<>();
        }
    };

    private Realm defaultRealm;

    public TomcatSecurityService() {
        final Server server = TomcatHelper.getServer();
        for (final Service service : server.findServices()) {
            if (service.getContainer() instanceof Engine) {
                final Engine engine = (Engine) service.getContainer();
                if (engine.getRealm() != null) {
                    defaultRealm = engine.getRealm();
                    break;
                }
            }
        }
    }

    @Override
    public boolean isCallerInRole(final String role) {
        final Principal principal = getCallerPrincipal();
        if (TomcatUser.class.isInstance(principal)) {
            if ("**".equals(role)) {
                return true; // ie logged in through tomcat
            }

            final TomcatUser tomcatUser = (TomcatUser) principal;
            final GenericPrincipal genericPrincipal = (GenericPrincipal) tomcatUser.getTomcatPrincipal();
            final String[] roles = genericPrincipal.getRoles();
            if (roles != null) {
                for (final String userRole : roles) {
                    if (userRole.equals(role)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return super.isCallerInRole(role);
    }

    public UUID login(final String realmName, final String username, final String password) throws LoginException {
        final Realm realm = findRealm(realmName);
        if (realm == null) {
            throw new LoginException("No Tomcat realm available");
        }

        final Principal principal = realm.authenticate(username, password);
        if (principal == null) {
            throw new CredentialNotFoundException(username);
        }

        final Subject subject = createSubject(realm, principal);
        return registerSubject(subject);
    }

    private Realm findRealm(final String realmName) {
        if (ONLY_DEFAULT_REALM || realmName == null || realmName.isEmpty()) {
            return defaultRealm;
        }

        final TomcatWebAppBuilder webAppBuilder = SystemInstance.get().getComponent(TomcatWebAppBuilder.class);
        if (webAppBuilder != null) {
            final Realm r = webAppBuilder.getRealms().get('/' + realmName);
            if (r != null) {
                return r;
            }
        }

        return defaultRealm;
    }

    private Subject createSubject(final Realm realm, final Principal principal) {
        final Subject subject = new Subject();

        // 1. Add the principal as is
        subject.getPrincipals().add(principal);
        subject.getPrincipals().add(new TomcatUser(realm, principal));

        Principal p = principal;
        if (principal instanceof TomcatUser) { // should never happen
            p = ((TomcatUser) principal).getTomcatPrincipal();
            subject.getPrincipals().add(p);
        }

        if (p instanceof GenericPrincipal) {
            final GenericPrincipal genericPrincipal = (GenericPrincipal) p;
            subject.getPrincipals().add(genericPrincipal.getUserPrincipal());

            // todo should we create credentials with the roles? groups?
            subject.getPrivateCredentials().add(p);
        }

        return subject;
    }

    @Override
    public Set<String> getLogicalRoles(final Principal[] principals, final Set<String> logicalRoles) {
        final Set<String> roles = new LinkedHashSet<>(logicalRoles.size());
        for (final String logicalRole : logicalRoles) {
            for (final Principal principal : principals) {
                if (principal instanceof TomcatUser) {
                    final TomcatUser user = (TomcatUser) principal;
                    if (TomcatHelper.hasRole(user.getRealm(), user.getTomcatPrincipal(), logicalRole)) {
                        roles.add(logicalRole);
                        break;
                    }
                } else if (principal != null) {
                    final String name = principal.getName();
                    if (logicalRole.equals(name)) {
                        roles.add(logicalRole);
                    }
                }
            }
        }
        return roles;
    }

    @Override
    public Principal getCallerPrincipal() {
        final Identity currentIdentity = clientIdentity.get();
        if (currentIdentity != null) {
            final Set<Principal> principals = currentIdentity.getSubject().getPrincipals();
            for (final Principal principal : principals) {
                if (principal.getClass().isAnnotationPresent(CallerPrincipal.class)) {
                    return principal;
                }
            }
            if (!principals.isEmpty()) {
                return principals.iterator().next();
            }
        }
        return super.getCallerPrincipal();
    }

    public Object enterWebApp(final Realm realm, final Principal principal, final String runAs) {
        Identity newIdentity = null;
        if (principal != null) {
            final Subject newSubject = createSubject(realm, principal);
            newIdentity = new Identity(newSubject, null);
        }

        final Identity oldIdentity = clientIdentity.get();
        final WebAppState webAppState = new WebAppState(oldIdentity, runAs != null);
        clientIdentity.set(newIdentity);

        if (runAs != null) {
            final Subject runAsSubject = createRunAsSubject(runAs);
            RUN_AS_STACK.get().addFirst(runAsSubject);
        }

        return webAppState;
    }

    @Override
    public void onLogout(final HttpServletRequest request) {
        final Request state = OpenEJBSecurityListener.requests.get();
        final Object webappState = state == null ? null : state.getNote(TomEERealm.SECURITY_NOTE);
        if (webappState != null) {
            exitWebApp(webappState);
        } else {
            super.onLogout(request);
        }
    }

    public void exitWebApp(final Object state) {
        if (state instanceof WebAppState) {
            final WebAppState webAppState = (WebAppState) state;
            if (webAppState.oldIdentity == null) {
                clientIdentity.remove();
            } else {
                clientIdentity.set(webAppState.oldIdentity);
            }

            if (webAppState.hadRunAs) {
                RUN_AS_STACK.get().removeFirst();
            }
        }
    }

    @Override
    public Subject getRunAsSubject(final BeanContext callingBeanContext) {
        final Subject runAsSubject = super.getRunAsSubject(callingBeanContext);
        if (runAsSubject != null) {
            return runAsSubject;
        }

        final LinkedList<Subject> stack = RUN_AS_STACK.get();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getFirst();
    }


    protected Subject createRunAsSubject(final String role) {
        if (role == null) {
            return null;
        }

        final Set<Principal> principals = new HashSet<>();
        principals.add(new RunAsRole(role));
        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    @CallerPrincipal
    public static class TomcatUser implements Principal {
        private final Realm realm;
        private final Principal tomcatPrincipal;


        public TomcatUser(final Realm realm, final Principal tomcatPrincipal) {
            if (realm == null) {
                throw new NullPointerException("realm is null");
            }
            if (tomcatPrincipal == null) {
                throw new NullPointerException("tomcatPrincipal is null");
            }
            this.realm = realm;
            this.tomcatPrincipal = tomcatPrincipal;
        }

        public Realm getRealm() {
            return realm;
        }

        public Principal getTomcatPrincipal() {
            return tomcatPrincipal;
        }

        @Override
        public String getName() {
            return tomcatPrincipal.getName();
        }

        @Override
        public String toString() {
            return "[TomcatUser: " + tomcatPrincipal + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TomcatUser that = (TomcatUser) o;

            return realm.equals(that.realm) && tomcatPrincipal.equals(that.tomcatPrincipal);
        }

        @Override
        public int hashCode() {
            int result;
            result = realm.hashCode();
            result = 31 * result + tomcatPrincipal.hashCode();
            return result;
        }
    }

    protected static class RunAsRole implements Principal {
        private final String name;

        public RunAsRole(final String name) {
            if (name == null) {
                throw new NullPointerException("name is null");
            }
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "[RunAsRole: " + name + "]";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final RunAsRole runAsRole = (RunAsRole) o;

            return name.equals(runAsRole.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class WebAppState implements Serializable {
        private final Identity oldIdentity;
        private final boolean hadRunAs;


        public WebAppState(final Identity oldIdentity, final boolean hadRunAs) {
            this.oldIdentity = oldIdentity;
            this.hadRunAs = hadRunAs;
        }
    }

    @Override
    protected SecurityContext getDefaultSecurityContext() {
        final Request request = OpenEJBSecurityListener.requests.get();
        if (request != null) {
            final Object subjectCallable = request.getAttribute("javax.security.auth.subject.callable");
            if (subjectCallable != null && Callable.class.isInstance(subjectCallable)) {
                // maybe we should check, but it's so specific ...
                try {
                    final Subject subject = (Subject) Callable.class.cast(subjectCallable).call();
                    return new SecurityContext(subject);

                } catch (final Exception e) {
                    // ignore and let it go to the default implementation
                }
            }
        }

        return super.getDefaultSecurityContext();
    }

    @Override
    public boolean supports(final String key) throws PolicyContextException {
        return KEYS.contains(key);
    }

    @Override
    public String[] getKeys() throws PolicyContextException {
        return KEYS.toArray(new String[0]);
    }

    @Override
    public Object getContext(final String key, final Object data) throws PolicyContextException {
        switch (key) {
            case KEY_REQUEST:
                return OpenEJBSecurityListener.requests.get();
            case KEY_SUBJECT:
                // quite obvious as internally we keep track of it
                // but we could also grab the request and the principals and build a new Subject with the principals
                return getSubject();
            default:
                throw new PolicyContextException("Handler does not support key: " + key);
        }
    }
}
