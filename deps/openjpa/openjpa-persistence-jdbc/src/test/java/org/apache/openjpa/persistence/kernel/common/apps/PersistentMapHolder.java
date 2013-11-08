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
package org.apache.openjpa.persistence.kernel.common.apps;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.jdbc.KeyColumn;
import org.apache.openjpa.persistence.PersistentMap;

@Entity
public class PersistentMapHolder {

    @PersistentMap(keyCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCKeyStringValue")
    private Map<MapElementPC, String> testPCKeyStringValue =
        new HashMap<MapElementPC, String>();

    @PersistentMap(elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testStringKeyPCValue")
    private Map<String, MapElementPC> testStringKeyPCValue =
        new HashMap<String, MapElementPC>();

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCKeyPCValue")
    private Map<MapElementPC, MapElementPC> testPCKeyPCValue =
        new HashMap<MapElementPC, MapElementPC>();

    @PersistentMap(keyCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCSubKeyStringValue")
    private Map<MapElementPCChild, String> testPCSubKeyStringValue =
        new HashMap<MapElementPCChild, String>();

    @PersistentMap(elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testStringKeyPCSubValue")
    private Map<String, MapElementPCChild> testStringKeyPCSubValue =
        new HashMap<String, MapElementPCChild>();

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCSubKeyPCValue")
    private Map<MapElementPCChild, MapElementPC> testPCSubKeyPCValue =
        new HashMap<MapElementPCChild, MapElementPC>();

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCSubKeyPCSubValue")
    private Map<MapElementPCChild, MapElementPCChild> testPCSubKeyPCSubValue =
        new HashMap<MapElementPCChild, MapElementPCChild>();

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCKeyPCSubValue")
    private Map<MapElementPC, MapElementPCChild> testPCKeyPCSubValue =
        new HashMap<MapElementPC, MapElementPCChild>();

    @PersistentMap(keyCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCIntfKeyStringValue")
    private Map<MapElementIntf, String> testPCIntfKeyStringValue =
        new HashMap<MapElementIntf, String>();

    @PersistentMap(elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testStringKeyPCIntfValue")
    private Map<String, MapElementIntf> testStringKeyPCIntfValue =
        new HashMap<String, MapElementIntf>();

    @PersistentMap(keyCascade = CascadeType.PERSIST,
        elementCascade = CascadeType.PERSIST)
    @KeyColumn(name = "testPCIntfKeyPCValue")
    private Map<MapElementIntf, MapElementPC> testPCIntfKeyPCValue =
        new HashMap<MapElementIntf, MapElementPC>();

    public Map getNamedMap(String name) {
        if (name.equals("testPCKeyStringValue"))
            return testPCKeyStringValue;
        if (name.equals("testStringKeyPCValue"))
            return testStringKeyPCValue;
        if (name.equals("testPCKeyPCValue"))
            return testPCKeyPCValue;
        if (name.equals("testPCSubKeyStringValue"))
            return testPCSubKeyStringValue;
        if (name.equals("testStringKeyPCSubValue"))
            return testStringKeyPCSubValue;
        if (name.equals("testPCSubKeyPCValue"))
            return testPCSubKeyPCValue;
        if (name.equals("testPCSubKeyPCSubValue"))
            return testPCSubKeyPCSubValue;
        if (name.equals("testPCKeyPCSubValue"))
            return testPCKeyPCSubValue;
        if (name.equals("testPCIntfKeyStringValue"))
            return testPCIntfKeyStringValue;
        if (name.equals("testStringKeyPCIntfValue"))
            return testStringKeyPCIntfValue;
        if (name.equals("testPCIntfKeyPCValue"))
            return testPCIntfKeyPCValue;

        return null;
    }
}
