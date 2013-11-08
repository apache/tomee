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

import org.apache.openjpa.jdbc.sql.Raw;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.exps.Literal;

/**
 * A literal value in a filter.
 *
 * @author Abe White
 */
public class Lit
    extends Const
    implements Literal {

    private Object _val;
    private int _ptype;
    private boolean _isRaw;
    private Object _rawVal;

    /**
     * Constructor. Supply literal value.
     */
    public Lit(Object val, int ptype) {
        _val = val;
        _ptype = ptype;
        if (_ptype == Literal.TYPE_DATE || _ptype == Literal.TYPE_TIME ||
            _ptype == Literal.TYPE_TIMESTAMP)
            _isRaw = true;
    }

    public Class getType() {
        if (_isRaw && _rawVal != null)
            return Raw.class;
        return (_val == null) ? Object.class : _val.getClass();
    }

    public void setImplicitType(Class type) {
        _val = Filters.convert(_val, type);
    }

    public int getParseType() {
        return _ptype;
    }

    public Object getValue() { 
        return _val;
    }

    public void setValue(Object val) {
        _val = val;
    }

    public Object getValue(Object[] params) {
        return getValue();
    }
    
    public boolean isRaw() {
        return _isRaw;
    }
    
    public void setRaw(boolean isRaw) {
        _isRaw = isRaw;
    }

    public Object getRawValue() {
        return _rawVal;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return new LitExpState();
    }

    /**
     * Expression state.
     */
    private static class LitExpState
        extends ConstExpState {

        public Object sqlValue;
        public int otherLength; 
    } 

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        LitExpState lstate = (LitExpState) state;
        if (other != null) {
            lstate.sqlValue = other.toDataStoreValue(sel, ctx, otherState,_val);
            lstate.otherLength = other.length(sel, ctx, otherState);
        } else
            lstate.sqlValue = _val;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        LitExpState lstate = (LitExpState) state;
        if (lstate.otherLength > 1)
            sql.appendValue(((Object[]) lstate.sqlValue)[index], 
                lstate.getColumn(index));
        else if (_isRaw) {
            int parseType = getParseType();
            if (parseType == Literal.TYPE_ENUM) { 
                StringBuilder value = new StringBuilder();
                boolean isOrdinal = false;
                if (lstate.sqlValue instanceof Integer)
                    isOrdinal = true;
                if (!isOrdinal)
                    value.append("'");
                value.append(lstate.sqlValue);
                if (!isOrdinal)
                    value.append("'");
                lstate.sqlValue = new Raw(value.toString());
                _rawVal = lstate.sqlValue;
            } else if (parseType == Literal.TYPE_DATE || parseType == Literal.TYPE_TIME ||
                parseType == Literal.TYPE_TIMESTAMP) {
                lstate.sqlValue = new Raw(_val.toString());
                _rawVal = lstate.sqlValue;
            } else {
                lstate.sqlValue = new Raw(_val instanceof String ? "'"+_val+"'" : _val.toString());
                _rawVal = lstate.sqlValue;
            }
        }
        Object useLiteral = ctx.fetch.getHint(QueryHints.HINT_USE_LITERAL_IN_SQL);
//        useLiteral = true;
        boolean useParamToken = useLiteral != null ? !(Boolean)useLiteral : true; 
        sql.appendValue(lstate.sqlValue, lstate.getColumn(index), null, useParamToken);
    }
}
