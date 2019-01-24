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

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ResourceFinder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import static java.util.Arrays.asList;

enum EntityMappingURLFinder implements BiFunction<String, AppModule, URL> {



    INSTANCE;
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, EntityMappingURLFinder.class);

    private final DefaultFinder defaultFinder = new DefaultFinder();

    private final URLFinder urlFinder = new URLFinder();

    private final AppModuleMetaInfFinder appModuleMetaInfFinder = new AppModuleMetaInfFinder();

    private final List<BiFunction<String, AppModule, URL>> finders = asList(defaultFinder, urlFinder, appModuleMetaInfFinder);


    @Override
    public URL apply(String location, AppModule appModule) {
        for (BiFunction<String, AppModule, URL> finder : finders) {
            URL url = finder.apply(location, appModule);
            if (Objects.nonNull(url)) {
                return url;
            }
        }
        return null;
    }


    private class DefaultFinder implements BiFunction<String, AppModule, URL> {

        @Override
        public URL apply(String location, AppModule appModule) {
            return Thread.currentThread().getContextClassLoader().getResource(location);
        }
    }

    private class URLFinder implements BiFunction<String, AppModule, URL> {

        @Override
        public URL apply(String location, AppModule appModule) {
            try {
                return new URL(location);
            } catch (MalformedURLException e) {
                LOGGER.info("Unable to using URL: " + location);
                return null;
            }
        }
    }

    private class AppModuleMetaInfFinder implements BiFunction<String, AppModule, URL> {

        @Override
        public URL apply(String location, AppModule appModule) {

            if (!location.contains(DeploymentLoader.META_INF)) {
                return null;
            }

            for (EjbModule ejbModule : appModule.getEjbModules()) {

                URL url = getUrl(location, ejbModule);
                if (Objects.nonNull(url)) {
                    return url;
                }
            }
            return null;
        }

        private URL getUrl(String location, EjbModule ejbModule) {
            try {
                final ResourceFinder finder = new ResourceFinder("", ejbModule.getClassLoader());
                Map<String, URL> map = DeploymentLoader.mapDescriptors(finder);
                String fileName = location.replace(DeploymentLoader.META_INF, "");
                return map.get(fileName);
            } catch (Exception ex) {
                LOGGER.error("Unable to read entity mappings from " + location, ex);
                return null;
            }
        }
    }
}
