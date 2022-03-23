/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.cmp.jpa;

import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@IdClass(ComplexId.class)
public class ComplexSuperclass {
    @Id
    public String firstId;
    @Id
    public String secondId;


    public ComplexSuperclass() {
    }

    public ComplexSuperclass(final String firstId, final String secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }

    public void initializeIds(final String firstId, final String secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }

    public String getFirstId() {
        return firstId;
    }

    public void setFirstId(final String firstId) {
        this.firstId = firstId;
    }

    public String getSecondId() {
        return secondId;
    }

    public void setSecondId(final String secondId) {
        this.secondId = secondId;
    }
}
