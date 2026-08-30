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

import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.LdapIdentityStoreDefinition;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TomEELDAPIdentityStoreTest {

    @Test
    public void emptyPasswordDoesNotBind() throws Exception {
        final TomEELDAPIdentityStore store = new TomEELDAPIdentityStore();

        final LdapIdentityStoreDefinition definition = mock(LdapIdentityStoreDefinition.class);
        final Field definitionField = TomEELDAPIdentityStore.class.getDeclaredField("definition");
        definitionField.setAccessible(true);
        definitionField.set(store, definition);

        final Method authenticate = TomEELDAPIdentityStore.class.getDeclaredMethod(
            "authenticateWithCallerDn", UsernamePasswordCredential.class, String.class);
        authenticate.setAccessible(true);

        final boolean result = (Boolean) authenticate.invoke(store,
            new UsernamePasswordCredential("user", ""), "cn=user,ou=people,dc=example,dc=org");

        assertFalse(result);
        verify(definition, never()).url();
    }

    @Test
    public void escapeFilterValue() {
        assertEquals("plainUser", TomEELDAPIdentityStore.escapeFilterValue("plainUser"));
        assertEquals("a\\2a", TomEELDAPIdentityStore.escapeFilterValue("a*"));
        assertEquals("back\\5cslash", TomEELDAPIdentityStore.escapeFilterValue("back\\slash"));
        assertEquals("nul\\00byte", TomEELDAPIdentityStore.escapeFilterValue("nul\0byte"));
        assertEquals("\\2a\\29\\28objectclass=\\2a", TomEELDAPIdentityStore.escapeFilterValue("*)(objectclass=*"));
        assertNull(TomEELDAPIdentityStore.escapeFilterValue(null));
    }

    @Test
    public void callerNameIsEscapedInSearchFilter() throws Exception {
        final LdapIdentityStoreDefinition definition = mock(LdapIdentityStoreDefinition.class);
        when(definition.callerSearchBase()).thenReturn("ou=people,dc=example,dc=org");
        when(definition.callerNameAttribute()).thenReturn("uid");
        when(definition.callerSearchScope()).thenReturn(LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE);
        when(definition.callerSearchFilter()).thenReturn("");

        final NamingEnumeration<SearchResult> noResults = emptyEnumeration();
        final LdapContext ldapContext = mock(LdapContext.class);
        when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(noResults);

        final String filter = searchFilterFor(definition, ldapContext, "user)(objectclass=*");
        assertEquals("(&(uid=user\\29\\28objectclass=\\2a)(|(objectclass=user)(objectclass=person)(objectclass=inetOrgPerson)(objectclass=organizationalPerson))(!(objectclass=computer)))", filter);
    }

    @Test
    public void callerNameIsEscapedInCustomSearchFilter() throws Exception {
        final LdapIdentityStoreDefinition definition = mock(LdapIdentityStoreDefinition.class);
        when(definition.callerSearchBase()).thenReturn("ou=people,dc=example,dc=org");
        when(definition.callerNameAttribute()).thenReturn("uid");
        when(definition.callerSearchScope()).thenReturn(LdapIdentityStoreDefinition.LdapSearchScope.SUBTREE);
        when(definition.callerSearchFilter()).thenReturn("(cn=%s)");

        final NamingEnumeration<SearchResult> noResults = emptyEnumeration();
        final LdapContext ldapContext = mock(LdapContext.class);
        when(ldapContext.search(anyString(), anyString(), any(SearchControls.class))).thenReturn(noResults);

        final String filter = searchFilterFor(definition, ldapContext, "a*");
        assertEquals("(cn=a\\2a)", filter);
    }

    @Test
    public void callerNameIsEscapedInCallerDn() throws Exception {
        final LdapIdentityStoreDefinition definition = mock(LdapIdentityStoreDefinition.class);
        when(definition.callerBaseDn()).thenReturn("ou=people,dc=example,dc=org");
        when(definition.callerNameAttribute()).thenReturn("uid");
        when(definition.callerSearchBase()).thenReturn("");

        final TomEELDAPIdentityStore store = storeWith(definition);
        final Method getCallerDn = TomEELDAPIdentityStore.class.getDeclaredMethod(
            "getCallerDn", LdapContext.class, String.class);
        getCallerDn.setAccessible(true);

        final String callerDn = (String) getCallerDn.invoke(store, mock(LdapContext.class), "user,ou=admins");
        assertEquals("uid=user\\,ou\\=admins,ou=people,dc=example,dc=org", callerDn);
    }

    private static String searchFilterFor(final LdapIdentityStoreDefinition definition,
                                          final LdapContext ldapContext,
                                          final String callerName) throws Exception {
        final TomEELDAPIdentityStore store = storeWith(definition);
        final Method getCallerDn = TomEELDAPIdentityStore.class.getDeclaredMethod(
            "getCallerDn", LdapContext.class, String.class);
        getCallerDn.setAccessible(true);
        getCallerDn.invoke(store, ldapContext, callerName);

        final ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
        verify(ldapContext).search(eq("ou=people,dc=example,dc=org"), filter.capture(), any(SearchControls.class));
        return filter.getValue();
    }

    private static TomEELDAPIdentityStore storeWith(final LdapIdentityStoreDefinition definition) throws Exception {
        final TomEELDAPIdentityStore store = new TomEELDAPIdentityStore();
        final Field definitionField = TomEELDAPIdentityStore.class.getDeclaredField("definition");
        definitionField.setAccessible(true);
        definitionField.set(store, definition);
        return store;
    }

    @SuppressWarnings("unchecked")
    private static NamingEnumeration<SearchResult> emptyEnumeration() {
        final NamingEnumeration<SearchResult> enumeration = mock(NamingEnumeration.class);
        when(enumeration.hasMoreElements()).thenReturn(false);
        return enumeration;
    }
}
