/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.server.axis2;

import java.io.File;

import junit.framework.TestCase;

public abstract class Axis2AbstractTestCase extends TestCase {
    protected String testDir = "src/test/";
    protected String sampleDir = "src/main/test/samples/";
    protected String outDir = "target/generated/samples/";
    protected String tempDir = "target/generated/temp";
    
    /**
     * Constructor.
     */
    public Axis2AbstractTestCase(String testName) {
        super(testName);
        
        testDir = new File(testDir).getAbsolutePath();
        sampleDir = new File(sampleDir).getAbsolutePath();
        outDir = new File(outDir).getAbsolutePath();
        tempDir = new File(tempDir).getAbsolutePath();
    }

    /**
     * Get test input file.
     *
     * @param path Path to test input file.
     */
    public String getTestFile(String path) {
        return new File(path).getAbsolutePath();
    }

    public void testDummy() throws Exception {
        //to allow commenting out all tests in a test case
    }
}

