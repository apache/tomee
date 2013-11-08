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
package org.apache.openjpa.jdbc.kernel;

import org.apache.openjpa.jdbc.kernel.exps.ExpContext;
import org.apache.openjpa.jdbc.kernel.exps.QueryExpressionsState;
import org.apache.openjpa.jdbc.kernel.exps.Val;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.kernel.exps.QueryExpressions;

/**
 * Object provider implementation wrapped around a projection select.
 *
 * @author Abe White
 */
class ProjectionResultObjectProvider
    extends SelectResultObjectProvider {

    private final QueryExpressions[] _exps;
    private final QueryExpressionsState[] _state;
    private final ExpContext _ctx;

    /**
     * Constructor.
     *
     * @param sel the select to execute
     * @param exps the query expressions
     * @param states the query expression states
     * @param ctx the query execution context
     */
    public ProjectionResultObjectProvider(SelectExecutor sel, 
        QueryExpressions exps, QueryExpressionsState state, ExpContext ctx) {
        this(sel, new QueryExpressions[]{ exps }, 
            new QueryExpressionsState[]{ state }, ctx);
    }

    /**
     * Constructor.
     *
     * @param sel the select to execute
     * @param exps the query expressions
     * @param states the query expression states
     * @param ctx the query execution context
     */
    public ProjectionResultObjectProvider(SelectExecutor sel, 
        QueryExpressions[] exps, QueryExpressionsState[] state, ExpContext ctx){
        super(sel, ctx.store, ctx.fetch);
        _exps = exps;
        _state = state;
        _ctx = ctx;
    }

    public Object getResultObject()
        throws Exception {
        Result res = getResult();
        // for a projection, Result has no base mapping
        res.setBaseMapping(null);
        int idx = res.indexOf();
        Object[] arr = new Object[_exps[idx].projections.length];
        for (int i = 0; i < _exps[idx].projections.length; i++)
            arr[i] = ((Val) _exps[idx].projections[i]).load(_ctx, 
                _state[idx].projections[i], res);
        return arr;
    }
}
