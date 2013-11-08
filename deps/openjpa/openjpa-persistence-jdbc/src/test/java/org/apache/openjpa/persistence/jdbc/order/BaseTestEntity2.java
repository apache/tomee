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
package org.apache.openjpa.persistence.jdbc.order;

import java.util.List;
import java.util.Set;

/*
 * Entity used for testing custom column defintions base values.  
 */
public class BaseTestEntity2 {

    private int id;
    
    private List<BaseTestElement2> one2Melems;
        
    private List<BaseTestElement2> m2melems;

    private Set<BaseTestElement2> collelems;

    public void setOne2Melems(List<BaseTestElement2> one2Melems) {
        this.one2Melems = one2Melems;
    }

    public List<BaseTestElement2> getOne2Melems() {
        return one2Melems;
    }

    public void setCollelems(Set<BaseTestElement2> collelems) {
        this.collelems = collelems;
    }

    public Set<BaseTestElement2> getCollelems() {
        return collelems;
    }

    public void setM2melems(List<BaseTestElement2> m2melems) {
        this.m2melems = m2melems;
    }

    public List<BaseTestElement2> getM2melems() {
        return m2melems;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }    
}
