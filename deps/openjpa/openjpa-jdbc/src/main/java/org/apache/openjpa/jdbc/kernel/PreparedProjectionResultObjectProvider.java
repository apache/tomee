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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.exps.ExpContext;
import org.apache.openjpa.jdbc.kernel.exps.QueryExpressionsState;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.kernel.exps.QueryExpressions;

/**
 * Object provider implementation wrapped around a projection select that has been executed earlier.
 *
 * @author Pinaki Poddar
 */
class PreparedProjectionResultObjectProvider
    extends ProjectionResultObjectProvider {

    public PreparedProjectionResultObjectProvider(SelectExecutor sel, 
        QueryExpressions exps, QueryExpressionsState state, ExpContext ctx, Result res) {
        super(sel, exps, state, ctx);
        _res = res;
    }

    public PreparedProjectionResultObjectProvider(SelectExecutor sel, 
        QueryExpressions[] exps, QueryExpressionsState[] state, ExpContext ctx, Result res){
        super(sel, exps, state, ctx);
        _res = res;
    }

    public Result getResult() {
        return _res;
    }
    
    public void open() throws SQLException {
        // do nothing
    }

}
