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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.util.*;

public class MultiA {

    private String string0;
    private String string1;

    private int aint0;
    private int aint1;

    private Set multiCs = new HashSet();

    public void setString0(String val) {
        string0 = val;
    }

    public String getString0() {
        return string0;
    }

    public void setInt0(int val) {
        aint0 = val;
    }

    public void setString1(String val) {
        string1 = val;
    }

    public String getString1() {
        return string1;
    }

    public int getInt0() {
        return aint0;
    }

    public void setInt1(int val) {
        aint1 = val;
    }

    public int getInt1() {
        return aint1;
    }

    public void setMultiCs(Set val) {
        multiCs = val;
    }

    public Set getMultiCs() {
        return multiCs;
    }
}
