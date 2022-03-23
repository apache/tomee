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

package org.apache.openejb.core.security;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.api.resource.DestroyableResource;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jacc.BasicJaccProvider;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.CallerPrincipal;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import jakarta.security.jacc.EJBMethodPermission;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyContextHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

/**
 * This security service chooses a UUID as its token as this can be serialized
 * to clients, is mostly secure, and can be deserialized in a client vm without
 * addition openejb-core classes.
 */
public abstract class AbstractSecurityService implements DestroyableResource, SecurityService<UUID>, ThreadContextListener,
                                                         BasicPolicyConfiguration.RoleResolver, PolicyContextHandler {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SECURITY, "org.apache.openejb.util.resources");

    protected static final String KEY_SUBJECT = "javax.security.auth.Subject.container";
    protected static final String KEY_REQUEST = "jakarta.servlet.http.HttpServletRequest";
    protected static final Set<String> KEYS = new HashSet<>(asList(KEY_REQUEST, KEY_SUBJECT));

    private static final Map<Object, Identity> identities = new ConcurrentHashMap<Object, Identity>();
    protected static final ThreadLocal<Identity> clientIdentity = new ThreadLocal<Identity>();
    protected String defaultUser = "guest";
    private String realmName = "PropertiesLogin";
    protected Subject defaultSubject;
    protected SecurityContext defaultContext;

    public AbstractSecurityService() {
        this(autoJaccProvider());
    }

    public AbstractSecurityService(final String jaccProvider) {
        JavaSecurityManagers.setSystemProperty(JaccProvider.class.getName(), jaccProvider);

        installJacc();

        ThreadContext.addThreadContextListener(this);

        // set the default subject and the default context
        updateSecurityContext();

        // we can now add the role resolver for Jacc to convert into strings
        SystemInstance.get().setComponent(BasicPolicyConfiguration.RoleResolver.class, this);

        // and finally we can register ourself as a PolicyContextHandler
        // we can register policy handlers and the role mapper
        try {
            for (String key : getKeys()) {
                PolicyContext.registerHandler(key, this, true);
            }
        } catch (final PolicyContextException e) {
            // best would probably to fail start if something wrong happens
            LOGGER.warning("Can't register PolicyContextHandler", e);
        }
    }

    @Override
    public void destroyResource() {
        ThreadContext.removeThreadContextListener(this);
    }

    public void onLogout(final HttpServletRequest request) {
        clientIdentity.remove();
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(final String realmName) {
        this.realmName = realmName;
    }

    /**
     * @return the defaultUser
     */
    public String getDefaultUser() {
        return defaultUser;
    }

    /**
     * @param defaultUser the defaultUser to set
     */
    public void setDefaultUser(final String defaultUser) {
        this.defaultUser = defaultUser;

        // set the default subject and the default context for the new default user
        updateSecurityContext();
    }

    // update the current subject and security context
    private void updateSecurityContext() {
        defaultSubject = createSubject(defaultUser, defaultUser);
        defaultContext = new SecurityContext(defaultSubject);
    }

    @Override
    public void init(final Properties props) throws Exception {
    }

    @Override
    public UUID login(final String username, final String password) throws LoginException {
        return login(realmName, username, password);
    }

    @Override
    public Set<String> getLogicalRoles(final Principal[] principals, final Set<String> logicalRoles) {
        final LinkedHashSet<String> roles = new LinkedHashSet<>(principals.length);
        for (final Principal principal : principals) {
            final String name = principal.getName();
            if (logicalRoles.contains(name)) {
                roles.add(name);
            }
        }
        return roles;
    }

    @Override
    public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
        final String moduleID = newContext.getBeanContext().getModuleID();
        JavaSecurityManagers.setContextID(moduleID);

        final SecurityContext defaultSecurityContext = getDefaultSecurityContext();

        final ProvidedSecurityContext providedSecurityContext = newContext.get(ProvidedSecurityContext.class);
        SecurityContext securityContext = oldContext != null ? oldContext.get(SecurityContext.class) :
                (providedSecurityContext != null ? providedSecurityContext.context : null);
        if (providedSecurityContext == null && (securityContext == null || securityContext == defaultSecurityContext)) {
            final Identity identity = clientIdentity.get();
            if (identity != null) {
                securityContext = new SecurityContext(identity.subject);
            } else {
                securityContext = defaultSecurityContext;
            }
        }

        newContext.set(SecurityContext.class, securityContext);
    }

    public UUID overrideWithRunAsContext(final ThreadContext ctx, final BeanContext newContext, final BeanContext oldContext) {
        Subject runAsSubject = getRunAsSubject(newContext);
        if (oldContext != null && runAsSubject == null) {
            runAsSubject = getRunAsSubject(oldContext);
        }
        ctx.set(SecurityContext.class, new SecurityContext(runAsSubject));
        return disassociate();
    }

    public Subject getRunAsSubject(final BeanContext callingBeanContext) {
        if (callingBeanContext == null) {
            return null;
        }
        return createRunAsSubject(callingBeanContext.getRunAsUser(), callingBeanContext.getRunAs());
    }

    protected Subject createRunAsSubject(final String runAsUser, final String runAsRole) {
        return createSubject(runAsUser, runAsRole);
    }

    @Override
    public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
        if (reenteredContext == null) {
            JavaSecurityManagers.setContextID(null);
        } else {
            JavaSecurityManagers.setContextID(reenteredContext.getBeanContext().getModuleID());
        }
    }

    protected UUID registerSubject(final Subject subject) {
        final Identity identity = new Identity(subject);
        final UUID token = identity.getToken();
        identities.put(token, identity);
        return token;
    }

    @Override
    public void logout(final UUID securityIdentity) throws LoginException {
        final Identity identity = identities.get(securityIdentity);
        if (identity == null) {
            throw new LoginException("Identity is not currently logged in: " + securityIdentity);
        }
        identities.remove(securityIdentity);
    }

    protected void unregisterSubject(final Object securityIdentity) {
        identities.remove(securityIdentity);
    }

    @Override
    public void associate(final UUID securityIdentity) throws LoginException {
        final Identity existing = clientIdentity.get();
        if (existing != null && existing.getToken() != null /*can be null if enterWebApp is called, this is not a login without a token*/) {
            throw new LoginException("Thread already associated with a client identity.  Refusing to overwrite. " +
                    "(current=" + existing.getToken() + "/" + existing.getSubject() + ", refused=" + securityIdentity + ")");
        }
        if (securityIdentity == null) {
            throw new NullPointerException("The security token passed in is null");
        }

        // The securityIdentity token must associated with a logged in Identity
        final Identity identity = identities.get(securityIdentity);
        if (identity == null) {
            throw new LoginException("Identity is not currently logged in: " + securityIdentity);
        }

        clientIdentity.set(identity);
    }

    @Override
    public UUID disassociate() {
        try {
            final Identity identity = clientIdentity.get();
            return identity == null ? null : identity.getToken();
        } finally {
            clientIdentity.remove();
        }
    }

    @Override
    public boolean isCallerInRole(final String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        final ThreadContext threadContext = ThreadContext.getThreadContext();
        if (threadContext == null) {
            return false;
        }

        final SecurityContext securityContext = threadContext.get(SecurityContext.class);

        if ("**".equals(role)) {
            return securityContext != defaultContext; // ie logged in
        }

        final Set<Group> grps = securityContext.subject.getPrincipals(Group.class);
        for (final Group grp : grps) {
            if (grp.getName().equals(role)) {
                return true;
            }
        }
        final Set<GroupPrincipal> grpsp = securityContext.subject.getPrincipals(GroupPrincipal.class);
        for (final GroupPrincipal grp : grpsp) {
            if (grp.getName().equals(role)) {
                return true;
            }
        }
        return false;
    }

    protected Subject getSubject() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        if (threadContext == null) {
            final Identity id = clientIdentity.get();
            if (id != null) {
                return id.getSubject();
            }
            return new Subject();
        }

        final SecurityContext securityContext = threadContext.get(SecurityContext.class);
        if (securityContext == null) { // unlikely
            return new Subject();
        }
        return securityContext.subject;
    }

    @Override
    public <P extends Principal> Set<P> getPrincipalsByType(final Class<P> pType) {
        if (pType == null) {
            throw new IllegalArgumentException("Principal type can't be null");
        }
        return getSubject().getPrincipals(pType);
    }

    @Override
    public ProtectionDomain getProtectionDomain() {
        return new ProtectionDomain(
            new CodeSource(null, (java.security.cert.Certificate[]) null),
            null, null,
            getSubject().getPrincipals().toArray(new Principal[0])
        );
    }

    @Override
    public Principal getCallerPrincipal() {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        if (threadContext == null) {
            final Identity id = clientIdentity.get();
            if (id != null) {
                return getCallerPrincipal(id.getSubject().getPrincipals());
            }
            return null;
        }

        final SecurityContext securityContext = threadContext.get(SecurityContext.class);
        final Set<Principal> principals = securityContext.subject.getPrincipals();
        return getCallerPrincipal(principals);
    }

    private Principal getCallerPrincipal(final Set<Principal> principals) {
        if (!principals.isEmpty()) {
            for (final Principal principal : principals) {
                if (principal.getClass().isAnnotationPresent(CallerPrincipal.class)) {
                    return principal;
                }
            }
            return principals.iterator().next();
        }
        return null;
    }

    @Override
    public boolean isCallerAuthorized(final Method method, final InterfaceType type) {
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final BeanContext beanContext = threadContext.getBeanContext();
        try {
            final String ejbName = beanContext.getEjbName();
            String name = type == null ? null : type.getSpecName();
            if ("LocalBean".equals(name) || "LocalBeanHome".equals(name)) {
                name = null;
            }
            final Identity currentIdentity = clientIdentity.get();
            final SecurityContext securityContext;
            if (currentIdentity == null) {
                securityContext = threadContext.get(SecurityContext.class);
            } else {
                securityContext = new SecurityContext(currentIdentity.getSubject());
            }
            securityContext.acc.checkPermission(new EJBMethodPermission(ejbName, name, method));
        } catch (final AccessControlException e) {
            return false;
        }
        return true;
    }

    protected static String autoJaccProvider() {
        return SystemInstance.isInitialized() ?
                SystemInstance.get().getProperty(JaccProvider.class.getName(), BasicJaccProvider.class.getName()) :
                BasicJaccProvider.class.getName();
    }

    protected static void installJacc() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final String providerKey = "jakarta.security.jacc.PolicyConfigurationFactory.provider";
        try {
            if (JavaSecurityManagers.getSystemProperty(providerKey) == null) {
                JavaSecurityManagers.setSystemProperty(providerKey, JaccProvider.Factory.class.getName());
                final ClassLoader cl = JaccProvider.Factory.class.getClassLoader();
                Thread.currentThread().setContextClassLoader(cl);
            }

            // Force the loading of the jakarta.security.jacc.PolicyConfigurationFactory.provider
            // Hopefully it will be cached thereafter and ClassNotFoundExceptions thrown
            // from the equivalent call in JaccPermissionsBuilder can be avoided.
            PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (final Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Configuration Factory: " + JavaSecurityManagers.getSystemProperty(providerKey), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        // check the system provided provider first - if for some reason it isn't loaded, load it
        final String systemPolicyProvider = SystemInstance.get().getOptions().getProperties().getProperty("jakarta.security.jacc.policy.provider");
        if (systemPolicyProvider != null && Policy.getPolicy() == null) {
            installPolicy(systemPolicyProvider);
        }

        if (! JaccProvider.Policy.class.getName().equals(Policy.getPolicy().getClass().getName())) {
            // this should delegate to the policy installed above
            installPolicy(JaccProvider.Policy.class.getName());
        }
    }

    private static void installPolicy(String policyProvider) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            final Class policyClass = Class.forName(policyProvider, true, classLoader);
            final Policy policy = (Policy) policyClass.newInstance();
            policy.refresh();
            Policy.setPolicy(policy);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not install JACC Policy Provider: " + policyProvider, e);
        }
    }


    protected Subject createSubject(final String name, final String groupName) {
        if (name == null) {
            return null;
        }

        final User user = new User(name);
        final Group group = new Group(groupName);
        group.addMember(user);

        final HashSet<Principal> principals = new HashSet<>();
        principals.add(user);
        principals.add(group);

        return new Subject(true, principals, new HashSet(), new HashSet());
    }

    @Override
    public Object currentState() {
        return clientIdentity.get();
    }

    @Override
    public void setState(final Object o) {
        if (Identity.class.isInstance(o)) {
            clientIdentity.set(Identity.class.cast(o));
        } else if (o == null) {
            clientIdentity.remove();
        }
    }

    protected SecurityContext getDefaultSecurityContext() {
        return defaultContext;
    }

    @Override
    public boolean supports(final String key) throws PolicyContextException {
        return KEY_SUBJECT.equals(key);
    }

    @Override
    public String[] getKeys() throws PolicyContextException {
        return new String[] {KEY_SUBJECT};
    }

    @Override
    public Object getContext(final String key, final Object data) throws PolicyContextException {
        if (KEY_SUBJECT.equals(key)) {
            return getSubject();
        }
        throw new PolicyContextException("Handler does not support key: " + key);
    }

    public static final class ProvidedSecurityContext {
        public final SecurityContext context;

        public ProvidedSecurityContext(final SecurityContext context) {
            this.context = context;
        }
    }

    public static final class SecurityContext {

        public final Subject subject;
        public final AccessControlContext acc;

        @SuppressWarnings("unchecked")
        public SecurityContext(final Subject subject) {
            this.subject = subject;
            this.acc = (AccessControlContext) Subject.doAsPrivileged(subject, (PrivilegedAction) AccessController::getContext, null);
        }
    }

    protected static class Identity implements Serializable {

        private final Subject subject;
        private final UUID token;

        public Identity(final Subject subject) {
            this.subject = subject;
            this.token = UUID.randomUUID();
        }

        public Identity(final Subject subject, final UUID token) {
            this.subject = subject;
            this.token = token;
        }

        public Subject getSubject() {
            return subject;
        }

        public UUID getToken() {
            return token;
        }
    }

    public static class Group implements java.security.Principal {

        private final List<Principal> members = new ArrayList<>();
        private final String name;

        public Group(final String name) {
            this.name = name;
        }

        public boolean addMember(final Principal user) {
            return members.add(user);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    @CallerPrincipal // to force it to be before group in getCallerPrincipal, otherwise we aren't deterministic
    public static class User implements Principal {

        private final String name;

        public User(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final User user = User.class.cast(o);
            return !(!Objects.equals(name, user.name));

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
