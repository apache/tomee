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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.openjpa.util.InternalException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * Test javax.transaction.TransactionManager OSGi service discovery.
 */
public class TestOSGiManagedRuntime extends TestCase {

    private static final String TXN_MANAGER_CLASS_NAME = "javax.transaction.TransactionManager";
    
    /**
     * A transaction manager instance an nothing more.
     */
    private static final TransactionManager TXN_MANAGER = new TransactionManager() {

        public void begin() throws NotSupportedException, SystemException {
        }

        public void commit() throws HeuristicMixedException,
                HeuristicRollbackException, IllegalStateException,
                RollbackException, SecurityException, SystemException {
        }

        public int getStatus() throws SystemException {
            return 0;
        }

        public Transaction getTransaction() throws SystemException {
            return null;
        }

        public void resume(Transaction tobj) throws IllegalStateException,
                InvalidTransactionException, SystemException {
        }

        public void rollback() throws IllegalStateException, SecurityException,
                SystemException {
        }

        public void setRollbackOnly() throws IllegalStateException,
                SystemException {
        }

        public void setTransactionTimeout(int seconds) throws SystemException {
        }

        public Transaction suspend() throws SystemException {
            return null;
        }
        
        public String toString() {
            return TestOSGiManagedRuntime.class.getName()+"::TXN_MANAGER";
        }
    };
    
    /**
     * A service reference instance an nothing more.
     */
    private static final ServiceReference TXN_SVC_REFERENCE = new ServiceReference() {
        
        @Override
        public boolean isAssignableTo(Bundle bundle, String className) {
            return false;
        }
        
        @Override
        public Bundle[] getUsingBundles() {
            return null;
        }
        
        @Override
        public String[] getPropertyKeys() {
            return null;
        }
        
        @Override
        public Object getProperty(String key) {
            return null;
        }
        
        @Override
        public Bundle getBundle() {
            return null;
        }
        
        @Override
        public int compareTo(Object reference) {
            return reference == TXN_SVC_REFERENCE ? 0 : 1;
        }
        
        public String toString() {
            return TestOSGiManagedRuntime.class.getName()+"::TXN_SVC_REFERENCE";
        }
    };
    
    /**
     * A fake bundle context with a reference counter for a javax.transaction.TRansactionManager
     */
    private static final class TestBundleContext implements BundleContext {

        private List<ServiceListener> serviceListeners;
        private TransactionManager transactionManager;
        private int txnRefCount;
        
        public void setTransactionManager(TransactionManager transactionManager) {
            
            if (transactionManager == null) {
            
                if (this.serviceListeners != null) {
                    for (ServiceListener listener :this.serviceListeners) {
                        listener.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING,TXN_SVC_REFERENCE));
                    }
                }
            }

            // test for properly calling ungetReference().
            assertEquals(0,this.txnRefCount);
            this.transactionManager = transactionManager;
            
