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

import java.util.ArrayList;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.PluralJoin;
import javax.persistence.criteria.SetJoin;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.Type;

import org.apache.openjpa.kernel.exps.AbstractExpressionBuilder;
import org.apache.openjpa.kernel.exps.ExpressionFactory;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.meta.AbstractManagedType;
import org.apache.openjpa.persistence.meta.Members;
import org.apache.openjpa.persistence.meta.Members.KeyAttributeImpl;
import org.apache.openjpa.persistence.meta.Members.MapAttributeImpl;
import org.apache.openjpa.persistence.meta.Members.Member;

/**
 * Implements strongly-typed Join expressions via singular and plural attributes.
 * 
 * @author Fay Wang
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 * 
 */
abstract class Joins {

    static Join clone(Join join) {
        java.util.List<Members.Member> members = new ArrayList<Members.Member>();
        java.util.List<JoinType> jts = new ArrayList<JoinType>();
        FromImpl<?, ?> root = getMembers((PathImpl)join, members, jts);
        Members.Member<?, ?> member = members.get(0);
        JoinType jt = jts.get(0);
        Join join1 = makeJoin(root, member, jt);
        for (int i = 1; i < members.size(); i++)
            join1 = makeJoin((FromImpl<?, ?>) join1, members.get(i), jts.get(i));

        return join1;
    }

    static Join<?, ?> makeJoin(FromImpl<?, ?> from, Members.Member member,
            JoinType jt) {
        if (member instanceof Members.SingularAttributeImpl)
            return new Joins.SingularJoin(from,
                    (Members.SingularAttributeImpl) member, jt);
        else if (member instanceof Members.CollectionAttributeImpl)
            return new Joins.Collection(from,
                    (Members.CollectionAttributeImpl) member, jt);
        else if (member instanceof Members.ListAttributeImpl)
            return new Joins.List(from, (Members.ListAttributeImpl) member, jt);
        else if (member instanceof Members.SetAttributeImpl)
            return new Joins.Set(from, (Members.SetAttributeImpl) member, jt);
        else if (member instanceof Members.MapAttributeImpl)
            return new Joins.Map(from, (Members.MapAttributeImpl) member, jt);
        return null;
    }

    static FromImpl getMembers(PathImpl join,
            java.util.List<Members.Member> members,
            java.util.List<JoinType> jts) {
        PathImpl parent = (PathImpl) join.getParentPath();
        Members.Member member = join.getMember();
        JoinType jt = ((Join) join).getJoinType();
        FromImpl<?, ?> from = null;
        if (parent instanceof RootImpl) {
            members.add(member);
            jts.add(jt);
            return (FromImpl) parent;
        } else {
            from = getMembers(parent, members, jts);
        }
        members.add(member);
        jts.add(jt);
        return from;
    }

    /**
     * Join a single-valued attribute.
     * 
     *
     * @param <Z> type from which joining
     * @param <X> type of the attribute being joined
     */
    static class SingularJoin<Z,X> extends FromImpl<Z,X> implements Join<Z,X> {
        private final JoinType joinType;
        private boolean allowNull = false;
        
        public SingularJoin(FromImpl<?,Z> from, Members.SingularAttributeImpl<? super Z, X> member, JoinType jt) {
            super(from, member, member.getJavaType());
            joinType = jt;
            allowNull = joinType != JoinType.INNER;
        }
        
        public JoinType getJoinType() {
            return joinType;
        }

        public FromImpl<?, Z> getParent() {
            return (FromImpl<?, Z>) _parent;
        }
        
        public Member<? extends Z, X> getMember() {
            return (Member<? extends Z, X>) _member;
        }
        
