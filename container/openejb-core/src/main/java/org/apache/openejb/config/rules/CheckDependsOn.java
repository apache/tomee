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

package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.util.CircularReferencesException;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LinkResolver;
import org.apache.openejb.util.References;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class CheckDependsOn extends ValidationBase {

    public void validate(final AppModule appModule) {
        module = appModule;

        final LinkResolver<Bean> app = new LinkResolver<>();

        for (final EjbModule ejbModule : appModule.getEjbModules()) {

            final Resolver<Bean> resolver = new Resolver(app, new LinkResolver<Bean>());

            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                final Bean b = new Bean(bean, ejbModule, ejbModule.getModuleUri(), resolver);

                resolver.module.add(ejbModule.getModuleUri(), bean.getEjbName(), b);

                resolver.app.add(ejbModule.getModuleUri(), bean.getEjbName(), b);

            }

        }

        for (final Bean bean : app.values()) {
            final EnterpriseBean enterpriseBean = bean.bean;

            if (!(enterpriseBean instanceof SessionBean)) {
                continue;
            }

            final SessionBean sessionBean = (SessionBean) enterpriseBean;

            if (sessionBean.getSessionType() != SessionType.SINGLETON) {
                continue;
            }

            for (final String ejbName : sessionBean.getDependsOn()) {
                final Bean referee = bean.resolveLink(ejbName);
                if (referee == null) {
                    bean.module.getValidation().fail(enterpriseBean.getEjbName(), "dependsOn.noSuchEjb", ejbName);
                } else {
                    bean.dependsOn.add(referee);
                }
            }
        }

        try {
            References.sort(new ArrayList<Bean>(app.values()), new References.Visitor<Bean>() {
                public String getName(final Bean t) {
                    return t.getId();
                }

                public Set<String> getReferences(final Bean t) {
                    final LinkedHashSet<String> refs = new LinkedHashSet<>();
                    for (final Bean bean : t.dependsOn) {
                        refs.add(bean.getId());
                    }
                    return refs;
                }
            });
        } catch (final CircularReferencesException e) {
            for (final List<Bean> circuit : e.getCircuits()) {
                final List<String> ejbNames = new ArrayList<>(circuit.size());
                for (final Bean bean : circuit) {
                    ejbNames.add(bean.bean.getEjbName());
                }
                fail("EAR", "dependsOn.circuit", Join.join(" -> ", ejbNames), ejbNames.get(0));
            }
        }

    }

    public static class Resolver<T> {
        private final LinkResolver<T> module;
        private final LinkResolver<T> app;

        public Resolver(final LinkResolver<T> app, final LinkResolver<T> module) {
            this.app = app;
            this.module = module;
        }

        public T resolveLink(final String link, final URI moduleUri) {
            final T value = module.resolveLink(link, moduleUri);
            if (value != null) {
                return value;
            }

            return app.resolveLink(link, moduleUri);
        }
    }

    public static class Bean {
        private final URI moduleUri;
        private final EnterpriseBean bean;
        private final ArrayList<Bean> dependsOn = new ArrayList<>();
        private final EjbModule module;
        private final Resolver<Bean> resolver;

        public Bean(final EnterpriseBean bean, final EjbModule module, final URI moduleUri, final Resolver<Bean> resolver) {
            this.bean = bean;
            this.module = module;
            this.moduleUri = moduleUri;
            this.resolver = resolver;
        }

        public Bean resolveLink(final String ejbName) {
            return resolver.resolveLink(ejbName, moduleUri);
        }

        public String getId() {
            return toString();
        }
    }
}
