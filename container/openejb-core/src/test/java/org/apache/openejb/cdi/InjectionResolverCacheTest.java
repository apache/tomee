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
package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.container.InjectionResolver;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.List;
import java.util.Properties;


/**
 * @version $Rev$ $Date$
 */

@RunWith(ApplicationComposer.class)
public class InjectionResolverCacheTest {

    @Module
    @Classes(cdi = true, value = { SimpleLogger.class })
    public EjbJar ejb() {
        return new EjbJar();
    }

    @Module
    @Classes(cdi = true)
    public WebApp web() { // we have shortcut when we have a single module so adding another one ensure we test an "ear"
        return new WebApp();
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("openejb.cache.cdi-type-resolution-failure", "true")
            .build();
    }

    public static class SimpleLogger {
        public SimpleLogger() {

        }

        public void log(final String toLog) {
            if (toLog != null) {
                System.out.println(toLog);
            }
        }
    }

    @Test
    public void check() {
        final List<AppContext> appCtxs = SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts();

        final WebBeansContext webBeansContext = appCtxs.get(0).getWebContexts().get(0).getWebBeansContext();
        Assert.assertTrue(webBeansContext instanceof WebappWebBeansContext);

        final WebappWebBeansContext webappWebBeansContext = (WebappWebBeansContext) webBeansContext;
        final BeanManagerImpl bm = webappWebBeansContext.getBeanManagerImpl();
        final InjectionResolver injectionResolver = bm.getInjectionResolver();
        Assert.assertTrue(injectionResolver instanceof WebAppInjectionResolver);
        final WebAppInjectionResolver webAppInjectionResolver = (WebAppInjectionResolver) injectionResolver;

        Assert.assertEquals(0, webAppInjectionResolver.getCacheSize());

        final Bean<?> bean = bm.resolve(bm.getBeans(SimpleLogger.class ));
        final CreationalContext<?> context = bm.createCreationalContext(bean);
        final SimpleLogger logger = (SimpleLogger) bm.getReference(bean, SimpleLogger.class, context);

        logger.log("Testing from web context in EAR");
        Assert.assertEquals(1, webAppInjectionResolver.getCacheSize());
    }
}
