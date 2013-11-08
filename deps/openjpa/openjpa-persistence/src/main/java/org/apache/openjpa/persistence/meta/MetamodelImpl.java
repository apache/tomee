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
package org.apache.openjpa.persistence.meta;

import static javax.persistence.metamodel.Type.PersistenceType.BASIC;
import static javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE;
import static javax.persistence.metamodel.Type.PersistenceType.ENTITY;
import static javax.persistence.metamodel.Type.PersistenceType.MAPPED_SUPERCLASS;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.StaticMetamodel;
import javax.persistence.metamodel.PluralAttribute.CollectionType;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.QueryContext;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.Resolver;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.meta.Members.Member;
import org.apache.openjpa.util.InternalException;

/**
 * Adapts JPA Metamodel to OpenJPA meta-data repository.
 * 
 * @author Pinaki Poddar
 * 
 */
public class MetamodelImpl implements Metamodel, Resolver {
    private final MetaDataRepository repos;
    private Map<Class<?>, Type<?>> _basics = new HashMap<Class<?>, Type<?>>();
    private Map<Class<?>, EntityType<?>> _entities = new HashMap<Class<?>, EntityType<?>>();
    private Set<EntityType<?>> _entitiesOnlySet = null;
    private Map<Class<?>, EmbeddableType<?>> _embeddables = new HashMap<Class<?>, EmbeddableType<?>>();
    private Map<Class<?>, MappedSuperclassType<?>> _mappedsupers = new HashMap<Class<?>, MappedSuperclassType<?>>();
    private Map<Class<?>, Types.PseudoEntity<?>> _pseudos = new HashMap<Class<?>, Types.PseudoEntity<?>>();

    private static Localizer _loc = Localizer.forPackage(MetamodelImpl.class);

    /**
     * Constructs a model with the current content of the supplied non-null repository.
     * 
     */
    public MetamodelImpl(MetaDataRepository repos) {
        this.repos = repos;
        Collection<Class<?>> classes = repos.loadPersistentTypes(true, null);
        for (Class<?> cls : classes) {
        	ClassMetaData meta = repos.getMetaData(cls, null, true);
            PersistenceType type = getPersistenceType(meta);
            switch (type) {
            case ENTITY:
                find(cls, _entities, ENTITY, false);
                if (meta.isEmbeddable())
                    find(cls, _embeddables, EMBEDDABLE, false);
                break;
            case EMBEDDABLE:
                find(cls, _embeddables, EMBEDDABLE, false);
                break;
            case MAPPED_SUPERCLASS:
                find(cls, _mappedsupers, MAPPED_SUPERCLASS, false);
                break;
            default:
            }
        }
    }
    
    public MetaDataRepository getRepository() {
        return repos;
    }

    /**
     *  Return the metamodel embeddable type representing the embeddable class.
     *  
     *  @param cls  the type of the represented embeddable class
     *  @return the metamodel embeddable type
     *  @throws IllegalArgumentException if not an embeddable class
     */
    public <X> EmbeddableType<X> embeddable(Class<X> clazz) {
        return (EmbeddableType<X>)find(clazz, _embeddables, EMBEDDABLE, false);
    }

    /**
     *  Return the metamodel entity type representing the entity.
     *  @param cls  the type of the represented entity
     *  @return the metamodel entity type
     *  @throws IllegalArgumentException if not an entity
     */
    public <X> EntityType<X> entity(Class<X> clazz) {
        return (EntityType<X>) find(clazz, _entities, ENTITY, false);
    }

    public <X> EntityType<X> entityImpl(Class<X> clazz) {
        return (EntityType<X>) find(clazz, _entities, ENTITY, true);
    }

    /*
     * Return the most up-to-date entity only set in the current meta model.
     */
    private Collection<EntityType<?>> getEntityValuesOnly() {
        if (_entitiesOnlySet == null) {
            _entitiesOnlySet = new HashSet<EntityType<?>>();
            for (Class<?> cls : _entities.keySet()) {
                // if key indicates it is a embeddable, do not add to the _entitiesOnlySet.
                if (!_embeddables.containsKey(cls)) {
                    _entitiesOnlySet.add(_entities.get(cls));
                }
            }
        }
        return _entitiesOnlySet;
    }

    /**
     * Return the metamodel embeddable types.
     * @return the metamodel embeddable types
     */
    public Set<EmbeddableType<?>> getEmbeddables() {
        return unmodifiableSet(_embeddables.values());
    }

    /**
     * Return the metamodel entity types.
     * @return the metamodel entity types
     */
    public Set<EntityType<?>> getEntities() {
        return unmodifiableSet(getEntityValuesOnly());
    }

    /**
     *  Return the metamodel managed types.
     *  @return the metamodel managed types
     */
    public Set<ManagedType<?>> getManagedTypes() {
        Set<ManagedType<?>> result = new HashSet<ManagedType<?>>();
        result.addAll(getEntityValuesOnly());
        result.addAll(_embeddables.values());
        result.addAll(_mappedsupers.values());
        return result;
    }
    
