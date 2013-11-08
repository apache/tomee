/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.ee;

import javax.transaction.TransactionManager;

import org.apache.openjpa.util.InternalException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * <p>Implementation of the {@link ManagedRuntime} interface that listens
 * for an OSGi service with interface <code>javax.transaction.TransactionManager</code>
 * to create a {@link TransactionManager} for controlling transactions.</p>
 * 
 * <p>The support for a transaction manager is usually activated inside an OSGi
 * {@link BundleActivator} implementation using {@link #registerServiceListener(BundleContext)}
 * and {@link #deregisterServiceListener(BundleContext)}.
 * </p>
 */
public class OSGiManagedRuntime extends AbstractManagedRuntime {

    /**
     * a static instance, which is managed by the ServiceListener added to the bundle
     * context by {@link #registerServiceListener(BundleContext)}.
     */
    private static TransactionManager transactionManager;
    private static ServiceReference serviceReference;
    private static ServiceListener listener;
    
    private static final class Listener implements ServiceListener {

        final BundleContext bundleContext;
        // avoid the garbage collection of the OSGiManagedRuntime class itself,
        // by holding a reference to it.
        final Class<OSGiManagedRuntime> clazzRef;

        public Listener(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
            this.clazzRef = OSGiManagedRuntime.class;
        }

        @Override
        public void serviceChanged(ServiceEvent event) {

            synchronized (this.clazzRef) {

                switch (event.getType()) {

                    case ServiceEvent.REGISTERED:
                        OSGiManagedRuntime.serviceReference = event.getServiceReference();
                        OSGiManagedRuntime.transactionManager =
                            (TransactionManager) this.bundleContext.getService(OSGiManagedRuntime.serviceReference);
                        break;

                    case ServiceEvent.UNREGISTERING:
                        OSGiManagedRuntime.transactionManager = null;
                        OSGiManagedRuntime.serviceReference = null;
                        this.bundleContext.ungetService(event.getServiceReference());
                        break;
                }
            }
        }
    }
    
    /**
     * <p>Register a service listener to the given bundle context by
     * {@link BundleContext#addServiceListener(ServiceListener,String)} with a filter 
     *  expression of <code>(objectClass=javax.transaction.TransactionManager)</code>.</p>
     *  
     *  <p>The caller is responsible for calling
     *  {@link #deregisterServiceListener(BundleContext)}, when
     *  the bundle context is being stopped.</p>
     *  
     * @param bundle The bundle, which is currently being started.
     * @throws InvalidSyntaxException When the filter expression is invalid.
     */
    public static synchronized void registerServiceListener(BundleContext bundleContext)
    throws InvalidSyntaxException {
        
        if (listener != null) {
            throw new InternalException("Another OSGi service listener has already been registered.");
        }
        
        listener = new Listener(bundleContext);
        
        bundleContext.addServiceListener(listener, "(" + Constants.OBJECTCLASS
            + "=javax.transaction.TransactionManager)");
        
        serviceReference = bundleContext.getServiceReference("javax.transaction.TransactionManager");
        
        if (serviceReference != null) {
            transactionManager = (TransactionManager)bundleContext.getService(serviceReference);
        }
    }
    
    /**
     * Remove a service listener previously started inside {@link #registerServiceListener(BundleContext)}.
     * 
     * @param bundleContext The bundle context to call
     *          {@link BundleContext#removeServiceListener(ServiceListener)} on.
     */
    public static synchronized void deregisterServiceListener(BundleContext bundleContext) {
       
        try {
            if (serviceReference != null) {
                bundleContext.ungetService(serviceReference);
                transactionManager = null;
                serviceReference = null;
            }
        }
        finally {
            // assure, that the service listener is removed, whatever happens above.
            if (listener != null) {
                bundleContext.removeServiceListener(listener);
                listener = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.openjpa.ee.ManagedRuntime#getTransactionManager()
     */
    @Override
    public TransactionManager getTransactionManager() throws Exception {
        synchronized (OSGiManagedRuntime.class) {
            
            if (transactionManager == null) {
                throw new InternalException("No javax.transaction.TransactionManager " +
                		"service is currently registered as an OSGi service.");
            }
            
            return transactionManager;
        }
    }

    /* (non-Javadoc)
     * 
     * @see org.apache.openjpa.ee.ManagedRuntime#setRollbackOnly(java.lang.Throwable)
     */
    @Override
    public void setRollbackOnly(Throwable cause) throws Exception {
        
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.apache.openjpa.ee.ManagedRuntime#getRollbackCause()
     */
    @Override
    public Throwable getRollbackCause() throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }

}
