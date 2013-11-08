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
package org.apache.openjpa.persistence;

import static javax.persistence.GenerationType.AUTO;
import static org.apache.openjpa.persistence.MetaDataTag.ACCESS;
import static org.apache.openjpa.persistence.MetaDataTag.CACHEABLE;
import static org.apache.openjpa.persistence.MetaDataTag.DATASTORE_ID;
import static org.apache.openjpa.persistence.MetaDataTag.DATA_CACHE;
import static org.apache.openjpa.persistence.MetaDataTag.DEPENDENT;
import static org.apache.openjpa.persistence.MetaDataTag.DETACHED_STATE;
import static org.apache.openjpa.persistence.MetaDataTag.ELEM_DEPENDENT;
import static org.apache.openjpa.persistence.MetaDataTag.ELEM_TYPE;
import static org.apache.openjpa.persistence.MetaDataTag.EMBEDDED_ID;
import static org.apache.openjpa.persistence.MetaDataTag.ENTITY_LISTENERS;
import static org.apache.openjpa.persistence.MetaDataTag.EXCLUDE_DEFAULT_LISTENERS;
import static org.apache.openjpa.persistence.MetaDataTag.EXCLUDE_SUPERCLASS_LISTENERS;
import static org.apache.openjpa.persistence.MetaDataTag.EXTERNALIZER;
import static org.apache.openjpa.persistence.MetaDataTag.EXTERNAL_VALS;
import static org.apache.openjpa.persistence.MetaDataTag.FACTORY;
import static org.apache.openjpa.persistence.MetaDataTag.FETCH_GROUP;
import static org.apache.openjpa.persistence.MetaDataTag.FETCH_GROUPS;
import static org.apache.openjpa.persistence.MetaDataTag.FLUSH_MODE;
import static org.apache.openjpa.persistence.MetaDataTag.GENERATED_VALUE;
import static org.apache.openjpa.persistence.MetaDataTag.ID;
import static org.apache.openjpa.persistence.MetaDataTag.ID_CLASS;
import static org.apache.openjpa.persistence.MetaDataTag.INVERSE_LOGICAL;
import static org.apache.openjpa.persistence.MetaDataTag.KEY_DEPENDENT;
import static org.apache.openjpa.persistence.MetaDataTag.KEY_TYPE;
import static org.apache.openjpa.persistence.MetaDataTag.LOAD_FETCH_GROUP;
import static org.apache.openjpa.persistence.MetaDataTag.LRS;
import static org.apache.openjpa.persistence.MetaDataTag.MANAGED_INTERFACE;
import static org.apache.openjpa.persistence.MetaDataTag.MAPPED_BY_ID;
import static org.apache.openjpa.persistence.MetaDataTag.MAP_KEY;
import static org.apache.openjpa.persistence.MetaDataTag.MAP_KEY_CLASS;
import static org.apache.openjpa.persistence.MetaDataTag.NATIVE_QUERIES;
import static org.apache.openjpa.persistence.MetaDataTag.NATIVE_QUERY;
import static org.apache.openjpa.persistence.MetaDataTag.ORDER_BY;
import static org.apache.openjpa.persistence.MetaDataTag.POST_LOAD;
import static org.apache.openjpa.persistence.MetaDataTag.POST_PERSIST;
import static org.apache.openjpa.persistence.MetaDataTag.POST_REMOVE;
import static org.apache.openjpa.persistence.MetaDataTag.POST_UPDATE;
import static org.apache.openjpa.persistence.MetaDataTag.PRE_PERSIST;
import static org.apache.openjpa.persistence.MetaDataTag.PRE_REMOVE;
import static org.apache.openjpa.persistence.MetaDataTag.PRE_UPDATE;
import static org.apache.openjpa.persistence.MetaDataTag.QUERIES;
import static org.apache.openjpa.persistence.MetaDataTag.QUERY;
import static org.apache.openjpa.persistence.MetaDataTag.READ_ONLY;
import static org.apache.openjpa.persistence.MetaDataTag.SEQ_GENERATOR;
import static org.apache.openjpa.persistence.MetaDataTag.TYPE;
import static org.apache.openjpa.persistence.MetaDataTag.VERSION;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ExcludeDefaultListeners;
import javax.persistence.ExcludeSuperclassListeners;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.LockModeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyClass;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.event.BeanLifecycleCallbacks;
import org.apache.openjpa.event.LifecycleCallbacks;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.MethodLifecycleCallbacks;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.DelegatingMetaDataFactory;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.LifecycleMetaData;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.Order;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.UpdateStrategies;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.UserException;

import serp.util.Strings;

/**
 * Persistence annotation metadata parser. Currently does not parse
 * deployment descriptors.
 *
 * @author Abe White
 * @author Steve Kim
 * @nojavadoc
 */
