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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Compares two entity types.
 *
 * @author Catalina Wei
 */
class NotEqualTypeExpression
    extends CompareEqualExpression {

    /**
     * Constructor. Supply values to compare.
     */
    public NotEqualTypeExpression(Val val1, Val val2) {
        super(val1, val2);
    }

    private ClassMapping getSubClassMapping(Val val1, Val val2, ExpContext ctx) {
        ClassMapping sub = null;
        Val val = val1 instanceof Type ? val2 : val1;
        if (val instanceof TypeLit)
            sub = (ClassMapping) val.getMetaData();
        else if (val instanceof Param)
            sub = ((Param) val).getValueMetaData(ctx);
        if (sub != null)
            ctx.isVerticalStrat = sub.isVerticalStrategy();
        return sub;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
            SQLBuffer buf) {
        Val val1 = getValue1();
        Val val2 = getValue2();
        ClassMapping sub = getSubClassMapping(val1, val2, ctx);
        if (ctx.isVerticalStrat) {
            appendTo(sel, ctx, (BinaryOpExpState)state, buf, false, false);
            return;
        }
        
        super.appendTo(sel, ctx, state, buf);
    }
    
    public void appendTo(Select sel, ExpContext ctx, BinaryOpExpState bstate, 
        SQLBuffer buf, boolean val1Null, boolean val2Null) {
        if (val1Null && val2Null)
            buf.append("1 <> 1");
        else if (val1Null || val2Null) {
            Val val = (val1Null) ? getValue2() : getValue1();
            ExpState state = (val1Null) ? bstate.state2 : bstate.state1;
            if (!isDirectComparison()) {
                int len = val.length(sel, ctx, state);
                for (int i = 0; i < len; i++) {
                    if (i > 0)
                        buf.append(" AND ");
                    val.appendTo(sel, ctx, state, buf, i);
                    buf.append(" IS NOT ").appendValue(null);
                }
            } else
                val.appendIsNotNull(sel, ctx, state, buf);
        } else {
            Val val1 = getValue1();
            Val val2 = getValue2();
            if (val1.length(sel, ctx, bstate.state1) == 1 
                && val2.length(sel, ctx, bstate.state2) == 1) {
                ClassMapping sub = getSubClassMapping(val1, val2, ctx);
                if (ctx.isVerticalStrat) {
                    processVerticalTypeAppend(sel, val1, val2, ctx, buf);
                    return;
                }
                    
                String op;
                if (sub != sel.getTablePerClassMeta())
                    op = "=";
                else
                    op = "<>";
                
               ctx.store.getDBDictionary().comparison(buf, op,
                    new FilterValueImpl(sel, ctx, bstate.state1, val1),
                    new FilterValueImpl(sel, ctx, bstate.state2, val2));
            } else {
                int len = java.lang.Math.max(val1.length(sel, ctx, 
                    bstate.state1), val2.length(sel, ctx, bstate.state2));
                buf.append("(");
                for (int i = 0; i < len; i++) {
                    if (i > 0)
                        buf.append(" OR ");
                    val1.appendTo(sel, ctx, bstate.state1, buf, i);
                    buf.append(" <> ");
                    val2.appendTo(sel, ctx, bstate.state2, buf, i);
                }
                buf.append(")");
            }
        }
    }
    
    void processVerticalTypeAppend(Select sel, Val val1, Val val2, ExpContext ctx,  
            SQLBuffer buf) {
        ClassMapping sub = getSubClassMapping(val1, val2, ctx);
        ClassMapping cm1 = (ClassMapping)((val1 instanceof Type) ? val1.getMetaData() :
            val1.getMetaData());
        if (sub != null && sub.isVerticalStrategy()) {
            ClassMetaData[] subs = cm1.getPCSubclassMetaDatas();
            List exSelectFrom = sel.getExcludedJoinedTableClassMeta();
            if (exSelectFrom == null) {
                exSelectFrom = new ArrayList();
                sel.setExcludedJoinedTableClassMeta(exSelectFrom);
            }
            List selectFrom = sel.getJoinedTableClassMeta();
            exSelectFrom.add(sub);
            if (selectFrom == null) {
                selectFrom = new ArrayList();
                sel.setJoinedTableClassMeta(selectFrom);
            }
            
            for (int i = 0; i < subs.length; i++) {
                if (!Modifier.isAbstract(subs[i].getDescribedType().getModifiers()) && 
                    !selectFrom.contains(subs[i]))
                    selectFrom.add(subs[i]);
            }
            buf.append("1=1");
            return;
        }
    }    
}
