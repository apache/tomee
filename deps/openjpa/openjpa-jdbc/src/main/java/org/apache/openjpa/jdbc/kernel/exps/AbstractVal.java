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

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.kernel.exps.Value;

/**
 * Abstract value for easy extension.
 *
 * @author Marc Prud'hommeaux
 */
abstract class AbstractVal
    implements Val {

    protected static final String TRUE = "1 = 1";
    protected static final String FALSE = "1 <> 1";
    private String _alias = null;

    public boolean isVariable() {
        return false;
    }

    public boolean isAggregate() {
        return false;
    }

    public boolean isXPath() {
        return false;
    }

    public Object toDataStoreValue(Select sel, ExpContext ctx, ExpState state, 
        Object val) {
        return val;
    }

    public void appendIsEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        sql.append(FALSE);
    }

    public void appendIsNotEmpty(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql){
        sql.append(TRUE);
    }

    public void appendIsNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        appendTo(sel, ctx, state, sql, 0);
        sql.append(" IS ").appendValue(null);
    }

    public void appendIsNotNull(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        appendTo(sel, ctx, state, sql, 0);
        sql.append(" IS NOT ").appendValue(null);
    }

    public void appendIndex(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        sql.append("1");
    }

    public void appendType(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        sql.append("1");
    }

    public void appendSize(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql) {
        sql.append("1");
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        visitor.exit(this);
    }

    public int getId() {
        return Val.VAL;
    }

    public String getAlias() {
        return _alias;
    }

    public void setAlias(String alias) {
        _alias = alias;
    }

    public Value getSelectAs() {
        return _alias != null ? this : null;
    }

    public Path getPath() {
        return null;
    }
    
    public String getName() {
        return null;
    }    
}

