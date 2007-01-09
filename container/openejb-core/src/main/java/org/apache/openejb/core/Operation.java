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
    OP_BUSINESS,
    OP_AFTER_BEGIN,
    OP_AFTER_COMPLETION,
    OP_BEFORE_COMPLETION,
    OP_REMOVE,
    OP_SET_CONTEXT,
    OP_UNSET_CONTEXT,
    OP_CREATE,
    OP_POST_CREATE,
    OP_ACTIVATE,
    OP_PASSIVATE,
    OP_FIND,
    OP_HOME,
    OP_LOAD,
    OP_STORE
}