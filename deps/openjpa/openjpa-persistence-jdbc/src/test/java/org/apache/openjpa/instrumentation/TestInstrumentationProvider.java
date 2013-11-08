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
package org.apache.openjpa.instrumentation;

import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.instrumentation.Instrument;
import org.apache.openjpa.lib.instrumentation.InstrumentationProvider;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Verifies the configuration and usage of a simple instrumentation
 * provider.
 * @author jrbauer
 *
 */
public class TestInstrumentationProvider extends SingleEMFTestCase {

    public static final String SINGLE_PROVIDER = 
        "org.apache.openjpa.instrumentation.SimpleProvider(Instrument='DataCache,QueryCache,QuerySQLCache')";

    public static final String MULTI_PROVIDER = 
        "org.apache.openjpa.instrumentation.SimpleProvider(Instrument='DataCache,QueryCache,QuerySQLCache'), " +
        "org.apache.openjpa.instrumentation.SecondProvider(Instrument='DataCache,QuerySQLCache')";

    public static final String DC_PROVIDER = 
        "org.apache.openjpa.instrumentation.SimpleProvider(Instrument='DataCache')";

    public void setUp() throws Exception {
        super.setUp("openjpa.Instrumentation",
                    SINGLE_PROVIDER,
                    "openjpa.DataCache",
                    "true(EnableStatistics=true)",
                    "openjpa.QueryCache",
                    "true",
                    "openjpa.RemoteCommitProvider",
                    "sjvm");
    }

    /**
     * Verifies simple instrumentation provider config with instruments defined
     * for data cache and query cache.
     */
    public void testProviderConfig() {
        // Verify an EMF was created with the supplied instrumentation
        assertNotNull(emf);

        // Verify the instrumentation value was stored in the config
        String instrValue = emf.getConfiguration().getInstrumentation();
        assertEquals(instrValue, SINGLE_PROVIDER);

        // Verify an instrumentation manager is available
        InstrumentationManager mgr = emf.getConfiguration().getInstrumentationManagerInstance();
        assertNotNull(mgr);
        
        // Verify the manager is managing the correct provider
        Set<InstrumentationProvider> providers = mgr.getProviders();
        assertNotNull(providers);
        assertEquals(1, providers.size());
        InstrumentationProvider provider = providers.iterator().next();
        assertEquals(provider.getClass(), SimpleProvider.class);
        
        // Verify the provider has instruments registered for the caches
        Set<Instrument> instruments = provider.getInstruments();
        assertNotNull(instruments);
        assertEquals(3,instruments.size());
        
        // Lightweight verification of the instruments
        Instrument inst = provider.getInstrumentByName(DCInstrument.NAME);
        assertNotNull(inst);
        assertTrue(inst instanceof DataCacheInstrument);
        DataCacheInstrument dci = (DataCacheInstrument)inst;
        assertEquals(dci.getCacheName(), "default");
        inst = provider.getInstrumentByName(QCInstrument.NAME);
        assertNotNull(inst);
        assertTrue(inst instanceof QueryCacheInstrument);
        inst = provider.getInstrumentByName(QSCInstrument.NAME);
        assertNotNull(inst);
        assertTrue(inst instanceof PreparedQueryCacheInstrument);
    }
    
    /**
     * Verifies configuring and adding an instrument to a provider after the provider
     * has been initialized within the persistence unit. 
     */
    public void testDynamicInstrumentConfig() {
        InstrumentationManager mgr = emf.getConfiguration().getInstrumentationManagerInstance();
        assertNotNull(mgr);

        Set<InstrumentationProvider> providers = mgr.getProviders();
        assertNotNull(providers);
        assertEquals(1, providers.size());
        InstrumentationProvider provider = providers.iterator().next();
        assertEquals(provider.getClass(), SimpleProvider.class);

        verifyBrokerLevelInstrument(provider);
    }

    /**
     * Verifies configuring and adding an instrumentation provider dynamically after
     * the persistence unit has been created.
     */
    public void testDynamicProviderConfig() {
        InstrumentationManager mgr = emf.getConfiguration().getInstrumentationManagerInstance();
        assertNotNull(mgr);

        DynamicProvider dyp = new DynamicProvider();
        mgr.manageProvider(dyp);
        verifyBrokerLevelInstrument(dyp);
        assertTrue(dyp.isStarted());
        assertNotNull(dyp.getInstrumentByName(BrokerLevelInstrument.NAME));
        assertEquals(2, mgr.getProviders().size());
    }

