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
package org.apache.openejb.config;

import junit.framework.TestCase;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentLoaderTest extends TestCase {

    public void testProcessesAltDdsMethod() throws Exception {
        /* We start with a directory structure like so:
         *
         * - META-INF/ejb-jar.xml
         * - META-INF/test.ejb-jar.xml
         * - META-INF/footest.ejb-jar.xml
         * - META-INF/bartest.ejb-jar.xml
         * - META-INF/persistence.xml
         * - META-INF/test.env-entry.properties
         */
        Map<String, URL> original = new HashMap<String, URL>();
        original.put("ejb-jar.xml", new File("META-INF/ejb-jar.xml").toURI().toURL());
        original.put("test.ejb-jar.xml", new File("META-INF/test.ejb-jar.xml").toURI().toURL());
        original.put("footest.ejb-jar.xml", new File("META-INF/footest.ejb-jar.xml").toURI().toURL());
        original.put("bartest.ejb-jar.xml", new File("META-INF/bartest.ejb-jar.xml").toURI().toURL());
        original.put("persistence.xml", new File("META-INF/persistence.xml").toURI().toURL());
        original.put("test.env-entry.properties", new File("META-INF/test.env-entry.properties").toURI().toURL());

        SystemInstance.get().setProperty("openejb.altdd.prefix", "footest, test");

        Map<String, URL> processed = new HashMap<String, URL>(original);
        DeploymentLoader.altDDSources(processed, false);

        // an item that existed and should have been replaced
        assertEquals(processed.get("ejb-jar.xml"), original.get("footest.ejb-jar.xml"));

        // an item that didn't exist and should have been added
        assertEquals(processed.get("env-entry.properties"), original.get("test.env-entry.properties"));

        // items that shouldn't have been affected
        assertEquals(processed.get("persistence.xml"), original.get("persistence.xml"));
        assertEquals(processed.get("test.ejb-jar.xml"), original.get("test.ejb-jar.xml"));
        assertEquals(processed.get("footest.ejb-jar.xml"), original.get("footest.ejb-jar.xml"));
        assertEquals(processed.get("bartest.ejb-jar.xml"), original.get("bartest.ejb-jar.xml"));
        assertEquals(processed.get("test.env-entry.properties"), original.get("test.env-entry.properties"));

        // there should be no other additional entries
        assertEquals(7, processed.size());
    }
}