    /**
     *  Return the metamodel managed type representing the 
     *  entity, mapped superclass, or embeddable class.
     *  @param cls  the type of the represented managed class
     *  @return the metamodel managed type
     *  @throws IllegalArgumentException if not a managed class
     */
    public <X> ManagedType<X> managedType(Class<X> clazz) {
        if (_embeddables.containsKey(clazz))
            return (EmbeddableType<X>) _embeddables.get(clazz);
        if (_entities.containsKey(clazz))
            return (EntityType<X>) _entities.get(clazz);
        if (_mappedsupers.containsKey(clazz))
            return (MappedSuperclassType<X>) _mappedsupers.get(clazz);
        throw new IllegalArgumentException(_loc.get("type-not-managed", clazz)
            .getMessage());
    }

    /**
     *  Return the type representing the basic, entity, mapped superclass, or embeddable class.
     *  This method differs from {@linkplain #type(Class)} as it also creates a basic or pesudo
     *  type for the given class argument if not already available in this receiver.
     *  
     *  @param cls  the type of the represented managed class
     *  @return the metamodel managed type
     *  @throws IllegalArgumentException if not a managed class
     */
    public <X> Type<X> getType(Class<X> cls) {
        try {
            return managedType(cls);
        } catch (IllegalArgumentException ex) {
            if (_basics.containsKey(cls))
                return (Type<X>)_basics.get(cls);
            if (_pseudos.containsKey(cls))
                return (Type<X>)_pseudos.get(cls);
            if (java.util.Map.class.isAssignableFrom(cls)) {
                Types.PseudoEntity<X> pseudo = new Types.PseudoEntity(cls, this);
                _pseudos.put(cls, new Types.PseudoEntity(cls, this));
                return pseudo;
            } else {
                Type<X> basic = new Types.Basic<X>(cls);
                _basics.put(cls, basic);
                return basic;
            }
        }
    }

    public static PersistenceType getPersistenceType(ClassMetaData meta) {
        if (meta == null)
            return BASIC;
        if (meta.isAbstract())
            return MAPPED_SUPERCLASS;
        if (meta.isEmbeddable())
            return EMBEDDABLE;
        return ENTITY;
    }

    /**
     * Looks up the given container for the managed type representing the given Java class.
     * The managed type may become instantiated as a side-effect.
     */
    private <V extends ManagedType<?>> V find(Class<?> cls, Map<Class<?>,V> container,  
            PersistenceType expected, boolean implFind) {
        if (container.containsKey(cls)) {
            if (implFind || expected != ENTITY || !_embeddables.containsKey(cls)) {
                return container.get(cls);
            }
        }
        ClassMetaData meta = repos.getMetaData(cls, null, false);
        if (meta != null) {
            instantiate(cls, meta, container, expected);
        }
        return container.get(cls);
    }

    /**
     * Instantiate
     * @param <X>
     * @param <V>
     * @param cls
     * @param container
     * @param expected
     */
    private <X,V extends ManagedType<?>> void instantiate(Class<X> cls, ClassMetaData meta, 
            Map<Class<?>,V> container, PersistenceType expected) {
        PersistenceType actual = getPersistenceType(meta);
        if (actual != expected) {
            if (!meta.isEmbeddable() || actual != PersistenceType.ENTITY ||
                expected != PersistenceType.EMBEDDABLE) 
                throw new IllegalArgumentException( _loc.get("type-wrong-category",
                    cls, actual, expected).getMessage());
        }
        switch (actual) {
        case EMBEDDABLE:
            Types.Embeddable<X> embedded = new Types.Embeddable<X>(meta, this);
            _embeddables.put(cls, embedded);
            populate(embedded);
            // no break : embeddables are stored as both entity and embeddable containers
        case ENTITY:
        	Types.Entity<X> entity = new Types.Entity<X>(meta, this);
            _entities.put(cls, entity);
            _entitiesOnlySet = null;
            populate(entity);
            break;
        case MAPPED_SUPERCLASS:
            Types.MappedSuper<X> mapped = new Types.MappedSuper<X>(meta, this);
            _mappedsupers.put(cls, mapped);
            populate(mapped);
            break;
        default:
            throw new InternalException(cls.getName());
        }
    }

    public <T> Set<T> unmodifiableSet(Collection<T> coll) {
        HashSet<T> result = new HashSet<T>();
        for (T t : coll)
            result.add(t);
        return result;
    }

    static CollectionType categorizeCollection(Class<?> cls) {
        if (Set.class.isAssignableFrom(cls))
            return CollectionType.SET;
        if (List.class.isAssignableFrom(cls))
            return CollectionType.LIST;
        if (Collection.class.isAssignableFrom(cls))
            return CollectionType.COLLECTION;
        if (Map.class.isAssignableFrom(cls))
            return CollectionType.MAP;
        
        throw new InternalException(cls.getName() + " not a collection");
    }
    
