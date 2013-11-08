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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKeyTemporal;
import javax.persistence.TemporalType;

@Entity
public class Item5 {
    @Id
    int id;
    
    @ElementCollection
    @MapKeyTemporal(TemporalType.TIME)
    Map<Timestamp, FileName4> images = new HashMap<Timestamp, FileName4>();
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Map<Timestamp, FileName4> getImages() {
        return images;
    }
    
    public void addImage(Timestamp ts, FileName4 fileName) {
        images.put(ts, fileName);
    }
    
    public void removeImage(Timestamp ts) {
        images.remove(ts);
    }
    
    public FileName4 getImage(Timestamp ts) {
        return images.get(ts);
    }
    
}
