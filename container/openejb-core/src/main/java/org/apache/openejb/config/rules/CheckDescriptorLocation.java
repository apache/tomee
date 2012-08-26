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

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ResourceFinder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.apache.openejb.util.CollectionsUtil.safe;


public class CheckDescriptorLocation extends ValidationBase {


    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_VALIDATION, "org.apache.openejb.config.rules");

    @Override
    public void validate(AppModule appModule){

        List<String> validated = new ArrayList<String>();

        for(WebModule webModule: safe(appModule.getWebModules()))
        {
            validated.add(webModule.getModuleId());
            validateWebModule(webModule);
        }

        for(EjbModule ejbModule: safe(appModule.getEjbModules()))
        {
            //without this check, CheckDescriptorLocationTest#testWarWithDescriptorInRoot() would fail
            if(!validated.contains(ejbModule.getModuleId()))
            {
                validateEjbModule(ejbModule);
            }
        }

    }




    private void validateWebModule(DeploymentModule webModule) {
        URL baseUrl = null;
        this.module= webModule;
        List<String> descriptorsToSearch = Arrays.asList("beans.xml","ejb-jar.xml","faces-config.xml");
        File file = webModule.getFile();
        if (file != null) {
            try {
                URL rootOfArchive=file.toURI().toURL();
                URL metaInf=new URL(rootOfArchive.toExternalForm()+"META-INF/");
                Map<String, URL> incorrectlyLocatedDescriptors
                        = retrieveDescriptors(file, descriptorsToSearch, rootOfArchive, metaInf);
                warnIncorrectLocationOfDescriptors(incorrectlyLocatedDescriptors,"WEB-INF");
            } catch (MalformedURLException ignored) {
                    //ignored
            }
        }

    }

    public void validateEjbModule(DeploymentModule deploymentModule) {
        URL baseUrl = null;
        this.module= deploymentModule;
        List<String> descriptorsToSearch = Arrays.asList("beans.xml","ejb-jar.xml","openejb-jar.xml","env-entries.properties");
        File file = deploymentModule.getFile();
        if (file != null) {
            try {
                URL rootOfArchive=file.toURI().toURL();
                Map<String, URL> incorrectlyLocatedDescriptors
                        = retrieveDescriptors(file, descriptorsToSearch, rootOfArchive);
                warnIncorrectLocationOfDescriptors(incorrectlyLocatedDescriptors,"META-INF");
            } catch (MalformedURLException ignored) {
                  //ignored
            }
        }

    }

    private static Map<String,URL> retrieveDescriptors(File file, List<String> descriptorsToSearch, URL... locationsToSearch ){
      final Map<String,URL>  descriptorAndWrongLocation = new HashMap<String,URL>();


            ResourceFinder finder = new ResourceFinder(locationsToSearch);
            for(String descriptor:descriptorsToSearch)
            {
                URL resource = finder.getResource(descriptor);
                if(resource!=null)
                {
                   descriptorAndWrongLocation.put(descriptor,resource);
                }
            }

        return descriptorAndWrongLocation;
    }

    private void warnIncorrectLocationOfDescriptors(Map<String, URL> descriptorsPlacedInWrongLocation, String expectedLocation) {
        for (Map.Entry<String, URL> map : descriptorsPlacedInWrongLocation.entrySet()) {

            warn(this.module.toString(), "descriptor.incorrectLocation", map.getValue().toExternalForm(), expectedLocation);
        }
    }
}