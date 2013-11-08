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
package org.apache.openjpa.persistence.jdbc.maps.update;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;


@Entity
@Table(name = "multilingual_string")
public class MultilingualString {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "string_id")
    private long id;

    @ElementCollection(fetch=FetchType.EAGER)
    @MapKeyColumn(name = "language_key")
    @CollectionTable(name = "multilingual_string_map", joinColumns = @JoinColumn(name = "string_id"))
    private Map<String, LocalizedString> map = new HashMap<String, LocalizedString>();

    public MultilingualString() {}
    
    public MultilingualString(String lang, String text) {
        setText(lang, text);
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Map<String, LocalizedString> getMap() {
        return map;
    }

    public void setMap(Map<String, LocalizedString> map) {
        this.map = map;
    }

    public void setText(String lang, String text) {
        map.put(lang, new LocalizedString(lang, text));
    }

    public String getText(String lang) {
        if (map.containsKey(lang)) {
            return map.get(lang).getString();
        }
        return null;
    }
}
