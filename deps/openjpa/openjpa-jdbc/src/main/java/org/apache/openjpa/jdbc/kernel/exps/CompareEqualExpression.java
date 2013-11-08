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

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Compares two values for equality.
 *
 * @author Abe White
 */
abstract class CompareEqualExpression
    implements Exp {

    private static final Localizer _loc = Localizer.forPackage
        (CompareEqualExpression.class);

    private final Val _val1;
    private final Val _val2;

    /**
     * Constructor. Supply values to compare.
     */
    public CompareEqualExpression(Val val1, Val val2) {
        _val1 = val1;
        _val2 = val2;
        if (_val1 instanceof Lit && _val2 instanceof Lit) {
            ((Lit)_val1).setRaw(true);
            ((Lit)_val2).setRaw(true);
        }
    }

    public Val getValue1() {
        return _val1;
    }

    public Val getValue2() {
        return _val2;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        boolean direct = isDirectComparison();
        int flags1 = 0;
        int flags2 = 0;
        ExpState s1 = null;
        ExpState s2 = null;
        if (_val1 instanceof Const) {
            s1 = _val1.initialize(sel, ctx, 0);
            if (direct && ((Const) _val1).getValue(ctx, s1) == null)
                flags2 = Val.NULL_CMP;
        }
        if (_val2 instanceof Const) {
            s2 = _val2.initialize(sel, ctx, 0);
            if (direct && ((Const) _val2).getValue(ctx, s2) == null)
                flags1 = Val.NULL_CMP;
        }

        if (_val1 instanceof PCPath && _val2 instanceof PCPath &&
            (((PCPath)_val1).isSubqueryPath() || ((PCPath)_val2).isSubqueryPath())) {
            flags1 = flags1 | Val.CMP_EQUAL;
            flags2 = flags2 | Val.CMP_EQUAL;
        }
        
        if (s1 == null)
            s1 = _val1.initialize(sel, ctx, flags1);
        if (s2 == null)
            s2 = _val2.initialize(sel, ctx, flags2);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.calculateValue(sel, ctx, bstate.state1, _val2, bstate.state2);
        _val2.calculateValue(sel, ctx, bstate.state2, _val1, bstate.state1);
        if (!Filters.canConvert(_val1.getType(), _val2.getType(), false)
            && !Filters.canConvert(_val2.getType(), _val1.getType(), false))
            throw new UserException(_loc.get("cant-convert", _val1.getType(),
                _val2.getType()));

        boolean val1Null = _val1 instanceof Const
            && ((Const) _val1).isSQLValueNull(sel, ctx, bstate.state1);
        boolean val2Null = _val2 instanceof Const
            && ((Const) _val2).isSQLValueNull(sel, ctx, bstate.state2);
        appendTo(sel, ctx, bstate, buf, val1Null, val2Null);
        if (sel != null)
            sel.append(buf, state.joins);
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val1.selectColumns(sel, ctx, bstate.state1, true);
        _val2.selectColumns(sel, ctx, bstate.state2, true);
    }

    /**
     * Append the SQL for the comparison.
     */
    protected abstract void appendTo(Select sel, ExpContext ctx, 
        BinaryOpExpState state, SQLBuffer buf, boolean val1Null, 
        boolean val2Null);

    /**
     * Subclasses can override this method if, when they compare to another,
     * value, the comparison is indirect. For example, field.contains (x)
     * should compare element values to null, not the field itself.
     */
    protected boolean isDirectComparison() {
        return true;
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val1.acceptVisit(visitor);
        _val2.acceptVisit(visitor);
        visitor.exit(this);
    }
}