    /**
     * Populate the static fields of the canonical type.
     */
    public <X> void populate(AbstractManagedType<X> type) {
        Class<X> cls = type.getJavaType();
        Class<?> mcls = repos.getMetaModel(cls, true);
        if (mcls == null)
            return;
        StaticMetamodel anno = mcls.getAnnotation(StaticMetamodel.class);
        if (anno == null)
            throw new IllegalArgumentException(_loc.get("meta-class-no-anno", 
                    mcls.getName(), cls.getName(), StaticMetamodel.class.getName()).getMessage());

        if (cls != anno.value()) {
            throw new IllegalStateException(_loc.get("meta-class-mismatch",
                    mcls.getName(), cls.getName(), anno.value()).getMessage());
        }
        
        ParameterizedType mfType = null;
        Attribute<? super X, ?> f = null;
        Field[] mfields = AccessController.doPrivileged(J2DoPrivHelper.getDeclaredFieldsAction(mcls));
        for (Field mf : mfields) {
            try {
                mfType = getParameterizedType(mf); // metamodel type
                if (mfType == null) {
                    continue;
                }
                f = type.getAttribute(mf.getName()); // persistent type

                // populate the static field with persistent type information
                mf.set(null, f);
            } catch (Exception e) {
                throw new RuntimeException(_loc.get("meta-field-mismatch",
                        new Object[] { mf.getName(), mcls.getName(), toTypeName(mfType), f.getJavaType().toString() })
                        .getMessage(), e);
            }
        }
    }
    
    /**
     * Gets the parameterized type of the given field after validating. 
     * 
     * @return the field's type as a parameterized type. If the field
     * is not parameterized type (that can happen for non-canonical 
     * metamodel or weaving process introducing synthetic fields),
     * returns null.
     */
    ParameterizedType getParameterizedType(Field mf) {
        java.lang.reflect.Type t = mf.getGenericType();
        if (t instanceof ParameterizedType == false) {
        	repos.getLog().warn(_loc.get("meta-field-not-param", 
            mf.getDeclaringClass(), mf.getName(), toTypeName(t)).getMessage());
        	return null;
        }
        ParameterizedType mfType = (ParameterizedType)t;
        java.lang.reflect.Type[] args = mfType.getActualTypeArguments();
        if (args.length < 2) {
            throw new IllegalStateException(_loc.get("meta-field-less-param", 
            mf.getDeclaringClass(), mf.getName(), toTypeName(t)).getMessage());
        }

        return mfType;
    }
    
    /**
     * Pretty prints a Type. 
     */
    String toTypeName(java.lang.reflect.Type type) {
        if (type instanceof GenericArrayType) {
            return toTypeName(((GenericArrayType)type).
                getGenericComponentType())+"[]";
        }
        if (type instanceof ParameterizedType == false) {
            Class<?> cls = (Class<?>)type;
            return cls.getName();
        }
        ParameterizedType pType = (ParameterizedType)type;
        java.lang.reflect.Type[] args = pType.getActualTypeArguments();
        StringBuilder tmp = new StringBuilder(pType.getRawType().toString());
        for (int i = 0; i < args.length; i++) {
            tmp.append((i == 0) ? '<' : ',');
            tmp.append(toTypeName(args[i]));
            if (i == args.length-1) tmp.append('>');
        }
        return tmp.toString();
    }
    
    /**
     * Validates the given field of the meta class matches the given 
     * FieldMetaData and 
     * @param <X>
     * @param <Y>
     * @param mField
     * @param member
     */
    void validate(Field metaField, FieldMetaData fmd) {
        
    }
    
    <X,Y> void validate(Field mField, Member<X, Y> member) {
        if (!ParameterizedType.class.isInstance(mField.getGenericType())) {
            throw new IllegalArgumentException(_loc.get("meta-bad-field", 
                mField).getMessage());
        }
        ParameterizedType mfType = (ParameterizedType)mField.getGenericType();
        java.lang.reflect.Type[] args = mfType.getActualTypeArguments();
        java.lang.reflect.Type owner = args[0];
        if (member.getDeclaringType().getJavaType() != owner)
            throw new IllegalArgumentException(_loc.get("meta-bad-field-owner", 
                    mField, owner).getMessage());
    }

    public Class classForName(String name, String[] imports) {
        throw new UnsupportedOperationException();
    }

    public AggregateListener getAggregateListener(String tag) {
        throw new UnsupportedOperationException();
    }

    public OpenJPAConfiguration getConfiguration() {
        return repos.getConfiguration();
    }

    public FilterListener getFilterListener(String tag) {
        throw new UnsupportedOperationException();
    }

    public QueryContext getQueryContext() {
        throw new UnsupportedOperationException();
    }    
}
