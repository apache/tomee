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

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PostPersist;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;

public class MessageListenerImpl {

    public static int prePersistCount;
    public static int postPersistCount;
    public static int preUpdateCount;
    public static int postUpdateCount;
    public static int preRemoveCount;
    public static int postRemoveCount;
    public static int postLoadCount;

    @PrePersist
    public void prePersist(Object o) {
        prePersistCount++;

        if (o instanceof Message) {
            ((Message) o).setCreated(new Date());
            ((Message) o).setUpdated(new Date());
        }
    }

    @PostPersist
    public void postPersist(Object o) {
        postPersistCount++;
    }

    @PostLoad
    public void postLoad(Object o) {
        postLoadCount++;
    }

    @PreUpdate
    public void preUpdate(Object o) {
        preUpdateCount++;

        if (o instanceof Message) {
            ((Message) o).setUpdated(new Date());
        }
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

    public static void resetCounters() {
        prePersistCount = 0;
        postPersistCount = 0;
        preUpdateCount = 0;
        postUpdateCount = 0;
        preRemoveCount = 0;
        postRemoveCount = 0;
        postLoadCount = 0;
    }

    public static String getStates() {
        return "prePersistCount = " + prePersistCount + ", postPersistCount = "
            + postPersistCount + ", preUpdateCount = " + preUpdateCount
            + ", postUpdateCount = " + postUpdateCount + ", preRemoveCount = "
            + preRemoveCount + ", postRemoveCount = " + postRemoveCount
            + ", postLoadCount = " + postLoadCount;
    }
}
