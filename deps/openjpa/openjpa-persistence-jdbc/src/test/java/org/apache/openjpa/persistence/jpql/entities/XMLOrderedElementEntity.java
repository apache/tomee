/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.jpql.entities;

import java.util.ArrayList;
import java.util.List;

public class XMLOrderedElementEntity implements IOrderedElements, java.io.Serializable {

    private int id;

    private List<String> elements;  
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getListElements() {
        return elements;
    }

    public void setListElements(List<String> elements) {
        this.elements = elements;
    }

    public void addListElements(String element) {
        if( elements == null) {
            elements = new ArrayList<String>();
        }
        elements.add(element);
    }
    
    public String removeListElements(int location) {
        String rtnVal = null;
        if( elements != null) {
            rtnVal = elements.remove(location);
        }
        return rtnVal;
    }
    
    public void insertListElements(int location, String name) {
        if( elements == null) {
            elements = new ArrayList<String>();
        }
        elements.add(location, name);
    }

    public String toString() {
        return "XMLOrderedElementEntity[" + id + "]=" + elements;
    }
}
