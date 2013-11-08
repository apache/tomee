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

import org.apache.openjpa.kernel.RestoreState;

/**
 * The possible settings for the restore behavior after transaction rollback
 * of an {@link OpenJPAEntityManager}.
 *
 * @since 1.0.0
 * @published
 */
public enum RestoreStateType {
    NONE(RestoreState.RESTORE_NONE),
    IMMUTABLE(RestoreState.RESTORE_IMMUTABLE),
    ALL(RestoreState.RESTORE_ALL);

    private final int restoreStateConstant;

    private RestoreStateType(int value) {
        restoreStateConstant = value;
    }

    int toKernelConstant() {
        return restoreStateConstant;
    }

    static RestoreStateType fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case RestoreState.RESTORE_NONE:
                return NONE;

            case RestoreState.RESTORE_IMMUTABLE:
                return IMMUTABLE;

            case RestoreState.RESTORE_ALL:
                return ALL;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
