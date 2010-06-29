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
package org.apache.openejb.osgi.core;

import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.FinderFactory;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.BundleAnnotationFinder;
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

    @Override
    public AbstractFinder create(DeploymentModule module) throws Exception {
        ClassLoader moduleCL = module.getClassLoader();

        if (moduleCL instanceof BundleReference) {
            Bundle bundle = ((BundleReference) moduleCL).getBundle();
            BundleContext bundleContext = bundle.getBundleContext();
            ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
            PackageAdmin packageAdmin = (PackageAdmin) bundleContext.getService(sr);

            final String location = module.getModuleId();
            if (location != null && !location.isEmpty()) {
                ResourceDiscoveryFilter filter = new ResourceDiscoveryFilter() {

                    @Override
                    public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                        return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH || discoveryRange == DiscoveryRange.FRAGMENT_BUNDLES;
                    }

                    @Override
                    public boolean zipFileDiscoveryRequired(String s) {
                        return s.equals(location);
                    }

                    @Override
                    public boolean directoryDiscoveryRequired(String s) {
                        return s.equals(location);
                    }
                };

                return new BundleAnnotationFinder(packageAdmin, bundle, filter);
            } else {
                return new BundleAnnotationFinder(packageAdmin, bundle);
            }
        }

        throw new IllegalStateException("Module classloader is not a BundleReference. Only use BundleFactoryFinder in an pure osgi environment");
    }

}
