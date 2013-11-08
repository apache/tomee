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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import javax.persistence.PreRemove;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PostPersist;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

@Entity
public class ExceptionsFromCallbacksEntity {
    @Id @GeneratedValue private long id;
    @Version private int version;
    @Transient private boolean throwOnPrePersist;
    @Transient private boolean throwOnPostPersist;
    @Transient private boolean throwOnPreUpdate;
    @Transient private boolean throwOnPostUpdate;
    private boolean throwOnPostLoad;
    @Transient private boolean throwOnPreRemove;
    @Transient private boolean throwOnPostRemove;
    private String stringField;

    public void setThrowOnPrePersist(boolean b) {
        throwOnPrePersist = b;
    }

    public void setThrowOnPostPersist(boolean b) {
        throwOnPostPersist = b;
    }

    public void setThrowOnPreUpdate(boolean b) {
        throwOnPreUpdate = b;
    }

    public void setThrowOnPostUpdate(boolean b) {
        throwOnPostUpdate = b;
    }

    public void setThrowOnPostLoad(boolean b) {
        throwOnPostLoad = b;
    }

    public void setThrowOnPreRemove(boolean b) {
        throwOnPreRemove = b;
    }

    public void setThrowOnPostRemove(boolean b) {
        throwOnPostRemove = b;
    }

    public void setStringField(String s) {
        stringField = s;
    }

    @PrePersist
    public void prePersist() {
        if (throwOnPrePersist)
            throw new CallbackTestException();
    }

    @PostPersist
    public void postPersist() {
        if (throwOnPostPersist)
            throw new CallbackTestException();
    }

    @PostLoad
    public void postLoad() {
        if (throwOnPostLoad && isInvokedFromTestMethod())
            throw new CallbackTestException();
    }

    private boolean isInvokedFromTestMethod() {
        return TestExceptionsFromCallbacks.testRunning;
    }

    @PreUpdate
    public void preUpdate() {
        if (throwOnPreUpdate)
            throw new CallbackTestException();
    }

    @PostUpdate
    public void postUpdate() {
        if (throwOnPostUpdate)
            throw new CallbackTestException();
    }

    @PreRemove
    public void preRemove() {
        if (throwOnPreRemove && isInvokedFromTestMethod())
            throw new CallbackTestException();
    }

    @PostRemove
    public void postRemove() {
        if (throwOnPostRemove && isInvokedFromTestMethod())
            throw new CallbackTestException();
    }

    public Object getId() {
        return id;
    }

    public class CallbackTestException
        extends RuntimeException {
    }
}
