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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
 * A collection-valued input parameter in an in-expression.
 *
 * @author Catalina Wei
 */
public class CollectionParam
    extends Const
    implements Parameter {
    private static final Localizer _loc = Localizer.forPackage(
        CollectionParam.class);

    private final Object _key;
    private Class _type = null;
    private int _idx = -1;
    private boolean _container = false;

    /**
     * Constructor. Supply parameter name and type.
     */
    public CollectionParam(Object key, Class type) {
        _key = key;
        setImplicitType(type);
    }

    public CollectionParam clone() {
        CollectionParam c = new CollectionParam(this._key, this._type);
        c._idx = this._idx;
        c._container = this._container;
        return c;
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

    public Object getValue(Object[] params) {
        return Filters.convert(params[_idx], getType());
    }

    public Object getValue(ExpContext ctx, ExpState state) {
        ParamExpState pstate = (ParamExpState) state;
        if (pstate.discValue[0] != null)
            return Arrays.asList(pstate.discValue);
        else
            return getValue(ctx.params);
    }

    public Object getSQLValue(Select sel, ExpContext ctx, ExpState state) {
        return ((ParamExpState) state).sqlValue;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return new ParamExpState(ctx.params[_idx]);
    }

    /**
     * Expression state.
     */
    public static class ParamExpState
        extends ConstExpState {

        public int size = 0;
        public Object[] sqlValue = null;
        public int[] otherLength;
        public ClassMapping[] mapping = null;
        public Discriminator[] disc = null;
        public Object discValue[] = null;
        
        ParamExpState(Object params) {
            if (params instanceof Collection)
                size = ((Collection) params).size();
            sqlValue = new Object[size];
            otherLength = new int[size];
            mapping = new ClassMapping[size];
            disc = new Discriminator[size];
            discValue = new Object[size];
            for (int i = 0; i < size; i++) {
                sqlValue[i] = null;
                otherLength[i] = 1;
                mapping[i] = null;
                disc[i] = null;
                discValue[i] = null;
            }
        }
    } 

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        ParamExpState pstate = (ParamExpState) state;
        Object value = getValue(ctx.params);

        if (!(value instanceof Collection))
            throw new IllegalArgumentException(_loc.get(
                "not-collection-parm", _key).toString());

        if (((Collection) value).isEmpty())
            throw new IllegalArgumentException(_loc.get(
                "empty-collection-parm", _key).toString());

        Iterator itr = ((Collection) value).iterator();
        for (int i = 0; i < pstate.size && itr.hasNext(); i++) {
            Object val = itr.next();
            if (other != null && !_container) {
                pstate.sqlValue[i] = other.toDataStoreValue(sel, ctx,
                    otherState, val);
                pstate.otherLength[i] = other.length(sel, ctx, otherState);
                if (other instanceof Type) {
                    pstate.mapping[i] = ctx.store.getConfiguration().
                    getMappingRepositoryInstance().getMapping((Class) val,
                        ctx.store.getContext().getClassLoader(), true);
                    pstate.disc[i] = pstate.mapping[i].getDiscriminator();
                    pstate.discValue[i] = pstate.disc[i] != null ?
                        pstate.disc[i].getValue() : null;
                }
            } else if (ImplHelper.isManageable(val)) {
                ClassMapping mapping = ctx.store.getConfiguration().
                getMappingRepositoryInstance().getMapping(val.getClass(),
                    ctx.store.getContext().getClassLoader(), true);
                pstate.sqlValue[i] = mapping.toDataStoreValue(val,
                    mapping.getPrimaryKeyColumns(), ctx.store);
                pstate.otherLength[i] = mapping.getPrimaryKeyColumns().length;
            } else
                pstate.sqlValue[i] = val;
        }
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        ParamExpState pstate = (ParamExpState) state;
        for (int i = 0; i < pstate.size; i++) {
            if (pstate.otherLength[i] > 1)
                sql.appendValue(((Object[]) pstate.sqlValue[i])[index], 
                        pstate.getColumn(index), this);
            else if (pstate.cols != null)
                sql.appendValue(pstate.sqlValue[i], pstate.getColumn(index),
                        this);
            else if (pstate.discValue[i] != null)
                sql.appendValue(pstate.discValue[i]);
            else
                sql.appendValue(pstate.sqlValue[i], pstate.getColumn(index),
                        this);
        }
    }
}
