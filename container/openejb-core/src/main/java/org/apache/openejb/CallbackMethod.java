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
package org.apache.openejb;

/**
 * @version $Revision$ $Date$
 */
public final class CallbackMethod {
    public final static CallbackMethod SET_CONTEXT = new CallbackMethod("setContext");
    public final static CallbackMethod UNSET_CONTEXT = new CallbackMethod("unsetContext");
    public final static CallbackMethod ACTIVATE = new CallbackMethod("activate");
    public final static CallbackMethod PASSIVATE = new CallbackMethod("passivate");
    public final static CallbackMethod LOAD = new CallbackMethod("load");
    public final static CallbackMethod STORE = new CallbackMethod("store");
    public final static CallbackMethod CREATE = new CallbackMethod("create");
    public final static CallbackMethod REMOVE = new CallbackMethod("remove");
    public final static CallbackMethod AFTER_BEGIN = new CallbackMethod("afterBegin");
    public final static CallbackMethod BEFORE_COMPLETION = new CallbackMethod("beforeCompletion");
    public final static CallbackMethod AFTER_COMPLETION = new CallbackMethod("afterCompletion");
    public final static CallbackMethod TIMEOUT = new CallbackMethod("timeout");

    private final String name;

    public CallbackMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
