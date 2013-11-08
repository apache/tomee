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

public class NonPKJoinPC3
    extends NonPKJoinPC2 {

    private PartialJoinPC3 partial3;
    private Set partial2s = new HashSet();

    public PartialJoinPC3 getPartial3() {
        return this.partial3;
    }

    public void setPartial3(PartialJoinPC3 partial3) {
        this.partial3 = partial3;
    }

    public Set getPartial2s() {
        return this.partial2s;
    }

    public void setPartial2s(Set partial2s) {
        this.partial2s = partial2s;
    }
}
