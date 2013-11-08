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
package org.apache.openjpa.slice;

import java.io.Serializable;

import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * Represents a database slice of immutable logical name, a configuration and
 * status. A Slice is uniquely identified by its logical name.
 * 
 * @author Pinaki Poddar 
 *
 */
@SuppressWarnings("serial")
public class Slice implements Comparable<Slice>,Serializable {
    public enum Status {
        NOT_INITIALIZED, 
        ACTIVE, 
        INACTIVE, // configured but not available
        EXCLUDED  // configured but not used
    }; 
    
    private final String name;
    private transient final OpenJPAConfiguration conf;
    private transient Status status;
    
    /**
     * Supply the logical name and configuration.
     */
    public Slice(String name, OpenJPAConfiguration conf) {
        this.name = name;
        this.conf = conf;
        this.status = Status.NOT_INITIALIZED;
    }
    
    /**
     * Gets the immutable logical name.
     */
    public String getName() {
        return name;
    }
    
    public OpenJPAConfiguration getConfiguration() {
        return conf;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
    
    public String toString() {
        return name;
    }
    
    public int compareTo(Slice other) {
        return name.compareTo(other.name);
    }
    
    /**
     * Equals by name.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (other instanceof Slice) {
            return name.equals(((Slice)other).getName());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
