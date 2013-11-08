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

import org.apache.openjpa.jdbc.sql.JoinSyntaxes;

/**
 * Type of join syntax to use.
 *
 * @since 1.0.0
 * @published
 */
public enum JoinSyntax {
    SQL92(JoinSyntaxes.SYNTAX_SQL92),
    TRADITIONAL(JoinSyntaxes.SYNTAX_TRADITIONAL),
    DATABASE(JoinSyntaxes.SYNTAX_DATABASE);

    private final int joinSyntaxConstant;

    private JoinSyntax(int value) {
        joinSyntaxConstant = value;
    }

    int toKernelConstant() {
        return joinSyntaxConstant;
    }

    static JoinSyntax fromKernelConstant(int kernelConstant) {
        switch (kernelConstant) {
            case JoinSyntaxes.SYNTAX_SQL92:
                return SQL92;

            case JoinSyntaxes.SYNTAX_TRADITIONAL:
                return TRADITIONAL;

            case JoinSyntaxes.SYNTAX_DATABASE:
                return DATABASE;

            default:
                throw new IllegalArgumentException(kernelConstant + "");
        }
    }
}
