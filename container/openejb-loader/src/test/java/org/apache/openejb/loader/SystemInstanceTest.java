/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.loader;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class SystemInstanceTest extends TestCase {

    public void testPropertiesInheritance() {
        Properties system = new Properties();
        system.setProperty("color", "red");
        system.setProperty("shape", "round");
        system.setProperty("texture", "matte");

        Properties systemInstance = new Properties(system);
        systemInstance.setProperty("color", "orange");
        systemInstance.setProperty("weight", "15");
        systemInstance.setProperty("height", "2");

        // The good parts
        assertEquals("SystemInstance.getProperty(\"shape\")", "round", systemInstance.getProperty("shape"));
        assertEquals("SystemInstance.getProperty(\"texture\")", "matte", systemInstance.getProperty("texture"));
        assertEquals("SystemInstance.getProperty(\"color\")", "orange", systemInstance.getProperty("color"));
        assertEquals("SystemInstance.getProperty(\"weight\")", "15", systemInstance.getProperty("weight"));
        assertEquals("SystemInstance.getProperty(\"height\")", "2", systemInstance.getProperty("height"));

        ArrayList<?> names = Collections.list(systemInstance.propertyNames());
        assertEquals("Names.size()", 5, names.size());

        // update "system" and check "systemInstance"
        system.setProperty("shape", "square");
        assertEquals("SystemInstance.getProperty(\"shape\")", "square", systemInstance.getProperty("shape"));

        // The bad, all java.util.Map methods do not reflect this inheritance
        assertEquals("SystemInstance.size()", 3, systemInstance.size());
        assertEquals("SystemInstance.get(\"shape\")", null, systemInstance.get("shape"));
        assertEquals("SystemInstance.get(\"texture\")", null, systemInstance.get("texture"));
        assertEquals("SystemInstance.get(\"color\")", "orange", systemInstance.get("color"));
        assertEquals("SystemInstance.get(\"weight\")", "15", systemInstance.get("weight"));
        assertEquals("SystemInstance.get(\"height\")", "2", systemInstance.get("height"));

    }
}
