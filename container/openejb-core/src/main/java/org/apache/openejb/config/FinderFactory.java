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

import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;

import java.io.File;
import java.net.URL;

public class FinderFactory {

    private static final FinderFactory factory = new FinderFactory();

    private static FinderFactory get() {
        FinderFactory factory = SystemInstance.get().getComponent(FinderFactory.class);
        return (factory != null)? factory: FinderFactory.factory;
    }

    public static IAnnotationFinder createFinder(DeploymentModule module) throws Exception {
        return get().create(module);
    }

    public IAnnotationFinder create(DeploymentModule module) throws Exception {
        if (module instanceof WebModule) {
            WebModule webModule = (WebModule) module;
            final ClassLoader webClassLoader = webModule.getClassLoader();
            return new AnnotationFinder(new AggregatedArchive(webClassLoader, webModule.getScannableUrls())).link();
        }
        
        if (module instanceof ConnectorModule) {
        	ConnectorModule connectorModule = (ConnectorModule) module;
        	final ClassLoader connectorClassLoader = connectorModule.getClassLoader();
        	return new AnnotationFinder(new ConfigurableClasspathArchive(connectorClassLoader, connectorModule.getLibraries())).link();
        }

        if (module.getJarLocation() != null) {
            String location = module.getJarLocation();
            File file = new File(location);

            URL url;
            if (file.exists()) {
                url = file.toURI().toURL();
                
                File webInfClassesFolder = new File(file, "WEB-INF/classes"); // is it possible?? normally no
				if (webInfClassesFolder.exists() && webInfClassesFolder.isDirectory()) {
                	url = webInfClassesFolder.toURI().toURL();
                }
            } else {
                url = new URL(location);
            }

            return new AnnotationFinder(new ConfigurableClasspathArchive(module.getClassLoader(), url)).link();
        } else {
            return new AnnotationFinder(new ClassesArchive()).link();
        }
    }
}
