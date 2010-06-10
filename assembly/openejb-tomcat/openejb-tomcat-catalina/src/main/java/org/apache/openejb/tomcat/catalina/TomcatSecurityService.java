/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.spi.CallerPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

public class TomcatSecurityService extends AbstractSecurityService {
    static protected final ThreadLocal<LinkedList<Subject>> runAsStack = new ThreadLocal<LinkedList<Subject>>() {
        protected LinkedList<Subject> initialValue() {
            return new LinkedList<Subject>();
        }
    };

    private Realm defaultRealm;

    public TomcatSecurityService() {
        Server server = ServerFactory.getServer();
        for (Service service : server.findServices()) {
            if (service.getContainer() instanceof Engine) {
                Engine engine = (Engine) service.getContainer();
                if (engine.getRealm() != null) {
                    defaultRealm = engine.getRealm();
                    break;
                }
            }
        }
    }

    public UUID login(String realmName, String username, String password) throws LoginException {
        if (defaultRealm == null) {
            throw new LoginException("No Tomcat realm available");
        }

        Principal principal = defaultRealm.authenticate(username, password);
        Subject subject = createSubject(defaultRealm, principal);
        UUID token = registerSubject(subject);
        return token;
    }

    private Subject createSubject(Realm realm, Principal principal) {
        TomcatUser tomcatUser = new TomcatUser(realm, principal);

        HashSet<Principal> principals = new HashSet<Principal>();
        principals.add(tomcatUser);

        Subject subject = new Subject(true, principals, new HashSet(), new HashSet());
        return subject;
    }

    public Set<String> getLogicalRoles(Principal[] principals, Set<String> logicalRoles) {
        LinkedHashSet<String> roles = new LinkedHashSet<String>(logicalRoles.size());
        for (String logicalRole : logicalRoles) {
            for (Principal principal : principals) {
                if (principal instanceof TomcatUser) {
                    TomcatUser user = (TomcatUser) principal;
                    if (user.getRealm().hasRole(user.getTomcatPrincipal(), logicalRole)) {
                        roles.add(logicalRole);
                        break;
                    }
                } else if (principal != null) {
                    String name = principal.getName();
                    if (logicalRole.equals(name)) {
                        roles.add(logicalRole);
                    }
                }
            }
        }
        return roles;
    }

    public Object enterWebApp(Realm realm, Principal principal, String runAs) {
        Identity newIdentity = null;
        if (principal != null) {
            Subject newSubject = createSubject(realm, principal);
            newIdentity = new Identity(newSubject, null);
        }

        Identity oldIdentity = clientIdentity.get();
        WebAppState webAppState = new WebAppState(oldIdentity, runAs != null);
        clientIdentity.set(newIdentity);

        if (runAs != null) {
            Subject runAsSubject = createRunAsSubject(runAs);
            runAsStack.get().addFirst(runAsSubject);
        }

        return webAppState;
    }

    public void exitWebApp(Object state) {
        if (state instanceof WebAppState) {
            WebAppState webAppState = (WebAppState) state;
            clientIdentity.set(webAppState.oldIdentity);
            if (webAppState.hadRunAs) {
                runAsStack.get().removeFirst();
            }
        }
    }

    protected Subject getRunAsSubject(CoreDeploymentInfo callingDeploymentInfo) {
        Subject runAsSubject = super.getRunAsSubject(callingDeploymentInfo);
        if (runAsSubject != null) return runAsSubject;

        LinkedList<Subject> stack = runAsStack.get();
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getFirst();
    }


    protected Subject createRunAsSubject(String role) {
        if (role == null) return null;

        RunAsRole runAsRole = new RunAsRole(role);

        HashSet<Principal> principals = new HashSet<Principal>();
        principals.add(runAsRole);

        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    @CallerPrincipal
    protected static class TomcatUser implements Principal {
        private final Realm realm;
        private final Principal tomcatPrincipal;


        public TomcatUser(Realm realm, Principal tomcatPrincipal) {
            if (realm == null) throw new NullPointerException("realm is null");
            if (tomcatPrincipal == null) throw new NullPointerException("tomcatPrincipal is null");
            this.realm = realm;
            this.tomcatPrincipal = tomcatPrincipal;
        }

        public Realm getRealm() {
            return realm;
        }

        public Principal getTomcatPrincipal() {
            return tomcatPrincipal;
        }

        public String getName() {
            return tomcatPrincipal.getName();
        }

        public String toString() {
            return "[TomcatUser: " + tomcatPrincipal + "]";
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TomcatUser that = (TomcatUser) o;

            return realm.equals(that.realm) && tomcatPrincipal.equals(that.tomcatPrincipal);
        }

        public int hashCode() {
            int result;
            result = realm.hashCode();
            result = 31 * result + tomcatPrincipal.hashCode();
            return result;
        }
    }

    protected static class RunAsRole implements Principal {
        private final String name;

        public RunAsRole(String name) {
            if (name == null) throw new NullPointerException("name is null");
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return "[RunAsRole: " + name + "]";
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RunAsRole runAsRole = (RunAsRole) o;

            return name.equals(runAsRole.name);
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class WebAppState {
        private final Identity oldIdentity;
        private final boolean hadRunAs;


        public WebAppState(Identity oldIdentity, boolean hadRunAs) {
            this.oldIdentity = oldIdentity;
            this.hadRunAs = hadRunAs;
        }
    }
}
