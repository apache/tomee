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
package org.apache.openjpa.persistence.jdbc;

import java.sql.ResultSet;

/**
 * The fetch direction to request when creating statements.
 *
 * @since 1.0.0
 * @published
 */
public enum FetchDirection {
    FORWARD(ResultSet.FETCH_FORWARD),
    REVERSE(ResultSet.FETCH_REVERSE),
    UNKNOWN(ResultSet.FETCH_UNKNOWN);

    private final int resultSetConstant;

    private FetchDirection(int value) {
        resultSetConstant = value;
    }

    int toKernelConstant() {
        return resultSetConstant;
    }

    static FetchDirection fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case ResultSet.FETCH_FORWARD:
                return FORWARD;

            case ResultSet.FETCH_REVERSE:
                return REVERSE;

            case ResultSet.FETCH_UNKNOWN:
                return UNKNOWN;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
