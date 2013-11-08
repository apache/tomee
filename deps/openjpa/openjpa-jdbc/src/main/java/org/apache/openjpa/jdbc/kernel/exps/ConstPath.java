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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.openjpa.jdbc.sql.SQLBuffer;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.exps.Context;
import org.apache.openjpa.kernel.exps.ExpressionVisitor;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.XMLMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.ImplHelper;

/**
 * A field traversal starting with a constant filter parameter.
 *
 * @author Abe White
 */
class ConstPath
    extends Const
    implements JDBCPath {

    private final Const _constant;
    private final LinkedList _actions = new LinkedList();

    /**
     * Constructor. Supply constant to traverse.
     */
    public ConstPath(Const constant) {
        _constant = constant;
    }

    public Class getType() {
        if (_actions.isEmpty()) {
            ClassMetaData meta = getMetaData();
            if (meta == null)
                return Object.class;
            return meta.getDescribedType();
        }

        Object last = _actions.getLast();
        if (last instanceof Class)
            return (Class) last;
        FieldMetaData fmd = (FieldMetaData) last;
        return fmd.getDeclaredType();
    }

    public void setImplicitType(Class type) {
        _actions.add(type);
    }

    public void get(FieldMetaData field, boolean nullTraversal) {
        _actions.add(field);
    }

    public void getKey() {
    }

    public FieldMetaData last() {
        ListIterator itr = _actions.listIterator(_actions.size());
        Object prev;
        while (itr.hasPrevious()) {
            prev = itr.previous();
            if (prev instanceof FieldMetaData)
                return (FieldMetaData) prev;
        }
        return null;
    }

    public Object getValue(Object[] params) {
        throw new InternalException();
    }

    public Object getValue(ExpContext ctx, ExpState state) {
        return ((ConstPathExpState) state).value;
    }

    public Object getSQLValue(Select sel, ExpContext ctx, ExpState state) {
        return ((ConstPathExpState) state).sqlValue;
    }

    public ExpState initialize(Select sel, ExpContext ctx, int flags) {
        return new ConstPathExpState(_constant.initialize(sel, ctx, 0));
    }

    public void calculateValue(Select sel, ExpContext ctx, ExpState state, 
        Val other, ExpState otherState) {
        super.calculateValue(sel, ctx, state, other, otherState);
        ConstPathExpState cstate = (ConstPathExpState) state;
        _constant.calculateValue(sel, ctx, cstate.constantState, null, null);
        cstate.value = _constant.getValue(ctx, cstate.constantState);
        boolean failed = false;

        // copied from org.apache.openjpa.query.InMemoryPath
        Object action;
        OpenJPAStateManager sm;
        Broker tmpBroker = null;
        for (Iterator itr = _actions.iterator(); itr.hasNext();) {
            // fail on null value
            if (cstate.value == null) {
                failed = true;
                break;
            }

            action = itr.next();
            if (action instanceof Class) {
                try {
                    cstate.value = Filters.convert(cstate.value,
                        (Class) action);
                    continue;
                } catch (ClassCastException cce) {
                    failed = true;
                    break;
                }
            }

            // make sure we can access the instance; even non-pc vals might
            // be proxyable
            sm = null;
            tmpBroker = null;
            if (ImplHelper.isManageable(cstate.value))
                sm = (OpenJPAStateManager) (ImplHelper.toPersistenceCapable(
                    cstate.value,
                    this.getMetaData().getRepository().getConfiguration())).
                    pcGetStateManager();
            if (sm == null) {
                tmpBroker = ctx.store.getContext().getBroker();
                tmpBroker.transactional(cstate.value, false, null);
                sm = tmpBroker.getStateManager(cstate.value);
            }

            try {
                // get the specified field value and switch candidate
                cstate.value = sm.fetchField(((FieldMetaData) action).
                    getIndex(), true);
            } finally {
                // setTransactional does not clear the state, which is
                // important since tmpVal might be also managed by
                // another broker if it's a proxied non-pc instance
                if (tmpBroker != null)
                    tmpBroker.nontransactional(sm.getManagedInstance(), null);
            }
        }

        if (failed)
            cstate.value = null;

        if (other != null) {
            cstate.sqlValue = other.toDataStoreValue(sel, ctx, otherState, 
                cstate.value);
            cstate.otherLength = other.length(sel, ctx, otherState);
        } else
            cstate.sqlValue = cstate.value;
    }

    public void appendTo(Select sel, ExpContext ctx, ExpState state, 
        SQLBuffer sql, int index) {
        ConstPathExpState cstate = (ConstPathExpState) state;
        if (cstate.otherLength > 1)
            sql.appendValue(((Object[]) cstate.sqlValue)[index], 
                cstate.getColumn(index));
        else
            sql.appendValue(cstate.sqlValue, cstate.getColumn(index));
    }

    public void acceptVisit(ExpressionVisitor visitor) {
        visitor.enter(this);
        _constant.acceptVisit(visitor);
        visitor.exit(this);
    }

    /**
     * Expression state.
     */
    private static class ConstPathExpState 
        extends ConstExpState {

        public final ExpState constantState;
        public Object value = null;
        public Object sqlValue = null;
        public int otherLength = 0;

        public ConstPathExpState(ExpState constantState) {
            this.constantState = constantState;
        }
    }
    
    public void get(FieldMetaData fmd, XMLMetaData meta) {
    }

    public void get(XMLMetaData meta, String name) {
    }

    public XMLMetaData getXmlMapping() {
        return null;
    }

    public void setSchemaAlias(String schemaAlias) {
    }
    
    public String getSchemaAlias() {
        return null;
    }
    
    public void setSubqueryContext(Context conext, String correlationVar) {
    }

    public String getCorrelationVar() {
        return null;
    }
}
