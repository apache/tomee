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
package org.apache.openejb.config;

import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.net.URL;
import static java.util.Arrays.asList;

public class FinderFactory {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, FinderFactory.class);

    private static final FinderFactory factory = singleton();

    private static FinderFactory singleton() {
        try {
            ClassLoader loader = FinderFactory.class.getClassLoader();
            Class<?> clazz = loader.loadClass("org.apache.openejb.core.osgi.BundleFinderFactory");
            return (FinderFactory) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            logger.debug("Optional OSGi Bundle annotation scanning not installed");
        } catch (Exception e) {
            logger.error("Failed creating BundleFinderFactory", e);
        }
        
        return new FinderFactory();
    }

    private static FinderFactory get() {
        FinderFactory factory = SystemInstance.get().getComponent(FinderFactory.class);
        return (factory != null)? factory: FinderFactory.factory;
    }

    public static AbstractFinder createFinder(DeploymentModule module) throws Exception {
        return get().create(module);
    }

    public AbstractFinder create(DeploymentModule module) throws Exception {
        if (module instanceof WebModule) {
            WebModule webModule = (WebModule) module;
            File file = new File(webModule.getJarLocation());
            URL[] urls = DeploymentLoader.getWebappUrls(file);
            final ClassLoader webClassLoader = webModule.getClassLoader();
            return new ClassFinder(webClassLoader, asList(urls));
        }

        if (module.getJarLocation() != null) {
            String location = module.getJarLocation();
            File file = new File(location);

            URL url;
            if (file.exists()) {
                url = file.toURI().toURL();
            } else {
                url = new URL(location);
            }
            return new ClassFinder(module.getClassLoader(), url);
        } else {
            return new ClassFinder(module.getClassLoader());
        }
    }
}
