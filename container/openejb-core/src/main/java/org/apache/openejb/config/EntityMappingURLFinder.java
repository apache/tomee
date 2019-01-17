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
import java.util.Map;
import java.util.function.BiFunction;

interface EntityMappingURLFinder extends BiFunction<String, AppModule, URL> {


    class DefaultFinder implements EntityMappingURLFinder {

        @Override
        public URL apply(String location, AppModule appModule) {
            return Thread.currentThread().getContextClassLoader().getResource(location);
        }
    }

    class URLFinder implements EntityMappingURLFinder {

        @Override
        public URL apply(String location, AppModule appModule) {
            try {
                return new URL(location);
            } catch (MalformedURLException e) {
                CmpJpaConversion.LOGGER.error("Unable to read entity mappings from " + location, e);
                return null;
            }
        }
    }

    class AppModuleMetaInfFinder implements EntityMappingURLFinder {

        @Override
        public URL apply(String location, AppModule appModule) {
            if (!location.contains(DeploymentLoader.META_INF)) {
                return null;
            }

            for (EjbModule ejbModule : appModule.getEjbModules()) {

                try {
                    final ResourceFinder finder = new ResourceFinder("", ejbModule.getClassLoader());
                    Map<String, URL> stringURLMap = DeploymentLoader.mapDescriptors(finder);
                    String fileName = location.replace(DeploymentLoader.META_INF, "");
                    boolean exist = stringURLMap.get(fileName) != null;
                    System.out.println(exist);
                } catch (Exception ex) {
                    CmpJpaConversion.LOGGER.error("Unable to read entity mappings from " + location, ex);
                    return null;
                }
            }
        }
    }