public class AnnotationPersistenceMetaDataParser
    implements MetaDataModes {

    private static final Localizer _loc = Localizer.forPackage
        (AnnotationPersistenceMetaDataParser.class);

    private static final Map<Class<?>, MetaDataTag> _tags =
        new HashMap<Class<?>, MetaDataTag>();

    static {
        _tags.put(Access.class, ACCESS);
        _tags.put(Cacheable.class, CACHEABLE);
        _tags.put(EmbeddedId.class, EMBEDDED_ID);
        _tags.put(EntityListeners.class, ENTITY_LISTENERS);
        _tags.put(ExcludeDefaultListeners.class, EXCLUDE_DEFAULT_LISTENERS);
        _tags.put(ExcludeSuperclassListeners.class,
            EXCLUDE_SUPERCLASS_LISTENERS);
        _tags.put(FlushModeType.class, FLUSH_MODE);
        _tags.put(GeneratedValue.class, GENERATED_VALUE);
        _tags.put(Id.class, ID);
        _tags.put(IdClass.class, ID_CLASS);
        _tags.put(MapKey.class, MAP_KEY);
        _tags.put(MapKeyClass.class, MAP_KEY_CLASS);
        _tags.put(MapsId.class, MAPPED_BY_ID);
        _tags.put(NamedNativeQueries.class, NATIVE_QUERIES);
        _tags.put(NamedNativeQuery.class, NATIVE_QUERY);
        _tags.put(NamedQueries.class, QUERIES);
        _tags.put(NamedQuery.class, QUERY);
        _tags.put(OrderBy.class, ORDER_BY);
        _tags.put(PostLoad.class, POST_LOAD);
        _tags.put(PostPersist.class, POST_PERSIST);
        _tags.put(PostRemove.class, POST_REMOVE);
        _tags.put(PostUpdate.class, POST_UPDATE);
        _tags.put(PrePersist.class, PRE_PERSIST);
        _tags.put(PreRemove.class, PRE_REMOVE);
        _tags.put(PreUpdate.class, PRE_UPDATE);
        _tags.put(SequenceGenerator.class, SEQ_GENERATOR);
        _tags.put(Version.class, VERSION);
        _tags.put(DataCache.class, DATA_CACHE);
        _tags.put(DataStoreId.class, DATASTORE_ID);
        _tags.put(Dependent.class, DEPENDENT);
        _tags.put(DetachedState.class, DETACHED_STATE);
        _tags.put(ElementDependent.class, ELEM_DEPENDENT);
        _tags.put(ElementType.class, ELEM_TYPE);
        _tags.put(ExternalValues.class, EXTERNAL_VALS);
        _tags.put(Externalizer.class, EXTERNALIZER);
        _tags.put(Factory.class, FACTORY);
        _tags.put(FetchGroup.class, FETCH_GROUP);
        _tags.put(FetchGroups.class, FETCH_GROUPS);
        _tags.put(InverseLogical.class, INVERSE_LOGICAL);
        _tags.put(KeyDependent.class, KEY_DEPENDENT);
        _tags.put(KeyType.class, KEY_TYPE);
        _tags.put(LoadFetchGroup.class, LOAD_FETCH_GROUP);
        _tags.put(LRS.class, LRS);
        _tags.put(ManagedInterface.class, MANAGED_INTERFACE);
        _tags.put(ReadOnly.class, READ_ONLY);
        _tags.put(Type.class, TYPE);
    }

    private final OpenJPAConfiguration _conf;
    private final Log _log;
    private MetaDataRepository _repos = null;
    private ClassLoader _envLoader = null;
    private boolean _override = false;
    private int _mode = MODE_NONE;

    // packages and their parse modes
    private final Map<Package, Integer> _pkgs = new HashMap<Package, Integer>();

    // the class we were invoked to parse
    protected Class<?> _cls = null;
    protected Stack<Class<?>> _stack = new Stack<Class<?>>();
    private File _file = null;

    /**
     * Constructor; supply configuration.
     */
    public AnnotationPersistenceMetaDataParser(OpenJPAConfiguration conf) {
        _conf = conf;
        _log = conf.getLog(OpenJPAConfiguration.LOG_METADATA);
    }

    /**
     * Configuration supplied on construction.
     */
    public OpenJPAConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * Metadata log.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * Returns the repository for this parser. If none has been set,
     * create a new repository and sets it.
     */
    public MetaDataRepository getRepository() {
        if (_repos == null) {
            MetaDataRepository repos = _conf.newMetaDataRepositoryInstance();
            MetaDataFactory mdf = repos.getMetaDataFactory();
            if (mdf instanceof DelegatingMetaDataFactory)
                mdf = ((DelegatingMetaDataFactory) mdf).getInnermostDelegate();
            if (mdf instanceof PersistenceMetaDataFactory)
                ((PersistenceMetaDataFactory) mdf).setAnnotationParser(this);
            _repos = repos;
        }
        return _repos;
    }

    /**
     * Set the metadata repository for this parser.
     */
    public void setRepository(MetaDataRepository repos) {
        _repos = repos;
    }

    /**
     * Return the environmental class loader to pass on to parsed
     * metadata instances.
     */
    public ClassLoader getEnvClassLoader() {
        return _envLoader;
    }

    /**
     * Set the environmental class loader to pass on to parsed
     * metadata instances.
     */
    public void setEnvClassLoader(ClassLoader loader) {
        _envLoader = loader;
    }

    /**
     * Whether to allow later parses of mapping information to override
     * earlier information for the same class. Defaults to false. Useful
     * when a tool is mapping a class, so that annotation partial mapping
     * information can be used even when mappings are stored in another
     * location.
     */
    public boolean getMappingOverride() {
        return _override;
    }

    /**
     * Whether to allow later parses of mapping information to override
     * earlier information for the same class. Defaults to false. Useful
     * when a tool is mapping a class, so that annotation partial mapping
     * information can be used even when mappings are stored in another
     * location.
     */
    public void setMappingOverride(boolean override) {
        _override = override;
    }

    /**
     * The parse mode.
     */
    public int getMode() {
        return _mode;
    }

    /**
     * The parse mode.
     */
    public void setMode(int mode, boolean on) {
        if (mode == MODE_NONE)
            _mode = MODE_NONE;
        else if (on)
            _mode |= mode;
        else
            _mode &= ~mode;
    }

    /**
     * The parse mode.
     */
    public void setMode(int mode) {
        _mode = mode;
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isMetaDataMode() {
        return (_mode & MODE_META) != 0;
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isQueryMode() {
        return (_mode & MODE_QUERY) != 0;
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isMappingMode() {
        return (_mode & MODE_MAPPING) != 0;
    }

    /**
     * Returns true if we're in mapping mode or in metadata mode with
     * mapping overide enabled.
     */
    protected boolean isMappingOverrideMode() {
        return isMappingMode() || (_override && isMetaDataMode());
    }

    /**
     * Clear caches.
     */
    public void clear() {
    	_stack.clear();
        _cls = null;
        _file = null;
        _pkgs.clear();
    }

    /**
     * Parse persistence metadata for the given class.
     */
    public void parse(Class<?> cls) {
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("parse-class", cls.getName()));

        _cls = cls;
        _stack.push(cls);
        try {
            parsePackageAnnotations();
            ClassMetaData meta = parseClassAnnotations();
            updateSourceMode(meta);
        } finally {
        	_stack.pop();
            _cls = _stack.isEmpty() ? null : _stack.peek();
            _file = null;
        }
    }

    /**
     * Update the source mode to the class package and class to indicate that
     * we've fully parsed them.
     */
    private void updateSourceMode(ClassMetaData meta) {
        if (_cls.getPackage() != null)
            addSourceMode(_cls.getPackage(), _mode);
        if (meta != null)
            meta.setSourceMode(_mode, true);
    }

    /**
     * Parse information in package-level class annotations.
     */
    private void parsePackageAnnotations() {
        Package pkg = _cls.getPackage();
        if (pkg == null)
            return;

        int pkgMode = getSourceMode(pkg);
        if (pkgMode == 0 && _log.isTraceEnabled())
            _log.trace(_loc.get("parse-package", _cls.getName()));
        if ((pkgMode & _mode) == _mode) // already visited
            return;

        MetaDataTag tag;
        for (Annotation anno : pkg.getDeclaredAnnotations()) {
            tag = _tags.get(anno.annotationType());
            if (tag == null) {
                handleUnknownPackageAnnotation(pkg, anno);
                continue;
            }

            switch (tag) {
                case NATIVE_QUERIES:
                    if (isQueryMode() && (pkgMode & MODE_QUERY) == 0)
                        parseNamedNativeQueries(pkg,
                            ((NamedNativeQueries) anno).value());
                    break;
                case NATIVE_QUERY:
                    if (isQueryMode() && (pkgMode & MODE_QUERY) == 0)
                        parseNamedNativeQueries(pkg, (NamedNativeQuery) anno);
                    break;
                case QUERIES:
                    if (isQueryMode() && (pkgMode & MODE_QUERY) == 0)
                        parseNamedQueries(pkg, ((NamedQueries) anno).value());
                    break;
                case QUERY:
                    if (isQueryMode() && (pkgMode & MODE_QUERY) == 0)
                        parseNamedQueries(pkg, (NamedQuery) anno);
                    break;
                case SEQ_GENERATOR:
                    if (isMappingOverrideMode() &&
                        (pkgMode & MODE_MAPPING) == 0)
                        parseSequenceGenerator(pkg, (SequenceGenerator) anno);
                    break;
                default:
                    throw new UnsupportedException(_loc.get("unsupported", pkg,
                        anno.toString()));
            }
        }

        // always parse mapping stuff after metadata stuff, in case there are
        // dependencies on metadata
        if (isMappingOverrideMode() && (pkgMode & MODE_MAPPING) == 0)
            parsePackageMappingAnnotations(pkg);
    }

    /**
     * Parse package mapping annotations.
     */
    protected void parsePackageMappingAnnotations(Package pkg) {
    }

    /**
     * Allow subclasses to handle unknown annotations.
     */
    protected boolean handleUnknownPackageAnnotation(Package pkg,
        Annotation anno) {
        return false;
    }

    /**
     * The source mode for the given package.
     */
    private int getSourceMode(Package pkg) {
        Number num = _pkgs.get(pkg);
        return (num == null) ? 0 : num.intValue();
    }

    /**
     * Add to the source mode for the given package.
     */
    private void addSourceMode(Package pkg, int mode) {
        Integer num = _pkgs.get(pkg);
        if (num == null)
            num = mode;
        else
            num = num.intValue() | mode;
        _pkgs.put(pkg, num);
    }

    /**
     * Read annotations for the current type.
     */
    private ClassMetaData parseClassAnnotations() {
        // Check to see if there is cached metadata for the class that we are currently parsing. It
        // is possible that one of the annotations (Entity, Embeddable, MappedSuperclass) is in the
        // orm.xml. We still need to look at these files for other annotations and more importantly
        // setup defaults (ie: Basic fields).
        ClassMetaData m = getRepository().getCachedMetaData(_cls);
        if (m == null) {
            if (!(AccessController.doPrivileged(J2DoPrivHelper.isAnnotationPresentAction(_cls, Entity.class)))
                .booleanValue()
                && !(AccessController.doPrivileged(J2DoPrivHelper.isAnnotationPresentAction(_cls, Embeddable.class)))
                    .booleanValue()
                && !(AccessController.doPrivileged(J2DoPrivHelper.isAnnotationPresentAction(_cls,
                    MappedSuperclass.class))).booleanValue())
                return null;
        }
        // find / create metadata
        ClassMetaData meta = (m == null) ? getMetaData() : m;
        if (meta == null)
            return null;

        Entity entity = _cls.getAnnotation(Entity.class);
        MappedSuperclass mapped = _cls.getAnnotation(MappedSuperclass.class);
        Embeddable embeddable = _cls.getAnnotation(Embeddable.class);
        if (isMetaDataMode()) {
            meta.setAbstract(mapped != null);
            if (embeddable != null) meta.setEmbeddable();
            // while the spec only provides for embedded exclusive, it doesn't
            // seem hard to support otherwise
            if (entity == null)
                meta.setEmbeddedOnly(true);
            else {
                meta.setEmbeddedOnly(false);
                if (!StringUtils.isEmpty(entity.name()))
                    meta.setTypeAlias(entity.name());
            }
        }

        // track fetch groups to parse them after fields, since they
        // rely on field metadata
        FetchGroup[] fgs = null;
        DetachedState detached = null;

        // track listeners since we need to merge them with entity callbacks
        Collection<LifecycleCallbacks>[] listeners = null;
        MetaDataTag tag;
        for (Annotation anno : _cls.getDeclaredAnnotations()) {
            tag = _tags.get(anno.annotationType());
            if (tag == null) {
                handleUnknownClassAnnotation(meta, anno);
                continue;
            }

            switch (tag) {
                case ENTITY_LISTENERS:
                    if (isMetaDataMode())
                        listeners = parseEntityListeners(meta,
                            (EntityListeners) anno);
                    break;
                case EXCLUDE_DEFAULT_LISTENERS:
                    if (isMetaDataMode())
                        meta.getLifecycleMetaData()
                            .setIgnoreSystemListeners(true);
                    break;
                case EXCLUDE_SUPERCLASS_LISTENERS:
                    if (isMetaDataMode())
                        meta.getLifecycleMetaData().setIgnoreSuperclassCallbacks
                            (LifecycleMetaData.IGNORE_HIGH);
                    break;
                case FLUSH_MODE:
                    if (isMetaDataMode())
                        warnFlushMode(meta);
                    break;
                case ID_CLASS:
                    if (isMetaDataMode()) {
                    	Class<?> idClass = ((IdClass)anno).value();
                    	if (!Serializable.class.isAssignableFrom(idClass)) {
                    		_log.warn(_loc.get("id-class-not-serializable", idClass, _cls).toString());
                    	}
                        meta.setObjectIdType(((IdClass) anno).value(), true);
                    }
                    break;
                case NATIVE_QUERIES:
                    if (isQueryMode() && (meta.getSourceMode() & MODE_QUERY)==0)
                        parseNamedNativeQueries(_cls,
                            ((NamedNativeQueries) anno).value());
                    break;
                case NATIVE_QUERY:
                    if (isQueryMode() && (meta.getSourceMode() & MODE_QUERY)==0)
                        parseNamedNativeQueries(_cls, (NamedNativeQuery) anno);
                    break;
                case QUERIES:
                    if (isQueryMode() && (meta.getSourceMode() & MODE_QUERY)==0)
                        parseNamedQueries(_cls, ((NamedQueries) anno).value());
                    break;
                case QUERY:
                    if (isQueryMode() && (meta.getSourceMode() & MODE_QUERY)==0)
                        parseNamedQueries(_cls, (NamedQuery) anno);
                    break;
                case SEQ_GENERATOR:
                    if (isMappingOverrideMode())
                        parseSequenceGenerator(_cls, (SequenceGenerator) anno);
                    break;
                case DATA_CACHE:
                    if (isMetaDataMode())
                        parseDataCache(meta, (DataCache) anno);
                    break;
                case DATASTORE_ID:
                    if (isMetaDataMode())
                        parseDataStoreId(meta, (DataStoreId) anno);
                    break;
                case DETACHED_STATE:
                    detached = (DetachedState) anno;
                    break;
                case FETCH_GROUP:
                    if (isMetaDataMode())
                        fgs = new FetchGroup[]{ (FetchGroup) anno };
                    break;
                case FETCH_GROUPS:
                    if (isMetaDataMode())
                        fgs = ((FetchGroups) anno).value();
                    break;
                case MANAGED_INTERFACE:
                    if (isMetaDataMode())
                        parseManagedInterface(meta, (ManagedInterface) anno);
                    break;
                case ACCESS:
                    if (isMetaDataMode())
                        parseAccess(meta, (Access)anno);
                    break;
                case CACHEABLE:
                    if (isMetaDataMode()) {
                        parseCache(meta, (Cacheable) anno);
                    }
                    break;
                default:
                    throw new UnsupportedException(_loc.get("unsupported", _cls,
                        anno.toString()));
            }
        }

        if (isMetaDataMode()) {
            parseDetachedState(meta, detached);

            // merge callback methods with declared listeners
            int[] highs = null;
            if (listeners != null) {
                highs = new int[listeners.length];
                for (int i = 0; i < listeners.length; i++)
                    if (listeners[i] != null)
                        highs[i] = listeners[i].size();
            }
            recordCallbacks(meta, parseCallbackMethods(_cls, listeners, false,
                false, getRepository()), highs, false);

            // scan possibly non-PC hierarchy for callbacks.
            // redundant for PC superclass but we don't know that yet
            // so let LifecycleMetaData determine that
            if (_cls.getSuperclass() != null &&
                !Object.class.equals(_cls.getSuperclass())) {
                recordCallbacks(meta, parseCallbackMethods(_cls.getSuperclass(),
                    null, true, false, getRepository()), null, true);
            }
        }

        for (FieldMetaData fmd : meta.getDeclaredFields())
            if (fmd.getManagement() == FieldMetaData.MANAGE_PERSISTENT)
                parseMemberAnnotations(fmd);
        // parse fetch groups after fields
        if (fgs != null)
            parseFetchGroups(meta, fgs);

        // always parse mapping after metadata in case there are dependencies
        if (isMappingOverrideMode()) {
            parseClassMappingAnnotations(meta);
            for (FieldMetaData fmd : meta.getDeclaredFields())
                if (fmd.getManagement() == FieldMetaData.MANAGE_PERSISTENT)
                    parseMemberMappingAnnotations(fmd);
        }
        return meta;
    }

    /**
     * Set the explicit access type, if specified.
     */
    private void parseAccess(ClassMetaData meta, Access access) {
    	if (access != null) {
    		meta.setAccessType(AccessCode.EXPLICIT
            | (access.value() == AccessType.FIELD ?
            	AccessCode.FIELD : AccessCode.PROPERTY));
    	}
    }

    /**
     * Parse class mapping annotations.
     */
    protected void parseClassMappingAnnotations(ClassMetaData meta) {
    }

    /**
     * Allow subclasses to handle unknown annotations.
     */
    protected boolean handleUnknownClassAnnotation(ClassMetaData meta,
        Annotation anno) {
        return false;
    }

    /**
     * Find or create metadata for the given type. May return null if
     * this class has already been parsed fully.
     */
    private ClassMetaData getMetaData() {
        ClassMetaData meta = getRepository().getCachedMetaData(_cls);
        if (meta != null
            && ((isMetaDataMode() 
                && (meta.getSourceMode() & MODE_META) != 0) 
                || (isMappingMode() && (meta.getSourceMode() & MODE_MAPPING) != 0) ) ) {
            if (_log.isWarnEnabled()) {
                _log.warn(_loc.get("dup-metadata", _cls.getName()));
            }
            if(_log.isTraceEnabled()) { 
                _log.trace(String.format(
                    "MetaData originally obtained from file: %s under mode :%d with scope %s, and type :%d",
                    meta.getSourceName(), meta.getSourceMode(), meta.getSourceScope(), meta.getSourceType()));
            }
            return null;
        }

        if (meta == null) {
            meta = getRepository().addMetaData(_cls, getAccessCode(_cls));
            meta.setEnvClassLoader(_envLoader);
            meta.setSourceMode(MODE_NONE);
            meta.setSource(getSourceFile(), SourceTracker.SRC_ANNOTATIONS, getSourceFile() == null ? ""
                : getSourceFile().getPath());
        }
        return meta;
    }

    /**
     * Gets the explicit access for the class, if any.
     * Explicit access type specification does not affect the access type of
     * other entity classes or mapped super classes in the entity hierarchy.
     */
    private int getAccessCode(Class<?> cls) {
        int accessCode = AccessCode.UNKNOWN;
        Access access = AccessController.doPrivileged(
            J2DoPrivHelper.getAnnotationAction(cls, Access.class));
        if (access != null) {
            accessCode |=  AccessCode.EXPLICIT |
                (access.value() == AccessType.FIELD ?
                AccessCode.FIELD : AccessCode.PROPERTY);
        }
        return accessCode;
    }

    /**
     * Determine the source file we're parsing.
     */
    protected File getSourceFile() {
        if (_file != null)
            return _file;

        Class<?> cls = _cls;
        while (cls.getEnclosingClass() != null)
            cls = cls.getEnclosingClass();

        String rsrc = StringUtils.replace(cls.getName(), ".", "/");
        ClassLoader loader = AccessController.doPrivileged(
            J2DoPrivHelper.getClassLoaderAction(cls));
        if (loader == null)
            loader = AccessController.doPrivileged(
                J2DoPrivHelper.getSystemClassLoaderAction());
        if (loader == null)
            return null;
        URL url = AccessController.doPrivileged(
            J2DoPrivHelper.getResourceAction(loader, rsrc + ".java"));
        if (url == null) {
            url = AccessController.doPrivileged(
                J2DoPrivHelper.getResourceAction(loader, rsrc + ".class"));
            if (url == null)
                return null;
        }
        try {
            _file = new File(url.toURI());
        } catch (URISyntaxException e) {
        } catch (IllegalArgumentException iae) {
            // this is thrown when the URI is non-hierarchical (aka JBoss)
        }
        return _file;
    }

    /**
     * Parse @DataStoreId.
     */
    private void parseDataStoreId(ClassMetaData meta, DataStoreId id) {
        parseDataStoreId(meta, id.strategy(), id.generator());
    }
    
    static void parseDataStoreId(ClassMetaData meta, GenerationType strategy, 
        String generator) {
        meta.setIdentityType(ClassMetaData.ID_DATASTORE);

        int strat = getGeneratedValueStrategy(meta, strategy,
            generator);
        if (strat != -1)
            meta.setIdentityStrategy(strat);
        else {
            switch (strategy) {
                case TABLE:
                case SEQUENCE:
                    // technically we should have separate system table and
                    // sequence generators, but it's easier to just rely on
                    // the system org.apache.openjpa.Sequence setting for both
                    if (StringUtils.isEmpty(generator))
                        meta.setIdentitySequenceName(
                            SequenceMetaData.NAME_SYSTEM);
                    else
                        meta.setIdentitySequenceName(generator);
                    break;
                case AUTO:
                    meta.setIdentityStrategy(ValueStrategies.NATIVE);
                    break;
                case IDENTITY:
                    meta.setIdentityStrategy(ValueStrategies.AUTOASSIGN);
                    break;
                default:
                    throw new UnsupportedException(strategy.toString());
            }
        }
    }
    
    /**
     * Warn that @FlushMode is not supported.
     */
    private void warnFlushMode(Object context) {
        if (_log.isWarnEnabled())
            _log.warn(_loc.get("unsupported", "FlushMode", context));
    }

    /**
     * Parse @DataCache.
     * 
     */
    private void parseDataCache(ClassMetaData meta, DataCache cache) {
        parseDataCache(meta, cache.enabled(), cache.name(), cache.timeout());
    }
    
    static void parseDataCache(ClassMetaData meta,
            boolean enabled,
            String name,
            int timeout) {
        if (timeout != Integer.MIN_VALUE) {
            meta.setDataCacheTimeout(timeout);
        }
        String cacheName = name;
        if (StringUtils.isEmpty(cacheName)) {
            cacheName = org.apache.openjpa.datacache.DataCache.NAME_DEFAULT;
        }
        meta.setDataCacheName(enabled ? cacheName : null);
    }

    private void parseManagedInterface(ClassMetaData meta,
        ManagedInterface iface) {
        meta.setManagedInterface(true);
    }

    /**
     * Parse @DetachedState. The annotation may be null.
     */
    private void parseDetachedState(ClassMetaData meta,
        DetachedState detached) {
        if (detached != null) {
            if (!detached.enabled())
                meta.setDetachedState(null);
            else if (StringUtils.isEmpty(detached.fieldName()))
                meta.setDetachedState(ClassMetaData.SYNTHETIC);
            else
                meta.setDetachedState(detached.fieldName());
        } else {
            Field[] fields = (Field[]) AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredFieldsAction(
                    meta.getDescribedType()));
            for (int i = 0; i < fields.length; i++)
                if ((AccessController.doPrivileged(J2DoPrivHelper
                    .isAnnotationPresentAction(fields[i], DetachedState.class)))
                    .booleanValue())
                    meta.setDetachedState(fields[i].getName());
        }
    }

    /**
     * Parse @EntityListeners
     */
    private Collection<LifecycleCallbacks>[] parseEntityListeners
        (ClassMetaData meta, EntityListeners listeners) {
        Class<?>[] classes = listeners.value();
        Collection<Class<?>> listenerColl = null;
        Collection<LifecycleCallbacks>[] parsed = null;
        for (Class<?> cls : classes) {
            if (!_conf.getCallbackOptionsInstance().getAllowsDuplicateListener()) {
                if (listenerColl == null)
                    listenerColl = new ArrayList<Class<?>>();
                if (listenerColl.contains(cls)) 
                    continue;
                listenerColl.add(cls);
            }
            
            parsed = parseCallbackMethods(cls, parsed, true, true,
                getRepository());
        }
        return parsed;
    }

    /**
     * Parse callback methods into the given array, and return that array,
     * creating one if null. Each index into the array is a collection of
     * callback adapters for that numeric event type.
     *
     * @param sups whether to scan superclasses
     * @param listener whether this is a listener or not
     */
    public static Collection<LifecycleCallbacks>[] parseCallbackMethods
        (Class<?> cls, Collection<LifecycleCallbacks>[] callbacks, boolean sups,
        boolean listener, MetaDataRepository repos) {

        if (cls == null)
            throw new IllegalArgumentException("cls cannot be null");

        // first sort / filter based on inheritance
        Set<Method> methods = new TreeSet<Method>(MethodComparator.
            getInstance());

        int mods;
        Class<?> sup = cls;
        MethodKey key;
        Set<MethodKey> seen = new HashSet<MethodKey>();
        do {
            for (Method m : (Method[]) AccessController.doPrivileged(
                J2DoPrivHelper.getDeclaredMethodsAction(sup))) {
                mods = m.getModifiers();
                if (Modifier.isStatic(mods) || Modifier.isFinal(mods) ||
                    Object.class.equals(m.getDeclaringClass()))
                    continue;

                key = new MethodKey(m);
                if (!seen.contains(key)) {
                    methods.add(m);
                    seen.add(key);
                }
            }
            sup = sup.getSuperclass();
        } while (sups && !Object.class.equals(sup));

        OpenJPAConfiguration conf = repos.getConfiguration();
        for (Method m : methods) {
            for (Annotation anno : (Annotation[]) AccessController
                .doPrivileged(J2DoPrivHelper
                    .getDeclaredAnnotationsAction(m))) {
                MetaDataTag tag = _tags.get(anno.annotationType());
                if (tag == null)
                    continue;
                int[] events = MetaDataParsers.getEventTypes(tag, conf);
                if (events == null)
                    continue;

                if (callbacks == null)
                    callbacks = (Collection<LifecycleCallbacks>[])
                        new Collection[LifecycleEvent.ALL_EVENTS.length];

                for (int i = 0; i < events.length; i++) {
                    int e = events[i];
                    if (callbacks[e] == null)
                        callbacks[e] = new ArrayList<LifecycleCallbacks>(3);
                    MetaDataParsers.validateMethodsForSameCallback(cls,
                        callbacks[e], m, tag, conf, repos.getLog());
                    if (listener) {
                        callbacks[e].add(new BeanLifecycleCallbacks(cls, m,
                            false));
                    } else {
                        callbacks[e].add(new MethodLifecycleCallbacks(m,
                            false));
                    }
                }
            }
        }
        return callbacks;
    }

    /**
     * Store lifecycle metadata.
     */
    private void recordCallbacks(ClassMetaData cls,
        Collection<LifecycleCallbacks>[] callbacks, int[] highs,
        boolean superClass) {
        if (callbacks == null)
            return;
        LifecycleMetaData meta = cls.getLifecycleMetaData();
        LifecycleCallbacks[] array;
        for (int event : LifecycleEvent.ALL_EVENTS) {
            if (callbacks[event] == null)
                continue;
            array = callbacks[event].toArray
                (new LifecycleCallbacks[callbacks[event].size()]);

            if (superClass) {
                meta.setNonPCSuperclassCallbacks(event, array,
                    (highs == null) ? 0 : highs[event]);
            } else {
                meta.setDeclaredCallbacks(event, array,
                    (highs == null) ? 0 : highs[event]);
            }
        }
    }

    /**
     * Create fetch groups.
     * If FetchGroup A includes FetchGroup B, then a bi-link is set between
     * A and B. Both A and B must be declared in the same Class.
     * <br>
     * Call {@link #parseFetchAttribute(ClassMetaData,
     * org.apache.openjpa.meta.FetchGroup, FetchAttribute)} only after the
     * bi-links have been established, because a field f will not only add the
     * fetch group A which explicitly includes f to its custom fetch groups but
     * also will also add any fetch group B that includes A.
     */
    static void parseFetchGroups(ClassMetaData meta, FetchGroup... groups) {
       FetchGroupImpl[] fetchGroupImpls = new FetchGroupImpl[groups.length];
       for (int i = 0; i < groups.length; i++) {
           FetchAttribute[] fetchAttributes = groups[i].attributes();
           FetchAttributeImpl[] fetchAttributeImpls= null;
           if (fetchAttributes != null && fetchAttributes.length > 0) {
               fetchAttributeImpls = new FetchAttributeImpl[fetchAttributes.length];
               for (int j = 0; j < fetchAttributes.length; j++) {
                   fetchAttributeImpls[j] = 
                           new FetchAttributeImpl(fetchAttributes[j].name(), fetchAttributes[j].recursionDepth()); 
               }
           }
           fetchGroupImpls[i] = new FetchGroupImpl(groups[i].name(), groups[i].postLoad());
           if (fetchAttributeImpls != null) {
               fetchGroupImpls[i].setAttributes(fetchAttributeImpls);
           }
           if (groups[i].fetchGroups() != null) {
               fetchGroupImpls[i].setFetchGroups(groups[i].fetchGroups());
           }
       }
       
       parseFetchGroups(meta, fetchGroupImpls);
    }
   
   /**
    * Parse fetch group input for the FetchGroup and FetchGroups annotations
    * as well as for the fetch-group and fetch-groups XML metadata
    * @param meta
    * @param groups
    */
   static void parseFetchGroups(ClassMetaData meta, FetchGroupImpl... groups) {
       org.apache.openjpa.meta.FetchGroup fg;
       for (FetchGroupImpl group : groups) {
           if (StringUtils.isEmpty(group.name()))
               throw new MetaDataException(_loc.get("unnamed-fg", meta));

           fg = meta.addDeclaredFetchGroup(group.name());
           if (group.postLoad())
               fg.setPostLoad(true);
           for (String s : group.fetchGroups()) {
               fg.addDeclaredInclude(s);
           }
       }
       // Add the parent-child style bi-links between fetch groups in a
       // separate pass.
       for (FetchGroupImpl group:groups) {
           fg = meta.getFetchGroup(group.name());
           String[] includedFetchGropNames = fg.getDeclaredIncludes();
           for (String includedFectchGroupName:includedFetchGropNames) {
               org.apache.openjpa.meta.FetchGroup child =
               meta.getFetchGroup(includedFectchGroupName);
               if (child == null)
                   throw new UserException(_loc.get("missing-included-fg",
                           meta.getDescribedType().getName(), fg.getName(),
                           includedFectchGroupName));
               child.addContainedBy(fg);
           }
       }

       for (FetchGroupImpl group : groups) {
           fg = meta.getFetchGroup(group.name());
           for (FetchAttributeImpl attr : group.attributes())
               parseFetchAttribute(meta, fg, attr);
       }
   }


    /**
     * Set a field's fetch group.
     */
    private static void parseFetchAttribute(ClassMetaData meta,
        org.apache.openjpa.meta.FetchGroup fg, FetchAttributeImpl attr) {
        FieldMetaData field = meta.getDeclaredField(attr.name());
        if (field == null
            || field.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            throw new MetaDataException(_loc.get("bad-fg-field", fg.getName(),
                meta, attr.name()));

        field.setInFetchGroup(fg.getName(), true);
        Set<String> parentFetchGroups = fg.getContainedBy();
        for (Object parentFetchGroup : parentFetchGroups)
        	field.setInFetchGroup(parentFetchGroup.toString(), true);
        if (attr.recursionDepth() != Integer.MIN_VALUE)
            fg.setRecursionDepth(field, attr.recursionDepth());
    }

    /**
     * Read annotations for the given member.
     */
    private void parseMemberAnnotations(FieldMetaData fmd) {
        // look for persistence strategy in annotation table
        Member member = getRepository().getMetaDataFactory().getDefaults().
            getBackingMember(fmd);
        PersistenceStrategy pstrat = PersistenceMetaDataDefaults.
            getPersistenceStrategy(fmd, member);
        if (pstrat == null)
            return;
        fmd.setExplicit(true);

        AnnotatedElement el = (AnnotatedElement) member;
        boolean lob = (AccessController.doPrivileged(J2DoPrivHelper
            .isAnnotationPresentAction(el, Lob.class))).booleanValue();
        if (isMetaDataMode()) {
            switch (pstrat) {
                case BASIC:
                    parseBasic(fmd, (Basic) el.getAnnotation(Basic.class), lob);
                    break;
                case MANY_ONE:
                    parseManyToOne(fmd, (ManyToOne) el.getAnnotation
                        (ManyToOne.class));
                    break;
                case ONE_ONE:
                    parseOneToOne(fmd, (OneToOne) el.getAnnotation
                        (OneToOne.class));
                    break;
                case EMBEDDED:
                    parseEmbedded(fmd, (Embedded) el.getAnnotation
                        (Embedded.class));
                    break;
                case ONE_MANY:
                    parseOneToMany(fmd, (OneToMany) el.getAnnotation
                        (OneToMany.class));
                    break;
                case MANY_MANY:
                    parseManyToMany(fmd, (ManyToMany) el.getAnnotation
                        (ManyToMany.class));
                    break;
                case PERS:
                    parsePersistent(fmd, (Persistent) el.getAnnotation
                        (Persistent.class));
                    break;
                case PERS_COLL:
                    parsePersistentCollection(fmd, (PersistentCollection)
                        el.getAnnotation(PersistentCollection.class));
                    break;
                case ELEM_COLL:
                    parseElementCollection(fmd, (ElementCollection)
                        el.getAnnotation(ElementCollection.class));
                    break;
                case PERS_MAP:
                    parsePersistentMap(fmd, (PersistentMap)
                        el.getAnnotation(PersistentMap.class));
                    break;
                case TRANSIENT:
                    break;
                default:
                    throw new InternalException();
            }
        }

        if (isMappingOverrideMode() && lob)
            parseLobMapping(fmd);

        // extensions
        MetaDataTag tag;
        for (Annotation anno : el.getDeclaredAnnotations()) {
            tag = _tags.get(anno.annotationType());
            if (tag == null) {
                handleUnknownMemberAnnotation(fmd, anno);
                continue;
            }

            switch (tag) {
                case ACCESS:
                    parseAccess(fmd, (Access)anno);
                    break;
                case FLUSH_MODE:
                    if (isMetaDataMode())
                        warnFlushMode(fmd);
                    break;
                case GENERATED_VALUE:
                    if (isMappingOverrideMode())
                        parseGeneratedValue(fmd, (GeneratedValue) anno);
                    break;
                case ID:
                case EMBEDDED_ID:
                    fmd.setPrimaryKey(true);
                    break;
                case MAPPED_BY_ID:
                    parseMapsId(fmd, (MapsId)anno);
                    break;
                case MAP_KEY:
                    if (isMappingOverrideMode())
                        parseMapKey(fmd, (MapKey) anno);
                    break;
                case MAP_KEY_CLASS:
                    if (isMappingOverrideMode())
                        parseMapKeyClass(fmd, (MapKeyClass) anno);
                    break;
                case ORDER_BY:
                    parseOrderBy(fmd,
                        (OrderBy) el.getAnnotation(OrderBy.class));
                    break;
                case SEQ_GENERATOR:
                    if (isMappingOverrideMode())
                        parseSequenceGenerator(el, (SequenceGenerator) anno);
                    break;
                case VERSION:
                    fmd.setVersion(true);
                    break;
                case DEPENDENT:
                    if (isMetaDataMode() && ((Dependent) anno).value())
                        fmd.setCascadeDelete(ValueMetaData.CASCADE_AUTO);
                    break;
                case ELEM_DEPENDENT:
                    if (isMetaDataMode() && ((ElementDependent) anno).value())
                        fmd.getElement().setCascadeDelete
                            (ValueMetaData.CASCADE_AUTO);
                    break;
                case ELEM_TYPE:
                    if (isMetaDataMode())
                        fmd.getElement().setTypeOverride(toOverrideType
                            (((ElementType) anno).value()));
                    break;
                case EXTERNAL_VALS:
                    if (isMetaDataMode())
                        fmd.setExternalValues(Strings.join(((ExternalValues)
                            anno).value(), ","));
                    break;
                case EXTERNALIZER:
                    if (isMetaDataMode())
                        fmd.setExternalizer(((Externalizer) anno).value());
                    break;
                case FACTORY:
                    if (isMetaDataMode())
                        fmd.setFactory(((Factory) anno).value());
                    break;
                case INVERSE_LOGICAL:
                    if (isMetaDataMode())
                        fmd.setInverse(((InverseLogical) anno).value());
                    break;
                case KEY_DEPENDENT:
                    if (isMetaDataMode() && ((KeyDependent) anno).value())
                        fmd.getKey()
                            .setCascadeDelete(ValueMetaData.CASCADE_AUTO);
                    break;
                case KEY_TYPE:
                    if (isMetaDataMode())
                        fmd.getKey().setTypeOverride(toOverrideType(((KeyType)
                            anno).value()));
                    break;
                case LOAD_FETCH_GROUP:
                	if (isMetaDataMode())
                        fmd.setLoadFetchGroup(((LoadFetchGroup) anno).value());
                	break;
                case LRS:
                    if (isMetaDataMode())
                        fmd.setLRS(((LRS) anno).value());
                    break;
                case READ_ONLY:
                    if (isMetaDataMode())
                        parseReadOnly(fmd, (ReadOnly) anno);
                    break;
                case TYPE:
                    if (isMetaDataMode())
                        fmd.setTypeOverride(toOverrideType(((Type) anno).
                            value()));
                    break;
                default:
                    throw new UnsupportedException(_loc.get("unsupported", fmd,
                        anno.toString()));
            }
        }
    }

    /**
     * Parse member mapping components.
     */
    protected void parseMemberMappingAnnotations(FieldMetaData fmd) {
    }

    /**
     * Parse @Cache.
     */
    private void parseCache(ClassMetaData meta, Cacheable cacheable) {
        meta.setCacheEnabled(cacheable.value());
    }

    /**
     * Allow subclasses to handle unknown annotations.
     */
    protected boolean handleUnknownMemberAnnotation(FieldMetaData fmd,
        Annotation anno) {
        return false;
    }

    /**
     * Convert the given class to its OpenJPA type override equivalent.
     */
    public static Class<?> toOverrideType(Class<?> cls) {
        return (cls == Entity.class)
            ? org.apache.openjpa.enhance.PersistenceCapable.class : cls;
    }

    /**
     * Parse @ReadOnly.
     */
    private void parseReadOnly(FieldMetaData fmd, ReadOnly ro) {
        if (ro.value() == UpdateAction.RESTRICT)
            fmd.setUpdateStrategy(UpdateStrategies.RESTRICT);
        else if (ro.value() == UpdateAction.IGNORE)
            fmd.setUpdateStrategy(UpdateStrategies.IGNORE);
        else
            throw new InternalException();
    }

    /**
     * Sets value generation information for the given field.
     */
    private void parseGeneratedValue(FieldMetaData fmd, GeneratedValue gen) {
        GenerationType strategy = gen.strategy();
        String generator = gen.generator();
        parseGeneratedValue(fmd, strategy, generator);
    }

    /**
     * Sets value generation information for the given field.
     */
    static void parseGeneratedValue(FieldMetaData fmd, GenerationType strategy,
        String generator) {
        int strat = getGeneratedValueStrategy(fmd, strategy, generator);
        if (strat != -1)
            fmd.setValueStrategy(strat);
        else {
            switch (strategy) {
                case TABLE:
                case SEQUENCE:
                    // technically we should have separate system table and
                    // sequence generators, but it's easier to just rely on
                    // the system org.apache.openjpa.Sequence setting for both
                    if (StringUtils.isEmpty(generator))
                        fmd.setValueSequenceName(SequenceMetaData.NAME_SYSTEM);
                    else
                        fmd.setValueSequenceName(generator);
                    break;
                case AUTO:
                    fmd.setValueSequenceName(SequenceMetaData.NAME_SYSTEM);
                    break;
                case IDENTITY:
                    fmd.setValueStrategy(ValueStrategies.AUTOASSIGN);
                    break;
                default:
                    throw new UnsupportedException(strategy.toString());
            }
        }
    }

    /**
     * Return the value strategy for the given generator, or -1 if the
     * strategy depends on the <code>GenerationType</code> rather than the
     * generator name.
     */
    private static int getGeneratedValueStrategy(Object context,
        GenerationType strategy, String generator) {
        if (strategy != AUTO || StringUtils.isEmpty(generator))
            return -1;

        if (Generator.UUID_HEX.equals(generator))
            return ValueStrategies.UUID_HEX;
        if (Generator.UUID_STRING.equals(generator))
            return ValueStrategies.UUID_STRING;
        if (Generator.UUID_TYPE4_HEX.equals(generator))
            return ValueStrategies.UUID_TYPE4_HEX;
        if (Generator.UUID_TYPE4_STRING.equals(generator))
            return ValueStrategies.UUID_TYPE4_STRING;
        throw new MetaDataException(_loc.get("generator-bad-strategy",
            context, generator));
    }

    /**
     * Parse @Basic. Given annotation may be null.
     */
    private void parseBasic(FieldMetaData fmd, Basic anno, boolean lob) {
        Class<?> type = fmd.getDeclaredType();
        if (lob && type != String.class
            && type != char[].class && type != Character[].class
            && type != byte[].class && type != Byte[].class)
            fmd.setSerialized(true);
        else if (!lob) {
            switch (fmd.getDeclaredTypeCode()) {
                case JavaTypes.OBJECT:
                    if (Enum.class.isAssignableFrom(type))
                        break;
                    // else no break
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                case JavaTypes.PC:
                case JavaTypes.PC_UNTYPED:
                    if (Serializable.class.isAssignableFrom(type))
                        fmd.setSerialized(true);
                    else
                        throw new MetaDataException(_loc.get("bad-meta-anno",
                            fmd, "Basic"));
                    break;
                case JavaTypes.ARRAY:
                    if (type == char[].class || type == Character[].class
                        || type == byte[].class || type == Byte[].class)
                        break;
                    if (Serializable.class.isAssignableFrom
                        (type.getComponentType()))
                        fmd.setSerialized(true);
                    else
                        throw new MetaDataException(_loc.get("bad-meta-anno",
                            fmd, "Basic"));
                    break;
            }
        }

        if (anno == null)
            return;
        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (!anno.optional())
            fmd.setNullValue(FieldMetaData.NULL_EXCEPTION);
    }

    /**
     * Parse @ManyToOne.
     */
    private void parseManyToOne(FieldMetaData fmd, ManyToOne anno) {
        if (!JavaTypes.maybePC(fmd.getValue()))
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "ManyToOne"));

        // don't specifically exclude relation from DFG b/c that will prevent
        // us from even reading the fk when reading from the primary table,
        // which is not what most users will want
        if (anno.fetch() == FetchType.EAGER)
            fmd.setInDefaultFetchGroup(true);
        if (!anno.optional())
            fmd.setNullValue(FieldMetaData.NULL_EXCEPTION);
        if (anno.targetEntity() != void.class)
            fmd.setTypeOverride(anno.targetEntity());
        setCascades(fmd, anno.cascade());
        fmd.setAssociationType(FieldMetaData.MANY_TO_ONE);
    }

    /**
     * Parse @OneToOne.
     */
    private void parseOneToOne(FieldMetaData fmd, OneToOne anno) {
        if (!JavaTypes.maybePC(fmd.getValue()))
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "OneToOne"));

        // don't specifically exclude relation from DFG b/c that will prevent
        // us from even reading the fk when reading from the primary table,
        // which is not what most users will want
        if (anno.fetch() == FetchType.EAGER)
            fmd.setInDefaultFetchGroup(true);
        if (!anno.optional())
            fmd.setNullValue(FieldMetaData.NULL_EXCEPTION);

        if (isMappingOverrideMode() && !StringUtils.isEmpty(anno.mappedBy()))
            fmd.setMappedBy(anno.mappedBy());
        if (anno.targetEntity() != void.class)
            fmd.setTypeOverride(anno.targetEntity());
        setCascades(fmd, anno.cascade());
        setOrphanRemoval(fmd, anno.orphanRemoval());
        fmd.setAssociationType(FieldMetaData.ONE_TO_ONE);
    }

    /**
     * Parse @Embedded. Given annotation may be null.
     */
    private void parseEmbedded(FieldMetaData fmd, Embedded anno) {
        if (!JavaTypes.maybePC(fmd.getValue()))
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "Embedded"));

        fmd.setInDefaultFetchGroup(true);
        fmd.setEmbedded(true);

        if (fmd.getEmbeddedMetaData() == null)
            fmd.addEmbeddedMetaData(getAccessCode(fmd.getDeclaredType()));
    }

    /**
     * Parse @OneToMany.
     */
    private void parseOneToMany(FieldMetaData fmd, OneToMany anno) {
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                if (JavaTypes.maybePC(fmd.getElement()))
                    break;
                // no break
            default:
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "OneToMany"));
        }

        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (isMappingOverrideMode() && !StringUtils.isEmpty(anno.mappedBy()))
            fmd.setMappedBy(anno.mappedBy());
        if (anno.targetEntity() != void.class)
            fmd.getElement().setDeclaredType(anno.targetEntity());
        setCascades(fmd.getElement(), anno.cascade());
        setOrphanRemoval(fmd.getElement(), anno.orphanRemoval());
        fmd.setAssociationType(FieldMetaData.ONE_TO_MANY);
    }

    /**
     * Parse @ManyToMany.
     */
    private void parseManyToMany(FieldMetaData fmd, ManyToMany anno) {
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                if (fmd.getDeclaredType() == Properties.class || JavaTypes.maybePC(fmd.getElement()))
                    break;
                // no break
            default:
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "ManyToMany"));
        }

        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (isMappingOverrideMode() && !StringUtils.isEmpty(anno.mappedBy()))
            fmd.setMappedBy(anno.mappedBy());
        if (anno.targetEntity() != void.class)
            fmd.getElement().setDeclaredType(anno.targetEntity());
        setCascades(fmd.getElement(), anno.cascade());
        fmd.setAssociationType(FieldMetaData.MANY_TO_MANY);
    }

    /**
     * Parse @MapKey.
     */
    private void parseMapKey(FieldMetaData fmd, MapKey anno) {
        String name = anno.name();
        if (StringUtils.isEmpty(name))
            fmd.getKey().setValueMappedBy(ValueMetaData.MAPPED_BY_PK);
        else
            fmd.getKey().setValueMappedBy(name);
    }

    /**
     * Parse @MapKeyClass.
     */
    private void parseMapKeyClass(FieldMetaData fmd, MapKeyClass anno) {
        if (anno.value() != void.class)
            fmd.getKey().setDeclaredType(anno.value());
        else
            throw new IllegalArgumentException(
                 "The value of the MapClassClass cannot be null");
    }

    /**
     * Parse @MapsId.
     */
    private void parseMapsId(FieldMetaData fmd, MapsId anno) {
        String value = anno.value();
        if (value != null)
            fmd.setMappedByIdValue(value);
        else
            fmd.setMappedByIdValue("");
    }

    /**
     * Setup the field as a LOB mapping.
     */
    protected void parseLobMapping(FieldMetaData fmd) {
    }

    /**
     * Parse @OrderBy.
     */
    private void parseOrderBy(FieldMetaData fmd, OrderBy anno) {
        String dec = anno.value();
        if (fmd.isElementCollection() &&
            fmd.getElement().getEmbeddedMetaData() != null) {
                if (dec.length() == 0 || dec.equals("ASC") ||
                    dec.equals("DESC"))
                    throw new MetaDataException(_loc.get(
                        "invalid-orderBy", fmd));
        }
        if (dec.length() == 0 || dec.equals("ASC"))
            dec = Order.ELEMENT + " asc";
        else if (dec.equals("DESC"))
            dec = Order.ELEMENT + " desc";

        fmd.setOrderDeclaration(dec);
    }

    /**
     * Parse @Persistent.
     */
    private void parsePersistent(FieldMetaData fmd, Persistent anno) {
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
                if (fmd.getDeclaredType() == byte[].class
                    || fmd.getDeclaredType() == Byte[].class
                    || fmd.getDeclaredType() == char[].class
                    || fmd.getDeclaredType() == Character[].class)
                    break;
                // no break
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "Persistent"));
        }

        if (!StringUtils.isEmpty(anno.mappedBy()))
            fmd.setMappedBy(anno.mappedBy());
        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (!anno.optional())
            fmd.setNullValue(FieldMetaData.NULL_EXCEPTION);
        setCascades(fmd, anno.cascade());
        if (anno.embedded()) {
            if (!JavaTypes.maybePC(fmd.getValue()))
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "Persistent(embedded=true)"));
            fmd.setEmbedded(true);
            if (fmd.getEmbeddedMetaData() == null) {
                fmd.addEmbeddedMetaData(getAccessCode(fmd.getDeclaredType()));
            }
        }
    }

    /**
     * Parse @PersistentCollection.
     */
    private void parsePersistentCollection(FieldMetaData fmd,
        PersistentCollection anno) {
        if (fmd.getDeclaredTypeCode() != JavaTypes.ARRAY
            && fmd.getDeclaredTypeCode() != JavaTypes.COLLECTION)
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "PersistentCollection"));

        fmd.setPersistentCollection(true); 
        
        if (!StringUtils.isEmpty(anno.mappedBy()))
            fmd.setMappedBy(anno.mappedBy());
        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (anno.elementType() != void.class)
            fmd.getElement().setDeclaredType(anno.elementType());
        setCascades(fmd.getElement(), anno.elementCascade());
        if (anno.elementEmbedded()) {
            if (!JavaTypes.maybePC(fmd.getElement()))
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "PersistentCollection(embeddedElement=true)"));
            fmd.getElement().setEmbedded(true);
            if (fmd.getElement().getEmbeddedMetaData() == null) {
                fmd.getElement().addEmbeddedMetaData(
                    getAccessCode(fmd.getElement().getDeclaredType()));
            }
        }
    }

    /**
     * Parse @ElementCollection.
     */
    private void parseElementCollection(FieldMetaData fmd,
        ElementCollection anno) {
        // TODO: throw exception if the runtime env is OpenJpa 1.x

        if (fmd.getDeclaredTypeCode() != JavaTypes.COLLECTION &&
            fmd.getDeclaredTypeCode() != JavaTypes.MAP)
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "ElementCollection"));

        if (anno.targetClass() != void.class)
            fmd.getElement().setDeclaredType(anno.targetClass());
        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        fmd.setElementCollection(true);
        ValueMetaData elem = fmd.getElement();
        boolean isEnum = elem.getDeclaredType().isEnum();
        if (!isEnum && JavaTypes.maybePC(elem)) {
            elem.setEmbedded(true);
            if (elem.getEmbeddedMetaData() == null)
                elem.addEmbeddedMetaData(getAccessCode(elem.getDeclaredType()));
        }
    }

    /**
     * Parse @PersistentMap.
     */
    private void parsePersistentMap(FieldMetaData fmd, PersistentMap anno) {
        if (fmd.getDeclaredTypeCode() != JavaTypes.MAP)
            throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                "PersistentMap"));

        fmd.setInDefaultFetchGroup(anno.fetch() == FetchType.EAGER);
        if (anno.keyType() != void.class)
            fmd.getKey().setDeclaredType(anno.keyType());
        if (anno.elementType() != void.class)
            fmd.getElement().setDeclaredType(anno.elementType());
        setCascades(fmd.getKey(), anno.keyCascade());
        setCascades(fmd.getElement(), anno.elementCascade());
        if (anno.keyEmbedded()) {
            if (!JavaTypes.maybePC(fmd.getKey()))
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "PersistentMap(embeddedKey=true)"));
            fmd.getKey().setEmbedded(true);
            if (fmd.getKey().getEmbeddedMetaData() == null) {
                fmd.getKey().addEmbeddedMetaData(
                    getAccessCode(fmd.getKey().getDeclaredType()));
            }
        }
        if (anno.elementEmbedded()) {
            if (!JavaTypes.maybePC(fmd.getElement()))
                throw new MetaDataException(_loc.get("bad-meta-anno", fmd,
                    "PersistentMap(embeddedValue=true)"));
            fmd.getElement().setEmbedded(true);
            if (fmd.getElement().getEmbeddedMetaData() == null)
                fmd.getElement().addEmbeddedMetaData(
                    getAccessCode(fmd.getElement().getDeclaredType()));
        }
    }

    /**
     * Set cascades on relation.
     */
    private void setCascades(ValueMetaData vmd, CascadeType[] cascades) {
        for (CascadeType cascade : cascades) {
            if (cascade == CascadeType.ALL || cascade == CascadeType.REMOVE)
                vmd.setCascadeDelete(ValueMetaData.CASCADE_IMMEDIATE);
            if (cascade == CascadeType.ALL || cascade == CascadeType.PERSIST)
                vmd.setCascadePersist(ValueMetaData.CASCADE_IMMEDIATE);
            if (cascade == CascadeType.ALL || cascade == CascadeType.MERGE)
                vmd.setCascadeAttach(ValueMetaData.CASCADE_IMMEDIATE);
            if (cascade == CascadeType.ALL || cascade == CascadeType.DETACH)
                vmd.setCascadeDetach(ValueMetaData.CASCADE_IMMEDIATE);
            if (cascade == CascadeType.ALL || cascade == CascadeType.REFRESH)
                vmd.setCascadeRefresh(ValueMetaData.CASCADE_IMMEDIATE);
        }
    }

    private void setOrphanRemoval(ValueMetaData vmd, boolean orphanRemoval) {
        if (orphanRemoval)
            vmd.setCascadeDelete(ValueMetaData.CASCADE_AUTO);
    }

    /**
     * Parse @SequenceGenerator.
     */
    private void parseSequenceGenerator(AnnotatedElement el,
        SequenceGenerator gen) {
        String name = gen.name();
        if (StringUtils.isEmpty(name))
            throw new MetaDataException(_loc.get("no-seq-name", el));

        if (_log.isTraceEnabled())
            _log.trace(_loc.get("parse-sequence", name));

        SequenceMetaData meta = getRepository().getCachedSequenceMetaData
            (name);
        if (meta != null) {
            if (_log.isWarnEnabled())
                _log.warn(_loc.get("dup-sequence", name, el));
            return;
        }

        // create new sequence
        meta = getRepository().addSequenceMetaData(name);
        String seq = gen.sequenceName();
        // Do not normalize the sequence name if it appears to be a plugin 
        if (seq.indexOf('(') == -1){
            seq = normalizeSequenceName(seq);
        }
        int initial = gen.initialValue();
        int allocate = gen.allocationSize();
        String schema = normalizeSchemaName(gen.schema());
        String catalog = normalizeCatalogName(gen.catalog());
        // don't allow initial of 0 b/c looks like def value
        if (initial == 0)
            initial = 1;

        // create plugin string from info
        String clsName, props;
        if (StringUtils.isEmpty(seq)) {
            clsName = SequenceMetaData.IMPL_NATIVE;
            props = null;
        } else if (seq.indexOf('(') != -1) // plugin
        {
            clsName = Configurations.getClassName(seq);
            props = Configurations.getProperties(seq);
            seq = null;
        } else {
            clsName = SequenceMetaData.IMPL_NATIVE;
            props = null;
        }

        meta.setSequencePlugin(Configurations.getPlugin(clsName, props));
        meta.setSequence(seq);
        meta.setInitialValue(initial);
        meta.setAllocate(allocate);
        meta.setSchema(schema);
        meta.setCatalog(catalog);
        meta.setSource(getSourceFile(), (el instanceof Class) ? el : null,
            SourceTracker.SRC_ANNOTATIONS);
    }

    /**
     * Parse @NamedQuery.
     */
    private void parseNamedQueries(AnnotatedElement el, NamedQuery... queries) {
        QueryMetaData meta;
        for (NamedQuery query : queries) {
            if (StringUtils.isEmpty(query.name()))
                throw new MetaDataException(_loc.get("no-query-name", el));
            if (StringUtils.isEmpty(query.query()))
                throw new MetaDataException(_loc.get("no-query-string",
                    query.name(), el));

            if (_log.isTraceEnabled())
                _log.trace(_loc.get("parse-query", query.name()));

            meta = getRepository().searchQueryMetaDataByName(query.name());
            if (meta != null) {
            	Class<?> definingType = meta.getDefiningType();
                if ((definingType == null || definingType != _cls)
                  && _log.isWarnEnabled()) {
                    _log.warn(_loc.get("dup-query", query.name(), el,
                    		definingType));
                }
                continue;
            }
            meta = getRepository().addQueryMetaData(_cls, query.name());
            meta.setLanguage(JPQLParser.LANG_JPQL);
            meta.setQueryString(query.query());
            for (QueryHint hint : query.hints())
                meta.addHint(hint.name(), hint.value());
            LockModeType lmt = processNamedQueryLockModeType(query);
            if (lmt != null && lmt != LockModeType.NONE) {
                meta.addHint("openjpa.FetchPlan.ReadLockMode", lmt);
            }

            meta.setSource(getSourceFile(), (el instanceof Class) ? el : null,
                SourceTracker.SRC_ANNOTATIONS, getSourceFile() == null ? "" : getSourceFile().getPath());
            if (isMetaDataMode())
                meta.setSourceMode(MODE_META);
            else if (isMappingMode())
                meta.setSourceMode(MODE_MAPPING);
            else
                meta.setSourceMode(MODE_QUERY);
        }
    }

    /**
     * A private worker method that calculates the lock mode for an individual NamedQuery. If the NamedQuery is 
     * configured to use the NONE lock mode(explicit or implicit), this method will promote the lock to a READ
     * level lock. This was done to allow for JPA1 apps to function properly under a 2.0 runtime. 
     */
    private LockModeType processNamedQueryLockModeType(NamedQuery query) {
        LockModeType lmt = query.lockMode();
        if (query.lockMode() != null) {
            String lm = _conf.getLockManager();
            boolean optimistic = _conf.getOptimistic();
            if (lm != null) {
                lm = lm.toLowerCase();
                if (lm.contains("pessimistic")) {
                    if (lmt == LockModeType.NONE && !optimistic) {
                        if (_log.isWarnEnabled() == true) {
                            _log.warn(_loc.get("override-named-query-lock-mode", new String[] { "annotation",
                                query.name(), _cls.getName() }));
                        }
                        lmt = LockModeType.READ;
                    }
                }
            }
        }
        return lmt;
    }

    /**
     * Parse @NamedNativeQuery.
     */
    private void parseNamedNativeQueries(AnnotatedElement el,
        NamedNativeQuery... queries) {
        QueryMetaData meta;
        for (NamedNativeQuery query : queries) {
            if (StringUtils.isEmpty(query.name()))
                throw new MetaDataException(_loc.get("no-native-query-name",
                    el));
            if (StringUtils.isEmpty(query.query()))
                throw new MetaDataException(_loc.get("no-native-query-string",
                    query.name(), el));

            if (_log.isTraceEnabled())
                _log.trace(_loc.get("parse-native-query", query.name()));

            meta = getRepository().searchQueryMetaDataByName(query.name());
            if (meta != null) {
            	Class<?> defType = meta.getDefiningType();
                if ((defType != _cls) && _log.isWarnEnabled()) {
                    _log.warn(_loc.get("dup-query", query.name(), el, defType));
                }
                continue;
            }

            meta = getRepository().addQueryMetaData(null, query.name());
            meta.setLanguage(QueryLanguages.LANG_SQL);
            meta.setQueryString(query.query());
            Class<?> res = query.resultClass();
            if (ImplHelper.isManagedType(getConfiguration(), res))
                meta.setCandidateType(res);
            else if (!void.class.equals(res))
                meta.setResultType(res);

            if (!StringUtils.isEmpty(query.resultSetMapping()))
                meta.setResultSetMappingName(query.resultSetMapping());
            for (QueryHint hint : query.hints())
                meta.addHint(hint.name(), hint.value());

            meta.setSource(getSourceFile(), (el instanceof Class) ? el : null,
                SourceTracker.SRC_ANNOTATIONS, getSourceFile() == null ? "" : getSourceFile().getPath());
            if (isMetaDataMode())
                meta.setSourceMode(MODE_META);
            else if (isMappingMode())
                meta.setSourceMode(MODE_MAPPING);
            else
                meta.setSourceMode(MODE_QUERY);
        }
    }

    /**
     * Set the explicit access type, if specified.
     */
    private void parseAccess(FieldMetaData meta, Access access) {
    	if (access != null) {
    		meta.setAccessType(AccessCode.EXPLICIT
            | (access.value() == AccessType.FIELD ?
            		AccessCode.FIELD : AccessCode.PROPERTY));
    	}
    }

    private static class MethodKey {

        private final Method _method;

        public MethodKey(Method m) {
            _method = m;
        }

        public int hashCode() {
            int code = 46 * 12 + _method.getName().hashCode();
            for (Class<?> param : _method.getParameterTypes())
                code = 46 * code + param.hashCode();
            return code;
        }

        public boolean equals(Object o) {
            if (!(o instanceof MethodKey))
                return false;
            Method other = ((MethodKey) o)._method;
            if (!_method.getName().equals(other.getName()))
                return false;
            return Arrays.equals(_method.getParameterTypes(),
                other.getParameterTypes());
        }
    }

    private static class MethodComparator implements Comparator<Method> {

        private static MethodComparator INSTANCE = null;

        public static MethodComparator getInstance() {
            if (INSTANCE == null)
                INSTANCE = new MethodComparator();
            return INSTANCE;
        }

        public int compare(Method m1, Method m2) {
            Class<?> c1 = m1.getDeclaringClass();
            Class<?> c2 = m2.getDeclaringClass();
            if (!c1.equals(c2)) {
                if (c1.isAssignableFrom(c2))
                    return -1;
                else
                    return 1;
            }
            int compare = m1.getName().compareTo(m2.getName ());
            if (compare != 0) {
                return compare;
            }

            Class<?>[] params1 = m1.getParameterTypes();
            Class<?>[] params2 = m2.getParameterTypes();
            compare = params1.length - params2.length;
            if (compare != 0) {
                return compare;
            }

            // Just using the Method#hashCode() is not enough as it only contains
            // the hash of the class + the hash of the NAME of the method...
            // Thus if they have the same number of parameters, we need to compare them all
            for (int i = 0; i < params1.length; i++) {
                compare = params1[i].hashCode() - params2[i].hashCode();
                if (compare != 0) {
                    return compare;
                }
            }

            return 0;
        }
    }
    
    /**
     * An internal class used to mimic the FetchGroup annotation.
     * This is needed to process the fetch-group element in xml
     * metadata with common code for the annotation.
     */
    static class FetchGroupImpl {
        private String name = "";
        private boolean postLoad = false;
        private FetchAttributeImpl[] attributes = {};
        private String[] fetchGroups = {};
        
        FetchGroupImpl(String name, boolean postLoad)
        {
            this.name = name;
            this.postLoad = postLoad;
        }

        public String name() {
            return name;
        }

        public boolean postLoad() {
            return postLoad;
        }

        public FetchAttributeImpl[] attributes() {
            return attributes;
        }

        public String[] fetchGroups() {
            return fetchGroups;
        }


        public void setAttributes(FetchAttributeImpl[] attributes) {
            this.attributes = attributes;
        }

        public void setFetchGroups(String[] fetchGroups) {
            this.fetchGroups = fetchGroups;
        }
    }
    
    /**
     * An internal class used to mimic the FetchAttribute annotation.
     * This is needed to process the fetch-attribute element in xml
     * metadata with common code for the annotation.
     */
    static class FetchAttributeImpl {
        private String name = "";
        private int recursionDepth = Integer.MIN_VALUE;
        
        public FetchAttributeImpl(String name, int recursionDepth) {
            this.name = name;
            this.recursionDepth = recursionDepth;
        }
        
        public String name() {
            return name;
        }

        public int recursionDepth() {
            return recursionDepth;
        }

        
    }

    protected String normalizeSequenceName(String seqName) {
        return seqName;
    }

    protected String normalizeSchemaName(String schName) {
        return schName;
    }

    protected String normalizeCatalogName(String catName) {
        return catName;
    }
}

