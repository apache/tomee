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

package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.StringTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InitEjbDeployments implements DynamicDeployer {
    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final StringTemplate deploymentIdTemplate;
    private static final String DEPLOYMENT_ID_FORMAT = "openejb.deploymentId.format";

    public InitEjbDeployments() {
        final String format = SystemInstance.get().getOptions().get(DEPLOYMENT_ID_FORMAT, "{ejbName}");
        this.deploymentIdTemplate = new StringTemplate(format);
    }

    public synchronized AppModule deploy(final AppModule appModule) throws OpenEJBException {

        final Set<String> abstractSchemaNames = new HashSet<>();
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (isCmpEntity(bean)) {
                    final EntityBean entity = (EntityBean) bean;
                    final String name = entity.getAbstractSchemaName();
                    if (name != null) {
                        abstractSchemaNames.add(name);
                    }
                }
            }
        }


        final Map<String, String> contextData = new HashMap<>();
        contextData.put("appId", appModule.getModuleId());

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            contextData.put("host", ejbModule.isWebapp() ? findHost(ejbModule.getModuleId(), appModule.getWebModules()) : appModule.uniqueHostIfExists());
            contextData.put("hash", Integer.toString(ejbModule.hashCode()));
            contextData.put("ejbJarId", ejbModule.getModuleId());
            deploy(ejbModule, contextData, abstractSchemaNames);
        }
        contextData.clear();
        return appModule;
    }

    private String findCommonHost(final Collection<WebModule> webModules) {
        String host = null;
        for (final WebModule w: webModules) {
            final String wHost = w.getHost();
            if (host == null) {
                host = wHost;
            } else if (!host.equals(wHost)) {
                return "lib"; // surely better to do
            }
        }
        return host != null ? host : "localhost";
    }

    private String findHost(final String id, final Collection<WebModule> webModules) {
        for (final WebModule w: webModules) {
            if (w.getModuleId().equals(id)) {
                final String host = w.getHost();
                return host != null ? host : "localhost";
            }
        }
        return "localhost";
    }

    public EjbModule deploy(final EjbModule ejbModule) throws OpenEJBException {
        return deploy(ejbModule, new HashMap<>(), new HashSet<>());
    }

    private EjbModule deploy(final EjbModule ejbModule, final Map<String, String> contextData, final Set<String> abstractSchemaNames) throws OpenEJBException {
        contextData.put("moduleId", ejbModule.getModuleId());
        contextData.put("moduleUri", ejbModule.getModuleUri().toString());

        final OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        StringTemplate deploymentIdTemplate = this.deploymentIdTemplate;
        if (openejbJar.getProperties().containsKey(DEPLOYMENT_ID_FORMAT)) {
            final String format = openejbJar.getProperties().getProperty(DEPLOYMENT_ID_FORMAT);
            logger.info("Using " + DEPLOYMENT_ID_FORMAT + " '" + format + "'");
            deploymentIdTemplate = new StringTemplate(format);
        }


        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            StringTemplate template = deploymentIdTemplate;

            final org.apache.openejb.api.EjbDeployment annotation = getEjbDeploymentAnnotation(ejbModule, bean);

            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null) {

                ejbDeployment = new EjbDeployment();

                ejbDeployment.setEjbName(bean.getEjbName());

                if (annotation != null && isDefined(annotation.id())) {
                    template = new StringTemplate(annotation.id());
                    ejbDeployment.setDeploymentId(formatDeploymentId(bean, contextData, template));
                } else {
                    ejbDeployment.setDeploymentId(formatDeploymentId(bean, contextData, template));
                    if (!(bean instanceof ManagedBean) || !((ManagedBean) bean).isHidden()) {
                        logger.info("Auto-deploying ejb " + bean.getEjbName() + ": EjbDeployment(deployment-id=" + ejbDeployment.getDeploymentId() + ")");
                    }
                }

                openejbJar.getEjbDeployment().add(ejbDeployment);
            } else if (ejbDeployment.getDeploymentId() == null) {
                if (annotation != null && isDefined(annotation.id())) {
                    template = new StringTemplate(annotation.id());
                    ejbDeployment.setDeploymentId(formatDeploymentId(bean, contextData, template));
                } else {
                    ejbDeployment.setDeploymentId(formatDeploymentId(bean, contextData, template));
                    logger.info("Auto-assigning deployment-id for ejb " + bean.getEjbName() + ": EjbDeployment(deployment-id=" + ejbDeployment.getDeploymentId() + ")");
                }
            }

            if (ejbDeployment.getContainerId() == null && annotation != null && isDefined(annotation.container())) {
                ejbDeployment.setContainerId(annotation.container());
            }

            if (isCmpEntity(bean)) {
                final EntityBean entity = (EntityBean) bean;
                if (entity.getAbstractSchemaName() == null) {
                    String abstractSchemaName = bean.getEjbName().trim().replaceAll("[ \\t\\n\\r-]+", "_");

                    // The AbstractSchemaName must be unique, we should check that it is
                    if (abstractSchemaNames.contains(abstractSchemaName)) {
                        int i = 2;
                        while (abstractSchemaNames.contains(abstractSchemaName + i)) {
                            i++;
                        }
                        abstractSchemaName = abstractSchemaName + i;
                    }

                    abstractSchemaNames.add(abstractSchemaName);
                    entity.setAbstractSchemaName(abstractSchemaName);
                }
            }
        }

        return ejbModule;
    }

    private org.apache.openejb.api.EjbDeployment getEjbDeploymentAnnotation(final EjbModule ejbModule, final EnterpriseBean bean) {
        try {
            final Class<?> beanClass = ejbModule.getClassLoader().loadClass(bean.getEjbClass());
            return beanClass.getAnnotation(org.apache.openejb.api.EjbDeployment.class);
        } catch (final ClassNotFoundException e) {
            // this should never happen, the class has already been loaded a ton of times by this point
            // unfortunately we have some unit tests that prevent us from throwing an exception just in case
            // Those are OpenEjb2ConversionTest and SunCmpConversionTest
            return null;
        }
    }

    private boolean isDefined(final String s) {
        return s != null && !"".equals(s);
    }

    private static boolean isCmpEntity(final EnterpriseBean bean) {
        return bean instanceof EntityBean && ((EntityBean) bean).getPersistenceType() == PersistenceType.CONTAINER;
    }

    private String formatDeploymentId(final EnterpriseBean bean, final Map<String, String> contextData, final StringTemplate template) {
        contextData.put("ejbType", bean.getClass().getSimpleName());
        contextData.put("ejbClass", bean.getEjbClass());

        // we don't have the ejb class object (only the string name) so we have
        // to extract the simple name from the FQN of the class
        final int simpleNameIdx = bean.getEjbClass().lastIndexOf('.');
        contextData.put("ejbClass.simpleName", bean.getEjbClass().substring(simpleNameIdx + 1));

        contextData.put("ejbName", bean.getEjbName());

        final String name = template.apply(contextData);
        if (bean instanceof CompManagedBean) { // avoid conflict in ear between an ejbmodule and a war using the same name
            return name + System.identityHashCode(bean);
        }
        return name;
    }
}
