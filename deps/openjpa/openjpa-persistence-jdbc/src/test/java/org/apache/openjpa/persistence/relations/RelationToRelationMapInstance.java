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
package org.apache.openjpa.persistence.relations;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.openjpa.persistence.PersistentMap;
import org.apache.openjpa.persistence.simple.AllFieldTypes;

@Entity
public class RelationToRelationMapInstance {
    @Id
    private int id;

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    private Map<AllFieldTypes,AllFieldTypes> map =
        new HashMap<AllFieldTypes,AllFieldTypes>();

    public Map<AllFieldTypes,AllFieldTypes> getMap() {
        return map;
    }
}