        /**
         * Return the metamodel attribute corresponding to the join.
         * @return metamodel attribute type corresponding to the join
         */
        public Attribute<? super Z, ?> getAttribute() {
            return  (Attribute<? super Z, ?> )_member;
        }
        
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> c) {
            ClassMetaData meta = _member.fmd.getDeclaredTypeMetaData();
            org.apache.openjpa.kernel.exps.Path path = null;
            SubqueryImpl<?> subquery = c.getDelegator();
            PathImpl<?,?> parent = getInnermostParentPath();
            Value val = c.getRegisteredValue(this);
            if (val != null)
                return val;
            else if (parent.inSubquery(subquery)) {
                org.apache.openjpa.kernel.exps.Subquery subQ = subquery.getSubQ();
                path = factory.newPath(subQ);
                path.setMetaData(subQ.getMetaData());
                path.setSchemaAlias(c.getAlias(this));
                path.get(_member.fmd, allowNull); 
            } else {
                path = (org.apache.openjpa.kernel.exps.Path) _parent.toValue(factory, c);
                path.get(_member.fmd, allowNull);
                path.setMetaData(meta);
                path.setImplicitType(meta.getDescribedType());
            }
            return path;
        }
        
        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, 
            CriteriaQueryImpl<?> c) {
            ClassMetaData meta = _member.fmd.getDeclaredTypeMetaData();
            org.apache.openjpa.kernel.exps.Path path = null;
            SubqueryImpl<?> subquery = c.getDelegator();
            PathImpl<?,?> parent = getInnermostParentPath();
            org.apache.openjpa.kernel.exps.Expression filter = null;
            PathImpl<?,?> correlatedParentPath = null;
            boolean bind = true;
            java.util.Set<Join<?,?>> corrJoins = null;
            org.apache.openjpa.kernel.exps.Expression join = null;
            if (!isCorrelated()) {
                if (subquery != null) {
                    corrJoins = subquery.getCorrelatedJoins();
                    org.apache.openjpa.kernel.exps.Subquery subQ = subquery.getSubQ();
                    if ((!corrJoins.isEmpty() && corrJoins.contains(_parent)) || 
                        (corrJoins.isEmpty() && parent.inSubquery(subquery) && _parent.getCorrelatedPath() != null)) { 
                        path = factory.newPath(subQ);
                        correlatedParentPath = _parent.getCorrelatedPath();
                        bind = false;
                    } else { 
                        if (c.isRegistered(_parent)) { 
                            Value var = c.getRegisteredVariable(_parent);
                            path = factory.newPath(var);
                        } else {
                            path = factory.newPath(subQ);
                        }
                        path.setMetaData(meta);
                        path.get(_member.fmd, allowNull);
                        path.setSchemaAlias(c.getAlias(_parent));
                    } 
                } else if (c.isRegistered(_parent)) {
                    Value var = c.getRegisteredVariable(_parent);
                    path = factory.newPath(var);
                    path.setMetaData(meta);
                    path.get(_member.fmd, allowNull);
                } else            
                    path = (org.apache.openjpa.kernel.exps.Path)toValue(factory, c);
                
                Class<?> type = meta == null ? AbstractExpressionBuilder.TYPE_OBJECT : meta.getDescribedType();
                Value var = null;
                if (bind) {
                    var = factory.newBoundVariable(c.getAlias(this),type);
                    join = factory.bindVariable(var, path);
                    c.registerVariable(this, var, path);
                }
                
                if (!_member.fmd.isTypePC()) { // multi-valued relation
                    setImplicitContainsTypes(path, var, AbstractExpressionBuilder.CONTAINS_TYPE_ELEMENT);
                    join = factory.contains(path, var);
                }                
            }
            if (getJoins() != null) {
                for (Join<?, ?> join1 : getJoins()) {
                    filter = Expressions.and(factory, 
                             ((FromImpl<?,?>)join1).toKernelExpression(factory, c), filter);
                }
            }
            org.apache.openjpa.kernel.exps.Expression expr = Expressions.and(factory, join, filter);
            
            if (correlatedParentPath == null) {
                return expr;
            } else {
                org.apache.openjpa.kernel.exps.Path parentPath = null;
                if (corrJoins != null && corrJoins.contains(_parent)) {
                    Value var = getVariableForCorrPath(subquery, correlatedParentPath);
                    parentPath = factory.newPath(var);
                } else {
                    parentPath = (org.apache.openjpa.kernel.exps.Path)correlatedParentPath.toValue(factory, c);
                }
                parentPath.get(_member.fmd, allowNull);
                parentPath.setSchemaAlias(c.getAlias(correlatedParentPath));
                if (c.ctx().getParent() != null && c.ctx().getVariable(parentPath.getSchemaAlias()) == null) 
                    parentPath.setSubqueryContext(c.ctx(), parentPath.getSchemaAlias());
                
                path.setMetaData(meta);
                //filter = bindVariableForKeyPath(path, alias, filter);
                filter = factory.equal(parentPath, path);
                return Expressions.and(factory, expr, filter);
            }
        }
        
        private Value getVariableForCorrPath(SubqueryImpl<?> subquery, PathImpl<?,?> path) {
            AbstractQuery<?> parent = subquery.getParent();
            if (parent instanceof CriteriaQueryImpl) {
                return ((CriteriaQueryImpl<?>)parent).getRegisteredVariable(path);
            }
            Value var = ((SubqueryImpl<?>)parent).getDelegate().getRegisteredVariable(path); 
            if (var != null)
                return var;
            return getVariableForCorrPath((SubqueryImpl<?>)parent, path);
        }
        
        /**
         * Set the implicit types of the given values based on the fact that
         * the first is supposed to contain the second.
         */
        public void setImplicitContainsTypes(Value val1, Value val2, int op) {
            if (val1.getType() == AbstractExpressionBuilder.TYPE_OBJECT) {
                if (op == AbstractExpressionBuilder.CONTAINS_TYPE_ELEMENT)
                    val1.setImplicitType(Collection.class);
                else
                    val1.setImplicitType(Map.class);
            }

            if (val2.getType() == AbstractExpressionBuilder.TYPE_OBJECT && val1 instanceof Path) {
                FieldMetaData fmd = ((org.apache.openjpa.kernel.exps.Path) val1).last();
                ClassMetaData meta;
                if (fmd != null) {
                    if (op == AbstractExpressionBuilder.CONTAINS_TYPE_ELEMENT || 
                        op == AbstractExpressionBuilder.CONTAINS_TYPE_VALUE) {
                        val2.setImplicitType(fmd.getElement().getDeclaredType());
                        meta = fmd.getElement().getDeclaredTypeMetaData();
                        if (meta != null) {
                            val2.setMetaData(meta);
                        }
                    } else {
                        val2.setImplicitType(fmd.getKey().getDeclaredType());
                        meta = fmd.getKey().getDeclaredTypeMetaData();
                        if (meta != null) {
                            val2.setMetaData(meta);
                        }
                    }
                }
            }
        }
        
        @Override
        public StringBuilder asVariable(AliasContext q) {
            return new StringBuilder(" " + joinType + " JOIN ").append(super.asVariable(q));
        }
    }
    
    /**
     * Join a plural attribute.
     * 
     * @param Z type being joined from
     * @param C Java collection type of the container
     * @param E type of the element being joined to
     * 
     */
    static abstract class AbstractCollection<Z,C,E> extends FromImpl<Z,E> implements PluralJoin<Z, C, E> {
        private final JoinType joinType;
        private boolean allowNull = false;
        
        public AbstractCollection(FromImpl<?,Z> from, Members.PluralAttributeImpl<? super Z, C, E> member, 
            JoinType jt) {
            super(from, member, member.getBindableJavaType());
            joinType = jt;
            allowNull = joinType != JoinType.INNER;
        }
        
        public final JoinType getJoinType() {
            return joinType;
        }

        /**
         * Gets the parent of this join.
         */
        public final FromImpl<?, Z> getParent() {
            return (FromImpl<?, Z>) _parent;
        }
        
        public Attribute<? super Z, E> getAttribute() {
            return (Member<? super Z, E>)_member;
        }
        
        public PluralAttribute<? super Z, C, E> getModel() {
            return (PluralAttribute<? super Z, C, E>) _member.getType();
        }
        
        public ClassMetaData getMemberClassMetaData() {
            return _member.fmd.isElementCollection() 
                ? _member.fmd.getElement().getEmbeddedMetaData()
                : _member.fmd.getElement().getDeclaredTypeMetaData();
        }

        /**
         * Convert this path to a kernel path (value).
         */
        @Override
        public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> c) {
            org.apache.openjpa.kernel.exps.Path path = null;
            SubqueryImpl<?> subquery = c.getDelegator();
            PathImpl<?,?> parent = getInnermostParentPath();
            
            Value var = c.getRegisteredVariable(this);
            if (var != null) {
                 path = factory.newPath(var);
            } else if (parent.inSubquery(subquery)) {
                org.apache.openjpa.kernel.exps.Subquery subQ = subquery.getSubQ();
                path = factory.newPath(subQ);
                path.setMetaData(subQ.getMetaData());
                path.setSchemaAlias(c.getAlias(this));
            } else {
                path = (org.apache.openjpa.kernel.exps.Path) _parent.toValue(factory, c);
                path.get(_member.fmd, allowNull);
            }
            return path;
        }

        /**
         * Convert this path to a join expression.
         * 
         */
        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, 
            CriteriaQueryImpl<?> c) {
            ClassMetaData meta = getMemberClassMetaData(); 
            org.apache.openjpa.kernel.exps.Path path = null;
            SubqueryImpl<?> subquery = c.getDelegator();
            org.apache.openjpa.kernel.exps.Expression filter = null;
            java.util.Set<Join<?,?>> corrJoins = null;
            boolean bind = true;
            org.apache.openjpa.kernel.exps.Expression join = null;
            PathImpl<?,?> corrJoin = getCorrelatedJoin(this);
            PathImpl<?,?> corrRoot = getCorrelatedRoot(subquery);

            PathImpl<?,?> correlatedParentPath = null;
            if (!isCorrelated()) {
                if (subquery != null) {
                    corrJoins = subquery.getCorrelatedJoins();
                    org.apache.openjpa.kernel.exps.Subquery subQ = subquery.getSubQ();
                    path = factory.newPath(subQ); 
                    if ((corrJoin != null || corrRoot != null) && _parent.getCorrelatedPath() != null) { 
                        subQ.setSubqAlias(c.getAlias(this));
                        path = factory.newPath(subQ);
                        correlatedParentPath = _parent.getCorrelatedPath();
                        bind = false;
                    } else {    
                        if (c.isRegistered(_parent)) { 
                            Value var = c.getRegisteredVariable(_parent);
                            path = factory.newPath(var);
                        } else {
                            path = factory.newPath(subQ);
                        }
                        path.setMetaData(meta);
                        path.get(_member.fmd, allowNull);
                        path.setSchemaAlias(c.getAlias(_parent));
                    } 
                } else if (c.isRegistered(_parent)) {
                    Value var = c.getRegisteredVariable(_parent);
                    path = factory.newPath(var);
                    path.setMetaData(meta);
                    path.get(_member.fmd, allowNull);
                } else {           
                    path = (org.apache.openjpa.kernel.exps.Path)toValue(factory, c);
                }
                Class<?> type = meta == null ? AbstractExpressionBuilder.TYPE_OBJECT : meta.getDescribedType(); 
                if (bind) {
                    Value var = factory.newBoundVariable(c.getAlias(this), type);
                    join = factory.bindVariable(var, path);
                    c.registerVariable(this, var, path);
                }
            }
            if (getJoins() != null) {
                for (Join<?, ?> join1 : getJoins()) {
                    filter = Expressions.and(factory, 
                        ((FromImpl<?,?>)join1).toKernelExpression(factory, c), filter);
                }
            }
            org.apache.openjpa.kernel.exps.Expression expr = Expressions.and(factory, join, filter);
            if (correlatedParentPath == null) {
                return expr;
            } else {
                org.apache.openjpa.kernel.exps.Path parentPath = null;
                if (!corrJoins.isEmpty() && corrJoins.contains(_parent)) {
                    Value var = getVariableForCorrPath(subquery, correlatedParentPath);
                    parentPath = factory.newPath(var);
                } else {
                    parentPath = (org.apache.openjpa.kernel.exps.Path) correlatedParentPath.toValue(factory, c);
                }
                parentPath.get(_member.fmd, allowNull);
                parentPath.setSchemaAlias(c.getAlias(correlatedParentPath));
                if (c.ctx().getParent() != null && c.ctx().getVariable(parentPath.getSchemaAlias()) == null) 
                    parentPath.setSubqueryContext(c.ctx(), parentPath.getSchemaAlias());
                
                path.setSchemaAlias(c.getAlias(correlatedParentPath));
                path.setMetaData(meta);
                Class<?> type = meta == null ? AbstractExpressionBuilder.TYPE_OBJECT : meta.getDescribedType(); 
                Value var = factory.newBoundVariable(c.getAlias(this), type);
                join = factory.bindVariable(var, parentPath);
                
                if (_member.fmd.getDeclaredTypeCode() == JavaTypes.MAP)
                    c.registerVariable(this, var, parentPath);
                
                if (_member.fmd.isElementCollection()) {
                    filter = Expressions.and(factory, join, filter);
                } else { 
                    filter = factory.equal(parentPath, path);
                }
                return Expressions.and(factory, expr, filter);
            }
        }
        
        private Value getVariableForCorrPath(SubqueryImpl<?> subquery, PathImpl<?,?> path) {
            AbstractQuery<?> parent = subquery.getParent();
            if (parent instanceof CriteriaQueryImpl) {
                return ((CriteriaQueryImpl<?>)parent).getRegisteredVariable(path);
            }
            Value var = ((SubqueryImpl<?>)parent).getDelegate().getRegisteredVariable(path); 
            if (var != null)
                return var;
            return getVariableForCorrPath((SubqueryImpl<?>)parent, path);
        }
        
        @Override
        public StringBuilder asVariable(AliasContext q) {
            return new StringBuilder(" " + joinType + " JOIN ").append(super.asVariable(q));
        }
    }
    
    /**
     * Join a java.util.Collection&lt;E&gt; type attribute.
     *
     * @param <Z> the type from which being joined
     * @param <E> the type of the the collection attribute elements
     */
    static class Collection<Z,E> extends AbstractCollection<Z,java.util.Collection<E>,E> 
        implements CollectionJoin<Z,E> {
        public Collection(FromImpl<?,Z> parent, Members.CollectionAttributeImpl<? super Z, E> member, JoinType jt) {
            super(parent, member, jt);
        }
        
        public CollectionAttribute<? super Z, E> getModel() {
            return (CollectionAttribute<? super Z, E>)_member;
        }
    }
    
    /**
     * Join a java.util.Set&lt;E&gt; type attribute.
     *
     * @param <Z> the type from which being joined
     * @param <E> the type of the the set attribute elements
     */
    static class Set<Z,E> extends AbstractCollection<Z,java.util.Set<E>,E> 
        implements SetJoin<Z,E> {
        public Set(FromImpl<?,Z> parent, Members.SetAttributeImpl<? super Z, E> member, JoinType jt) {
            super(parent, member, jt);
        }
        
        public SetAttribute<? super Z, E> getModel() {
            return (SetAttribute<? super Z, E>)_member;
        }
    }
    
    /**
     * Join a java.util.List&lt;E&gt; type attribute.
     *
     * @param <Z> the type from which being joined
     * @param <E> the type of the the list attribute elements
     */
    
    static class List<Z,E> extends AbstractCollection<Z,java.util.List<E>,E> 
        implements ListJoin<Z,E> {
        
        public List(FromImpl<?,Z> parent, Members.ListAttributeImpl<? super Z, E> member, JoinType jt) {
            super(parent, member, jt);
        }
        
        public ListAttribute<? super Z, E> getModel() {
            return (ListAttribute<? super Z, E>)_member;
        }
        
        public Expression<Integer> index() {
            return new Expressions.Index(this);
        }
    }
    
    /**
     * Join a java.util.Map&lt;K,V&gt; type attribute.
     *
     * @param <Z> the type from which being joined
     * @param <K> the type of the the map attribute keys
     * @param <V> the type of the the map attribute values
     */
    
    static class Map<Z,K,V> extends AbstractCollection<Z,java.util.Map<K,V>,V> 
        implements MapJoin<Z,K,V> {
        private KeyJoin<K,V> _keyJoin;
        
        public Map(FromImpl<?,Z> parent, Members.MapAttributeImpl<? super Z, K,V> member, JoinType jt) {
            super(parent, member, jt);
        }
        
        public MapAttribute<? super Z, K,V> getModel() {
            return (MapAttribute<? super Z, K,V>) _member;
        }
        
        public Join<java.util.Map<K, V>, K> joinKey() {
            return joinKey(JoinType.INNER);
        }
        
        /**
         * Create a pseudo-attribute of a pseudo-managed type for java.util.Map&lt;K,V&gt; 
         * to represent its keys of type java.util.Set&lt;V&gt;.
         */
        public Join<java.util.Map<K, V>, K> joinKey(JoinType jt) {
            AbstractManagedType<java.util.Map<K,V>> pseudoOwner = (AbstractManagedType<java.util.Map<K,V>>)
               _member.owner.model.getType(getModel().getJavaType());
            KeyAttributeImpl<java.util.Map<K,V>, K> keyAttr = 
              new Members.KeyAttributeImpl<java.util.Map<K,V>, K>(pseudoOwner, _member.fmd);
            _keyJoin = new KeyJoin<K, V>((FromImpl<?,java.util.Map<K,V>>)this, keyAttr, jt);
            return _keyJoin;
        }
        
        public Expression<java.util.Map.Entry<K, V>> entry() {
            return new MapEntry<K,V>(this);
        }
        
        public Path<K> key() {
            return new MapKey<Z,K>(this);
        }
        
        public Path<V> value() {
            return this;
        }
                
        @Override
        public org.apache.openjpa.kernel.exps.Expression toKernelExpression(ExpressionFactory factory, 
            CriteriaQueryImpl<?> c) {
            return (_keyJoin == null) 
                ? super.toKernelExpression(factory, c)
                : _keyJoin.toKernelExpression(factory, c);
        }
    }
    
       
   static class MapKey<Z,K> extends PathImpl<Z,K> {
       private final Map<?,K,?> map;
       private final MapAttributeImpl<Z, K, ?> attr;
       
       public MapKey(Map<Z,K,?> joinMap){
           super(((MapAttribute<Z, K, ?>)joinMap.getAttribute()).getKeyJavaType());
           attr = ((MapAttributeImpl<Z, K, ?>)joinMap.getAttribute());
           this.map = joinMap;
       }
       
       @Override
       public Type<?> getType() {
           return attr.getKeyType();
       }
       
       /**
        * Convert this path to a join expression.
        * 
        */
       @Override
       public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> c) {
           Value val = c.getRegisteredVariable(map);
           org.apache.openjpa.kernel.exps.Path path = factory.newPath(val);
           return factory.getKey(path);
       }
       
       @Override
       public StringBuilder asValue(AliasContext q) {
           StringBuilder buffer = new StringBuilder("KEY(");
           Value var = q.getRegisteredVariable(map);
           buffer.append(var != null ? var.getName() : map.asValue(q)).append(")");
           return buffer;
       }
   }
       
   static class MapEntry<K,V> extends ExpressionImpl<java.util.Map.Entry<K,V>> {
       private final Map<?,K,V> map;
       
       public MapEntry(Map<?,K,V> joinMap){
           super(((MapAttribute)joinMap.getAttribute()).getJavaType());
           this.map = joinMap;
       }
       
       /**
        * Convert this path to a join expression.
        * 
        */
       @Override
       public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> c) {
           Value val = c.getRegisteredVariable(map);
           org.apache.openjpa.kernel.exps.Path path = factory.newPath(val);
           org.apache.openjpa.kernel.exps.Path var = factory.newPath(val);
           return factory.mapEntry(path, var);
       }
       
       @Override
       public StringBuilder asValue(AliasContext q) {
           StringBuilder buffer = new StringBuilder("ENTRY(");
           Value var = q.getRegisteredVariable(map);
           buffer.append(var != null ? var.getName() : map.asValue(q)).append(")");
           return buffer;
       }
   }
   
   /**
    * A specialized join via key of a java.util.Map&lt;K,V&gt; attribute.
    * Treats the map key as a pseudo-attribute of type java.util.Set&lt;K&gt; of a pseduo-managed type corresponding
    * to java.util.Map&lt;K,V&gt;. 
    *  
    * @param <K> the type of the key of the original java.util.Map attribute 
    * @param <V> the type of the value of the original java.util.Map attribute
    */
   static class KeyJoin<K,V> extends Joins.Set<java.util.Map<K, V>, K> {
    public KeyJoin(FromImpl<?, java.util.Map<K, V>> parent, KeyAttributeImpl<? super java.util.Map<K, V>, K> member, 
            JoinType jt) {
        super(parent, member, jt);
    }
    
    @Override
    public Value toValue(ExpressionFactory factory, CriteriaQueryImpl<?> c) {
        return factory.getKey(getParent().toValue(factory, c));
    }
   }
}
