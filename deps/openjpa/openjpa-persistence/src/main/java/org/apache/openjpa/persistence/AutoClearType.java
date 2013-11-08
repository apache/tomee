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
package org.apache.openjpa.persistence;

import org.apache.openjpa.kernel.AutoClear;

/**
 * The possible settings for the auto-clear behavior of an
 * {@link OpenJPAEntityManager}.
 *
 * @since 1.0.0
 * @published
 */
public enum AutoClearType {
    DATASTORE(AutoClear.CLEAR_DATASTORE),
    ALL(AutoClear.CLEAR_ALL);

    private final int autoClearConstant;

    private AutoClearType(int value) {
        autoClearConstant = value;
    }

    int toKernelConstant() {
        return autoClearConstant;
    }

    static AutoClearType fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case AutoClear.CLEAR_DATASTORE:
                return DATASTORE;

            case AutoClear.CLEAR_ALL:
                return ALL;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
