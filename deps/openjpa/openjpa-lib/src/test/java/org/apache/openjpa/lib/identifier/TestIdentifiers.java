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
package org.apache.openjpa.lib.identifier;

import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.lib.identifier.IdentifierUtilImpl;
import org.apache.openjpa.lib.test.AbstractTestCase;

public class TestIdentifiers extends AbstractTestCase {

    public void testIdentifierConversion() {
        
        // Create a naming configs used for testing.
        IdentifierConfiguration defConfig = new IdConfigurationTestImpl();
        IdentifierConfiguration newConfig = new NewIdConfigurationTestImpl();
        
        IdentifierUtil nu = new IdentifierUtilImpl(defConfig);
        
        // Test basic name conversion with single name converter
        String n0 = "\"TABLE\"";
        String cn0 = nu.convert(newConfig, "DEFAULT", n0);
        assertEquals("`TABLE`", cn0);
        
        // Test basic name conversion with single name converter - no 
        // conversion
        String n1 = "TABLE";
        String cn1 = nu.convert(newConfig, "DEFAULT", n1);
        assertEquals("TABLE", cn1);

        // Test basic name separator conversion with compound name converter
        String n2 = "TABLE.SCHEMA";
        String cn2 = nu.convertFull(newConfig, "DEFAULT", n2);
        assertEquals("TABLE:SCHEMA", cn2);

        String n3 = "\"TABLE\".\"SCHEMA\"";
        String cn3 = nu.convertFull(newConfig, "DEFAULT", n3);
        assertEquals("`TABLE`:`SCHEMA`", cn3);
    }
    
    
}
