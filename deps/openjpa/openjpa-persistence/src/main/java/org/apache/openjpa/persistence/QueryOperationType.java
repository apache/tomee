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

import org.apache.openjpa.kernel.QueryOperations;

/**
 * The possible operations that a query can perform.
 *
 * @since 1.0.0
 * @published
 */
public enum QueryOperationType {
    SELECT(QueryOperations.OP_SELECT),
    DELETE(QueryOperations.OP_DELETE),
    UPDATE(QueryOperations.OP_UPDATE);

    private final int queryOperationConstant;

    private QueryOperationType(int value) {
        queryOperationConstant = value;
    }

    int toKernelConstant() {
        return queryOperationConstant;
    }

    static QueryOperationType fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case QueryOperations.OP_SELECT:
                return SELECT;

            case QueryOperations.OP_UPDATE:
                return UPDATE;

            case QueryOperations.OP_DELETE:
                return DELETE;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
