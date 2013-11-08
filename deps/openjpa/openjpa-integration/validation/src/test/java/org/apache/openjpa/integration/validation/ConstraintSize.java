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
package org.apache.openjpa.integration.validation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;


@Entity(name = "VSIZE")
@Table(name = "SIZE_ENTITY")
public class ConstraintSize implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private long id;

    @Basic
    @Size(min = 0, max = 10)
    private String myString;

    private Map<String,String> myMap;  // @Size(1,2) constraint is on the getter

    
    /* 
     * Some helper methods to create the entities to test with
     */
    public static ConstraintSize createInvalidString() {
        ConstraintSize c = new ConstraintSize();
        c.setMyString("abcdefghijklmno");
        c.setValidMap();
        return c;
    }

    public static ConstraintSize createInvalidMap() {
        ConstraintSize c = new ConstraintSize();
        c.setMyString("");
        c.setInvalidMap();
        return c;
    }

    public static ConstraintSize createInvalidSize() {
        ConstraintSize c = new ConstraintSize();
        c.setMyString("abcdefghijklmno");
        c.setInvalidMap();
        return c;
    }

    public static ConstraintSize createValid() {
        ConstraintSize c = new ConstraintSize();
        c.setMyString("abc");
        c.setValidMap();
        return c;
    }

    
    /*
     * Main entity code
     */
    public ConstraintSize() {
    }

    public long getId() {
        return id;
    }

    public String getMyString() {
        return myString;
    }

    public void setMyString(String s) {
        myString = s;
    }

    @Size(min = 1, max = 2)
    public Map<String,String> getMyMap() {
        return myMap;
    }

    public void setMyMap(Map<String,String> m) {
        myMap = m;
    }
    
    
    private void setInvalidMap() {
        Map<String,String> m = new HashMap<String,String>();
        m.put("a", "a value");
        m.put("b", "b value");
        m.put("c", "c value");
        setMyMap(m);
    }
    
    private void setValidMap() {
        Map<String,String> m = new HashMap<String,String>();
        m.put("a", "a value");
        setMyMap(m);
    }
    
}
