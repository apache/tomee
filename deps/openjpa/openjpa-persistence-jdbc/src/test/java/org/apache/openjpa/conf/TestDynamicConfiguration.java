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
package org.apache.openjpa.conf;

import javax.persistence.EntityManager;

import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.datacache.common.apps.PObject;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests dynamic modification of configuration property.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestDynamicConfiguration extends SingleEMFTestCase {

	public void setUp() throws Exception {
		super.setUp(PObject.class);
	}
	
    public void testConfigurationIsEqualByValueAndHashCode() {
		OpenJPAEntityManagerFactorySPI emf1 = createEMF(FRESH_EMF);
		assertNotNull(emf1);
		OpenJPAConfiguration conf1 = emf1.getConfiguration();
		
		OpenJPAEntityManagerFactorySPI emf2 = createEMF(FRESH_EMF);
		assertNotNull(emf2);
		OpenJPAConfiguration conf2 = emf2.getConfiguration();
		
		try {
	        assertFalse(emf1==emf2);
	        assertFalse(emf1.equals(emf2));
	        assertFalse(conf1==conf2);
	        assertEquals(conf1, conf2);
	        assertEquals(conf1.hashCode(), conf2.hashCode());
	        assertEquals(conf1.toProperties(false), conf2.toProperties(false));
		} finally {
		    clear(emf1);
		    closeEMF(emf1);
            clear(emf2);
            closeEMF(emf2);
		}
	}
	
	public void testConfigurationIsReadOnlyAfterFirstConstruction() {
		OpenJPAConfiguration conf = emf.getConfiguration();
		assertFalse(conf.isReadOnly());
		emf.createEntityManager();
		assertTrue(conf.isReadOnly());
	}
	
	public void testNonDynamicValuesCanNotBeChanged() {
		emf.createEntityManager();
		OpenJPAConfiguration conf = emf.getConfiguration();
		
		String oldValue = conf.getConnectionURL();
		String newValue = "jdbc://mydb:8087/DBDoesNotExist";
		try {
			conf.setConnectionURL(newValue);
			fail("Expected exception to modify configuration");
		} catch (Exception ex) { // good
			assertEquals(oldValue, conf.getConnectionURL());
		}
	}
	
	public void testDynamicValuesCanBeChanged() {
		OpenJPAConfiguration conf = emf.getConfiguration();
		
		int oldValue = conf.getLockTimeout();
		int newValue = oldValue + 10;
		
		conf.setLockTimeout(newValue);
		assertEquals(newValue, conf.getLockTimeout());
	}

	public void testDynamicValuesAreCorrectlySet() {
		OpenJPAConfiguration conf = emf.getConfiguration();
		
		Value lockTimeoutValue = conf.getValue("LockTimeout");
		assertNotNull(lockTimeoutValue);
		assertTrue(lockTimeoutValue.isDynamic());
		
		Value connectionURLValue = conf.getValue("ConnectionURL");
		assertNotNull(connectionURLValue);
		assertFalse(connectionURLValue.isDynamic());
	}
	
	public void testDynamicChangeDoesNotChangeHashCode() {
		OpenJPAConfiguration conf1 = emf.getConfiguration();
		
		int oldValue = conf1.getLockTimeout();
		int newValue = oldValue+10;
		int oldHash = conf1.hashCode();
		conf1.setLockTimeout(newValue);
		int newHash = conf1.hashCode();
		
		assertEquals(oldHash, newHash);
	}
	
	public void testClassMetaDataRecognizesDataCacheTimeoutValueChange() {
		OpenJPAConfiguration conf = emf.getConfiguration();
		
		// ensure that PObject is in metadata repository
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		PObject pc = new PObject();
		em.persist(pc);
		
		int oldValue = conf.getDataCacheTimeout();
		
		ClassMetaData meta = conf.getMetaDataRepositoryInstance()
			.getCachedMetaData(PObject.class);
		assertNotNull(meta);
		assertEquals(oldValue, meta.getDataCacheTimeout());
		
		int newValue = oldValue + 10;
		conf.setDataCacheTimeout(newValue);
		assertEquals(newValue, conf.getDataCacheTimeout());
		assertEquals(newValue, meta.getDataCacheTimeout());
		
	}
}
