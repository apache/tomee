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
import org.apache.tomee.catalina.realm.event.DigestAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.FindSecurityConstraintsEvent;
import org.apache.tomee.catalina.realm.event.GssAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.HasResourcePermissionEvent;
import org.apache.tomee.catalina.realm.event.HasRoleEvent;
import org.apache.tomee.catalina.realm.event.HasUserDataPermissionEvent;
import org.apache.tomee.catalina.realm.event.SslAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;
import org.apache.webbeans.config.WebBeansContext;
import org.ietf.jgss.GSSContext;

import javax.enterprise.inject.spi.BeanManager;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

/**
 * This simple CDI based realm gives the ability to send events a webapp can react to in order to authenticate the user.
 *
 * There is one different event per credential types to make it easier to implement.
 */
public class CdiEventRealm implements Realm {

    protected Container container = null;
    protected final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private CredentialHandler credentialHandler;


    @Override
    public Principal authenticate(final String username, final String credentials) {
        if (beanManager() == null) {
            return null;
        }

        final UserPasswordAuthenticationEvent event = new UserPasswordAuthenticationEvent(username, credentials);
        beanManager().fireEvent(event);
        return event.getPrincipal();
    }

    @Override
    public Principal authenticate(final String username, final String digest, final String nonce, final String nc,
                                  final String cnonce, final String qop, final String realm, final String md5a2) {
        if (beanManager() == null) {
            return null;
        }

        final DigestAuthenticationEvent event = new DigestAuthenticationEvent(username, digest, nonce, nc,
                cnonce, qop, realm, md5a2);
        beanManager().fireEvent(event);
        return event.getPrincipal();
    }

    @Override
    public Principal authenticate(final GSSContext gssContext, final boolean storeCreds) {
        if (beanManager() == null) {
            return null;
        }

        final GssAuthenticationEvent event = new GssAuthenticationEvent(gssContext, storeCreds);
        beanManager().fireEvent(event);
        return event.getPrincipal();
    }

    @Override
    public Principal authenticate(final X509Certificate[] certs) {
        if (beanManager() == null) {
            return null;
        }

        final SslAuthenticationEvent event = new SslAuthenticationEvent(certs);
        beanManager().fireEvent(event);
        return event.getPrincipal();
    }

    @Override
    public void backgroundProcess() {
        // no-op for now
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(final Request request, final Context context) {
        if (beanManager() == null) {
            return null;
        }

        final FindSecurityConstraintsEvent event = new FindSecurityConstraintsEvent(request, context);
        beanManager().fireEvent(event);
        return event.getSecurityConstraints();
    }

    @Override
    public boolean hasResourcePermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraint,
                                         final Context context) throws IOException {
        if (beanManager() == null) {
            return false;
        }

        final HasResourcePermissionEvent event = new HasResourcePermissionEvent(request, response, constraint, context);
        beanManager().fireEvent(event);
        return event.isHasResourcePermission();
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        if (beanManager() == null) {
            return false;
        }

        final HasRoleEvent event = new HasRoleEvent(wrapper, principal, role);
        beanManager().fireEvent(event);
        return event.isHasRole();
    }

    @Override
    public boolean hasUserDataPermission(final Request request, final Response response, final SecurityConstraint[] constraint) throws IOException {
        if (beanManager() == null) {
            return false;
        }

        final HasUserDataPermissionEvent event = new HasUserDataPermissionEvent(request, response, constraint);
        beanManager().fireEvent(event);
        return event.isHasUserDataPermission();
    }

    @Override
    public Container getContainer() {
        return (container);
    }

    @Override
    public void setContainer(final Container container) {
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);
    }

    @Override
    public CredentialHandler getCredentialHandler() {
        return credentialHandler;
    }

    @Override
    public void setCredentialHandler(final CredentialHandler credentialHandler) {
        this.credentialHandler = credentialHandler;
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private BeanManager beanManager() {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        if (webBeansContext == null) {
            return null; // too early to have a cdi bean
        }
        return webBeansContext.getBeanManagerImpl();
    }
}
