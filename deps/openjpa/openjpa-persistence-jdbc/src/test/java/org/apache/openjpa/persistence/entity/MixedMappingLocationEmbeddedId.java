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
package org.apache.openjpa.persistence.entity;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Transient;

public class MixedMappingLocationEmbeddedId implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;
    @Basic
    private int id;
    @Basic
    private String country;

    private String basic10;
    
    public MixedMappingLocationEmbeddedId() {
        // TODO Auto-generated constructor stub
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }
}
