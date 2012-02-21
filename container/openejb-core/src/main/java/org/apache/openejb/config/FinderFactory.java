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

import org.apache.openejb.xbean.xml.XMLAnnotationFinderHelper;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class FinderFactory {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB, FinderFactory.class);

    private static final String SCAN_XML = "META-INF/org/apache/xbean/scan.xml";
    private static final String WEB_SCAN_XML = SCAN_XML.replace("META-INF", "WEB-INF");

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
            final IAnnotationFinder finder = xmlFinder(module, inputStream(webModule.getFile(), WEB_SCAN_XML), webModule.getScannableUrls(), AggregatedArchive.class);
            if (finder != null) {
                return finder;
            }
            return new AnnotationFinder(new AggregatedArchive(webClassLoader, webModule.getScannableUrls())).link();
        }
        
        if (module instanceof ConnectorModule) {
        	ConnectorModule connectorModule = (ConnectorModule) module;
        	final ClassLoader connectorClassLoader = connectorModule.getClassLoader();
            final IAnnotationFinder finder = xmlFinder(module, connectorClassLoader.getResourceAsStream(SCAN_XML), connectorModule.getLibraries());
            if (finder != null) {
                return finder;
            }
        	return new AnnotationFinder(new ClasspathArchive(connectorClassLoader, connectorModule.getLibraries())).link();
        }

        if (module.getJarLocation() != null) {
            String location = module.getJarLocation();
            File file = new File(location);

            URL url;
            if (file.exists()) {
                url = file.toURI().toURL();
                
                File webInfClassesFolder = new File(file, "WEB-INF/classes");
				if (webInfClassesFolder.exists() && webInfClassesFolder.isDirectory()) {
                	url = webInfClassesFolder.toURI().toURL();
                }
                if (webInfClassesFolder.getParentFile().exists()) {
                    final FileInputStream fis = inputStream(webInfClassesFolder.getParentFile().getParentFile(), WEB_SCAN_XML);
                    final IAnnotationFinder finder = xmlFinder(module, fis, Arrays.asList(url));
                    if (finder != null) {
                        return finder;
                    }
                }
            } else {
                url = new URL(location);
            }

            final IAnnotationFinder finder = xmlFinder(module, module.getClassLoader().getResourceAsStream(SCAN_XML), Arrays.asList(url));
            if (finder != null) {
                return finder;
            }

            return new AnnotationFinder(new ClasspathArchive(module.getClassLoader(), url)).link();
        } else {
            return new AnnotationFinder(new ClassesArchive()).link();
        }
    }

    private static IAnnotationFinder xmlFinder(final DeploymentModule module, final InputStream scanIs, final Iterable<URL> urls, final Class<?> clazz) {
        if (scanIs != null) {
            try {
                final IAnnotationFinder finder = XMLAnnotationFinderHelper.finderFromXml(scanIs, module.getClassLoader(), urls, clazz);
                logger.info("using scan.xml for module " + module.getModuleId());
                return finder;
            } catch (JAXBException jaxbEx) {
                logger.warning("can't use scan.xml for " + module.getModuleId());
            }
        }
        return null;
    }

    private static FileInputStream inputStream(final File file, final String xml) throws FileNotFoundException {
        final File scanFile = new File(file, xml);
        if (scanFile.exists()) {
            return new FileInputStream(scanFile);
        }
        return null;
    }

}
