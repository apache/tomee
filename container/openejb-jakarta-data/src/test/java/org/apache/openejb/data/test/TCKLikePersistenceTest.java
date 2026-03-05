/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.data.test;

import jakarta.data.exceptions.EntityExistsException;
import jakarta.data.exceptions.OptimisticLockingFailureException;
import jakarta.inject.Inject;
import org.apache.openejb.data.test.entity.Product;
import org.apache.openejb.data.test.repo.ProductCatalog;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests that mimic the Jakarta Data TCK PersistenceEntityTests.
 * These run locally via ApplicationComposer, much faster than the remote TCK.
 */
@RunWith(ApplicationComposer.class)
public class TCKLikePersistenceTest {

    @Inject
    private ProductCatalog catalog;

    @Module
    @Classes(cdi = true, value = {ProductCatalog.class})
    public EjbJar beans() {
        return new EjbJar();
    }

    @Module
    public PersistenceUnit persistence() {
        final PersistenceUnit unit = new PersistenceUnit("product-unit");
        unit.setJtaDataSource("productDatabase");
        unit.setNonJtaDataSource("productDatabaseUnmanaged");
        unit.getClazz().add(Product.class.getName());
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        return unit;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("productDatabase", "new://Resource?type=DataSource");
        p.put("productDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("productDatabase.JdbcUrl", "jdbc:hsqldb:mem:productdb-tck");
        return p;
    }

    private void cleanUp() {
        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testEntityManager ---
    @Test
    public void testInsertFindDeleteByProductNumLike() {
        cleanUp();
        Product p = Product.of("apple", 1.29, "TEST-01");
        catalog.add(p);

        List<Product> found = catalog.findByProductNumLike("TEST-%");
        assertEquals(1, found.size());
        assertEquals("apple", found.get(0).getName());

        long deleted = catalog.deleteByProductNumLike("TEST-%");
        assertEquals(1, deleted);
    }

    // --- Mimics testIdAttributeWithDifferentName ---
    @Test
    public void testIdAttributeWithDifferentName() {
        cleanUp();
        Product p = Product.of("banana", 0.59, "TEST-02");
        catalog.add(p);

        Optional<Product> found = catalog.get("TEST-02");
        assertTrue("Should find product by ID", found.isPresent());
        assertEquals("banana", found.get().getName());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testInsertEntityThatAlreadyExists ---
    @Test
    public void testInsertEntityThatAlreadyExists() {
        cleanUp();
        Product p = Product.of("watermelon", 6.29, "TEST-94");
        catalog.add(p);

        try {
            catalog.add(Product.of("duplicate", 1.0, "TEST-94"));
            fail("Should throw EntityExistsException for duplicate key");
        } catch (EntityExistsException e) {
            // expected
        }

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testLike ---
    @Test
    public void testLike() {
        cleanUp();
        catalog.add(Product.of("apple", 1.29, "TEST-10"));
        catalog.add(Product.of("apricot", 2.49, "TEST-11"));
        catalog.add(Product.of("banana", 0.59, "TEST-12"));

        List<Product> found = catalog.findByNameLike("ap%");
        assertEquals(2, found.size());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testMultipleInsertUpdateDelete ---
    @Test
    public void testMultipleInsertUpdateDelete() {
        cleanUp();

        Product[] products = catalog.addMultiple(
            Product.of("grape", 3.49, "TEST-20"),
            Product.of("kiwi", 2.99, "TEST-21")
        );
        assertEquals(2, products.length);

        // Update multiple
        products[0].setPrice(3.99);
        products[1].setPrice(3.29);
        Product[] updated = catalog.modifyMultiple(products);
        assertEquals(3.99, updated[0].getPrice(), 0.001);
        assertEquals(3.29, updated[1].getPrice(), 0.001);

        // Delete
        catalog.remove(updated[0]);

        // Deleting already-removed entity should throw OptimisticLockingFailureException
        try {
            catalog.remove(updated[0]);
            fail("OptimisticLockingFailureException must be raised because the entities are not found for deletion.");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testNull ---
    @Test
    public void testNull() {
        cleanUp();
        catalog.add(Product.of("pear", null, "TEST-30"));
        catalog.add(Product.of("plum", 1.99, "TEST-31"));

        List<Product> nullPrice = catalog.findByPriceNull();
        assertEquals(1, nullPrice.size());
        assertEquals("pear", nullPrice.get(0).getName());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testQueryWithPositionalParameters (JDQL: WHERE ...) ---
    @Test
    public void testQueryWithPositionalParameters() {
        cleanUp();
        catalog.add(Product.of("pear", 1.49, "TEST-40"));
        catalog.add(Product.of("plum", 1.99, "TEST-41"));
        catalog.add(Product.of("apple", 1.29, "TEST-42"));
        catalog.add(Product.of("apricot", 2.49, "TEST-43"));

        // findByNameLengthAndPriceBelow: WHERE LENGTH(name) = ?1 AND price < ?2 ORDER BY name
        List<Product> found = catalog.findByNameLengthAndPriceBelow(4, 2.0);
        assertEquals(2, found.size());
        assertEquals("pear", found.get(0).getName());
        assertEquals("plum", found.get(1).getName());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testQueryWithNamedParameters (JDQL: FROM Product WHERE ...) ---
    @Test
    public void testQueryWithNamedParameters() {
        cleanUp();
        catalog.add(Product.of("apple", 1.29, "TEST-50"));
        catalog.add(Product.of("banana", 0.59, "TEST-51"));
        catalog.add(Product.of("cherry", 4.99, "TEST-52"));

        // withTaxBetween: FROM Product WHERE (:rate * price <= :max AND :rate * price >= :min) ORDER BY name
        Stream<Product> stream = catalog.withTaxBetween(0.50, 2.00, 1.1);
        List<Product> found = stream.collect(Collectors.toList());
        // apple: 1.1*1.29=1.419 -> in range [0.50, 2.00]
        // banana: 1.1*0.59=0.649 -> in range [0.50, 2.00]
        // cherry: 1.1*4.99=5.489 -> out of range
        assertEquals(2, found.size());
        assertEquals("apple", found.get(0).getName());
        assertEquals("banana", found.get(1).getName());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Mimics testVersionedInsertUpdateDelete ---
    @Test
    public void testVersionedInsertUpdateDelete() {
        cleanUp();

        Product p1 = catalog.add(Product.of("zucchini", 1.49, "TEST-91"));
        Product p2 = catalog.add(Product.of("cucumber", 1.29, "TEST-92"));

        long v1initial = p1.getVersionNum();
        long v2initial = p2.getVersionNum();

        // Modify p1
        p1.setPrice(1.59);
        p1 = catalog.modify(p1);

        // Modify p2
        p2.setPrice(1.39);
        p2 = catalog.modify(p2);

        // Versions must have changed
        assertNotEquals("Version should change after update", v1initial, p1.getVersionNum());
        assertNotEquals("Version should change after update", v2initial, p2.getVersionNum());

        // Modify p1 again
        long v1afterFirst = p1.getVersionNum();
        p1.setPrice(1.54);
        p1 = catalog.modify(p1);
        assertNotEquals("Version should change again", v1afterFirst, p1.getVersionNum());
        assertNotEquals("Version should differ from initial", v1initial, p1.getVersionNum());

        // Set p2 back to old version and try to modify -> should fail
        p2.setVersionNum(v2initial);
        p2.setPrice(1.34);
        try {
            catalog.modify(p2);
            fail("Must raise OptimisticLockingFailureException for entity instance with old version.");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }

        // Remove p1
        catalog.remove(p1);
        Optional<Product> gone = catalog.get("TEST-91");
        assertFalse("Product should be deleted", gone.isPresent());

        // Removing already-removed p1 should throw OptimisticLockingFailureException
        try {
            catalog.remove(p1);
            fail("Must raise OptimisticLockingFailureException for entity that was already removed from the database.");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }

        // Remove p2 with wrong version should throw OptimisticLockingFailureException
        p2.setVersionNum(v2initial);
        try {
            catalog.remove(p2);
            fail("Must raise OptimisticLockingFailureException for entity with non-matching version.");
        } catch (OptimisticLockingFailureException e) {
            // expected
        }

        assertEquals(1, catalog.deleteByProductNumLike("TEST-%"));
    }

    // --- Mimics testNotRunOnNOSQL ---
    @Test
    public void testSaveAndCountAndDeleteBy() {
        cleanUp();
        catalog.save(Product.of("melon", 3.99, "TEST-60"));
        catalog.save(Product.of("mango", 2.49, "TEST-61"));
        catalog.save(Product.of("mint", 0.99, "TEST-62"));

        long count = catalog.countByPriceGreaterThanEqual(2.0);
        assertEquals(2, count);

        catalog.deleteById("TEST-60");
        Optional<Product> gone = catalog.get("TEST-60");
        assertFalse("Product should be deleted", gone.isPresent());

        catalog.deleteByProductNumLike("TEST-%");
    }

    // --- Test @Delete with @By annotation ---
    @Test
    public void testDeleteWithByAnnotation() {
        cleanUp();
        catalog.add(Product.of("orange", 1.99, "TEST-70"));

        catalog.deleteById("TEST-70");
        assertFalse(catalog.get("TEST-70").isPresent());
    }

    // --- Test findByPriceNotNullAndPriceLessThanEqual ---
    @Test
    public void testCompoundFinder() {
        cleanUp();
        catalog.add(Product.of("item1", 1.00, "TEST-80"));
        catalog.add(Product.of("item2", null, "TEST-81"));
        catalog.add(Product.of("item3", 5.00, "TEST-82"));
        catalog.add(Product.of("item4", 3.00, "TEST-83"));

        Stream<Product> found = catalog.findByPriceNotNullAndPriceLessThanEqual(3.0);
        List<Product> list = found.collect(Collectors.toList());
        assertEquals(2, list.size());

        catalog.deleteByProductNumLike("TEST-%");
    }
}
