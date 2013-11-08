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

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.Literal;
import org.apache.openjpa.meta.JavaTypes;

/**
 * A type literal value.
 *
 * @author Catalina Wei
 */
public class TypeLit
    extends Const
    implements Literal {

    private Object _val;
    private int _ptype;

    /**
     * Constructor. Supply literal value.
     */
    public TypeLit(Object val, int ptype) {
        _val = val;
        _ptype = ptype;
    }

    public Class getType() {
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
        public ClassMapping mapping = null;
        public Discriminator disc = null;
        public Object discValue = null;
    } 

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        LitExpState lstate = (LitExpState) state;
        lstate.mapping = (ClassMapping) getMetaData();
        lstate.disc = lstate.mapping.getDiscriminator();
        lstate.discValue = lstate.disc != null ? lstate.disc.getValue() :
            null;
        sel.select(((ClassMapping) getMetaData()).getPrimaryKeyColumns(),
            lstate.joins);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        LitExpState lstate = (LitExpState) state;
        if (lstate.otherLength > 1)
            sql.appendValue(((Object[]) lstate.sqlValue)[index], 
                lstate.getColumn(index));
        else {
            if (lstate.discValue != null)                
                sql.append(getDiscriminator(lstate));
            else
                sql.append("1");
        }
    }

    private String getDiscriminator(LitExpState lstate) {
        StringBuilder disc = new StringBuilder(lstate.discValue.toString());
        switch(lstate.disc.getJavaType()) {
        case JavaTypes.INT:
            return disc.toString();
        case JavaTypes.CHAR:
        case JavaTypes.STRING:
        default:
            return disc.insert(0, "'").append("'").toString();
        }        
    }
}
