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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.CacheDistributionPolicy;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.lib.conf.Value;
import org.apache.openjpa.lib.conf.ValueListener;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.xml.Commentable;
import org.apache.openjpa.util.BigDecimalId;
import org.apache.openjpa.util.BigIntegerId;
import org.apache.openjpa.util.BooleanId;
import org.apache.openjpa.util.ByteId;
import org.apache.openjpa.util.CharId;
import org.apache.openjpa.util.DateId;
import org.apache.openjpa.util.DoubleId;
import org.apache.openjpa.util.FloatId;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.IntId;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.LongId;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.ObjectId;
import org.apache.openjpa.util.OpenJPAId;
import org.apache.openjpa.util.ShortId;
import org.apache.openjpa.util.StringId;
import org.apache.openjpa.util.UnsupportedException;

import serp.util.Strings;

/**
 * Contains metadata about a persistent type.
 * This metadata is available both at enhancement time and runtime.
 *  Note that this class employs aggressive caching, and therefore it is
 * important to finalize the configuration of field metadatas before invoking
 * methods that depend on that configuration, such as
 * {@link #getPrimaryKeyFields}.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class ClassMetaData
    extends Extensions
    implements Comparable<ClassMetaData>, SourceTracker, MetaDataContext, 
    MetaDataModes, Commentable, ValueListener {

    /**
     * Unknown identity type.
     */
    public static final int ID_UNKNOWN = 0;

    /**
     * Datastore identity type.
     */
    public static final int ID_DATASTORE = 1;

    /**
     * Application identity type.
     */
    public static final int ID_APPLICATION = 2;

    /**
     * Unknown access type.
     */
    public static final int ACCESS_UNKNOWN = AccessCode.UNKNOWN;

    /**
     * Persistent attributes are accessed via direct field access. Bit flag.
     */
    public static final int ACCESS_FIELD = AccessCode.FIELD;

    /**
     * Persistent attributes are accessed via setters and getters. Bit flag.
     */
    public static final int ACCESS_PROPERTY = AccessCode.PROPERTY;

    /**
     * Persistent class has explicitly defined an access type. 
     * This will allow the attributes to use mixed access i.e. some field 
     * may use ACCESS_FIELD while others ACCESS_PROPERTY. 
     */
    public static final int ACCESS_EXPLICIT = AccessCode.EXPLICIT;
    
    /**
     * Value for using a synthetic detached state field, which is the default.
     */
    public static final String SYNTHETIC = "`syn";

    protected static final String DEFAULT_STRING = "`";

    private static final Localizer _loc = Localizer.forPackage
        (ClassMetaData.class);

    private static final FetchGroup[] EMPTY_FETCH_GROUP_ARRAY
        = new FetchGroup[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private MetaDataRepository _repos;
    private transient ClassLoader _loader = null;

    private final ValueMetaData _owner;
    private final LifecycleMetaData _lifeMeta = new LifecycleMetaData(this);
    private File _srcFile = null;
    private String _srcName = null;
    private int _srcType = SRC_OTHER;
    private int _lineNum = 0;  
    private int _colNum = 0;  
    private String[] _comments = null;
    private int _listIndex = -1;
    private int _srcMode = MODE_META | MODE_MAPPING;
    private int _resMode = MODE_NONE;

    private Class<?> _type = Object.class;
    private int _hashCode = Object.class.getName().hashCode();
    private String _typeString = Object.class.getName();
    private final Map<String,FieldMetaData> _fieldMap = new TreeMap<String,FieldMetaData>();
    private Map<String,FieldMetaData> _supFieldMap = null;
    private boolean _defSupFields = false;
    private Collection<String> _staticFields = null;
    private int[] _fieldDataTable = null;
    private Map<String,FetchGroup> _fgMap = null;

    ////////////////////////////////////////////////////////////////////
    // Note: if you add additional state, make sure to add it to copy()
    ////////////////////////////////////////////////////////////////////

    private Class<?> _objectId = null;
    private Boolean _objectIdShared = null;
    private Boolean _openjpaId = null;
    private Boolean _extent = null;
    private Boolean _embedded = null;
    private boolean _embeddable = false;
    private Boolean _interface = null;
    private Class<?> _impl = null;
    private List<Class<?>> _interfaces = null;
    private final Map<Class<?>,Map<String,String>> _ifaceMap = 
    	new HashMap<Class<?>,Map<String,String>>();
    private Integer _identity = null;
    private int _idStrategy = ValueStrategies.NONE;
    private int _accessType = AccessCode.UNKNOWN;
    
    private String _seqName = DEFAULT_STRING;
    private SequenceMetaData _seqMeta = null;
    private String _cacheName = DEFAULT_STRING; // null implies @DataCache(enabled=false)
    private boolean _dataCacheEnabled = false;     // true implies the class has been annotated by the user or name of
                                                // the cache is explicitly set by the user to a null string

    private Boolean _cacheEnabled = null;       // denotes status of JPA 2 @Cacheable annotation
    private int _cacheTimeout = Integer.MIN_VALUE;
    private Boolean _detachable = null;
    private String _detachState = DEFAULT_STRING;
    private String _alias = null;
    private int _versionIdx = Integer.MIN_VALUE;

    private Class<?> _super = null;
    private ClassMetaData _superMeta = null;
    private Class<?>[] _subs = null;
    private ClassMetaData[] _subMetas = null;
    private ClassMetaData[] _mapSubMetas = null;

    private FieldMetaData[] _fields = null;
    private FieldMetaData[] _unmgdFields = null;
    private FieldMetaData[] _allFields = null;
    private FieldMetaData[] _allPKFields = null;
    private FieldMetaData[] _allDFGFields = null;
    private FieldMetaData[] _definedFields = null;
    private FieldMetaData[] _listingFields = null;
    private FieldMetaData[] _allListingFields = null;
    private FieldMetaData[] _allProxyFields = null;
    private FieldMetaData[] _allLrsFields = null;
    private FetchGroup[] _fgs = null;
    private FetchGroup[] _customFGs = null;
    private boolean _intercepting = false;
    private Boolean _useIdClassFromParent = null;
    private boolean _abstract = false;
    private Boolean _hasAbstractPKField = null;
    private Boolean _hasPKFieldsFromAbstractClass = null;
    private int[] _pkAndNonPersistentManagedFmdIndexes = null;
    private Boolean inverseManagedFields = null;
    private List<FieldMetaData> _mappedByIdFields;
    private boolean _mappedByIdFieldsSet = false;
    private boolean _useSchemaElement = true;

    /**
     * Constructor. Supply described type and repository.
     */
    protected ClassMetaData(Class<?> type, MetaDataRepository repos) {
        _repos = repos;
        _owner = null;
        setDescribedType(type);
        registerForValueUpdate("DataCacheTimeout");
    }

    /**
     * Embedded constructor. Supply embedding value.
     */
    protected ClassMetaData(ValueMetaData owner) {
        _owner = owner;
        _repos = owner.getRepository();
        setEnvClassLoader(owner.getFieldMetaData().getDefiningMetaData().
            getEnvClassLoader());
        registerForValueUpdate("DataCacheTimeout");
    }

    /**
     * Return the owning repository.
     */
    public MetaDataRepository getRepository() {
        return _repos;
    }

    /**
     * If this metadata is for an embedded object, returning the owning value.
     */
    public ValueMetaData getEmbeddingMetaData() {
        return _owner;
    }

    /**
     * The persistence capable class described by this metadata.
     */
    public Class<?> getDescribedType() {
        return _type;
    }
    
    /**
     * The persistence capable stringified class described by this metadata.
     */
    public String getDescribedTypeString(){
        return _typeString;
    }
    
    /**
     * Set the class described by this metadata. The type may be reset when
     * an embedded value changes its declared type.
     */
    protected void setDescribedType(Class<?> type) {
        if (type.getSuperclass() != null && "java.lang.Enum".equals
            (type.getSuperclass().getName()))
            throw new MetaDataException(_loc.get("enum", type));
        _type = type;
        _typeString = _type.getName();
        _hashCode = _typeString.hashCode();
        if (PersistenceCapable.class.isAssignableFrom(type))
            setIntercepting(true);
    }

    /**
     * The environmental loader used when loading this metadata.
     * The class metadata should use this loader when loading metadata for
     * its superclass and field types.
     */
    public ClassLoader getEnvClassLoader() {
        return _loader;
    }

    /**
     * The class environmental loader used when loading this metadata.
     * The class metadata should use this loader when loading metadata for
     * its superclass and field types.
     */
    public void setEnvClassLoader(ClassLoader loader) {
        _loader = loader;
    }

    /**
     * The persistence capable superclass of the described type.
     */
    public Class<?> getPCSuperclass() {
        return _super;
    }

    /**
     * The persistence capable superclass of the described type.
     */
    public void setPCSuperclass(Class<?> pc) {
        clearAllFieldCache();
        _super = pc;
    }

    /**
     * The metadata for this class' superclass.
     */
    public ClassMetaData getPCSuperclassMetaData() {
        if (_superMeta == null && _super != null) {
            if (_owner != null) {
                _superMeta = _repos.newEmbeddedClassMetaData(_owner);
                _superMeta.setDescribedType(_super);
            } else
                _superMeta = _repos.getMetaData(_super, _loader, true);
        }
        return _superMeta;
    }

    /**
     * The metadata for this class' superclass.
     */
    public void setPCSuperclassMetaData(ClassMetaData meta) {
        clearAllFieldCache();
        _superMeta = meta;
        if (meta != null)
            setPCSuperclass(meta.getDescribedType());
    }

    /**
     * Whether this class is mapped to the datastore. By default, only
     * returns false if class is embedded-only, but subclasses might override
     * to allow unmapped other types.
     */
    public boolean isMapped() {
        return _embedded != Boolean.TRUE;
    }

    /**
     * Return the closest mapped superclass.
     */
    public ClassMetaData getMappedPCSuperclassMetaData() {
        ClassMetaData sup = getPCSuperclassMetaData();
        if (sup == null || sup.isMapped())
            return sup;
        return sup.getMappedPCSuperclassMetaData();
    }

    /**
     * Return the known persistence capable subclasses of the described type,
     * or empty array if none or if this is embedded metadata.
     */
    public Class<?>[] getPCSubclasses() {
        if (_owner != null)
            return MetaDataRepository.EMPTY_CLASSES;

        _repos.processRegisteredClasses(_loader);
        if (_subs == null) {
            Collection<Class<?>> subs = _repos.getPCSubclasses(_type);
            _subs = (Class[]) subs.toArray(new Class[subs.size()]);
        }
        return _subs;
    }

    /**
     * Return the metadata for the known persistence capable subclasses of
     * the described type, or empty array if none or if this is embedded
     * metadata.
     */
    public ClassMetaData[] getPCSubclassMetaDatas() {
        if (_owner != null)
            return _repos.EMPTY_METAS;

        Class<?>[] subs = getPCSubclasses(); // checks for new
        if (_subMetas == null) {
            if (subs.length == 0)
                _subMetas = _repos.EMPTY_METAS;
            else {
                ClassMetaData[] metas = _repos.newClassMetaDataArray
                    (subs.length);
                for (int i = 0; i < subs.length; i++)
                    metas[i] = _repos.getMetaData(subs[i], _loader, true);
                _subMetas = metas;
            }
        }
        return _subMetas;
    }

    /**
     * Return all mapped subclasses.
     */
    public ClassMetaData[] getMappedPCSubclassMetaDatas() {
        if (_owner != null)
            return _repos.EMPTY_METAS;

        ClassMetaData[] subs = getPCSubclassMetaDatas(); // checks for new
        if (_mapSubMetas == null) {
            if (subs.length == 0)
                _mapSubMetas = subs;
            else {
                List<ClassMetaData> mapped = 
                	new ArrayList<ClassMetaData>(subs.length);
                for (int i = 0; i < subs.length; i++)
                    if (subs[i].isMapped())
                        mapped.add(subs[i]);
                _mapSubMetas = (ClassMetaData[]) mapped.toArray
                    (_repos.newClassMetaDataArray(mapped.size()));
            }
        }
        return _mapSubMetas;
    }

    /**
     * The type of identity being used. This will be one of:
     * <ul>
     * <li>{@link #ID_UNKNOWN}: unknown identity type</li>
     * <li>{@link #ID_DATASTORE}: identity managed by the data store and
     * independent	of the fields of the instance</li>
     * <li>{@link #ID_APPLICATION}: identity managed by the application and
     * defined by one or more fields of the instance</li>
     * </ul> If unspecified, defaults to {@link #ID_DATASTORE} if there are no
     * primary key fields, and {@link #ID_APPLICATION} otherwise.
     */
    public int getIdentityType() {
    	if (_identity != null) {
    		return _identity;
    	} else {
    		ClassMetaData sup = getPCSuperclassMetaData();
	        if (sup != null && sup.getIdentityType() != ID_UNKNOWN) 
	            _identity = sup.getIdentityType();
	        else if (getPrimaryKeyFields().length > 0) 
	            _identity = ID_APPLICATION;
	        else if (isMapped()) 
	            _identity = ID_DATASTORE;
	        else if (isAbstract())
	        	_identity = ID_UNKNOWN;
	        else
	            _identity = _repos.getMetaDataFactory().getDefaults().
	            	 getDefaultIdentityType();
    	}
        return _identity;
    }
  
    /**
     * The type of identity being used. This will be one of:
     * <ul>
     * <li>{@link #ID_UNKNOWN}: unknown identity type</li>
     * <li>{@link #ID_DATASTORE}: identity managed by the data store and
     * independent	of the fields of the instance</li>
     * <li>{@link #ID_APPLICATION}: identity managed by the application and
     * defined by one or more fields of the instance</li>
     * </ul> If unspecified, defaults to {@link #ID_DATASTORE} if there are no
     * primary key fields, and {@link #ID_APPLICATION} otherwise.
     */
    public void setIdentityType(int type) {
        _identity = type;
        if (type != ID_APPLICATION) {
            _objectId = null;
            _openjpaId = null;
        }
    }

    /**
     * The metadata-specified class to use for the object ID.
     */
    public Class<?> getObjectIdType() {
        // if this entity does not use IdClass from the parent entity,
        // just return the _objectId set during annotation parsing time.
        if (!useIdClassFromParent()) {
            if (_objectId != null)
                return _objectId;
        } 
        
        // if this entity uses IdClass from the parent entity,
        // the _objectId set during the parsing time should be
        // ignored, and let the system determine the objectId type
        // of this entity.
        if (getIdentityType() != ID_APPLICATION)
            return null;
        ClassMetaData sup = getPCSuperclassMetaData();
        if (sup != null && sup.getIdentityType() != ID_UNKNOWN) {
            _objectId = sup.getObjectIdType();
            return _objectId;
        }

        // figure out OpenJPA identity type based on primary key field
        FieldMetaData[] pks = getPrimaryKeyFields();
        if (pks.length != 1)
            return null;
        switch (pks[0].getObjectIdFieldTypeCode()) {
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                _objectId = ByteId.class;
                break;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                _objectId = CharId.class;
                break;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                _objectId = DoubleId.class;
                break;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                _objectId = FloatId.class;
                break;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                _objectId = IntId.class;
                break;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                _objectId = LongId.class;
                break;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                _objectId = ShortId.class;
                break;
            case JavaTypes.STRING:
                _objectId = StringId.class;
                break;
            case JavaTypes.DATE:
                _objectId = DateId.class;
                break;
            case JavaTypes.OID:
            case JavaTypes.OBJECT:
                _objectId = ObjectId.class;
                break;
            case JavaTypes.BIGDECIMAL:
                _objectId = BigDecimalId.class;
                break;
            case JavaTypes.BIGINTEGER:
                _objectId = BigIntegerId.class;
                break;
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                _objectId = BooleanId.class;
                break;
        }
        return _objectId;
    }

    /**
     * The metadata-specified class to use for the object ID.
     * When there is IdClass annotation, AnnotationMetaDataParser
     * will call this method to set ObjectId type. However, if 
     * this is a derived identity in the child entity where a 
     * relation field (parent entity) is used as an id, and this 
     * relation field has an IdClass, the IdClass annotation in 
     * the child entity can be ignored as Openjpa will automatically   
     * wrap parent's IdClass as child's IdClass. 
     */
    public void setObjectIdType(Class<?> cls, boolean shared) {
        _objectId = null;
        _openjpaId = null;
        _objectIdShared = null;
        if (cls != null) {
            // don't let people assign OpenJPAId types; safer to calculate it
            // ourselves
            setIdentityType(ID_APPLICATION);
            if (!OpenJPAId.class.isAssignableFrom(cls)) {
                _objectId = cls;
                _objectIdShared = (shared) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
    }

    /**
     * Whether this type uses an application identity class that is shared
     * with other classes, and is therefore wrapped in an {@link ObjectId}.
     */
    public boolean isObjectIdTypeShared() {
        if (_objectIdShared != null)
            return _objectIdShared.booleanValue();
        if (_super != null)
            return getPCSuperclassMetaData().isObjectIdTypeShared();
        return isOpenJPAIdentity();
    }

    /**
     * Whether this type uses OpenJPA identity.
     */
    public boolean isOpenJPAIdentity() {
        if (_openjpaId == null) {
            Class<?> cls = getObjectIdType();
            if (cls == null)
                return false;
            _openjpaId = (OpenJPAId.class.isAssignableFrom(cls)) ? Boolean.TRUE
                : Boolean.FALSE;
        }
        return _openjpaId.booleanValue();
    }

    /**
     * The strategy to use for datastore identity generation.
     * One of the constants from {@link ValueStrategies}.
     */
    public int getIdentityStrategy() {
        if (getIdentityType() == ID_DATASTORE
            && _idStrategy == ValueStrategies.NONE) {
            ClassMetaData sup = getPCSuperclassMetaData();
            if (sup != null && sup.getIdentityType() != ID_UNKNOWN)
                _idStrategy = sup.getIdentityStrategy();
            else
                _idStrategy = ValueStrategies.NATIVE;
        }
        return _idStrategy;
    }

    /**
     * The strategy to use for datastore identity generation.
     * One of the constants from {@link ValueStrategies}.
     */
    public void setIdentityStrategy(int strategy) {
        _idStrategy = strategy;
        if (strategy != ValueStrategies.SEQUENCE)
            setIdentitySequenceName(null);
    }

    /**
     * The datastore identity sequence name, or null for none.
     */
    public String getIdentitySequenceName() {
        if (DEFAULT_STRING.equals(_seqName)) {
            if (_super != null)
                _seqName = getPCSuperclassMetaData().getIdentitySequenceName();
            else
                _seqName = null;
        }
        return _seqName;
    }

    /**
     * The datastore identity sequence name, or null for none.
     */
    public void setIdentitySequenceName(String seqName) {
        _seqName = seqName;
        _seqMeta = null;
        if (seqName != null)
            setIdentityStrategy(ValueStrategies.SEQUENCE);
    }

    /**
     * Metadata for the datastore identity sequence.
     */
    public SequenceMetaData getIdentitySequenceMetaData() {
        if (_seqMeta == null && getIdentitySequenceName() != null)
            _seqMeta = _repos.getSequenceMetaData(this,
                getIdentitySequenceName(), true);
        return _seqMeta;
    }

    /**
     * Information about lifecycle callbacks for this class.
     */
    public LifecycleMetaData getLifecycleMetaData() {
        return _lifeMeta;
    }

    /**
     * Returns the alias for the described type, or <code>null</code> if none
     * has been set.
     * 
     * @see #setTypeAlias
     */
    public String getTypeAlias() {
        if (_alias == null)
            _alias = Strings.getClassName(_type);
        return _alias;
    }

    /**
     * Sets the alias for the described type. The alias can be
     * any arbitrary string that the implementation can later use to
     * refer to the class. Note that at runtime, only the alias
     * computed when the persistent type was enhanced is used.
     *
     * @param alias the alias name to apply to the described type
     */
    public void setTypeAlias(String alias) {
        _alias = alias;
    }

    /**
     * The access type used by this class. 
     * 
     */
    public int getAccessType() {
        return _accessType;
    }
    
    /**
     * Sets the access type. 
     */
    public void setAccessType(int type) {
    	if (type == _accessType || type == AccessCode.UNKNOWN)
    		return;
    	if (!AccessCode.isValidClassCode(type)) {
            throw new IllegalArgumentException(_loc.get("access-type-invalid", 
    		    this, AccessCode.toClassString(type)).getMessage());
    	}
    	if (_accessType != AccessCode.UNKNOWN) { // changing access type
    	    _repos.getLog().trace(_loc.get("access-type-change", 
    		    this, AccessCode.toClassString(type), 
    		    AccessCode.toClassString(_accessType)).getMessage());
    	}
        _accessType = type;
    }
    
    /**
     * Asserts the the given field (which must belong to this receiver)
     * can be set to the given access code. If the field code is allowed,
     * it may have the side-effect of changing the access code of this receiver.
     */
    void mergeFieldAccess(FieldMetaData fmd, int fCode) {
    	setAccessType(AccessCode.mergeFieldCode(this, fmd, fCode));
    }

    /**
     * Affirms if access style is explicitly defined.
     */    
    public boolean isExplicitAccess() {
        return AccessCode.isExplicit(_accessType);
    }

    /**
     * Affirms if attributes of this class use mixed access types.
     */    
    public boolean isMixedAccess() {
    	return AccessCode.isMixed(_accessType);
    }
    
    /**
     * Whether the type requires extent management.
     */
    public boolean getRequiresExtent() {
        if (_owner != null || isEmbeddedOnly())
            return false;

        if (_extent == null) {
            ClassMetaData sup = getPCSuperclassMetaData();
            if (sup != null)
                _extent = (sup.getRequiresExtent()) ? Boolean.TRUE
                    : Boolean.FALSE;
            else
                _extent = Boolean.TRUE;
        }
        return _extent.booleanValue();
    }

    /**
     * Whether the type requires extent management.
     */
    public void setRequiresExtent(boolean req) {
        _extent = (req) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Whether the type can only be used as an embedded object.
     */
    public boolean isEmbeddedOnly() {
        if (_embedded == null) {
            ClassMetaData sup = getPCSuperclassMetaData();
            if (sup != null)
                _embedded = (sup.isEmbeddedOnly()) ? Boolean.TRUE
                    : Boolean.FALSE;
            else
                _embedded = Boolean.FALSE;
        }
        return _embedded.booleanValue();
    }

    /**
     * Whether the type can only be used as an embedded object.
     */
    public void setEmbeddedOnly(boolean embed) {
        _embedded = (embed) ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public boolean isEmbeddable() {
        return _embeddable;
    }
    
    public void setEmbeddable() {
        _embeddable = true;
    }

    /**
     * Whether the type's fields are actively intercepted, either by
     * redefinition or enhancement.
     */
    public boolean isIntercepting() {
        return _intercepting;
    }

    /**
     * Whether the type's fields are actively intercepted, either by
     * redefinition or enhancement.
     */
    public void setIntercepting(boolean intercepting) {
        _intercepting = intercepting;
    }

    /**
     * Whether the type is a managed interface.
     */
    public boolean isManagedInterface() {
        if (!_type.isInterface())
            return false;
        return _interface == null ? false : _interface.booleanValue();
    }

    /**
     * Whether the type is a managed interface
     */
    public void setManagedInterface(boolean managedInterface) {
        if (!_type.isInterface())
            throw new MetaDataException(_loc.get("not-interface", _type));
        _interface = managedInterface ? Boolean.TRUE : Boolean.FALSE;

        // managed interfaces always do proper interception; OpenJPA generates
        // the implementations.
        if (isManagedInterface())
            setIntercepting(true);

        // managed interfaces always use property access.
        setAccessType(AccessCode.PROPERTY);
    }

    /**
     * Return the managed interface implementor if any.
     */
    public Class<?> getInterfaceImpl() {
        return _impl;
    }

    /**
     * Set the managed interface implementor class.
     */
    public void setInterfaceImpl(Class<?> impl) {
        _impl = impl;
    }

    /**
     * Return all explicitly declared interfaces this class implements.
     */
    public Class<?>[] getDeclaredInterfaces() {
        if (_interfaces == null)
            return MetaDataRepository.EMPTY_CLASSES;
        return (Class[]) _interfaces.toArray(new Class[_interfaces.size()]);
    }

    /**
     * Explicitly declare the given interface among the ones this
     * class implements.
     */
    public void addDeclaredInterface(Class<?> iface) {
        if (iface == null || !iface.isInterface())
            throw new MetaDataException(_loc.get("declare-non-interface",
                this, iface));
        if (_interfaces == null)
            _interfaces = new ArrayList<Class<?>>();
        _interfaces.add(iface);
    }

    /**
     * Remove the given interface from the declared list.
     */
    public boolean removeDeclaredInterface(Class<?> iface) {
        if (_interfaces == null)
            return false;
        return _interfaces.remove(iface);
    }

    /**
     * Alias properties from the given interface during  queries to
     * the local field.
     */
    public void setInterfacePropertyAlias(Class<?> iface, String orig, 
        String local) {
        synchronized (_ifaceMap) {
            Map<String,String> fields = _ifaceMap.get(iface);
            if (fields == null) {
                fields = new HashMap<String,String>();
                _ifaceMap.put(iface, fields);
            }
            if (fields.containsKey(orig))
                throw new MetaDataException(_loc.get("duplicate-iface-alias", 
                    this, orig, local));
            fields.put(orig, local);
        }
    }
    
    /**
     * Get local field alias for the given interface property.
     */
    public String getInterfacePropertyAlias(Class<?> iface, String orig) {
        synchronized (_ifaceMap) {
            Map<String,String> fields = _ifaceMap.get(iface);
            if (fields == null)
                return null;
            return fields.get(orig);
        }
    }
    
    /**
     * Return all aliases property named for the given interface.
     */
    public String[] getInterfaceAliasedProperties(Class<?> iface) {
        synchronized (_ifaceMap) {
            Map<String,String> fields = _ifaceMap.get(iface);
            if (fields == null)
                return EMPTY_STRING_ARRAY;
            return fields.keySet().toArray(new String[fields.size()]);
        }
    }
    
    /**
     * Return the number of fields that use impl or intermediate data, in
     * order to create a compacted array for storage of said data.
     */
    public int getExtraFieldDataLength() {
        int[] table = getExtraFieldDataTable();
        for (int i = table.length - 1; i >= 0; i--)
            if (table[i] != -1)
                return table[i] + 1;
        return 0;
    }

    /**
     * Return the intermediate field data index of the given field
     * in the compacted array, or -1 if the field does not use extra data.
     *
     * @see #getExtraFieldDataLength
     */
    public int getExtraFieldDataIndex(int field) {
        int[] array = getExtraFieldDataTable();
        if (field < 0 || field >= array.length)
            return -1;
        return array[field];
    }

    /**
     * Creates a table mapping each field index to its extra data index.
     */
    private int[] getExtraFieldDataTable() {
        if (_fieldDataTable == null) {
            FieldMetaData[] fmds = getFields();
            int[] table = new int[fmds.length];
            int idx = 0;
            for (int i = 0; i < fmds.length; i++) {
                if (fmds[i].usesIntermediate()
                    || fmds[i].usesImplData() != Boolean.FALSE)
                    table[i] = idx++;
                else
                    table[i] = -1;
            }
            _fieldDataTable = table;
        }
        return _fieldDataTable;
    }

    /**
     * Return whether the given name represents a managed or static field of
     * this class, including superclass fields.
     */
    public boolean isAccessibleField(String field) {
        if (getDeclaredField(field) != null)
            return true;
        if (_staticFields == null) {
            Field[] fields = (Field[]) AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredFieldsAction(_type)); 
            Set<String> names = new HashSet<String>();
            for (int i = 0; i < fields.length; i++)
                if (Modifier.isStatic(fields[i].getModifiers()))
                    names.add(fields[i].getName());
            _staticFields = names;
        }
        if (_staticFields.contains(field))
            return true;
        if (_super != null)
            return getPCSuperclassMetaData().isAccessibleField(field);
        return false;
    }

    /**
     * Return all fields that are types that need to be wrappered by a proxy.
     * The types that need to be proxied are:
     * <p>
     *  <li>org.apache.openjpa.meta.JavaTypes.CALENDAR
     *  <li>org.apache.openjpa.meta.JavaTypes.COLLECTION
     *  <li>org.apache.openjpa.meta.JavaTypes.DATE
     *  <li>org.apache.openjpa.meta.JavaTypes.MAP
     *  <li>org.apache.openjpa.meta.JavaTypes.OBJECT
     */
    public FieldMetaData[] getProxyFields() {
        if (_allProxyFields == null) {
            // Make sure _allFields has been initialized
            if (_allFields == null) {
                getFields();
            }
            List<FieldMetaData> res = new ArrayList<FieldMetaData>();
            for (FieldMetaData fmd : _allFields) {
                switch (fmd.getDeclaredTypeCode()) {
                    case JavaTypes.CALENDAR:
                    case JavaTypes.COLLECTION:
                    case JavaTypes.DATE:
                    case JavaTypes.MAP:
                    case JavaTypes.OBJECT:
                        res.add(fmd);
                        break;
                }
            }
            _allProxyFields = res.toArray(new FieldMetaData[res.size()]);
        }
        return _allProxyFields;
    }
    
    /**
     * Return all large result set fields. Will never return null.
     */
    public FieldMetaData[] getLrsFields() {
        if (_allLrsFields == null) {
            // Make sure _allFields has been initialized
            if (_allFields == null) {
                getFields();
            }
            List<FieldMetaData> res = new ArrayList<FieldMetaData>();
            for (FieldMetaData fmd : _allFields) {
                if(fmd.isLRS()==true){
                    res.add(fmd);
                }
            }
            _allLrsFields = res.toArray(new FieldMetaData[res.size()]);
        }
        return _allLrsFields;
    }
    
    /**
     * Return all field metadata, including superclass fields.
     */
    public FieldMetaData[] getFields() {
        if (_allFields == null) {
            if (_super == null)
                _allFields = getDeclaredFields();
            else {
                FieldMetaData[] fields = getDeclaredFields();
                FieldMetaData[] supFields = getPCSuperclassMetaData().
                    getFields();

                FieldMetaData[] allFields = _repos.newFieldMetaDataArray
                    (fields.length + supFields.length);
                System.arraycopy(supFields, 0, allFields, 0, supFields.length);
                replaceDefinedSuperclassFields(allFields, supFields.length);

                for (int i = 0; i < fields.length; i++) {
                    fields[i].setIndex(supFields.length + i);
                    allFields[supFields.length + i] = fields[i];
                }
                _allFields = allFields;
            }
        }
        return _allFields;
    }

    /**
     * Replace superclass fields that we define with our version.
     */
    private void replaceDefinedSuperclassFields(FieldMetaData[] fields,
        int len) {
        if (_supFieldMap == null || !_defSupFields)
            return;

        // don't assume fields are in order; this method is used for
        // listing order as well
        FieldMetaData supField;
        for (int i = 0; i < len; i++) {
            supField = (FieldMetaData) _supFieldMap.get(fields[i].getName());
            if (supField != null) {
                fields[i] = supField;
                supField.setIndex(i);
            }
        }
    }

    /**
     * Return the superclass copy of the given field.
     */
    protected FieldMetaData getSuperclassField(FieldMetaData supField) {
        ClassMetaData sm = getPCSuperclassMetaData();
        FieldMetaData fmd = sm == null ? null : sm.getField(supField.getName());
        if (fmd == null 
        || fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            throw new MetaDataException(_loc.get("unmanaged-sup-field",
                supField, this));
        return fmd;
    }

    /**
     * Return only the fields for this class, without superclass fields.
     */
    public FieldMetaData[] getDeclaredFields() {
        if (_fields == null) {
            List<FieldMetaData> fields = 
            	new ArrayList<FieldMetaData>(_fieldMap.size());
            ;
            for (FieldMetaData fmd : _fieldMap.values()) {
                if (fmd.getManagement() != FieldMetaData.MANAGE_NONE) {
                    fmd.setDeclaredIndex(fields.size());
                    fmd.setIndex(fmd.getDeclaredIndex());
                    fields.add(fmd);
                }
            }
            _fields = fields.toArray
            	(_repos.newFieldMetaDataArray(fields.size()));
        }
        return _fields;
    }

    /**
     * Return primary key fields, or empty array if none. The order
     * in which the keys are returned will be the order in which
     * the fields are declared, starting at the least-derived superclass
     * and ending with the primary key fields of the most-derived subclass.
     */
    public FieldMetaData[] getPrimaryKeyFields() {
        // check for primary key fields even if not set to ID_APPLICATION so 
        // that Application Id tool sees them even when user doesn't declare 
    	// Application identity
        if (_allPKFields == null) {
            FieldMetaData[] fields = getFields();
            int num = 0;
            for (int i = 0; i < fields.length; i++)
                if (fields[i].isPrimaryKey())
                    num++;

            if (num == 0)
                _allPKFields = _repos.EMPTY_FIELDS;
            else {
                FieldMetaData[] pks = _repos.newFieldMetaDataArray(num);
                num = 0;
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].isPrimaryKey()) {
                        fields[i].setPrimaryKeyIndex(num);
                        pks[num] = fields[i];
                        num++;
                    }
                }
                _allPKFields = pks;
            }
        }
        return _allPKFields;
    }

    /**
     * Return the list of fields in the default fetch group,
     * including superclass fields, or an empty array if none.
     */
    public FieldMetaData[] getDefaultFetchGroupFields() {
        if (_allDFGFields == null) {
            FieldMetaData[] fields = getFields();
            int num = 0;
            for (int i = 0; i < fields.length; i++)
                if (fields[i].isInDefaultFetchGroup())
                    num++;

            FieldMetaData[] dfgs = _repos.newFieldMetaDataArray(num);
            num = 0;
            for (int i = 0; i < fields.length; i++)
                if (fields[i].isInDefaultFetchGroup())
                    dfgs[num++] = fields[i];
            _allDFGFields = dfgs;
        }
        return _allDFGFields;
    }

    /**
     * Return the version field for this class, if any.
     */
    public FieldMetaData getVersionField() {
        if (_allFields == null) {
            getFields();
        }
        if (_versionIdx == Integer.MIN_VALUE) {
            int idx = -1;
            for (int i = 0; i < _allFields.length; i++) {
                if (_allFields[i].isVersion()) {
                    if (idx != -1)
                        throw new MetaDataException(_loc.get("mult-vers-fields", this, _allFields[idx], _allFields[i]));
                    idx = i;
                }
            }
            _versionIdx = idx;
        }
        if (_versionIdx == -1)
            return null;

        return _allFields[_versionIdx];
    }

    /**
     * Return the metadata for the persistent or transactional field with
     * the given absolute index.
     *
     * @return the field's metadata, or null if not found
     */
    public FieldMetaData getField(int index) {
        if(_allFields == null){
            getFields();
        }
        if (index < 0 || index >= _allFields.length)
            return null;
        return _allFields[index];
    }

    /**
     * Return the metadata for the persistent or transactional field with
     * the given relative index.
     *
     * @return the field's metadata, or null if not found
     */
    public FieldMetaData getDeclaredField(int index) {
        FieldMetaData[] fields = getDeclaredFields();
        if (index < 0 || index >= fields.length)
            return null;
        return fields[index];
    }

    /**
     * Return the metadata for the persistent or transactional field with
     * the given name.
     *
     * @return the field's metadata, or null if not found
     */
    public FieldMetaData getField(String name) {
        FieldMetaData fmd = getDeclaredField(name);
        if (fmd != null)
            return fmd;
        if (_supFieldMap != null && _defSupFields) {
            fmd = (FieldMetaData) _supFieldMap.get(name);
            if (fmd != null)
                return fmd;
        }
        if (_super != null)
            return getPCSuperclassMetaData().getField(name);
        return null;
    }

    /**
     * Return the metadata for the persistent or transactional field with
     * the given name, without including superclass fields.
     *
     * @return the field's metadata, or null if not found
     */
    public FieldMetaData getDeclaredField(String name) {
        FieldMetaData field = (FieldMetaData) _fieldMap.get(name);
        if (field == null || field.getManagement() == FieldMetaData.MANAGE_NONE)
            return null;
        return field;
    }

    /**
     * Return any fields that were added as non-managed.
     * All other methods to get fields return only those that are managed.
     */
    public FieldMetaData[] getDeclaredUnmanagedFields() {
        if (_unmgdFields == null) {
            List<FieldMetaData> unmanaged = new ArrayList<FieldMetaData>(3);
            ;
            for (FieldMetaData field : _fieldMap.values()) {
                if (field.getManagement() == FieldMetaData.MANAGE_NONE)
                    unmanaged.add(field);
            }
            _unmgdFields = unmanaged.toArray
                (_repos.newFieldMetaDataArray(unmanaged.size()));
        }
        return _unmgdFields;
    }

    /**
     * Add a new field metadata to this class.
     */
    public FieldMetaData addDeclaredField(String name, Class<?> type) {
        FieldMetaData fmd = _repos.newFieldMetaData(name, type, this);
        clearFieldCache();
        _fieldMap.put(name, fmd);

        return fmd;
    }

    /**
     * Remove the given field from management.
     *
     * @return true if the field was removed, false otherwise
     */
    public boolean removeDeclaredField(FieldMetaData field) {
        if (field != null && _fieldMap.remove(field.getName()) != null) {
            clearFieldCache();
            return true;
        }
        return false;
    }

    /**
     * Return the defined superclass field with the given name, or null if none.
     */
    public FieldMetaData getDefinedSuperclassField(String name) {
        if (_supFieldMap == null)
            return null;
        return (FieldMetaData) _supFieldMap.get(name);
    }

    /**
     * Add a new defined superclass field metadata to this class.
     */
    public FieldMetaData addDefinedSuperclassField(String name, Class<?> type,
        Class<?> sup) {
        FieldMetaData fmd = _repos.newFieldMetaData(name, type, this);
        fmd.setDeclaringType(sup);
        clearAllFieldCache();
        _defSupFields = false;
        if (_supFieldMap == null)
            _supFieldMap = new HashMap<String,FieldMetaData>();
        _supFieldMap.put(name, fmd);
        return fmd;
    }

    /**
     * Remove the given field from management.
     *
     * @return true if the field was removed, false otherwise
     */
    public boolean removeDefinedSuperclassField(FieldMetaData field) {
        if (field != null && _supFieldMap != null
            && _supFieldMap.remove(field.getName()) != null) {
            clearAllFieldCache();
            _defSupFields = false;
            return true;
        }
        return false;
    }

    /**
     * Incorporate superclass fields redefined in this subclass into this
     * metadata. This method is generally called after metadata is resolved
     * and mapping information is loaded, but before mapping resolve.
     *
     * @param force whether to force re-mapping of even mapped superclass fields
     */
    public void defineSuperclassFields(boolean force) {
        if (_defSupFields)
            return;

        ClassMetaData sup = getPCSuperclassMetaData();
        if (isMapped() && sup != null) {
            // redefine all unmapped superclass fields
            FieldMetaData[] sups = sup.getFields();
            for (int i = 0; i < sups.length; i++) {
                if ((force || !sups[i].getDefiningMetaData().isMapped())
                    && getDefinedSuperclassField(sups[i].getName()) == null) {
                    addDefinedSuperclassField(sups[i].getName(),
                        sups[i].getDeclaredType(), sups[i].getDeclaringType());
                }
            }
        }
        resolveDefinedSuperclassFields();

        // this ensures that all field indexes get set when fields are cached.
        // I don't like doing this twice (it's also done in resolveMeta), but
        // we have to re-cache in case this class or any superclass replaced
        // some fields with redefined versions, and I don't want outside code
        // to have to call this method after resolve just to get field indexes,
        // etc set correctly
        clearAllFieldCache();
        cacheFields();
    }

    /**
     * Resolve superclass fields we've redefined.
     */
    private void resolveDefinedSuperclassFields() {
        _defSupFields = true;
        if (_supFieldMap == null)
            return;

        FieldMetaData sup;
        for (FieldMetaData fmd : _supFieldMap.values()) {
            sup = getSuperclassField(fmd);

            // JPA metadata doesn't qualify superclass field names, so we
            // might not know the declaring type until now
            if (fmd.getDeclaringType() == Object.class) {
                fmd.setDeclaringType(sup.getDeclaringType());
                fmd.backingMember(getRepository().getMetaDataFactory().
                    getDefaults().getBackingMember(fmd));
            }
            fmd.copy(sup);
            fmd.resolve(MODE_META);
        }
    }

    /**
     * Returns an array of all the fields defined by this class.
     * This includes mapped declared fields and any concrete mapping of
     * unmapped superclass fields performed by this class.
     */
    public FieldMetaData[] getDefinedFields() {
        if (_definedFields == null) {
            FieldMetaData[] fields = getFields();
            List<FieldMetaData> defined = 
            	new ArrayList<FieldMetaData>(fields.length);
            for (FieldMetaData fmd : fields) {
                if (fmd.isMapped()
                    && fmd.getDefiningMetaData() == this)
                    defined.add(fmd);
            }
            _definedFields = (FieldMetaData[]) defined.toArray
                (_repos.newFieldMetaDataArray(defined.size()));
        }
        return _definedFields;
    }

    /**
     * Returns all fields in the order they are listed in the metadata
     * file. Unlisted fields are placed after listed ones.
     */
    public FieldMetaData[] getFieldsInListingOrder() {
        if (_allListingFields == null) {
            // combine declared and unmanaged fields into listing order array
            FieldMetaData[] dec = getDeclaredFields();
            FieldMetaData[] unmgd = getDeclaredUnmanagedFields();
            FieldMetaData[] decListing = _repos.newFieldMetaDataArray
                (dec.length + unmgd.length);
            System.arraycopy(dec, 0, decListing, 0, dec.length);
            System.arraycopy(unmgd, 0, decListing, dec.length, unmgd.length);
            Arrays.sort(decListing, ListingOrderComparator.getInstance());

            if (_super == null)
                _allListingFields = decListing;
            else {
                // place superclass fields in listing order before our
                // listing-order declared fields
                FieldMetaData[] sup = getPCSuperclassMetaData().
                    getFieldsInListingOrder();
                FieldMetaData[] listing = _repos.newFieldMetaDataArray
                    (sup.length + decListing.length);
                System.arraycopy(sup, 0, listing, 0, sup.length);
                replaceDefinedSuperclassFields(listing, sup.length);
                System.arraycopy(decListing, 0, listing, sup.length,
                    decListing.length);
                _allListingFields = listing;
            }
        }
        return _allListingFields;
    }

    /**
     * Returns all fields defined by this class in the order they are listed
     * in the metadata file. Unlisted fields are placed after listed ones.
     * This array includes declared transactional and unmanaged fields.
     */
    public FieldMetaData[] getDefinedFieldsInListingOrder() {
        if (_listingFields == null) {
            FieldMetaData[] fields = getFields();
            List<FieldMetaData> defined = 
            	new ArrayList<FieldMetaData>(fields.length);
            for (FieldMetaData fmd : fields)
                if (fmd.getDefiningMetaData() == this)
                    defined.add(fmd);
            FieldMetaData[] unmgd = getDeclaredUnmanagedFields();
            FieldMetaData[] listing = _repos.newFieldMetaDataArray
                (defined.size() + unmgd.length);
            for (int i = 0; i < defined.size(); i++)
                listing[i] = (FieldMetaData) defined.get(i);
            System.arraycopy(unmgd, 0, listing, defined.size(), unmgd.length);
            Arrays.sort(listing, ListingOrderComparator.getInstance());
            _listingFields = listing;
        }
        return _listingFields;
    }

    /**
     * The name of the data cache that stores the managed instance of this class, by default.
     * This can be overwritten by per-instance basis {@linkplain CacheDistributionPolicy cache distribution policy}. 
     * 
     * @return null if this class is disabled from cache by @DataCache(enabled=false).
     *         {@linkplain DataCache#NAME_DEFAULT default} if @DataCache(enabled=true) without a name.
     *         Otherwise, data cache name set by the user via @DataCache name attribute. 
     * 
     */
    public String getDataCacheName() {
        if (DEFAULT_STRING.equals(_cacheName)) {
            if (_super != null && StringUtils.isNotEmpty(getPCSuperclassMetaData().getDataCacheName())) {
                _cacheName = getPCSuperclassMetaData().getDataCacheName();
            } else {
                _cacheName = DataCache.NAME_DEFAULT;
            }
        }
        return _cacheName;
    }
    
    /**
     * Set the cache name for this class. 
     * 
     * @param can be null to disable cache.
     */
    public void setDataCacheName(String name) {
        _cacheName = name;
        if (name != null)
            _dataCacheEnabled = true;
    }
    
    /**
     * Affirms true if this receiver is annotated with @DataCache and is not disabled. 
     * A separate state variable is necessary besides the name of the cache defaulted to a special string.
     */
    public boolean getDataCacheEnabled() {
        return _dataCacheEnabled;
    }

    /**
     * The cache timeout for this class. -1 indicates no timeout.
     */
    public int getDataCacheTimeout() {
        if (_cacheTimeout == Integer.MIN_VALUE) {
            if (_super != null)
                _cacheTimeout = getPCSuperclassMetaData().
                    getDataCacheTimeout();
            else
                _cacheTimeout = _repos.getConfiguration().
                    getDataCacheTimeout();
        }
        return _cacheTimeout;
    }

    /**
     * The cache timeout for this class. -1 indicates no timeout.
     */
    public void setDataCacheTimeout(int timeout) {
        _cacheTimeout = timeout;
    }

    /**
     * Return the data cache for this class, or null if it is not cachable.
     */
    public DataCache getDataCache() {
        String name = getDataCacheName();
        if (name == null) {
            return null;
        }
        return _repos.getConfiguration().getDataCacheManagerInstance().getDataCache(name, true);
    }

    /**
     * Whether instances are detachable.
     */
    public boolean isDetachable() {
        if (_detachable == null) {
            if (_super != null)
                _detachable = (getPCSuperclassMetaData().isDetachable())
                    ? Boolean.TRUE : Boolean.FALSE;
            else
                _detachable = Boolean.FALSE;
        }
        return _detachable.booleanValue();
    }

    /**
     * Whether instances are detachable.
     */
    public void setDetachable(boolean detachable) {
        _detachable = (detachable) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * The name of the detach state field, or null if none.
     */
    public String getDetachedState() {
        if (DEFAULT_STRING.equals(_detachState)) {
            ClassMetaData sup = getPCSuperclassMetaData();
            if (sup != null && sup.isDetachable() == isDetachable())
                _detachState = sup.getDetachedState();
            else {
                Boolean use = usesDetachedState(SYNTHETIC, true);
                _detachState = (Boolean.FALSE.equals(use)) ? null : SYNTHETIC;
            }
        }
        return _detachState;
    }

    /**
     * The name of the detach state field, or null if none.
     */
    public void setDetachedState(String field) {
        _detachState = field;
    }

    /**
     * Return the detach state field, or null if none.
     */
    public Field getDetachedStateField() {
        // no caching; only used at enhancement
        String fieldName = getDetachedState();
        if (fieldName == null || SYNTHETIC.equals(fieldName))
            return null;

        Field f = Reflection.findField(_type, fieldName, false);
        if (f != null)
            return f;
        else
            throw new MetaDataException(
                _loc.get("no-detach-state", fieldName, _type));
    }

    /**
     * Whether an instance of this type has detached state.
     *
     * @return true if a detached instance must have detached state, false
     * if it does not, and null if it may use a
     * manually-constructed instance without detached state
     */
    public Boolean usesDetachedState() {
        // no need to let conf disallow because it's taken into account in
        // getDetachedState() call
        return usesDetachedState(getDetachedState(), false);
    }

    /**
     * Whether an instance of this type has detached state, assuming the given
     * detached state field.
     *
     * @return true if a detached instance must have detached state, false
     * if it does not, and null if it may use a
     * manually-constructed instance without detached state
     */
    private Boolean usesDetachedState(String detachedField,
        boolean confDisallows) {
        if (!isDetachable())
            return Boolean.FALSE;

        // if we declare a detached state field, have to use it
        if (detachedField == null)
            return Boolean.FALSE;
        if (!SYNTHETIC.equals(detachedField))
            return Boolean.TRUE;

        // allow conf to disallow
        if (confDisallows && !_repos.getConfiguration().
            getDetachStateInstance().getDetachedStateField())
            return Boolean.FALSE;

        // have to use detached state to store datastore id
        if (getIdentityType() == ID_DATASTORE)
            return Boolean.TRUE;

        // allow detached state use, but don't require
        return null;
    }

    /**
     * Clear cached field data.
     */
    protected void clearAllFieldCache() {
        _allFields = null;
        _allDFGFields = null;
        _allPKFields = null;
        _allProxyFields = null;
        _allLrsFields = null;
        _definedFields = null;
        _listingFields = null;
        _allListingFields = null;
        _fieldDataTable = null;
    }

    /**
     * Clear defined field data.
     */
    protected void clearDefinedFieldCache() {
        _definedFields = null;
        _listingFields = null;
    }

    /**
     * Clear cached field data.
     */
    protected void clearFieldCache() {
        clearAllFieldCache();
        _fields = null;
        _unmgdFields = null;
        _versionIdx = Integer.MIN_VALUE;
    }

    /**
     * Clear cached subclass data.
     */
    protected void clearSubclassCache() {
        _subs = null;
        _subMetas = null;
        _mapSubMetas = null;
    }

    /**
     * Clear impl data and intermediate data table.
     */
    void clearExtraFieldDataTable() {
        _fieldDataTable = null;
    }

    /**
     * Cache field arrays.
     */
    private void cacheFields() {
        getFields();
        getPrimaryKeyFields();
    }

    public int hashCode() {
        return _hashCode;
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof ClassMetaData))
            return false;
        return _type == ((ClassMetaData) other).getDescribedType();
    }

    public int compareTo(ClassMetaData other) {
        if (other == this)
            return 0;
        return _type.getName().compareTo(((ClassMetaData) other).
            getDescribedType().getName());
    }

    public String toString() {
        return getDescribedType().getName();
    }

    ////////////////////////
    // Resolve and validate
    ////////////////////////

    /**
     * The resolve mode for this metadata.
     */
    public int getResolve() {
        return _resMode;
    }

    /**
     * The resolve mode for this metadata.
     */
    public void setResolve(int mode) {
        _resMode = mode;
    }

    /**
     * The resolve mode for this metadata.
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

        int val = _repos.getValidate();
        boolean runtime = (val & MetaDataRepository.VALIDATE_RUNTIME) != 0;
        boolean validate =
            !ImplHelper.isManagedType(getRepository().getConfiguration(), _type)
            || (val & MetaDataRepository.VALIDATE_UNENHANCED) == 0;

        // we only do any actions for metadata mode
        if ((mode & MODE_META) != 0 && (cur & MODE_META) == 0) {
            resolveMeta(runtime);
            if (validate && (val & MetaDataRepository.VALIDATE_META) != 0)
                validateMeta(runtime);
        }
        if ((mode & MODE_MAPPING) != 0 && (cur & MODE_MAPPING) == 0) {
            resolveMapping(runtime);
            if (validate && (val & MetaDataRepository.VALIDATE_MAPPING) != 0)
                validateMapping(runtime);
        }
        if ((mode & MODE_MAPPING_INIT) != 0 && (cur & MODE_MAPPING_INIT) == 0)
            initializeMapping();
        return false;
    }

    /**
     * Resolve metadata.
     */
    protected void resolveMeta(boolean runtime) {
        boolean embed = _owner != null && _owner.getDeclaredType() == _type;
        Log log = _repos.getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get((embed) ? "resolve-embed-meta" : "resolve-meta",
                this + "@" + System.identityHashCode(this)));

        if (runtime && !_type.isInterface() &&
            !ImplHelper.isManagedType(getRepository().getConfiguration(),_type))
            throw new MetaDataException(_loc.get("not-enhanced", _type));

        // are we the target of an embedded value?
        if (embed) {
            if (recursiveEmbed(_owner)) {
                throw new MetaDataException(_loc.get("recurse-embed", _owner));
            }

            // copy info from the "real" metadata for this type
            ClassMetaData meta = _repos.getMetaData(_type, _loader, true);
            meta.resolve(MODE_META);
            copy(this, meta);
            _embedded = Boolean.FALSE; // embedded instance isn't embedded-only
        }

        // make sure superclass is resolved
        ClassMetaData sup = getPCSuperclassMetaData();
        if (sup != null) {
            sup.resolve(MODE_META);
            if (embed) {
                // embedded instance always redefine all superclass fields
                FieldMetaData[] sups = sup.getFields();
                for (int i = 0; i < sups.length; i++) {
                    if (_supFieldMap == null
                        || !_supFieldMap.containsKey(sups[i].getName())) {
                        addDefinedSuperclassField(sups[i].getName(),
                            sups[i].getDeclaredType(),
                            sups[i].getDeclaringType());
                    }
                }
            }
        }

        // resolve fields and remove invalids
        FieldMetaData fmd;
        for (Iterator<FieldMetaData> itr = _fieldMap.values().iterator(); 
        	itr.hasNext();) {
            // only pass on metadata resolve mode so that metadata is always
            // resolved before any other resolve modes our subclasses pass along
            fmd = itr.next();
            fmd.resolve(MODE_META);

            if (!fmd.isExplicit()
                && (fmd.getDeclaredTypeCode() == JavaTypes.OBJECT
                || fmd.getDeclaredTypeCode() == JavaTypes.PC_UNTYPED
                || (fmd.getDeclaredTypeCode() == JavaTypes.ARRAY
                && fmd.getElement().getDeclaredTypeCode()
                == JavaTypes.OBJECT))) {
                _repos.getLog().warn(_loc.get("rm-field", fmd));
                if (fmd.getListingIndex() != -1)
                    fmd.setManagement(FieldMetaData.MANAGE_NONE);
                else
                    itr.remove();
                clearFieldCache();
            }
        }

        // embedded instances must embed all superclass fields too
        if (embed) {
            clearAllFieldCache();
            resolveDefinedSuperclassFields();
        }

        // this ensures that all field indexes get set when fields are cached
        cacheFields();

        // resolve lifecycle metadata now to prevent lazy threading problems
        _lifeMeta.resolve();

        // record implements in the repository
        if (_interfaces != null) {
            for (Class<?> iface : _interfaces)
                _repos.addDeclaredInterfaceImpl(this, iface);
        }

        // resolve fetch groups
        if (_fgMap != null)
            for (FetchGroup fg : _fgMap.values())
                fg.resolve();

        if (!embed && _type.isInterface()) {
            if (_interface != Boolean.TRUE)
                throw new MetaDataException(_loc.get("interface", _type));

            if (runtime) {
                _impl = _repos.getImplGenerator().createImpl(this);
                _repos.setInterfaceImpl(this, _impl);
            }
        }

        // if this is runtime, create a pc instance and scan it for comparators
        if (runtime && !Modifier.isAbstract(_type.getModifiers())) {
            ProxySetupStateManager sm = new ProxySetupStateManager();
            sm.setProxyData(PCRegistry.newInstance(_type, sm, false), this);
        }
    }

    private boolean recursiveEmbed(ValueMetaData owner) {
        ClassMetaData cm = owner.getFieldMetaData().getDefiningMetaData(); 
        if (cm.getDescribedType().isAssignableFrom(_type))
            return true;
        ValueMetaData owner1 = cm.getEmbeddingMetaData();
        if (owner1 == null)
            return false;
        else 
            return recursiveEmbed(owner1);
    }
    
    /**
     * Validate resolved metadata.
     */
    protected void validateMeta(boolean runtime) {
        validateDataCache();
        validateDetachable();
        validateExtensionKeys();
        validateIdentity();
        validateAccessType();
    }

    /**
     * Resolve mapping data. Logs resolve message and resolves super by default.
     */
    protected void resolveMapping(boolean runtime) {
        Log log = _repos.getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("resolve-mapping", this + "@"
                + System.identityHashCode(this)));

        // make sure superclass is resolved first
        ClassMetaData sup = getPCSuperclassMetaData();
        if (sup != null)
            sup.resolve(MODE_MAPPING);
    }

    /**
     * Validate mapping data.
     */
    protected void validateMapping(boolean runtime) {
    }

    /**
     * Initialize mapping. Logs init message by default.
     */
    protected void initializeMapping() {
        Log log = _repos.getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("init-mapping", this + "@"
                + System.identityHashCode(this)));
    }

    /**
     * Validate data cache settings.
     */
    private void validateDataCache() {
        int timeout = getDataCacheTimeout();
        if (timeout < -1 || timeout == 0)
            throw new MetaDataException(_loc.get("cache-timeout-invalid",
                _type, String.valueOf(timeout)));

        if (_super == null) {
            return;
        }
        String cache = getDataCacheName();
        if (cache == null) {
            return;
        }

        String superCache = getPCSuperclassMetaData().getDataCacheName();
        
        if (!StringUtils.isEmpty(superCache)) {  
            if (!StringUtils.equals(cache, superCache)) {
                throw new MetaDataException(_loc.get("cache-names", new Object[] { _type, cache, _super, superCache }));
            }
        }
    }

    /**
     * Assert that the identity handling for this class is valid.
     */
    private void validateIdentity() {
        // make sure identity types are consistent
        ClassMetaData sup = getPCSuperclassMetaData();
        int id = getIdentityType();
        if (sup != null && sup.getIdentityType() != ID_UNKNOWN
            && sup.getIdentityType() != id)
            throw new MetaDataException(_loc.get("id-types", _type));

        // check for things the data store doesn't support
        Collection<String> opts = _repos.getConfiguration().supportedOptions();
        if (id == ID_APPLICATION
            && !opts.contains(OpenJPAConfiguration.OPTION_ID_APPLICATION)) {
            throw new UnsupportedException(_loc.get("appid-not-supported",
                _type));
        }
        if (id == ID_DATASTORE
            && !opts.contains(OpenJPAConfiguration.OPTION_ID_DATASTORE)) {
            throw new UnsupportedException(_loc.get
                ("datastoreid-not-supported", _type));
        }

        if (id == ID_APPLICATION) {
            if (_idStrategy != ValueStrategies.NONE)
                throw new MetaDataException(_loc.get("appid-strategy", _type));
            validateAppIdClass();
        } else if (id != ID_UNKNOWN)
            validateNoPKFields();

        int strategy = getIdentityStrategy();
        if (strategy == ValueStrategies.SEQUENCE
            && getIdentitySequenceName() == null)
            throw new MetaDataException(_loc.get("no-seq-name", _type));

        ValueStrategies.assertSupported(strategy, this,
            "datastore identity strategy");
    }

    /**
     * Make sure the application identity class is valid.
     */
    private void validateAppIdClass() {
        // base types must declare an oid class if not single-field identity
        FieldMetaData[] pks = getPrimaryKeyFields();
        if (getObjectIdType() == null) {
            if (pks.length == 1)
                throw new MetaDataException(_loc.get("unsupported-id-type",
                    _type, pks[0].getName(),
                    pks[0].getDeclaredType().getName()));
            throw new MetaDataException(_loc.get("no-id-class", _type, 
            		Arrays.asList(toNames(pks))));
        }
        if (_objectId == null)
            return;

        if (isOpenJPAIdentity()) {
            if (pks[0].getDeclaredTypeCode() == JavaTypes.OID) {
                ClassMetaData embed = pks[0].getEmbeddedMetaData();
                validateAppIdClassMethods(embed.getDescribedType());
                validateAppIdClassPKs(embed, embed.getFields(),
                    embed.getDescribedType());
            }
            return;
        }

        if (_super != null) {
            // concrete superclass oid must match or be parent of ours
            ClassMetaData sup = getPCSuperclassMetaData();
            Class<?> objectIdType = sup.getObjectIdType();
            if (objectIdType != null && 
                !objectIdType.isAssignableFrom(_objectId))
                throw new MetaDataException(_loc.get("id-classes",
                    new Object[]{ _type, _objectId, _super,
                        sup.getObjectIdType() }));

            // validate that no other pks are declared if we have a
            // concrete PC superclass
            if (hasConcretePCSuperclass())
                validateNoPKFields();
        }

        // if this class has its own oid class, do some more validation
        if (_super == null
            || _objectId != getPCSuperclassMetaData().getObjectIdType()) {
            // make sure non-abstract oid classes override the proper methods
            if (!Modifier.isAbstract(_objectId.getModifiers()))
                validateAppIdClassMethods(_objectId);

            // make sure the application id class has all primary key fields
            validateAppIdClassPKs(this, pks, _objectId);
        }
    }
    /**
     * Return true if this class uses IdClass derived from idClass of the 
     * parent entity which annotated as id in the child class. 
     * In this case, there are no key fields in the child entity corresponding 
     * to the fields in the IdClass.
     */
    public boolean useIdClassFromParent() {
        if (_useIdClassFromParent == null) {
            if (_objectId == null)
                _useIdClassFromParent = false;
            else {
                FieldMetaData[] pks = getPrimaryKeyFields();
                if (pks.length != 1) 
                    _useIdClassFromParent = false;
                else {
                    ClassMetaData pkMeta = pks[0].getTypeMetaData();
                    if (pkMeta == null)
                        _useIdClassFromParent = false;
                    else {
                        Class<?> pkType = pkMeta.getObjectIdType();
                        if (pkType == ObjectId.class) //parent id is EmbeddedId
                            pkType = pkMeta.getPrimaryKeyFields()[0].getType();
                        if (pkType == _objectId)
                            _useIdClassFromParent = true;
                        else {
                            Field f = Reflection.findField(_objectId, 
                                pks[0].getName(), false);
                            if (f != null) 
                                _useIdClassFromParent = false;
                            else 
                                throw new MetaDataException(_loc.get(
                                        "invalid-id", _type, pks[0].getName()));
                        }
                    }
                }
            }
        }
        return _useIdClassFromParent.booleanValue();
    }

    /**
     * Return true if this class has a concrete persistent superclass.
     */
    private boolean hasConcretePCSuperclass() {
        if (_super == null)
            return false;
        if (!Modifier.isAbstract(_super.getModifiers()) && 
        		(!getPCSuperclassMetaData().isAbstract()))
            return true;
        return getPCSuperclassMetaData().hasConcretePCSuperclass();
    }

    /**
     * Ensure that the user has overridden the equals and hashCode methods,
     * and has the proper constructors.
     */
    private void validateAppIdClassMethods(Class<?> oid) {
        try {
            oid.getConstructor((Class[]) null);
        } catch (Exception e) {
            throw new MetaDataException(_loc.get("null-cons", oid, _type)).
                setCause(e);
        }

        // check for equals and hashcode overrides; don't enforce it
        // for abstract application id classes, since they may not necessarily
        // declare primary key fields
        Method method;
        try {
            method = oid.getMethod("equals", new Class[]{ Object.class });
        } catch (Exception e) {
            throw new GeneralException(e).setFatal(true);
        }

        boolean abs = Modifier.isAbstract(_type.getModifiers());
        if (!abs && method.getDeclaringClass() == Object.class)
            throw new MetaDataException(_loc.get("eq-method", _type));

        try {
            method = oid.getMethod("hashCode", (Class[]) null);
        } catch (Exception e) {
            throw new GeneralException(e).setFatal(true);
        }
        if (!abs && method.getDeclaringClass() == Object.class)
            throw new MetaDataException(_loc.get("hc-method", _type));
    }

    /**
     * Validate that the primary key class has all pk fields.
     */
    private void validateAppIdClassPKs(ClassMetaData meta,
        FieldMetaData[] fmds, Class<?> oid) {
        if (fmds.length == 0 && !Modifier.isAbstract(meta.getDescribedType().
            getModifiers()))
            throw new MetaDataException(_loc.get("no-pk", _type));

        // check that the oid type contains all pk fields
        Field f;
        Method m;
        Class<?> c;
        for (int i = 0; i < fmds.length; i++) {
            switch (fmds[i].getDeclaredTypeCode()) {
                case JavaTypes.ARRAY:
                    c = fmds[i].getDeclaredType().getComponentType();
                    if (c == byte.class || c == Byte.class
                        || c == char.class || c == Character.class) {
                        c = fmds[i].getDeclaredType();
                        break;
                    }
                    // else no break
                case JavaTypes.PC_UNTYPED:
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                case JavaTypes.OID: // we're validating embedded fields
                    throw new MetaDataException(_loc.get("bad-pk-type",
                        fmds[i]));
                default:
                    c = fmds[i].getObjectIdFieldType();
            }

            if (AccessCode.isField(fmds[i].getAccessType())) {
                f = Reflection.findField(oid, fmds[i].getName(), false);
                if (f == null || !f.getType().isAssignableFrom(c))
                    throw new MetaDataException(_loc.get("invalid-id",
                        _type, fmds[i].getName()));
            } else if (AccessCode.isProperty(fmds[i].getAccessType())) {
                m = Reflection.findGetter(oid, fmds[i].getName(), false);
                if (m == null || !m.getReturnType().isAssignableFrom(c))
                    throw new MetaDataException(_loc.get("invalid-id",
                        _type, fmds[i].getName()));
                m = Reflection.findSetter(oid, fmds[i].getName(),
                    fmds[i].getObjectIdFieldType(), false);
                if (m == null || m.getReturnType() != void.class)
                    throw new MetaDataException(_loc.get("invalid-id",
                        _type, fmds[i].getName()));
            }
        }
    }

    /**
     * Validate that this class doesn't declare any primary key fields.
     */
    private void validateNoPKFields() {
        FieldMetaData[] fields = getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
            if (fields[i].isPrimaryKey())
                throw new MetaDataException(_loc.get("bad-pk", fields[i]));
    }

    /**
     * Assert that this class' access type is allowed.
     * If no access style is set or an explicit style is set return.
     * Otherwise, if the superclass has persistent attributes,  check that 
     * the superclass access style, if defaulted, is the same as that of this 
     * receiver. 
     */
    private void validateAccessType() {
        if (AccessCode.isEmpty(_accessType) 
           || AccessCode.isExplicit(_accessType))
            return;
        ClassMetaData sup = getPCSuperclassMetaData();
        while (sup != null && sup.isExplicitAccess())
        	sup = sup.getPCSuperclassMetaData();
        if (sup != null && sup.getDeclaredFields().length > 0) {
        	int supCode = sup.getAccessType();
        	if (!AccessCode.isCompatibleSuper(_accessType, supCode))
             throw new MetaDataException(_loc.get("access-inconsistent-inherit",
             new Object[]{this, AccessCode.toClassString(_accessType), 
                          sup, AccessCode.toClassString(supCode)}).toString());
        }
    }

    /**
     * Assert that detachment configuration is valid.
     */
    private void validateDetachable() {
        boolean first = true;
        for (ClassMetaData parent = getPCSuperclassMetaData();
            first && parent != null; parent = parent.getPCSuperclassMetaData())
        {
            if (parent.isDetachable())
                first = false;
        }

        Field field = getDetachedStateField();
        if (field != null) {
            if (!first)
                throw new MetaDataException(_loc.get("parent-detach-state",
                    _type));
            if (getField(field.getName()) != null)
                throw new MetaDataException(_loc.get("managed-detach-state",
                    field.getName(), _type));
            if (field.getType() != Object.class)
                throw new MetaDataException(_loc.get("bad-detach-state",
                    field.getName(), _type));
        }
    }

    ///////////////
    // Fetch Group
    ///////////////

    /**
     * Return the fetch groups declared explicitly in this type.
     */
    public FetchGroup[] getDeclaredFetchGroups() {
        if (_fgs == null) {
            _fgs = (_fgMap == null) ? EMPTY_FETCH_GROUP_ARRAY : _fgMap.values().toArray(new FetchGroup[_fgMap.size()]); 
        }
        return _fgs;
    }

    /**
     * Return all fetch groups for this type, including superclass groups but excluding the standard groups
     * such as "default" or "all".
     */
    public FetchGroup[] getCustomFetchGroups() {
        if (_customFGs == null) {
            // map fetch groups to names, allowing our groups to override super
            Map<String,FetchGroup> fgs = new HashMap<String,FetchGroup>();
            ClassMetaData sup = getPCSuperclassMetaData();
            if (sup != null) {
                FetchGroup[] supFGs = sup.getCustomFetchGroups();
                for (int i = 0; i < supFGs.length; i++) {
                    fgs.put(supFGs[i].getName(), supFGs[i]);
                }
            }
            FetchGroup[] decs = getDeclaredFetchGroups();
            for (int i = 0; i < decs.length; i++) {
                fgs.put(decs[i].getName(), decs[i]);
            }
            // remove standard groups
            fgs.remove(FetchGroup.NAME_DEFAULT);
            fgs.remove(FetchGroup.NAME_ALL);

            _customFGs = fgs.values().toArray(new FetchGroup[fgs.size()]);
        }
        return _customFGs;
    }

    /**
     * Gets a named fetch group. If not available in this receiver then looks
     * up the inheritance hierarchy. 
     *
     * @param name name of a fetch group.
     * @return an existing fetch group of the given name if known to this 
     * receiver or any of its superclasses. Otherwise null.
     */
    public FetchGroup getFetchGroup(String name) {
        FetchGroup fg = (_fgMap == null) ? null : _fgMap.get(name);
        if (fg != null)
            return fg;
        ClassMetaData sup = getPCSuperclassMetaData();
        if (sup != null)
            return sup.getFetchGroup(name);
        if (FetchGroup.NAME_DEFAULT.equals(name))
            return FetchGroup.DEFAULT;
        if (FetchGroup.NAME_ALL.equals(name))
            return FetchGroup.ALL;
        return null;
    }

    /**
     * Adds fetch group of the given name, or returns existing instance.
     *
     * @param name a non-null, non-empty name. Must be unique within this
     * receiver's scope. The super class <em>may</em> have a group with
     * the same name.
     */
    public FetchGroup addDeclaredFetchGroup(String name) {
    	if (StringUtils.isEmpty(name))
    		throw new MetaDataException(_loc.get("empty-fg-name", this));
        if (_fgMap == null)
            _fgMap = new HashMap<String,FetchGroup>();
        FetchGroup fg = _fgMap.get(name);
        if (fg == null) {
        	fg = new FetchGroup(this, name);
        	_fgMap.put(name, fg);
            _fgs = null;
            _customFGs = null;
        }
        return fg;
    }

    /**
     * Remove a declared fetch group.
     */
    public boolean removeDeclaredFetchGroup(FetchGroup fg) {
        if (fg == null)
            return false;
        if (_fgMap.remove(fg.getName()) != null) {
            _fgs = null;
            _customFGs = null;
            return true;
        }
        return false;
    }

    /////////////////
    // SourceTracker
    /////////////////

    public File getSourceFile() {
        return _srcFile;
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File file, int srcType, String srcName) {
        _srcFile = file;
        _srcType = srcType;
        _srcName = srcName;
    }

    public String getResourceName() {
        return _type.getName();
    }
    
    public int getLineNumber() {
        return _lineNum;
    }

    public void setLineNumber(int lineNum) {
        _lineNum = lineNum;
    }

    public int getColNumber() {
        return _colNum;
    }

    public void setColNumber(int colNum) {
        _colNum = colNum;
    }
    

    /**
     * The source mode this metadata has been loaded under.
     */
    public int getSourceMode() {
        return _srcMode;
    }

    /**
     * The source mode this metadata has been loaded under.
     */
    public void setSourceMode(int mode) {
        _srcMode = mode;
    }

    /**
     * The source mode this metadata has been loaded under.
     */
    public void setSourceMode(int mode, boolean on) {
        if (mode == MODE_NONE)
            _srcMode = mode;
        else if (on)
            _srcMode |= mode;
        else
            _srcMode &= ~mode;
    }

    /**
     * The index in which this class was listed in the metadata. Defaults to
     * <code>-1</code> if this class was not listed in the metadata.
     */
    public int getListingIndex() {
        return _listIndex;
    }

    /**
     * The index in which this field was listed in the metadata. Defaults to
     * <code>-1</code> if this class was not listed in the metadata.
     */
    public void setListingIndex(int index) {
        _listIndex = index;
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

    //////////////
    // State copy
    //////////////

    /**
     * Copy the metadata from the given instance to this one. Do not
     * copy mapping information.
     */
    public void copy(ClassMetaData meta) {
        if (meta.getDescribedType() != _type)
            throw new InternalException();
        super.copy(meta);

        // copy class-level info; use get methods to force resolution of
        // lazy data
        _super = meta.getPCSuperclass();
        _objectId = meta.getObjectIdType();
        _extent = (meta.getRequiresExtent()) ? Boolean.TRUE : Boolean.FALSE;
        _embedded = (meta.isEmbeddedOnly()) ? Boolean.TRUE : Boolean.FALSE;
        _embeddable = meta._embeddable;
        _interface = (meta.isManagedInterface()) ? Boolean.TRUE : Boolean.FALSE;
        setIntercepting(meta.isIntercepting());
        _abstract = meta.isAbstract();
        _impl = meta.getInterfaceImpl();
        _identity = meta._identity == null ? null : meta.getIdentityType();
        _idStrategy = meta.getIdentityStrategy();
        _seqName = meta.getIdentitySequenceName();
        _seqMeta = null;
        _alias = meta.getTypeAlias();
        _accessType = meta.getAccessType();

        // only copy this information if it wasn't set explicitly for this
        // instance
        if (DEFAULT_STRING.equals(_cacheName))
            _cacheName = meta.getDataCacheName();
        if (_cacheTimeout == Integer.MIN_VALUE)
            _cacheTimeout = meta.getDataCacheTimeout();
        _cacheEnabled = meta.getCacheEnabled();
        _dataCacheEnabled = meta.getDataCacheEnabled();
        if (_detachable == null)
            _detachable = meta._detachable;
        if (DEFAULT_STRING.equals(_detachState))
            _detachState = meta.getDetachedState();

        // synch field information; first remove extra fields
        clearFieldCache();
        _fieldMap.keySet().retainAll(meta._fieldMap.keySet());

        // add copies of declared fields; other defined fields already copied
        FieldMetaData[] fields = meta.getDeclaredFields();
        FieldMetaData field;
        for (int i = 0; i < fields.length; i++) {
            field = getDeclaredField(fields[i].getName());
            if (field == null)
                field = addDeclaredField(fields[i].getName(),
                    fields[i].getDeclaredType());
            field.setDeclaredIndex(-1);
            field.setIndex(-1);
            field.copy(fields[i]);
        }

        // copy fetch groups
        FetchGroup[] fgs = meta.getDeclaredFetchGroups();
        FetchGroup fg;
        for (int i = 0; i < fgs.length; i++) {
            fg = addDeclaredFetchGroup(fgs[i].getName());
            fg.copy(fgs[i]); 
        }

        // copy interface re-mapping
        _ifaceMap.clear();
        _ifaceMap.putAll(meta._ifaceMap);
    }

    /**
     * Recursive helper to copy embedded metadata.
     */
    private static void copy(ClassMetaData embed, ClassMetaData dec) {
        ClassMetaData sup = dec.getPCSuperclassMetaData();
        if (sup != null) {
            embed.setPCSuperclass(sup.getDescribedType());
            copy(embed.getPCSuperclassMetaData(), sup);
        }
        embed.copy(dec);
    }

    protected void addExtensionKeys(Collection exts) {
        _repos.getMetaDataFactory().addClassExtensionKeys(exts);
    }

    /**
     * Comparator used to put field metadata into listing order.
     */
    private static class ListingOrderComparator
        implements Comparator<FieldMetaData> {

        private static final ListingOrderComparator _instance
            = new ListingOrderComparator();

        /**
         * Access singleton instance.
         */
        public static ListingOrderComparator getInstance() {
            return _instance;
        }

        public int compare(FieldMetaData f1, FieldMetaData f2) {
            if (f1 == f2)
                return 0;
            if (f1 == null)
                return 1;
            if (f2 == null)
                return -1;

            if (f1.getListingIndex() == f2.getListingIndex()) {
                if (f1.getIndex() == f2.getIndex())
                    return f1.getFullName(false).compareTo
                        (f2.getFullName(false));
				if (f1.getIndex () == -1)
					return 1;
				if (f2.getIndex () == -1)
					return -1;
				return f1.getIndex () - f2.getIndex ();
			}	
			if (f1.getListingIndex () == -1)
				return 1;
			if (f2.getListingIndex () == -1)
				return -1;
			return f1.getListingIndex () - f2.getListingIndex ();
		}
	}
    
    public void registerForValueUpdate(String...values) {
    	if (values == null)
    		return;
    	for (String key : values) {
    		Value value = getRepository().getConfiguration()
    			.getValue(key);
    		if (value != null)
    			value.addListener(this);
    	}
    }
    
    public void valueChanged(Value val) {
    	if (val != null && val.matches("DataCacheTimeout")) {
    		_cacheTimeout = Integer.MIN_VALUE;
    	}
    }
    
    /**
     * Utility method to get names of all fields including the superclasses'
     * sorted in lexical order.
     */
    public String[] getFieldNames() {
    	return toNames(getFields());
    }
    
    /**
     * Utility method to get names of all declared fields excluding the 
     * superclasses' sorted in lexical order.
     */
    public String[] getDeclaredFieldNames() {
    	return toNames(getDeclaredFields());
    }
    
    String[] toNames(FieldMetaData[] fields) {
    	List<String> result = new ArrayList<String>();
    	for (FieldMetaData fmd : fields) {
    		result.add(fmd.getName());
    	}
    	Collections.sort(result);
    	return result.toArray(new String[result.size()]);
    }
    

    public boolean isAbstract() {
        return _abstract;
    }

    public void setAbstract(boolean flag) {
        _abstract = flag;
    }

    /**
     * Convenience method to determine if the pcType modeled by
     * this ClassMetaData object is both abstract and declares PKFields. This
     * method is used by the PCEnhancer to determine if special handling is
     * required.
     *
     * @return
     */
    public boolean hasAbstractPKField() {
        if (_hasAbstractPKField != null) {
            return _hasAbstractPKField.booleanValue();
        }

        // Default to false, set to true only if this type is abstract and
        // declares a PKField.
        _hasAbstractPKField = Boolean.FALSE;

        if (isAbstract() == true) {
            FieldMetaData[] declaredFields = getDeclaredFields();
            if (declaredFields != null && declaredFields.length != 0) {
                for (FieldMetaData fmd : declaredFields) {
                    if (fmd.isPrimaryKey()) {
                        _hasAbstractPKField = Boolean.TRUE;
                        break;
                    }
                }
            }
        }

        return _hasAbstractPKField.booleanValue();
    }
    
    /**
     * Convenience method to determine if this type is a direct
     * decendent of an abstract type declaring PKFields. Returns true if there
     * are no pcTypes mapped to a table between this type and an abstract pcType
     * declaring PKFields. Returns false if there no such abstract pcTypes in
     * the inheritance hierarchy or if there are any pcTypes mapped to tables in
     * between the type represented by this ClassMetaData object and the
     * abstract pcType declaring PKFields.
     *
     * @return
     */
    public boolean hasPKFieldsFromAbstractClass() {
        if (_hasPKFieldsFromAbstractClass != null) {
            return _hasPKFieldsFromAbstractClass.booleanValue();
        }

        // Default to FALSE, until proven true.
        _hasPKFieldsFromAbstractClass = Boolean.FALSE;

        FieldMetaData[] pkFields = getPrimaryKeyFields();
        for (FieldMetaData fmd : pkFields) {
            ClassMetaData fmdDMDA = fmd.getDeclaringMetaData();
            if (fmdDMDA.isAbstract()) {
                ClassMetaData cmd = getPCSuperclassMetaData();
                while (cmd != fmdDMDA) {
                    if (fmdDMDA.isAbstract()) {
                        cmd = cmd.getPCSuperclassMetaData();
                    } else {
                        break;
                    }
                }
                if (cmd == fmdDMDA) {
                    _hasPKFieldsFromAbstractClass = Boolean.TRUE;
                    break;
                }
            }
        }

        return _hasPKFieldsFromAbstractClass.booleanValue();
    }
    
    /**
     * Sets the eligibility status of this class for cache.
     * 
     */
    public void setCacheEnabled(boolean enabled) { 
        _cacheEnabled = enabled;
    }
    
    /**
     * Returns tri-state status on whether this class has been enabled for caching.
     * 
     * @return TRUE or FALSE denote this class has been explicitly enabled or disabled for caching.
     * If no status has been explicitly set, then the status of the persistent super class, if any, is returned. 
     */
    public Boolean getCacheEnabled() { 
        if (_cacheEnabled != null)
            return _cacheEnabled;
        return getPCSuperclassMetaData() != null ?  getPCSuperclassMetaData().getCacheEnabled() : null; 
    }
    
    public String getSourceName(){
        return _srcName; 
    }
    
    public int[] getPkAndNonPersistentManagedFmdIndexes() {
        if (_pkAndNonPersistentManagedFmdIndexes == null) {
            List<Integer> ids = new ArrayList<Integer>();
            for (FieldMetaData fmd : getFields()) {
                if (fmd.isPrimaryKey() || fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT) {
                    ids.add(fmd.getIndex());
                }
            }
            int idsSize = ids.size();
            _pkAndNonPersistentManagedFmdIndexes = new int[idsSize];
            for(int i = 0; i<idsSize; i++){
                _pkAndNonPersistentManagedFmdIndexes[i] = ids.get(i).intValue();
            }
        }
        return _pkAndNonPersistentManagedFmdIndexes;
    }

    public boolean hasInverseManagedFields() {
        if (inverseManagedFields == null) {
            Boolean res = Boolean.FALSE;
            for (FieldMetaData fmd : getFields()) {
                if (fmd.getInverseMetaDatas().length > 0) {
                    res = Boolean.TRUE;
                    break;
                }
            }
            inverseManagedFields = res;
        }
        return inverseManagedFields.booleanValue();
    }
    
    public List<FieldMetaData> getMappyedByIdFields() {
        if (!_mappedByIdFieldsSet) {
            List<FieldMetaData> fmdArray = null;
            for (FieldMetaData fmd : getFields()) {
                if (getIdentityType() == ClassMetaData.ID_APPLICATION) {
                    String mappedByIdValue = fmd.getMappedByIdValue();
                    if (mappedByIdValue != null) {
                        if (fmdArray == null) {
                            fmdArray = new ArrayList<FieldMetaData>();
                        }
                        fmdArray.add(fmd);
                    }
                }
            }
            _mappedByIdFields = fmdArray;
            _mappedByIdFieldsSet = true;
        }
        return _mappedByIdFields;
    }

    /**
     * Set whether to include schema name in generated class files
     */
    public boolean getUseSchemaElement() {
        return _useSchemaElement;
    }

    /**
     * Get whether to include schema name in generated class files
     */
    public void setUseSchemaElement(boolean useSchemaElement) {
        this._useSchemaElement = useSchemaElement;
    }
}
