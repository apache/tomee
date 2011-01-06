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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;


import javax.enterprise.inject.spi.Bean;


import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.InjectionBuilder;
import org.apache.openejb.assembler.classic.JndiEncBuilder;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.config.AnnotationDeployer;
import org.apache.openejb.config.JndiEncInfoBuilder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PassthroughFactory;
import org.apache.webbeans.spi.ResourceInjectionService;
import org.apache.webbeans.spi.api.ResourceReference;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.naming.Context;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CdiResourceInjectionService implements ResourceInjectionService {

    private Logger logger = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), CdiResourceInjectionService.class);
    private ClassLoader classLoader;
    private AppInfo appModule;
    private final Map<CdiBeanInfo, Context> contexts = new HashMap<CdiBeanInfo, Context>();

    public CdiResourceInjectionService() {

    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setAppModule(AppInfo appModule) {
        this.appModule = appModule;
    }

    public void buildInjections(Set<Class<?>> managedBeanClasses) throws OpenEJBException {
        AnnotationDeployer deployer = new AnnotationDeployer();

        for (Class<?> clazz : managedBeanClasses) {

            CdiBeanInfo cdiInfo = new CdiBeanInfo();
            cdiInfo.setClassLoader(classLoader);
            cdiInfo.setBeanName(clazz.getName());
            cdiInfo.setBeanClass(clazz);
            deployer.deploy(cdiInfo);

            JndiEncInfoBuilder infoBuilder = new JndiEncInfoBuilder(appModule);
            JndiEncInfo moduleJndiEnc = new JndiEncInfo();
            JndiEncInfo jndiEnc = new JndiEncInfo();
            infoBuilder.build(cdiInfo, cdiInfo.getBeanName(), appModule.appId, moduleJndiEnc, jndiEnc);

            InjectionBuilder builder = new InjectionBuilder(classLoader);
            List<Injection> injections = builder.buildInjections(jndiEnc);

            cdiInfo.setInjections(injections);
            // TODO: handle app/global/module namespaces?
            JndiEncBuilder encBuilder = new JndiEncBuilder(jndiEnc, injections, appModule.appId, classLoader);
            this.contexts.put(cdiInfo, encBuilder.build(JndiEncBuilder.JndiScope.comp));
        }
    }


    @Override
    public <X, T extends Annotation> X getResourceReference(ResourceReference<X, T> resourceReference) {
        for (Entry<CdiBeanInfo, Context> entry : this.contexts.entrySet()) {
            if (entry.getKey().getBeanClass() == resourceReference.getOwnerClass()) {
                List<Injection> injections = entry.getKey().getInjections();
                for (Injection injection : injections) {
                    if (injection.getTarget() == resourceReference.getOwnerClass() &&
                            injection.getName().equals(resourceReference.getName())) {
                        Context context = InjectionProcessor.unwrap(entry.getValue());
                        try {
                            return resourceReference.getResourceType().cast(context.lookup(injection.getJndiName()));
                        } catch (NamingException e) {
                            logger.warning("Injection data not found in JNDI context: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget().getName() + "/" + injection.getName());
                            return null;
                        }

                    }
                }
            }
        }
        return null;
    }

    @Override
    public void injectJavaEEResources(Object managedBeanInstance)
            throws Exception {

        for (Entry<CdiBeanInfo, Context> entry : this.contexts.entrySet()) {
            if (entry.getKey().getBeanClass() == managedBeanInstance.getClass()) {
                ObjectRecipe receipe = PassthroughFactory.recipe(managedBeanInstance);
                receipe.allow(Option.FIELD_INJECTION);
                receipe.allow(Option.PRIVATE_PROPERTIES);
                receipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                receipe.allow(Option.NAMED_PARAMETERS);

                fillInjectionProperties(receipe, entry.getKey().getInjections(), managedBeanInstance.getClass(), entry.getValue());
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fillInjectionProperties(ObjectRecipe objectRecipe, List<Injection> injections, Class<?> beanClass, Context context) {
        if (injections == null) return;

        boolean usePrefix = true;
        try {
            if (beanClass != null) beanClass.getConstructor();
        } catch (NoSuchMethodException e) {
            // Using constructor injection
            // xbean can't handle the prefix yet
            usePrefix = false;
        }

        Class<?> clazz = beanClass;

        for (Injection injection : injections) {
            if (!injection.getTarget().isAssignableFrom(clazz)) continue;
            try {
                String jndiName = injection.getJndiName();
                Object value = InjectionProcessor.unwrap(context).lookup(jndiName);

                String prefix;
                if (usePrefix) {
                    prefix = injection.getTarget().getName() + "/";
                } else {
                    prefix = "";
                }

                objectRecipe.setProperty(prefix + injection.getName(), value);
            } catch (NamingException e) {
                logger.warning("Injection data not found in JNDI context: jndiName='" + injection.getJndiName() + "', target=" + injection.getTarget().getName() + "/" + injection.getName());
            }
        }
    }

    @Override
    public void clear() {
        this.contexts.clear();
    }

    /**
     * delegation of serialization behavior
     */
    public <T> void writeExternal(Bean<T> bean, T actualResource, ObjectOutput out) throws IOException{}

    /**
     * delegation of serialization behavior
     */
    public <T> T readExternal(Bean<T> bean, ObjectInput out) throws IOException,
								    ClassNotFoundException {
        return null;
    }

}
