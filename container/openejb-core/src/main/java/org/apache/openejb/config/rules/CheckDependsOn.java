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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class CheckDependsOn extends ValidationBase {

    public void validate(AppModule appModule) {
        module = appModule;

        LinkResolver<Bean> app = new LinkResolver<Bean>();

        for (EjbModule ejbModule : appModule.getEjbModules()) {

            String moduleId = ejbModule.getModuleId();

            URI moduleUri = null;
            if (moduleId != null) {
                try {
                    moduleUri = new URI(moduleId);
                } catch (URISyntaxException e) {
                    return;
                }
            }

            Resolver<Bean> resolver = new Resolver(app, new LinkResolver<Bean>());

            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                Bean b = new Bean(bean, ejbModule, moduleUri, resolver);

                resolver.module.add(ejbModule.getModuleId(), bean.getEjbName(), b);

                resolver.app.add(ejbModule.getModuleId(), bean.getEjbName(), b);

            }

        }

        boolean missingBeans = false;
        for (Bean bean : app.values()) {
            EnterpriseBean enterpriseBean = bean.bean;

            if (!(enterpriseBean instanceof SessionBean)) continue;

            SessionBean sessionBean = (SessionBean) enterpriseBean;

            if (sessionBean.getSessionType() != SessionType.SINGLETON) continue;

            for (String ejbName : sessionBean.getDependsOn()) {
                Bean referee = bean.resolveLink(ejbName);
                if (referee == null) {
                    bean.module.getValidation().fail(enterpriseBean.getEjbName(), "dependsOn.noSuchEjb", ejbName);
                    missingBeans = true;
                } else {
                    bean.dependsOn.add(referee);
                }
            }
        }

       // if (missingBeans) return;

        try {
            References.sort(new ArrayList<Bean>(app.values()), new References.Visitor<Bean>() {
                public String getName(Bean t) {
                    return t.getId();
                }

                public Set<String> getReferences(Bean t) {
                    LinkedHashSet<String> refs = new LinkedHashSet<String>();
                    for (Bean bean : t.dependsOn) {
                        refs.add(bean.getId());
                    }
                    return refs;
                }
            });
        } catch (CircularReferencesException e) {
            for (List<Bean> circuit : e.getCircuits()) {
                List<String> ejbNames = new ArrayList<String>(circuit.size());
                for (Bean bean : circuit) {
                    ejbNames.add(bean.bean.getEjbName());
                }
                fail("EAR", "dependsOn.circuit", Join.join(" -> ", ejbNames), ejbNames.get(0));
            }
        }

    }

    public static class Resolver<T> {
        private final LinkResolver<T> module;
        private final LinkResolver<T> app;

        public Resolver(LinkResolver<T> app, LinkResolver<T> module) {
            this.app = app;
            this.module = module;
        }

        public T resolveLink(String link, URI moduleUri) {
            T value = module.resolveLink(link, moduleUri);
            if (value != null) return value;

            return app.resolveLink(link, moduleUri);
        }
    }

    public static class Bean {
        private final URI moduleUri;
        private final EnterpriseBean bean;
        private final ArrayList<Bean> dependsOn = new ArrayList<Bean>();
        private final EjbModule module;
        private final Resolver<Bean> resolver;

        public Bean(EnterpriseBean bean, EjbModule module, URI moduleUri, Resolver<Bean> resolver) {
            this.bean = bean;
            this.module = module;
            this.moduleUri = moduleUri;
            this.resolver = resolver;
        }

        public Bean resolveLink(String ejbName) {
            return resolver.resolveLink(ejbName, moduleUri);
        }

        public String getId() {
            return toString();
        }
    }
}
