/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.persistence.osgi;

import java.util.Hashtable;

import javax.persistence.spi.PersistenceProvider;

import org.apache.openjpa.ee.OSGiManagedRuntime;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * Used to discover/resolve JPA providers in an OSGi environment.
 *
 * @version $Rev$ $Date$
 */
public class PersistenceActivator implements BundleActivator {
    // following is so Aries can find and extend us for OSGi RFC 143
    public static final String PERSISTENCE_PROVIDER_ARIES = "javax.persistence.provider";
    // following would be set by Aries to expose their OSGi enabled provider
    public static final String PERSISTENCE_PROVIDER = PersistenceProvider.class.getName();
    public static final String OSGI_PERSISTENCE_PROVIDER = PersistenceProviderImpl.class.getName();
    private static BundleContext ctx = null;
    private static ServiceRegistration svcReg = null;

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext ctx) throws Exception {
        this.ctx = ctx;
        PersistenceProvider provider = new PersistenceProviderImpl();
        Hashtable<String, String> props = new Hashtable<String, String>();
        // Aries queries for service providers by property "javax.persistence.provider"
        props.put(PERSISTENCE_PROVIDER_ARIES, OSGI_PERSISTENCE_PROVIDER);
        // The persistence service tracker in the geronimo spec api bundle examines
        // the property named "javax.persistence.PersistenceProvider" rather than
        // the the property provided for Aries.  In order to properly track the OpenJPA 
        // provider, this property must be set upon service registration.
        props.put(PERSISTENCE_PROVIDER, OSGI_PERSISTENCE_PROVIDER);
        svcReg = ctx.registerService(PERSISTENCE_PROVIDER, provider, props);
        
        OSGiManagedRuntime.registerServiceListener(ctx);
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext ctx) throws Exception {
        
        OSGiManagedRuntime.deregisterServiceListener(ctx);
        
        if (svcReg != null) {
            svcReg.unregister();
            svcReg = null;
        }
        this.ctx = null;
    }

    /* (non-Javadoc)
     * OPENJPA-1491 Allow us to use the OSGi Bundle's ClassLoader instead of the application one.
     * This class and method are dynamically loaded by BundleUtils, so any method signature changes
     * here need to also be reflected in BundleUtils.getBundleClassLoader()
     */
    public static ClassLoader getBundleClassLoader() {
        ClassLoader cl = null;
        if (ctx != null) {
            Bundle b = ctx.getBundle();
            cl = new BundleDelegatingClassLoader(b);
        }
        return cl;
    }

}
