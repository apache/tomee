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
package org.apache.openjpa.jdbc.kernel;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;
import org.apache.openjpa.persistence.jdbc.*;
import org.apache.openjpa.persistence.*;

@Entity
@Table(name = "JPA_A")
public class A {

    @Id
    @Column(name = "ID", nullable = false)
    private int id;

    @Column(name = "NAME")
    private String name;
    
    @Column(name = "AGE")
    private int age;
    
    @PersistentMap(keyType = String.class,
        elementType = String.class)
    @ContainerTable(name="JPA_A_MAPS_C",
         joinColumns = @XJoinColumn(name = "MAP_ID"))
    @KeyColumn(name="MAP_KEY")
    @ElementColumn(name="MAP_VALUE")
    private Map<String,String> map = new HashMap<String,String>();

    public A() {
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    public Map<String,String> getMap() {
        return this.map;
    }

    public void setMap(Map<String,String> map) {
        this.map = map;
    }

}
