/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core;

public enum Operation {
    INJECTION(true),
    POST_CONSTRUCT(true),
    BUSINESS(false),
    BUSINESS_WS(false),
    TIMEOUT(true),
    AFTER_BEGIN(true),
    AFTER_COMPLETION(true),
    BEFORE_COMPLETION(true),
    PRE_DESTROY(true),
    REMOVE(false),
    SET_CONTEXT(true),
    UNSET_CONTEXT(true),
    CREATE(true),
    POST_CREATE(true),
    ACTIVATE(true),
    PASSIVATE(true),
    FIND(true),
    HOME(true),
    LOAD(true),
    STORE(true);

    private boolean callback;


    Operation(boolean callback) {
        this.callback = callback;
    }


    public boolean isCallback() {
        return callback;
    }
}