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

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.meta.Members;
import org.apache.openjpa.persistence.meta.Members.Member;

/**
 * Path is an expression representing a persistent attribute traversed from a parent path.
 * The type of the path is the type of the persistent attribute.
 * If the persistent attribute is bindable, then further path can be traversed from this path. 
 * 
 * @author Pinaki Poddar
 * @author Fay Wang
 * 
 * @param <Z> the type of the parent path 
 * @param <X> the type of this path
 */
class PathImpl<Z,X> extends ExpressionImpl<X> implements Path<X> {
    protected final PathImpl<?,Z> _parent;
    protected final Members.Member<? super Z,?> _member;
    private boolean isEmbedded = false;
    private PathImpl<?,?> _correlatedPath;
    
    /**
     * Protected constructor use by root path which neither represent a member nor has a parent. 
     */
    protected PathImpl(Class<X> cls) {
        super(cls);
        _parent = null;
        _member = null;
    }
    
    /**
     * Create a path from the given parent representing the given member. 
     * 
     * @param parent the path from which this path needs to be constructed. Must not be null.
     * @param member the persistent property that represents this path.
     * @param cls denotes the type expressed by this path.
     */
    public PathImpl(PathImpl<?,Z> parent, Members.Member<? super Z, ?> member, Class<X> cls) {
        super(cls);
        _parent = parent;
        if (_parent.isEmbedded) {
            FieldMetaData fmd = getEmbeddedFieldMetaData(member.fmd);
            _member = new Members.SingularAttributeImpl(member.owner, fmd);
        } else {
            _member = member;
        }
        isEmbedded = _member.fmd.isElementCollection() ? _member.fmd.getElement().isEmbedded() : 
            _member.fmd.isEmbedded();
    }

    /** 
     * Gets the bindable object that corresponds to the path expression.
     *  
     * @throws IllegalArgumentException if this path is not bindable 
     */
    public Bindable<X> getModel() { 
        if (_member instanceof Bindable<?> == false) {
            throw new IllegalArgumentException(this + " represents a basic path and not a bindable");
        }
        return (Bindable<X>)_member;
    }
    
    /**
     *  Gets the parent of this path or null if this path is the root.
     */
    public final Path<Z> getParentPath() {
        return _parent;
    }
    
    /**
     * Gets the path that originates this traversal. Can be itself if this path is the root.
     */
    public PathImpl<?,?> getInnermostParentPath() {
        return (_parent == null) ? this : _parent.getInnermostParentPath();
    }

    /**
     * Gets the field that may have been embedded inside the given field. 
     * For example, a given primary key field which is using an embedded class as a complex primary key. 
     * @param fmd a given field
     * @return the embedded field or the given field itself
     */
    protected FieldMetaData getEmbeddedFieldMetaData(FieldMetaData fmd) {
        Members.Member<?,?> member = getInnermostMember(_parent,_member);
    	
        ClassMetaData embeddedMeta = member.fmd.isElementCollection() 
        		? member.fmd.getElement().getEmbeddedMetaData() 
        		: member.fmd.getEmbeddedMetaData();
        
        return (embeddedMeta != null) ? embeddedMeta.getField(fmd.getName()) : fmd;
    }
    
    protected Members.Member<?,?> getInnermostMember(PathImpl<?,?> parent, Members.Member<?,?> member) {
        return member != null ? member : getInnermostMember(parent._parent,  parent._member); 
    }
    
    /**
     * Makes this path correlated to the given path.  
     */
    public void setCorrelatedPath(PathImpl<?,?> correlatedPath) {
        _correlatedPath = correlatedPath;
    }
    
    /**
     * Gets the path correlated to this path, if any.
     */
    public PathImpl<?,?> getCorrelatedPath() {
        return _correlatedPath;
    }
    
    /**
     * Affirms if this path is correlated to another path.
     */
    public boolean isCorrelated() {
        return _correlatedPath != null;
    }
    
