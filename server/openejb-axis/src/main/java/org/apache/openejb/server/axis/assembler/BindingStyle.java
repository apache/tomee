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
package org.apache.openejb.server.axis.assembler;

public enum BindingStyle {
    RPC_LITERAL(false, true, false),
    RPC_ENCODED(false, false, false),
    DOCUMENT_LITERAL(true, true, false),
    DOCUMENT_ENCODED(true, false, false),
    DOCUMENT_LITERAL_WRAPPED(true, true, true);

    private final boolean document;
    private final boolean literal;
    private final boolean wrapped;

    BindingStyle(boolean document, boolean literal, boolean wrapped) {
        this.document = document;
        this.literal = literal;
        this.wrapped = wrapped;
    }

    public boolean isRpc() {
        return !document;
    }

    public boolean isDocument() {
        return document;
    }

    public boolean isEncoded() {
        return !literal;
    }

    public boolean isLiteral() {
        return literal;
    }

    public boolean isWrapped() {
        return wrapped;
    }

    public static BindingStyle getBindingStyle(String style, String use) {
        if ("rpc".equalsIgnoreCase(style)) {
            if (use == null ||"encoded".equalsIgnoreCase(use)) {
                return RPC_ENCODED;
            } else if ("literal".equalsIgnoreCase(use)) {
                return RPC_LITERAL;
            } else {
                throw new IllegalArgumentException("Use must be literal or encoded: " + use);
            }
        } else if ("document".equalsIgnoreCase(style)) {
            if (use == null || "encoded".equalsIgnoreCase(use)) {
                return DOCUMENT_ENCODED;
            } else if ("literal".equalsIgnoreCase(use)) {
                return DOCUMENT_LITERAL;
            } else {
                throw new IllegalArgumentException("Use must be literal or encoded: " + use);
            }
        } else {
            throw new IllegalArgumentException("Style must rpc or document: " + style);
        }
    }
}
