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
package org.apache.tomee.catalina.realm;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.openejb.config.sys.PropertiesAdapter;
import org.apache.tomee.catalina.TomEERuntimeException;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.ietf.jgss.GSSContext;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Set;

public class LazyRealm implements Realm {
    private String realmClass;
    private String properties;
    private boolean cdi = false;

    private volatile Realm delegate = null;
    private Container container = null;

    private CreationalContext<Object> creationalContext = null;

    public void setRealmClass(final String realmClass) {
        this.realmClass = realmClass;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public void setCdi(final boolean cdi) {
        this.cdi = cdi;
    }

    private Realm instance() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    final Object instance;

                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if (container != null && container.getLoader() != null && container.getLoader().getClassLoader() != null) {
                        cl = container.getLoader().getClassLoader();
                    }

                    final Class<?> clazz;
                    try {
                        clazz = cl.loadClass(realmClass);
                    } catch (ClassNotFoundException e) {
                        throw new TomEERuntimeException(e);
                    }

                    if (!cdi) {
                        try {
                            final ObjectRecipe recipe = new ObjectRecipe(clazz);
                            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                            recipe.allow(Option.FIELD_INJECTION);
                            recipe.allow(Option.PRIVATE_PROPERTIES);
                            if (properties != null) {
                                final Properties props = new PropertiesAdapter()
                                        .unmarshal(properties.trim().replaceAll("\\p{Space}*(\\p{Alnum}*)=", "\n$1="));
                                recipe.setAllProperties(props);
                            }
                            instance = recipe.create();
                        } catch (Exception e) {
                            throw new TomEERuntimeException(e);
                        }
                    } else {
                        final BeanManager bm = WebBeansContext.currentInstance().getBeanManagerImpl();
                        final Set<Bean<?>> beans = bm.getBeans(clazz);
                        final Bean<?> bean = bm.resolve(beans);
                        creationalContext = bm.createCreationalContext(null);
                        instance = bm.getReference(bean, clazz, creationalContext);
                    }

                    if (instance == null) {
                        throw new TomEERuntimeException("realm can't be retrieved from cdi");
                    }
                    if (instance instanceof Realm) {
                        delegate = (Realm) instance;
                    } else {
                        delegate = new LowTypedRealm(instance);
                    }
                    delegate.setContainer(container);
                }
            }
        }
        return delegate;
    }

    @Override
    public Container getContainer() {
        if (delegate != null) {
            return instance().getContainer();
        }
        return container;
    }

    @Override
    public void setContainer(final Container container) {
        container.addLifecycleListener(new LifecycleListener() {
            @Override
            public void lifecycleEvent(final LifecycleEvent event) {
                if (Lifecycle.BEFORE_STOP_EVENT.equals(event.getType())) {
                    if (creationalContext != null) {
                        creationalContext.release();
                    }
                }
            }
        });

        if (delegate != null) {
            instance().setContainer(container);
        } else {
            this.container = container;
        }
    }

    @Override
    public String getInfo() {
        return instance().getInfo();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        instance().addPropertyChangeListener(listener);
    }

    @Override
    public Principal authenticate(final String username, final String credentials) {
        return instance().authenticate(username, credentials);
    }

    @Override
    public Principal authenticate(final String username, final String digest, final String nonce, final String nc,
                                  final String cnonce, final String qop, final String realm, final String md5a2) {
        return instance().authenticate(username, digest, nonce, nc, cnonce, qop, realm, md5a2);
    }

    @Override
    public Principal authenticate(final GSSContext gssContext, final boolean storeCreds) {
        return instance().authenticate(gssContext, storeCreds);
    }

    @Override
    public Principal authenticate(final X509Certificate[] certs) {
        return instance().authenticate(certs);
    }

    @Override
    public void backgroundProcess() {
        instance().backgroundProcess();
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(final Request request, final Context context) {
        return instance().findSecurityConstraints(request, context);
    }

    @Override
    public boolean hasResourcePermission(final Request request, final Response response,
                                         final SecurityConstraint[] constraint, final Context context) throws IOException {
        return instance().hasResourcePermission(request, response, constraint, context);
    }

    @Override
    public boolean hasRole(final Wrapper wrapper, final Principal principal, final String role) {
        return instance().hasRole(wrapper, principal, role);
    }

    @Override
    public boolean hasUserDataPermission(final Request request, final Response response, final SecurityConstraint[] constraint) throws IOException {
        return instance().hasUserDataPermission(request, response, constraint);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        instance().removePropertyChangeListener(listener);
    }
}
