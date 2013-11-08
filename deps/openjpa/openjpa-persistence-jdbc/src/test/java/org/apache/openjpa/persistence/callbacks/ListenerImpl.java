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

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;

public class ListenerImpl {

    static int prePersistCount;
    static int postPersistCount;
    static int preUpdateCount;
    static int postUpdateCount;
    static int preRemoveCount;
    static int postRemoveCount;
    static int postLoadCount;

    @PrePersist
    public void prePersist(Object o) {
        prePersistCount++;
    }

    @PostPersist
    public void postPersist(Object o) {
        postPersistCount++;
    }

    @PostLoad
    public void postLoad(Object o) {
        postLoadCount++;
    }

    // dummy methods for testing OPENJPA-2197
    public void postLoad(int someotherValue, String dummyParameter) {
        // do nothing. This just breaks the other method ... ;)
    }
    public void postLoad(int someotherValue) {
        // do nothing. This just breaks the other method ... ;)
    }

    @PreUpdate
    public void preUpdate(Object o) {
        preUpdateCount++;
    }

    @PostUpdate
    public void postUpdate(Object o) {
        postUpdateCount++;
    }

    @PreRemove
    public void preRemove(Object o) {
        preRemoveCount++;
    }

    @PostRemove
    public void postRemove(Object o) {
        postRemoveCount++;
    }
}
