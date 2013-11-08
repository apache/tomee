/*
 * TestSchema.java
 *
 * Created on October 6, 2006, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.jdbc.schema;

import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;


public class TestSchema
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {

    private Schema _schema = new SchemaGroup().addSchema("schema");
    
    /** Creates a new instance of TestSchema */
    public TestSchema() {
    }
    public TestSchema(String test) {
        super(test);
    }

    /**
     * Test the {@link Schema} class.
     */
    public void testSchema() {
        assertEquals("schema", _schema.getName());
        assertTrue(_schema.getSchemaGroup().isNameTaken("schema"));

        assertTrue(!_schema.getSchemaGroup().isNameTaken("table1"));
        Table table1 = _schema.addTable("table1");
        assertTrue(_schema.getSchemaGroup().isNameTaken("table1"));
        Table table2 = _schema.addTable("table2");
        assertTrue(_schema.getSchemaGroup().isNameTaken("table2"));

        Table[] tables = _schema.getTables();
        assertEquals(2, tables.length);
        assertEquals(table1, tables[0]);
        assertEquals(table2, tables[1]);
        assertEquals(table1, _schema.getTable("table1"));
        assertEquals(table2, _schema.getTable("table2"));
        assertNull(_schema.getTable("table3"));

        assertTrue(_schema.removeTable(table1));
        assertTrue(!_schema.getSchemaGroup().isNameTaken("table1"));
        assertNull(_schema.getTable("table1"));
        assertEquals(table2, _schema.getTable("table2"));
        assertTrue(_schema.removeTable(table2));
        assertTrue(!_schema.getSchemaGroup().isNameTaken("table2"));
        assertNull(_schema.getTable("table2"));
        assertEquals(0, _schema.getTables().length);
    }

    /**
     * Test the {@link Table} class.
     */
    public void testTable() {
        Table table = _schema.addTable("table");
        assertEquals(_schema, table.getSchema());
        assertEquals("table", table.getName());

        assertTrue(!table.isNameTaken("schema"));
        assertTrue(table.isNameTaken("table"));

        // pk testing
        assertNull(table.getPrimaryKey());
        PrimaryKey pk = table.addPrimaryKey("pk");
        assertEquals(table, pk.getTable());
        assertEquals(pk, table.getPrimaryKey());
        assertTrue(!table.isNameTaken("pk"));
        assertTrue(_schema.getSchemaGroup().isNameTaken("pk"));

        table.removePrimaryKey();
        assertNull(table.getPrimaryKey());
        assertTrue(!_schema.getSchemaGroup().isNameTaken("pk"));

        // column testing
        Column c2 = table.addColumn("c2");
        Column c1 = table.addColumn("c1");
        assertTrue(table.isNameTaken("c1"));
        assertTrue(!_schema.getSchemaGroup().isNameTaken("c1"));
        assertEquals(table, c1.getTable());
        Column[] cols = table.getColumns();
        assertEquals(2, cols.length);
        assertEquals(c2, cols[0]);
        assertEquals(c1, cols[1]);

        assertEquals(c1, table.getColumn("c1"));
        assertEquals(c2, table.getColumn("c2"));

        assertTrue(table.removeColumn(c1));
        assertTrue(!table.isNameTaken("c1"));
        assertNull(table.getColumn("c1"));

        // foreign key testing
        ForeignKey fk = table.addForeignKey("fk");
        assertTrue(_schema.getSchemaGroup().isNameTaken("fk"));
        assertTrue(!table.isNameTaken("fk"));
        assertEquals(table, fk.getTable());
        ForeignKey[] fks = table.getForeignKeys();
        assertEquals(1, fks.length);
        assertEquals(fk, fks[0]);

        assertTrue(table.removeForeignKey(fk));
        assertTrue(!_schema.getSchemaGroup().isNameTaken("fk"));
        assertEquals(0, table.getForeignKeys().length);

        // index testing
        Index idx = table.addIndex("idx");
        assertTrue(_schema.getSchemaGroup().isNameTaken("idx"));
        assertTrue(!table.isNameTaken("idx"));
        assertEquals(table, idx.getTable());
        Index[] idxs = table.getIndexes();
        assertEquals(1, idxs.length);
        assertEquals(idx, idxs[0]);

        assertEquals(idx, table.getIndex("idx"));
        assertTrue(table.removeIndex(idx));
        assertTrue(!table.isNameTaken("idx"));
        assertNull(table.getIndex("idx"));
    }

    /**
     * Test the {@link Index} class.
     */
    public void testIndex() {
        Table table = _schema.addTable("table");
        Column c1 = table.addColumn("c1");
        Column c2 = table.addColumn("c2");
        Table table2 = _schema.addTable("table2");
        Column c3 = table2.addColumn("c3");

        Index idx = table.addIndex("idx");
        try {
            idx.addColumn(c3);
            fail("Allowed addition of column of another table.");
        } catch (RuntimeException re) {
        }

        Column[] cols = idx.getColumns();
        assertEquals(0, cols.length);

        idx.addColumn(c1);
        idx.addColumn(c2);
        cols = idx.getColumns();
        assertEquals(2, cols.length);
        assertEquals(c1, cols[0]);
        assertEquals(c2, cols[1]);

        assertTrue(idx.removeColumn(c1));
        cols = idx.getColumns();
        assertEquals(1, cols.length);
        assertEquals(c2, cols[0]);
        assertTrue(idx.removeColumn(c2));
        cols = idx.getColumns();
        assertEquals(0, cols.length);

        assertTrue(!idx.isUnique());
        idx.setUnique(true);
        assertTrue(idx.isUnique());
    }

    /**
     * Test the {@link ForeignKey} class.
     */
    public void testForeignKey() {
        Table table = _schema.addTable("table");
        Column c1 = table.addColumn("c1");
        Column c2 = table.addColumn("c2");
        Table table2 = _schema.addTable("table2");
        Column c3 = table2.addColumn("c3");
        Column c4 = table2.addColumn("c4");

        ForeignKey fk = table.addForeignKey("fk");
        try {
            fk.join(c4, c2);
            fail("Allowed addition of column of another table.");
        } catch (RuntimeException re) {
        }

        Column[] cols = fk.getColumns();
        Column[] pkCols = fk.getPrimaryKeyColumns();
        assertEquals(0, cols.length);
        assertEquals(0, pkCols.length);
        PrimaryKey pk = table2.addPrimaryKey("pk");
        pk.addColumn(c3);
        fk.join(c1, c3);
        pk.addColumn(c4);
        fk.join(c2, c4);
        cols = fk.getColumns();
        pkCols = fk.getPrimaryKeyColumns();
        assertEquals(2, cols.length);
        assertEquals(c1, cols[0]);
        assertEquals(c2, cols[1]);
        assertEquals(c3, pkCols[0]);
        assertEquals(c4, pkCols[1]);

        assertTrue(fk.removeJoin(c1));
        cols = fk.getColumns();
        pkCols = fk.getPrimaryKeyColumns();
        assertEquals(1, cols.length);
        assertEquals(1, pkCols.length);
        assertEquals(c2, cols[0]);
        assertEquals(c4, pkCols[0]);
        assertTrue(fk.removeJoin(c2));
        cols = fk.getColumns();
        pkCols = fk.getPrimaryKeyColumns();
        assertEquals(0, cols.length);
        assertEquals(0, pkCols.length);

        assertEquals(ForeignKey.ACTION_NONE, fk.getDeleteAction());
        assertTrue(fk.isLogical());
        fk.setDeleteAction(ForeignKey.ACTION_RESTRICT);
        assertEquals(ForeignKey.ACTION_RESTRICT, fk.getDeleteAction());
        assertFalse(fk.isLogical());
    }

    /**
     * Tests the {@link SchemaGroup} class.
     */
    public void testSchemaGroup() {
        SchemaGroup group = _schema.getSchemaGroup();
        assertEquals(_schema, group.getSchema("schema"));
        Table foo1 = _schema.addTable("foo");

        Schema schema2 = group.addSchema("schema2");
        assertNull(schema2.getTable("foo"));
        Table foo2 = schema2.addTable("foo");
        assertEquals(foo2, schema2.getTable("foo"));
        assertEquals(foo1, _schema.getTable("foo"));

        assertEquals(foo1, group.findTable("schema.foo"));
        assertEquals(foo2, group.findTable("schema2.foo"));
    }

    /**
     * Test primary key removal.
     */
    public void testPrimaryKeyRemoval() {
        Table table = _schema.addTable("table");
        Column c1 = table.addColumn("c1");
        Column c2 = table.addColumn("c2");
        Table table2 = _schema.addTable("table2");
        Column c3 = table2.addColumn("c3");
        Column c4 = table2.addColumn("c4");
        PrimaryKey pk = table2.addPrimaryKey("pk");
        pk.addColumn(c3);
        pk.addColumn(c4);
        ForeignKey fk = table.addForeignKey("fk");
        fk.join(c1, c3);
        fk.join(c2, c4);

        table2.removePrimaryKey();
        assertNull(pk.getTable());
        assertNull(table2.getPrimaryKey());
        assertEquals(0, table.getForeignKeys().length);
    }

    /**
     * Test column removal.
     */
    public void testColumnRemoval() {
        Table table = _schema.addTable("table");
        Column c1 = table.addColumn("c1");
        Column c2 = table.addColumn("c2");
        PrimaryKey pk = table.addPrimaryKey("pk");
        pk.addColumn(c1);
        Index idx1 = table.addIndex("idx1");
        idx1.addColumn(c1);
        Index idx2 = table.addIndex("idx2");
        idx2.addColumn(c1);
        idx2.addColumn(c2);

        Table table2 = _schema.addTable("table2");
        Column c3 = table2.addColumn("c3");
        Column c4 = table2.addColumn("c4");
        pk = table2.addPrimaryKey("pk2");
        pk.addColumn(c3);
        ForeignKey fk = table.addForeignKey("fk");
        fk.join(c1, c3);

        table.removeColumn(c1);
        assertNull(table.getPrimaryKey());
        assertNull(table.getIndex("idx1"));
        assertEquals(1, idx2.getColumns().length);
        assertEquals(0, table.getForeignKeys().length);
    }

    public static void main(String[] args) {
        //main(TestSchema.class);
	}
    
}