    /**
     * Convert this path to a kernel path.
     */
    @Override
    public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> q) {
        if (q.isRegistered(this))
            return q.getRegisteredValue(this);
        org.apache.openjpa.kernel.exps.Path path = null;
        SubqueryImpl<?> subquery = q.getDelegator();
        boolean allowNull = _parent == null ? false : _parent instanceof Join 
            && ((Join<?,?>)_parent).getJoinType() != JoinType.INNER;
        PathImpl<?,?> corrJoin = getCorrelatedJoin(this);
        PathImpl<?,?> corrRoot = getCorrelatedRoot(subquery);
        if (_parent != null && q.isRegistered(_parent)) {
            path = factory.newPath(q.getRegisteredVariable(_parent));
            path.setSchemaAlias(q.getAlias(_parent));
            path.get(_member.fmd, allowNull);
        } else if (_parent != null && _parent._correlatedPath != null && q.isRegistered(_parent._correlatedPath)){
            path = factory.newPath(q.getRegisteredVariable(_parent._correlatedPath));
            path.setSchemaAlias(q.getAlias(_parent._correlatedPath));
            path.get(_member.fmd, allowNull);
        } else if (corrJoin != null || corrRoot != null) {
            org.apache.openjpa.kernel.exps.Subquery subQ = subquery.getSubQ();
            path = factory.newPath(subQ);
            path.setMetaData(subQ.getMetaData());
            path.setSchemaAlias(q.getAlias(_parent));
            traversePath(_parent, path, _member.fmd);
        } else if (_parent != null) {
            Value val = _parent.toValue(factory, q);
            if (val instanceof org.apache.openjpa.kernel.exps.Path) {
                path = (org.apache.openjpa.kernel.exps.Path)val;
                path.get(_member.fmd, allowNull);
            } else {
                val.setAlias(q.getAlias(this));
                return val;
            }
        } else if (_parent == null) {
            path = factory.newPath();
            path.setMetaData(q.getMetamodel().getRepository().getCachedMetaData(getJavaType()));
        }
        if (_member != null && !_member.isCollection()) {
            path.setImplicitType(getJavaType());
        }
        path.setAlias(q.getAlias(this));
        return path;
    }
    
    public PathImpl<?,?> getCorrelatedRoot(SubqueryImpl<?> subquery) {
        if (subquery == null)
            return null;
        PathImpl<?,?> root = getInnermostParentPath();
        if (subquery.getRoots() != null && subquery.getRoots().contains(this))
            return root;
        return null;
    }
    
    
    public PathImpl<?,?> getCorrelatedJoin(PathImpl<?,?> path) {
        if (path._correlatedPath != null)
            return path._correlatedPath;
        if (path._parent == null)
            return null;
        return getCorrelatedJoin(path._parent);
    }
    
    /**
     * Affirms if this receiver occurs in the roots of the given subquery.
     */
    public boolean inSubquery(SubqueryImpl<?> subquery) {
        return subquery != null && (subquery.getRoots() == null ? false : subquery.getRoots().contains(this));
    }
    
    protected void traversePath(PathImpl<?,?> parent,  org.apache.openjpa.kernel.exps.Path path, FieldMetaData fmd) {
        boolean allowNull = parent == null ? false : parent instanceof Join 
            && ((Join<?,?>)parent).getJoinType() != JoinType.INNER;
        FieldMetaData fmd1 = parent._member == null ? null : parent._member.fmd;
        PathImpl<?,?> parent1 = parent._parent;
        if (parent1 == null || parent1.getCorrelatedPath() != null) {
            if (fmd != null) 
                path.get(fmd, allowNull);
            return;
        }
        traversePath(parent1, path, fmd1);
        if (fmd != null) 
            path.get(fmd, allowNull);
    }
    
    /**
     *  Gets a new path that represents the given single-valued attribute from this path.
     */
    public <Y> Path<Y> get(SingularAttribute<? super X, Y> attr) {
    	if (getType() != attr.getDeclaringType()) {
    		attr = (SingularAttribute)((ManagedType)getType()).getAttribute(attr.getName());
    	}
        return new PathImpl<X,Y>(this, (Members.SingularAttributeImpl<? super X, Y>)attr, attr.getJavaType());
    }
    
    /**
     *  Gets a new path that represents the given multi-valued attribute from this path.
     */
    public <E, C extends java.util.Collection<E>> Expression<C>  get(PluralAttribute<X, C, E> coll) {
    	if (getType() != coll.getDeclaringType()) {
    		coll = (PluralAttribute)((ManagedType)getType()).getAttribute(coll.getName());
    	}
        return new PathImpl<X,C>(this, (Members.PluralAttributeImpl<? super X, C, E>)coll, coll.getJavaType());
    }

    /**
     *  Gets a new path that represents the given map-valued attribute from this path.
     */
    public <K, V, M extends java.util.Map<K, V>> Expression<M> get(MapAttribute<X, K, V> map) {
    	if (getType() != map.getDeclaringType()) {
    		map = (MapAttribute)((ManagedType)getType()).getAttribute(map.getName());
    	}
        return new PathImpl<X,M>(this, (Members.MapAttributeImpl<? super X,K,V>)map, (Class<M>)map.getJavaType());
    }
    
    /**
     * Gets a new path that represents the attribute of the given name from this path.
     * 
     * @exception IllegalArgumentException if this path represents a basic attribute that is can not be traversed 
     * further.
     */
    public <Y> Path<Y> get(String attName) {
        Type<?> type = this.getType();
        if (type.getPersistenceType() == PersistenceType.BASIC) {
            throw new IllegalArgumentException(this + " is a basic path and can not be navigated to " + attName);
        }
        
        Members.Member<? super X, Y> next = (Members.Member<? super X, Y>) 
           ((ManagedType<? super X>)type).getAttribute(attName);
        return new PathImpl<X,Y>(this, next, next.getJavaType());
    }
    
    public Type<?> getType() {
        return _member.getType();
    }

    @SuppressWarnings("unchecked")
    public Member<? extends Z, X> getMember() {
        return (Member<? extends Z, X>) _member;
    }
    
    /**
     * Get the type() expression corresponding to this path. 
     */
    public Expression<Class<? extends X>> type() {
        return new Expressions.Type<Class<? extends X>>(this);
    }
    
    public StringBuilder asValue(AliasContext q) {
        StringBuilder buffer = new StringBuilder();
        if (_parent != null) {
            Value var = q.getRegisteredVariable(_parent);
            buffer.append(var != null ? var.getName() : _parent.asValue(q)).append(".");
        }
        if (_member != null) {
            buffer.append(_member.fmd.getName());
        } 
        return buffer;
    }
    
    public StringBuilder asVariable(AliasContext q) {
        Value var = q.getRegisteredVariable(this);
        return asValue(q).append(" ").append(var == null ? "?" : var.getName());
    }
}
