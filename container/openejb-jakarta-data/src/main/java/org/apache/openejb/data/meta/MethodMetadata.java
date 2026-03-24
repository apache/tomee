/*
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
package org.apache.openejb.data.meta;

import org.apache.openejb.data.query.MethodNameParser;

public class MethodMetadata {

    public enum Strategy {
        BUILTIN,
        METHOD_NAME
    }

    private final Strategy strategy;
    private final MethodNameParser.ParsedQuery parsedQuery;

    public MethodMetadata(final Strategy strategy, final MethodNameParser.ParsedQuery parsedQuery) {
        this.strategy = strategy;
        this.parsedQuery = parsedQuery;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public MethodNameParser.ParsedQuery getParsedQuery() {
        return parsedQuery;
    }
}