    public void verifyBrokerLevelInstrument(InstrumentationProvider provider) {
        // Create a new broker level instrument and register it with the
        // provider
        BrokerLevelInstrument bli = new BrokerLevelInstrument();
        provider.addInstrument(bli);
        // Verify instrument has not been initialized or started 
        assertFalse(bli.isInitialized());
        assertFalse(bli.isStarted());
        
        // Create a new EM/Broker
        EntityManager em = emf.createEntityManager();
        // Vfy the instrument has been initialized and started
        assertTrue(bli.isInitialized());
        assertTrue(bli.isStarted());
        // Close the EM/Broker
        em.close();
        // Vfy the instrument has stopped
        assertFalse(bli.isStarted());
    }
    
    /**
     * Verifies the data cache metrics are available through simple instrumentation.
     */
    public void testDataCacheInstrument() {
        OpenJPAEntityManagerFactorySPI oemf = createEMF(
            "openjpa.Instrumentation", DC_PROVIDER,
            "openjpa.DataCache", "true(EnableStatistics=true)",
            "openjpa.RemoteCommitProvider", "sjvm",
            "openjpa.jdbc.SynchronizeMappings", "buildSchema",
            CacheableEntity.class);

        // Verify an EMF was created with the supplied instrumentation
        assertNotNull(oemf);

        // Verify the instrumentation value was stored in the config
        String instrValue = oemf.getConfiguration().getInstrumentation();
        assertEquals(DC_PROVIDER, instrValue);

        // Verify an instrumentation manager is available
        InstrumentationManager mgr = oemf.getConfiguration().getInstrumentationManagerInstance();
        assertNotNull(mgr);
        
        // Get the data cache instrument
        Set<InstrumentationProvider> providers = mgr.getProviders();
        assertNotNull(providers);
        assertEquals(1, providers.size());
        InstrumentationProvider provider = providers.iterator().next();
        assertEquals(provider.getClass(), SimpleProvider.class);
        Instrument inst = provider.getInstrumentByName(DCInstrument.NAME);
        assertNotNull(inst);
        assertTrue(inst instanceof DataCacheInstrument);
        DataCacheInstrument dci = (DataCacheInstrument)inst;
        assertEquals(dci.getCacheName(), "default");
        
        OpenJPAEntityManagerSPI oem = oemf.createEntityManager();
        
        CacheableEntity ce = new CacheableEntity();
        int id = new Random().nextInt();
        ce.setId(id);
        
        oem.getTransaction().begin();
        oem.persist(ce);
        oem.getTransaction().commit();
        oem.clear();
        assertTrue(oemf.getCache().contains(CacheableEntity.class, id));
        ce = oem.find(CacheableEntity.class, id);
        
        assertTrue(dci.getHitCount() > 0);
        assertTrue(dci.getReadCount() > 0);
        assertTrue(dci.getWriteCount() > 0);

        assertTrue(dci.getHitCount(CacheableEntity.class.getName()) > 0);
        assertTrue(dci.getReadCount(CacheableEntity.class.getName()) > 0);
        assertTrue(dci.getWriteCount(CacheableEntity.class.getName()) > 0);

        
        closeEMF(oemf);
    }

    /**
     * Verifies multiple instrumentation providers can be specified.
     */
    public void testMultipleProviderConfig() {
        OpenJPAEntityManagerFactorySPI oemf = createEMF(
            "openjpa.Instrumentation", 
            MULTI_PROVIDER,
            "openjpa.DataCache",
            "true(EnableStatistics=true)",
            "openjpa.QueryCache",
            "true",
            "openjpa.RemoteCommitProvider",
            "sjvm");
        
        // Verify an EMF was created with the supplied instrumentation
        assertNotNull(oemf);

        // Verify the instrumentation value was stored in the config
        String instrValue = oemf.getConfiguration().getInstrumentation();
        assertEquals(MULTI_PROVIDER, instrValue);

        // Verify an instrumentation manager is available
        InstrumentationManager mgr = oemf.getConfiguration().getInstrumentationManagerInstance();
        assertNotNull(mgr);
        
        // Verify the manager is managing the correct provider
        Set<InstrumentationProvider> providers = mgr.getProviders();
        assertNotNull(providers);
        assertEquals(2, providers.size());
        for (InstrumentationProvider provider : providers) {
            assertTrue( provider instanceof SimpleProvider ||
                        provider instanceof SecondProvider);
            if (provider instanceof SimpleProvider) {
                assertEquals(3, provider.getInstruments().size());
            } else {
                assertEquals(2, provider.getInstruments().size());
            }
        }
        closeEMF(oemf);
    }
}
