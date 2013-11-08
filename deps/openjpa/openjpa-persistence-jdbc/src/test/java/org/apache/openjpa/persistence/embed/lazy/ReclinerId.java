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
package org.apache.openjpa.persistence.embed.lazy;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ReclinerId {

    private int id;
    private String color;

    public void setId(int id) {
        this.id = id;
    }

    @Column(name="RECID_ID")
    public int getId() {
        return id;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Column(name="RECID_COLOR")
    public String getColor() {
        return color;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReclinerId) {
            ReclinerId rid = (ReclinerId)obj;
            return rid.id == id &&
                rid.color.equals(color);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id ^ color.hashCode();
    }
}
