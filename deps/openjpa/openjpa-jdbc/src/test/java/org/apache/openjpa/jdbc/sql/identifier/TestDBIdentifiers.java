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
package org.apache.openjpa.jdbc.sql.identifier;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;

import junit.framework.TestCase;

/**
 * Test operations on DBIdentifier and QualifiedDBIdentifier.
 *
 */
public class TestDBIdentifiers extends TestCase {

    public void testSchemaOps() {
        // Use a schema name with punctuation.  It will get normalized with
        // delimiters.
        QualifiedDBIdentifier p1 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("my.schema"));
        DBIdentifier s1 = DBIdentifier.newSchema("my.schema");
        assertTrue(QualifiedDBIdentifier.equal(p1, s1));
        assertEquals("\"my.schema\"", p1.getName());
        assertEquals("\"my.schema\"", s1.getName());
        assertEquals(p1.getType(), DBIdentifierType.SCHEMA);
        assertEquals(s1.getType(), DBIdentifierType.SCHEMA);
        assertFalse(s1 instanceof QualifiedDBIdentifier);

        QualifiedDBIdentifier p2 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("SCHEMA"));
        DBIdentifier s2 = DBIdentifier.newSchema("SCHEMA2");
        assertFalse(QualifiedDBIdentifier.equal(p2, s2));
        assertEquals("SCHEMA", p2.getName());
        assertEquals("SCHEMA2", s2.getName());
        assertEquals(p2.getType(), DBIdentifierType.SCHEMA);
        assertEquals(s2.getType(), DBIdentifierType.SCHEMA);
        assertTrue(p2 instanceof QualifiedDBIdentifier);
        assertFalse(s2 instanceof QualifiedDBIdentifier);
    }

    public void testTableOps() {
        DBIdentifier name = DBIdentifier.newTable("table");
        assertEquals("table", name.getName());
        
        // Assert name is normalized using delimiters
        name = DBIdentifier.newTable("my table");
        assertEquals("\"my table\"", name.getName());
        // Assert name does not get split into multiple identifiers
        DBIdentifier[] names = QualifiedDBIdentifier.splitPath(name);
        assertTableEquals(names, null, "\"my table\"");
        
        // Split a delimited schema qualified table name
        name = DBIdentifier.newTable("\"my.schema\".\"my.table\"");
        assertEquals("\"my.schema\".\"my.table\"", name.getName());
        names = QualifiedDBIdentifier.splitPath(name);
        assertTableEquals(names, "\"my.schema\"", "\"my.table\"");
        
        // Split a non-delimited schema qualified table name
        name = DBIdentifier.newTable("my_schema.my_table");
        assertEquals("my_schema.my_table", name.getName());
        names = QualifiedDBIdentifier.splitPath(name);
        assertTableEquals(names, "my_schema", "my_table");

        // Use Qualified Identifier to create a compound normalized schema and table name
        DBIdentifier tName = DBIdentifier.newTable("my table");
        DBIdentifier sName = DBIdentifier.newSchema("my schema");
        QualifiedDBIdentifier path = QualifiedDBIdentifier.newPath(sName, tName);
        assertEquals("\"my schema\".\"my table\"", path.getName());
        assertEquals(tName.getName(), path.getBaseName());
        assertEquals(sName.getName(), path.getSchemaName().getName());
        
        // Use Qualified Identifier to create a compound non-normalized schema and table name
        tName = DBIdentifier.newTable("my_table");
        sName = DBIdentifier.newSchema("my_schema");
        path = QualifiedDBIdentifier.newPath(sName, tName);
        assertEquals("my_schema.my_table", path.getName());
        assertEquals(tName.getName(), path.getBaseName());
        assertEquals(sName.getName(), path.getSchemaName().getName());
        
        QualifiedDBIdentifier p1 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("schema"), 
            DBIdentifier.newTable("my table"));
        QualifiedDBIdentifier p2 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("schema"), 
            DBIdentifier.newTable("\"my table\""));        
        QualifiedDBIdentifier p3 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("schema"), 
            DBIdentifier.newTable("my_table"));
        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(p3));
        assertFalse(p2.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals(DBIdentifier.NULL));
    }
    
    public void testColumnOps() {
        
        DBIdentifier c0 = DBIdentifier.newColumn("\"col.1\"");
        assertEquals("\"col.1\"", c0.getName());
        assertTrue (c0 instanceof QualifiedDBIdentifier);
        assertEquals(c0.getType(), DBIdentifierType.COLUMN);
        
        // Test 3 part column name with mixed delimiting
        DBIdentifier c1 = DBIdentifier.newColumn("column 1");
        DBIdentifier t1 = DBIdentifier.newTable("table");
        DBIdentifier s1 = DBIdentifier.newSchema("\"my schema\"");
        
        DBIdentifier p1 = QualifiedDBIdentifier.newPath(s1, t1, c1);
        assertEquals("\"my schema\".table.\"column 1\"", p1.getName());
        
        DBIdentifier c2 = DBIdentifier.newColumn("\"column_2\"");
        // Create a new table name without delimiters, but switch on the
        // delimit flag.  Otherwise, it will get parsed as a multi-part name.
        DBIdentifier t2 = DBIdentifier.newTable("table.2", true);
        DBIdentifier p2 = QualifiedDBIdentifier.newPath(t2, c2);
        assertEquals("\"table.2\".\"column_2\"", p2.getName());
        
    }
    
    public void testDBIdentifierOps() {
        
        // Test truncate
        DBIdentifier n1 = DBIdentifier.newColumn("abcdefgh");
        n1 = DBIdentifier.truncate(n1,6);
        assertEquals("ab", n1.getName());

        // Test truncate with delimiters
        DBIdentifier n2 = DBIdentifier.newColumn("\"abcd efgh\"");
        n2 = DBIdentifier.truncate(n2,3);
        assertEquals("\"abcd e\"", n2.getName());

        // Test append
        DBIdentifier n3 = DBIdentifier.newColumn("abcdefgh");
        n3 = DBIdentifier.append(n3,"ijk");
        assertEquals("abcdefghijk", n3.getName());

        // Test append with the base name delimited
        DBIdentifier n4 = DBIdentifier.newColumn("\"abcd efgh\"");
        n4 = DBIdentifier.append(n4, "i k");
        assertEquals("\"abcd efghi k\"", n4.getName());
        
        // Test append with both names delimited
        DBIdentifier n5 = DBIdentifier.newColumn("\"abcd efgh\"");
        n5 = DBIdentifier.append(n5, "\"i k\"");
        assertEquals("\"abcd efghi k\"", n5.getName());

        // Test clone
        DBIdentifier cn1 = DBIdentifier.newTable("sch.tbl");
        DBIdentifier cn2 = cn1.clone();
        assertFalse(cn1 == cn2);
        assertEquals(cn1.getName(), cn2.getName());
        assertEquals(cn1, cn2);
        
        DBIdentifier tbl = DBIdentifier.newTable("tbl");
        DBIdentifier sch = DBIdentifier.newSchema("sch");
        QualifiedDBIdentifier path = QualifiedDBIdentifier.newPath(sch, tbl);
        QualifiedDBIdentifier path2 = path.clone();
        assertEquals(tbl.getName(), path.getBaseName());
        assertEquals(sch, path.getSchemaName());
        assertEquals(tbl.getName(), path2.getBaseName());
        assertEquals(sch, path2.getSchemaName());
        
        DBIdentifier tbl2 = DBIdentifier.newTable("tbl2");
        DBIdentifier sch2 = DBIdentifier.newSchema("sch2");
        DBIdentifier col = DBIdentifier.newColumn("col");
        QualifiedDBIdentifier cpath = QualifiedDBIdentifier.newPath(sch2, tbl2, col);
        QualifiedDBIdentifier cpath2 = cpath.clone();
        assertEquals(col.getName(), cpath2.getBaseName());
        assertEquals(sch2, cpath2.getSchemaName());
        assertEquals(tbl2, cpath2.getObjectTableName());
        
        // Test delimit operation on create
        DBIdentifier dName = DBIdentifier.newColumn("\"ITEMNAME\"", true);
        assertEquals("\"ITEMNAME\"", dName.getName());
        
    }
    
    public void testPathOps() {
        
        // Test equals operator with case insensitive names
        QualifiedDBIdentifier p1 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("MyTable"), 
            DBIdentifier.newColumn("mycol"));
        QualifiedDBIdentifier p2 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("MYTABLE"), 
            DBIdentifier.newColumn("MYCOL"));
        assertTrue(QualifiedDBIdentifier.equal(p1, p1));
        
        // Test equals operator with delimited, case sensitive names
        QualifiedDBIdentifier p3 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("\"MyTable\""), 
            DBIdentifier.newColumn("\"mycol\""));
        QualifiedDBIdentifier p4 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("MYTABLE"), 
            DBIdentifier.newColumn("MYCOL"));
        QualifiedDBIdentifier p5 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("\"MyTable\""), 
            DBIdentifier.newColumn("\"mycol\""));
        assertFalse(QualifiedDBIdentifier.equal(p3, p4));
        assertTrue(QualifiedDBIdentifier.equal(p3, p5));
        
        // Test setPath method
        QualifiedDBIdentifier p6 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("MyTable"), 
            DBIdentifier.newColumn("mycol"));
        DBIdentifier n1 = DBIdentifier.newSchema("Schema_1");
        DBIdentifier n2 = DBIdentifier.newTable("Table_1");
        DBIdentifier n3 = DBIdentifier.newColumn("Column_1");
        p6.setPath(n1);
        assertEquals("Schema_1", n1.getName());
        assertEquals(n1.getType(), DBIdentifierType.SCHEMA);
        
        p6.setPath(n2);
        assertEquals("Table_1", n2.getName());
        assertEquals(n2.getType(), DBIdentifierType.TABLE);

        p6.setPath(n3);
        assertEquals("Column_1", n3.getName());
        assertEquals(n3.getType(), DBIdentifierType.COLUMN);

        // Test isDelimited method
        QualifiedDBIdentifier p7 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("MyTable"), 
            DBIdentifier.newColumn("mycol"));
        assertFalse(p7.isDelimited());
        // All identifiers not delimited
        QualifiedDBIdentifier p8 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("My Table"), 
            DBIdentifier.newColumn("mycol"));
        assertFalse(p8.isDelimited());
        // All identifiers delimited by default
        QualifiedDBIdentifier p9 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("My Table"), 
            DBIdentifier.newColumn("my col"));
        assertTrue(p9.isDelimited());

        // All identifiers specifically delimited
        QualifiedDBIdentifier p10 = QualifiedDBIdentifier.newPath(DBIdentifier.newTable("\"MyTable\""), 
            DBIdentifier.newColumn("\"my col\""));
        assertTrue(p10.isDelimited());

        // All identifiers specifically delimited sch + tbl + col
        QualifiedDBIdentifier p11 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("\"MySchema\""), 
            DBIdentifier.newTable("\"my tbl\""));
        assertTrue(p11.isDelimited());

        // Table identifier not delimited
        QualifiedDBIdentifier p12 = QualifiedDBIdentifier.newPath(DBIdentifier.newSchema("\"MySchema\""), 
            DBIdentifier.newTable("mytbl"), 
            DBIdentifier.newColumn("\"my col\""));
        assertFalse(p12.isDelimited());

    }

    private void assertTableEquals(DBIdentifier[] names, String schema,
        String table) {
        assertNotNull(names);
        assertEquals((schema == null ? 1 : 2), names.length);
        int idx = 0;
        if (schema != null) {
            assertEquals(DBIdentifierType.SCHEMA, names[idx].getType());
            assertEquals(names[idx].getName(), schema);
            idx++;
        } else {
            assertEquals(DBIdentifierType.TABLE, names[idx].getType());
            String path = QualifiedDBIdentifier.join(DBIdentifier.newSchema(schema), 
                DBIdentifier.newTable(table));
            assertEquals(names[idx].getName(), path);
        }
    }
    
}
