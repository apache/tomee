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
package org.apache.openjpa.ee;

import junit.framework.TestCase;

import serp.util.Strings;

/**
 * Test class for build transformation performed by WASManagedRuntime.
 *
 */
public class TestWASManagedRuntime extends TestCase {

    /**
     * This test will verify that the WASManagedRuntime$WASSynchronization
     * class was properly modified by the maven build process (reference
     * the top level pom.xml).  This testcase will not execute properly
     * within Eclipse since the Eclipse target directory (probably) hasn't
     * been modified via the maven build.
     *
     * @throws ClassNotFoundException
     */
    public void testInterfaceAdded() throws ClassNotFoundException {

        String msg = null;

        try {
            Class.forName(WASManagedRuntime.CLASS);
            fail("expected an exception to be thrown");
        } catch (NoClassDefFoundError e) {
            msg = e.getMessage();
        }
        String interfaceName = Strings.
            getClassName(WASManagedRuntime.INTERFACE);
        assertTrue("message should have contained "
            + interfaceName + ", but was '" + msg + "'",
            msg.indexOf(interfaceName) != -1);
    }
}
