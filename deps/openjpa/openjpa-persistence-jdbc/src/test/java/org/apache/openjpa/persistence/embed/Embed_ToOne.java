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
package org.apache.openjpa.persistence.embed;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;

@Embeddable 
public class Embed_ToOne {
    protected String name1;
    protected String name2;
    protected String name3;
    @OneToOne(cascade=CascadeType.ALL)
    protected EntityB1 b;
    
    public String getName1() {
        return name1;
    }
    
    public void setName1(String name1) {
        this.name1 = name1;
    }
    
    public String getName2() {
        return name2;
    }
    
    public void setName2(String name2) {
        this.name2 = name2;
    }
    
    public String getName3() {
        return name3;
    }
    
    public void setName3(String name3) {
        this.name3 = name3;
    }
    
    public void setEntityB(EntityB1 b) {
        this.b = b;
    }
    
    public EntityB1 getEntityB() {
        return b;
    }
    
}
