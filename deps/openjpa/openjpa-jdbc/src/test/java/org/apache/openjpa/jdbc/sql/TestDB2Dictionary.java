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
package org.apache.openjpa.jdbc.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.kernel.StoreContext;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;

public class TestDB2Dictionary extends MockObjectTestCase {
    final JDBCConfiguration mockConfiguration = mock(JDBCConfiguration.class);
    final Statement mockStatement = mock(Statement.class);
    final Connection mockConnection = mock(Connection.class);
    final ResultSet mockRS = mock(ResultSet.class);
    final DataSource mockDS = mock(DataSource.class);
    final DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);

    final StoreContext sc = null;
    final String schema = "abcd";

    /*
     * When DS1 is non null we should get a connection and use it to obtain the schema name.
     */
    public void testGetDefaultSchemaNameDS1() throws Exception {
        // Expected method calls on the mock objects above. If any of these are
        // do not occur, or if any other methods are invoked on the mock objects
        // an exception will be thrown and the test will fail.
        checking(new Expectations() {
            {
                // Wiring, make sure the appropriate mocks are created.
                oneOf(mockConfiguration).getDataSource(with(equal(sc)));
                will(returnValue(mockDS));

                oneOf(mockDS).getConnection();
                will(returnValue(mockConnection));

                oneOf(mockConnection).createStatement();
                will(returnValue(mockStatement));

                oneOf(mockStatement).executeQuery(with(any(String.class)));
                will(returnValue(mockRS));

                // expect one call to rs.next() - needs to return true.
                oneOf(mockRS).next();
                will(returnValue(true));

                // return our schema name
                oneOf(mockRS).getString(1);
                will(returnValue(schema));

                // cleanup
                oneOf(mockConnection).close();
                oneOf(mockRS).close();
                oneOf(mockStatement).close();

                allowing(mockConfiguration);
            }
        });

        DBDictionary dict = new DB2Dictionary();
        dict.setConfiguration(mockConfiguration);
        assertNotNull(dict);
        assertEquals(schema, dict.getDefaultSchemaName());
    }

    /*
     * When ds1 is null, fallback to ds2
     */
    public void testGetDefaultSchemaNameDS2() throws Exception {
        // Expected method calls on the mock objects above. If any of these are
        // do not occur, or if any other methods are invoked on the mock objects
        // an exception will be thrown and the test will fail.
        checking(new Expectations() {
            {
                // Wiring, make sure the appropriate mocks are created.
                oneOf(mockConfiguration).getDataSource(with(equal(sc)));
                will(returnValue(null));

                oneOf(mockConfiguration).getDataSource2(with(equal(sc)));
                will(returnValue(mockDS));

                oneOf(mockDS).getConnection();
                will(returnValue(mockConnection));

                oneOf(mockConnection).createStatement();
                will(returnValue(mockStatement));

                oneOf(mockStatement).executeQuery(with(any(String.class)));
                will(returnValue(mockRS));

                // expect one call to rs.next() - needs to return true.
                oneOf(mockRS).next();
                will(returnValue(true));

                // return our schema name
                oneOf(mockRS).getString(1);
                will(returnValue(schema));

                // cleanup
                oneOf(mockConnection).close();
                oneOf(mockRS).close();
                oneOf(mockStatement).close();

                allowing(mockConfiguration);
            }
        });

        DBDictionary dict = new DB2Dictionary();
        dict.setConfiguration(mockConfiguration);
        assertNotNull(dict);
        assertEquals(schema, dict.getDefaultSchemaName());
    }

    /*
     * When ds1 is null, fallback to ds2
     */
    public void testGetDefaultSchemaNameNoDS() throws Exception {
        // Expected method calls on the mock objects above. If any of these are
        // do not occur, or if any other methods are invoked on the mock objects
        // an exception will be thrown and the test will fail.
        checking(new Expectations() {
            {
                // both datasources are null for this test.
                oneOf(mockConfiguration).getDataSource(with(equal(sc)));
                will(returnValue(null));

                oneOf(mockConfiguration).getDataSource2(with(equal(sc)));
                will(returnValue(null));

                allowing(mockConfiguration);
            }
        });

        DBDictionary dict = new DB2Dictionary();
        dict.setConfiguration(mockConfiguration);
        assertNotNull(dict);
        assertEquals(null, dict.getDefaultSchemaName());
    }

    /*
     * TestWhitespace trim
     */
    public void testGetDefaultSchemaNameTrimmed() throws Exception {
        final String schema2 = "abcd     ";
        // Expected method calls on the mock objects above. If any of these are
        // do not occur, or if any other methods are invoked on the mock objects
        // an exception will be thrown and the test will fail.
        checking(new Expectations() {
            {
                // Wiring, make sure the appropriate mocks are created.
                oneOf(mockConfiguration).getDataSource(with(equal(sc)));
                will(returnValue(mockDS));

                oneOf(mockDS).getConnection();
                will(returnValue(mockConnection));

                oneOf(mockConnection).createStatement();
                will(returnValue(mockStatement));

                oneOf(mockStatement).executeQuery(with(any(String.class)));
                will(returnValue(mockRS));

                // expect one call to rs.next() - needs to return true.
                oneOf(mockRS).next();
                will(returnValue(true));

                // return our schema name
                oneOf(mockRS).getString(1);
                will(returnValue(schema2));

                // cleanup
                oneOf(mockConnection).close();
                oneOf(mockRS).close();
                oneOf(mockStatement).close();

                allowing(mockConfiguration);
            }
        });

        DBDictionary dict = new DB2Dictionary();
        dict.setConfiguration(mockConfiguration);
        assertNotNull(dict);
        assertEquals(schema2.trim(), dict.getDefaultSchemaName());
    }

    /*
     * Verifies that the ConnectedConfiguration method only uses the DBMetaData to determine the correct behavior.
     */
    public void testConnectedConfigurationOnlyUsesMetaData() throws Exception {
        checking(new Expectations() {
            {
                // No activity on the connection other than getting the metadata. 
                allowing(mockConnection).getMetaData();
                will(returnValue(mockMetaData));

                // anything on the configuration or DBMetaData is fair game. 
                allowing(mockMetaData); 
                allowing(mockConfiguration);
            }
        });

        DB2Dictionary dict = new DB2Dictionary();
        
        // skip all the meta data resolution code. 
        dict.db2ServerType = DB2Dictionary.db2UDBV82OrLater;
        dict.setMajorVersion(9);
        dict.setConfiguration(mockConfiguration);
        assertNotNull(dict);
        dict.connectedConfiguration(mockConnection);
    }
}
