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
package org.apache.openjpa.persistence.jdbc.update;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.test.CombinatorialPersistenceTestCase;

/**
 * Tests for SQL statement ordering capabilities of different update strategies
 * for a Parent-Child model against different physical database constraints.
 * 
 * SQL statement ordering is influenced by 
 * 1. In-memory schema model: The in-memory schema model can be aware of logical
 *    or physical foreign keys. 
 *    a) This is configured by <code>jdbc.SchemaFactory</code> property setting 
 *       to <code>native(ForeignKeys=true|false)</code> which makes OpenJPA to 
 *       read physical foreign key information from database 
 *    b) @ForeignKey annotation on the relation -- OpenJPA then considers 
 *       logical foreign key 
 *       
 *  2. Physical Schema: The database schema can be defined with physical
 *     foreign keys. This is configured by <code>jdbc.MappingDefaults</code>
 *     property setting to <code>ForeignKeyDeleteAction</code> 
 *     
 *  3. Update Strategy: the update manager is configured by 
 *     <code>jdbc.UpdateManager</code> 
 *     
 *  4. Order of persistence operation: The order in which the application calls
 *     persistence operations such as persist() or remove(). In this test, we
 *     control this by PersistOrder enum. This application order is maintained
 *     if the update manager is set to <code>'operation-order'</code>.
 *     
 * 
 *   This test case also demonstrates how to write a test case that runs with
 *   multiple combination of configurations. Each configuration discussed above
 *   has multiple possible values and testing all possible combination can be 
 *   an arduous task. The {@link CombinatorialPersistenceTestCase combinatorial}
 *   test case utility helps to auto-generate these multiple configurations and
 *   execute the same test with all the combinations.
 *   
 * @author Pinaki Poddar
 * 
 */
public class TestParentChild extends CombinatorialPersistenceTestCase {
	// Each of these property keys can take multiple possible values 
	private static String Key_UpdateManager = "openjpa.jdbc.UpdateManager";
	private static String Key_SchemaFactory = "openjpa.jdbc.SchemaFactory";
    private static String Key_MappingDefaults = "openjpa.jdbc.MappingDefaults";
	private static String Key_PersistOrder = "persist-order";

	private static String[] Option_MappingDefaults = {
        "ForeignKeyDeleteAction=restrict, JoinForeignKeyDeleteAction=restrict",
        "ForeignKeyDeleteAction=none, JoinForeignKeyDeleteAction=none" };
	
	private static String[] Option_SchemaFactory = {
		"native(ForeignKeys=false)", 
		"native(ForeignKeys=true)" };

	private static String[] Option_UpdateManager = { 
		"operation-order",
		"constraint" };
	
	private static enum PersistOrder {
		IMPLICIT_CASCADE, 
		CHILD_THEN_PARENT, 
		PARENT_THEN_CHILD
	};


	// The options are added in a static block, so that we can count on
	// total number of combinations before the test is set up.
	static {
        getHelper().addOption(Key_MappingDefaults, Option_MappingDefaults);
		getHelper().addOption(Key_SchemaFactory, Option_SchemaFactory);
		getHelper().addOption(Key_UpdateManager, Option_UpdateManager);

		// The last argument tells that this is a runtime option. So the
		// values are included to generate combinations but are excluded
		// from generating OpenJPA configuration.
        getHelper().addOption(Key_PersistOrder, PersistOrder.values(), true);
	}

	public void setUp() {
        // The options can also be added in setup() as well but then 
        // coutTestCase() will only record test methods and not multiply them
        // with number of configuration combinations the same tests will run.
        getHelper().addOption(Key_MappingDefaults, Option_MappingDefaults);
		getHelper().addOption(Key_SchemaFactory, Option_SchemaFactory);
		getHelper().addOption(Key_UpdateManager, Option_UpdateManager);

        getHelper().addOption(Key_PersistOrder, PersistOrder.values(), true);
		
		sql.clear();
		super.setUp(DROP_TABLES, Parent.class, Child.class);
	}

	/**
     * This test will run in 2*2*2*3 = 24 times with different configurations.
	 */
	public void testInsert() {
		Parent parent = createData(getPersistOrder(), 3);
		validateData(parent.getId(), 3);

        // verification can be challenging under multiple configuration options
        // see the methods as exemplars how verification can vary based on
		// configuration.
		assertLogicalOrPhysicalForeignKey();
		assertPhysicalForeignKeyCreation();
	}

	Parent createData(PersistOrder order, int nChild) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		Parent parent = new Parent();
		parent.setName("parent");
		for (int i = 1; i <= nChild; i++)
			parent.newChild("Child-" + i);
		switch (order) {
		case IMPLICIT_CASCADE:
			em.persist(parent);
			break;
		case CHILD_THEN_PARENT:
			for (Child child : parent.getChildren()) {
				em.persist(child);
			}
			em.persist(parent);
			break;
		case PARENT_THEN_CHILD:
			em.persist(parent);
			for (Child child : parent.getChildren()) {
				em.persist(child);
			}
			break;
		default:
			throw new RuntimeException("Bad order " + order);
		}
		em.getTransaction().commit();
		em.clear();
		return parent;
	}

	void validateData(Object pid, int childCount) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Parent parent = em.find(Parent.class, pid);
		assertNotNull(parent);
		assertEquals(childCount, parent.getChildren().size());
		em.getTransaction().rollback();
	}
	
	/**
	 * Asserts that foreign key constraint will be defined on the database
	 * for certain combinations of configurations.
	 */
	void assertPhysicalForeignKeyCreation() {
        String regex = "ALTER TABLE .* ADD FOREIGN KEY \\(PARENT_ID\\) "
		             + "REFERENCES Parent \\(id\\)(\\sDEFERRABLE)?";
		if (getMappingDefaults().contains("restrict")) {
			assertSQL(regex);
		} else {
			assertNotSQL(regex);
		}
	}

	/**
	 * Asserts that update SQL will be issued to set the foreign key value
	 * after the insert under some configuration.
	 */
	void assertPostInsertUpdate() {
		if (getPersistOrder().equals(PersistOrder.CHILD_THEN_PARENT)
			&& getMappingDefaults().contains("restrict")) {
			assertSQL("UPDATE .* SET PARENT_ID .* WHERE .*");
		}
	}
	
	/**
	 * Asserts that foreign key will be logical or physical under different 
	 * combination of configuration.
	 */
	void assertLogicalOrPhysicalForeignKey() {
		ForeignKey fk = getChildParentForeignKey();
        boolean physicalKeyExists = getMappingDefaults().contains("restrict");
        boolean keyRead = getSchemaFactory().contains("ForeignKeys=true");
		if (physicalKeyExists && keyRead)
			assertFalse(fk.isLogical());
		else if (keyRead)
			assertTrue(fk.isLogical());
	}
	
	ForeignKey getChildParentForeignKey() {
		MetaDataRepository repos = emf.getConfiguration()
				.getMetaDataRepositoryInstance();
		ClassMetaData child = repos.getCachedMetaData(Child.class);
		FieldMapping parent = (FieldMapping) child.getField("parent");
		return parent.getForeignKey();
	}

	PersistOrder getPersistOrder() {
		return (PersistOrder) getHelper().getOption(Key_PersistOrder);
	}

	String getMappingDefaults() {
		return getHelper().getOptionAsString(Key_MappingDefaults);
	}

	String getSchemaFactory() {
		return getHelper().getOptionAsString(Key_SchemaFactory);
	}
}
