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
package org.apache.openejb.config.rules;

import org.apache.openejb.config.EjbModule;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CheckDescriptorLocation extends ValidationBase {


    EjbModule currentModule;

    @Override
    public void validate(EjbModule ejbModule) {
        URL baseUrl = null;
        this.currentModule = ejbModule;
        File file = ejbModule.getFile();
        if (file != null) {
            validateDescriptorsAreNotPlacedInRoot(file);
        }

    }

    private void validateDescriptorsAreNotPlacedInRoot(File file) {
        ResourceFinder resourceFinder = null;
        try {
            resourceFinder = new ResourceFinder(file.toURI().toURL());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //ResourceFinder resourceFinder = new ResourceFinder(baseUrl);
        Map<String, URL> descriptorsPlacedInWrongLocation = getDescriptorsPlacedInWrongLocation(resourceFinder);

        if (descriptorsPlacedInWrongLocation.size() > 0) {
            warnIncorrectLocationOfDescriptors(descriptorsPlacedInWrongLocation);

        }
    }

    private static Map<String, URL> getDescriptorsPlacedInWrongLocation(
            ResourceFinder finder) {

        Map<String, URL> descriptorsMap = null;
        try {
            descriptorsMap = retrieveDescriptorsPlacedInWrongLocation(finder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return descriptorsMap;
    }


    private static Map<String, URL> retrieveDescriptorsPlacedInWrongLocation(
            ResourceFinder finder) throws IOException {
        final Map<String, URL> map = new HashMap<String, URL>();
        String[] known = {"web.xml", "ejb-jar.xml", "openejb-jar.xml", "env-entries.properties", "beans.xml", "ra.xml", "application.xml", "application-client.xml", "persistence.xml"};
        for (String descriptor : known) {
            final URL url = finder.getResource(descriptor);
            if (url != null) map.put(descriptor, url);
        }

        return map;
    }

    private void warnIncorrectLocationOfDescriptors(Map<String, URL> descriptorsPlacedInWrongLocation) {
        for (Map.Entry<String, URL> map : descriptorsPlacedInWrongLocation.entrySet()) {

            warn(currentModule.toString(), "descriptor.incorrectLocation", map.getKey(), map.getValue());
        }
    }
}