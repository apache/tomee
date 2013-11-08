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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.exps.Path;
import org.apache.openjpa.util.InternalException;

/**
 * Entity Type expression.
 *
 * @author Catalina Wei
 * @since 2.0.0
 */
class Type
    extends UnaryOp {
    
    Discriminator _disc = null;

    public Type(Val val) {
        super(val);
        setMetaData(val.getMetaData());
        if (getMetaData() != null)
            _disc = ((ClassMapping) getMetaData()).getDiscriminator();
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return initializeValue(sel, ctx, flags);
    }

    public Object load(ExpContext ctx, ExpState state, Result res)
        throws SQLException {
        Object type = null;
        if (_disc != null && _disc.getColumns().length > 0) {
            type = res.getObject(_disc.getColumns()[0], null, state.joins);
            ClassMapping sup = (ClassMapping) getMetaData();
            ClassMapping[] subs = sup.getMappedPCSubclassMappings();
            for (ClassMapping sub : subs) {
                if (sub.getDiscriminator().getValue().equals(type))
                    return sub.getDescribedType();
            }
        }
        else
            type = getValue().load(ctx, state, res);
        return type.getClass();
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, null, null);
    }

    public void select(Select sel, ExpContext ctx, ExpState state, 
        boolean pks) {
        if (_disc != null && _disc.getColumns().length > 0)
            sel.select(_disc.getColumns(), state.joins);
        else
            getValue().select(sel, ctx, state, pks);
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        getValue().calculateValue(sel, ctx, state, null, null);
        getValue().appendType(sel, ctx, state, sql);
        sel.append(sql, state.joins);
    }

    protected Class getType(Class c) {
        return Class.class;
    }

    protected String getOperator() {
        // since we override appendTo(), this method should never be called
        throw new InternalException();
    }

    public Path getPath() {
        return getValue() instanceof Path ? (Path) getValue() : null;
    }

    public Discriminator getDiscriminator() {
        return _disc;
    }
}
