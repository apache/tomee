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
package org.apache.openjpa.persistence.jdbc.meta;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ReverseMappingTool;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests the added useSchemaElement functionality of the
 * ReverseMappingTool and CodeGenerator classes.
 *
 * @author Austin Dorenkamp (ajdorenk)
 */
public class TestUseSchemaElement extends /*TestCase*/ SingleEMFTestCase {

    public void setUp() throws Exception {
        super.setUp();
        File f = new File("target/orm.xml");

        // Make sure to clean up orm.xml from a prior run
        if (f.exists()) {
            assertTrue(f.delete());
        }
        setSupportedDatabases(org.apache.openjpa.jdbc.sql.DerbyDictionary.class);
    }

    @Override
    public String getPersistenceUnitName(){
        return "rev-mapping-pu";
    }

    public void testGettersAndSetters() throws Exception {

        JDBCConfiguration conf = (JDBCConfiguration) ((OpenJPAEntityManagerFactory) emf).getConfiguration();

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        Query q = em.createNativeQuery("CREATE TABLE USCHEMA.USCHANTBL (ID INTEGER PRIMARY KEY)");
        try {
            q.executeUpdate();
            em.getTransaction().commit();
        } catch (Throwable t) {
            em.getTransaction().rollback();
            System.out.println(t.toString());
        }

        ReverseMappingTool.Flags flags = new ReverseMappingTool.Flags();
        flags.metaDataLevel = "package";
        flags.generateAnnotations = true;
        flags.accessType = "property";
        flags.nullableAsObject = true;
        flags.useSchemaName = false;
        flags.useSchemaElement = false;
        flags.packageName = "";
        flags.directory = Files.getFile("./target", null);
        ReverseMappingTool.run(conf, new String[0], flags, null);

        /* Now that the tool has been run, we will test it by reading the generated files */

        // This tests the removal of the schema annotation in the Uschantbl.java file
        File uschantbl = new File("./target/Uschantbl.java");
        Scanner inFile = null;
        String currentLine;
        try {
            inFile = new Scanner(uschantbl);

            while(inFile.hasNextLine())
            {
                currentLine = inFile.nextLine();
                if((currentLine.length()) > 0 && (currentLine.charAt(0) != '@'))
                {
                    continue;
                }

                if(currentLine.contains("Table(schema="))
                {
                    fail("Uschantbl.java still contains schema name");
                }
            }

        } catch (FileNotFoundException e) {
            fail("Uschantbl.java not generated in ./target by ReverseMappingTool");
        }
        finally {
            if (inFile != null) {
                inFile.close();
            }
        }

        // Delete file to clean up workspace
        assertTrue(uschantbl.delete());

        // This tests the removal of the schema name from the orm.xml file
        File orm = new File("target/orm.xml");
        try {
            inFile = new Scanner(orm);
            while(inFile.hasNextLine())
            {
                if(inFile.nextLine().contains("<table schema="))
                {
                    fail("Orm.xml still contains schema name");
                }
            }
        } catch (FileNotFoundException e) {
            fail("Orm.xml not generated in root directory by ReverseMappingTool");
        }
        finally {
            if (inFile != null) {
                inFile.close();
            }
        }
        // Delete file to clean up workspace. Also, test will break with
        // org.apache.openjpa.util.UserException if orm.xml exists prior to running
        //assertTrue(orm.delete());
    }

}
