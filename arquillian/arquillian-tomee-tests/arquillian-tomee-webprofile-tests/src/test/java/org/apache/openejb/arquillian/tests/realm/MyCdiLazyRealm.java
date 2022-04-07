/**
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
package org.apache.openejb.arquillian.tests.realm;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.ietf.jgss.GSSContext;

import jakarta.enterprise.context.ApplicationScoped;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import static java.util.Arrays.asList;

@ApplicationScoped // can't be request scoped cause it is a realm impl
public class MyCdiLazyRealm implements Realm {
    private Container container;

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(final Container container) {
        this.container = container;
    }

    @Override
    public CredentialHandler getCredentialHandler() {
        return null;
    }

    @Override
    public void setCredentialHandler(final CredentialHandler credentialHandler) {

    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {

    }

    @Override
    public Principal authenticate(final String username) {
        return "user".equalsIgnoreCase(username) ? new GenericPrincipal(username, "pwd", asList("role")) : null;
    }

    @Override
    public Principal authenticate(final String username, final String credentials) {
        return "user".equalsIgnoreCase(username) && "pwd".equalsIgnoreCase(credentials) ? new GenericPrincipal(username, "pwd", asList("role")) : null;
    }

    @Override
    public Principal authenticate(final String username, final String digest, final String nonce,
                                  final String nc, final String cnonce, final String qop,
                                  final String realm, final String md5a2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal authenticate(final GSSContext gssContext, final boolean storeCreds) {
        throw new UnsupportedOperationException();
    }

    @Override public Principal authenticate(final GSSName gssName, final GSSCredential gssCredential) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal authenticate(final X509Certificate[] certs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void backgroundProcess() {
        // no-op
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(final Request request, final Context context) {
        return null;
    }

    @Override
    public boolean hasResourcePermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraint, final Context context) throws IOException {
        return false;
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        return false;
    }

    @Override
    public boolean hasUserDataPermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraint) throws IOException {
        return false;
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        // no-op
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
