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
package org.apache.openejb.core.transaction;

import javax.ejb.TransactionAttributeType;

public enum TransactionType {
    Mandatory,
    Never,
    NotSupported,
    Required,
    RequiresNew,
    Supports,
    BeanManaged;

    public static TransactionType get(TransactionAttributeType type) {
        switch (type) {
            case REQUIRED: return Required;
            case REQUIRES_NEW: return RequiresNew;
            case MANDATORY: return Mandatory;
            case NEVER: return Never;
            case NOT_SUPPORTED: return NotSupported;
            case SUPPORTS: return Supports;
            default: throw new IllegalArgumentException("Uknown TransactionAttributeType."+ type);
        }
    }

    public static TransactionType get(String name) {
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(name)) return type;
        }

        throw new IllegalArgumentException("Uknown TransactionType " + name);
    }
}
