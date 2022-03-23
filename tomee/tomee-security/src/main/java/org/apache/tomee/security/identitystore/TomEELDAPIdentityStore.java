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
package org.apache.tomee.security.identitystore;

import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.IdentityStorePermission;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.list;
import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;
import static javax.naming.Context.SECURITY_AUTHENTICATION;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;
import static javax.naming.directory.SearchControls.ONELEVEL_SCOPE;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

// todo create an LDAPtive version of it for pooling and other capabilities
@ApplicationScoped
public class TomEELDAPIdentityStore implements IdentityStore {

    private static final String DEFAULT_USER_FILTER = "(&(%s=%s)(|(objectclass=user)(objectclass=person)(objectclass=inetOrgPerson)(objectclass=organizationalPerson))(!(objectclass=computer)))";
    private static final String DEFAULT_GROUP_FILTER = "(&(%s=%s)(|(objectclass=group)(objectclass=groupofnames)(objectclass=groupofuniquenames)))";

    @Inject
    private Supplier<LdapIdentityStoreDefinition> definitionSupplier;
    private LdapIdentityStoreDefinition definition;

    private Set<ValidationType> validationTypes;

    @PostConstruct
    private void init() throws Exception {
        definition = definitionSupplier.get();
        validationTypes = new HashSet<>(asList(definition.useFor()));
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
        final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;

        LdapContext ldapContext = null;
        try {

            // init ldap context for future searches
            ldapContext = lookup(definition.url(), definition.bindDn(), definition.bindDnPassword());

            // retrieve the caller DN based on the user login (credentials)
            final String callerName = usernamePasswordCredential.getCaller();
            final String callerDn = getCallerDn(ldapContext, callerName);

            // if not found
            if (callerDn == null) {
                return INVALID_RESULT;
            }

            // do a direct bind with the caller DN we found and the provided password
            if (!authenticateWithCallerDn(usernamePasswordCredential, callerDn)) {
                return INVALID_RESULT;
            }

            // find the groups
            Set<String> groups = null;
            if (validationTypes().contains(ValidationType.PROVIDE_GROUPS)) {
                groups = getGroupsWithCallerDn(ldapContext, callerDn);
            }

            return new CredentialValidationResult(
                null,
                callerName,
                callerDn,
                null,
                groups);

        } finally {
            silentlyCloseLdapContext(ldapContext);
        }

    }

    private Set<String> getGroupsWithCallerDn(final LdapContext ldapContext, final String callerDn) {
        if (StringUtils.isEmpty(callerDn)) {
            return emptySet();
        }

        if (StringUtils.isEmpty(definition.groupSearchBase())
            && StringUtils.isNotEmpty(definition.groupMemberOfAttribute())) {

            Set<String> groups = null;
            try {
                final Attributes attributes = ldapContext.getAttributes(callerDn, new String[]{definition.groupMemberOfAttribute()});
                final Attribute memberOfAttribute = attributes.get(definition.groupMemberOfAttribute());

                groups = new HashSet<>();
                if (memberOfAttribute != null) {
                    for (Object group : list(memberOfAttribute.getAll())) {
                        if (group != null) {
                            final LdapName dn = new LdapName(group.toString());
                            final Attribute attribute = dn.getRdn(dn.size() - 1).toAttributes().get(definition.groupNameAttribute());
                            if (attribute == null) {
                                throw new RuntimeException(definition.groupNameAttribute() + "does not match any group in DN: " + group.toString());
                            }
                            final String groupName = attribute.get(0).toString();
                            if (groupName != null) {
                                groups.add(groupName);
                            }
                        }
                    }
                }

            } catch (final NamingException e) {
                // todo better exception handling
                throw new RuntimeException(e);

            }
            return groups;

        } else {

            String filter = null;
            if (StringUtils.isNotEmpty(definition.groupSearchFilter())) {
                filter = format(definition.groupSearchFilter(), callerDn);

            } else {
                filter = format(DEFAULT_GROUP_FILTER, definition.groupMemberAttribute(), callerDn);
            }

            final List<SearchResult> searchResults = query(ldapContext, definition.groupSearchBase(), filter, getGroupSearchControls());

            Set<String> groups = new HashSet<>();
            try {
                for (SearchResult searchResult : searchResults) {
                    Attribute attribute = searchResult.getAttributes().get(definition.groupNameAttribute());
                    if (attribute != null) {
                        for (Object group : list(attribute.getAll())) {
                            if (group != null) {
                                groups.add(group.toString());
                            }
                        }
                    }
                }

            } catch (final NamingException e) {
                // todo better exception handling
                throw new RuntimeException(e);

            }
            return groups;
        }
    }

