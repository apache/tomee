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

import java.util.Map;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.XMLMetaData;

/**
 * Test if one string starts with another.
 *
 * @author Abe White
 */
class StartsWithExpression
    implements Exp {

    private final Val _val1;
    private final Val _val2;

    /**
     * Constructor. Supply values.
     */
    public StartsWithExpression(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState s1 = _val1.initialize(sel, ctx, 0);
        ExpState s2 = _val2.initialize(sel, ctx, 0);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.calculateValue(sel, ctx, bstate.state1, _val2, bstate.state2);
        _val2.calculateValue(sel, ctx, bstate.state2, _val1, bstate.state1);

        if (_val1 instanceof Const 
            && ((Const) _val1).getValue(ctx, bstate.state1) == null)
            buf.append("1 <> 1");
        else if (_val2 instanceof Const) {
            Object o = ((Const) _val2).getValue(ctx, bstate.state2);
            if (o == null)
                buf.append("1 <> 1");
            else {
                Column col = null;
                if (_val1 instanceof PCPath) {
                    Column[] cols = ((PCPath) _val1).getColumns(bstate.state1);
                    if (cols.length == 1)
                        col = cols[0];
                }

                _val1.appendTo(sel, ctx, bstate.state1, buf, 0);
                buf.append(" LIKE ");
                buf.appendValue(o.toString() + "%", col);
            }
        } else {
            String pre = null;
            String post = null;
            DBDictionary dict = ctx.store.getDBDictionary();
            String func = dict.stringLengthFunction;
            if (func != null) {
                int idx = func.indexOf("{0}");
                pre = func.substring(0, idx);
                post = func.substring(idx + 3);
            }

            // if we can't use LIKE, we have to take the substring of the
            // first value and compare it to the second
            dict.assertSupport(pre != null, "StringLengthFunction");
            dict.substring(buf,
                new FilterValueImpl(sel, ctx, bstate.state1, _val1),
                new ZeroFilterValue(sel, state),
                new StringLengthFilterValue(sel, ctx, bstate.state2, pre,post));
            buf.append(" = ");
            _val2.appendTo(sel, ctx, bstate.state2, buf, 0);
        }

        sel.append(buf, state.joins);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.selectColumns(sel, ctx, bstate.state1, true);
        _val2.selectColumns(sel, ctx, bstate.state2, true);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }

    /**
     * Evaluates to 0.
     */
    private static class ZeroFilterValue
        implements FilterValue {

        private final Select _sel;
        private final ExpState _state;

        public ZeroFilterValue(Select sel, ExpState state) {
            _sel = sel;
            _state = state;
        }

        public Class getType() {
            return int.class;
        }

        public int length() {
            return 1;
        }

        public void appendTo(SQLBuffer buf) {
            appendTo(buf, 0);
        }

        public void appendTo(SQLBuffer buf, int index) {
            buf.appendValue(0);
        }

        public String getColumnAlias(Column col) {
            return _sel.getColumnAlias(col, _state.joins);
        }

        public String getColumnAlias(String col, Table table) {
            return _sel.getColumnAlias(col, table, _state.joins);
        }

        public Object toDataStoreValue(Object val) {
            return val;
        }

        public boolean isConstant() {
            return true;
        }

        public Object getValue() {
            return 0;
        }

        public Object getSQLValue() {
            return 0;
        }

        public boolean isPath() {
            return false;
        }

        public ClassMapping getClassMapping() {
            return null;
        }

        public FieldMapping getFieldMapping() {
            return null;
        }
        
        public PCPath getXPath() {
            return null;
        }
        
        public XMLMetaData getXmlMapping() {
            return null;
        }

        public boolean requiresCast() {
            return false;
        }

    }

    /**
     * Evaluates to the length of a given value.
     */
    private class StringLengthFilterValue
        implements FilterValue {

        private final Select _sel;
        private final ExpContext _ctx;
        private final ExpState _state;
        private final String _pre;
        private final String _post;

        public StringLengthFilterValue(Select sel, ExpContext ctx, 
            ExpState state, String pre, String post){
            _sel = sel;
            _ctx = ctx;
            _state = state;
            _pre = pre;
            _post = post;
        }

        public Class getType() {
            return int.class;
        }

        public int length() {
            return 1;
        }

        public void appendTo(SQLBuffer buf) {
            appendTo(buf, 0);
        }

        public void appendTo(SQLBuffer buf, int index) {
            buf.append(_pre);
            _val2.appendTo(_sel, _ctx, _state, buf, index);
            buf.append(_post);
        }

        public String getColumnAlias(Column col) {
            return _sel.getColumnAlias(col, _state.joins);
        }

        public String getColumnAlias(String col, Table table) {
            return _sel.getColumnAlias(col, table, _state.joins);
        }

        public Object toDataStoreValue(Object val) {
            return _val2.toDataStoreValue(_sel, _ctx, _state, val);
        }

        public boolean isConstant() {
            return false;
        }

        public Object getValue() {
            return null;
        }

        public Object getSQLValue() {
            return null;
        }

        public boolean isPath() {
            return false;
        }

        public ClassMapping getClassMapping() {
            return null;
        }

        public FieldMapping getFieldMapping() {
            return null;
        }
        
        public PCPath getXPath() {
            return null;
        }
        
        public XMLMetaData getXmlMapping() {
            return null;
        }

        public boolean requiresCast() {
            return false;
        }
    }
}
