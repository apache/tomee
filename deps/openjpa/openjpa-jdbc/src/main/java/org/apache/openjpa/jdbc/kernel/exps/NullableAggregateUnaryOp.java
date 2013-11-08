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

/**
 * OPENJPA-1794
 * An aggregate unary operation that can indicate whether a null value from the data store
 * should be returned as null.
 */
@SuppressWarnings("serial")
public abstract class NullableAggregateUnaryOp extends UnaryOp {

    public NullableAggregateUnaryOp(Val val) {
        super(val);
    }

    public NullableAggregateUnaryOp(Val val, boolean noParen) {
        super(val, noParen);
    }

    @Override
    protected boolean nullableValue(ExpContext ctx, ExpState state) {
        // If this is a simple operator (no joins involved), check compatibility to determine
        // whether 'null' should be returned for the aggregate operation
        if (ctx != null && ctx.store != null && (state.joins == null || state.joins.isEmpty())) {
            return ctx.store.getConfiguration().getCompatibilityInstance().getReturnNullOnEmptyAggregateResult();
        }
        return false;
    }
}
