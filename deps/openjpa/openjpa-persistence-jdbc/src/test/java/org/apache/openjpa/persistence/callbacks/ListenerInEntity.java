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
package org.apache.openjpa.persistence.callbacks;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Entity
public class ListenerInEntity {
    @Id @GeneratedValue
    private long id;

    private int value;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    transient int prePersistCount;
    transient int postPersistCount;
    transient int preUpdateCount;
    transient int postUpdateCount;
    transient int preRemoveCount;
    transient int postRemoveCount;
    transient int postLoadCount;

    @PrePersist
    public void prePersist() {
        prePersistCount++;
    }

    @PostPersist
    public void postPersist() {
        postPersistCount++;
    }

    @PostLoad
    public void postLoad() {
        postLoadCount++;
    }

    @PreUpdate
    public void preUpdate() {
        preUpdateCount++;
    }

    @PostUpdate
    public void postUpdate() {
        postUpdateCount++;
    }

    @PreRemove
    public void preRemove() {
        preRemoveCount++;
    }

    @PostRemove
    public void postRemove() {
        postRemoveCount++;
    }
}