            if (transactionManager != null) {
                
                if (this.serviceListeners != null) {
                    for (ServiceListener listener :this.serviceListeners) {
                        listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,TXN_SVC_REFERENCE));
                    }
                }
            }
        }
        
        public int getServiceListenerCount() {
            
            if (this.serviceListeners == null) {
                return 0;
            }
            else {
                return this.serviceListeners.size();
            }
        }
        
        public int getTxnRefCount() {
            return this.txnRefCount;
        }
        
        public String getProperty(String key) {
            throw new UnsupportedOperationException();
        }

        public Bundle getBundle() {
            throw new UnsupportedOperationException();
        }

        public Bundle installBundle(String location, InputStream input)
                throws BundleException {
            throw new UnsupportedOperationException();
        }

        public Bundle installBundle(String location) throws BundleException {
            throw new UnsupportedOperationException();
        }

        public Bundle getBundle(long id) {
            throw new UnsupportedOperationException();
        }

        public Bundle[] getBundles() {
            throw new UnsupportedOperationException();
        }

        public void addServiceListener(ServiceListener listener, String filter)
                throws InvalidSyntaxException {
            
            assertEquals("(objectClass="+TXN_MANAGER_CLASS_NAME+")",filter);
            
            if (this.serviceListeners == null)
                this.serviceListeners = new ArrayList<ServiceListener>();
            
            this.serviceListeners.add(listener);
        }

        public void addServiceListener(ServiceListener listener) {
            
            throw new AssertionFailedError("service listener must be added using an objectClass filter.");
        }

        public void removeServiceListener(ServiceListener listener) {
            
            if (this.serviceListeners == null || ! this.serviceListeners.remove(listener)) {
                throw new AssertionFailedError("Try to remove service listener, which has not been added before.");
            }
            
            assertEquals(0,this.txnRefCount);
        }

        public void addBundleListener(BundleListener listener) {
            throw new UnsupportedOperationException();
        }

        public void removeBundleListener(BundleListener listener) {
            throw new UnsupportedOperationException();
        }

        public void addFrameworkListener(FrameworkListener listener) {
            throw new UnsupportedOperationException();
        }

        public void removeFrameworkListener(FrameworkListener listener) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("rawtypes")
        public ServiceRegistration registerService(String[] clazzes,
                Object service, Dictionary properties) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("rawtypes")
        public ServiceRegistration registerService(String clazz,
                Object service, Dictionary properties) {
            throw new UnsupportedOperationException();
        }

        public ServiceReference[] getServiceReferences(String clazz,
                String filter) throws InvalidSyntaxException {
            throw new UnsupportedOperationException();
        }

        public ServiceReference[] getAllServiceReferences(String clazz,
                String filter) throws InvalidSyntaxException {
            throw new UnsupportedOperationException();
        }

        public ServiceReference getServiceReference(String clazz) {
            
            assertEquals(TXN_MANAGER_CLASS_NAME,clazz);
            
            if (this.transactionManager == null) {
                return null;
            }
            
            assertEquals(TXN_MANAGER_CLASS_NAME,clazz);
            return TXN_SVC_REFERENCE;
        }

        @Override
        public Object getService(ServiceReference reference) {
            
            assertSame(TXN_SVC_REFERENCE,reference);
            ++this.txnRefCount;
            return this.transactionManager;
        }

        @Override
        public boolean ungetService(ServiceReference reference) {
            
            assertSame(TXN_SVC_REFERENCE,reference);
            --this.txnRefCount;
            return true;
        }

        public File getDataFile(String filename) {
            throw new UnsupportedOperationException();
        }

        public Filter createFilter(String filter) throws InvalidSyntaxException {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static void assertTxnManagerAvailable(TestBundleContext context, ManagedRuntime mr) throws Exception {
        
        assertSame(TXN_MANAGER,mr.getTransactionManager());
        assertEquals(1,context.getTxnRefCount());
    }
    
    private static void assertTxnManagerUnavailable(TestBundleContext context, ManagedRuntime mr) throws Exception {
        
        InternalException ie = null;
        
        try {
            mr.getTransactionManager();
        } catch (InternalException e) {
            ie = e;
        }
        assertNotNull(ie);
        assertEquals(0,context.getTxnRefCount());
    }
    
    /**
     * Test the discovery, when transaction manager is available before starting
     * OSGiManagedRuntime and disappears after stopping the managed runtime.
     * 
     * @throws Throwable
     */
    public void testTxnServiceDiscoveryPreStartPostStop() throws Throwable {
        
        TestBundleContext context = new TestBundleContext();
        context.setTransactionManager(TXN_MANAGER);
        
        OSGiManagedRuntime.registerServiceListener(context);

        ManagedRuntime mr = new OSGiManagedRuntime();
        
        try {
        
            assertEquals(1,context.getServiceListenerCount());
           
            assertTxnManagerAvailable(context, mr);
        
            context.setTransactionManager(null);
            
            assertTxnManagerUnavailable(context, mr);

            OSGiManagedRuntime.deregisterServiceListener(context);
         }
        catch(Throwable e) {
            // this is her in order to make test repeatable in one JVM, because
            // OSGiManagerRuntime has static properties.
            OSGiManagedRuntime.deregisterServiceListener(context);
            throw e;
        }
        assertEquals(0,context.getServiceListenerCount());
            
        assertTxnManagerUnavailable(context, mr);
    }
    
    /**
     * Test the discovery, when transaction manager is available before starting
     * OSGiManagedRuntime and disappears before stopping the managed runtime.
     * 
     * @throws Throwable
     */
    public void testTxnServiceDiscoveryPreStartPreStop() throws Throwable {
        
        TestBundleContext context = new TestBundleContext();
        context.setTransactionManager(TXN_MANAGER);
        
        OSGiManagedRuntime.registerServiceListener(context);

        ManagedRuntime mr = new OSGiManagedRuntime();
        
        try {
        
            assertEquals(1,context.getServiceListenerCount());
           
            assertTxnManagerAvailable(context, mr);
        
            OSGiManagedRuntime.deregisterServiceListener(context);
        }
        catch(Throwable e) {
            // this is her in order to make test repeatable in one JVM, because
            // OSGiManagerRuntime has static properties.
            OSGiManagedRuntime.deregisterServiceListener(context);
            throw e;
        }
        assertEquals(0,context.getServiceListenerCount());
            
        assertTxnManagerUnavailable(context, mr);
    }
    
    /**
     * Test the discovery, when transaction manager becomes available after starting
     * OSGiManagedRuntime and disappears after stopping the managed runtime.
     * 
     * @throws Throwable
     */
    public void testTxnServiceDiscoveryPostStartPostStop() throws Throwable {
        
        TestBundleContext context = new TestBundleContext();
        
        OSGiManagedRuntime.registerServiceListener(context);
        ManagedRuntime mr = new OSGiManagedRuntime();
                
        try{ 
            assertEquals(1,context.getServiceListenerCount());
        
            assertTxnManagerUnavailable(context, mr);
            
            context.setTransactionManager(TXN_MANAGER);
            
            assertTxnManagerAvailable(context, mr);
            
            OSGiManagedRuntime.deregisterServiceListener(context);
        }
        catch(Throwable e) {
            // this is her in order to make test repeatable in one JVM, because
            // OSGiManagerRuntime has static proeprties.
            OSGiManagedRuntime.deregisterServiceListener(context);
            throw e;
        }
        assertEquals(0,context.getServiceListenerCount());
        
        assertTxnManagerUnavailable(context, mr);
    }
    
    /**
     * Test the discovery, when transaction manager becomes available after starting
     * OSGiManagedRuntime and disappears before stopping the managed runtime.
     * 
     * @throws Throwable
     */
    public void testTxnServiceDiscoveryPostStartPreStop() throws Throwable {
        
        TestBundleContext context = new TestBundleContext();
        
        OSGiManagedRuntime.registerServiceListener(context);
        ManagedRuntime mr = new OSGiManagedRuntime();
        
        try {
            assertEquals(1,context.getServiceListenerCount());
        
            assertTxnManagerUnavailable(context, mr);
        
            context.setTransactionManager(TXN_MANAGER);

            assertTxnManagerAvailable(context, mr);
        
            context.setTransactionManager(null);
            
            assertTxnManagerUnavailable(context, mr);
            
            OSGiManagedRuntime.deregisterServiceListener(context);
        }
        catch(Throwable e) {
            // this is her in order to make test repeatable in one JVM, because
            // OSGiManagerRuntime has static proeprties.
            OSGiManagedRuntime.deregisterServiceListener(context);
            throw e;
        }
        assertEquals(0,context.getServiceListenerCount());
            
        assertTxnManagerUnavailable(context, mr);
    }
}
