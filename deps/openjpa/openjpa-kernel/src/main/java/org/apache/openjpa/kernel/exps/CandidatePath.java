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
package org.apache.openjpa.kernel.exps;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.XMLMetaData;
import org.apache.openjpa.util.ImplHelper;

/**
 * A path represents a traversal into fields of a candidate object.
 *
 * @author Abe White
 */
public class CandidatePath
    extends Val
    implements Path {

    protected LinkedList _actions = null;
    protected String _correlationVar = null;

    /**
     * Traverse into the given field of the current object, and update
     * the current object to that field value.
     */
    public void get(FieldMetaData field, boolean nullTraversal) {
        if (_actions == null)
            _actions = new LinkedList();
        _actions.add(new Traversal(field, nullTraversal));
    }

    public Class getType() {
        if (_actions == null)
            return getCandidateType();

        Object last = _actions.getLast();
        if (last instanceof Class)
            return (Class) last;
        FieldMetaData fmd = ((Traversal) last).field;
        return fmd.getDeclaredType();
    }

    protected Class getCandidateType() {
        ClassMetaData meta = getMetaData();
        if (meta == null)
            return Object.class;
        return meta.getDescribedType();
    }

    public void setImplicitType(Class type) {
    }

    public FieldMetaData last() {
        if (_actions == null)
            return null;

        ListIterator itr = _actions.listIterator(_actions.size());
        Object prev;
        while (itr.hasPrevious()) {
            prev = itr.previous();
            if (prev instanceof Traversal)
                return ((Traversal) prev).field;
        }
        return null;
    }

    /**
     * Cast this path to the given type.
     */
    public void castTo(Class type) {
        if (_actions == null)
            _actions = new LinkedList();
        _actions.add(type);
    }

    protected Object eval(Object candidate, Object orig,
        StoreContext ctx, Object[] params) {
        if (_actions == null)
            return candidate;

        Object action;
        OpenJPAStateManager sm;
        Broker tmpBroker = null;
        for (Iterator itr = _actions.iterator(); itr.hasNext();) {
            action = itr.next();

            // fail on null value
            if (candidate == null) {
                if (action instanceof Traversal
                    && ((Traversal) action).nullTraversal)
                    return null;
                throw new NullPointerException();
            }

            // check that the cast is valid
            if (action instanceof Class) {
                candidate = Filters.convert(candidate, (Class) action);
                continue;
            }

            // make sure we can access the instance; even non-pc vals might
            // be proxyable
            sm = null;
            tmpBroker = null;
            if (ImplHelper.isManageable(candidate))
                sm = (OpenJPAStateManager) (ImplHelper.toPersistenceCapable(
                    candidate, ctx.getConfiguration())).
                    pcGetStateManager();
            if (sm == null) {
                tmpBroker = ctx.getBroker();
                tmpBroker.transactional(candidate, false, null);
                sm = tmpBroker.getStateManager(candidate);
            }

            try {
                // get the specified field value and switch candidate
                Traversal traversal = (Traversal) action;
                candidate = sm.fetchField(traversal.field.getIndex(), true);
            } finally {
                // transactional does not clear the state, which is
                // important since tmpCandidate might be also managed by
                // another broker if it's a proxied non-pc instance
                if (tmpBroker != null)
                    tmpBroker.nontransactional(sm.getManagedInstance(), null);
            }
        }
        return candidate;
    }

    public int hashCode() {
        return (_actions == null) ? 0 : _actions.hashCode();
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof CandidatePath))
            return false;
        return ObjectUtils.equals(_actions, ((CandidatePath) other)._actions);
    }

    /**
     * Represents a traversal through a field.
     */
    public static class Traversal {

        public final FieldMetaData field;
        public final boolean nullTraversal;

        private Traversal(FieldMetaData field, boolean nullTraversal) {
            this.field = field;
            this.nullTraversal = nullTraversal;
        }

        public int hashCode() {
            return field.hashCode();
        }

        public boolean equals(Object other) {
            if (other == this)
                return true;
            return ((Traversal) other).field.equals(field);
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
        return _correlationVar;
    }
}
