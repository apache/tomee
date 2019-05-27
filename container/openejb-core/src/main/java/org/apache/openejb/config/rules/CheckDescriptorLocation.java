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

package org.apache.openejb.config.rules;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.util.CollectionsUtil.safe;


public class CheckDescriptorLocation extends ValidationBase {

    @Override
    public void validate(final AppModule appModule) {

        final List<String> validated = new ArrayList<>();

        for (final WebModule webModule : safe(appModule.getWebModules())) {
            validated.add(webModule.getModuleId());
            validateWebModule(webModule);
        }

        for (final EjbModule ejbModule : safe(appModule.getEjbModules())) {
            //without this check, CheckDescriptorLocationTest#testWarWithDescriptorInRoot() would fail
            if (!validated.contains(ejbModule.getModuleId())) {
                validateEjbModule(ejbModule);
            }
        }

    }

    private void validateWebModule(final DeploymentModule webModule) {
        this.module = webModule;
        final List<String> descriptorsToSearch = Arrays.asList("beans.xml", "ejb-jar.xml", "faces-config.xml");
        final File file = webModule.getFile();
        if (file != null) {
            try {
                final URL rootOfArchive = file.toURI().toURL();
                final URL metaInf = new URL(rootOfArchive.toExternalForm() + "META-INF/");
                final Map<String, URL> incorrectlyLocatedDescriptors = retrieveDescriptors(descriptorsToSearch, rootOfArchive, metaInf);
                warnIncorrectLocationOfDescriptors(incorrectlyLocatedDescriptors, "WEB-INF");
            } catch (final MalformedURLException ignored) {
                //ignored
            }
        }

    }

    public void validateEjbModule(final DeploymentModule deploymentModule) {
        this.module = deploymentModule;
        final List<String> descriptorsToSearch = Arrays.asList("beans.xml", "ejb-jar.xml", "openejb-jar.xml", "env-entries.properties");
        final File file = deploymentModule.getFile();
        if (file != null) {
            try {
                final URL rootOfArchive = file.toURI().toURL();
                final Map<String, URL> incorrectlyLocatedDescriptors
                    = retrieveDescriptors(descriptorsToSearch, rootOfArchive);
                warnIncorrectLocationOfDescriptors(incorrectlyLocatedDescriptors, "META-INF");
            } catch (final MalformedURLException ignored) {
                //ignored
            }
        }

    }

    private static Map<String, URL> retrieveDescriptors(final List<String> descriptorsToSearch, final URL... locationsToSearch) {
        final Map<String, URL> descriptorAndWrongLocation = new HashMap<>();
        final ResourceFinder finder = new ResourceFinder(locationsToSearch);
        for (final String descriptor : descriptorsToSearch) {
            final URL resource = finder.getResource(descriptor);
            if (resource != null) {
                descriptorAndWrongLocation.put(descriptor, resource);
            }
        }

        return descriptorAndWrongLocation;
    }

    private void warnIncorrectLocationOfDescriptors(final Map<String, URL> descriptorsPlacedInWrongLocation, final String expectedLocation) {
        for (final Map.Entry<String, URL> map : descriptorsPlacedInWrongLocation.entrySet()) {

            warn(this.module.toString(), "descriptor.incorrectLocation", map.getValue().toExternalForm(), expectedLocation);
        }
    }
}