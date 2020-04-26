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

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.realm.RealmBase;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomee.catalina.realm.event.DigestAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.FindSecurityConstraintsEvent;
import org.apache.tomee.catalina.realm.event.GssAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.SslAuthenticationEvent;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;
import org.apache.webbeans.config.WebBeansContext;
import org.ietf.jgss.GSSContext;

import jakarta.enterprise.inject.spi.BeanManager;
import java.security.Principal;
import java.security.cert.X509Certificate;

/**
 * This simple CDI based realm gives the ability to send events a webapp can react to in order to authenticate the user.
 *
 * There is one different event per credential types to make it easier to implement.
 */
public class CdiEventRealm extends RealmBase {

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
        final SecurityConstraint[] sc = super.findSecurityConstraints(request, context);

        if (beanManager() == null) {
            return sc;
        }

        final FindSecurityConstraintsEvent event = new FindSecurityConstraintsEvent(request.getRequest(), context.getPath());
        beanManager().fireEvent(event);

        if (!event.getRoles().isEmpty()) {
            final SecurityConstraint s = new SecurityConstraint();
            final SecurityCollection collection = new SecurityCollection();

            collection.addPattern("/*"); // only for the current request
            collection.addMethod(request.getMethod());
            s.addCollection(collection);

            if (event.getUserConstraint() != null) {
                s.setUserConstraint(event.getUserConstraint());
            }

            for(final String r: event.getRoles()) {
                s.addAuthRole(r);
            }

            return new SecurityConstraint[] { s };
        }

        return sc;
    }

    @Override
    protected String getPassword(final String username) {
        // must never happen cause we overridden all authenticate() mthd
        throw new UnsupportedOperationException();
    }

    @Override
    protected Principal getPrincipal(final String username) {
        // must never happen cause we overridden all authenticate() mthd
        throw new UnsupportedOperationException();
    }

    private BeanManager beanManager() {
        final WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        if (webBeansContext == null) {
            return null; // too early to have a cdi bean
        }
        return webBeansContext.getBeanManagerImpl();
    }
}
