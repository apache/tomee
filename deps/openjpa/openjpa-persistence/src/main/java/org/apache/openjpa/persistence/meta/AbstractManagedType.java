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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.PluralAttribute.CollectionType;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Implements the managed persistent type and its attributes.
 * 
 * Provides identity and version attribute facilities for Identifiable type but does not
 * implement it.
 * 
 * @author Pinaki Poddar
 *
 * @param <X> the 
 */
public abstract class AbstractManagedType<X> extends Types.BaseType<X> 
    implements ManagedType<X> {
    
    private static final Localizer _loc = Localizer.forPackage(AbstractManagedType.class);
    public final MetamodelImpl model;
    public final ClassMetaData meta;

    private java.util.Set<Attribute<? super X, ?>> attrs = new HashSet<Attribute<? super X, ?>>();

    private final DeclaredAttributeFilter<X> declaredAttributeFilter;
    private final SingularAttributeFilter<X> singularAttributeFilter;
    private final SingularAttributeFilter<X> pluralAttributeFilter;
    
    /**
     * A protected constructor for creating psudo-managed types.
     */
    protected AbstractManagedType(Class<X> cls, MetamodelImpl model) {
        super(cls);
        this.model = model;
        this.meta = null;
        declaredAttributeFilter = null;
        singularAttributeFilter = null;
        pluralAttributeFilter   = null;
    }
    
    /**
     * Construct a managed type. The supplied metadata must be resolved i.e. all
     * its fields populated. Because this receiver will populate its attributes
     * corresponding to the available fields of the metadata.
     * 
     */
    public AbstractManagedType(ClassMetaData meta, MetamodelImpl model) {
        super((Class<X>) meta.getDescribedType());

        this.model = model;
        this.meta = meta;
        FieldMetaData[] fmds = meta.getFields();
        for (FieldMetaData f : fmds) {
            int decCode = f.getDeclaredTypeCode();
            switch (decCode) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Boolean>(this, f));
                break;
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Byte>(this, f));
                break;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Character>(this, f));
                break;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Double>(this, f));
                break;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Float>(this, f));
                break;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Integer>(this, f));
                break;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Long>(this, f));
                break;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                attrs.add(new Members.SingularAttributeImpl<X, Short>(this, f));
                break;
            case JavaTypes.STRING:
                attrs.add(new Members.SingularAttributeImpl<X, String>(this, f));
                break;
            case JavaTypes.NUMBER:
                attrs.add(new Members.SingularAttributeImpl<X, Number>(this, f));
                break;
            case JavaTypes.DATE:
                attrs.add(new Members.SingularAttributeImpl<X, Date>(this, f));
                break;
            case JavaTypes.CALENDAR:
                attrs.add(new Members.SingularAttributeImpl<X, Calendar>(this, f));
                break;
            case JavaTypes.BIGDECIMAL:
                attrs.add(new Members.SingularAttributeImpl<X, BigDecimal>(this, f));
                break;
            case JavaTypes.BIGINTEGER:
                attrs.add(new Members.SingularAttributeImpl<X, BigInteger>(this, f));
                break;
            case JavaTypes.LOCALE:
                attrs.add(new Members.SingularAttributeImpl<X, Locale>(this, f));
                break;
            case JavaTypes.OBJECT:
            case JavaTypes.OID:
            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
            case JavaTypes.INPUT_READER:
            case JavaTypes.INPUT_STREAM:
                attrs.add(new Members.SingularAttributeImpl(this, f));
                break;
            case JavaTypes.ARRAY:
                Compatibility compat = meta.getRepository().getConfiguration().getCompatibilityInstance();
                if(compat.getUseListAttributeForArrays() || f.isPersistentCollection()) {
                    attrs.add(new Members.ListAttributeImpl(this, f));
                }
                else { 
                    attrs.add(new Members.SingularAttributeImpl(this, f));
                }
                break;
            case JavaTypes.COLLECTION:
                switch (MetamodelImpl.categorizeCollection(f.getDeclaredType())) {
                case COLLECTION:
                    attrs.add(new Members.CollectionAttributeImpl(this, f));
                    break;
                case LIST:
                    attrs.add(new Members.ListAttributeImpl(this, f));
                    break;
                case SET:
                    attrs.add(new Members.SetAttributeImpl(this, f));
                    break;
                }
                break;
            case JavaTypes.MAP:
                attrs.add(new Members.MapAttributeImpl(this, f));
                break;
            case JavaTypes.ENUM:
                attrs.add(new Members.SingularAttributeImpl(this, f));
                break;
            default:
                throw new IllegalStateException(_loc.get("field-unrecognized",
                        f.getFullName(false), decCode).getMessage());
            }
        }
        declaredAttributeFilter = new DeclaredAttributeFilter<X>(this);
        singularAttributeFilter = new SingularAttributeFilter<X>();
        pluralAttributeFilter = new SingularAttributeFilter<X>().inverse();
    }

    /**
     * Returns all the attributes of the managed type including attributes of the super type.
     * 
     */
    public java.util.Set<Attribute<? super X, ?>> getAttributes() {
        return Collections.unmodifiableSet(attrs);
    }

    /**
     * Returns all the attributes declared by this managed type only.
     * 
     */
    public java.util.Set<Attribute<X, ?>> getDeclaredAttributes() {
        return filter(attrs, new TreeSet<Attribute<X, ?>>(),
                declaredAttributeFilter);
    }

    /**
     * Returns the single-valued attributes of the managed type.
     * 
     */
    public java.util.Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
        return filter(attrs, new TreeSet<SingularAttribute<? super X, ?>>(),
                singularAttributeFilter);
    }

    /**
     * Returns the single-valued attributes declared by the managed type.
     * 
     */
    public java.util.Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
        return filter(attrs, new TreeSet<SingularAttribute<X, ?>>(),
                declaredAttributeFilter, 
                singularAttributeFilter);
    }

    /**
     * Returns the attribute of the given name and Java type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <Y> Attribute<? super X, Y> getAttribute(String name, Class<Y> type) {
        Attribute<? super X, ?> result = pick(attrs,
                new AttributeNameFilter<X>(name),
                new AttributeTypeFilter<X, Y>(type));
        if (result == null)
            notFoundException("attr-not-found", name, type);
        
        return (Attribute<? super X, Y>)result;
    }
    
    /**
     * Returns the single-valued attribute of the given name and Java type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
        Attribute<? super X, ?> result = pick(attrs,
                new AttributeNameFilter<X>(name),
                new AttributeTypeFilter<X, Y>(type), 
                singularAttributeFilter);
        if (result == null)
            notFoundException("attr-not-found-single", name, type);
         
        return (SingularAttribute<? super X, Y>) result;
    }
    
    /**
     * Returns the declared attribute of the given name and Java type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <Y> Attribute<X, Y> getDeclaredAttribute(String name, Class<Y> type) {
        Attribute<? super X, ?> result = pick(attrs,
                new AttributeNameFilter<X>(name),
                new AttributeTypeFilter<X, Y>(type), 
                declaredAttributeFilter);
        if (result == null)
            notFoundException("attr-not-found-decl-single",name, type);
            
        return (Attribute<X, Y>) result;
    }

    /**
     * Returns the declared single-valued attribute of the given name and Java type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
        Attribute<? super X, ?> result = pick(attrs,
                new AttributeNameFilter<X>(name),
                new AttributeTypeFilter<X, Y>(type), 
                declaredAttributeFilter,
                singularAttributeFilter);
        if (result == null)
            notFoundException("attr-not-found-decl-single",name, type);

        return (SingularAttribute<X, Y>) result;
    }

    /**
     * Returns all collection-valued attributes of the managed type.
     * 
     */
    public java.util.Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
        return filter(attrs, new HashSet<PluralAttribute<? super X, ?, ?>>(),
                pluralAttributeFilter);
    }

    /**
     * Return all collection-valued attributes declared by the managed type.
     * 
     */
    public java.util.Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
        return filter(attrs, new HashSet<PluralAttribute<X, ?, ?>>(),
                declaredAttributeFilter, 
                pluralAttributeFilter);
    }

    /**
     * Returns the attribute of the given name, of type java.util.Collection and contains the
     * given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs,
                new PluralCategoryFilter<X>(CollectionType.COLLECTION),
                new ElementTypeFilter<X, E>(elementType),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-coll", name, elementType);

        return (CollectionAttribute<? super X, E>) result;
    }

    /**
     * Returns the attribute of the given name, of type java.util.Set and contains the
     * given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs,
                new PluralCategoryFilter<X>(CollectionType.SET),
                new ElementTypeFilter<X, E>(elementType),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-set",name, elementType);

        return (SetAttribute<? super X, E>) result;
    }

    /**
     * Returns the attribute of the given name, of type java.util.List and contains the
     * given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs,
                new PluralCategoryFilter<X>(CollectionType.LIST),
                new ElementTypeFilter<X, E>(elementType),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-list",name, elementType);

        return (ListAttribute<? super X, E>) result;
    }

    /**
     * Returns the attribute of the given name, of type java.util.Map and contains the
     * given key/value type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, 
        Class<V> valueType) {
        Attribute<? super X, ?> result = pick(attrs,
                new AttributeNameFilter<X>(name),
                new PluralCategoryFilter<X>(CollectionType.MAP),
                new EntryTypeFilter<X, K, V>(keyType, valueType));
        if (result == null)
            notFoundException("attr-not-found-map", name, keyType, valueType);

        return (MapAttribute<? super X, K, V>) result;
    }

    /**
     * Returns the declared attribute of the given name, of type java.util.Collection and contains 
     * the given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> CollectionAttribute<X, E> getDeclaredCollection(String name,  Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs, 
                declaredAttributeFilter,
                new PluralCategoryFilter<X>(CollectionType.COLLECTION),
                new ElementTypeFilter<X, E>(elementType),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-decl-coll", name, elementType);

        return (CollectionAttribute<X, E>) result;
    }

    /**
     * Returns the declared attribute of the given name, of type java.util.Set and contains 
     * the given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs, 
                declaredAttributeFilter,
                new PluralCategoryFilter<X>(CollectionType.SET),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-decl-set", name, elementType);

        return (SetAttribute<X, E>) result;
    }

    /**
     * Returns the declared attribute of the given name, of type java.util.List and contains 
     * the given element type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
        Attribute<? super X, ?> result = pick(attrs, 
                declaredAttributeFilter,
                new PluralCategoryFilter<X>(CollectionType.LIST),
                new ElementTypeFilter<X, E>(elementType),
                new AttributeNameFilter<X>(name));
        if (result == null)
            notFoundException("attr-not-found-decl-list", name, elementType);

        return (ListAttribute<X, E>) result;
    }

    /**
     * Returns the declared attribute of the given name, of type java.util.Map and contains 
     * the given key/value type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, 
        Class<V> valueType) {
        Attribute<? super X, ?> result = pick(attrs,
                declaredAttributeFilter,
                new AttributeNameFilter<X>(name),
                new PluralCategoryFilter<X>(CollectionType.MAP),
                new EntryTypeFilter<X, K, V>(keyType, valueType));
        if (result == null)
            notFoundException("attr-not-found-decl-map", name, keyType, valueType);

        return (MapAttribute<X, K, V>) result;
    }

    // ==============================================================================
    // No type checking
    // ==============================================================================
    
    /**
     * Returns the attribute of the given name of any type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public Attribute<? super X, ?> getAttribute(String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns the declared attribute of the given name of any type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public Attribute<X, ?> getDeclaredAttribute(String name) {
        return getDeclaredAttribute(name, null);
    }

    /**
     * Returns the single-valued attribute of the given name of any type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
        return getSingularAttribute(name, null);
    }

    /**
     * Returns the declared, single-valued attribute of the given name of any type.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
        return getDeclaredSingularAttribute(name, null);
    }

    /**
     * Returns the attribute of the given name and of type java.util.Collection.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public CollectionAttribute<? super X, ?> getCollection(String name) {
        return getCollection(name, null);
    }

    /**
     * Returns the attribute of the given name and of type java.util.Set.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public SetAttribute<? super X, ?> getSet(String name) {
        return getSet(name, null);
    }

    /**
     * Returns the attribute of the given name and of type java.util.List.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public ListAttribute<? super X, ?> getList(String name) {
        return getList(name, null);
    }

    /**
     * Returns the attribute of the given name and of type java.util.Map.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public MapAttribute<? super X, ?, ?> getMap(String name) {
        return getMap(name, null, null);
    }

    /**
     * Returns the declared attribute of the given name and of type java.util.Collection.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
        return getDeclaredCollection(name, null);
    }

    /**
     * Returns the declared attribute of the given name and of type java.util.Set.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public SetAttribute<X, ?> getDeclaredSet(String name) {
        return getDeclaredSet(name, null);
    }

    /**
     * Returns the declared attribute of the given name and of type java.util.List.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public ListAttribute<X, ?> getDeclaredList(String name) {
        return getDeclaredList(name, null);
    }

    /**
     * Returns the declared attribute of the given name and of type java.util.Map.
     * 
     * @throws IllegalArgumentException  if no such attribute exists
     */
    public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
        return getDeclaredMap(name, null, null);
    }

    //--------------------------------------------------------------------------
    // Primary Key and Version Attribute related functions
    //--------------------------------------------------------------------------
    /**
     *   Returns the attributes corresponding to the id class of the identifiable type.
     *   
     *   @throws IllegalArgumentException if the this type is not using an id class
     */
    public final java.util.Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
        if (meta.isOpenJPAIdentity())
            throw new IllegalArgumentException(meta + " does not use IdClass. Object Id type = " + 
                meta.getObjectIdType() + " Identity Type = " + meta.getIdentityType());
        return filter(attrs, new HashSet<SingularAttribute<? super X, ?>>(),
                new IdAttributeFilter<X>());
    }
    
    /**
     *  Returns the attribute of given type that corresponds to the id attribute of this 
     *  identifiable managed type.
     *  
     *  @throws IllegalArgumentException if no such attribute exists
     */
     public final <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
         Attribute<? super X, ?> result =  pick(attrs, 
                 new AttributeTypeFilter<X, Y>(type), 
                 new IdAttributeFilter<X>());
         if (result != null)
             return (SingularAttribute<? super X, Y>) result;
         throw new IllegalArgumentException();
     }
     
     /**
      *  Returns the declared attribute of given type that corresponds to the id attribute of this 
      *  identifiable managed type.
      *  
      *  @throws IllegalArgumentException if no such attribute exists
      */
     public final <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
         Attribute<? super X, ?> result =  pick(attrs, 
                 declaredAttributeFilter,
                 new AttributeTypeFilter<X, Y>(type), 
                 new IdAttributeFilter<X>());
         if (result != null)
             return (SingularAttribute<X, Y>) result;
         throw new IllegalArgumentException();
     }
     
     /**
      *  Returns the attribute of given type that corresponds to the version attribute of this 
      *  managed type.
      *  
      *  @throws IllegalArgumentException if no such attribute exists
      */
     public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
         Attribute<? super X, ?> result = pick(attrs, 
                 new VersionAttributeFilter<X>(), 
                 new AttributeTypeFilter<X,Y>(type));
         if (result == null) 
             notFoundException("version-not-found", "", type);
         return (SingularAttribute<? super X, Y>)result;
     }

     /**
      *  Returns the declared attribute of given type that corresponds to the version attribute of 
      *  this managed type.
      *  
      *  @throws IllegalArgumentException if no such attribute exists
      */
     public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
         Attribute<? super X, ?> result = pick(attrs,
                 declaredAttributeFilter,
                 new VersionAttributeFilter<X>(), 
                 new AttributeTypeFilter<X,Y>(type));
         if (result == null) 
             notFoundException("decl-version-not-found", "", type);
         return (SingularAttribute<X, Y>)result;
     }

    // =====================================================================
    // Support functions
    // =====================================================================

    FieldMetaData getField(String name) {
        return getField(name, null, null, null, false);
    }

    FieldMetaData getField(String name, Class type) {
        return getField(name, type, null, null, false);
    }

    FieldMetaData getField(String name, Class type, boolean declaredOnly) {
        return getField(name, type, null, null, declaredOnly);
    }

    /**
     * Get the field of the given name after validating the conditions. null
     * value on any condition implies not to validate.
     * 
     * @param name simple name i.e. without the class name
     * @param type the expected type of the field.
     * @param element
     *            the expected element type of the field.
     * @param key
     *            the expected key type of the field.
     * @param declared
     *            is this field declared in this receiver
     * 
     * @exception IllegalArgumentException
     *                if any of the validation fails.
     * 
     */
    FieldMetaData getField(String name, Class<?> type, Class<?> elementType,
            Class<?> keyType, boolean decl) {
        FieldMetaData fmd = decl ? meta.getDeclaredField(name) : meta
                .getField(name);

        if (fmd == null) {
            if (decl && meta.getField(name) != null) {
                throw new IllegalArgumentException(_loc.get("field-not-decl",
                        name, cls, meta.getField(name).getDeclaringType())
                        .getMessage());
            } else {
                throw new IllegalArgumentException(_loc.get("field-missing",
                        name, meta.getDescribedType(),
                        Arrays.toString(meta.getFieldNames())).getMessage());
            }
        }
        assertType("field-type-mismatch", fmd, fmd.getDeclaredType(), type);
        assertType("field-element-type-mismatch", fmd, fmd.getElement()
                .getDeclaredType(), elementType);
        assertType("field-key-type-mismatch", fmd, fmd.getKey()
                .getDeclaredType(), keyType);
        return fmd;
    }

    void assertType(String msg, FieldMetaData fmd, Class<?> actual,
            Class<?> expected) {
        if (expected != null && !expected.isAssignableFrom(actual)) {
            if (wrap(expected) != wrap(actual)) {
                throw new IllegalArgumentException(_loc.get(msg, fmd.getName(),
                        actual, expected).getMessage());
            }
        }
    }

    Class<?> wrap(Class<?> c) {
        if (c.isPrimitive()) {
            if (c == int.class)
                return Integer.class;
            if (c == long.class)
                return Long.class;
            if (c == boolean.class)
                return Boolean.class;
            if (c == byte.class)
                return Byte.class;
            if (c == char.class)
                return Character.class;
            if (c == double.class)
                return Double.class;
            if (c == float.class)
                return Float.class;
            if (c == short.class)
                return Short.class;
        }
        return c;
    }
    
    // -------------------------------------------------------------------------
    // Exception handling
    // -------------------------------------------------------------------------
    private void notFoundException(String msg, String name, Class<?> t1) {
        throw new IllegalArgumentException(
            _loc.get(msg, name, (t1 == null ? "any" : t1.getName()), meta).getMessage());
    }
    
    private void notFoundException(String msg, String name, Class<?> t1, Class<?> t2) {
        throw new IllegalArgumentException(
            _loc.get(msg, new Object[]{name, (t1 == null ? "any" : t1.getName()), 
                    (t2 == null ? "any" : t1.getName()), meta}).getMessage());
    }
    // --------------------------------------------------------------------------
    // Attribute filtering
    // --------------------------------------------------------------------------
    /**
     * Affirms if a given element satisfy a condition.
     * 
     */
    public static interface Filter<T> {
        boolean selects(T attr);

        Filter<T> inverse();
    }
    

    /**
     * Applies chain of filters ANDed on the given collection to populate the given result.
     * A null filter evaluates always TRUE.
     * The arguments are not passed as variable argument list to suppress warnings in in the caller
     * for generic varargs array construction.
     */
    
    public static <T, C extends java.util.Collection<E>, E> C filter(Collection<T> original, 
        C result, Filter<T> f1, Filter<T> f2, Filter<T> f3, Filter<T> f4) {
        for (T t : original) {
            if ((f1 == null || f1.selects(t)) && (f2 == null || f2.selects(t)) 
             && (f3 == null || f3.selects(t)) && (f4 == null || f4.selects(t)))
                result.add((E) t);
        }
        return result;
    }
    
    /**
     * Applies chain of filters ANDed on the given collection to pick a single element.
     * A null filter evaluates always TRUE.
     * The arguments are not passed as variable argument list to suppress warnings in in the caller
     * for generic varargs array construction.
     */
    public static <T> T pick(Collection<T> original, Filter<T> f1, Filter<T> f2, Filter<T> f3, 
        Filter<T> f4) {
        for (T t : original) {
            if ((f1 == null || f1.selects(t)) && (f2 == null || f2.selects(t)) 
             && (f3 == null || f3.selects(t)) && (f4 == null || f4.selects(t)))
                return t;
        }
        return null;
    }
    
    static <T, C extends java.util.Collection<E>, E> C filter(Collection<T> original, 
            C result, Filter<T> f1) {
        return filter(original, result, f1, null, null, null);
    }
    
    static <T, C extends java.util.Collection<E>, E> C filter(Collection<T> original, 
            C result, Filter<T> f1, Filter<T> f2) {
        return filter(original, result, f1, f2, null, null);
    }
    
    static <T, C extends java.util.Collection<E>, E> C filter(Collection<T> original, 
            C result, Filter<T> f1, Filter<T> f2, Filter<T> f3) {
        return filter(original, result, f1, f2, f3, null);
    }
    
    static <T> T pick(Collection<T> original, Filter<T> f1) {
        return pick(original, f1, null, null, null);
    }

    static <T> T pick(Collection<T> original, Filter<T> f1, Filter<T> f2) {
        return pick(original, f1, f2, null, null);
    }
    
    static <T> T pick(Collection<T> original, Filter<T> f1, Filter<T> f2, Filter<T> f3) {
        return pick(original, f1, f2, f3, null);
    }
    

    /**
     * Affirms if the given attribute is a Singular attribute.
     * 
     */
    public static final class SingularAttributeFilter<X> implements
            Filter<Attribute<? super X, ?>> {
        private final boolean _invert;

        public SingularAttributeFilter() {
            this(false);
        }

        public SingularAttributeFilter(boolean inverted) {
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            return _invert ? attr.isCollection() : !attr.isCollection();
        }

        public SingularAttributeFilter<X> inverse() {
            return new SingularAttributeFilter<X>(!_invert);
        }
    }

    public static final class DeclaredAttributeFilter<X> implements
            Filter<Attribute<? super X, ?>> {
        private final ManagedType<X> owner;
        private final boolean _invert;

        DeclaredAttributeFilter(ManagedType<X> owner) {
            this(owner, false);
        }

        DeclaredAttributeFilter(ManagedType<X> owner, boolean inverted) {
            this.owner = owner;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            return _invert ? attr.getDeclaringType() != owner : attr
                    .getDeclaringType() == owner;
        }

        public DeclaredAttributeFilter<X> inverse() {
            return new DeclaredAttributeFilter<X>(owner, !_invert);
        }
    }

    /**
     * Selects if the attribute type matches the given Java class.
     * null matches any type.
     */
    public static final class AttributeTypeFilter<X, Y> implements Filter<Attribute<? super X, ?>> {
        private final Class<Y> _type;
        private final boolean _invert;

        public AttributeTypeFilter(Class<Y> type) {
            this(type, false);
        }

        public AttributeTypeFilter(Class<Y> type, boolean inverted) {
            _type = type;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            boolean result = _type == null || Filters.canConvert(attr.getJavaType(), _type, false);
            return _invert ? !result : result;
        }

        public AttributeTypeFilter<X, Y> inverse() {
            return new AttributeTypeFilter<X, Y>(_type, !_invert);
        }        
    }

    public static final class AttributeNameFilter<X> implements
            Filter<Attribute<? super X, ?>> {
        private final String _name;
        private final boolean _invert;

        public AttributeNameFilter(String name) {
            this(name, false);
        }

        public AttributeNameFilter(String name, boolean inverted) {
            _name = name;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            return _invert ? !attr.getName().equals(_name) : attr.getName()
                    .equals(_name);
        }

        public AttributeNameFilter<X> inverse() {
            return new AttributeNameFilter<X>(_name, !_invert);
        }
    }

    public static final class PluralCategoryFilter<X> implements
            Filter<Attribute<? super X, ?>> {
        private final CollectionType _category;
        private final boolean _invert;

        public PluralCategoryFilter(CollectionType category) {
            this(category, false);
        }

        public PluralCategoryFilter(CollectionType category, boolean inverted) {
            _category = category;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            boolean result = (attr instanceof PluralAttribute<?, ?, ?>)
                    && ((PluralAttribute<?, ?, ?>) attr).getCollectionType() == _category;
            return _invert ? !result : result;
        }

        public PluralCategoryFilter<X> inverse() {
            return new PluralCategoryFilter<X>(_category, !_invert);
        }
    }

    /**
     * Selects plural attribute of given element type.
     * null element type imply <em>any</em> element type.
     */
    
    public static final class ElementTypeFilter<X, E> implements Filter<Attribute<? super X, ?>> {
        private final Class<E> _elementType;
        private final boolean _invert;

        public ElementTypeFilter(Class<E> eType) {
            this(eType, false);
        }

        public ElementTypeFilter(Class<E> eType, boolean inverted) {
            _elementType = eType;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            boolean result = (attr instanceof PluralAttribute<?, ?, ?>)
                    && (_elementType == null 
                    || ((PluralAttribute<?, ?, ?>) attr).getElementType().getJavaType() 
                         == _elementType);
            return _invert ? !result : result;
        }

        public ElementTypeFilter<X, E> inverse() {
            return new ElementTypeFilter<X, E>(_elementType, !_invert);
        }
    }

    public static final class EntryTypeFilter<X, K, V> implements
            Filter<Attribute<? super X, ?>> {
        private final Class<K> _keyType;
        private final Class<V> _valueType;
        private final boolean _invert;

        public EntryTypeFilter(Class<K> kType, Class<V> vType) {
            this(kType, vType, false);
        }

        public EntryTypeFilter(Class<K> kType, Class<V> vType, boolean inverted) {
            _keyType = kType;
            _valueType = vType;
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            boolean result = (attr instanceof MapAttribute<?, ?, ?>)
                    && (_keyType == null 
                    || ((MapAttribute<?, ?, ?>) attr).getKeyType().getJavaType() == _keyType)
                    && (_valueType == null 
                    || ((MapAttribute<?, ?, ?>) attr).getElementType().getJavaType() == _valueType);
            return _invert ? !result : result;
        }

        public EntryTypeFilter<X, K, V> inverse() {
            return new EntryTypeFilter<X, K, V>(_keyType, _valueType, !_invert);
        }
    }

    public static final class IdAttributeFilter<X> implements
            Filter<Attribute<? super X, ?>> {
        private final boolean _invert;

        public IdAttributeFilter() {
            this(false);
        }

        public IdAttributeFilter(boolean inverted) {
            _invert = inverted;
        }

        public boolean selects(Attribute<? super X, ?> attr) {
            boolean result = ((Members.Member<?, ?>) attr).fmd.isPrimaryKey();
            return _invert ? !result : result;
        }

        public IdAttributeFilter<X> inverse() {
            return new IdAttributeFilter<X>(!_invert);
        }
    }
    
    public static final class VersionAttributeFilter<X> implements Filter<Attribute<? super X, ?>> {
        private final boolean _invert;

        public VersionAttributeFilter() {
            this(false);
        }
        
        public VersionAttributeFilter(boolean inverted) {
            _invert = inverted;
        }
        
        public boolean selects(Attribute<? super X, ?> attr) {
            FieldMetaData fmd = ((Members.Member<?, ?>) attr).fmd;
            boolean result = fmd.isVersion();
            return _invert ? !result : result;
        }
        
        public IdAttributeFilter<X> inverse() {
            return new IdAttributeFilter<X>(!_invert);
        }
    }

}
