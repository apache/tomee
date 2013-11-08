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
package org.apache.openjpa.persistence.datacache;

/**
 * Extend the single-JVM cache test to test dynamic pcdata.
 */
public class TestDataCachePCDataGenerator
    extends TestSJVMCache {

    public TestDataCachePCDataGenerator(String test) {
        super(test);
    }

    protected String[] getConfs() {
        return addGenerator(super.getConfs());
    }

    protected String[] getConfs2() {
        return addGenerator(super.getConfs2());
    }

    private String[] addGenerator(String[] confs) {
        String[] copy = new String[confs.length + 2];
        System.arraycopy(confs, 0, copy, 0, confs.length);
        copy[confs.length] = "openjpa.DynamicDataStructs";
        copy[confs.length + 1] = "true";
        return copy;
    }
}
