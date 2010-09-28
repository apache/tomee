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
package org.apache.openejb.server.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.jaxws.ClientConfigurationFactory;
import org.apache.axis2.jaxws.util.ClassLoaderUtils;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.util.Hashtable;
import java.util.Map;

public class Axis2ClientConfigurationFactory extends ClientConfigurationFactory {
    private static final Logger logger = Logger.getInstance(LogCategory.AXIS2, Axis2ClientConfigurationFactory.class);

    private Map<ClassLoader, ConfigurationContext> contextCache = new Hashtable<ClassLoader, ConfigurationContext>();

    private boolean reuseConfigurationContext;

    public Axis2ClientConfigurationFactory(boolean reuse) {
        this.reuseConfigurationContext = reuse;
    }

    public ConfigurationContext getClientConfigurationContext() {
        ClassLoader cl = ClassLoaderUtils.getContextClassLoader(null);
        if (cl == null) {
            if (this.reuseConfigurationContext) {
                cl = ClientConfigurationFactory.class.getClassLoader();
            } else {
                return createConfigurationContext();
            }
        }

        synchronized (cl) {
            return getConfigurationContext(cl);
        }
    }

    private ConfigurationContext getConfigurationContext(ClassLoader cl) {
        ConfigurationContext context = this.contextCache.get(cl);
        if (context == null) {
            context = createConfigurationContext();
            this.contextCache.put(cl, context);
            if (logger.isDebugEnabled()) {
                logger.debug("Created new configuration context " + context + "  for " + cl);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Configuration context " + context + " reused for " + cl);
            }
        }
        return context;
    }

    private ConfigurationContext removeConfigurationContext(ClassLoader cl) {
        return this.contextCache.remove(cl);
    }

    public void clearCache() {
        this.contextCache.clear();
    }

    public ConfigurationContext clearCache(ClassLoader cl) {
        ConfigurationContext context = null;
        if (cl != null) {
            synchronized (cl) {
                context = removeConfigurationContext(cl);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Removed configuration context " + context + " for " + cl);
            }
        }

        return context;
    }

    private ConfigurationContext createConfigurationContext() {
        String repoPath = System.getProperty(Constants.AXIS2_REPO_PATH);
        String axisConfigPath = System.getProperty(Constants.AXIS2_CONFIG_PATH);
        try {
            return ConfigurationContextFactory.createConfigurationContextFromFileSystem(repoPath, axisConfigPath);
        } catch (AxisFault e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
