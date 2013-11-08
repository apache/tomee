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
package org.apache.openjpa.persistence.access.xml;

public class XMLFieldEmbedEntity {

    private int id;
    
    private String name;

    private XMLEmbedPropAccess epa;
    
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public XMLEmbedPropAccess getEPA() {
        return epa;
    }

    public void setEPA(XMLEmbedPropAccess ep) {
        epa = ep;
    }

    public boolean equals(Object obj) {
        if (obj instanceof XMLFieldEmbedEntity) {
            XMLFieldEmbedEntity ps = (XMLFieldEmbedEntity)obj;
            return epa.equals(ps.getEPA()) && getId() == ps.getId() &&
                   getName().equals(ps.getName());
        }
        return false;
    }
}
