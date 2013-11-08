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

import org.apache.openjpa.kernel.ConnectionRetainModes;

/**
 * The possible values for use when configuring the connection retain
 * behavior for an {@link OpenJPAEntityManager}.
 *
 * @since 1.0.0
 * @published
 */
public enum ConnectionRetainMode {
    ON_DEMAND(ConnectionRetainModes.CONN_RETAIN_DEMAND),
    TRANSACTION(ConnectionRetainModes.CONN_RETAIN_TRANS),
    ALWAYS(ConnectionRetainModes.CONN_RETAIN_ALWAYS);

    private final int connectionRetainConstant;

    private ConnectionRetainMode(int value) {
        connectionRetainConstant = value;
    }

    int toKernelConstant() {
        return connectionRetainConstant;
    }

    static ConnectionRetainMode fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case ConnectionRetainModes.CONN_RETAIN_DEMAND:
                return ON_DEMAND;

            case ConnectionRetainModes.CONN_RETAIN_ALWAYS:
                return ALWAYS;

            case ConnectionRetainModes.CONN_RETAIN_TRANS:
                return TRANSACTION;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
