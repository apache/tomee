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

import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.kernel.exps.Parameter;

import serp.util.Strings;

/**
 * Test if a string matches a regexp.
 *
 * @author Abe White
 */
class MatchesExpression
    implements Exp {

    private final Val _val;
    private final Const _const;
    private final String _single;
    private final String _multi;
    private final String _escape;

    /**
     * Constructor. Supply values.
     */
    public MatchesExpression(Val val, Const con,
        String single, String multi, String escape) {
        _val = val;
        _const = con;
        _single = single;
        _multi = multi;
        _escape = escape;
    }

    public ExpState initialize(Select sel, ExpContext ctx, Map contains) {
        ExpState s1 = _val.initialize(sel, ctx, 0);
        ExpState s2 = _const.initialize(sel, ctx, 0);
        return new BinaryOpExpState(sel.and(s1.joins, s2.joins), s1, s2);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer buf) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val.calculateValue(sel, ctx, bstate.state1, _const, bstate.state2);
        _const.calculateValue(sel, ctx, bstate.state2, _val, bstate.state1);

        Column col = null;
        if (_val instanceof PCPath) {
            Column[] cols = ((PCPath) _val).getColumns(bstate.state1);
            if (cols.length == 1)
                col = cols[0];
        }

        Object o = _const.getValue(ctx, bstate.state2);
        if (o == null)
            buf.append("1 <> 1");
        else {
            // look for ignore case flag and strip it out if present
            boolean ignoreCase = false;
            String str = o.toString();
            int idx = str.indexOf("(?i)");
            if (idx != -1) {
                ignoreCase = true;
                if (idx + 4 < str.length())
                    str = str.substring(0, idx) + str.substring(idx + 4);
                else
                    str = str.substring(0, idx);
                str = str.toLowerCase();
            }

            // append target
            if (ignoreCase)
                buf.append("LOWER(");
            _val.appendTo(sel, ctx, bstate.state1, buf, 0);
            if (ignoreCase)
                buf.append(")");

            // create a DB wildcard string by replacing the
            // multi token (e.g., '.*') and the single token (e.g., ".")
            // with '%' and '.' with '_'
            str = replaceEscape(str, _multi, "%", _escape);
            str = replaceEscape(str, _single, "_", _escape);
            buf.append(" LIKE ").appendValue(str, col, 
                    _const instanceof Parameter ? (Parameter)_const : null);

            // escape out characters by using the database's escape sequence
            DBDictionary dict = ctx.store.getDBDictionary();
            if (_escape != null) {
                if (_escape.equals("\\")) 
                    buf.append(" ESCAPE '").append(dict.searchStringEscape).append("'");
                else
                    buf.append(" ESCAPE '").append(_escape).append("'");
            }
            
        }
        sel.append(buf, state.joins);
    }

    /** 
     * Perform a string replacement with simplistic escape handing. 
     *  
     * @param  str      the source string
     * @param  from     the string to find
     * @param  to       the string to replace
     * @param  escape   the string to use to escape replacement
     * @return          the replaced string
     */
    private static String replaceEscape(String str, String from, String to,
        String escape) {
        String[] parts = Strings.split(str, from, Integer.MAX_VALUE);
        StringBuilder repbuf = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                // if the previous part ended with an escape character, then
                // escape the character and remove the previous escape;
                // this doesn't support any double-escaping or other more
                // sophisticated features
                if (!from.equals(to) && parts[i - 1].endsWith(escape)) {
                    repbuf.setLength(repbuf.length() - 1);
                    repbuf.append(from);
                } else
                    repbuf.append(to);
            }
            repbuf.append(parts[i]);
        }
        return repbuf.toString();
    }

    public void selectColumns(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        BinaryOpExpState bstate = (BinaryOpExpState) state;
        _val.selectColumns(sel, ctx, bstate.state1, true);
        _const.selectColumns(sel, ctx, bstate.state2, true);
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _val.acceptVisit(visitor);
        _const.acceptVisit(visitor);
        visitor.exit(this);
    }
}
