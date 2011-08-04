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
package org.apache.openejb.cdi;

import java.util.List;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.logger.WebBeansLogger;

/**
 * @version $Rev$ $Date$
 */
public class CdiBuilder {
    private static final WebBeansLogger logger = WebBeansLogger.getLogger(CdiBuilder.class);

    public CdiBuilder() {
    }

    public void build(AppInfo appInfo, AppContext appContext, List<BeanContext> allDeployments) {

        ThreadContext.addThreadContextListener(new RequestScopedThreadContextListener());
        ThreadSingletonService singletonService = SystemInstance.get().getComponent(ThreadSingletonService.class);
        logger.info("existing thread singleton service in SystemInstance() " + singletonService);
        //TODO hack for tests.  Currently initialized in OpenEJB line 90.  cf alternative in AccessTimeoutTest which would
        //presumably have to be replicated in about 70 other tests.
        if (singletonService == null) {
            singletonService = initializeOWB(getClass().getClassLoader());
        }
        singletonService.initialize(new StartupObject(appContext, appInfo, allDeployments));
    }

    private boolean hasBeans(AppInfo appInfo) {
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            if (ejbJar.beans != null) return true;
        }

        return false;
    }

    public static ThreadSingletonService initializeOWB(ClassLoader classLoader) {
        ThreadSingletonService singletonService = new ThreadSingletonServiceImpl();
        logger.info("Created new singletonService " + singletonService);
        SystemInstance.get().setComponent(ThreadSingletonService.class, singletonService);
        try {
            WebBeansFinder.setSingletonService(singletonService);
            logger.info("succeeded in installing singleton service");
        } catch (Exception e) {
            //ignore
            logger.info("Could not install our singleton service", e);
        }
        //TODO there must be a better place to initialize this
        ThreadContext.addThreadContextListener(new OWBContextThreadListener());
        return singletonService;
    }

}
