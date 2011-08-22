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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.service.DefaultLoaderService;
import org.apache.webbeans.spi.LoaderService;
import org.apache.webbeans.spi.plugins.OpenWebBeansPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class OptimizedLoaderService implements LoaderService {

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB.createChild("cdi"), OptimizedLoaderService.class);

    private final LoaderService loaderService;

    public OptimizedLoaderService() {
        this(new DefaultLoaderService());
    }

    public OptimizedLoaderService(LoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @Override
    public <T> List<T> load(Class<T> serviceType) {
        return load(serviceType, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public <T> List<T> load(Class<T> serviceType, ClassLoader classLoader) {
        // ServiceLoader is expensive (can take up to a half second).  This is an optimization
        if (OpenWebBeansPlugin.class.equals(serviceType)) return loadWebBeansPlugins(classLoader);

        // As far as we know, this only is reached for CDI Extension discovery
        return loaderService.load(serviceType, classLoader);
    }

    private <T> List<T> loadWebBeansPlugins(ClassLoader loader) {
        String[] knownPlugins = {
                "org.apache.openejb.cdi.CdiPlugin",
                "org.apache.geronimo.openejb.cdi.GeronimoWebBeansPlugin",
                "org.apache.webbeans.jsf.plugin.OpenWebBeansJsfPlugin",
        };

        List<T> list = new ArrayList<T>();
        for (String name : knownPlugins) {
            Class<T> clazz;
            try {
                clazz = (Class<T>) loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore
                continue;
            }

            try {
                list.add(clazz.newInstance());
            } catch (Exception e) {
                log.error("Unable to load OpenWebBeansPlugin: " + name);
            }
        }
        return list;
    }
}
