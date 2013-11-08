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

import org.apache.openjpa.kernel.DetachState;

/**
 * The possible settings for the detachment behavior of an
 * {@link OpenJPAEntityManager}.
 *
 * @since 1.0.0
 * @published
 */
public enum DetachStateType {
    FETCH_GROUPS(DetachState.DETACH_FETCH_GROUPS),
    LOADED(DetachState.DETACH_LOADED),
    ALL(DetachState.DETACH_ALL);

    private final int detachStateConstant;

    private DetachStateType(int value) {
        detachStateConstant = value;
    }

    int toKernelConstant() {
        return detachStateConstant;
    }

    static DetachStateType fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case DetachState.DETACH_FETCH_GROUPS:
                return FETCH_GROUPS;

            case DetachState.DETACH_LOADED:
                return LOADED;

            case DetachState.DETACH_ALL:
                return ALL;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
