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
package org.apache.openjpa.persistence.annotations.xml;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.strats.FlatClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests metadata-complete tag switches off any annotation processing.
 * 
 * EntityA uses metadata-complete tag and hence all its annotated mapping info
 * must not be processed.
 * 
 * EntityB does not use metadata-complete tag and hence its mapping info should
 * be combination of annotation mapping info overwritten by xml description
 * mapping info.
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestMetaDataComplete extends SingleEMFTestCase {
	private static OpenJPAEntityManagerFactorySPI oemf;
	private static ClassMetaData entityA, entityB, derivedA, derivedB;
	private static int currentTest = 0;

	public void setUp() throws Exception {
        currentTest++;
		if (oemf == null) {
            super.setUp(EntityA.class, EntityB.class, DerivedA.class,
					DerivedB.class);
            oemf = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence
					.cast(emf);
			MetaDataRepository repos = oemf.getConfiguration()
					.getMetaDataRepositoryInstance();

			entityA = repos.getMetaData(EntityA.class, null, true);
			entityB = repos.getMetaData(EntityB.class, null, true);
            derivedA = repos.getMetaData(DerivedA.class, null, true);
            derivedB = repos.getMetaData(DerivedB.class, null, true);
		}
	}

	public void tearDown() throws Exception {
	    // only cleanup after the last test has run
	    if (currentTest >= 6) {
	        closeEMF(oemf);
            oemf = null;
	        super.tearDown();
            entityA = null;
            entityB = null;
            derivedA = null;
            derivedB = null;
	    }
	}

	protected String getPersistenceUnitName() {
		return "test-metadata-complete";
	}

	public void testIgnoresClassAnnotationIfMetaDataComplete() {
		// inheritance strategy of EntityA by annotation is SINGLE_TABLE
        // inheritance strategy of EntityA in xml descriptor is JOINED
		assertEquals(FullClassStrategy.class, ((ClassMapping) entityA)
				.getStrategy().getClass());
        assertEquals(VerticalClassStrategy.class, ((ClassMapping) derivedA)
				.getStrategy().getClass());
	}

	public void testProcessesClassAnnotationIfMetaDataIsNotComplete() {
		// inheritance strategy of EntityB by annotation is SINGLE_TABLE
        // inheritance strategy of EntityB in xml descriptor is not specified
		assertEquals(FullClassStrategy.class, ((ClassMapping) entityB)
				.getStrategy().getClass());
		assertEquals(FlatClassStrategy.class, ((ClassMapping) derivedB)
				.getStrategy().getClass());
	}

	public void testIgnoresFieldAnnotationIfMetaDataComplete() {
		// generation strategy of EntityA.id by annotation is IDENTITY
		// inheritance strategy of EntityA in xml descriptor is SEQUENCE
		int valueStrategyA = entityA.getField("id").getValueStrategy();
		assertEquals(ValueStrategies.SEQUENCE, valueStrategyA);
	}

	public void testProcessesFieldAnnotationIfMetaDataIsNotComplete() {
		// generation strategy of EntityB.id by annotation is IDENTITY
        // inheritance strategy of EntityA in xml descriptor is not specified
		int valueStrategyB = entityB.getField("id").getValueStrategy();
		assertEquals(ValueStrategies.AUTOASSIGN, valueStrategyB);
	}

	public void testIgnoresNamedQueryIfMetaDataComplete() {
		// DerivedA has annotated NamedQuery
		String namedQuery = "DerivedA.SelectAll";
		try {
			oemf.createEntityManager().createNamedQuery(namedQuery);
            fail("Expected not to find NamedQuery [" + namedQuery + "]");
		} catch (ArgumentException e) {
			assertTrue(e.getMessage().contains(namedQuery));
		}
	}

	public void testProcessesNamedQueryIfMetaDataIsNotComplete() {
		// EntityB has annotated NamedQuery
		// EntityB has a Named Query in xml descriptor
        oemf.createEntityManager().createNamedQuery("EntityB.SelectOne");
        oemf.createEntityManager().createNamedQuery("EntityB.SelectAll");
	}
}
