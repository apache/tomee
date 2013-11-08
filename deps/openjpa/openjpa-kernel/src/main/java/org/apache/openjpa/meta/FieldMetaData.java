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
package org.apache.openjpa.meta;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.JavaVersions;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.xml.Commentable;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.ProxyManager;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.UserException;

import serp.util.Strings;

/**
 * Metadata for a managed class field.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class FieldMetaData
    extends Extensions
    implements ValueMetaData, MetaDataContext, MetaDataModes, Commentable {
    
    /**
     * Constant specifying that no null-value was given.
     */
    public static final int NULL_UNSET = -1;

    /**
     * Constant specifying to use a datastore null to persist null values
     * in object fields.
     */
    public static final int NULL_NONE = 0;

    /**
     * Constant specifying to use a datastore default value to persist null
     * values in object fields.
     */
    public static final int NULL_DEFAULT = 1;

    /**
     * Constant specifying to throw an exception when attempting to persist
     * null values in object fields.
     */
    public static final int NULL_EXCEPTION = 2;

    /**
     * Constant specifying the management level of a field.
     */
    public static final int MANAGE_PERSISTENT = 3;

    /**
     * Constant specifying the management level of a field.
     */
    public static final int MANAGE_TRANSACTIONAL = 1;

    /**
     * Constant specifying the management level of a field.
     */
    public static final int MANAGE_NONE = 0;
    
    public static final int ONE_TO_ONE = 1;
    public static final int ONE_TO_MANY = 2;
    public static final int MANY_TO_ONE = 3;
    public static final int MANY_TO_MANY = 4;

    private static final Localizer _loc = Localizer.forPackage
        (FieldMetaData.class);

    private static final int DFG_FALSE = 1;
    private static final int DFG_TRUE = 2;
    private static final int DFG_EXPLICIT = 4;

    private static final Method DEFAULT_METHOD;
    static {
        try {
            DEFAULT_METHOD = Object.class.getMethod("wait", (Class[]) null);
        } catch (Exception e) {
            // shouldn't ever happen
            throw new InternalException(e);
        }
    }

    // name and type
    private final ValueMetaData _val;
    private final ValueMetaData _key;
    private final ValueMetaData _elem;
    private final ClassMetaData _owner;
    private final String _name;
    private Class<?> _dec = null;
    private ClassMetaData _decMeta = null;
    private String _fullName = null;
    private String _embedFullName = null;
    private int _resMode = MODE_NONE;
    private String _mappedByIdValue = null;
    private int _access = AccessCode.UNKNOWN;

    // load/store info
    private String[] _comments = null;
    private int _listIndex = -1;

    ////////////////////////////////////////////////////////////////////
    // Note: if you add additional state, make sure to add it to copy()
    ////////////////////////////////////////////////////////////////////

    // misc info
    private Class<?> _proxyClass = null;
    private Object _initializer = null;
    private boolean _transient = false;
    private boolean _primKey = false;
    private Boolean _version = null;
    private int _nullValue = NULL_UNSET;
    private int _manage = MANAGE_PERSISTENT;
    private int _index = -1;
    private int _decIndex = -1;
    private int _pkIndex = -1;
    private boolean _explicit = false;
    private int _dfg = 0;
    private Set<String> _fgSet = null;
    private String[] _fgs = null;
    private String   _lfg = null;
    private Boolean _lrs = null;
    private Boolean _stream = null;
    private String _extName = null;
    private String _factName = null;
    private String _extString = null;
    private Map _extValues = Collections.EMPTY_MAP;
    private Map _fieldValues = Collections.EMPTY_MAP;
    private Boolean _enumField = null;
    private Boolean _lobField = null;
    private Boolean _serializableField = null;
    private boolean _generated = false;
    private boolean _useSchemaElement = true;

    // Members aren't serializable. Use a proxy that can provide a Member
    // to avoid writing the full Externalizable implementation.
    private MemberProvider _backingMember = null;

    // Members aren't serializable. Initializing _extMethod and _factMethod to
    // DEFAULT_METHOD is sufficient to trigger lazy population of these fields.
    private transient Method _extMethod = DEFAULT_METHOD;
    private transient Member _factMethod = DEFAULT_METHOD;

    // intermediate and impl data
    private boolean _intermediate = true;
    private Boolean _implData = Boolean.TRUE;

    // value generation
    private int _valStrategy = -1;
    private int _upStrategy = -1;
    private String _seqName = ClassMetaData.DEFAULT_STRING;
    private SequenceMetaData _seqMeta = null;

    // inverses
    private String _mappedBy = null;
    private FieldMetaData _mappedByMeta = null;
    private FieldMetaData[] _inverses = null;
    private String _inverse = ClassMetaData.DEFAULT_STRING;

    // ordering on load
    private Order[] _orders = null;
    private String _orderDec = null;
    // indicate if this field is used by other field as "order by" value 
    private boolean _usedInOrderBy = false;
    private boolean _isElementCollection = false;
    private int _associationType;

    private boolean _persistentCollection = false; 

    private Boolean _delayCapable = null;
    /**
     * Constructor.
     *
     * @param name the field name
     * @param type the field type
     * @param owner the owning class metadata
     */
    protected FieldMetaData(String name, Class<?> type, ClassMetaData owner) {
        _name = name;
        _owner = owner;
        _dec = null;
        _decMeta = null;
        _val = owner.getRepository().newValueMetaData(this);
        _key = owner.getRepository().newValueMetaData(this);
        _elem = owner.getRepository().newValueMetaData(this);

        setDeclaredType(type);
    }

    /**
     * Supply the backing member object; this allows us to utilize
     * parameterized type information if available.
     * Sets the access style of this receiver based on whether the given 
     * member represents a field or getter method.
     */
    public void backingMember(Member member) {
        if (member == null)
            return;
        if (Modifier.isTransient(member.getModifiers()))
            _transient = true;

        _backingMember = new MemberProvider(member);

        Class<?> type;
        Class<?>[] types;
        if (member instanceof Field) {
            Field f = (Field) member;
            type = f.getType();
            types = JavaVersions.getParameterizedTypes(f);
            setAccessType(AccessCode.FIELD);
        } else {
            Method meth = (Method) member;
            type = meth.getReturnType();
            types = JavaVersions.getParameterizedTypes(meth);
            setAccessType(AccessCode.PROPERTY);
        }

        setDeclaredType(type);
        if (Collection.class.isAssignableFrom(type)
            && _elem.getDeclaredType() == Object.class
            && types.length == 1) {
            _elem.setDeclaredType(types[0]);
        } else if (Map.class.isAssignableFrom(type)
            && types.length == 2) {
            if (_key.getDeclaredType() == Object.class)
                _key.setDeclaredType(types[0]);
            if (_elem.getDeclaredType() == Object.class)
                _elem.setDeclaredType(types[1]);
        }
    }

    /**
     * Return the backing member supplied in {@link #backingMember}.
     */
    public Member getBackingMember() {
        return (_backingMember == null) ? null : _backingMember.getMember();
    }

    /**
     * The metadata repository.
     */
    public MetaDataRepository getRepository() {
        return _owner.getRepository();
    }

    /**
     * The class that defines the metadata for this field.
     */
    public ClassMetaData getDefiningMetaData() {
        return _owner;
    }

    /**
     * The declaring class.
     */
    public Class<?> getDeclaringType() {
        return (_dec == null) ? _owner.getDescribedType() : _dec;
    }

    /**
     * The declaring class.
     */
    public void setDeclaringType(Class<?> cls) {
        _dec = cls;
        _decMeta = null;
        _fullName = null;
        _embedFullName = null;
    }

    /**
     * The declaring class.
     */
    public ClassMetaData getDeclaringMetaData() {
        if (_dec == null)
            return _owner;
        if (_decMeta == null)
            _decMeta = getRepository().getMetaData(_dec,
                _owner.getEnvClassLoader(), true);
        return _decMeta;
    }

    /**
     * The field name.
     */
    public String getName() {
        return _name;
    }

    /**
     * The field name, qualified by the owning class.
     * @deprecated Use getFullName(boolean) instead.
     */
    public String getFullName() {
        return getFullName(false);
    }

    /**
     * The field name, qualified by the owning class and optionally the
     * embedding owner's name (if any).
     */
    public String getFullName(boolean embedOwner) {
        if (_fullName == null)
            _fullName = getDeclaringType().getName() + "." + _name;
        if (embedOwner && _embedFullName == null) {
            if (_owner.getEmbeddingMetaData() == null)
                _embedFullName = _fullName;
            else
                _embedFullName = _owner.getEmbeddingMetaData().
                    getFieldMetaData().getFullName(true) + "." + _fullName;
        }
        return (embedOwner) ? _embedFullName : _fullName;
    }

    /**
     * The field name, qualified by the defining class.
     */
    public String getRealName() {
    	// Added to support OPENJPA-704
        return getDefiningMetaData().getDescribedType().getName() + "." + _name;
    }

    /**
     * MetaData about the field value.
     */
    public ValueMetaData getValue() {
        return _val;
    }

    /**
     * Metadata about the key value.
     */
    public ValueMetaData getKey() {
        return _key;
    }

    /**
     * Metadata about the element value.
     */
    public ValueMetaData getElement() {
        return _elem;
    }

    /**
     * Return whether this field is mapped to the datastore. By default,
     * returns true for all persistent fields whose defining class is mapped.
     */
    public boolean isMapped() {
        return _manage == MANAGE_PERSISTENT && _owner.isMapped();
    }

    /**
     * The type this field was initialized with, and therefore the
     * type to use for proxies when loading data into this field.
     */
    public Class<?> getProxyType() {
        return (_proxyClass == null) ? getDeclaredType() : _proxyClass;
    }

    /**
     * The type this field was initialized with, and therefore the
     * type to use for proxies when loading data into this field.
     */
    public void setProxyType(Class<?> type) {
        _proxyClass = type;
    }

    /**
     * The initializer used by the field, or null if none. This
     * is additional information for initializing the field, such as
     * a custom {@link Comparator} used by a {@link Set} or
     * a {@link TimeZone} used by a {@link Calendar}.
     */
    public Object getInitializer() {
        return _initializer;
    }

    /**
     * The initializer used by the field, or null if none. This
     * is additional information for initializing the field, such as
     * a custom {@link Comparator} used by a {@link Set} or
     * a {@link TimeZone} used by a {@link Calendar}.
     */
    public void setInitializer(Object initializer) {
        _initializer = initializer;
    }

    /**
     * Return whether this is a transient field.
     */
    public boolean isTransient() {
        return _transient;
    }

    /**
     * Return whether this is a transient field.
     */
    public void setTransient(boolean trans) {
        _transient = trans;
    }

    /**
     * The absolute index of this persistent/transactional field.
     */
    public int getIndex() {
        return _index;
    }

    /**
     * The absolute index of this persistent/transactional field.
     */
    public void setIndex(int index) {
        _index = index;
    }

    /**
     * The relative index of this persistent/transactional field.
     */
    public int getDeclaredIndex() {
        return _decIndex;
    }

    /**
     * The relative index of this persistent/transactional field.
     */
    public void setDeclaredIndex(int index) {
        _decIndex = index;
    }

    /**
     * The index in which this field was listed in the metadata. Defaults to
     * <code>-1</code> if this field was not listed in the metadata.
     */
    public int getListingIndex() {
        return _listIndex;
    }

    /**
     * The index in which this field was listed in the metadata. Defaults to
     * <code>-1</code> if this field was not listed in the metadata.
     */
    public void setListingIndex(int index) {
        _listIndex = index;
    }

    /**
     * The absolute primary key index for this field, or -1 if not a primary
     * key. The first primary key field has index 0, the second index 1, etc.
     */
    public int getPrimaryKeyIndex() {
        return _pkIndex;
    }

    /**
     * The absolute primary key index for this field, or -1 if not a primary
     * key. The first primary key field has index 0, the second index 1, etc.
     */
    public void setPrimaryKeyIndex(int index) {
        _pkIndex = index;
    }

    /**
     * Return the management level for the field. Will be one of:
     * <ul>
     * <li>{@link #MANAGE_PERSISTENT}: the field is persistent</li>
     * <li>{@link #MANAGE_TRANSACTIONAL}: the field is transactional but not
     * persistent</li>
     * <li>{@link #MANAGE_NONE}: the field is not managed</li>
     * </ul> Defaults to {@link #MANAGE_PERSISTENT}.
     */
    public int getManagement() {
        return _manage;
    }

    /**
     * Return the management level for the field. Will be one of:
     * <ul>
     * <li>{@link #MANAGE_PERSISTENT}: the field is persistent</li>
     * <li>{@link #MANAGE_TRANSACTIONAL}: the field is transactional but not
     * persistent</li>
     * <li>{@link #MANAGE_NONE}: the field is not managed</li>
     * </ul> 
     * Defaults to {@link #MANAGE_PERSISTENT}.
     */
    public void setManagement(int manage) {
        if ((_manage == MANAGE_NONE) != (manage == MANAGE_NONE))
            _owner.clearFieldCache();
        _manage = manage;
    }

    /**
     * Whether this is a primary key field.
     */
    public boolean isPrimaryKey() {
        return _primKey;
    }

    /**
     * Whether this is a primary key field.
     */
    public void setPrimaryKey(boolean primKey) {
        _primKey = primKey;
    }

    /**
     * For a primary key field, return the type of the corresponding object id 
     * class field.
     */
    public int getObjectIdFieldTypeCode() {
        ClassMetaData relmeta = getDeclaredTypeMetaData();
        if (relmeta == null)
            return getDeclaredTypeCode();
        if (relmeta.getIdentityType() == ClassMetaData.ID_DATASTORE) {
            boolean unwrap = getRepository().getMetaDataFactory().getDefaults().
                isDataStoreObjectIdFieldUnwrapped();
            return (unwrap) ? JavaTypes.LONG : JavaTypes.OBJECT;
        }
        if (relmeta.isOpenJPAIdentity())
            return relmeta.getPrimaryKeyFields()[0].getObjectIdFieldTypeCode();
        return JavaTypes.OBJECT;
    }

    /**
     * For a primary key field, return the type of the corresponding object id 
     * class field.
     */
    public Class<?> getObjectIdFieldType() {
        ClassMetaData relmeta = getDeclaredTypeMetaData();
        if (relmeta == null || getValue().isEmbedded())
            return getDeclaredType();
        switch (relmeta.getIdentityType()) {
            case ClassMetaData.ID_DATASTORE:
                boolean unwrap = getRepository().getMetaDataFactory().
                    getDefaults().isDataStoreObjectIdFieldUnwrapped();
                return (unwrap) ? long.class : Object.class;
            case ClassMetaData.ID_APPLICATION:
                if (relmeta.isOpenJPAIdentity())
                    return relmeta.getPrimaryKeyFields()[0].
                        getObjectIdFieldType();
                return (relmeta.getObjectIdType() == null) ? Object.class
                    : relmeta.getObjectIdType();
            default:
                return Object.class;
        } 
    }

    /**
     * Whether this field holds optimistic version information.
     */
    public boolean isVersion() {
        return _version == Boolean.TRUE;
    }

    /**
     * Whether this field holds optimistic version information.
     */
    public void setVersion(boolean version) {
        _version = (version) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Whether this field is in the default fetch group.
     */
    public boolean isInDefaultFetchGroup() {
        if (_dfg == 0) {
            if (_manage != MANAGE_PERSISTENT || isPrimaryKey() || isVersion())
                _dfg = DFG_FALSE;
            else {
                // field left as default; dfg setting depends on type
                switch (getTypeCode()) {
                    case JavaTypes.OBJECT:
                        if (isSerializable() || isEnum())
                            _dfg = DFG_TRUE;
                        else
                            _dfg = DFG_FALSE;
                        break;
                    case JavaTypes.ARRAY:
                        if (isLobArray())
                            _dfg = DFG_TRUE;
                        else
                            _dfg = DFG_FALSE;
                        break;
                    case JavaTypes.COLLECTION:
                    case JavaTypes.MAP:
                    case JavaTypes.PC:
                    case JavaTypes.PC_UNTYPED:
                        _dfg = DFG_FALSE;
                        break;
                    default:
                        _dfg = DFG_TRUE;
                }
            }
        }
        return (_dfg & DFG_TRUE) > 0;
    }

    private boolean isEnum() {
        if (_enumField == null) {
            Class<?> decl = getDeclaredType();
            _enumField =  Enum.class.isAssignableFrom(decl) 
                ? Boolean.TRUE : Boolean.FALSE;
        }
        return _enumField.booleanValue();
    }

    private boolean isSerializable() {
        if (_serializableField == null) {
            Class<?> decl = getDeclaredType();
            if (Serializable.class.isAssignableFrom(decl))
                _serializableField = Boolean.TRUE;
            else
                _serializableField = Boolean.FALSE;
        }
        return _serializableField.booleanValue();
    }

    private boolean isLobArray() {
        // check for byte[], Byte[], char[], Character[]
        if (_lobField == null) {
            Class<?> decl = getDeclaredType();
            if (decl == byte[].class || decl == Byte[].class ||
                decl == char[].class || decl == Character[].class)
                _lobField = Boolean.TRUE;
            else
                _lobField = Boolean.FALSE;
        }
        return _lobField.booleanValue();
    }

    /**
     * Whether this field is in the default fetch group.
     */
    public void setInDefaultFetchGroup(boolean dfg) {
        if (dfg)
            _dfg = DFG_TRUE;
        else
            _dfg = DFG_FALSE;
        _dfg |= DFG_EXPLICIT;
    }

    /**
     * Whether the default fetch group setting is explicit.
     */
    public boolean isDefaultFetchGroupExplicit() {
        return (_dfg & DFG_EXPLICIT) > 0;
    }

    /**
     * Whether the default fetch group setting is explicit. Allow setting
     * for testing.
     */
    public void setDefaultFetchGroupExplicit(boolean explicit) {
        if (explicit)
            _dfg |= DFG_EXPLICIT;
        else
            _dfg &= ~DFG_EXPLICIT;
    }

    /**
     * Gets the name of the custom fetch groups those are associated to this 
     * receiver.  This does not include the "default" and "all" fetch groups.
     *
     * @return the set of fetch group names, not including the default and
     * all fetch groups.
     */
    public String[] getCustomFetchGroups() {
        if (_fgs == null) {
            if (_fgSet == null || _manage != MANAGE_PERSISTENT 
                || isPrimaryKey() || isVersion())
                _fgs = new String[0];
            else
                _fgs = (String[]) _fgSet.toArray(new String[_fgSet.size()]);
        }
        return _fgs;
    }

    /**
     * The fetch group that is to be loaded when this receiver is loaded, or
     * null if none set.
     */
    public String getLoadFetchGroup () {
    	return _lfg;
    }
    
    /**
     * The fetch group that is to be loaded when this receiver is loaded, or
     * null if none set.
     */
    public void setLoadFetchGroup (String lfg) {
        if ("".equals(lfg))
            lfg = null;
    	_lfg = lfg;
    }

    /**
     * Whether this field is in the given fetch group.
     */
    public boolean isInFetchGroup(String fg) {
        if (_manage != MANAGE_PERSISTENT || isPrimaryKey() || isVersion())
            return false;
        if (FetchGroup.NAME_ALL.equals(fg))
            return true;
        if (FetchGroup.NAME_DEFAULT.equals(fg))
            return isInDefaultFetchGroup();
        return _fgSet != null && _fgSet.contains(fg);
    }

    /**
     * Set whether this field is in the given fetch group.
     *
     * @param fg is the name of a fetch group that must be present in the
     * class that declared this field or any of its persistent superclasses.
     */
    public void setInFetchGroup(String fg, boolean in) {
        if (StringUtils.isEmpty(fg))
            throw new MetaDataException(_loc.get("empty-fg-name", this));
        if (fg.equals(FetchGroup.NAME_ALL))
            return;
        if (fg.equals(FetchGroup.NAME_DEFAULT)) {
            setInDefaultFetchGroup(in);
            return;
        }
        if (_owner.getFetchGroup(fg) == null)
            throw new MetaDataException(_loc.get("unknown-fg", fg, this));
        if (in && _fgSet == null)
            _fgSet = new HashSet<String>();
        if ((in && _fgSet.add(fg))
            || (!in && _fgSet != null && _fgSet.remove(fg)))
            _fgs = null;
    }
    
    /**
     * How the data store should treat null values for this field:
     * <ul>
     * <li>{@link #NULL_UNSET}: no value supplied</li>
     * <li>{@link #NULL_NONE}: leave null values as null in the data store</li>
     * <li>{@link #NULL_EXCEPTION}: throw an exception if this field is null
     * at commit</li>
     * <li>{@link #NULL_DEFAULT}: use the database default if this field is
     * null at commit</li>
     * </ul> Defaults to {@link #NULL_UNSET}.
     */
    public int getNullValue() {
        return _nullValue;
    }

    /**
     * How the data store should treat null values for this field:
     * <ul>
     * <li>{@link #NULL_UNSET}: no value supplied</li>
     * <li>{@link #NULL_NONE}: leave null values as null in the data store</li>
     * <li>{@link #NULL_EXCEPTION}: throw an exception if this field is null
     * at commit</li>
     * <li>{@link #NULL_DEFAULT}: use the database default if this field is
     * null at commit</li>
     * </ul> Defaults to {@link #NULL_UNSET}.
     */
    public void setNullValue(int nullValue) {
        _nullValue = nullValue;
    }

    /**
     * Whether this field is explicitly declared in the metadata.
     */
    public boolean isExplicit() {
        return _explicit;
    }

    /**
     * Whether this field is explicitly declared in the metadata.
     */
    public void setExplicit(boolean explicit) {
        _explicit = explicit;
    }

    /**
     * The field that this field shares a mapping with.
     */
    public String getMappedBy() {
        return _mappedBy;
    }

    /**
     * The field that this field shares a mapping with.
     */
    public void setMappedBy(String mapped) {
        _mappedBy = mapped;
        _mappedByMeta = null;
    }

    /**
     * The field that this field shares a mapping with.
     */
    public FieldMetaData getMappedByMetaData() {
        if (_mappedBy != null && _mappedByMeta == null) {
            ClassMetaData meta = null;
            switch (getTypeCode()) {
                case JavaTypes.PC:
                    meta = getTypeMetaData();
                    break;
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    meta = _elem.getTypeMetaData();
                    break;
            }

            FieldMetaData field = (meta == null) ? null
                : getMappedByField(meta, _mappedBy);
            if (field == null)
                throw new MetaDataException(_loc.get("no-mapped-by", this,
                    _mappedBy));
            if (field.getMappedBy() != null)
                throw new MetaDataException(_loc.get("circ-mapped-by", this,
                    _mappedBy));
            OpenJPAConfiguration conf = getRepository().getConfiguration();
            boolean isAbstractMappingUniDirectional = getRepository().getMetaDataFactory().
                    getDefaults().isAbstractMappingUniDirectional(conf);
            if (isAbstractMappingUniDirectional) {
                if (field.getDeclaringMetaData().isAbstract())
                    throw new MetaDataException(_loc.get("no-mapped-by-in-mapped-super", field,
                            field.getDeclaringMetaData()));

                if (this.getDeclaringMetaData().isAbstract())
                    throw new MetaDataException(_loc.get("no-mapped-by-in-mapped-super", this,
                            this.getDeclaringMetaData()));
            }
            _mappedByMeta = field;
        }
        return _mappedByMeta;
    }

    public FieldMetaData getMappedByField(ClassMetaData meta, String mappedBy) {
        FieldMetaData field = meta.getField(mappedBy);
        if (field != null)
            return field;
        int dotIdx = mappedBy.indexOf("."); 
        if ( dotIdx == -1)
            return null;
        String fieldName = mappedBy.substring(0, dotIdx);
        FieldMetaData field1 = meta.getField(fieldName);
        if (field1 == null)
            return null;
        ClassMetaData meta1 = field1.getEmbeddedMetaData();
        if (meta1 == null)
            return null;
        String mappedBy1 = mappedBy.substring(dotIdx + 1);
        return getMappedByField(meta1, mappedBy1);
    }
    
    
    /**
     * Logical inverse field.
     */
    public String getInverse() {
        if (ClassMetaData.DEFAULT_STRING.equals(_inverse))
            _inverse = null;
        return _inverse;
    }

    /**
     * Logical inverse field.
     */
    public void setInverse(String inverse) {
        _inverses = null;
        _inverse = inverse;
    }

    /**
     * Return all inverses of this field.
     */
    public FieldMetaData[] getInverseMetaDatas() {
        if (_inverses == null) {
            // can't declare both an inverse owner and a logical inverse
            String inv = getInverse();
            if (_mappedBy != null && inv != null && !_mappedBy.equals(inv))
                throw new MetaDataException(_loc.get("mapped-not-inverse",
                    this));

            // get the metadata for the type on the other side of this relation
            ClassMetaData meta = null;
            switch (getTypeCode()) {
                case JavaTypes.PC:
                    meta = getTypeMetaData();
                    break;
                case JavaTypes.ARRAY:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    meta = _elem.getTypeMetaData();
                    break;
            }

            Collection<FieldMetaData> inverses = null;
            if (meta != null) {
                // add mapped by and named inverse, if any
                FieldMetaData field = getMappedByMetaData();
                if (field != null) {
                    // mapped by field isn't necessarily a pc type, but all
                    // inverses must be
                    if (field.getTypeCode() == JavaTypes.PC
                        || field.getElement().getTypeCode() == JavaTypes.PC) {
                        inverses = new ArrayList<FieldMetaData>(3);
                        inverses.add(field);
                    }
                } else if (inv != null) {
                    field = meta.getField(inv);
                    if (field == null)
                        throw new MetaDataException(_loc.get("no-inverse",
                            this, inv));
                    inverses = new ArrayList<FieldMetaData>(3);
                    inverses.add(field);
                }

                // scan rel type for fields that name this field as an inverse
                FieldMetaData[] fields = meta.getFields();
                Class<?> type = getDeclaringMetaData().getDescribedType();
                for (int i = 0; i < fields.length; i++) {
                    // skip fields that aren't compatible with our owning class
                    switch (fields[i].getTypeCode()) {
                        case JavaTypes.PC:
                            if (!type.isAssignableFrom(fields[i].getType()))
                                continue;
                            break;
                        case JavaTypes.COLLECTION:
                        case JavaTypes.ARRAY:
                            if (!type.isAssignableFrom(fields[i].
                                getElement().getType()))
                                continue;
                            break;
                        default:
                            continue;
                    }

                    // if the field declares us as its inverse and we haven't
                    // already added it (we might have if we also declared it
                    // as our inverse), add it now
                    if (_name.equals(fields[i].getMappedBy())
                        || _name.equals(fields[i].getInverse())) {
                        if (inverses == null)
                            inverses = new ArrayList<FieldMetaData>(3);
                        if (!inverses.contains(fields[i]))
                            inverses.add(fields[i]);
                    }
                }
            }

            MetaDataRepository repos = getRepository();
            if (inverses == null)
                _inverses = repos.EMPTY_FIELDS;
            else
                _inverses = inverses.toArray
                    (repos.newFieldMetaDataArray(inverses.size()));
        }
        return _inverses;
    }

    /**
     * The strategy to use for insert value generation.
     * One of the constants from {@link ValueStrategies}.
     */
    public int getValueStrategy() {
        if (_valStrategy == -1)
            _valStrategy = ValueStrategies.NONE;
        return _valStrategy;
    }

    /**
     * The strategy to use for insert value generation.
     * One of the constants from {@link ValueStrategies}.
     */
    public void setValueStrategy(int strategy) {
        _valStrategy = strategy;
        if (strategy != ValueStrategies.SEQUENCE)
            setValueSequenceName(null);
    }

    /**
     * The value sequence name, or null for none.
     */
    public String getValueSequenceName() {
        if (ClassMetaData.DEFAULT_STRING.equals(_seqName))
            _seqName = null;
        return _seqName;
    }

    /**
     * The value sequence name, or null for none.
     */
    public void setValueSequenceName(String seqName) {
        _seqName = seqName;
        _seqMeta = null;
        if (seqName != null)
            setValueStrategy(ValueStrategies.SEQUENCE);
    }

    /**
     * Metadata for the value sequence.
     */
    public SequenceMetaData getValueSequenceMetaData() {
        if (_seqMeta == null && getValueSequenceName() != null)
            _seqMeta = getRepository().getSequenceMetaData(_owner,
                getValueSequenceName(), true);
        return _seqMeta;
    }

    /**
     * The strategy to use when updating the field.
     */
    public int getUpdateStrategy() {
        if (isVersion())
            return UpdateStrategies.RESTRICT;
        if (_upStrategy == -1)
            _upStrategy = UpdateStrategies.NONE;
        return _upStrategy;
    }

    /**
     * Set the update strategy.
     */
    public void setUpdateStrategy(int strategy) {
        _upStrategy = strategy;
    }

    /**
     * Whether this field is backed by a large result set.
     */
    public boolean isLRS() {
        return _lrs == Boolean.TRUE && _manage == MANAGE_PERSISTENT;
    }

    /**
     * Whether this field is backed by a large result set.
     */
    public void setLRS(boolean lrs) {
        _lrs = (lrs) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Whether this field is backed by a stream.
     *
     * @since 1.1.0
     */
    public boolean isStream() {
        return _stream == Boolean.TRUE && _manage == MANAGE_PERSISTENT;
    }
    
    /**
     * Whether this field is backed by a stream.
     *
     * @since 1.1.0
     */
    public void setStream(boolean stream) {
        _stream = (stream) ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Whether this field uses intermediate data when loading/storing
     * information through a {@link OpenJPAStateManager}. Defaults to true.
     *
     * @see OpenJPAStateManager#setIntermediate(int,Object)
     */
    public boolean usesIntermediate() {
        return _intermediate;
    }

    /**
     * Whether this field uses intermediate data when loading/storing
     * information through a {@link OpenJPAStateManager}. Defaults to true.
     *
     * @see OpenJPAStateManager#setIntermediate(int,Object)
     */
    public void setUsesIntermediate(boolean intermediate) {
        _intermediate = intermediate;
        _owner.clearExtraFieldDataTable();
    }

    /**
     * Whether this field uses impl data in conjunction with standard
     * field data when acting on a {@link OpenJPAStateManager}.
     * Defaults to {@link Boolean#TRUE} (non-cachable impl data).
     *
     * @return {@link Boolean#FALSE} if this field does not use impl data,
     * {@link Boolean#TRUE} if this field uses non-cachable impl
     * data, or <code>null</code> if this field uses impl data that
     * should be cached across instances
     * @see OpenJPAStateManager#setImplData(int,Object)
     */
    public Boolean usesImplData() {
        return _implData;
    }

    /**
     * Whether this field uses impl data in conjunction with standard
     * field data when acting on a {@link OpenJPAStateManager}.
     *
     * @see OpenJPAStateManager#setImplData(int,Object)
     * @see #usesImplData
     */
    public void setUsesImplData(Boolean implData) {
        _implData = implData;
        _owner.clearExtraFieldDataTable();
    }

    /**
     * The orderings for this field to be applied on load, or empty array.
     */
    public Order[] getOrders() {
        if (_orders == null) {
            if (_orderDec == null)
                _orders = getRepository().EMPTY_ORDERS;
            else {
                String[] decs = Strings.split(_orderDec, ",", 0);
                Order[] orders = getRepository().newOrderArray(decs.length);
                int spc;
                boolean asc;
                for (int i = 0; i < decs.length; i++) {
                    decs[i] = decs[i].trim();
                    spc = decs[i].indexOf(' ');
                    if (spc == -1)
                        asc = true;
                    else {
                        asc = decs[i].substring(spc + 1).trim().
                            toLowerCase().startsWith("asc");
                        decs[i] = decs[i].substring(0, spc);
                    }
                    orders[i] = getRepository().newOrder(this, decs[i], asc);
                    //set "isUsedInOrderBy" to the field
                    ClassMetaData elemCls = getElement()
                        .getDeclaredTypeMetaData();
                    if (elemCls != null) {
                      FieldMetaData fmd = elemCls.getDeclaredField(decs[i]);
                      if (fmd != null)
                        fmd.setUsedInOrderBy(true);                      
                    }
                }
                _orders = orders;
            }
        }
        return _orders;
    }

    /**
     * The orderings for this field to be applied on load.
     */
    public void setOrders(Order[] orders) {
        _orderDec = null;
        _orders = orders;
    }

    /**
     * String declaring the orderings for this field to be applied on load,
     * or null. The string is of the form:<br />
     * <code>orderable[ asc|desc][, ...]</code><br />
     * The orderable <code>#element</code> is used to denote the value of
     * the field's elements.
     */
    public String getOrderDeclaration() {
        if (_orderDec == null && _orders != null) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < _orders.length; i++) {
                if (i > 0)
                    buf.append(", ");
                buf.append(_orders[i].getName()).append(" ");
                buf.append((_orders[i].isAscending()) ? "asc" : "desc");
            }
            _orderDec = buf.toString();
        }
        return _orderDec;
    }

    /**
     * String declaring the orderings for this field to be applied on load,
     * or null. The string is of the form:<br />
     * <code>orderable[ asc|desc][, ...]</code><br />
     * The orderable <code>#element</code> is used to denote the value of
     * the field's elements.
     */
    public void setOrderDeclaration(String dec) {
        _orderDec = StringUtils.trimToNull(dec);
        _orders = null;
    }

    /**
     * Order this field value when it is loaded.
     */
    public Object order(Object val) {
        if (val == null)
            return null;

        Order[] orders = getOrders();
        if (orders.length == 0)
            return val;

        // create a comparator for the elements of the value
        Comparator<?> comp;
        if (orders.length == 1)
            comp = orders[0].getComparator();
        else {
            List<Comparator<?>> comps = null;
            Comparator<?> curComp;
            for (int i = 0; i < orders.length; i++) {
                curComp = orders[i].getComparator();
                if (curComp != null) {
                    if (comps == null)
                        comps = new ArrayList<Comparator<?>>(orders.length);
                    if (i != comps.size())
                        throw new MetaDataException(_loc.get
                            ("mixed-inmem-ordering", this));
                    comps.add(curComp);
                }
            }
            if (comps == null)
                comp = null;
            else
                comp = new ComparatorChain(comps);
        }

        if (comp == null)
            return val;

        // sort
        switch (getTypeCode()) {
            case JavaTypes.ARRAY:
                List l = JavaTypes.toList(val, _elem.getType(), true);
                Collections.sort(l, (Comparator<? super Order>) comp);
                return JavaTypes.toArray(l, _elem.getType());
            case JavaTypes.COLLECTION:
                if (val instanceof List)
                    Collections.sort((List) val, (Comparator<? super Order>) comp);
                return val;
            default:
                throw new MetaDataException(_loc.get("cant-order", this));
        }
    }

    /**
     * Whether the field is externalized.
     */
    public boolean isExternalized() {
        return getExternalizerMethod() != null
            || getExternalValueMap() != null;
    }

    /**
     * Convert the given field value to its external value through the
     * provided externalizer, or return the value as-is if no externalizer.
     */
    public Object getExternalValue(Object val, StoreContext ctx) {
        Map extValues = getExternalValueMap();
        if (extValues != null) {
            Object foundVal = extValues.get(val);
            if (foundVal == null) {
                throw new UserException(_loc.get("bad-externalized-value",
                        new Object[] { val, extValues.keySet(), this }))
                        .setFatal(true).setFailedObject(val);
            } else {
                return foundVal;
            }
        }

        Method externalizer = getExternalizerMethod();
        if (externalizer == null)
            return val;

        // special case for queries: allow the given value to pass through
        // as-is if it is already in externalized form
        if (val != null && getType().isInstance(val)
            && (!getDeclaredType().isInstance(val)
            || getDeclaredType() == Object.class))
            return val;

        try {
            // either invoke the static toExternal(val[, ctx]) method, or the
            // non-static val.toExternal([ctx]) method
            if (Modifier.isStatic(externalizer.getModifiers())) {
                if (externalizer.getParameterTypes().length == 1)
                    return externalizer.invoke(null, new Object[]{ val });
                return externalizer.invoke(null, new Object[]{ val, ctx });
            }
            if (val == null)
                return null;
            if (externalizer.getParameterTypes().length == 0)
                return externalizer.invoke(val, (Object[]) null);
            return externalizer.invoke(val, new Object[]{ ctx });
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (Exception e) {
            throw new MetaDataException(_loc.get("externalizer-err", this,
                Exceptions.toString(val), e.toString())).setCause(e);
        }
    }

    /**
     * Return the result of passing the given external value through the
     * factory to get the field value. If no factory is present,
     * the given value is returned as-is.
     */
    public Object getFieldValue(Object val, StoreContext ctx) {
        Map fieldValues = getFieldValueMap();
        if (fieldValues != null)
            return fieldValues.get(val);

        Member factory = getFactoryMethod();
        if (factory == null)
            return val;

        try {
            if (val == null && getNullValue() == NULL_DEFAULT)
                return AccessController.doPrivileged(
                    J2DoPrivHelper.newInstanceAction(getDeclaredType())); 

            // invoke either the constructor for the field type,
            // or the static type.toField(val[, ctx]) method
            if (factory instanceof Constructor) {
                if (val == null)
                    return null;
                return ((Constructor) factory).newInstance
                    (new Object[]{ val });
            }

            Method meth = (Method) factory;
            if (meth.getParameterTypes().length == 1)
                return meth.invoke(null, new Object[]{ val });
            return meth.invoke(null, new Object[]{ val, ctx });
        } catch (Exception e) {
            // unwrap cause
            if (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).
                    getTargetException();
                if (t instanceof Error)
                    throw (Error) t;
                e = (Exception) t;

                // allow null values to cause NPEs and illegal arg exceptions
                // without error
                if (val == null && (e instanceof NullPointerException
                    || e instanceof IllegalArgumentException))
                    return null;
            }

            if (e instanceof OpenJPAException)
                throw (OpenJPAException) e;
            if (e instanceof PrivilegedActionException)
                e = ((PrivilegedActionException) e).getException();
            throw new MetaDataException(_loc.get("factory-err", this,
                Exceptions.toString(val), e.toString())).setCause(e);
        }
    }

    /**
     * The name of this field's externalizer, or null if none.
     */
    public String getExternalizer() {
        return _extName;
    }

    /**
     * The name of this field's externalizer, or null if none.
     */
    public void setExternalizer(String externalizer) {
        _extName = externalizer;
        _extMethod = DEFAULT_METHOD;
    }

    /**
     * The name of this field's factory, or null if none.
     */
    public String getFactory() {
        return _factName;
    }

    /**
     * The name of this field's factory, or null if none.
     */
    public void setFactory(String factory) {
        _factName = factory;
        _factMethod = DEFAULT_METHOD;
    }

    /**
     * Properties string mapping field values to external values.
     */
    public String getExternalValues() {
        return _extString;
    }

    /**
     * Properties string mapping field values to external values.
     */
    public void setExternalValues(String values) {
        _extString = values;
        _extValues = null;
    }

    /**
     * Return the mapping of field values to external values.
     */
    public Map getExternalValueMap() {
        parseExternalValues();
        return _extValues;
    }

    /**
     * Return the mapping of external values to field values.
     */
    public Map getFieldValueMap() {
        parseExternalValues();
        return _fieldValues;
    }

    /**
     * Parse external values into maps.
     */
    private void parseExternalValues() {
        if (_extValues != Collections.EMPTY_MAP
            && _fieldValues != Collections.EMPTY_MAP)
            return;

        if (_extString == null) {
            _extValues = null;
            _fieldValues = null;
            return;
        }

        // parse string into options; this takes care of proper trimming etc
        Options values = Configurations.parseProperties(_extString);
        if (values.isEmpty())
            throw new MetaDataException(_loc.get("no-external-values", this,
                _extString));

        Map extValues = new HashMap((int) (values.size() * 1.33 + 1));
        Map fieldValues = new HashMap((int) (values.size() * 1.33 + 1));
        Map.Entry entry;
        Object extValue, fieldValue;
        for (Iterator itr = values.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            fieldValue = transform((String) entry.getKey(),
                getDeclaredTypeCode());
            extValue = transform((String) entry.getValue(), getTypeCode());

            extValues.put(fieldValue, extValue);
            fieldValues.put(extValue, fieldValue);
        }

        _extValues = extValues;
        _fieldValues = fieldValues;
    }

    /**
     * Return the string value converted to the given type code. The string
     * must be non-null and trimmed.
     */
    private Object transform(String val, int typeCode) {
        if ("null".equals(val))
            return null;

        switch (typeCode) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                return Boolean.valueOf(val);
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                return Byte.valueOf(val);
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                return Integer.valueOf(val);
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                return Long.valueOf(val);
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                return Short.valueOf(val);
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                return Double.valueOf(val);
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                return Float.valueOf(val);
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                return Character.valueOf(val.charAt(0));
            case JavaTypes.STRING:
                return val;
            case JavaTypes.ENUM:
                return Enum.valueOf((Class<? extends Enum>)getDeclaredType(), val);
        }
        throw new MetaDataException(_loc.get("bad-external-type", this));
    }

    /**
     * The externalizer method.
     */
    public Method getExternalizerMethod() {
        if (_manage != MANAGE_PERSISTENT)
            return null;
        if (_extMethod == DEFAULT_METHOD) {
            if (_extName != null) {
                _extMethod = findMethod(_extName);
                if (_extMethod == null)
                    throw new MetaDataException(_loc.get("bad-externalizer",
                        this, _extName));
            } else
                _extMethod = null;
        }
        return _extMethod;
    }

    /**
     * The factory method or constructor.
     */
    public Member getFactoryMethod() {
        if (_manage != MANAGE_PERSISTENT)
            return null;
        if (_factMethod == DEFAULT_METHOD) {
            if (getExternalizerMethod() == null)
                _factMethod = null;
            else {
                try {
                    if (_factName == null)
                        _factMethod = getDeclaredType().getConstructor
                            (new Class[]{ getType() });
                    else
                    	_factMethod = findMethodByNameAndType(_factName, getType());
                } catch (OpenJPAException ke) {
                    throw ke;
                } catch (Exception e) {
                }

                if (!(_factMethod instanceof Constructor)
                    && !(_factMethod instanceof Method))
                    throw new MetaDataException(_loc.get("bad-factory", this));
            }
        }
        return _factMethod;
    }

    /**
     * Find the method for the specified name. Possible forms are:
     * <ul>
     * <li>toExternalString</li>
     * <li>MyFactoryClass.toExternalString</li>
     * <li>com.company.MyFactoryClass.toExternalString</li>
     * </ul>
     *
     * @param method the name of the method to locate
     * @return the method for invocation
     */
    private Method findMethod(String method) {
    	return findMethodByNameAndType(method, null);
    }
    
    /**
     * Find the method for the specified name and type. Possible forms are:
     * <ul>
     * <li>toExternalString</li>
     * <li>MyFactoryClass.toExternalString</li>
     * <li>com.company.MyFactoryClass.toExternalString</li>
     * </ul>
     *
     * @param method the name of the method to locate
     * @param type The type of the parameter which will pass the object from the database.
     * @return the method for invocation
     */
    private Method findMethodByNameAndType(String method, Class<?> type) {
        if (StringUtils.isEmpty(method))
            return null;

        // get class name and get package name divide on the last '.', so the
        // names don't apply in this case, but the methods do what we want
        String methodName = Strings.getClassName(method);
        String clsName = Strings.getPackageName(method);

        Class<?> cls = null;
        Class<?> owner = _owner.getDescribedType();

        if (clsName.length() == 0)
            cls = getDeclaredType();
        else if (clsName.equals(owner.getName())
            || clsName.equals(Strings.getClassName(owner)))
            cls = owner;
        else
            cls = JavaTypes.classForName(clsName, this);

        // find the named method
        Method[] methods = cls.getMethods();
        Class<?>[] params;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                params = methods[i].getParameterTypes();

                // static factory methods require one argument or one argument
                // plus a context; non-static methods require zero arguments or
                // just a context
                if (Modifier.isStatic(methods[i].getModifiers())
                    && (params.length == 1 || (params.length == 2
                    && isStoreContextParameter(params[1]))))
                	
                	if (type == null) {
                		return methods[i];
                	} else if (isConvertibleToByMethodInvocationConversion(type, params[0])) {
                		return methods[i];
                	}
                if (!Modifier.isStatic(methods[i].getModifiers())
                    && (params.length == 0 || (params.length == 1
                    && isStoreContextParameter(params[0]))))
                    return methods[i];
            }
        }

        return null;
    }

	/**
	 * Test if the {@code sourceType} is convertible to the {@code destType}.
	 * Convertible follows the rules in Java Language Specification, 3rd Ed, s5.3 and means that:
	 * <ul>
	 * <li>{@code sourceType} and {@code destType} are the same type (identity conversion)</li>
	 * <li>For primitive types: that {@code sourceType} can be widened into {@code destType} 
	 * or that {@code sourceType} can be boxed into a class assignable to {@code destType}.</li>
	 * <li>For non-primitive types: that the {@code sourceType} can be unboxed into a primitive
	 *  that is the same as, or can be widened into,
	 * {@code destType} or {@code sourceType} can be assigned to {@code destType}.</li> 
	 * 
	 * @return True iff the conditions above are true.
	 */
	private boolean isConvertibleToByMethodInvocationConversion(Class<?> sourceType, Class<?> destType) {
		// Note that class.isAssignableFrom is a widening reference conversion test
		if (sourceType.isPrimitive()) {
			return isConvertibleToByIdentityPrimitiveConversion(sourceType, destType) 
				|| isConvertibleToByWideningPrimitive(sourceType, destType) 
				|| destType.isAssignableFrom(box(sourceType));
		} else {
			// Note that unbox will return null if the sourceType is not a wrapper.  
			// The identity primitive conversion and widening primitive handle this.
			return isConvertibleToByIdentityPrimitiveConversion(unbox(sourceType), destType) 
			|| isConvertibleToByWideningPrimitive(unbox(sourceType), destType) 
			|| destType.isAssignableFrom(sourceType);
		}
	}
	
	/**
	 * @return The results of unboxing {@code sourceType} following Java Language Specification, 3rd Ed, s5.1.8 
	 */
	private Class<?> unbox(Class<?> sourceType) {
		if (sourceType == java.lang.Boolean.class) {
			return java.lang.Boolean.TYPE;
		} else if (sourceType == java.lang.Byte.class) {
			return java.lang.Byte.TYPE;
		} else if (sourceType == java.lang.Short.class) {
			return java.lang.Short.TYPE;
		} else if (sourceType == java.lang.Character.class) {
			return java.lang.Character.TYPE;
		} else if (sourceType == java.lang.Integer.class) {
			return java.lang.Integer.TYPE;
		} else if (sourceType == java.lang.Long.class) {
			return java.lang.Long.TYPE;
		} else if (sourceType == java.lang.Float.class) {
			return java.lang.Float.TYPE;
		} else if (sourceType == java.lang.Double.class) {
			return java.lang.Double.TYPE;
		} else {
			return null;
		}
	}

	/**
	 * @return The results of unboxing {@code sourceType} following Java Language Specification, 3rd Ed, s5.1.7 
	 */
	private Class<?> box(Class<?> sourceType) {
		if (sourceType.isPrimitive()) {
			if (sourceType == java.lang.Boolean.TYPE) {
				return java.lang.Boolean.class;
			} else if (sourceType == java.lang.Byte.TYPE) {
				return java.lang.Byte.class;
			} else if (sourceType == java.lang.Short.TYPE) {
				return java.lang.Short.class;
			} else if (sourceType == java.lang.Character.TYPE) {
				return java.lang.Character.class;
			} else if (sourceType == java.lang.Integer.TYPE) {
				return java.lang.Integer.class;
			} else if (sourceType == java.lang.Long.TYPE) {
				return java.lang.Long.class;
			} else if (sourceType == java.lang.Float.TYPE) {
				return java.lang.Float.class;
			} else if (sourceType == java.lang.Double.TYPE) {
				return java.lang.Double.class;
			} 
			return null;  // Should never be reached because all primitives are accounted for above.
		} else {
			throw new IllegalArgumentException("Cannot box a type that is not a primitive.");
		}
	}
	
	/**
	 * @return true iff {@sourceType} can be converted by a widening primitive conversion
	 *  following Java Language Specification, 3rd Ed, s5.1.2 
	 */
	private boolean isConvertibleToByWideningPrimitive(Class<?> sourceType, Class<?> destType) {
		// Widening conversion following Java Language Specification, s5.1.2.
		if (sourceType == java.lang.Byte.TYPE) {
			return destType == java.lang.Short.TYPE ||
			    destType == java.lang.Integer.TYPE ||
			    destType == java.lang.Long.TYPE ||
			    destType == java.lang.Float.TYPE ||
			    destType == java.lang.Double.TYPE;
		} else if (sourceType == java.lang.Short.TYPE) {
			return destType == java.lang.Integer.TYPE ||
				destType == java.lang.Long.TYPE ||
				destType == java.lang.Float.TYPE ||
				destType == java.lang.Double.TYPE;
		} else if (sourceType == java.lang.Character.TYPE) {
			return destType == java.lang.Integer.TYPE || 
			  	destType == java.lang.Long.TYPE || 
			  	destType == java.lang.Float.TYPE || 
			  	destType == java.lang.Double.TYPE;
		} else if (sourceType == java.lang.Integer.TYPE) {
			return destType == java.lang.Long.TYPE ||
			  	destType == java.lang.Float.TYPE ||
			  	destType == java.lang.Double.TYPE;
		} else if (sourceType == java.lang.Long.TYPE) {
			return destType == java.lang.Float.TYPE ||
			  	destType == java.lang.Double.TYPE;
		} else if (sourceType == java.lang.Float.TYPE) {
			return destType == java.lang.Double.TYPE;
		}
		return false;
	}

	/**
	 * Returns true iff the sourceType is a primitive that can be converted to 
	 * destType using an identity conversion - i.e. sourceType and destType are the same type.
	 * following Java Language Specification, 3rd Ed, s5.1.1 
	 */
	private boolean isConvertibleToByIdentityPrimitiveConversion(Class<?> sourceType, Class<?> destType) {
		return sourceType != null && sourceType.isPrimitive() && sourceType == destType;
	}
	
    /**
     * Return true if the given type is a store context type; we can't
     * use the standard <code>isAssignableFrom</code> because of classloader
     * oddness.
     */
    private static boolean isStoreContextParameter(Class<?> type) {
        return StoreContext.class.getName().equals(type.getName());
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof FieldMetaData))
            return false;
        return getFullName(true).equals(((FieldMetaData) other).
            getFullName(true));
    }

    public int hashCode() {
        return getFullName(true).hashCode();
    }

    public int compareTo(Object other) {
        if (other == null)
            return 1;
        return getFullName(true).compareTo(((FieldMetaData) other).
            getFullName(true));
    }

    public String toString() {
        return getFullName(true);
    }

    ////////////////////////
    // Resolve and validate
    ////////////////////////

    /**
     * Resolve mode for this field.
     */
    public int getResolve() {
        return _resMode;
    }

    /**
     * Resolve mode for this field.
     */
    public void setResolve(int mode) {
        _resMode = mode;
    }

    /**
     * Resolve mode for this field.
     */
    public void setResolve(int mode, boolean on) {
        if (mode == MODE_NONE)
            _resMode = mode;
        else if (on)
            _resMode |= mode;
        else
            _resMode &= ~mode;
    }

    /**
     * Resolve and validate metadata. Return true if already resolved.
     */
    public boolean resolve(int mode) {
        if ((_resMode & mode) == mode)
            return true;
        int cur = _resMode;
        _resMode |= mode;

        Log log = getRepository().getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("resolve-field", _owner + "@"
                + System.identityHashCode(_owner) + "." + _name));

        // we only perform actions for metadata mode
        if ((mode & MODE_META) == 0 || (cur & MODE_META) != 0)
            return false;

        Method externalizer = getExternalizerMethod();
        if (externalizer != null)
            setType(externalizer.getReturnType());

        // only pass on metadata resolve mode so that metadata is always
        // resolved before any other resolve modes our subclasses pass along
        _val.resolve(MODE_META);
        _key.resolve(MODE_META);
        _elem.resolve(MODE_META);

        MetaDataRepository repos = getRepository();
        int validate = repos.getValidate();
        if ((validate & MetaDataRepository.VALIDATE_META) != 0
            && (!ImplHelper.isManagedType(repos.getConfiguration(),
                _owner.getDescribedType())
            || (validate & MetaDataRepository.VALIDATE_UNENHANCED) == 0)) {
            validateLRS();
            if ((validate & MetaDataRepository.VALIDATE_RUNTIME) == 0)
                validateSupportedType();
            validateValue();
            validateExtensionKeys();
        }
        return false;
    }

    /**
     * Validate that this field can be used for LRS.
     */
    private void validateLRS() {
        if (!isLRS())
            return;

        // can't use lrs for arrays
        if (getTypeCode() == JavaTypes.ARRAY)
            throw new MetaDataException(_loc.get("bad-lrs-array", this));

        // can't use lrs for externalized vals
        if (getExternalizerMethod() != null)
            throw new MetaDataException(_loc.get("bad-lrs-extern", this));

        // can't use lrs for concrete types
        if (getType() != Collection.class && getType() != Map.class
            && getType() != Set.class)
            throw new MetaDataException(_loc.get("bad-lrs-concrete", this));
    }

    /**
     * Validate that this field is supported by the runtime.
     */
    private void validateSupportedType() {
        // log warnings about things we don't handle
        OpenJPAConfiguration conf = getRepository().getConfiguration();
        Collection<String> opts = conf.supportedOptions();
        Log log = conf.getLog(OpenJPAConfiguration.LOG_METADATA);
        switch (getTypeCode()) {
            case JavaTypes.PC:
                if (isEmbedded() && !opts.contains(
                	OpenJPAConfiguration.OPTION_EMBEDDED_RELATION)) {
                    setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed", this));
                } else
                if (isEmbedded() && getDeclaredTypeCode() != JavaTypes.PC) {
                    setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed-extern", this));
                }
                break;
            case JavaTypes.COLLECTION:
                if (!opts.contains(OpenJPAConfiguration.OPTION_TYPE_COLLECTION))
                    throw new UnsupportedException(
                        _loc.get("type-not-supported",
                            "Collection", this));
                if (_elem.isEmbeddedPC() && !opts.contains(
                    OpenJPAConfiguration.OPTION_EMBEDDED_COLLECTION_RELATION)){
                    _elem.setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed-element", this));
                }
                break;
            case JavaTypes.ARRAY:
                if (!opts.contains(OpenJPAConfiguration.OPTION_TYPE_ARRAY))
                    throw new UnsupportedException(
                        _loc.get("type-not-supported",
                            "Array", this));
                if (_elem.isEmbeddedPC() && !opts.contains(
                    OpenJPAConfiguration.OPTION_EMBEDDED_COLLECTION_RELATION)) {
                    _elem.setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed-element", this));
                }
                break;
            case JavaTypes.MAP:
                if (!opts.contains(OpenJPAConfiguration.OPTION_TYPE_MAP))
                    throw new UnsupportedException(
                        _loc.get("type-not-supported",
                            "Map", this));
                if (_elem.isEmbeddedPC() && !opts.contains(
                	OpenJPAConfiguration.OPTION_EMBEDDED_MAP_RELATION)) {
                    _elem.setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed-element", this));
                }
                if (_key.isEmbeddedPC() && !opts.contains(
                	OpenJPAConfiguration.OPTION_EMBEDDED_MAP_RELATION)) {
                    _key.setEmbedded(false);
                    if (log.isWarnEnabled())
                        log.warn(_loc.get("cant-embed-key", this));
                }
                break;
        }
    }

    /**
     * Validate our value strategy.
     */
    private void validateValue() {
        if (getExternalizerMethod() != null && getExternalValueMap() != null)
            throw new MetaDataException(_loc.get("extern-externvalues", this));
        if (getValueStrategy() == ValueStrategies.SEQUENCE
            && getValueSequenceName() == null)
            throw new MetaDataException(_loc.get("no-seq-name", this));
        ValueStrategies.assertSupported(getValueStrategy(), this,
            "value strategy");
    }

    /**
     * Copy state from the given field to this one. Do not copy mapping
     * information.
     */
    public void copy(FieldMetaData field) {
        super.copy(field);

        _intermediate = field.usesIntermediate();
        _implData = field.usesImplData();

        // copy field-level info; use get methods to force resolution of
        // lazy data
        _proxyClass = field.getProxyType();
        _initializer = field.getInitializer();
        _transient = field.isTransient();
        _nullValue = field.getNullValue();
        _manage = field.getManagement();
        _explicit = field.isExplicit();
        _extName = field.getExternalizer();
        _extMethod = DEFAULT_METHOD;
        _factName = field.getFactory();
        _factMethod = DEFAULT_METHOD;
        _extString = field.getExternalValues();
        _extValues = Collections.EMPTY_MAP;
        _fieldValues = Collections.EMPTY_MAP;
        _primKey = field.isPrimaryKey();
        _backingMember = field._backingMember;
        _enumField = field._enumField;
        _lobField = field._lobField;
        _serializableField = field._serializableField;
        _generated = field._generated;
        _mappedByIdValue = field._mappedByIdValue;
        _isElementCollection = field._isElementCollection;
        _access = field._access;
        _orderDec = field._orderDec;
        _useSchemaElement = field._useSchemaElement;

        // embedded fields can't be versions
        if (_owner.getEmbeddingMetaData() == null && _version == null)
            _version = (field.isVersion()) ? Boolean.TRUE : Boolean.FALSE;

        // only copy this data if not already set explicitly in this instance
        if (_dfg == 0) {
            _dfg = (field.isInDefaultFetchGroup()) ? DFG_TRUE : DFG_FALSE;
            if (field.isDefaultFetchGroupExplicit())
                _dfg |= DFG_EXPLICIT;
        }
        if (_fgSet == null && field._fgSet != null)
            _fgSet = new HashSet(field._fgSet);
        if (_lfg == null)
            _lfg = field.getLoadFetchGroup();
        if (_lrs == null)
            _lrs = (field.isLRS()) ? Boolean.TRUE : Boolean.FALSE;
        if (_valStrategy == -1)
            _valStrategy = field.getValueStrategy();
        if (_upStrategy == -1)
            _upStrategy = field.getUpdateStrategy();
        if (ClassMetaData.DEFAULT_STRING.equals(_seqName)) {
            _seqName = field.getValueSequenceName();
            _seqMeta = null;
        }
        if (ClassMetaData.DEFAULT_STRING.equals(_inverse))
            _inverse = field.getInverse();

        // copy value metadata
        _val.copy(field);
        _key.copy(field.getKey());
        _elem.copy(field.getElement());
    }

    protected void addExtensionKeys(Collection exts) {
        getRepository().getMetaDataFactory().addFieldExtensionKeys(exts);
    }

    ///////////////
    // Commentable
    ///////////////

    public String[] getComments() {
        return (_comments == null) ? EMPTY_COMMENTS : _comments;
    }

    public void setComments(String[] comments) {
        _comments = comments;
    }

    ////////////////////////////////
    // ValueMetaData implementation
    ////////////////////////////////

    public FieldMetaData getFieldMetaData() {
        return this;
    }

    public Class getType() {
        return _val.getType();
    }

    public void setType(Class type) {
        _val.setType(type);
        if (type.isArray())
            _elem.setType(type.getComponentType());
        else if (type == Properties.class) {
            _key.setType(String.class);
            _elem.setType(String.class);
        }
    }

    public int getTypeCode() {
        return _val.getTypeCode();
    }

    public void setTypeCode(int code) {
        _val.setTypeCode(code);
    }

    public boolean isTypePC() {
        return _val.isTypePC();
    }

    public ClassMetaData getTypeMetaData() {
        return _val.getTypeMetaData();
    }

    public Class getDeclaredType() {
        return _val.getDeclaredType();
    }

    public void setDeclaredType(Class type) {
        _val.setDeclaredType(type);
        if (type.isArray())
            _elem.setDeclaredType(type.getComponentType());
        else if (type == Properties.class) {
            _key.setDeclaredType(String.class);
            _elem.setDeclaredType(String.class);
        }
    }

    public int getDeclaredTypeCode() {
        return _val.getDeclaredTypeCode();
    }

    public void setDeclaredTypeCode(int type) {
        _val.setDeclaredTypeCode(type);
    }

    public boolean isDeclaredTypePC() {
        return _val.isDeclaredTypePC();
    }

    public ClassMetaData getDeclaredTypeMetaData() {
        return _val.getDeclaredTypeMetaData();
    }

    public boolean isEmbedded() {
        return _val.isEmbedded();
    }

    public void setEmbedded(boolean embedded) {
        _val.setEmbedded(embedded);
    }

    public boolean isEmbeddedPC() {
        return _val.isEmbeddedPC();
    }

    public ClassMetaData getEmbeddedMetaData() {
        return _val.getEmbeddedMetaData();
    }

    public ClassMetaData addEmbeddedMetaData(int access) {
        return _val.addEmbeddedMetaData(access);
    }
    public ClassMetaData addEmbeddedMetaData() {
        return _val.addEmbeddedMetaData();
    }

    public int getCascadeDelete() {
        return _val.getCascadeDelete();
    }

    public void setCascadeDelete(int delete) {
        _val.setCascadeDelete(delete);
    }

    public int getCascadePersist() {
        return _val.getCascadePersist();
    }

    public void setCascadePersist(int persist) {
        _val.setCascadePersist(persist);
    }

    public int getCascadeAttach() {
        return _val.getCascadeAttach();
    }

    public void setCascadeAttach(int attach) {
        _val.setCascadeAttach(attach);
    }
    
    public int getCascadeDetach() {
        return _val.getCascadeDetach();
    }

    public void setCascadeDetach(int detach) {
        _val.setCascadeDetach(detach);
    }

    public int getCascadeRefresh() {
        return _val.getCascadeRefresh();
    }

    public void setCascadeRefresh(int refresh) {
        _val.setCascadeRefresh(refresh);
    }

    public boolean isSerialized() {
        return _val.isSerialized();
    }

    public void setSerialized(boolean serialized) {
        _val.setSerialized(serialized);
    }

    public String getValueMappedBy() {
        return _val.getValueMappedBy();
    }

    public void setValueMappedBy(String mapped) {
        _val.setValueMappedBy(mapped);
    }

    public FieldMetaData getValueMappedByMetaData () {
		return _val.getValueMappedByMetaData ();
	}

	public Class<?> getTypeOverride () {
		return _val.getTypeOverride ();
	}

	public void setTypeOverride(Class type) {
		_val.setTypeOverride (type);
	}

	public void copy (ValueMetaData vmd) {
		_val.copy (vmd);
	}

    /**
     * Check if this field is used by other field as "order by" value.
     *
     * @since 1.1.0
     */
    public boolean isUsedInOrderBy() {
    	return _usedInOrderBy;
    }
    
    /**
     * Whether this field is used by other field as "order by" value .
     *
     * @since 1.1.0
     */
    public void setUsedInOrderBy(boolean isUsed) {
    	_usedInOrderBy = isUsed;
    }
    
    /**
     * Serializable wrapper around a {@link Method} or {@link Field}. For 
     * space considerations, this does not support {@link Constructor}s.
     */
	public static class MemberProvider
        implements Externalizable {

        private transient Member _member;

        public MemberProvider() {
            // for externalization
        }

        MemberProvider(Member member) {
            if (member instanceof Constructor)
                throw new IllegalArgumentException();

            _member = member;
        }

        public Member getMember() {
            return _member;
        }

        public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
            boolean isField = in.readBoolean();
            Class<?> cls = (Class<?>) in.readObject();
            String memberName = (String) in.readObject();
            try {
                if (isField)
                    _member = AccessController.doPrivileged(
                        J2DoPrivHelper.getDeclaredFieldAction(
                            cls, memberName)); 
                else {
                    Class<?>[] parameterTypes = (Class[]) in.readObject();
                    _member = AccessController.doPrivileged(
                        J2DoPrivHelper.getDeclaredMethodAction(
                            cls, memberName, parameterTypes));
                }
            } catch (SecurityException e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            } catch (PrivilegedActionException pae) {
                IOException ioe = new IOException(
                    pae.getException().getMessage());
                ioe.initCause(pae);
                throw ioe;
            }
        }

        public void writeExternal(ObjectOutput out)
            throws IOException {
            boolean isField = _member instanceof Field;
            out.writeBoolean(isField);
            out.writeObject(_member.getDeclaringClass());
            out.writeObject(_member.getName());
            if (!isField)
                out.writeObject(((Method) _member).getParameterTypes());
        }
    }

    public boolean isValueGenerated() {
        return _generated;
    }

    public void setValueGenerated(boolean generated) {
        this._generated = generated;
    }

    public boolean isElementCollection() {
        return _isElementCollection;
    }

    public void setElementCollection(boolean isElementCollection) {
        this._isElementCollection = isElementCollection;
    }

    public String getMappedByIdValue() {
        return _mappedByIdValue;
    }

    public void setMappedByIdValue(String mappedByIdValue) {
        this._mappedByIdValue = mappedByIdValue;
    }
    
    public boolean isMappedById() {
    	return (_mappedByIdValue != null);
    }
    
    /**
     * Gets the access type used by this field. If no access type is set for
     * this field then return the access type used by the declaring class.
     */
    public int getAccessType() {
        if (AccessCode.isUnknown(_access)) {
        	int fCode = AccessCode.toFieldCode(getDeclaringMetaData()
        			.getAccessType());
        	return fCode;
        }
        return _access;
    }
    
    /**
     * Sets access type of this field. The access code is verified for validity
     * as well as against the access style used by the declaring class.
     */
    public void setAccessType(int fCode) {
    	ClassMetaData owner = getDeclaringMetaData();
    	owner.mergeFieldAccess(this, fCode);
        _access = fCode;
    }
    
    public int getAssociationType() {
        return _associationType;
    }
    
    public void setAssociationType(int type) {
        _associationType = type;
    }

    public boolean isPersistentCollection() {
        return _persistentCollection;
    }

    public void setPersistentCollection(boolean persistentCollection) {
        _persistentCollection = persistentCollection;
    }
    private Class<?> _relationType = Unknown.class;
    public Class<?> getRelationType() {
    	if (_relationType == Unknown.class) {
            if (isDeclaredTypePC())
            	_relationType = getDeclaredType();
            else if (getElement().isDeclaredTypePC())
            	_relationType = getElement().getDeclaredType();
            else if (getKey().isDeclaredTypePC())
            	_relationType = getKey().getDeclaredType();
            else
            	_relationType = null;
    	}
    	return _relationType;
    }
    private class Unknown{};
    
    public boolean isDelayCapable() {
        if (_delayCapable != null) {
            return _delayCapable.booleanValue();
        }
        if (getTypeCode() != JavaTypes.COLLECTION || isLRS()) {
           _delayCapable = Boolean.FALSE;
           return _delayCapable;
        } else {
            // Verify the proxy manager is configured to handle delay loading
            ProxyManager pm = getRepository().getConfiguration().getProxyManagerInstance();
            if (pm != null) {
                _delayCapable = pm.getDelayCollectionLoading();
            } else {
                _delayCapable = Boolean.FALSE;
            }
        }
        return _delayCapable;
    }
    
    public void setDelayCapable(Boolean delayCapable) {
        _delayCapable = delayCapable;
    }

    /**
     * Whether to include schema name in generated files
     */
    public boolean getUseSchemaElement() {
        return _useSchemaElement;
    }

    /**
     * Whether to include schema name in generated files
     */
    public void setUseSchemaElement(boolean _useSchemaElement) {
        this._useSchemaElement = _useSchemaElement;
    }
}
