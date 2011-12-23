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
package org.apache.openejb.core.osgi.impl;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.BundleAnnotationFinder;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.apache.xbean.osgi.bundle.util.ResourceDiscoveryFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
public class BundleFinderFactory extends FinderFactory {
    private static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final String WEB_INF_BEANS_XML = "WEB-INF/beans.xml";

    @Override
    public IAnnotationFinder create(DeploymentModule module) throws Exception {
        
        ClassLoader moduleCL = module.getClassLoader();
        
        while (!(moduleCL instanceof BundleReference)) {

            moduleCL = moduleCL.getParent();
            
            if (moduleCL == null)
                break;

        }
        
        if (moduleCL != null && moduleCL instanceof BundleReference) {
            Bundle bundle = ((BundleReference) moduleCL).getBundle();
            BundleContext bundleContext = bundle.getBundleContext();
            ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(sr);
            final String location = module.getModuleUri().toString();
            final boolean isWAR = location.endsWith(".war");
            boolean useLocation = location != null
                    && !location.isEmpty()
                    && !module.isStandaloneModule();
            Set<String> beanArchiveJarNames = findBeansXml(bundle, packageAdmin, useLocation? location : "");

            BundleAnnotationFinder bundleAnnotationFinder;
            if (useLocation) {
                ResourceDiscoveryFilter filter = new ResourceDiscoveryFilter() {

                    @Override
                    public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                        return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH || discoveryRange == DiscoveryRange.FRAGMENT_BUNDLES;
                    }

                    @Override
                    public boolean zipFileDiscoveryRequired(String s) {
                        return isWAR ? s.startsWith(location) : s.equals(location);
                    }

                    @Override
                    public boolean directoryDiscoveryRequired(String s) {
                        return isWAR ? s.startsWith(location) : s.equals(location);
                    }
                };

                bundleAnnotationFinder = new BundleAnnotationFinder(packageAdmin, bundle, filter, beanArchiveJarNames);
            } else {
                ResourceDiscoveryFilter filter = new ResourceDiscoveryFilter() {

                    @Override
                    public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                        return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH || discoveryRange == DiscoveryRange.FRAGMENT_BUNDLES;
                    }

                    @Override
                    public boolean zipFileDiscoveryRequired(String s) {
                        return true;
                    }

                    @Override
                    public boolean directoryDiscoveryRequired(String s) {
                        return true;
                    }
                };

                bundleAnnotationFinder = new BundleAnnotationFinder(packageAdmin, bundle, filter, beanArchiveJarNames);
            }
            bundleAnnotationFinder.link();
            return bundleAnnotationFinder;
        }

        throw new IllegalStateException("Module classloader is not a BundleReference. Only use BundleFactoryFinder in an pure osgi environment");
    }

    //TODO consider passing in location?
    private Set<String> findBeansXml(Bundle mainBundle, PackageAdmin packageAdmin, String location)
            throws Exception
    {
        final Set<String> beanArchiveJarNames = new HashSet<String>();
        BundleResourceFinder brfXmlJar =  new BundleResourceFinder(packageAdmin, mainBundle, "", META_INF_BEANS_XML);

        BundleResourceFinder.ResourceFinderCallback rfCallback = new BundleResourceFinder.ResourceFinderCallback()
        {

            public boolean foundInDirectory(Bundle bundle, String basePath, URL url) throws Exception
            {
//                logger.info("adding the following beans.xml URL: " + url);
                beanArchiveJarNames.add(basePath);
                return true;
            }

            public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream in) throws Exception
            {
                URL jarURL = bundle.getEntry(jarName);
//                URL beansUrl = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());

//                logger.info("adding the following beans.xml URL: " + beansUrl);

                beanArchiveJarNames.add(jarName);
                return true;
            }

        };

        brfXmlJar.find(rfCallback);

// TODO I found no other way to find WEB-INF/beanx.xml directly
        Enumeration<URL> urls = mainBundle.findEntries(location + "/WEB-INF", "beans.xml", true);
        boolean webBeansXmlFound = false;
        while (urls != null && urls.hasMoreElements()) {
            URL webBeansXml = urls.nextElement();
            String webBeansXMlString = webBeansXml.toExternalForm();
            if (!webBeansXMlString.endsWith("/" + WEB_INF_BEANS_XML)) {
                continue;
            }

            if (webBeansXmlFound) {
                throw new IllegalStateException("found more than WEB-INF/beans.xml file!" + webBeansXml);
            }

            //            logger.info("adding the following WEB-INF/beans.xml URL: " + webBeansXml);
            beanArchiveJarNames.add(location + (location.isEmpty()? "": "/") + "WEB-INF/classes/");
            webBeansXmlFound = true;

        }
        return beanArchiveJarNames;
    }

}
