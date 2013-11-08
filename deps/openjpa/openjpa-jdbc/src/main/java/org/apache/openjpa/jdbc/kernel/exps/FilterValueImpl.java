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
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.meta.XMLMetaData;

/**
 * Implementation of {@link FilterValue} that wraps a {@link Val}.
 *
 * @author Abe White
 */
class FilterValueImpl
    implements FilterValue {

    private final Select _sel;
    private final ExpContext _ctx;
    private final ExpState _state;
    private final Val _val;

    public FilterValueImpl(Select sel, ExpContext ctx, ExpState state, 
        Val val) {
        _sel = sel;
        _ctx = ctx;
        _state = state;
        _val = val;
    }

    public Class getType() {
        return _val.getType();
    }

    public int length() {
        return _val.length(_sel, _ctx, _state);
    }

    public void appendTo(SQLBuffer buf) {
        appendTo(buf, 0);
    }

    public void appendTo(SQLBuffer buf, int index) {
        _val.appendTo(_sel, _ctx, _state, buf, index);
    }

    public String getColumnAlias(Column col) {
        return _sel.getColumnAlias(col, _state.joins);
    }

    public String getColumnAlias(String col, Table table) {
        return _sel.getColumnAlias(col, table, _state.joins);
    }

    public Object toDataStoreValue(Object val) {
        return _val.toDataStoreValue(_sel, _ctx, _state, val);
    }

    public boolean isConstant() {
        return _val instanceof Const;
    }

    public Object getValue() {
        return (isConstant()) ? ((Const) _val).getValue(_ctx.params) : null;
    }

    public Object getSQLValue() {
        return (isConstant()) ? ((Const) _val).getSQLValue(_sel, _ctx, _state) 
            : null;
    }

    public boolean isPath() {
        return _val instanceof PCPath;
    }

    public ClassMapping getClassMapping() {
        return (isPath()) ? ((PCPath) _val).getClassMapping(_state) : null;
    }

    public FieldMapping getFieldMapping() {
        return (isPath()) ? ((PCPath) _val).getFieldMapping(_state) : null;
    }
    
    public PCPath getXPath() {
        if (isPath() && ((PCPath) _val).isXPath())
            return (PCPath) _val;
        else
            return null;
    }
    
    public XMLMetaData getXmlMapping() {
        return (getXPath() == null) ? null : getXPath().getXmlMapping();
    }

    public boolean requiresCast() {
        return !(_val instanceof All || _val instanceof Any || _val instanceof PCPath);
    }
}
