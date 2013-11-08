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
package org.apache.openjpa.jdbc.kernel.exps;

import org.apache.openjpa.kernel.Filters;

/**
 * Sum.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
class Sum
    extends NullableAggregateUnaryOp { // OPENJPA-1794

    /**
     * Constructor. Provide the value to operate on.
     */
    public Sum(Val val) {
        super(val);
    }

    protected Class getType(Class c) {
        Class wrap = Filters.wrap(c);
        if (wrap == Integer.class
            || wrap == Short.class
            || wrap == Byte.class)
            return long.class;
        return c;
    }

    protected String getOperator() {
        return "SUM";
    }

    public boolean isAggregate() {
        return true;
    }
}
