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
import org.apache.openjpa.util.ImplHelper;

/**
 * Obtaining the object id of a constant.
 *
 * @author Abe White
 */
class ConstGetObjectId
    extends Const {

    private final Const _constant;

    /**
     * Constructor. Supply constant to traverse.
     */
    public ConstGetObjectId(Const constant) {
        _constant = constant;
    }

    public Class getType() {
        return Object.class;
    }

    public void setImplicitType(Class type) {
    }

    public Object getValue(Object[] params) {
        Object o = _constant.getValue(params);
        if (!(ImplHelper.isManageable(o)))
            return null;
        return (ImplHelper.toPersistenceCapable(o,
            this.getMetaData().getRepository().getConfiguration()))
            .pcFetchObjectId();
    }

    public Object getValue(ExpContext ctx, ExpState state) {
        return ctx.store.getContext().getObjectId(_constant.getValue(ctx, 
            ((ConstGetObjectIdExpState) state).constantState));
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return new ConstGetObjectIdExpState(_constant.initialize(sel, ctx, 0));
    }

    public Object getSQLValue(Select sel, ExpContext ctx, ExpState state) {
        return ((ConstGetObjectIdExpState) state).sqlValue;
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        ConstGetObjectIdExpState cstate = (ConstGetObjectIdExpState) state;
        _constant.calculateValue(sel, ctx, cstate.constantState, null, null);
        Object oid = ctx.store.getContext().getObjectId(_constant.getValue(ctx, 
            cstate.constantState));
        if (other != null) {
            cstate.sqlValue = other.toDataStoreValue(sel, ctx, otherState, oid);
            cstate.otherLength = other.length(sel, ctx, otherState);
        } else
            cstate.sqlValue = oid;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        ConstGetObjectIdExpState cstate = (ConstGetObjectIdExpState) state;
        if (cstate.otherLength > 1)
            sql.appendValue(((Object[]) cstate.sqlValue)[index], 
                cstate.getColumn(index));
        else
            sql.appendValue(cstate.sqlValue, cstate.getColumn(index));
    }

    /**
     * Expression state.
     */
    private static class ConstGetObjectIdExpState 
        extends ConstExpState {

        public final ExpState constantState;
        public Object sqlValue = null;
        public int otherLength = 0;

        public ConstGetObjectIdExpState(ExpState constantState) {
            this.constantState = constantState;
        }
    }
}
