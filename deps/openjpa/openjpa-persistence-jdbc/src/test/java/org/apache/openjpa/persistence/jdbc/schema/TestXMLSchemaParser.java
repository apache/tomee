/*
 * TestXMLSchemaParser.java
 *
 * Created on October 6, 2006, 2:42 PM
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

import java.io.IOException;
import java.sql.Types;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Sequence;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.XMLSchemaParser;


public class TestXMLSchemaParser
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
        
    protected JDBCConfiguration conf = null;
    private SchemaGroup _group = null;
    
    
    /** Creates a new instance of TestXMLSchemaParser */
    public TestXMLSchemaParser() {
    }
    
    public TestXMLSchemaParser(String test) {
        super(test);
    }
    
    public void setUp()
    throws Exception {
        this.conf = new JDBCConfigurationImpl();
        _group = getSchemaGroup();
    }
    
    /**
     * Parses the schema group from the schema XML
     * resources in this package.
     */
    protected SchemaGroup parseSchemaGroup()
    throws IOException {
        XMLSchemaParser parser = new SCMSchemaParser(this.conf);
        parser.parse(TestXMLSchemaParser.class, false);
        parser.parse(TestSchema.class, false);    // will go up to package level
        return parser.getSchemaGroup();
    }
    
    /**
     * Return the schema group to use in testing.  Returns
     * {@link #parseSchemaGroup} by default.
     */
    protected SchemaGroup getSchemaGroup()
    throws Exception {
        return parseSchemaGroup();
    }
    
    /**
     * Checks the generated schema group for accuracy.
     */
    public void testSchemaParsing() {
        assertEquals(2, _group.getSchemas().length);
        assertEquals("SCHEMA1", _group.getSchema("SCHEMA1").getName());
        assertEquals("SCHEMA2", _group.getSchema("SCHEMA2").getName());
    }
    
    /**
     * Checks the pased sequences.
     */
    public void testSequenceParsing() {
        Schema schema = _group.getSchema("SCHEMA1");
        assertEquals(2, schema.getSequences().length);
        assertEquals(0, _group.getSchema("SCHEMA2").getSequences().length);
        
        Sequence seq1 = schema.getSequence("SEQ1");
        assertNotNull(seq1);
        assertEquals("SEQ1", seq1.getName());
        assertEquals(seq1, _group.findSequence("SEQ1"));
        assertEquals(seq1, _group.findSequence("SCHEMA1.SEQ1"));
        assertEquals(1, seq1.getInitialValue());
        assertEquals(1, seq1.getIncrement());
        assertEquals(0, seq1.getAllocate());
        
        Sequence seq2 = schema.getSequence("SEQ2");
        assertNotNull(seq2);
        assertEquals(3, seq2.getInitialValue());
        assertEquals(5, seq2.getIncrement());
        assertEquals(50, seq2.getAllocate());
    }
    
    /**
     * Checks table and column parsing.
     */
    public void testTableColumnParsing() {
        Schema schema1 = _group.getSchema("SCHEMA1");
        Table[] tables = schema1.getTables();
        assertEquals(2, tables.length);
        assertEquals("TABLE1", tables[0].getName());
        assertEquals("TABLE3", tables[1].getName());
        
        Column[] cols = tables[0].getColumns();
        assertEquals(2, cols.length);
        assertEquals("COL1", cols[0].getName());
        assertEquals("COL2", cols[1].getName());
        assertEquals(Types.VARCHAR, cols[0].getType());
        assertEquals(Types.FLOAT, cols[1].getType());
        assertTrue(cols[0].isNotNull());
        assertTrue(!cols[1].isNotNull());
        assertEquals("def", cols[0].getDefault());
        assertNull(cols[1].getDefault());
    }
    
    /**
     * Test that primary keys are resolved correctly.
     */
    public void testPrimaryKeyParsing() {
        Table table = _group.getSchema("SCHEMA1").getTable("TABLE1");
        PrimaryKey pk = table.getPrimaryKey();
        assertNotNull(pk);
        assertEquals("PK1", pk.getName());
        assertTrue(pk.isLogical());
        assertEquals(1, pk.getColumns().length);
        assertEquals(table.getColumn("COL1"), pk.getColumns()[0]);
        
        table = _group.getSchema("SCHEMA2").getTable("TABLE2");
        pk = table.getPrimaryKey();
        assertNotNull(pk);
        assertEquals("PK2", pk.getName());
        assertTrue(!pk.isLogical());
        assertEquals(2, pk.getColumns().length);
        assertEquals(table.getColumn("COL1"), pk.getColumns()[0]);
        assertEquals(table.getColumn("COL2"), pk.getColumns()[1]);
    }
    
    /**
     * Test that indexes are resolved correctly.
     */
    public void testIndexParsing() {
        Table table = _group.getSchema("SCHEMA1").getTable("TABLE1");
        Index idx = table.getIndex("IDX1");
        assertNotNull(idx);
        assertTrue(idx.isUnique());
        assertEquals(1, idx.getColumns().length);
        assertEquals(table.getColumn("COL2"), idx.getColumns()[0]);
        
        table = _group.getSchema("SCHEMA2").getTable("TABLE2");
        idx = table.getIndex("IDX2");
        assertNotNull(idx);
        assertTrue(!idx.isUnique());
        assertEquals(2, idx.getColumns().length);
        assertEquals(table.getColumn("COL1"), idx.getColumns()[0]);
        assertEquals(table.getColumn("COL2"), idx.getColumns()[1]);
    }
    
    /**
     * Test that foreign keys are resolved correctly.
     */
    public void testForeignKeyParsing() {
        Table table1 = _group.getSchema("SCHEMA1").getTable("TABLE1");
        Table table2 = _group.getSchema("SCHEMA2").getTable("TABLE2");
        
        ForeignKey fk = table1.getForeignKeys()[0];
        assertEquals("FK1", fk.getName());
        assertNotNull(fk);
        assertEquals(ForeignKey.ACTION_RESTRICT, fk.getDeleteAction());
        Column[] cols = fk.getColumns();
        Column[] pkCols = fk.getPrimaryKeyColumns();
        assertEquals(2, cols.length);
        assertEquals(2, pkCols.length);
        assertEquals(table1.getColumn("COL1"), cols[0]);
        assertEquals(table2.getColumn("COL1"), pkCols[0]);
        assertEquals(table1.getColumn("COL2"), cols[1]);
        assertEquals(table2.getColumn("COL2"), pkCols[1]);
        
        fk = table2.getForeignKeys()[0];
        assertEquals("FK2", fk.getName());
        assertNotNull(fk);
        assertEquals(ForeignKey.ACTION_NONE, fk.getDeleteAction());
        cols = fk.getColumns();
        pkCols = fk.getPrimaryKeyColumns();
        assertEquals(1, cols.length);
        assertEquals(1, pkCols.length);
        assertEquals(table2.getColumn("COL2"), cols[0]);
        assertEquals(table1.getColumn("COL1"), pkCols[0]);
    }
    
    public static void main(String[] args) {
        //main(TestXMLSchemaParser.class);
    }
    
    public static class SCMSchemaParser
            extends XMLSchemaParser {
        
        public SCMSchemaParser(JDBCConfiguration conf) {
            super(conf);
            setSuffix(".scm");
        }
    }
    
}
