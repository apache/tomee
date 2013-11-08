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

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.DynamicPersistenceCapable;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PCRegistry.RegisterClassListener;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.util.StringDistance;
import org.apache.openjpa.util.ClassResolver;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.OpenJPAId;

import serp.util.Strings;

/**
 * Repository of and factory for persistent metadata.
 * 
 * @since 0.3.0
 * @author Abe White
 * @author Steve Kim (query metadata)
 */
@SuppressWarnings("serial")
public class MetaDataRepository implements PCRegistry.RegisterClassListener, Configurable, Closeable, MetaDataModes,
    Serializable {

    /**
     * Constant to not validate any metadata.
     */
    public static final int VALIDATE_NONE = 0;

    /**
     * Bit flag to validate metadata.
     */
    public static final int VALIDATE_META = 1;

    /**
     * Bit flag to validate mappings.
     */
    public static final int VALIDATE_MAPPING = 2;

    /**
     * Bit flag to validate unenhanced metadata only.
     */
    public static final int VALIDATE_UNENHANCED = 4;

    /**
     * Bit flag for runtime validation. Requires that all classes are enhanced, and performs extra
     * field resolution steps.
     */
    public static final int VALIDATE_RUNTIME = 8;

    protected static final Class<?>[] EMPTY_CLASSES = new Class[0];
    protected static final NonPersistentMetaData[] EMPTY_NON_PERSISTENT = new NonPersistentMetaData[0];
    protected final ClassMetaData[] EMPTY_METAS;
    protected final FieldMetaData[] EMPTY_FIELDS;
    protected final Order[] EMPTY_ORDERS;

    private static final Localizer _loc = Localizer.forPackage(MetaDataRepository.class);

    // system sequence
    private SequenceMetaData _sysSeq = null;
    // cache of parsed metadata, oid class to class, and interface class
    // to metadatas
    private Map<Class<?>, ClassMetaData> _metas = new HashMap<Class<?>, ClassMetaData>();
    private Map<String, ClassMetaData> _metaStringMap = new ConcurrentHashMap<String, ClassMetaData>();
    private Map<Class<?>, Class<?>> _oids = Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());
    private Map<Class<?>, Collection<Class<?>>> _impls =
        Collections.synchronizedMap(new HashMap<Class<?>, Collection<Class<?>>>());
    private Map<Class<?>, Class<?>> _ifaces = Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());
    private Map<String, QueryMetaData> _queries = new HashMap<String, QueryMetaData>();
    private Map<String, SequenceMetaData> _seqs = new HashMap<String, SequenceMetaData>();
    private Map<String, List<Class<?>>> _aliases = Collections.synchronizedMap(new HashMap<String, List<Class<?>>>());
    private Map<Class<?>, NonPersistentMetaData> _pawares =
        Collections.synchronizedMap(new HashMap<Class<?>, NonPersistentMetaData>());
    private Map<Class<?>, NonPersistentMetaData> _nonMapped =
        Collections.synchronizedMap(new HashMap<Class<?>, NonPersistentMetaData>());
    private Map<Class<?>, Class<?>> _metamodel = Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());

    // map of classes to lists of their subclasses
    private Map<Class<?>, List<Class<?>>> _subs = Collections.synchronizedMap(new HashMap<Class<?>, List<Class<?>>>());

    // xml mapping
    protected final XMLMetaData[] EMPTY_XMLMETAS;
    private final Map<Class<?>, XMLMetaData> _xmlmetas = new HashMap<Class<?>, XMLMetaData>();

    private transient OpenJPAConfiguration _conf = null;
    private transient Log _log = null;
    private transient InterfaceImplGenerator _implGen = null;
    private transient MetaDataFactory _factory = null;

    private int _resMode = MODE_META | MODE_MAPPING;
    private int _sourceMode = MODE_META | MODE_MAPPING | MODE_QUERY;
    private int _validate = VALIDATE_META | VALIDATE_UNENHANCED;

    // we buffer up any classes that register themselves to prevent
    // reentrancy errors if classes register during a current parse (common)
    private final Collection<Class<?>> _registered = new HashSet<Class<?>>();

    // set of metadatas we're in the process of resolving
    private final List<ClassMetaData> _resolving = new ArrayList<ClassMetaData>();
    private final List<ClassMetaData> _mapping = new ArrayList<ClassMetaData>();
    private final List<RuntimeException> _errs = new LinkedList<RuntimeException>();

    // system listeners
    private LifecycleEventManager.ListenerList _listeners = new LifecycleEventManager.ListenerList(3);
    private boolean _systemListenersActivated = false;

    protected boolean _preload = false;
    protected boolean _preloadComplete = false;
    protected boolean _locking = true;
    private static final String PRELOAD_STR = "Preload";
    
    // A boolean used to decide whether or not we need to call to PCEnhancer to check whether we have any down level
    // Entities.
    private boolean _logEnhancementLevel = true;

    // A boolean used to decide whether to filter Class<?> objects submitted by the PCRegistry listener system
    private boolean _filterRegisteredClasses = false;
    
    /**
     * Default constructor. Configure via {@link Configurable}.
     */
    public MetaDataRepository() {
        EMPTY_METAS = newClassMetaDataArray(0);
        EMPTY_FIELDS = newFieldMetaDataArray(0);
        EMPTY_ORDERS = newOrderArray(0);
        EMPTY_XMLMETAS = newXMLClassMetaDataArray(0);
        
    }

    /**
     * Return the configuration for the repository.
     */
    public OpenJPAConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * Return the metadata log.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * The I/O used to load metadata.
     */
    public MetaDataFactory getMetaDataFactory() {
        return _factory;
    }

    /**
     * The I/O used to load metadata.
     */
    public void setMetaDataFactory(MetaDataFactory factory) {
        factory.setRepository(this);
        _factory = factory;
    }

    /**
     * The metadata validation level. Defaults to <code>VALIDATE_META | VALIDATE_UNENHANCED</code>.
     */
    public int getValidate() {
        return _validate;
    }

    /**
     * The metadata validation level. Defaults to <code>VALIDATE_META | VALIDATE_UNENHANCED</code>.
     */
    public void setValidate(int validate) {
        _validate = validate;
    }

    /**
     * The metadata validation level. Defaults to
     * <code>VALIDATE_META | VALIDATE_MAPPING | VALIDATE_UNENHANCED</code>.
     */
    public void setValidate(int validate, boolean on) {
        if (validate == VALIDATE_NONE)
            _validate = validate;
        else if (on)
            _validate |= validate;
        else
            _validate &= ~validate;
    }

    /**
     * The metadata resolution mode. Defaults to <code>MODE_META | MODE_MAPPING</code>.
     */
    public int getResolve() {
        return _resMode;
    }

    /**
     * The metadata resolution mode. Defaults to <code>MODE_META | MODE_MAPPING</code>.
     */
    public void setResolve(int mode) {
        _resMode = mode;
    }

    /**
     * The metadata resolution mode. Defaults to <code>MODE_META | MODE_MAPPING</code>.
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
     * The source mode determining what metadata to load. Defaults to
     * <code>MODE_META | MODE_MAPPING | MODE_QUERY</code>.
     */
    public int getSourceMode() {
        return _sourceMode;
    }

    /**
     * The source mode determining what metadata to load. Defaults to
     * <code>MODE_META | MODE_MAPPING | MODE_QUERY</code>.
     */
    public void setSourceMode(int mode) {
        _sourceMode = mode;
    }

    /**
     * The source mode determining what metadata to load. Defaults to
     * <code>MODE_META | MODE_MAPPING | MODE_QUERY</code>.
     */
    public void setSourceMode(int mode, boolean on) {
        if (mode == MODE_NONE)
            _sourceMode = mode;
        else if (on)
            _sourceMode |= mode;
        else
            _sourceMode &= ~mode;
    }

    /**
     * Sets whether this repository will load all known persistent classes at initialization.
     * Defaults to false.
     */
    public boolean getPreload() {
        return _preload;
    }
    
    /**
     * Sets whether this repository will load all known persistent classes at initialization.
     * Defaults to false.
     */
    public void setPreload(boolean l) {
        _preload = l;
    }


     /**
     * If the openjpa.MetaDataRepository plugin value Preload=true is set, this method will load all
     * MetaData for all persistent classes and will remove locking from this class. 
     */
    public synchronized void preload() {
        if (_preload == false) {
            return;
        }
        // If pooling EMFs, this method may be invoked more than once. Only perform this work once.
        if (_preloadComplete == true) {
            return;
        }


        MultiClassLoader multi = AccessController.doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
        multi.addClassLoader(AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction()));
        multi.addClassLoader(AccessController.doPrivileged(J2DoPrivHelper
            .getClassLoaderAction(MetaDataRepository.class)));
        // If a ClassLoader was passed into Persistence.createContainerEntityManagerFactory on the PersistenceUnitInfo
        // we need to add that loader to the chain of classloaders
        ClassResolver resolver = _conf.getClassResolverInstance();
        if (resolver != null) {
            ClassLoader cl = resolver.getClassLoader(null, null);
            if (cl != null) {
                multi.addClassLoader(cl);
            }
        }

        Set<String> classes = getPersistentTypeNames(false, multi);
        if (classes == null || classes.size() == 0) {
            throw new MetaDataException(_loc.get("repos-initializeEager-none"));
        }
        if (_log.isTraceEnabled() == true) {
            _log.trace(_loc.get("repos-initializeEager-found", classes));
        }

        List<Class<?>> loaded = new ArrayList<Class<?>>();
        for (String c : classes) {
            try {
                Class<?> cls = AccessController.doPrivileged((J2DoPrivHelper.getForNameAction(c, true, multi)));
                loaded.add(cls);
                // This call may be unnecessary?
                _factory.load(cls, MODE_ALL, multi);
            } catch (PrivilegedActionException pae) {
                throw new MetaDataException(_loc.get("repos-initializeEager-error"), pae);
            }
        }
        resolveAll(multi);
        
        // Preload XML MetaData
        for (Class<?> cls : loaded) {
            ClassMetaData cmd = getCachedMetaData(cls);
            if (cmd != null) {
                getXMLMetaData(cls);
                for (FieldMetaData fmd : cmd.getFields()) {
                    getXMLMetaData(fmd.getDeclaredType());
                }
            }
        }
        
        // Hook in this class as a listener and process registered classes list to populate _aliases
        // list.
        PCRegistry.addRegisterClassListener(this);
        processRegisteredClasses(multi);
        _locking = false;
        _preloadComplete = true;
    }

    
    /**
     * Return the metadata for the given class.
     * 
     * @param cls
     *            the class to retrieve metadata for
     * @param envLoader
     *            the environmental class loader, if any
     * @param mustExist
     *            if true, throws a {@link MetaDataException} if no metadata is found
     */
    public ClassMetaData getMetaData(Class<?> cls, ClassLoader envLoader, boolean mustExist) {
        if (_locking) {
            synchronized(this){
                return getMetaDataInternal(cls, envLoader, mustExist);    
            }
        } else {
            return getMetaDataInternal(cls, envLoader, mustExist);
        }
    }

    private ClassMetaData getMetaDataInternal(Class<?> cls, ClassLoader envLoader, boolean mustExist) {
        ClassMetaData meta = getMetaDataInternal(cls, envLoader);
        if (meta == null) {
            if (cls != null && DynamicPersistenceCapable.class.isAssignableFrom(cls))
                cls = cls.getSuperclass();

            // if cls is a generated interface, use the user interface
            // to locate metadata
            if (cls != null && _implGen != null && _implGen.isImplType(cls))
                cls = _implGen.toManagedInterface(cls);
            meta = getMetaDataInternal(cls, envLoader);
        }
        if (meta == null && mustExist) {
            if (cls != null && !ImplHelper.isManagedType(_conf, cls))
                throw new MetaDataException(_loc.get("no-meta-notpc", cls)).setFatal(false);

            Set<String> pcNames = getPersistentTypeNames(false, envLoader);
            if (pcNames != null && pcNames.size() > 0)
                throw new MetaDataException(_loc.get("no-meta-types", cls, pcNames));

            throw new MetaDataException(_loc.get("no-meta", cls));
        }
        resolve(meta);
        return meta;
    }

    /**
     * Return the metadata for the given alias name.
     * 
     * @param alias
     *            the alias to class to retrieve metadata for
     * @param envLoader
     *            the environmental class loader, if any
     * @param mustExist
     *            if true, throws a {@link MetaDataException} if no metadata is found
     * @see ClassMetaData#getTypeAlias
     */
    public ClassMetaData getMetaData(String alias, ClassLoader envLoader, boolean mustExist) {
        if (alias == null && mustExist)
            throw new MetaDataException(_loc.get("no-alias-meta", alias, _aliases));
        if (alias == null)
            return null;

        // check cache
        processRegisteredClasses(envLoader);
        List<Class<?>> classList = _aliases.get(alias);

        // multiple classes may have been defined with the same alias: we
        // will filter by checking against the current list of the
        // persistent types and filter based on which classes are loadable
        // via the current environment's ClassLoader
        Set<String> pcNames = getPersistentTypeNames(false, envLoader);
        Class<?> cls = null;
        for (int i = 0; classList != null && i < classList.size(); i++) {
            Class<?> c = classList.get(i);
            try {
                // re-load the class in the current environment loader so
                // that we can handle redeployment of the same class name
                Class<?> nc = Class.forName(c.getName(), false, envLoader);

                // if we have specified a list of persistent clases,
                // also check to ensure that the class is in that list
                if (pcNames == null || pcNames.size() == 0 || pcNames.contains(nc.getName())) {
                    cls = nc;
                    if (!classList.contains(cls))
                        classList.add(cls);
                    break;
                }
            } catch (Throwable t) {
                // this happens when the class is not loadable by
                // the environment class loader, so it was probably
                // listed elsewhere; also ignore linkage failures and
                // other class loading problems
            }
        }
        if (cls != null)
            return getMetaData(cls, envLoader, mustExist);

        // maybe this is some type we've seen but just isn't valid
        if (_aliases.containsKey(alias)) {
            if (mustExist)
                throwNoRegisteredAlias(alias);
            return null;
        }

        // We need to synchronize on _aliases because a ConcurrentModificationException can if there
        // is a thread in getAliasNames() AND this class isn't using any locking.
        synchronized (_aliases) {
            // record that this is an invalid type
            _aliases.put(alias, null);
        }

        if (!mustExist)
            return null;
        return throwNoRegisteredAlias(alias);
    }

    private ClassMetaData throwNoRegisteredAlias(String alias) {
        String close = getClosestAliasName(alias);
        if (close != null)
            throw new MetaDataException(_loc.get("no-alias-meta-hint", alias, _aliases, close));
        else
            throw new MetaDataException(_loc.get("no-alias-meta", alias, _aliases));
    }

    /**
     * @return the nearest match to the specified alias name
     * @since 1.1.0
     */
    public String getClosestAliasName(String alias) {
        Collection<String> aliases = getAliasNames();
        return StringDistance.getClosestLevenshteinDistance(alias, aliases);
    }

    /**
     * @return the registered alias names
     * @since 1.1.0
     */
    public Collection<String> getAliasNames() {
        if (_locking) {
            synchronized (_aliases) {
                return getAliasNamesInternal();
            }
        } else {
            return getAliasNamesInternal();
        }
    }

    private final Collection<String> getAliasNamesInternal() {
        Collection<String> aliases = new HashSet<String>();
        for(Map.Entry<String, List<Class<?>>> e : _aliases.entrySet()){
            if (e.getValue() != null) {
                aliases.add(e.getKey());
            }            
        }
        return aliases;
    }

    /**
     * Internal method to get the metadata for the given class, without resolving it.
     */
    private ClassMetaData getMetaDataInternal(Class<?> cls, ClassLoader envLoader) {
        if (cls == null)
            return null;

        // check cache for existing metadata, or give up if no metadata and
        // our list of configured persistent types doesn't include the class
        ClassMetaData meta = (ClassMetaData) _metas.get(cls);
        if (meta != null && ((meta.getSourceMode() & MODE_META) != 0 || (_sourceMode & MODE_META) == 0))
            return meta;

        // if runtime, cut off search if not in pc list. we don't do this at
        // dev time so that user can manipulate persistent classes he's writing
        // before adding them to the list
        if ((_validate & VALIDATE_RUNTIME) != 0) {
            Set<String> pcNames = getPersistentTypeNames(false, envLoader);
            if (pcNames != null && !pcNames.contains(cls.getName()))
                return meta;
        }

        if (meta == null) {
            // check to see if maybe we know this class has no metadata
            if (_metas.containsKey(cls))
                return null;

            // make sure this isn't an obviously bad class
            if (cls.isPrimitive() || cls.getName().startsWith("java.") || cls == PersistenceCapable.class)
                return null;

            // designed to get around jikes 1.17 / JDK1.5 issue where static
            // initializers are not invoked when a class is referenced, so the
            // class never registers itself with the system
            if ((_validate & VALIDATE_RUNTIME) != 0) {
                try {
                    Class.forName(cls.getName(), true, AccessController.doPrivileged(J2DoPrivHelper
                        .getClassLoaderAction(cls)));
                } catch (Throwable t) {
                }
            }
        }

        // not in cache: load metadata or mappings depending on source mode.
        // loading metadata might also load mappings, but doesn't have to
        int mode = 0;
        if ((_sourceMode & MODE_META) != 0)
            mode = _sourceMode & ~MODE_MAPPING;
        else if ((_sourceMode & MODE_MAPPING) == 0)
            mode = _sourceMode;
        if (mode != MODE_NONE) {
            if (_log.isTraceEnabled())
                _log.trace(_loc.get("load-cls", cls, toModeString(mode)));
            _factory.load(cls, mode, envLoader);
        }

        // check cache again
        if (meta == null)
            meta = (ClassMetaData) _metas.get(cls);
        if (meta != null && ((meta.getSourceMode() & MODE_META) != 0 || (_sourceMode & MODE_META) == 0))
            return meta;

        // record that this class has no metadata; checking for this later
        // speeds things up in environments with slow class loading
        // like appservers
        if (meta != null)
            removeMetaData(meta);
        _metas.put(cls, null);
        return null;
    }

    /**
     * Return a string representation of the given mode flags.
     */
    private static String toModeString(int mode) {
        StringBuilder buf = new StringBuilder(31);
        if ((mode & MODE_META) != 0)
            buf.append("[META]");
        if ((mode & MODE_QUERY) != 0)
            buf.append("[QUERY]");
        if ((mode & MODE_MAPPING) != 0)
            buf.append("[MAPPING]");
        if ((mode & MODE_MAPPING_INIT) != 0)
            buf.append("[MAPPING_INIT]");
        return buf.toString();
    }

    /**
     * Prepare metadata for mapping resolution. This method might map parts of the metadata that
     * don't rely on other classes being mapped, but that other classes might rely on during their
     * own mapping (for example, primary key fields). By default, this method only calls
     * {@link ClassMetaData#defineSuperclassFields}.
     */
    protected void prepareMapping(ClassMetaData meta) {
        meta.defineSuperclassFields(false);
    }

    /**
     * Resolve the given metadata if needed. There are three goals:
     * <ol>
     * <li>Make sure no unresolved metadata gets back to the client.</li>
     * <li>Avoid infinite reentrant calls for mutually-dependent metadatas by allowing unresolved
     * metadata to be returned to other metadatas.</li>
     * <li>Always make sure the superclass metadata is resolved before the subclass metadata so that
     * the subclass can access the super's list of fields.</li>
     * </ol>
     * Note that the code calling this method is synchronized, so this method doesn't have to be.
     */
    private void resolve(ClassMetaData meta) {
        // return anything that has its metadata resolved, because that means
        // it is either fully resolved or must at least be in the process of
        // resolving mapping, etc since we do that right after meta resolve
        if (meta == null || _resMode == MODE_NONE || (meta.getResolve() & MODE_META) != 0)
            return;

        // resolve metadata
        List<ClassMetaData> resolved = resolveMeta(meta);
        if (resolved == null)
            return;

        // load mapping data
        for (int i = 0; i < resolved.size(); i++)
            loadMapping(resolved.get(i));
        for (int i = 0; i < resolved.size(); i++)
            preMapping(resolved.get(i));

        // resolve mappings
        boolean err = true;
        if ((_resMode & MODE_MAPPING) != 0)
            for (int i = 0; i < resolved.size(); i++)
                err &= resolveMapping(resolved.get(i));

        // throw errors encountered
        // OPENJPA-1535 Always throw a MetaDataException because callers
        // of loadRegisteredClassMetaData expect only MetaDataException
        // to be thrown.
        if (err && !_errs.isEmpty()) {
            RuntimeException re;
            if ((_errs.size() == 1) && (_errs.get(0) instanceof MetaDataException)) {
                re = _errs.get(0);
            } else {
                re = new MetaDataException(_loc.get("resolve-errs"))
                    .setNestedThrowables((Throwable[]) _errs
                    .toArray(new Exception[_errs.size()]));
            }
            _errs.clear();
            throw re;
        }
    }

    /**
     * Resolve metadata mode, returning list of processed metadadatas, or null if we're still in the
     * process of resolving other metadatas.
     */
    private List<ClassMetaData> resolveMeta(ClassMetaData meta) {
        if (meta.getPCSuperclass() == null) {
            // set superclass
            Class<?> sup = meta.getDescribedType().getSuperclass();
            ClassMetaData supMeta;
            while (sup != null && sup != Object.class) {
                supMeta = getMetaData(sup, meta.getEnvClassLoader(), false);
                if (supMeta != null) {
                    meta.setPCSuperclass(sup);
                    meta.setPCSuperclassMetaData(supMeta);
                    break;
                } else
                    sup = sup.getSuperclass();
            }
            if (meta.getDescribedType().isInterface()) {
                Class<?>[] sups = meta.getDescribedType().getInterfaces();
                for (int i = 0; i < sups.length; i++) {
                    supMeta = getMetaData(sups[i], meta.getEnvClassLoader(), false);
                    if (supMeta != null) {
                        meta.setPCSuperclass(sup);
                        meta.setPCSuperclassMetaData(supMeta);
                        break;
                    }
                }
            }
            if (_log.isTraceEnabled())
                _log.trace(_loc.get("assigned-sup", meta, meta.getPCSuperclass()));
        }

        // resolve relation primary key fields for mapping dependencies
        FieldMetaData[] fmds = meta.getDeclaredFields();
        for (int i = 0; i < fmds.length; i++)
            if (fmds[i].isPrimaryKey())
                getMetaData(fmds[i].getDeclaredType(), meta.getEnvClassLoader(), false);

        // resolve metadata; if we're not in the process of resolving
        // others, this will return the set of interrelated metas that
        // resolved
        return processBuffer(meta, _resolving, MODE_META);
    }

    /**
     * Load mapping information for the given metadata.
     */
    private void loadMapping(ClassMetaData meta) {
        if ((meta.getResolve() & MODE_MAPPING) != 0)
            return;

        // load mapping information
        if ((meta.getSourceMode() & MODE_MAPPING) == 0 && (_sourceMode & MODE_MAPPING) != 0) {
            // embedded-only metadata doesn't have mapping, so always loaded
            if (meta.isEmbeddedOnly())
                meta.setSourceMode(MODE_MAPPING, true);
            else {
                // load mapping data
                int mode = _sourceMode & ~MODE_META;
                if (_log.isTraceEnabled())
                    _log.trace(_loc.get("load-mapping", meta, toModeString(mode)));
                try {
                    _factory.load(meta.getDescribedType(), mode, meta.getEnvClassLoader());
                } catch (RuntimeException re) {
                    removeMetaData(meta);
                    _errs.add(re);
                }
            }
        }
    }

    /**
     * Pre-mapping preparation.
     */
    private void preMapping(ClassMetaData meta) {
        if ((meta.getResolve() & MODE_MAPPING) != 0)
            return;

        // prepare mappings for resolve; if not resolving mappings, then
        // make sure any superclass fields defined in metadata are resolved
        try {
            if ((_resMode & MODE_MAPPING) != 0) {
                if (_log.isTraceEnabled())
                    _log.trace(_loc.get("prep-mapping", meta));
                prepareMapping(meta);
            } else
                meta.defineSuperclassFields(false);
        } catch (RuntimeException re) {
            removeMetaData(meta);
            _errs.add(re);
        }
    }

    /**
     * Resolve and initialize mapping.
     * 
     * @return false if we're still in the process of resolving mappings
     */
    private boolean resolveMapping(ClassMetaData meta) {
        List<ClassMetaData> mapped = processBuffer(meta, _mapping, MODE_MAPPING);
        if (mapped == null)
            return false;

        // initialize mapping for runtime use
        if ((_resMode & MODE_MAPPING_INIT) != 0) {
            for (int i = 0; i < mapped.size(); i++) {
                meta = (ClassMetaData) mapped.get(i);
                try {
                    meta.resolve(MODE_MAPPING_INIT);
                } catch (RuntimeException re) {
                    removeMetaData(meta);
                    _errs.add(re);
                }
            }
        }
        return true;
    }

    /**
     * Process the given metadata and the associated buffer.
     */
    private List<ClassMetaData> processBuffer(ClassMetaData meta, List<ClassMetaData> buffer, int mode) {
        // add the metadata to the buffer unless an instance for the same entity
        // is already there
        for (ClassMetaData cmd : buffer)
            if (cmd.getDescribedType().equals(meta.getDescribedType()))
                return null;

        // if we're already processing a metadata, just buffer this one; when
        // the initial metadata finishes processing, we traverse the buffer
        // and process all the others that were introduced during reentrant
        // calls
        buffer.add(meta);
        if (buffer.size() != 1)
            return null;

        // continually pop a metadata and process it until we run out; note
        // that each processing call might place more metas in the buffer as
        // one class tries to access metadata for another
        ClassMetaData buffered;
        List<ClassMetaData> processed = new ArrayList<ClassMetaData>(5);
        while (!buffer.isEmpty()) {
            buffered = buffer.get(0);
            try {
                buffered.resolve(mode);
                processed.add(buffered);
                buffer.remove(buffered);
            } catch (RuntimeException re) {
                _errs.add(re);

                // any exception during resolution of one type means we can't
                // resolve any of the related types, so clear buffer. this also
                // ensures that if two types relate to each other and one
                // dies, we don't get into infinite cycles
                for (ClassMetaData cmd : buffer) {
                    removeMetaData(cmd);
                    if (cmd != buffered) {
                        _errs.add(new MetaDataException(_loc.get("prev-errs", cmd, buffered)));
                    }
                }
                buffer.clear();
            }
        }
        
        return processed;
    }

    /**
     * Return all the metadata instances currently in the repository.
     */
    public ClassMetaData[] getMetaDatas() {
        if (_locking) {
            synchronized(this){
                return getMetaDatasInternal();    
            }
        } else {
            return getMetaDatasInternal();
        }
    }
    
    private ClassMetaData[] getMetaDatasInternal() {
            // prevent concurrent mod errors when resolving one metadata
            // introduces others
            ClassMetaData[] metas = (ClassMetaData[]) _metas.values().toArray(new ClassMetaData[_metas.size()]);
            for (int i = 0; i < metas.length; i++)
                if (metas[i] != null)
                    getMetaData(metas[i].getDescribedType(), metas[i].getEnvClassLoader(), true);

            List<ClassMetaData> resolved = new ArrayList<ClassMetaData>(_metas.size());
            for (ClassMetaData meta : _metas.values()) {
                if (meta != null)
                    resolved.add(meta);
            }
            metas = resolved.toArray(newClassMetaDataArray(resolved.size()));
            Arrays.sort(metas);
            return metas;
    }

    /**
     * Return the cached metadata for the given class, without any resolution. Return null if none.
     */
    public ClassMetaData getCachedMetaData(Class<?> cls) {
        return (ClassMetaData) _metas.get(cls);
    }

    /**
     * Create a new metadata, populate it with default information, add it to the repository, and
     * return it. Use the default access type.
     */
    public ClassMetaData addMetaData(Class<?> cls) {
        return addMetaData(cls, AccessCode.UNKNOWN);
    }

    /**
     * Create a new metadata, populate it with default information, add it to the repository, and
     * return it.
     * 
     * @param access
     *            the access type to use in populating metadata
     */
    public ClassMetaData addMetaData(Class<?> cls, int access) {
        return addMetaData(cls, access, false);
    }
    
    /**
     * Create a new metadata, populate it with default information, add it to the repository, and
     * return it.
     * 
     * @param access
     *            the access type to use in populating metadata
     */
    public ClassMetaData addMetaData(Class<?> cls, int access, boolean ignoreTransient) {
        if (cls == null || cls.isPrimitive())
            return null;

        ClassMetaData meta = newClassMetaData(cls);
        _factory.getDefaults().populate(meta, access, ignoreTransient);

        // synchronize on this rather than the map, because all other methods
        // that access _metas are synchronized on this
        if (_locking) {
            synchronized(this){
                return metasPutInternal(cls, meta);
            }
        } else {
            return metasPutInternal(cls, meta);
        }
            
    }

    private ClassMetaData metasPutInternal(Class<?> cls, ClassMetaData meta){
            if (_pawares.containsKey(cls))
                throw new MetaDataException(_loc.get("pc-and-aware", cls));
            _metas.put(cls, meta);
        return meta;
    }

    /**
     * Create a new class metadata instance.
     */
    protected ClassMetaData newClassMetaData(Class<?> type) {
        return new ClassMetaData(type, this);
    }

    /**
     * Create a new array of the proper class metadata subclass.
     */
    protected ClassMetaData[] newClassMetaDataArray(int length) {
        return new ClassMetaData[length];
    }

    /**
     * Create a new field metadata instance.
     */
    protected FieldMetaData newFieldMetaData(String name, Class<?> type, ClassMetaData owner) {
        return new FieldMetaData(name, type, owner);
    }

    /**
     * Create a new array of the proper field metadata subclass.
     */
    protected FieldMetaData[] newFieldMetaDataArray(int length) {
        return new FieldMetaData[length];
    }

    /**
     * Create a new array of the proper xml class metadata subclass.
     */
    protected XMLMetaData[] newXMLClassMetaDataArray(int length) {
        return new XMLClassMetaData[length];
    }

    /**
     * Create a new embedded class metadata instance.
     */
    protected ClassMetaData newEmbeddedClassMetaData(ValueMetaData owner) {
        return new ClassMetaData(owner);
    }

    /**
     * Create a new value metadata instance.
     */
    protected ValueMetaData newValueMetaData(FieldMetaData owner) {
        return new ValueMetaDataImpl(owner);
    }

    /**
     * Create an {@link Order} for the given field and declaration. This method delegates to
     * {@link #newRelatedFieldOrder} and {@link #newValueFieldOrder} by default.
     */
    protected Order newOrder(FieldMetaData owner, String name, boolean asc) {
        // paths can start with (or equal) '#element'
        if (name.startsWith(Order.ELEMENT))
            name = name.substring(Order.ELEMENT.length());
        if (name.length() == 0)
            return newValueOrder(owner, asc);

        // next token should be '.'
        if (name.charAt(0) == '.')
            name = name.substring(1);

        // related field
        ClassMetaData meta = owner.getElement().getTypeMetaData();
        if (meta == null)
            throw new MetaDataException(_loc.get("nonpc-field-orderable", owner, name));
        FieldMetaData rel = getOrderByField(meta, name);
        if (rel == null)
            throw new MetaDataException(_loc.get("bad-field-orderable", owner, name));
        return newRelatedFieldOrder(owner, rel, asc);
    }

    public FieldMetaData getOrderByField(ClassMetaData meta, String orderBy) {
        FieldMetaData field = meta.getField(orderBy);
        if (field != null)
            return field;
        int dotIdx = orderBy.indexOf(".");
        if (dotIdx == -1)
            return null;
        String fieldName = orderBy.substring(0, dotIdx);
        FieldMetaData field1 = meta.getField(fieldName);
        if (field1 == null)
            return null;
        ClassMetaData meta1 = field1.getEmbeddedMetaData();
        if (meta1 == null)
            return null;
        String mappedBy1 = orderBy.substring(dotIdx + 1);
        return getOrderByField(meta1, mappedBy1);
    }

    /**
     * Order by the field value.
     */
    protected Order newValueOrder(FieldMetaData owner, boolean asc) {
        return new InMemoryValueOrder(asc, getConfiguration());
    }

    /**
     * Order by a field of the related type.
     */
    protected Order newRelatedFieldOrder(FieldMetaData owner, FieldMetaData rel, boolean asc) {
        return new InMemoryRelatedFieldOrder(rel, asc, getConfiguration());
    }

    /**
     * Create an array of orders of the given size.
     */
    protected Order[] newOrderArray(int size) {
        return new Order[size];
    }

    /**
     * Remove a metadata instance from the repository.
     * 
     * @return true if removed, false if not in this repository
     */
    public boolean removeMetaData(ClassMetaData meta) {
        if (meta == null)
            return false;
        return removeMetaData(meta.getDescribedType());
    }

    /**
     * Remove a metadata instance from the repository.
     * 
     * @return true if removed, false if not in this repository
     */
    public boolean removeMetaData(Class<?> cls) {
        if(_locking){
            synchronized(this){
                return removeMetaDataInternal(cls);
            }
        }else{
            return removeMetaDataInternal(cls);
        }
    }

    private boolean removeMetaDataInternal(Class<?> cls) {
            if (cls == null)
                return false;
            if (_metas.remove(cls) != null) {
                Class<?> impl = _ifaces.remove(cls);
                if (impl != null)
                    _metas.remove(impl);
                return true;
            }
            return false;
    }
    /**
     * Add the given metadata as declared interface implementation.
     */
    void addDeclaredInterfaceImpl(ClassMetaData meta, Class<?> iface) {
        if (_locking) {
            synchronized (_impls) {
                addDeclaredInterfaceImplInternal(meta, iface);
            }
        } else {
            addDeclaredInterfaceImplInternal(meta, iface);
        }
    }

   private void addDeclaredInterfaceImplInternal(ClassMetaData meta, Class<?> iface) {
            Collection<Class<?>> vals = _impls.get(iface);

            // check to see if the superclass already declares to avoid dups
            if (vals != null) {
                ClassMetaData sup = meta.getPCSuperclassMetaData();
                for (; sup != null; sup = sup.getPCSuperclassMetaData())
                    if (vals.contains(sup.getDescribedType()))
                        return;
            }
            addToCollection(_impls, iface, meta.getDescribedType(), false);
        }
    /**
     * Set the implementation for the given managed interface.
     */
    void setInterfaceImpl(ClassMetaData meta, Class<?> impl) {
        if (_locking) {
            synchronized (this) {
                setInterfaceImplInternal(meta, impl);
            }
        } else {
            setInterfaceImplInternal(meta, impl);
        }
    }

    private void setInterfaceImplInternal(ClassMetaData meta, Class<?> impl) {
            if (!meta.isManagedInterface())
                throw new MetaDataException(_loc.get("not-managed-interface", meta, impl));
            _ifaces.put(meta.getDescribedType(), impl);
            addDeclaredInterfaceImpl(meta, meta.getDescribedType());
            ClassMetaData sup = meta.getPCSuperclassMetaData();
            while (sup != null) {
                // record superclass interface info while we can as well as we
                // will only register concrete superclass in PCRegistry
                sup.clearSubclassCache();
                addToCollection(_subs, sup.getDescribedType(), impl, true);
                sup = (ClassMetaData) sup.getPCSuperclassMetaData();
        }
    }

    InterfaceImplGenerator getImplGenerator() {
        return _implGen;
    }

    /**
     * Return the least-derived class metadata for the given application identity object.
     * 
     * @param oid
     *            the oid to get the metadata for
     * @param envLoader
     *            the environmental class loader, if any
     * @param mustExist
     *            if true, throws a {@link MetaDataException} if no metadata is found
     */
    public ClassMetaData getMetaData(Object oid, ClassLoader envLoader, boolean mustExist) {
        if (oid == null && mustExist)
            throw new MetaDataException(_loc.get("no-oid-meta", oid, "?", _oids.toString()));
        if (oid == null)
            return null;

        if (oid instanceof OpenJPAId) {
            Class<?> cls = ((OpenJPAId) oid).getType();
            return getMetaData(cls, envLoader, mustExist);
        }

        // check cache
        processRegisteredClasses(envLoader);
        Class<?> cls = _oids.get(oid.getClass());
        if (cls != null)
            return getMetaData(cls, envLoader, mustExist);

        // maybe this is some type we've seen but just isn't valid
        if (_oids.containsKey(oid.getClass())) {
            if (mustExist)
                throw new MetaDataException(_loc.get("no-oid-meta", oid, oid.getClass(), _oids));
            return null;
        }

        // if still not match, register any classes that look similar to the
        // oid class and check again
        resolveIdentityClass(oid);
        if (processRegisteredClasses(envLoader).length > 0) {
            cls = _oids.get(oid.getClass());
            if (cls != null)
                return getMetaData(cls, envLoader, mustExist);
        }

        // record that this is an invalid type
        _oids.put(oid.getClass(), null);

        if (!mustExist)
            return null;
        throw new MetaDataException(_loc.get("no-oid-meta", oid, oid.getClass(), _oids)).setFailedObject(oid);
    }

    /**
     * Make some guesses about the name of a target class for an unknown application identity class.
     */
    private void resolveIdentityClass(Object oid) {
        if (oid == null)
            return;

        Class<?> oidClass = oid.getClass();
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("resolve-identity", oidClass));

        ClassLoader cl = AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(oidClass));
        String className;
        while (oidClass != null && oidClass != Object.class) {
            className = oidClass.getName();

            // we take a brute-force approach: try to load all the class'
            // substrings. this will handle the following common naming cases:
            //
            // com.company.MyClass$ID -> com.company.MyClass
            // com.company.MyClassId -> com.company.MyClass
            // com.company.MyClassOid -> com.company.MyClass
            // com.company.MyClassPK -> com.company.MyClass
            //
            // this isn't the fastest thing possible, but this method will
            // only be called once per JVM per unknown app id class
            for (int i = className.length(); i > 1; i--) {
                if (className.charAt(i - 1) == '.')
                    break;

                try {
                    Class.forName(className.substring(0, i), true, cl);
                } catch (Exception e) {
                } // consume all exceptions
            }

            // move up the OID hierarchy
            oidClass = oidClass.getSuperclass();
        }
    }

    /**
     * Return all least-derived metadatas with some mapped assignable type that implement the given
     * class.
     * 
     * @param cls
     *            the class or interface to retrieve implementors for
     * @param envLoader
     *            the environmental class loader, if any
     * @param mustExist
     *            if true, throws a {@link MetaDataException} if no metadata is found
     */
    public ClassMetaData[] getImplementorMetaDatas(Class<?> cls, ClassLoader envLoader, boolean mustExist) {
        if (cls == null && mustExist)
            throw new MetaDataException(_loc.get("no-meta", cls));
        if (cls == null)
            return EMPTY_METAS;

        // get impls of given interface / abstract class
        loadRegisteredClassMetaData(envLoader);
        Collection<Class<?>> vals = _impls.get(cls);
        ClassMetaData[] mapped = null;
        if (vals != null) {
            if (_locking) {
                synchronized (vals) {
                    mapped = getImplementorMetaDatasInternal(vals, envLoader, mustExist);
                }
            } else {
                mapped = getImplementorMetaDatasInternal(vals, envLoader, mustExist);
            }
        }

        if (mapped == null && mustExist)
            throw new MetaDataException(_loc.get("no-meta", cls));
        if (mapped == null)
            return EMPTY_METAS;
        return mapped;
    }

    private ClassMetaData[] getImplementorMetaDatasInternal(Collection<Class<?>> classes, ClassLoader envLoader,
        boolean mustExist) {
        Collection<ClassMetaData> mapped = new ArrayList<ClassMetaData>(classes.size());
        ClassMetaData meta = null;
        for (Class<?> c : classes) {
            meta = getMetaData(c, envLoader, true);
            if (meta.isMapped() || meta.getMappedPCSubclassMetaDatas().length > 0) {
                mapped.add(meta);
            }
        }
        return mapped.toArray(new ClassMetaData[mapped.size()]);
    }
    /**
     * Gets the metadata corresponding to the given persistence-aware class. Returns null, if the
     * given class is not registered as persistence-aware.
     */
    public NonPersistentMetaData getPersistenceAware(Class<?> cls) {
        return (NonPersistentMetaData) _pawares.get(cls);
    }

    /**
     * Gets all the metadatas for persistence-aware classes
     * 
     * @return empty array if no class has been registered as pers-aware
     */
    public NonPersistentMetaData[] getPersistenceAwares() {
        if (_locking) {
            synchronized (_pawares) {
                return getPersistenceAwaresInternal();
            }
        } else {
            return getPersistenceAwaresInternal();
        }
    }

    private NonPersistentMetaData[] getPersistenceAwaresInternal() {
            if (_pawares.isEmpty())
                return EMPTY_NON_PERSISTENT;
            return (NonPersistentMetaData[]) _pawares.values().toArray(new NonPersistentMetaData[_pawares.size()]);
    }

    /**
     * Add the given class as persistence-aware.
     * 
     * @param cls
     *            non-null and must not alreaddy be added as persitence-capable
     */
    public NonPersistentMetaData addPersistenceAware(Class<?> cls) {
        if (cls == null)
            return null;
        if (_locking) {
            synchronized (this) {
                return addPersistenceAwareInternal(cls);
            }
        } else {
            return addPersistenceAwareInternal(cls);
        }
    }

    private NonPersistentMetaData addPersistenceAwareInternal(Class<?> cls) {
            if (_pawares.containsKey(cls))
                return (NonPersistentMetaData) _pawares.get(cls);
            if (getCachedMetaData(cls) != null)
                throw new MetaDataException(_loc.get("pc-and-aware", cls));
            NonPersistentMetaData meta =
                new NonPersistentMetaData(cls, this, NonPersistentMetaData.TYPE_PERSISTENCE_AWARE);
            _pawares.put(cls, meta);
            return meta;
    }

    /**
     * Remove a persitence-aware class from the repository
     * 
     * @return true if removed
     */
    public boolean removePersistenceAware(Class<?> cls) {
        return _pawares.remove(cls) != null;
    }

    /**
     * Gets the metadata corresponding to the given non-mapped interface. Returns null, if the given
     * interface is not registered as persistence-aware.
     */
    public NonPersistentMetaData getNonMappedInterface(Class<?> iface) {
        return (NonPersistentMetaData) _nonMapped.get(iface);
    }

    /**
     * Gets the corresponding metadatas for all registered, non-mapped interfaces
     * 
     * @return empty array if no non-mapped interface has been registered.
     */
    public NonPersistentMetaData[] getNonMappedInterfaces() {
        if (_locking) {
            synchronized (_nonMapped) {
                return getNonMappedInterfacesInternal();
            }
        } else {
            return getNonMappedInterfacesInternal();
        }
    }

    private NonPersistentMetaData[] getNonMappedInterfacesInternal() {
            if (_nonMapped.isEmpty())
                return EMPTY_NON_PERSISTENT;
            return (NonPersistentMetaData[]) _nonMapped.values().toArray(new NonPersistentMetaData[_nonMapped.size()]);
    }

    /**
     * Add the given non-mapped interface to the repository.
     * 
     * @param iface
     *            the non-mapped interface
     */
    public NonPersistentMetaData addNonMappedInterface(Class<?> iface) {
        if (iface == null)
            return null;
        if (!iface.isInterface())
            throw new MetaDataException(_loc.get("not-non-mapped", iface));
        if (_locking) {
            synchronized (this) {
                return addNonMappedInterfaceInternal(iface);
            }
        } else {
            return addNonMappedInterfaceInternal(iface);
        }
    }
    
    private NonPersistentMetaData addNonMappedInterfaceInternal(Class<?> iface) {
            if (_nonMapped.containsKey(iface))
                return (NonPersistentMetaData) _nonMapped.get(iface);
            if (getCachedMetaData(iface) != null)
                throw new MetaDataException(_loc.get("non-mapped-pc", iface));
            NonPersistentMetaData meta =
                new NonPersistentMetaData(iface, this, NonPersistentMetaData.TYPE_NON_MAPPED_INTERFACE);
            _nonMapped.put(iface, meta);
            return meta;
        }

    /**
     * Remove a non-mapped interface from the repository
     * 
     * @return true if removed
     */
    public boolean removeNonMappedInterface(Class<?> iface) {
        return _nonMapped.remove(iface) != null;
    }

    /**
     * Clear the cache of parsed metadata. This method also clears the internal
     * {@link MetaDataFactory MetaDataFactory}'s cache.
     */
    public void clear() {
            if (_log.isTraceEnabled())
            _log.trace(_loc.get("clear-repos", this));
        if (_locking) {
            synchronized (this) {
                clearInternal();
            }
        } else {
            clearInternal();
        }
    }

    private void clearInternal(){
        // Recreating these datastructures is probably faster than calling clear. Future change?
            _metas.clear();
            _oids.clear();
            _subs.clear();
            _impls.clear();
            _queries.clear();
            _seqs.clear();
            _registered.clear();
            _factory.clear();
            _aliases.clear();
            _pawares.clear();
            _nonMapped.clear();
            _metaStringMap.clear();
    }
    /**
     * Return the set of configured persistent classes, or null if the user did not configure any.
     * 
     * @param devpath
     *            if true, search for metadata files in directories in the classpath if no classes
     *            are configured explicitly
     * @param envLoader
     *            the class loader to use, or null for default
     */
    public Set<String> getPersistentTypeNames(boolean devpath, ClassLoader envLoader) {
        if (_locking) {
            synchronized (this) {
                return getPersistentTypeNamesInternal(devpath, envLoader);
            }
        } else {
            return getPersistentTypeNamesInternal(devpath, envLoader);
        }
    }

    private Set<String> getPersistentTypeNamesInternal(boolean devpath, ClassLoader envLoader) {
        return _factory.getPersistentTypeNames(devpath, envLoader);
    }
    /**
     * Load the persistent classes named in configuration.
     * This ensures that all subclasses and application identity classes of
     * each type are known in advance, without having to rely on the
     * application loading the classes before performing operations that
     * might involve them.
     *
     * @param devpath if true, search for metadata files in directories
     * in the classpath if the no classes are configured explicitly
     * @param envLoader the class loader to use, or null for default
     * @return the loaded classes, or empty collection if none
     */
    public Collection<Class<?>> loadPersistentTypes(boolean devpath, ClassLoader envLoader) {
        return loadPersistentTypes(devpath, envLoader, false);
    }
    /**
     * Load the persistent classes named in configuration. This ensures that all subclasses and
     * application identity classes of each type are known in advance, without having to rely on the
     * application loading the classes before performing operations that might involve them.
     * 
     * @param devpath
     *            if true, search for metadata files in directories in the classpath if the no
     *            classes are configured explicitly
     * @param envLoader
     *            the class loader to use, or null for default
     * @param mustExist
     *            if true then empty list of classes or any unloadable but specified class will
     *            raise an exception.
     * @return the loaded classes, or empty collection if none
     */
    public Collection<Class<?>> loadPersistentTypes(boolean devpath, ClassLoader envLoader, boolean mustExist) {
        if (_locking) {
            synchronized (this) {
                return loadPersistentTypesInternal(devpath, envLoader, mustExist);
            }
        } else {
            return loadPersistentTypesInternal(devpath, envLoader, mustExist);
        }
    }

    private Collection<Class<?>> loadPersistentTypesInternal(boolean devpath, ClassLoader envLoader, 
        boolean mustExist) {
            Set<String> names = getPersistentTypeNames(devpath, envLoader);
            if (names == null || names.isEmpty()) {
                if (!mustExist)
                    return Collections.emptyList();
                else
                    throw new MetaDataException(_loc.get("eager-no-class-found"));
            }

            // attempt to load classes so that they get processed
            ClassLoader clsLoader = _conf.getClassResolverInstance().getClassLoader(getClass(), envLoader);
            List<Class<?>> classes = new ArrayList<Class<?>>(names.size());
            Class<?> cls;
            for (String className : names) {
                cls = classForName(className, clsLoader);
                if (_factory.isMetaClass(cls)) {
                    setMetaModel(cls);
                    continue;
                }
                if (cls != null) {
                    classes.add(cls);

                    // if the class is an interface, load its metadata to kick
                    // off the impl generator
                    if (cls.isInterface())
                        getMetaData(cls, clsLoader, false);
                } else if (cls == null && mustExist) {
                    throw new MetaDataException(_loc.get("eager-class-not-found", className));
                }
            }
            return classes;
    }

    /**
     * Return the class for the given name, or null if not loadable.
     */
    private Class<?> classForName(String name, ClassLoader loader) {
        try {
            return Class.forName(name, true, loader);
        } catch (Exception e) {
            if ((_validate & VALIDATE_RUNTIME) != 0) {
                if (_log.isWarnEnabled())
                    _log.warn(_loc.get("bad-discover-class", name, loader));
            } else if (_log.isInfoEnabled())
                _log.info(_loc.get("bad-discover-class", name, loader));
            if (_log.isTraceEnabled())
                _log.trace(e);
        } catch (NoSuchMethodError nsme) {
            if (nsme.getMessage().indexOf(".pc") == -1)
                throw nsme;

            // if the error is about a method that uses the PersistenceCapable
            // 'pc' method prefix, perform some logging and continue. This
            // probably just means that the class is not yet enhanced.
            if ((_validate & VALIDATE_RUNTIME) != 0) {
                if (_log.isWarnEnabled())
                    _log.warn(_loc.get("bad-discover-class", name, loader));
            } else if (_log.isInfoEnabled())
                _log.info(_loc.get("bad-discover-class", name, loader));
            if (_log.isTraceEnabled())
                _log.trace(nsme);
        }
        return null;
    }

    /**
     * Return all known subclasses for the given class mapping. Note that this method only works
     * during runtime when the repository is registered as a {@link RegisterClassListener}.
     */
    Collection<Class<?>> getPCSubclasses(Class<?> cls) {
        Collection<Class<?>> subs = _subs.get(cls);
        if (subs == null)
            return Collections.emptyList();
        return subs;
    }

    // //////////////////////////////////////
    // RegisterClassListener implementation
    // //////////////////////////////////////

    public void register(Class<?> cls) {
        // buffer registered classes until an oid metadata request is made,
        // at which point we'll parse everything in the buffer
        synchronized (_registered) {
            _registered.add(cls);
            registerAlias(cls);
        }
    }

    /**
     * Parses the metadata for all registered classes.
     */
    private void loadRegisteredClassMetaData(ClassLoader envLoader) {
        Class<?>[] reg = processRegisteredClasses(envLoader);
        for (int i = 0; i < reg.length; i++) {
            try {
                getMetaData(reg[i], envLoader, false);
            } catch (MetaDataException me) {
                if (_log.isWarnEnabled())
                    _log.warn(me);
            }
        }
    }

    /**
     * Updates our datastructures with the latest registered classes.
     */
    Class<?>[] processRegisteredClasses(ClassLoader envLoader) {
        if (_registered.isEmpty())
            return EMPTY_CLASSES;

        // copy into new collection to avoid concurrent mod errors on reentrant
        // registrations
        Class<?>[] reg;
        synchronized (_registered) {
            reg = _registered.toArray(new Class[_registered.size()]);
            _registered.clear();
        }

        Collection<String> pcNames = getPersistentTypeNames(false, envLoader);
        Collection<Class<?>> failed = null;
        for (int i = 0; i < reg.length; i++) {
            // Don't process types that aren't listed by the user; it may belong to a different persistence unit.
            if (pcNames != null && !pcNames.isEmpty() && !pcNames.contains(reg[i].getName())) {
                continue;
            }
            
            // If the compatibility option "filterPCRegistryClasses" is enabled, then verify that the type is
            // accessible to the envLoader/Thread Context ClassLoader
            if (_filterRegisteredClasses) {
                Log log = (_conf == null) ? null : _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
                ClassLoader loadCL = (envLoader != null) ?
                        envLoader :
                        AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
                        
                try {
                    Class<?> classFromAppClassLoader = Class.forName(reg[i].getName(), true, loadCL);
                    
                    if (!reg[i].equals(classFromAppClassLoader)) {
                        // This is a class that belongs to a ClassLoader not associated with the Application,
                        // so it should be processed.
                        if (log != null && log.isTraceEnabled()) {
                            log.trace(
                                "Metadata Repository will ignore Class " + reg[i].getName() + 
                                ", since it originated from a ClassLoader not associated with the application.");
                        }
                        continue;
                    }
                } catch (ClassNotFoundException cnfe) {
                    // Catch exception and log its occurrence, and permit MDR processing to continue to preserve
                    // original behavior.
                    if (log != null && log.isTraceEnabled()) {
                        log.trace("The Class " + reg[i].getName() + " was identified as a persistent class " +
                            "by configuration, but the Class could not be found.");
                    }
                }
            }

            checkEnhancementLevel(reg[i]);
            try {
                processRegisteredClass(reg[i]);
            } catch (Throwable t) {
                if (!_conf.getRetryClassRegistration())
                    throw new MetaDataException(_loc.get("error-registered", reg[i]), t);

                if (_log.isWarnEnabled())
                    _log.warn(_loc.get("failed-registered", reg[i]), t);
                if (failed == null)
                    failed = new ArrayList<Class<?>>();
                failed.add(reg[i]);
            }
        }
        if (failed != null) {
            if (_locking) {
                synchronized (_registered) {
                    _registered.addAll(failed);
                }
            } else {
                _registered.addAll(failed);
            }
        }
        return reg;
    }

    /**
     * Updates our datastructures with the given registered class. Relies on the fact that a child
     * class cannot register itself without also registering its parent class by specifying its
     * persistence capable superclass in the registration event.
     */
    private void processRegisteredClass(Class<?> cls) {
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("process-registered", cls));

        // update subclass lists; synchronize on this because accessing _metas
        // requires it
        Class<?> leastDerived = cls;
        synchronized (this) {
            ClassMetaData meta;
            for (Class<?> anc = cls; (anc = PCRegistry.getPersistentSuperclass(anc)) != null;) {
                addToCollection(_subs, anc, cls, true);
                meta = (ClassMetaData) _metas.get(anc);
                if (meta != null)
                    meta.clearSubclassCache();
                leastDerived = anc;
            }
        }
        
        // update oid mappings if this is a base concrete class
        Object oid = null;
        try {
            oid = PCRegistry.newObjectId(cls);
        } catch (InternalException ie) {
            // thrown for single field identity with null pk field value
        }
        if (oid != null) {
            Class<?> existing = _oids.get(oid.getClass());
            if (existing != null) {
                // if there is already a class for this OID, then we know
                // that multiple classes are using the same OID: therefore,
                // put the least derived PC superclass into the map. This
                // gets around the problem of an abstract PC superclass
                // using application identity (since newObjectId
                // will return null for abstract classes).
                Class<?> sup = cls;
                while (PCRegistry.getPersistentSuperclass(sup) != null)
                    sup = PCRegistry.getPersistentSuperclass(sup);

                _oids.put(oid.getClass(), sup);
            } else if (existing == null || cls.isAssignableFrom(existing))
                _oids.put(oid.getClass(), cls);
        }

        // update mappings from interfaces and non-pc superclasses to
        // pc implementing types
        if (_locking) {
            synchronized (_impls) {
                updateImpls(cls, leastDerived, cls);
            }
        } else {
            updateImpls(cls, leastDerived, cls);
        }

        // set alias for class
        registerAlias(cls);
    }
    
    
    /**
     * Register the given class to the list of known aliases.
     * The alias is registered only if the class has been enhanced.
     * 
     */
    void registerAlias(Class<?> cls) {
        registerAlias(PCRegistry.getTypeAlias(cls), cls);
    }
    
    public void registerAlias(String alias, Class<?> cls) {
        if (alias == null)
            return;
        try {
            if (alias != null) {
                List<Class<?>> classes = _aliases.get(alias);
                if (classes == null)
                    classes = new ArrayList<Class<?>>(3);
                if (!classes.contains(cls)) {
                    classes.add(cls);
                    _aliases.put(alias, classes);
                }
            }
        } catch (IllegalStateException ise) {
            // the class has not been registered to PCRegistry
        }
    }

    /**
     * Update the list of implementations of base classes and interfaces.
     */
    private void updateImpls(Class<?> cls, Class<?> leastDerived, Class<?> check) {
        // allow users to query on common non-pc superclasses
        Class<?> sup = check.getSuperclass();
        if (leastDerived == cls && sup != null && sup != Object.class) {
            addToCollection(_impls, sup, cls, false);
            updateImpls(cls, leastDerived, sup);
        }

        // allow users to query on any implemented interfaces unless defaults
        // say the user must implement persistent interfaces explicitly in meta
        if (!_factory.getDefaults().isDeclaredInterfacePersistent())
            return;
        Class<?>[] ints = check.getInterfaces();
        for (int i = 0; i < ints.length; i++) {
            // don't map java-standard interfaces
            if (ints[i].getName().startsWith("java."))
                continue;

            // only map least-derived interface implementors
            if (leastDerived == cls || isLeastDerivedImpl(ints[i], cls)) {
                addToCollection(_impls, ints[i], cls, false);
                updateImpls(cls, leastDerived, ints[i]);
            }
        }
    }

    /**
     * Return true if the given class is the least-derived persistent implementor of the given
     * interface, false otherwise.
     */
    private boolean isLeastDerivedImpl(Class<?> inter, Class<?> cls) {
        Class<?> parent = PCRegistry.getPersistentSuperclass(cls);
        while (parent != null) {
            if (Arrays.asList(parent.getInterfaces()).contains(inter))
                return false;
            parent = PCRegistry.getPersistentSuperclass(parent);
        }
        return true;
    }

    /**
     * Add the given value to the collection cached in the given map under the given key.
     */
    private void addToCollection(Map map, Class<?> key, Class<?> value, boolean inheritance) {
        if (_locking) {
            synchronized (map) {
                addToCollectionInternal(map, key, value, inheritance);
            }
        } else {
            addToCollectionInternal(map, key, value, inheritance);
        }
    }

    private void addToCollectionInternal(Map map, Class<?> key, Class<?> value, boolean inheritance) {
        Collection coll = (Collection) map.get(key);
        if (coll == null) {
            if (inheritance) {
                InheritanceComparator comp = new InheritanceComparator();
                comp.setBase(key);
                coll = new TreeSet<Class<?>>(comp);
            } else
                coll = new LinkedList<Class<?>>();
            map.put(key, coll);
        }
        coll.add(value);
    }

    /**
     * Puts the meta class corresponding to the given entity class.
     */
    public void setMetaModel(Class<?> m2) {
        Class<?> cls = _factory.getManagedClass(m2);
        if (cls != null)
            _metamodel.put(cls, m2);
    }

    /**
     * Puts the meta class corresponding to the given persistent class.
     */
    public void setMetaModel(ClassMetaData meta, Class<?> m2) {
        _metamodel.put(meta.getDescribedType(), m2);
    }

    /**
     * Gets the meta class corresponding to the given persistent class.
     */
    public Class<?> getMetaModel(ClassMetaData meta, boolean load) {
        return getMetaModel(meta.getDescribedType(), load);
    }

    /**
     * Gets the meta class corresponding to the given class. If load is false, returns the meta
     * class if has been set for the given persistent class earlier. If the load is true then also
     * attempts to apply the current naming policy to derive meta class name and attempts to load
     * the meta class.
     */
    public Class<?> getMetaModel(Class<?> entity, boolean load) {
        if (_metamodel.containsKey(entity))
            return _metamodel.get(entity);
        String m2 = _factory.getMetaModelClassName(entity.getName());
        try {
            ClassLoader loader = AccessController.doPrivileged(J2DoPrivHelper.getClassLoaderAction(entity));
            Class<?> m2cls = AccessController.doPrivileged(J2DoPrivHelper.getForNameAction(m2, true, loader));
            _metamodel.put(entity, m2cls);
            return m2cls;
        } catch (Throwable t) {
            if (_log.isTraceEnabled())
                _log.warn(_loc.get("meta-no-model", m2, entity, t));
        }
        return null;
    }

    // /////////////////////////////
    // Configurable implementation
    // /////////////////////////////

    public void setConfiguration(Configuration conf) {
        _conf = (OpenJPAConfiguration) conf;
        _log = _conf.getLog(OpenJPAConfiguration.LOG_METADATA);
        _filterRegisteredClasses = _conf.getCompatibilityInstance().getFilterPCRegistryClasses();
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        initializeMetaDataFactory();
		if (_implGen == null)
			_implGen = new InterfaceImplGenerator(this);
        if (_preload == true) {
            _oids = new HashMap<Class<?>, Class<?>>();
            _impls = new HashMap<Class<?>, Collection<Class<?>>>();
            _ifaces = new HashMap<Class<?>, Class<?>>();
            _aliases = new HashMap<String, List<Class<?>>>();
            _pawares = new HashMap<Class<?>, NonPersistentMetaData>();
            _nonMapped = new HashMap<Class<?>, NonPersistentMetaData>();
            _subs = new HashMap<Class<?>, List<Class<?>>>();
            // Wait till we're done loading MetaData to flip _lock boolean.
        }            
    }

    private void initializeMetaDataFactory() {
        if (_factory == null) {
            MetaDataFactory mdf = _conf.newMetaDataFactoryInstance();
            if (mdf == null)
                throw new MetaDataException(_loc.get("no-metadatafactory"));
            setMetaDataFactory(mdf);
        }
    }

    // ////////////////
    // Query metadata
    // ////////////////

    /**
     * Return query metadata for the given class, name, and classloader.
     */
    public QueryMetaData getQueryMetaData(Class<?> cls, String name, ClassLoader envLoader, boolean mustExist) {
        if (_locking) {
            synchronized (this) {
                return getQueryMetaDataInternal(cls, name, envLoader, mustExist);
            }
        } else {
            return getQueryMetaDataInternal(cls, name, envLoader, mustExist);
        }
    }

    private QueryMetaData getQueryMetaDataInternal(Class<?> cls, String name, ClassLoader envLoader, 
        boolean mustExist) {
            QueryMetaData meta = getQueryMetaDataInternal(cls, name, envLoader);
            if (meta == null) {
                // load all the metadatas for all the known classes so that
                // query names are seen and registered
                resolveAll(envLoader);
                meta = getQueryMetaDataInternal(cls, name, envLoader);
            }

            if (meta == null && mustExist) {
                if (cls == null) {
                    throw new MetaDataException(_loc.get("no-named-query-null-class", getPersistentTypeNames(false,
                        envLoader), name));
                } else {
                    throw new MetaDataException(_loc.get("no-named-query", cls, name));
                }
            }

            return meta;
    }

    /**
     * Resolve all known metadata classes.
     */
    private void resolveAll(ClassLoader envLoader) {
        Collection<Class<?>> types = loadPersistentTypes(false, envLoader);
        for (Class<?> c : types) {
            getMetaData(c, envLoader, false);
        }
    }

    /**
     * Return query metadata for the given class, name, and classloader.
     */
    private QueryMetaData getQueryMetaDataInternal(Class<?> cls, String name, ClassLoader envLoader) {
        if (name == null)
            return null;
        QueryMetaData qm = null;
        if (cls == null) {
            qm = searchQueryMetaDataByName(name);
            if (qm != null)
                return qm;
        }
        // check cache
        qm = (QueryMetaData) _queries.get(name);
        if (qm != null)
            return qm;

        // get metadata for class, which will find queries in metadata file
        if (cls != null && getMetaData(cls, envLoader, false) != null) {
            qm = _queries.get(name);
            if (qm != null)
                return qm;
        }
        if ((_sourceMode & MODE_QUERY) == 0)
            return null;

        // see if factory can figure out a scope for this query
        if (cls == null)
            cls = _factory.getQueryScope(name, envLoader);

        // not in cache; load
        _factory.load(cls, MODE_QUERY, envLoader);
        return _queries.get(name);
    }

    /**
     * Return the cached query metadata.
     */
    public QueryMetaData[] getQueryMetaDatas() {
        if (_locking) {
            synchronized (this) {
                return (QueryMetaData[]) _queries.values().toArray(new QueryMetaData[_queries.size()]);
            }
        } else {
            return (QueryMetaData[]) _queries.values().toArray(new QueryMetaData[_queries.size()]);
        }
    }
    

    public QueryMetaData getCachedQueryMetaData(Class<?> cls, String name) {
        return getCachedQueryMetaData(name);
    }

    /**
     * Return the cached query metadata for the given name.
     */
    public QueryMetaData getCachedQueryMetaData(String name) {
        if (_locking) {
            synchronized (this) {
                return (QueryMetaData) _queries.get(name);
            }
        } else {
            return (QueryMetaData) _queries.get(name);
        }
    }

    /**
     * Add a new query metadata to the repository and return it.
     */
    public QueryMetaData addQueryMetaData(Class<?> cls, String name) {
        if (_locking) {
            synchronized (this) {
                QueryMetaData meta = newQueryMetaData(cls, name);
                _queries.put(name, meta);
                return meta;
            }
        }else{
            QueryMetaData meta = newQueryMetaData(cls, name);
            _queries.put(name, meta);
            return meta;   
        }
    }

    /**
     * Create a new query metadata instance.
     */
    protected QueryMetaData newQueryMetaData(Class<?> cls, String name) {
        QueryMetaData meta =
            new QueryMetaData(name, _conf.getCompatibilityInstance().getConvertPositionalParametersToNamed());
        meta.setDefiningType(cls);
        return meta;
    }

    /**
     * Remove the given query metadata from the repository.
     */
    public boolean removeQueryMetaData(QueryMetaData meta) {
        if (meta == null)
            return false;
        if (_locking) {
            synchronized (this) {
                return _queries.remove(meta.getName()) != null;
            }
        } else {
            return _queries.remove(meta.getName()) != null;
        }
    }

    /**
     * Remove query metadata for the given class name if in the repository.
     */
    public boolean removeQueryMetaData(Class<?> cls, String name) {
        if (_locking) {
            synchronized (this) {
                if (name == null)
                    return false;
                return _queries.remove(name) != null;
            }
        } else {
            if (name == null)
                return false;
            return _queries.remove(name) != null;
        }
    }

    /**
     * Searches all cached query metadata by name.
     */
    public QueryMetaData searchQueryMetaDataByName(String name) {        
        return (QueryMetaData) _queries.get(name);
    }
    
    /**
     * Return a unique key for a given class / name. The class argument can be null.
     */
    protected static Object getQueryKey(Class<?> cls, String name) {
        if (cls == null)
            return name;
        QueryKey key = new QueryKey();
        key.clsName = cls.getName();
        key.name = name;
        return key;
    }

    // ///////////////////
    // Sequence metadata
    // ///////////////////

    /**
     * Return sequence metadata for the given name and classloader.
     */
    public SequenceMetaData getSequenceMetaData(String name, ClassLoader envLoader, boolean mustExist) {
        if (_locking) {
            synchronized (this) {
                return getSequenceMetaDataInternal(name, envLoader, mustExist);
            }
        } else {
            return getSequenceMetaDataInternal(name, envLoader, mustExist);
        }
    }

    private SequenceMetaData getSequenceMetaDataInternal(String name, ClassLoader envLoader, boolean mustExist) {
            SequenceMetaData meta = getSequenceMetaDataInternal(name, envLoader);
            if (meta == null && SequenceMetaData.NAME_SYSTEM.equals(name)) {
                if (_sysSeq == null)
                    _sysSeq = newSequenceMetaData(name);
                return _sysSeq;
            }
            if (meta == null && mustExist)
                throw new MetaDataException(_loc.get("no-named-sequence", name));
            return meta;
    }

    /**
     * Used internally by metadata to retrieve sequence metadatas based on possibly-unqualified
     * sequence name.
     */
    SequenceMetaData getSequenceMetaData(ClassMetaData context, String name, boolean mustExist) {
        // try with given name
        MetaDataException e = null;
        try {
            SequenceMetaData seq = getSequenceMetaData(name, context.getEnvClassLoader(), mustExist);
            if (seq != null)
                return seq;
        } catch (MetaDataException mde) {
            e = mde;
        }

        // if given name already fully qualified, give up
        if (name.indexOf('.') != -1) {
            if (e != null)
                throw e;
            return null;
        }

        // try with qualified name
        name = Strings.getPackageName(context.getDescribedType()) + "." + name;
        try {
            return getSequenceMetaData(name, context.getEnvClassLoader(), mustExist);
        } catch (MetaDataException mde) {
            // throw original exception
            if (e != null)
                throw e;
            throw mde;
        }
    }

    /**
     * Return sequence metadata for the given name and classloader.
     */
    private SequenceMetaData getSequenceMetaDataInternal(String name, ClassLoader envLoader) {
        if (name == null)
            return null;

        // check cache
        SequenceMetaData meta = _seqs.get(name);
        if (meta == null) {
            // load metadata for registered classes to hopefully find sequence
            // definition
            loadRegisteredClassMetaData(envLoader);
            meta = _seqs.get(name);
        }
        return meta;
    }

    /**
     * Return the cached sequence metadata.
     */
    public SequenceMetaData[] getSequenceMetaDatas() {
        if (_locking) {
            synchronized (this) {
                return (SequenceMetaData[]) _seqs.values().toArray(new SequenceMetaData[_seqs.size()]);
            }
        } else {
            return (SequenceMetaData[]) _seqs.values().toArray(new SequenceMetaData[_seqs.size()]);
        }
    }

    /**
     * Return the cached a sequence metadata for the given name.
     */
    public SequenceMetaData getCachedSequenceMetaData(String name) {
        if (_locking) {
            synchronized (this) {
                return (SequenceMetaData) _seqs.get(name);
            }
        } else {
            return (SequenceMetaData) _seqs.get(name);
        }
    }

    /**
     * Add a new sequence metadata to the repository and return it.
     */
    public SequenceMetaData addSequenceMetaData(String name) {
        if (_locking) {
            synchronized (this) {
                SequenceMetaData meta = newSequenceMetaData(name);
                _seqs.put(name, meta);
                return meta;
            }
        } else {
            SequenceMetaData meta = newSequenceMetaData(name);
            _seqs.put(name, meta);
            return meta;
        }
    }

    /**
     * Create a new sequence metadata instance.
     */
    protected SequenceMetaData newSequenceMetaData(String name) {
        return new SequenceMetaData(name, this);
    }

    /**
     * Remove the given sequence metadata from the repository.
     */
    public boolean removeSequenceMetaData(SequenceMetaData meta) {
        if (meta == null)
            return false;
        if (_locking) {
            synchronized (this) {
                return _seqs.remove(meta.getName()) != null;
            }
        } else {
            return _seqs.remove(meta.getName()) != null;
        }
    }

    /**
     * Remove sequence metadata for the name if in the repository.
     */
    public boolean removeSequenceMetaData(String name) {
        if (name == null)
            return false;
        if (_locking) {
            synchronized (this) {
                return _seqs.remove(name) != null;
            }
        } else {
            return _seqs.remove(name) != null;
        }
    }

    /**
     * Whether any system (default) listeners have been registered.  Used as a quick test to
     * determine whether the callback/listener mechanism has been enabled.
     * @return boolean
     */
    public boolean is_systemListenersActivated() {
        return _systemListenersActivated;
    }

    /**
     * Add the given system lifecycle listener.
     */
    public void addSystemListener(Object listener) {
        if (_locking) {
            synchronized (this) {
                // copy to avoid issues with ListenerList and avoid unncessary
                // locking on the list during runtime
                LifecycleEventManager.ListenerList listeners = new LifecycleEventManager.ListenerList(_listeners);
                listeners.add(listener);
                _listeners = listeners;
                _systemListenersActivated = true;
            }
        } else {
            LifecycleEventManager.ListenerList listeners = new LifecycleEventManager.ListenerList(_listeners);
            listeners.add(listener);
            _listeners = listeners;
            _systemListenersActivated = true;
        }
    }

    /**
     * Remove the given system lifecycle listener.
     */
    public boolean removeSystemListener(Object listener) {
        if (_locking) {
            synchronized (this) {
                return removeSystemListenerInternal(listener);
            }
        } else {
            return removeSystemListenerInternal(listener);
        }
    }

    private boolean removeSystemListenerInternal(Object listener) {
            if (!_listeners.contains(listener))
                return false;

            // copy to avoid issues with ListenerList and avoid unncessary
            // locking on the list during runtime
            LifecycleEventManager.ListenerList listeners = new LifecycleEventManager.ListenerList(_listeners);
            listeners.remove(listener);
            _listeners = listeners;
            return true;
    }

    /**
     * Return the system lifecycle listeners
     */
    public LifecycleEventManager.ListenerList getSystemListeners() {
        return _listeners;
    }

    /**
     * Free the resources used by this repository. Closes all user sequences.
     */
    public void close() {
        if (_locking) {
            synchronized (this) {
                closeInternal();
            }
        } else {
            closeInternal();
        }
    }

    private void closeInternal() {
            SequenceMetaData[] smds = getSequenceMetaDatas();
            for (int i = 0; i < smds.length; i++)
                smds[i].close();
            clear();
    }

    /**
     * Query key struct.
     */
    private static class QueryKey implements Serializable {

        public String clsName;
        public String name;

        public int hashCode() {
            int clsHash = (clsName == null) ? 0 : clsName.hashCode();
            int nameHash = (name == null) ? 0 : name.hashCode();
            return clsHash + nameHash;
        }

        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof QueryKey))
                return false;

            QueryKey qk = (QueryKey) obj;
            return StringUtils.equals(clsName, qk.clsName) && StringUtils.equals(name, qk.name);
        }
    }

    /**
     * Return XML metadata for a given field metadata
     * 
     * @param fmd
     * @return XML metadata
     */
    public XMLMetaData getXMLMetaData(Class<?> cls) {
        if (_locking) {
            synchronized (this) {
                return getXMLMetaDataInternal(cls);
            }
        } else {
            return getXMLMetaDataInternal(cls);
        }
    }
    
    private XMLMetaData getXMLMetaDataInternal(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        // check if cached before
        XMLMetaData xmlmeta = _xmlmetas.get(cls);
        if (xmlmeta != null)
            return xmlmeta;

        // load JAXB XML metadata
        _factory.loadXMLMetaData(cls);

        xmlmeta = (XMLClassMetaData) _xmlmetas.get(cls);

        return xmlmeta;
    }

    /**
     * Create a new metadata, populate it with default information, add it to the repository, and
     * return it.
     * 
     * @param access
     *            the access type to use in populating metadata
     */
    public XMLClassMetaData addXMLClassMetaData(Class<?> type) {
        XMLClassMetaData meta = newXMLClassMetaData(type);
        if(_locking){
            synchronized(this){
                _xmlmetas.put(type, meta);                
            }
        }else{
            _xmlmetas.put(type, meta);
        }
        return meta;
    }

    /**
     * Return the cached XMLClassMetaData for the given class Return null if none.
     */
    public XMLMetaData getCachedXMLMetaData(Class<?> cls) {
        return _xmlmetas.get(cls);
    }

    /**
     * Create a new xml class metadata
     * 
     * @param type
     * @param name
     * @return a XMLClassMetaData
     */
    protected XMLClassMetaData newXMLClassMetaData(Class<?> type) {
        return new XMLClassMetaData(type);
    }

    /**
     * Create a new xml field meta, add it to the fieldMap in the given xml class metadata
     * 
     * @param type
     * @param name
     * @param meta
     * @return a XMLFieldMetaData
     */
    public XMLFieldMetaData newXMLFieldMetaData(Class<?> type, String name) {
        return new XMLFieldMetaData(type, name);
    }

    public static boolean needsPreload(OpenJPAConfiguration conf) {
        if (conf == null)
            return false;
        Options o = Configurations.parseProperties(Configurations.getProperties(conf.getMetaDataRepository()));
        if (o.getBooleanProperty(PRELOAD_STR) == true || o.getBooleanProperty(PRELOAD_STR.toLowerCase()) == true) {
            return true;
        }
        return false;
    }

    /**
     * This private worker ensures that a message is logged when an Entity is enhanced by a version of the enhancer that
     * is older than the current version.
     */
    private void checkEnhancementLevel(Class<?> cls) {
        if (_logEnhancementLevel == false) {
            return;
        }
        Log log = _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
        boolean res = PCEnhancer.checkEnhancementLevel(cls, _conf.getLog(OpenJPAConfiguration.LOG_RUNTIME));
        if (log.isTraceEnabled() == false && res == true) {
            // Since trace isn't enabled flip the flag so we only log this once.
            _logEnhancementLevel = false;
            log.info(_loc.get("down-level-entity"));
        }
    }
    
    /**
     * This method returns the ClassMetaData whose described type name matches the typeName parameter. It ONLY operates
     * against MetaData that is currently known by this repository. Note: This method call WILL NOT resolve any
     * metadata.
     */
    public ClassMetaData getCachedMetaData(String typeName) {
        ClassMetaData cmd = _metaStringMap.get(typeName);
        if (cmd == null) {
            for (ClassMetaData c : getMetaDatas()) {
                if (c.getDescribedType().getName().equals(typeName)) {
                    _metaStringMap.put(typeName, c);
                    return c;
                }
            }
        }
        return cmd;
    }
}