    private boolean authenticateWithCallerDn(
        final UsernamePasswordCredential usernamePasswordCredential,
        final String callerDn) {

        try {
            // do a direct bind and see if an exception happens
            silentlyCloseLdapContext(lookup(definition.url(), callerDn, usernamePasswordCredential.getPasswordAsString()));
            return true;

        } catch (final Exception e) {
            return false;
        }
    }

    private void silentlyCloseLdapContext(final LdapContext callerDnContext) {
        if (callerDnContext == null) {
            return;
        }

        try {
            callerDnContext.close();

        } catch (final NamingException e) {
            // ignore
        }
    }

    private String getCallerDn(final LdapContext ldapContext, final String callerName) {

        String callerDn = null;

        if (StringUtils.isNotEmpty(definition.callerBaseDn())
            && StringUtils.isNotEmpty(definition.callerNameAttribute())
            && StringUtils.isEmpty(definition.callerSearchBase())) {

            // caller DN may be provided in annotation
            callerDn = format("%s=%s,%s", definition.callerNameAttribute(), callerName,
                              definition.callerBaseDn());

        } else {

            // let's try to look it up in LDAP
            String filter = null;
            if (StringUtils.isNotEmpty(definition.callerSearchFilter())) {
                filter = format(definition.callerSearchFilter(),
                                callerName);

            } else {
                filter = format(DEFAULT_USER_FILTER,
                                definition.callerNameAttribute(),
                                callerName);
            }

            final List<SearchResult> callerDns =
                query(ldapContext, definition.callerSearchBase(), filter, getCallerSearchControls());

            if (callerDns.size() == 1) {
                callerDn = callerDns.get(0).getNameInNamespace();
            }

            // todo if more than one result????
        }

        return callerDn;
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new IdentityStorePermission("getGroups"));
        }

        LdapContext ldapContext = null;
        try {
            ldapContext = lookup(definition.url(), definition.bindDn(), definition.bindDnPassword());

            String callerDn = validationResult.getCallerDn();

            // if not set as CallerDn, try to find it based on the principal name
            if (StringUtils.isEmpty(callerDn)) {
                callerDn = getCallerDn(ldapContext, validationResult.getCallerPrincipal().getName());
            }

            return getGroupsWithCallerDn(ldapContext, callerDn);

        } finally {
            silentlyCloseLdapContext(ldapContext);
        }
    }

    @Override
    public int priority() {
        return definition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }

    public static LdapContext lookup(final String url, final String bindDn, final String bindDnPassword) {
        final Hashtable<String, String> ldapEnvironment = new Hashtable<>();

        ldapEnvironment.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnvironment.put(PROVIDER_URL, url);

        ldapEnvironment.put(SECURITY_AUTHENTICATION, "simple");
        ldapEnvironment.put(SECURITY_PRINCIPAL, bindDn);
        ldapEnvironment.put(SECURITY_CREDENTIALS, bindDnPassword);

        try {
            return new InitialLdapContext(ldapEnvironment, null);

        } catch (final Exception e) {
            // todo better exception handling
            throw new RuntimeException(e);
        }
    }

    private SearchControls getCallerSearchControls() {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(of(definition.callerSearchScope()));
        controls.setCountLimit(definition.maxResults());
        controls.setTimeLimit(definition.readTimeout());
        return controls;
    }

    private SearchControls getGroupSearchControls() {
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(of(definition.groupSearchScope()));
        controls.setCountLimit(definition.maxResults());
        controls.setTimeLimit(definition.readTimeout());
        controls.setReturningAttributes(new String[]{definition.groupNameAttribute()});
        return controls;
    }

    private static int of(LdapIdentityStoreDefinition.LdapSearchScope searchScope) {
        if (searchScope == LdapIdentityStoreDefinition.LdapSearchScope.ONE_LEVEL) {
            return ONELEVEL_SCOPE;

        } else if (searchScope == LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE) {
            return SUBTREE_SCOPE;

        } else {
            return ONELEVEL_SCOPE;
        }
    }

    private static List<SearchResult> query(
        final LdapContext ldapContext,
        final String base,
        final String filter,
        final SearchControls controls) {

        try {
            return list(ldapContext.search(base, filter, controls));

        } catch (final Exception e) {
            // todo better exception handling
            throw new RuntimeException(e);
        }
    }

}
