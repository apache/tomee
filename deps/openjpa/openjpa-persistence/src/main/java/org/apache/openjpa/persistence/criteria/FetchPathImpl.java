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
package org.apache.openjpa.persistence.criteria;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.openjpa.persistence.meta.Members;

/**
 *  
 * @author Pinaki Poddar
 *
 * @param <Z> type of parent
 * @param <X> type of this
 */
class FetchPathImpl<Z,X> extends PathImpl<Z,X> implements Fetch<Z, X> {
    protected Set<Fetch<?,?>> _fetches;
    protected JoinType joinType;
    
    
    FetchPathImpl(FetchParent<?,Z> parent, Members.Member<? super Z,X> member) {
        this(parent, member, JoinType.INNER);
    }
    
    FetchPathImpl(FetchParent<?,Z> parent, Members.Member<? super Z,X> member, JoinType type) {
        super((PathImpl<?,Z>)parent, member, member.getJavaType());
        this.joinType = type;
    }
    
    public JoinType getJoinType() {
        return joinType;
    }
    
    /**
     * Return the metamodel attribute corresponding to the fetch join.
     * @return metamodel attribute for the join
     */
    public Attribute<Z, X> getAttribute() {
        return (Attribute<Z, X>)_member;
    }
    
    public FetchParent<?, Z> getParent() {
        return (FetchParent<?, Z>)_parent;
    }
    
    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> assoc) {
        return addFetch((Members.Member<? super X,Y>)assoc, JoinType.INNER);
    }

    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> assoc) {
        return addFetch((Members.Member<? super X,Y>)assoc, JoinType.INNER);
    }

    public <X,Y> Fetch<X, Y> fetch(String assocName) {
        return fetch(assocName, JoinType.INNER);
    }

    public <Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> assoc, JoinType jt) {
        return addFetch((Members.Member<? super X,Y>)assoc, jt);
    }

    public <Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> assoc, JoinType jt) {
        return addFetch((Members.Member<? super X,Y>)assoc, jt);
    }

    public <X,Y> Fetch<X, Y> fetch(String assocName, JoinType jt) {
        Attribute<? super X, ?> assoc = ((ManagedType<X>)_member.getType()).getAttribute(assocName);
        return addFetch((Members.Member<? super X,Y>)assoc, jt);
    }

    public Set<Fetch<X, ?>> getFetches() {
        Set<Fetch<X,?>> result = new HashSet<Fetch<X,?>>();
        for (Fetch f : _fetches) {
            result.add(f);
        }
        return result;
    }
    
    private <X,Y> Fetch<X,Y> addFetch(Members.Member<? super X, Y> member, JoinType jt) {
        Fetch<X,Y> fetch = new FetchPathImpl(this, member, jt);
        if (_fetches == null)
            _fetches = new HashSet<Fetch<?,?>>();
        _fetches.add(fetch);
        return fetch;
    }

    public StringBuilder asValue(AliasContext q) {
        return super.asValue(q).insert(0, " " + joinType + " JOIN FETCH ");
    }
}
