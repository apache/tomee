/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.lifecycle;

import java.util.Properties;
import java.util.logging.Logger;

import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.el.ELContextStore;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.WebBeansUtil;

/**
 * Default LifeCycle for a standalone application without a ServletContainer.
 */
public class StandaloneLifeCycle extends AbstractLifeCycle
{
    public StandaloneLifeCycle(Properties properties, Logger logger)
    {
        super(properties);
        this.logger = logger;
    }
    
    public StandaloneLifeCycle()
    {
        this(null, WebBeansLoggerFacade.getLogger(StandaloneLifeCycle.class));
    }

    @Override
    public void beforeStartApplication(Object object)
    {
        webBeansContext.getContextFactory().initRequestContext(null);
        webBeansContext.getContextFactory().initSessionContext(null);
        webBeansContext.getContextFactory().initConversationContext(null);
        webBeansContext.getContextFactory().initApplicationContext(null);
        webBeansContext.getContextFactory().initSingletonContext(null);
    }

    @Override
    public void beforeStopApplication(Object endObject)
    {
        webBeansContext.getContextFactory().destroyRequestContext(null);
        webBeansContext.getContextFactory().destroySessionContext(null);
        webBeansContext.getContextFactory().destroyConversationContext();
        webBeansContext.getContextFactory().destroyApplicationContext(null);
        webBeansContext.getContextFactory().destroySingletonContext(null);

        // clean up the EL caches after each request
        ELContextStore elStore = ELContextStore.getInstance(false);
        if (elStore != null)
        {
            elStore.destroyELContextStore();
        }
    }

    @Override
    protected void afterStopApplication(Object stopObject)
    {
        WebBeansFinder.clearInstances(WebBeansUtil.getCurrentClassLoader());
    }
}
