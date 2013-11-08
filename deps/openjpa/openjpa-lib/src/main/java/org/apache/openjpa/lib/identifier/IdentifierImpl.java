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
package org.apache.openjpa.lib.identifier;

import java.io.Serializable;

/**
 * Base identifer implementation.
 */
public class IdentifierImpl implements Identifier, Serializable {
    
    private String _name = null;
    
    protected IdentifierImpl() {}
    
    public IdentifierImpl(String name) {
        setName(name);
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        if (_name == null) {
            return super.hashCode();
        }
        return _name.toUpperCase().hashCode();
    }

    public int length() {
        if (getName() == null) {
            return 0;
        }
        return getName().length();
    }

    public int compareTo(Identifier o) {
        if (_name == null && (o == null || o.getName() == null)) {
            return 0;
        }
        if (_name == null)
            return 1;
        if (o == null || o.getName() == null)
            return -1;
        return _name.compareTo(o.getName());
    }
}
