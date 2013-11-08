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
package org.apache.openjpa.persistence.blob.mysql;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Testcase for MySQL Blob types. OPENJPA-740 introduced intelligent column type for BLOBs, OPENJPA-1870 refined it a
 * bit.  
 */
public class TestBlobColumnType extends SingleEMFTestCase {

    private static boolean _firstRun=true;
    private boolean _runTest = false; // only test with MySQL 

    public void setUp() throws Exception {
        
        // create EMF solely to obtain a DBDictionary.
        // need to do this without BlobColumnEntity.class since it contains a column definition which might
        // not work with all databases. 
        super.setUp((Object) null);
        if (!(getDBDictionary() instanceof MySQLDictionary || getDBDictionary() instanceof MariaDBDictionary)) {
            // normal teardown will take care of the EMF.
            return;
        }
        
        // remove the EMF
        tearDown();
        
        _runTest = true;
        super.setUp(BlobColumnEntity.class, DROP_TABLES, "openjpa.jdbc.SchemaFactory", "native");
        
        if(_firstRun) { 
            emf.createEntityManager().close(); // trigger table creation.
            _firstRun = false; 
        }
    }

    private Column getCol(String name) { 
        ClassMapping mapping = getMapping(BlobColumnEntity.class);

        Table t = mapping.getTable();
        Column col = t.getColumn(DBIdentifier.newIdentifier(name, DBIdentifierType.COLUMN, true));
        assertNotNull(col);
        return col;
    }
    
    public void testSmallLob() {
        if (_runTest) {
            assertEquals(MySQLDictionary.tinyBlobTypeName, getCol("smallLob").getTypeIdentifier().getName());
        }
    }
    
    public void testMedLob() {
        if (_runTest) {
            assertEquals(MySQLDictionary.mediumBlobTypeName, getCol("medLob").getTypeIdentifier().getName());
        }
    }
    
    public void testLongBlob() {
        if (_runTest) {
            assertEquals(MySQLDictionary.longBlobTypeName, getCol("longLob").getTypeIdentifier().getName());
        }
    }
    
    public void testOldLob() {
        if (_runTest) {
            assertEquals(getDBDictionary().blobTypeName, getCol("oldLob").getTypeIdentifier().getName());
        }
    }
    
    public void testDefaultLob() {
        if (_runTest) {
            assertEquals(getDBDictionary().blobTypeName, getCol("defaultLob").getTypeIdentifier().getName());
        }
    }
    
    public void testDefinedLob() {
        if (_runTest) {
            assertEquals("BINARY", getCol("definedLob").getTypeIdentifier().getName());
        }
    }
}
