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
package org.apache.openejb.config;

import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.BundleAnnotationFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.File;
import java.net.URL;

import static org.apache.openejb.util.Join.join;

/**
 * @version $Rev$ $Date$
 */
public class BundleAnnotationDeployer extends AbstractAnnotationDeployer implements DynamicDeployer {

    public BundleAnnotationDeployer() {
        super(new DiscoverAnnotatedBeans(getPackageAdmin()));
    }

    private static PackageAdmin getPackageAdmin() {
        ClassLoader cl = BundleAnnotationDeployer.class.getClassLoader();
        Bundle bundle = getBundle(cl);
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
        return (PackageAdmin) bundleContext.getService(sr);
    }

    private static Bundle getBundle(ClassLoader classLoader) {
        return ((BundleReference) classLoader).getBundle();
    }


    public static class DiscoverAnnotatedBeans extends AbstractDiscoverAnnotatedBeans {

        public DiscoverAnnotatedBeans(PackageAdmin packageAdmin) {
            this.packageAdmin = packageAdmin;
        }

        private final PackageAdmin packageAdmin;

        @Override
        protected AbstractFinder newFinder(ClientModule clientModule) throws Exception {
            AbstractFinder finder;
            finder = new BundleAnnotationFinder(packageAdmin, getBundle(clientModule.getClassLoader()));
            return finder;
        }

        @Override
        protected AbstractFinder newFinder(WebModule webModule) throws Exception {
            AbstractFinder finder;
            File file = new File(webModule.getJarLocation());
            URL[] urls = DeploymentLoader.getWebappUrls(file);
            final ClassLoader webClassLoader = webModule.getClassLoader();
            finder = new BundleAnnotationFinder(packageAdmin, getBundle(webClassLoader));
            return finder;
        }

        @Override
        protected AbstractFinder newFinder(EjbModule ejbModule) throws Exception {
            AbstractFinder finder;
            finder = new BundleAnnotationFinder(packageAdmin, getBundle(ejbModule.getClassLoader()));
            return finder;
        }
    }

}