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
public class BaseTestEntity {

    private int id;
    
    private List<BaseTestElement> one2Melems;
        
    private List<BaseTestElement> m2melems;

    private Set<BaseTestElement> collelems;

    public void setOne2Melems(List<BaseTestElement> one2Melems) {
        this.one2Melems = one2Melems;
    }

    public List<BaseTestElement> getOne2Melems() {
        return one2Melems;
    }

    public void setCollelems(Set<BaseTestElement> collelems) {
        this.collelems = collelems;
    }

    public Set<BaseTestElement> getCollelems() {
        return collelems;
    }

    public void setM2melems(List<BaseTestElement> m2melems) {
        this.m2melems = m2melems;
    }

    public List<BaseTestElement> getM2melems() {
        return m2melems;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }    
}
