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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public interface DeploymentModule {
    static String OPENEJB_MODULENAME_USE_HASH = "openejb.modulename.useHash";

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

    class ID {
        private final String name;
        private final File location;
        private final URI uri;

        public ID(NamedModule vendorDd, NamedModule specDd, String name, File location, URI uri, DeploymentModule module) {
            this.name = name(vendorDd, specDd, uri, location, name, module);
            this.location = location(location, uri);
            this.uri = uri(uri, location, this.name);
        }

        private URI uri(URI uri, File location, String name) {
            if (uri != null) return uri;
            if (location != null) return location.toURI();
            return URI.create(name);
        }

        private File location(File location, URI uri) {
            if (location != null) return location;
            if (uri != null && uri.isAbsolute()) return new File(uri);
            return null;
        }

        private String name(NamedModule vendor, NamedModule spec, URI uri, File location, String name, DeploymentModule module) {
            if (name != null && !name.startsWith("@")) return name;
            if (vendor != null && vendor.getModuleName() != null) return vendor.getModuleName().trim();
            if (vendor != null && vendor.getId() != null) return vendor.getId().trim();
            if (spec != null && spec.getModuleName() != null) return spec.getModuleName().trim();
            if (spec != null && spec.getId() != null) return spec.getId().trim();
            if (uri != null) return stripExtension(uri.getPath());
            if (location != null && SystemInstance.get().getOptions().get(OPENEJB_MODULENAME_USE_HASH, false)) return moduleName(location) + module.hashCode();
            if (location != null) return moduleName(location);
            if (name != null) return name;
            return "@" + module.getClass().getSimpleName() + module.hashCode();
        }

        private String moduleName(File location) {
            List<String> invalid = new ArrayList<String>();
            invalid.add("classes");
            invalid.add("test-classes");
            invalid.add("target");
            invalid.add("build");
            invalid.add("dist");
            invalid.add("bin");

            while (invalid.contains(location.getName())) {
                location = location.getParentFile();
            }
            return stripExtension(location.getName());
        }

        private String stripExtension(String name) {
            String[] exts = {".jar", ".zip", ".ear", ".war", ".rar", ".unpacked"};
            for (String ext : exts) {
                if (name.endsWith(ext)) {
                    return name.substring(0, name.length() - ext.length());
                }
            }
            return name;
        }

        public String getName() {
            if (name.startsWith("@")) return name.substring(1);
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
