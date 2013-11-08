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

import java.util.Collection;
import java.util.Map;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.exps.Parameter;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.ImplHelper;

/**
 * A parameter in a filter.
 *
 * @author Abe White
 */
public class Param
    extends Const
    implements Parameter {

    private static final Localizer _loc = Localizer.forPackage(Param.class);
    private final Object _key;
    private Class _type = null;
    private int _idx = -1;
    private boolean _container = false;

    /**
     * Constructor. Supply parameter name and type.
     */
    public Param(Object key, Class type) {
        _key = key;
        setImplicitType(type);
    }

    public Object getParameterKey() {
        return _key;
    }

    public Class getType() {
        return _type;
    }

    public void setImplicitType(Class type) {
        _type = type;
        _container = (getMetaData() == null || !ImplHelper.isManagedType(
            getMetaData().getRepository().getConfiguration(), type))
            && (Collection.class.isAssignableFrom(type)
            || Map.class.isAssignableFrom(type));
    }

    public int getIndex() {
        return _idx;
    }

    public void setIndex(int idx) {
        _idx = idx;
    }

    public ClassMapping getValueMetaData(ExpContext ctx) {
        Object[] params = ctx.params;
        if (params[_idx] != null && params[_idx] instanceof Class)
            return (ClassMapping) ctx.store.getConfiguration().getMetaDataRepositoryInstance().
                getMetaData((Class) params[_idx], getClass().getClassLoader(), true);
        return null;
    }

    public Object getValue(Object[] params) {
        return Filters.convert(params[_idx], getType());
    }

    public Object getValue(ExpContext ctx, ExpState state) {
        ParamExpState pstate = (ParamExpState) state;
        return (pstate.discValue != null) ? pstate.discValue :
            getValue(ctx.params);
    }

    public Object getSQLValue(Select sel, ExpContext ctx, ExpState state) {
        return ((ParamExpState) state).sqlValue;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return new ParamExpState();
    }

    /**
     * Expression state.
     */
    public static class ParamExpState
        extends ConstExpState {

        public Object sqlValue = null;
        public int otherLength = 1;
        public ClassMapping mapping = null;
        public Discriminator disc = null;
        public Object discValue = null;
    } 

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        Object val = getValue(ctx.params);
        ParamExpState pstate = (ParamExpState) state;
        if (other != null && !_container) {
            pstate.sqlValue = other.toDataStoreValue(sel, ctx, otherState, val);
            pstate.otherLength = other.length(sel, ctx, otherState);
            if (other instanceof Type) {
                pstate.mapping = ctx.store.getConfiguration().
                    getMappingRepositoryInstance().getMapping((Class) val,
                        ctx.store.getContext().getClassLoader(), true);
                pstate.disc = pstate.mapping.getDiscriminator();
                pstate.discValue = pstate.disc.getValue() != null ? pstate.disc.getValue() : "1";
            }
        } else if (ImplHelper.isManageable(val)) {
            ClassMapping mapping = ctx.store.getConfiguration().
                getMappingRepositoryInstance().getMapping(val.getClass(),
                ctx.store.getContext().getClassLoader(), true);
            pstate.sqlValue = mapping.toDataStoreValue(val,
                mapping.getPrimaryKeyColumns(), ctx.store);
            pstate.otherLength = mapping.getPrimaryKeyColumns().length;
        } else
            pstate.sqlValue = val;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        ParamExpState pstate = (ParamExpState) state;
        if (pstate.otherLength > 1)
            sql.appendValue(((Object[]) pstate.sqlValue)[index], 
                pstate.getColumn(index), this);
        else if (pstate.cols != null)
            sql.appendValue(pstate.sqlValue, pstate.getColumn(index), this);
        else if (pstate.discValue != null)
            sql.appendValue(pstate.discValue);
        else
            sql.appendValue(pstate.sqlValue, pstate.getColumn(index), this);
    }

    public int length(Select sel, ExpContext ctx, ExpState state) {
        ParamExpState pstate = (ParamExpState) state;
        if (getMetaData() == null || pstate.cols == null)
            return 1;
        return pstate.cols.length;
    }
}
