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

public class MultiC
    extends MultiB {

    private String cString0;
    private String cString1;

    private int cInt;

    private Set multiAs = new HashSet();

    public void setCString0(String val) {
        cString0 = val;
    }

    public String getCString0() {
        return cString0;
    }

    public void setMultiAs(Set val) {
        multiAs = val;
    }

    public Set getMultiAs() {
        return multiAs;
    }
}
