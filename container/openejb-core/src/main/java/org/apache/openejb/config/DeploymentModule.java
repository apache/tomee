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

import org.apache.openejb.jee.NamedModule;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.URLs;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public interface DeploymentModule {
    String OPENEJB_MODULENAME_USE_HASH = "openejb.modulename.useHash";

    String getModuleId();

    URI getModuleUri();

    ClassLoader getClassLoader();

    String getJarLocation();

    File getFile();

    Map<String, Object> getAltDDs();

    ValidationContext getValidation();

    Set<String> getWatchedResources();

    boolean isStandaloneModule();

    void setStandaloneModule(boolean isStandalone);

    Properties getProperties();

    AppModule appModule();

    class ID {
        private final String name;
        private final File location;
        private final URI uri;
        private boolean overriden;

        /**
         * The intention of this is to hold the extracted and archived versions
         */
        private final Set<String> locations = new LinkedHashSet<>();

        public ID(final NamedModule vendorDd, final NamedModule specDd, final String name, final File location, final URI uri, final DeploymentModule module) {
            this.name = name(vendorDd, specDd, uri, location, name, module);
            this.location = location(location, uri);
            this.uri = uri(uri, location, this.name);
            if (location != null) {
                this.locations.add(location.getAbsolutePath());
            }
        }

        public Set<String> getLocations() {
            return locations;
        }

        private URI uri(final URI uri, final File location, final String name) {
            if (uri != null) {
                return uri;
            }
            if (location != null) {
                return location.toURI();
            }
            return URLs.uri(name);
        }

        private File location(final File location, final URI uri) {
            if (location != null) {
                return location;
            }
            if (uri != null && uri.isAbsolute()) {
                return new File(uri);
            }
            return null;
        }

        private String name(final NamedModule vendor, final NamedModule spec, final URI uri, final File location, final String name, final DeploymentModule module) {
            if (location != null) {
                final String systPropName = SystemInstance.get().getOptions().get(location.getName() + ".moduleId", (String) null);
                if (systPropName != null) {
                    overriden = true;
                    return systPropName;
                }
            }

            if (spec != null && spec.getModuleName() != null) {
                return spec.getModuleName().trim(); // used to override defaults so do it first
            }
            if (name != null && !name.startsWith("@")) {
                return name;
            }
            if (vendor != null && vendor.getModuleName() != null) {
                return vendor.getModuleName().trim();
            }
            if (vendor != null && vendor.getId() != null) {
                return vendor.getId().trim();
            }
            if (spec != null && spec.getId() != null) {
                return spec.getId().trim();
            }
            if (uri != null) {
                return stripExtension(uri.getPath());
            }
            if (location != null && SystemInstance.get().getOptions().get(OPENEJB_MODULENAME_USE_HASH, false)) {
                return moduleName(location) + module.hashCode();
            }
            if (location != null) {
                return moduleName(location);
            }
            if (name != null) {
                return name;
            }
            return "@" + module.getClass().getSimpleName() + module.hashCode();
        }

        public boolean isOverriden() {
            return overriden;
        }

        private String moduleName(final File location) {
            return stripExtension(NameFiltering.filter(location).getName());
        }

        private String stripExtension(final String name) {
            final String[] exts = {".jar", ".zip", ".ear", ".war", ".rar", ".unpacked"};
            for (final String ext : exts) {
                if (name.endsWith(ext)) {
                    return name.substring(0, name.length() - ext.length());
                }
            }
            return name;
        }

        public String getName() {
            if (name.startsWith("@")) {
                return name.substring(1);
            }
            return name;
        }

        public File getLocation() {
            return location;
        }

        public URI getUri() {
            return uri;
        }
    }
}
