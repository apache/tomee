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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.CascadeType;
import javax.persistence.GenerationType;
import javax.persistence.LockModeType;

import static javax.persistence.CascadeType.*;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.BeanLifecycleCallbacks;
import org.apache.openjpa.event.LifecycleCallbacks;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.MethodLifecycleCallbacks;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.CFMetaDataParser;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.meta.XMLVersionParser;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.DelegatingMetaDataFactory;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.LifecycleMetaData;
import org.apache.openjpa.meta.MetaDataContext;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.UpdateStrategies;

import static org.apache.openjpa.meta.MetaDataModes.*;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.Order;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.AnnotationPersistenceMetaDataParser.FetchAttributeImpl;
import org.apache.openjpa.persistence.AnnotationPersistenceMetaDataParser.FetchGroupImpl;

import static org.apache.openjpa.persistence.MetaDataTag.*;
import static org.apache.openjpa.persistence.PersistenceStrategy.*;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * Custom SAX parser used by the system to quickly parse persistence
 * metadata files. This parser may invoke
 * {@linkplain AnnotationPersistenceMetaDataParser another parser} to scan
 * source code annotation.
 *
 * @author Steve Kim
 * @author Pinaki Poddar
 * @nojavadoc
 */
public class XMLPersistenceMetaDataParser
    extends CFMetaDataParser
    implements PersistenceMetaDataFactory.Parser {

    // parse constants
    protected static final String ELEM_PKG = "package";
    protected static final String ELEM_ACCESS = "access";
    protected static final String ELEM_ATTRS = "attributes";
    protected static final String ELEM_LISTENER = "entity-listener";
    protected static final String ELEM_CASCADE = "cascade";
    protected static final String ELEM_CASCADE_ALL = "cascade-all";
    protected static final String ELEM_CASCADE_PER = "cascade-persist";
    protected static final String ELEM_CASCADE_MER = "cascade-merge";
    protected static final String ELEM_CASCADE_REM = "cascade-remove";
    protected static final String ELEM_CASCADE_REF = "cascade-refresh";
    protected static final String ELEM_CASCADE_DET = "cascade-detach";
    protected static final String ELEM_PU_META = "persistence-unit-metadata";
    protected static final String ELEM_PU_DEF = "persistence-unit-defaults";
    protected static final String ELEM_XML_MAP_META_COMPLETE = "xml-mapping-metadata-complete";
    protected static final String ELEM_DELIM_IDS = "delimited-identifiers";
    
    // The following is needed for input into the delimitString() method
    protected static enum localDBIdentifiers {
        SEQUENCE_GEN_SEQ_NAME,
        SEQUENCE_GEN_SCHEMA
    }    

    private static final Map<String, Object> _elems =
        new HashMap<String, Object>();

    // Map for storing deferred metadata which needs to be populated
    // after embeddables are loaded.
    private static final Map<Class<?>, ArrayList<MetaDataContext>>
        _embeddables = new HashMap<Class<?>, ArrayList<MetaDataContext>>();
    private static final Map<Class<?>, Integer>
        _embeddableAccess = new HashMap<Class<?>, Integer>();
    
    // Hold fetch group info
    private FetchGroupImpl[] _fgs = null;
    private List<FetchGroupImpl> _fgList = null;
    private List<String> _referencedFgList = null;
    private FetchGroupImpl _currentFg = null;
    private List<FetchAttributeImpl> _fetchAttrList = null;

    static {
        _elems.put(ELEM_PKG, ELEM_PKG);
        _elems.put(ELEM_ACCESS, ELEM_ACCESS);
        _elems.put(ELEM_ATTRS, ELEM_ATTRS);
        _elems.put(ELEM_LISTENER, ELEM_LISTENER);
        _elems.put(ELEM_CASCADE, ELEM_CASCADE);
        _elems.put(ELEM_CASCADE_ALL, ELEM_CASCADE_ALL);
        _elems.put(ELEM_CASCADE_PER, ELEM_CASCADE_PER);
        _elems.put(ELEM_CASCADE_REM, ELEM_CASCADE_REM);
        _elems.put(ELEM_CASCADE_MER, ELEM_CASCADE_MER);
        _elems.put(ELEM_CASCADE_REF, ELEM_CASCADE_REF);
        _elems.put(ELEM_CASCADE_DET, ELEM_CASCADE_DET);
        _elems.put(ELEM_PU_META, ELEM_PU_META);
        _elems.put(ELEM_PU_DEF, ELEM_PU_DEF);
        _elems.put(ELEM_XML_MAP_META_COMPLETE, ELEM_XML_MAP_META_COMPLETE);
        _elems.put(ELEM_DELIM_IDS, ELEM_DELIM_IDS);

        _elems.put("entity-listeners", ENTITY_LISTENERS);
        _elems.put("pre-persist", PRE_PERSIST);
        _elems.put("post-persist", POST_PERSIST);
        _elems.put("pre-remove", PRE_REMOVE);
        _elems.put("post-remove", POST_REMOVE);
        _elems.put("pre-update", PRE_UPDATE);
        _elems.put("post-update", POST_UPDATE);
        _elems.put("post-load", POST_LOAD);
        _elems.put("exclude-default-listeners", EXCLUDE_DEFAULT_LISTENERS);
        _elems.put("exclude-superclass-listeners",
            EXCLUDE_SUPERCLASS_LISTENERS);

        _elems.put("named-query", QUERY);
        _elems.put("named-native-query", NATIVE_QUERY);
        _elems.put("query-hint", QUERY_HINT);
        _elems.put("query", QUERY_STRING);

        _elems.put("flush-mode", FLUSH_MODE);
        _elems.put("sequence-generator", SEQ_GENERATOR);

        _elems.put("id", ID);
        _elems.put("id-class", ID_CLASS);
        _elems.put("embedded-id", EMBEDDED_ID);
        _elems.put("maps-id", MAPPED_BY_ID);
        _elems.put("version", VERSION);
        _elems.put("generated-value", GENERATED_VALUE);
        _elems.put("map-key", MAP_KEY);
        _elems.put("order-by", ORDER_BY);
        _elems.put("order-column", ORDER_COLUMN);
        _elems.put("lob", LOB);
        _elems.put("data-store-id", DATASTORE_ID);
        _elems.put("data-cache", DATA_CACHE);

        _elems.put("basic", BASIC);
        _elems.put("many-to-one", MANY_ONE);
        _elems.put("one-to-one", ONE_ONE);
        _elems.put("embedded", EMBEDDED);
        _elems.put("one-to-many", ONE_MANY);
        _elems.put("many-to-many", MANY_MANY);
        _elems.put("transient", TRANSIENT);
        _elems.put("element-collection", ELEM_COLL);
        _elems.put("persistent", PERS);
        _elems.put("persistent-collection", PERS_COLL);
        _elems.put("persistent-map", PERS_MAP);
        _elems.put("map-key-class", MAP_KEY_CLASS);
        
        _elems.put("read-only", READ_ONLY);
        _elems.put("external-values", EXTERNAL_VALS);
        _elems.put("external-value", EXTERNAL_VAL);
        _elems.put("externalizer", EXTERNALIZER);
        _elems.put("factory", FACTORY);
        
        _elems.put("fetch-groups", FETCH_GROUPS);
        _elems.put("fetch-group", FETCH_GROUP);
        _elems.put("fetch-attribute", FETCH_ATTRIBUTE);
        _elems.put("referenced-fetch-group", REFERENCED_FETCH_GROUP);
        
        _elems.put("openjpa-version", OPENJPA_VERSION);
}

    private static final Localizer _loc = Localizer.forPackage
        (XMLPersistenceMetaDataParser.class);

    private final OpenJPAConfiguration _conf;
    private MetaDataRepository _repos = null;
    private AnnotationPersistenceMetaDataParser _parser = null;
    private ClassLoader _envLoader = null;
    private int _mode = MODE_NONE;
    private boolean _override = false;

    private final Stack<Object> _elements = new Stack<Object>();
    private final Stack<Object> _parents = new Stack<Object>();
    
    private StringBuffer _externalValues = null;

    protected Class<?> _cls = null;
    // List of classes currently being parsed
    private ArrayList<Class<?>> _parseList = new ArrayList<Class<?>>();
    private int _fieldPos = 0;
    private int _clsPos = 0;
    private int _access = AccessCode.UNKNOWN;
    private PersistenceStrategy _strategy = null;
    private Set<CascadeType> _cascades = null;
    private Set<CascadeType> _pkgCascades = null;

    private Class<?> _listener = null;
    private Collection<LifecycleCallbacks>[] _callbacks = null;
    private Collection<Class<?>> _listeners = null;
    private int[] _highs = null;
    private boolean _isXMLMappingMetaDataComplete = false;

    private String _ormVersion;
    private String _schemaLocation;

    private static final String ORM_XSD_1_0 = "orm_1_0.xsd";
    private static final String ORM_XSD_2_0 = "orm_2_0.xsd";

    /**
     * Constructor; supply configuration.
     */
    public XMLPersistenceMetaDataParser(OpenJPAConfiguration conf) {
        _conf = conf;
        setValidating(true);
        setLog(conf.getLog(OpenJPAConfiguration.LOG_METADATA));
        setParseComments(true);
        setMode(MODE_META | MODE_MAPPING | MODE_QUERY);
        setParseText(true);
    }

    /**
     * Configuration supplied on construction.
     */
    public OpenJPAConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * The annotation parser. When class is discovered in an XML file,
     * we first parse any annotations present, then override with the XML.
     */
    public AnnotationPersistenceMetaDataParser getAnnotationParser() {
        return _parser;
    }

    /**
     * The annotation parser. When class is discovered in an XML file,
     * we first parse any annotations present, then override with the XML.
     */
    public void setAnnotationParser(AnnotationPersistenceMetaDataParser parser){
        _parser = parser;
    }

    /**
     * Returns the repository for this parser. If none has been set, creates
     * a new repository and sets it.
     */
    public MetaDataRepository getRepository() {
        if (_repos == null) {
            MetaDataRepository repos = _conf.newMetaDataRepositoryInstance();
            MetaDataFactory mdf = repos.getMetaDataFactory();
            if (mdf instanceof DelegatingMetaDataFactory)
                mdf = ((DelegatingMetaDataFactory) mdf).getInnermostDelegate();
            if (mdf instanceof PersistenceMetaDataFactory)
                ((PersistenceMetaDataFactory) mdf).setXMLParser(this);
            _repos = repos;
        }
        return _repos;
    }

    /**
     * Set the metadata repository for this parser.
     */
    public void setRepository(MetaDataRepository repos) {
        _repos = repos;
        if (repos != null
            && (repos.getValidate() & MetaDataRepository.VALIDATE_RUNTIME) != 0)
            setParseComments(false);
        
        if (repos != null) {
            // Determine if the Thread Context Classloader needs to be temporally overridden to the Classloader
            // that loaded the OpenJPA classes, to avoid a potential deadlock issue with the way Xerces
            // handles parsers and classloaders.
            this.setOverrideContextClassloader(repos.getConfiguration().getCompatibilityInstance().
                getOverrideContextClassloader());
        }
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
     * when a tool is mapping a class, so that .jdo file partial mapping
     * information can be used even when mappings are stored in .orm files
     * or other locations.
     */
    public boolean getMappingOverride() {
        return _override;
    }

    /**
     * Whether to allow later parses of mapping information to override
     * earlier information for the same class. Defaults to false. Useful
     * when a tool is mapping a class, so that .jdo file partial mapping
     * information can be used even when mappings are stored in .orm files
     * or other locations.
     */
    public void setMappingOverride(boolean override) {
        _override = override;
    }

    /**
     * The parse mode according to the expected document type. The
     * mode constants act as bit flags, and therefore can be combined.
     */
    public int getMode() {
        return _mode;
    }

    /**
     * The parse mode according to the expected document type.
     */
    public void setMode(int mode, boolean on) {
        if (mode == MODE_NONE)
            setMode(MODE_NONE);
        else if (on)
            setMode(_mode | mode);
        else
            setMode(_mode & ~mode);
    }

    /**
     * The parse mode according to the expected document type.
     */
    public void setMode(int mode) {
        _mode = mode;
        if (_parser != null)
            _parser.setMode(mode);
    }

    public void parse(URL url) throws IOException {
        // peek at the doc to determine the version
        XMLVersionParser vp = new XMLVersionParser("entity-mappings");
        try {
            vp.parse(url);
            _ormVersion = vp.getVersion();
            _schemaLocation = vp.getSchemaLocation();
        } catch (Throwable t) {
                Log log = getLog();
                if (log.isInfoEnabled())
                    log.trace(_loc.get("version-check-error",
                        url.toString()));
        }
        super.parse(url);
    }

    public void parse(File file) throws IOException {
        // peek at the doc to determine the version
        XMLVersionParser vp = new XMLVersionParser("entity-mappings");
        try {
            vp.parse(file);
            _ormVersion = vp.getVersion();
            _schemaLocation = vp.getSchemaLocation();
        } catch (Throwable t) {
                Log log = getLog();
                if (log.isInfoEnabled())
                    log.trace(_loc.get("version-check-error",
                        file.toString()));
        }
        super.parse(file);
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
     * mapping override enabled.
     */
    protected boolean isMappingOverrideMode() {
        return isMappingMode() || (_override && isMetaDataMode());
    }

    ///////////////
    // XML parsing
    ///////////////

    /**
     * Push a parse element onto the stack.
     */
    protected void pushElement(Object elem) {
        _elements.push(elem);
    }

    /**
     * Pop a parse element from the stack.
     */
    protected Object popElement() {
        return _elements.pop();
    }

    /**
     * Peek a parse element from the stack.
     */
    protected Object peekElement() {
        return _elements.peek();
    }

    /**
     * Return the current element being parsed. May be a class metadata,
     * field metadata, query metadata, etc.
     */
    protected Object currentElement() {
        if (_elements.isEmpty())
            return null;
        return _elements.peek();
    }

    /**
     * Return the current {@link PersistenceStrategy} if any.
     */
    protected PersistenceStrategy currentStrategy() {
        return _strategy;
    }

    /**
     * Return the tag of the current parent element.
     */
    protected Object currentParent() {
        if (_parents.isEmpty())
            return null;
        return _parents.peek();
    }

    /**
     * Return whether we're running the parser at runtime.
     */
    protected boolean isRuntime() {
        return (getRepository().getValidate()
            & MetaDataRepository.VALIDATE_RUNTIME) != 0;
    }

    @Override
    protected Object getSchemaSource() {
        // use the latest schema by default.  'unknown' docs should parse
        // with the latest schema.
        String ormxsd = "orm_2_0-xsd.rsrc";
        boolean useExtendedSchema = true;
        // if the version and/or schema location is for 1.0, use the 1.0
        // schema
        if (_ormVersion != null &&
            _ormVersion.equals(XMLVersionParser.VERSION_1_0) ||
            (_schemaLocation != null &&
            _schemaLocation.indexOf(ORM_XSD_1_0) != -1)) {
            ormxsd = "orm-xsd.rsrc";
            useExtendedSchema = false;
        }
        InputStream ormxsdIS = XMLPersistenceMetaDataParser.class.getResourceAsStream(ormxsd);
        
        ArrayList<InputStream> schema = new ArrayList<InputStream>();
        schema.add(ormxsdIS);
        
        if (useExtendedSchema) {
            // Get the extendable schema
            InputStream extendableXSDIS = 
                    XMLPersistenceMetaDataParser.class.getResourceAsStream("extendable-orm.xsd");
            if (extendableXSDIS != null) {
                schema.add(extendableXSDIS);
            }
            else {
                // TODO: log/trace
            }
            
            // Get the openjpa extended schema
            InputStream openjpaXSDIS = 
                    XMLPersistenceMetaDataParser.class.getResourceAsStream("openjpa-orm.xsd");
            if (openjpaXSDIS != null) {
                schema.add(openjpaXSDIS);
            }
            else {
                // TODO: log/trace
            }
        }
        
        return schema.toArray();
    }

    @Override
    protected String getPackageAttributeName() {
        return null;
    }

    @Override
    protected String getClassAttributeName() {
        return "class";
    }

    @Override
    protected int getClassElementDepth() {
        return 1;
    }

    @Override
    protected boolean isClassElementName(String name) {
        return "entity".equals(name)
            || "embeddable".equals(name)
            || "mapped-superclass".equals(name);
    }

    @Override
    protected void reset() {
    	// Add all remaining deferred embeddable metadata
        addDeferredEmbeddableMetaData();

        super.reset();
        _elements.clear();
        _parents.clear();
        _cls = null;
        _parseList.clear();
        _fieldPos = 0;
        _clsPos = 0;

        _access = AccessCode.UNKNOWN;
        _strategy = null;
        _listener = null;
        _callbacks = null;
        _highs = null;
        _cascades = null;
        _pkgCascades = null;
    }

    @Override
    protected boolean startSystemElement(String name, Attributes attrs)
        throws SAXException {
        Object tag = (Object) _elems.get(name);
        boolean ret = false;
        if (tag == null) {
            if (isMappingOverrideMode())
                tag = startSystemMappingElement(name, attrs);
            ret = tag != null;
        } else if (tag instanceof MetaDataTag) {
            switch ((MetaDataTag) tag) {
                case QUERY:
                    ret = startNamedQuery(attrs);
                    break;
                case QUERY_HINT:
                    ret = startQueryHint(attrs);
                    break;
                case NATIVE_QUERY:
                    ret = startNamedNativeQuery(attrs);
                    break;
                case QUERY_STRING:
                    ret = startQueryString(attrs);
                    break;
                case SEQ_GENERATOR:
                    ret = startSequenceGenerator(attrs);
                    break;
                case FLUSH_MODE:
                    ret = startFlushMode(attrs);
                    break;
                case ENTITY_LISTENERS:
                    ret = startEntityListeners(attrs);
                    break;
                case PRE_PERSIST:
                case POST_PERSIST:
                case PRE_REMOVE:
                case POST_REMOVE:
                case PRE_UPDATE:
                case POST_UPDATE:
                case POST_LOAD:
                    ret = startCallback((MetaDataTag) tag, attrs);
                    break;
                default:
                    warnUnsupportedTag(name);
            }
        } else if (tag == ELEM_PU_META || tag == ELEM_PU_DEF)
            ret = isMetaDataMode();
        else if (tag == ELEM_XML_MAP_META_COMPLETE) {
            setAnnotationParser(null);
            _isXMLMappingMetaDataComplete = true;
        }
        else if (tag == ELEM_ACCESS)
            ret = _mode != MODE_QUERY;
        else if (tag == ELEM_LISTENER)
            ret = startEntityListener(attrs);
        else if (tag == ELEM_DELIM_IDS)
            ret = startDelimitedIdentifiers();
        else if (tag == ELEM_CASCADE)
            ret = isMetaDataMode();
        else if (tag == ELEM_CASCADE_ALL || tag == ELEM_CASCADE_PER
            || tag == ELEM_CASCADE_MER || tag == ELEM_CASCADE_REM
            || tag == ELEM_CASCADE_REF || tag == ELEM_CASCADE_DET)
            ret = startCascade(tag, attrs);

        if (ret)
            _parents.push(tag);
        return ret;
    }

    @Override
    protected void endSystemElement(String name)
        throws SAXException {
        Object tag = _elems.get(name);
        if (tag == null && isMappingOverrideMode())
            endSystemMappingElement(name);
        else if (tag instanceof MetaDataTag) {
            switch ((MetaDataTag) tag) {
                case QUERY:
                    endNamedQuery();
                    break;
                case QUERY_HINT:
                    endQueryHint();
                    break;
                case NATIVE_QUERY:
                    endNamedNativeQuery();
                    break;
                case QUERY_STRING:
                    endQueryString();
                    break;
                case SEQ_GENERATOR:
                    endSequenceGenerator();
                    break;
            }
        } else if (tag == ELEM_ACCESS)
            endAccess();
        else if (tag == ELEM_LISTENER)
            endEntityListener();

        _parents.pop();
    }

    /**
     * Implement to parse a mapping element outside of any class.
     *
     * @return the tag for the given element, or null to skip the element
     */
    protected Object startSystemMappingElement(String name, Attributes attrs)
        throws SAXException {
        return null;
    }

    /**
     * Implement to parse a mapping element outside of any class.
     */
    protected void endSystemMappingElement(String name)
        throws SAXException {
    }

    @Override
    protected boolean startClassElement(String name, Attributes attrs)
        throws SAXException {
        Object tag = (Object) _elems.get(name);
        boolean ret = false;
        if (tag == null) {
            if (isMappingOverrideMode())
                tag = startClassMappingElement(name, attrs);
            ret = tag != null;
        } else if (tag instanceof MetaDataTag) {
            switch ((MetaDataTag) tag) {
                case GENERATED_VALUE:
                    ret = startGeneratedValue(attrs);
                    break;
                case ID:
                    ret = startId(attrs);
                    break;
                case EMBEDDED_ID:
                    ret = startEmbeddedId(attrs);
                    break;
                case ID_CLASS:
                    ret = startIdClass(attrs);
                    break;
                case LOB:
                    ret = startLob(attrs);
                    break;
                case QUERY:
                    ret = startNamedQuery(attrs);
                    break;
                case QUERY_HINT:
                    ret = startQueryHint(attrs);
                    break;
                case NATIVE_QUERY:
                    ret = startNamedNativeQuery(attrs);
                    break;
                case QUERY_STRING:
                    ret = startQueryString(attrs);
                    break;
                case SEQ_GENERATOR:
                    ret = startSequenceGenerator(attrs);
                    break;
                case VERSION:
                    ret = startVersion(attrs);
                    break;
                case MAP_KEY:
                    ret = startMapKey(attrs);
                    break;
                case MAP_KEY_CLASS:
                    ret = startMapKeyClass(attrs);
                    break;
                case FLUSH_MODE:
                    ret = startFlushMode(attrs);
                    break;
                case ORDER_COLUMN:
                    ret = startOrderColumn(attrs);
                    break;
                case ORDER_BY:
                case ENTITY_LISTENERS:
                    ret = isMetaDataMode();
                    break;
                case EXCLUDE_DEFAULT_LISTENERS:
                    ret = startExcludeDefaultListeners(attrs);
                    break;
                case EXCLUDE_SUPERCLASS_LISTENERS:
                    ret = startExcludeSuperclassListeners(attrs);
                    break;
                case PRE_PERSIST:
                case POST_PERSIST:
                case PRE_REMOVE:
                case POST_REMOVE:
                case PRE_UPDATE:
                case POST_UPDATE:
                case POST_LOAD:
                    ret = startCallback((MetaDataTag) tag, attrs);
                    break;
                case DATASTORE_ID:
                    ret = startDatastoreId(attrs);
                    break;
                case DATA_CACHE:
                    ret = startDataCache(attrs);
                    break;
                case READ_ONLY:
                    ret = startReadOnly(attrs);
                    break;
                case EXTERNAL_VALS:
                    ret = startExternalValues(attrs);
                    break;
                case EXTERNAL_VAL:
                    ret = startExternalValue(attrs);
                    break;
                case EXTERNALIZER:
                    ret = startExternalizer(attrs);
                    break;
                case FACTORY:
                    ret = startFactory(attrs);
                    break;
                case FETCH_GROUPS:
                    ret = startFetchGroups(attrs);
                    break;
                case FETCH_GROUP:
                    ret = startFetchGroup(attrs);
                    break;
                case FETCH_ATTRIBUTE:
                    ret = startFetchAttribute(attrs);
                    break;
                case REFERENCED_FETCH_GROUP:
                    ret = startReferencedFetchGroup(attrs);
                    break;
                case OPENJPA_VERSION:
                    ret = true;
                    // TODO: right now the schema enforces this value, but may need to change in the future
                    break;
                default:
                    warnUnsupportedTag(name);
            }
        } else if (tag instanceof PersistenceStrategy) {
            PersistenceStrategy ps = (PersistenceStrategy) tag;
            if (_openjpaNamespace > 0) {
                if (ps == PERS
                    || ps == PERS_COLL
                    || ps == PERS_MAP)
                    ret = startStrategy(ps, attrs);
                else
                    ret = startExtendedStrategy(ps, attrs);
            }
            else {
                ret = startStrategy(ps, attrs); 
            }
            if (ret)
                _strategy = ps;
        } else if (tag == ELEM_LISTENER)
            ret = startEntityListener(attrs);
        else if (tag == ELEM_ATTRS)
            ret = _mode != MODE_QUERY;
        else if (tag == ELEM_CASCADE)
            ret = isMetaDataMode();
        else if (tag == ELEM_CASCADE_ALL || tag == ELEM_CASCADE_PER
            || tag == ELEM_CASCADE_MER || tag == ELEM_CASCADE_REM
            || tag == ELEM_CASCADE_REF || tag == ELEM_CASCADE_DET)
            ret = startCascade(tag, attrs);

        if (ret)
            _parents.push(tag);
        return ret;
    }

    @Override
    protected void endClassElement(String name)
        throws SAXException {
        Object tag = _elems.get(name);
        if (tag == null && isMappingOverrideMode())
            endClassMappingElement(name);
        else if (tag instanceof MetaDataTag) {
            switch ((MetaDataTag) tag) {
                case GENERATED_VALUE:
                    endGeneratedValue();
                    break;
                case ID:
                    endId();
                    break;
                case EMBEDDED_ID:
                    endEmbeddedId();
                    break;
                case ID_CLASS:
                    endIdClass();
                    break;
                case LOB:
                    endLob();
                    break;
                case QUERY:
                    endNamedQuery();
                    break;
                case QUERY_HINT:
                    endQueryHint();
                    break;
                case NATIVE_QUERY:
                    endNamedNativeQuery();
                    break;
                case QUERY_STRING:
                    endQueryString();
                    break;
                case SEQ_GENERATOR:
                    endSequenceGenerator();
                    break;
                case VERSION:
                    endVersion();
                    break;
                case ORDER_BY:
                    endOrderBy();
                    break;
                case EXTERNAL_VALS:
                    endExternalValues();
                    break;
                case EXTERNALIZER:
                    endExternalizer();
                    break;
                case FACTORY:
                    endFactory();
                    break;
                case FETCH_GROUP:
                    endFetchGroup();
                    break;
                case REFERENCED_FETCH_GROUP:
                    endReferencedFetchGroup();
                    break;
            }
        } else if (tag instanceof PersistenceStrategy) {
            PersistenceStrategy ps = (PersistenceStrategy) tag;
            if (_openjpaNamespace > 0) {
                endExtendedStrategy(ps);
            }
            else {
                endStrategy(ps); 
            }
        }
        else if (tag == ELEM_ACCESS)
            endAccess();
        else if (tag == ELEM_LISTENER)
            endEntityListener();

        _parents.pop();
    }

    /**
     * Log warning about an unsupported tag.
     */
    private void warnUnsupportedTag(String name) {
        Log log = getLog();
        if (log.isInfoEnabled())
            log.trace(_loc.get("unsupported-tag", name));
    }

    /**
     * Implement to parse a mapping element within a class.
     *
     * @return the tag for the given element, or null to skip element
     */
    protected Object startClassMappingElement(String name, Attributes attrs)
        throws SAXException {
        return null;
    }

    /**
     * Implement to parse a mapping element within a class.
     */
    protected void endClassMappingElement(String name)
        throws SAXException {
    }

    boolean isMetaDataComplete(Attributes attrs) {
    	return attrs != null
    	    && "true".equals(attrs.getValue("metadata-complete"));
    }

    void resetAnnotationParser() {
    	setAnnotationParser(((PersistenceMetaDataFactory)getRepository()
    			.getMetaDataFactory()).getAnnotationParser());
    }

    @Override
    protected boolean startClass(String elem, Attributes attrs)
        throws SAXException {
        boolean metaDataComplete = false;
        super.startClass(elem, attrs);
        if (isMetaDataComplete(attrs)) {
            metaDataComplete = true;
        	setAnnotationParser(null);
        } else if (!_isXMLMappingMetaDataComplete){
        	resetAnnotationParser();
        }

        // query mode only?
        _cls = classForName(currentClassName());

        // Prevent a reentrant parse for the same class
        if (parseListContains(_cls)) {
            return false;
        }

        if (_mode == MODE_QUERY) {
            if(_conf.getCompatibilityInstance().getParseAnnotationsForQueryMode()){ 
                if (_parser != null) {
                    _parser.parse(_cls);
                }
            }
            return true;
        }

        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-class", _cls.getName()));

        MetaDataRepository repos = getRepository();
        ClassMetaData meta = repos.getCachedMetaData(_cls);
        if (meta != null
            && ((isMetaDataMode() && (meta.getSourceMode() & MODE_META) != 0)
            || (isMappingMode() && (meta.getSourceMode() & MODE_MAPPING) != 0)))
        {
            if(isDuplicateClass(meta)) { 
                if (log.isWarnEnabled()) {
                    log.warn(_loc.get("dup-metadata", _cls, getSourceName()));
                }
                if(log.isTraceEnabled()) { 
                    log.trace(String.format(
                        "MetaData originally obtained from source: %s under mode: %d with scope: %s, and type: %d",
                        meta.getSourceName(), meta.getSourceMode(), meta.getSourceScope(), meta.getSourceType()));
                }
            }
            _cls = null;
            return false;
        }
        
        int access = AccessCode.UNKNOWN;
        if (meta == null) {
            int accessCode = toAccessType(attrs.getValue("access"));
            // if access not specified and access was specified at
            // the system level, use the system default (which may
            // be UNKNOWN)
            if (accessCode == AccessCode.UNKNOWN)
                accessCode = _access;
            meta = repos.addMetaData(_cls, accessCode, metaDataComplete);
            FieldMetaData[] fmds = meta.getFields();
            if (metaDataComplete) {
                for (int i = 0; i < fmds.length; i++) {
                    fmds[i].setExplicit(true);
                }
            }
            meta.setEnvClassLoader(_envLoader);
            meta.setSourceMode(MODE_NONE);

            // parse annotations first so XML overrides them
            if (_parser != null) {
                _parser.parse(_cls);
            }
        }
        access = meta.getAccessType();

        boolean mappedSuper = "mapped-superclass".equals(elem);
        boolean embeddable = "embeddable".equals(elem);

        if (isMetaDataMode()) {
            Locator locator = getLocation().getLocator();
            meta.setSource(getSourceFile(), SourceTracker.SRC_XML, locator != null ? locator.getSystemId() : "" );
            meta.setSourceMode(MODE_META, true);
        
            if (locator != null) {
                meta.setLineNumber(locator.getLineNumber());
                meta.setColNumber(locator.getColumnNumber());
            }
            meta.setListingIndex(_clsPos);
            String name = attrs.getValue("name");
            if (!StringUtils.isEmpty(name))
                meta.setTypeAlias(name);
            meta.setAbstract(mappedSuper);
            meta.setEmbeddedOnly(mappedSuper || embeddable);

            if (embeddable) {
                meta.setEmbeddable();
                setDeferredEmbeddableAccessType(_cls, access);
            }
        }

        if (attrs.getValue("cacheable") != null) {
            meta.setCacheEnabled(Boolean.valueOf(attrs.getValue("cacheable")));
        }

        if (isMappingMode())
            meta.setSourceMode(MODE_MAPPING, true);
        if (isMappingOverrideMode())
            startClassMapping(meta, mappedSuper, attrs);
        if (isQueryMode())
            meta.setSourceMode(MODE_QUERY, true);

        _clsPos++;
        _fieldPos = 0;
        addComments(meta);
        pushElement(meta);
        return true;
    }

    @Override
    protected void endClass(String elem)
        throws SAXException {
        if (_mode != MODE_QUERY) {
            ClassMetaData meta = (ClassMetaData) popElement();
            storeCallbacks(meta);
            
            if (isMappingOverrideMode())
                endClassMapping(meta);
        }
        _cls = null;
        super.endClass(elem);
    }
    
    /**
     * Implement to add mapping attributes to class.
     */
    protected void startClassMapping(ClassMetaData mapping,
        boolean mappedSuper, Attributes attrs)
        throws SAXException {
    }

    /**
     * Implement to finalize class mapping.
     */
    protected void endClassMapping(ClassMetaData mapping)
        throws SAXException {
    }

    /**
     * Default access element.
     */
    private void endAccess() {
        _access = toAccessType(currentText());
    }

    /**
     * Parse the given string as an entity access type, defaulting to given
     * default if string is empty.
     */
    private int toAccessType(String str) {
        if (StringUtils.isEmpty(str))
            return AccessCode.UNKNOWN;
        if ("PROPERTY".equals(str))
            return AccessCode.EXPLICIT | AccessCode.PROPERTY;
        return AccessCode.EXPLICIT | AccessCode.FIELD;
    }
    /**
     * Parse flush-mode element.
     */
    private boolean startFlushMode(Attributes attrs)
        throws SAXException {
        Log log = getLog();
        if (log.isWarnEnabled())
            log.warn(_loc.get("unsupported", "flush-mode", getSourceName()));
        return false;
    }

    /**
     * Parse sequence-generator.
     */
    protected boolean startSequenceGenerator(Attributes attrs) {
        if (!isMappingOverrideMode())
            return false;

        String name = attrs.getValue("name");
        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-sequence", name));

        SequenceMetaData meta = getRepository().getCachedSequenceMetaData(name);
        if (meta != null && log.isWarnEnabled())
            log.warn(_loc.get("override-sequence", name));

        meta = getRepository().addSequenceMetaData(name);
        String seq = attrs.getValue("sequence-name");
        // Do not normalize the sequence name if it appears to be a plugin 
        if (seq.indexOf('(') == -1){
            seq = normalizeSequenceName(seq);
        }
        String val = attrs.getValue("initial-value");
        int initial = val == null ? 1 : Integer.parseInt(val);
        val = attrs.getValue("allocation-size");
        int allocate = val == null ? 50 : Integer.parseInt(val);
        String schema = normalizeSchemaName(attrs.getValue("schema"));
        String catalog = normalizeCatalogName(attrs.getValue("catalog"));

        String clsName, props;
        if (seq == null || seq.indexOf('(') == -1) {
            clsName = SequenceMetaData.IMPL_NATIVE;
            props = null;
        } else { // plugin
            clsName = Configurations.getClassName(seq);
            props = Configurations.getProperties(seq);
            seq = null;
        }

        meta.setSequencePlugin(Configurations.getPlugin(clsName, props));
        meta.setSequence(seq);
        meta.setInitialValue(initial);
        meta.setAllocate(allocate);
        meta.setSchema(schema);
        meta.setCatalog(catalog);

        Object cur = currentElement();
        Object scope = (cur instanceof ClassMetaData)
            ? ((ClassMetaData) cur).getDescribedType() : null;
        meta.setSource(getSourceFile(), scope, SourceTracker.SRC_XML);
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            meta.setLineNumber(locator.getLineNumber());
            meta.setColNumber(locator.getColumnNumber());
        }
        return true;
    }

    protected void endSequenceGenerator() {
    }

    /**
     * Parse id.
     */
    protected boolean startId(Attributes attrs)
        throws SAXException {
        FieldMetaData fmd = parseField(attrs);
        fmd.setExplicit(true);
        fmd.setPrimaryKey(true);
        return true;
    }

    protected void endId()
        throws SAXException {
        finishField();
    }

    /**
     * Parse embedded-id.
     */
    protected boolean startEmbeddedId(Attributes attrs)
        throws SAXException {
        FieldMetaData fmd = parseField(attrs);
        fmd.setExplicit(true);
        fmd.setPrimaryKey(true);
        fmd.setEmbedded(true);
        fmd.setSerialized(false);
        if (fmd.getEmbeddedMetaData() == null)
//            fmd.addEmbeddedMetaData();
            deferEmbeddable(fmd.getDeclaredType(), fmd);
        return true;
    }

    protected void endEmbeddedId()
        throws SAXException {
        finishField();
    }

    /**
     * Parse id-class.
     */
    protected boolean startIdClass(Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;

        ClassMetaData meta = (ClassMetaData) currentElement();
        String cls = attrs.getValue("class");
        Class<?> idCls = null;
        try {
            idCls = classForName(cls);
        } catch (Throwable t) {
            throw getException(_loc.get("invalid-id-class", meta, cls), t);
        }
        if (!Serializable.class.isAssignableFrom(idCls)) {
        	_conf.getConfigurationLog().warn(_loc.get("id-class-not-serializable", idCls, _cls));
        }
        meta.setObjectIdType(idCls, true);
        return true;
    }

    protected void endIdClass()
        throws SAXException {
    }

    /**
     * Parse lob.
     */
    protected boolean startLob(Attributes attrs)
        throws SAXException {
        FieldMetaData fmd = (FieldMetaData) currentElement();
        int typeCode = fmd.isElementCollection() ? fmd.getElement().getDeclaredTypeCode() : fmd.getDeclaredTypeCode();
        Class<?> type = fmd.isElementCollection() ? fmd.getElement().getDeclaredType() : fmd.getDeclaredType();
        if (typeCode != JavaTypes.STRING
            && type != char[].class
            && type != Character[].class
            && type != byte[].class
            && type != Byte[].class)
            fmd.setSerialized(true);
        return true;
    }

    protected void endLob()
        throws SAXException {
    }

    /**
     * Parse generated-value.
     */
    protected boolean startGeneratedValue(Attributes attrs)
        throws SAXException {
        if (!isMappingOverrideMode())
            return false;

        String strategy = attrs.getValue("strategy");
        String generator = attrs.getValue("generator");
        GenerationType type = StringUtils.isEmpty(strategy)
            ? GenerationType.AUTO : GenerationType.valueOf(strategy);

        FieldMetaData fmd = (FieldMetaData) currentElement();
        AnnotationPersistenceMetaDataParser.parseGeneratedValue(fmd, type,
            generator);
        return true;
    }

    protected void endGeneratedValue()
        throws SAXException {
    }

    /**
     * Lazily parse cascades.
     */
    protected boolean startCascade(Object tag, Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;

        Set<CascadeType> cascades = null;
        if (currentElement() instanceof FieldMetaData) {
            if (_cascades == null)
                _cascades = EnumSet.noneOf(CascadeType.class);
            cascades = _cascades;
        } else {
            if (_pkgCascades == null)
                _pkgCascades = EnumSet.noneOf(CascadeType.class);
            cascades = _pkgCascades;
        }
        boolean all = ELEM_CASCADE_ALL == tag;
        if (all || ELEM_CASCADE_PER == tag)
            cascades.add(PERSIST);
        if (all || ELEM_CASCADE_REM == tag)
            cascades.add(REMOVE);
        if (all || ELEM_CASCADE_MER == tag)
            cascades.add(MERGE);
        if (all || ELEM_CASCADE_REF == tag)
            cascades.add(REFRESH);
        if (all || ELEM_CASCADE_DET == tag)
            cascades.add(DETACH);
        return true;
    }

    /**
     * Set the cached cascades into the field.
     */
    protected void setCascades(FieldMetaData fmd) {
        Set<CascadeType> cascades = _cascades;
        if (_cascades == null)
            cascades = _pkgCascades;
        if (cascades == null)
            return;

        ValueMetaData vmd = fmd;
        if (_strategy == ONE_MANY || _strategy == MANY_MANY) {
            vmd = fmd.getElement();
        }
        for (CascadeType cascade : cascades) {
            switch (cascade) {
                case PERSIST:
                    vmd.setCascadePersist(ValueMetaData.CASCADE_IMMEDIATE);
                    break;
                case MERGE:
                    vmd.setCascadeAttach(ValueMetaData.CASCADE_IMMEDIATE);
                    break;
                case DETACH:
                    vmd.setCascadeDetach(ValueMetaData.CASCADE_IMMEDIATE);
                    break;
                case REMOVE:
                    vmd.setCascadeDelete(ValueMetaData.CASCADE_IMMEDIATE);
                    break;
                case REFRESH:
                    vmd.setCascadeRefresh(ValueMetaData.CASCADE_IMMEDIATE);
                    break;
            }
        }
        _cascades = null;
    }

    /**
     * Parse common field attributes.
     */
    private FieldMetaData parseField(Attributes attrs)
        throws SAXException {
        ClassMetaData meta = (ClassMetaData) currentElement();
        String name = attrs.getValue("name");
        FieldMetaData field = meta.getDeclaredField(name);
        int fldAccess = getFieldAccess(field, attrs);
        // If the access defined in XML is not the same as what was defined
        // by default or annotation, find the appropriate backing member and
        // replace what is currently defined in metadata.
        if ((field == null || field.getDeclaredType() == Object.class ||
             field.getAccessType() != fldAccess)
            && meta.getDescribedType() != Object.class) {
            Member member = _repos.getMetaDataFactory().getDefaults()
     	        .getMemberByProperty(meta, name, fldAccess, false);
            Class<?> type = Field.class.isInstance(member) ?
                ((Field)member).getType() : ((Method)member).getReturnType();

            if (field == null) {
                field = meta.addDeclaredField(name, type);
                PersistenceMetaDataDefaults.setCascadeNone(field);
                PersistenceMetaDataDefaults.setCascadeNone(field.getKey());
                PersistenceMetaDataDefaults.setCascadeNone(field.getElement());
            }
            field.backingMember(member);
        } else if (field == null) {
            field = meta.addDeclaredField(name, Object.class);
            PersistenceMetaDataDefaults.setCascadeNone(field);
            PersistenceMetaDataDefaults.setCascadeNone(field.getKey());
            PersistenceMetaDataDefaults.setCascadeNone(field.getElement());
        }

        if (isMetaDataMode())
            field.setListingIndex(_fieldPos);

        _fieldPos++;
        pushElement(field);
        addComments(field);

        if (isMappingOverrideMode())
            startFieldMapping(field, attrs);
        return field;
    }

    /**
     * Pops field element.
     */
    private void finishField()
        throws SAXException {
        FieldMetaData field = (FieldMetaData) popElement();
        setCascades(field);
        if (isMappingOverrideMode())
            endFieldMapping(field);
        _strategy = null;
    }

    /**
     * Determines access for field based upon existing metadata and XML
     * attributes.
     *
     * @param field FieldMetaData current metadata for field
     * @param attrs XML Attributes defined on this field
     * @return
     */
    private int getFieldAccess(FieldMetaData field, Attributes attrs) {
        if (attrs != null) {
            String access = attrs.getValue("access");
            if ("PROPERTY".equals(access))
                return AccessCode.EXPLICIT | AccessCode.PROPERTY;
            if ("FIELD".equals(access))
                return AccessCode.EXPLICIT | AccessCode.FIELD;
        }
        // Check access defined on field, if provided
        if (field != null) {
            return field.getAccessType();
        }
        // Otherwise, get the default access type of the declaring class
        ClassMetaData meta = (ClassMetaData) currentElement();
        if (meta != null) {
            return AccessCode.toFieldCode(meta.getAccessType());
        }
        return AccessCode.UNKNOWN;
    }

    /**
     * Implement to add field mapping data. Does nothing by default.
     */
    protected void startFieldMapping(FieldMetaData field, Attributes attrs)
        throws SAXException {
    }

    /**
     * Implement to finalize field mapping. Does nothing by default.
     */
    protected void endFieldMapping(FieldMetaData field)
        throws SAXException {
    }

    /**
     * Parse version.
     */
    protected boolean startVersion(Attributes attrs)
        throws SAXException {
        FieldMetaData fmd = parseField(attrs);
        fmd.setExplicit(true);
        fmd.setVersion(true);
        return true;
    }

    protected void endVersion()
        throws SAXException {
        finishField();
    }

    /**
     * Parse strategy element.
     */
    private boolean startStrategy(PersistenceStrategy strategy,
        Attributes attrs)
        throws SAXException {
        FieldMetaData fmd = parseField(attrs);
        fmd.setExplicit(true);
        fmd.setManagement(FieldMetaData.MANAGE_PERSISTENT);

        String val = attrs.getValue("optional");
        if ("false".equals(val))
            fmd.setNullValue(FieldMetaData.NULL_EXCEPTION);
        else if ("true".equals(val)
                && fmd.getNullValue() == FieldMetaData.NULL_EXCEPTION) {
            // Reset value if the field was annotated with optional=false.
            // Otherwise leave it alone.
            fmd.setNullValue(FieldMetaData.NULL_UNSET);
        }
        if (isMappingOverrideMode()) {
            val = attrs.getValue("mapped-by");
            if (val != null)
                fmd.setMappedBy(val);
        }
        parseStrategy(fmd, strategy, attrs);
        return true;
    }

    private void endStrategy(PersistenceStrategy strategy)
        throws SAXException {
        finishField();
    }

    /**
     * Parse strategy specific attributes.
     */
    private void parseStrategy(FieldMetaData fmd,
        PersistenceStrategy strategy, Attributes attrs)
        throws SAXException {
        switch (strategy) {
            case BASIC:
                parseBasic(fmd, attrs);
                break;
            case EMBEDDED:
                parseEmbedded(fmd, attrs);
                break;
            case ONE_ONE:
                parseOneToOne(fmd, attrs);
                break;
            case MANY_ONE:
                parseManyToOne(fmd, attrs);
                break;
            case MANY_MANY:
                parseManyToMany(fmd, attrs);
                break;
            case ONE_MANY:
                parseOneToMany(fmd, attrs);
                break;
            case TRANSIENT:
                String val = attrs.getValue("fetch");
                if (val != null) {
                    fmd.setInDefaultFetchGroup("EAGER".equals(val));
                }
                fmd.setManagement(FieldMetaData.MANAGE_NONE);
                break;
            case ELEM_COLL:
                parseElementCollection(fmd, attrs);
                break;
            case PERS:
                parsePersistent(fmd, attrs);
                break;
            case PERS_COLL:
                parsePersistentCollection(fmd, attrs);
                break;
            case PERS_MAP:
                parsePersistentMap(fmd, attrs);
                break;
        }
    }

    /**
     * Parse basic.
     */
    protected void parseBasic(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("fetch");
        if (val != null) {
            fmd.setInDefaultFetchGroup("EAGER".equals(val));
        }
    }

    /**
     * Parse embedded.
     */
    protected void parseEmbedded(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        assertPC(fmd, "Embedded");
        fmd.setInDefaultFetchGroup(true);
        fmd.setEmbedded(true);
        fmd.setSerialized(false); // override any Lob annotation

        if (fmd.getEmbeddedMetaData() == null)
//            fmd.addEmbeddedMetaData();
            deferEmbeddable(fmd.getDeclaredType(), fmd);
    }

    /**
     * Throw proper exception if given value is not possibly persistence
     * capable.
     */
    private void assertPC(FieldMetaData fmd, String attr)
        throws SAXException {
        if (!JavaTypes.maybePC(fmd))
            throw getException(_loc.get("bad-meta-anno", fmd, attr));
    }

    /**
     * Parse one-to-one.
     */
    protected void parseOneToOne(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("fetch");
        boolean dfg = (val != null && val.equals("LAZY")) ? false : true;
        
        // We need to toggle the DFG explicit flag here because this is used for an optimization when selecting an
        // Entity with lazy fields. 
        fmd.setDefaultFetchGroupExplicit(true);
        fmd.setInDefaultFetchGroup(dfg);
        fmd.setDefaultFetchGroupExplicit(false);
        
        val = attrs.getValue("target-entity");
        if (val != null)
            fmd.setTypeOverride(AnnotationPersistenceMetaDataParser.toOverrideType(classForName(val)));
        assertPC(fmd, "OneToOne");
        fmd.setSerialized(false); // override any Lob annotation
        boolean orphanRemoval = Boolean.valueOf(attrs.getValue(
            "orphan-removal"));
        setOrphanRemoval(fmd, orphanRemoval);
        String mapsId = attrs.getValue("maps-id");
        if (mapsId != null)
            fmd.setMappedByIdValue(mapsId);
    }

    /**
     * Parse many-to-one.
     */
    protected void parseManyToOne(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("fetch");
        boolean dfg = (val != null && val.equals("LAZY")) ? false : true;
        
        // We need to toggle the DFG explicit flag here because this is used for an optimization when selecting an
        // Entity with lazy fields. 
        fmd.setDefaultFetchGroupExplicit(true);
        fmd.setInDefaultFetchGroup(dfg);
        fmd.setDefaultFetchGroupExplicit(false);
        
        val = attrs.getValue("target-entity");
        if (val != null)
            fmd.setTypeOverride(AnnotationPersistenceMetaDataParser.toOverrideType(classForName(val)));
        assertPC(fmd, "ManyToOne");
        fmd.setSerialized(false); // override any Lob annotation
        String mapsId = attrs.getValue("maps-id");
        if (mapsId != null)
            fmd.setMappedByIdValue(mapsId);
    }

    /**
     * Parse many-to-many.
     */
    protected void parseManyToMany(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("fetch");
        if (val != null) {
            fmd.setInDefaultFetchGroup("EAGER".equals(val));
        }
        val = attrs.getValue("target-entity");
        if (val != null)
            fmd.getElement().setDeclaredType(classForName(val));
        assertPCCollection(fmd, "ManyToMany");
        fmd.setSerialized(false); // override Lob in annotation
    }

    /**
     * Throw exception if given field not a collection of possible persistence
     * capables.
     */
    private void assertPCCollection(FieldMetaData fmd, String attr)
        throws SAXException {
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                if (JavaTypes.maybePC(fmd.getElement()))
                    break;
                // no break
            default:
                throw getException(_loc.get("bad-meta-anno", fmd, attr));
        }
    }

    /**
     * Parse one-to-many.
     */
    protected void parseOneToMany(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("fetch");
        if (val != null) {
            fmd.setInDefaultFetchGroup("EAGER".equals(val));
        }
        val = attrs.getValue("target-entity");
        if (val != null)
            fmd.getElement().setDeclaredType(classForName(val));
        assertPCCollection(fmd, "OneToMany");
        fmd.setSerialized(false); // override any Lob annotation
        boolean orphanRemoval = Boolean.valueOf(attrs.getValue(
            "orphan-removal"));
        setOrphanRemoval(fmd.getElement(), orphanRemoval);
    }

    protected void setOrphanRemoval(ValueMetaData vmd, boolean orphanRemoval) {
        if (orphanRemoval)
            vmd.setCascadeDelete(ValueMetaData.CASCADE_AUTO);
    }

    protected void parseElementCollection(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String val = attrs.getValue("target-class");
        if (val != null)
            fmd.getElement().setDeclaredType(classForName(val));

        if (fmd.getDeclaredTypeCode() != JavaTypes.COLLECTION &&
            fmd.getDeclaredTypeCode() != JavaTypes.MAP)
            throw getException(_loc.get("bad-meta-anno", fmd,
                    "ElementCollection"));

        val = attrs.getValue("fetch");
        if (val != null)
            fmd.setInDefaultFetchGroup("EAGER".equals(val));
        fmd.setElementCollection(true);
        fmd.setSerialized(false);
        if (JavaTypes.maybePC(fmd.getElement()) && !fmd.getElement().getDeclaredType().isEnum()) {
            fmd.getElement().setEmbedded(true);
            if (fmd.getElement().getEmbeddedMetaData() == null)
//                fmd.getElement().addEmbeddedMetaData();
                deferEmbeddable(fmd.getElement().getDeclaredType(),
                    fmd.getElement());
        }
    }

    /**
     * Parse map-key.
     */
    private boolean startMapKey(Attributes attrs)
        throws SAXException {
        if (!isMappingOverrideMode())
            return false;

        FieldMetaData fmd = (FieldMetaData) currentElement();
        String mapKey = attrs.getValue("name");
        if (mapKey == null)
            fmd.getKey().setValueMappedBy(ValueMetaData.MAPPED_BY_PK);
        else
            fmd.getKey().setValueMappedBy(mapKey);
        return true;
    }


    /**
     * Parse map-key-class.
     */
    private boolean startMapKeyClass(Attributes attrs)
    throws SAXException {
        if (!isMappingOverrideMode())
            return false;

        FieldMetaData fmd = (FieldMetaData) currentElement();
        String mapKeyClass = attrs.getValue("class");

        if (mapKeyClass != null) {
            try {
                fmd.getKey().setDeclaredType(Class.forName(mapKeyClass));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class not found");
            }
        } else
            throw new IllegalArgumentException(
            "The value of the MapKeyClass cannot be null");
        return true;
    }

    /**
     * Parse order-by.
     */
    private void endOrderBy()
        throws SAXException {
        FieldMetaData fmd = (FieldMetaData) currentElement();
        String dec = currentText();
        if (fmd.isElementCollection() &&
            fmd.getElement().getEmbeddedMetaData() != null ||
            isDeferredEmbeddable(fmd.getElement().getDeclaredType(),
                fmd.getElement())) {
            if (dec.length() == 0 || dec.equals("ASC") ||
                dec.equals("DESC"))
                throw new MetaDataException(_loc.get(
                    "invalid-orderBy", fmd));
        }
        if (StringUtils.isEmpty(dec) || dec.equals("ASC"))
            dec = Order.ELEMENT + " asc";
        else if (dec.equals("DESC"))
            dec = Order.ELEMENT + " desc";

        fmd.setOrderDeclaration(dec);
    }

    /**
     * Parse named-query.
     */
    protected boolean startNamedQuery(Attributes attrs)
        throws SAXException {
        if (!isQueryMode())
            return false;

        String name = attrs.getValue("name");
        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-query", name));

        QueryMetaData meta = getRepository().searchQueryMetaDataByName(name);
        if (meta != null) {
        	Class<?> defType = meta.getDefiningType();
            if ((defType != _cls) && log.isWarnEnabled()) {
                log.warn(_loc.get("dup-query", name, currentLocation(),
            	        defType));
            }
            pushElement(meta);
            return true;
        }

        meta = getRepository().addQueryMetaData(null, name);
        meta.setDefiningType(_cls);
        meta.setLanguage(JPQLParser.LANG_JPQL);
        meta.setQueryString(attrs.getValue("query"));
        String lockModeStr = attrs.getValue("lock-mode");
        LockModeType lmt = processNamedQueryLockModeType(log, lockModeStr, name);
        if (lmt != null && lmt != LockModeType.NONE) {
            meta.addHint("openjpa.FetchPlan.ReadLockMode", lmt);
        }
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            meta.setLineNumber(locator.getLineNumber());
            meta.setColNumber(locator.getColumnNumber());
        }
        Object cur = currentElement();
        Object scope = (cur instanceof ClassMetaData)
            ? ((ClassMetaData) cur).getDescribedType() : null;
        meta.setSource(getSourceFile(), scope, SourceTracker.SRC_XML, locator == null ? "" : locator.getSystemId());
        if (isMetaDataMode())
            meta.setSourceMode(MODE_META);
        else if (isMappingMode())
            meta.setSourceMode(MODE_MAPPING);
        else
            meta.setSourceMode(MODE_QUERY);
        pushElement(meta);
        return true;
    }
    
    /**
     * A private worker method that calculates the lock mode for an individual NamedQuery. If the NamedQuery is 
     * configured to use the NONE lock mode(explicit or implicit), this method will promote the lock to a READ
     * level lock. This was done to allow for JPA1 apps to function properly under a 2.0 runtime. 
     */
    private LockModeType processNamedQueryLockModeType(Log log, String lockModeString, String queryName) {
        if (lockModeString == null) {
            return null;
        }
        LockModeType lmt = LockModeType.valueOf(lockModeString);
        String lm = _conf.getLockManager();
        boolean optimistic = _conf.getOptimistic();
        if (lm != null) {
            lm = lm.toLowerCase();
            if (lm.contains("pessimistic")) {
                if (lmt == LockModeType.NONE && !optimistic) {
                    if (log != null && log.isWarnEnabled() == true) {
                        log.warn(_loc.get("override-named-query-lock-mode", new String[] { "xml", queryName,
                            _cls.getName() }));
                    }
                    lmt = LockModeType.READ;
                }
            }
        }

        return lmt;
    }

    protected void endNamedQuery()
        throws SAXException {
        popElement();
    }

    protected boolean startQueryString(Attributes attrs)
        throws SAXException {
        return true;
    }

    protected void endQueryString()
        throws SAXException {
        QueryMetaData meta = (QueryMetaData) currentElement();
        meta.setQueryString(currentText());
    }

    /**
     * Parse query-hint.
     */
    protected boolean startQueryHint(Attributes attrs)
        throws SAXException {
        QueryMetaData meta = (QueryMetaData) currentElement();
        meta.addHint(attrs.getValue("name"), attrs.getValue("value"));
        return true;
    }

    protected void endQueryHint()
        throws SAXException {
    }

    /**
     * Parse native-named-query.
     */
    protected boolean startNamedNativeQuery(Attributes attrs)
        throws SAXException {
        if (!isQueryMode())
            return false;

        String name = attrs.getValue("name");
        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-native-query", name));

        QueryMetaData meta = getRepository().getCachedQueryMetaData(name);
        if (meta != null && isDuplicateQuery(meta) ) {
            log.warn(_loc.get("override-query", name, currentLocation()));
        }

        meta = getRepository().addQueryMetaData(null, name);
        meta.setDefiningType(_cls);
        meta.setLanguage(QueryLanguages.LANG_SQL);
        meta.setQueryString(attrs.getValue("query"));
        String val = attrs.getValue("result-class");
        if (val != null) {
            Class<?> type = classForName(val);
            if (ImplHelper.isManagedType(getConfiguration(), type))
                meta.setCandidateType(type);
            else
                meta.setResultType(type);
        }

        val = attrs.getValue("result-set-mapping");
        if (val != null)
            meta.setResultSetMappingName(val);

        Object cur = currentElement();
        Object scope = (cur instanceof ClassMetaData) ? ((ClassMetaData) cur).getDescribedType() : null;
        Locator locator = getLocation().getLocator();
        meta.setSource(getSourceFile(), scope, SourceTracker.SRC_XML, locator == null ? "" : locator.getSystemId());
        if (locator != null) {
            meta.setLineNumber(locator.getLineNumber());
            meta.setColNumber(locator.getColumnNumber());
        }
        if (isMetaDataMode())
            meta.setSourceMode(MODE_META);
        else if (isMappingMode())
            meta.setSourceMode(MODE_MAPPING);
        else
            meta.setSourceMode(MODE_QUERY);
        pushElement(meta);
        return true;
    }

    protected void endNamedNativeQuery()
        throws SAXException {
        popElement();
    }

    /**
     * Start entity-listeners
     */
    private boolean startEntityListeners(Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;
        if (currentElement() == null)
            return true;

        // reset listeners declared in annotations.
        LifecycleMetaData meta = ((ClassMetaData) currentElement()).
            getLifecycleMetaData();
        for (int i = 0; i < LifecycleEvent.ALL_EVENTS.length; i++)
            meta.setDeclaredCallbacks(i, null, 0);
        return true;
    }

    /**
     * Parse exclude-default-listeners.
     */
    private boolean startExcludeDefaultListeners(Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;
        ClassMetaData meta = (ClassMetaData) currentElement();
        meta.getLifecycleMetaData().setIgnoreSystemListeners(true);
        return true;
    }

    /**
     * Parse exclude-superclass-listeners.
     */
    private boolean startExcludeSuperclassListeners(Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;
        ClassMetaData meta = (ClassMetaData) currentElement();
        meta.getLifecycleMetaData().setIgnoreSuperclassCallbacks
            (LifecycleMetaData.IGNORE_HIGH);
        return true;
    }

    /**
     * Parse entity-listener.
     */
    private boolean startEntityListener(Attributes attrs)
        throws SAXException {
        _listener = classForName(attrs.getValue("class"));
        if (!_conf.getCallbackOptionsInstance().getAllowsDuplicateListener()) {
            if (_listeners == null)
                _listeners = new ArrayList<Class<?>>();
            if (_listeners.contains(_listener)) 
                return true;
            _listeners.add(_listener);    
        }
            
        boolean system = currentElement() == null;
        Collection<LifecycleCallbacks>[] parsed =
            AnnotationPersistenceMetaDataParser.parseCallbackMethods(_listener,
                null, true, true, _repos);
        if (parsed == null)
            return true;

        if (_callbacks == null) {
            _callbacks = (Collection<LifecycleCallbacks>[])
                new Collection[LifecycleEvent.ALL_EVENTS.length];
            if (!system)
                _highs = new int[LifecycleEvent.ALL_EVENTS.length];
        }
        for (int i = 0; i < parsed.length; i++) {
            if (parsed[i] == null)
                continue;
            if (_callbacks[i] == null)
                _callbacks[i] = parsed[i];
            else
                _callbacks[i].addAll(parsed[i]);
            if (!system)
                _highs[i] += parsed[i].size();
        }
        return true;
    }

    private void endEntityListener()
        throws SAXException {
        // should be in endEntityListeners I think to merge callbacks
        // into a single listener.  But then the user cannot remove.
        if (currentElement() == null && _callbacks != null) {
            _repos.addSystemListener(new PersistenceListenerAdapter
                (_callbacks));
            _callbacks = null;
        }
        _listener = null;
    }

    private boolean startCallback(MetaDataTag callback, Attributes attrs)
        throws SAXException {
        if (!isMetaDataMode())
            return false;
        int[] events = MetaDataParsers.getEventTypes(callback, _conf);
        if (events == null)
            return false;

        boolean system = currentElement() == null;

        // If in a multi-level parse, do not add system level listeners.
        // Otherwise, they will get added multiple times.
        if (system && _parseList != null && _parseList.size() > 0) {
            return false;
        }

        Class<?> type = currentElement() == null ? null :
            ((ClassMetaData) currentElement()).getDescribedType();
        if (type == null)
            type = Object.class;

        if (_callbacks == null) {
            _callbacks = (Collection<LifecycleCallbacks>[])
                new Collection[LifecycleEvent.ALL_EVENTS.length];
            if (!system)
                _highs = new int[LifecycleEvent.ALL_EVENTS.length];
        }

        LifecycleCallbacks adapter;
        if (_listener != null)
            adapter = new BeanLifecycleCallbacks(_listener,
                attrs.getValue("method-name"), false, type);
        else
            adapter = new MethodLifecycleCallbacks(_cls,
                attrs.getValue("method-name"), false);

        for (int i = 0; i < events.length; i++) {
            int event = events[i];
            if (_listener != null) {
                MetaDataParsers.validateMethodsForSameCallback(_listener,
                    _callbacks[event], ((BeanLifecycleCallbacks) adapter).
                    getCallbackMethod(), callback, _conf, getLog());
            } else {
                MetaDataParsers.validateMethodsForSameCallback(_cls,
                    _callbacks[event], ((MethodLifecycleCallbacks) adapter).
                    getCallbackMethod(), callback, _conf, getLog());

            }
            if (_callbacks[event] == null)
                _callbacks[event] = new ArrayList<LifecycleCallbacks>(3);
            _callbacks[event].add(adapter);
            if (!system && _listener != null)
                _highs[event]++;
        }
        return true;
    }

    /**
     * Store lifecycle metadata.
     */
    private void storeCallbacks(ClassMetaData cls) {
        LifecycleMetaData meta = cls.getLifecycleMetaData();
        Class<?> supCls = cls.getDescribedType().getSuperclass();
        Collection<LifecycleCallbacks>[] supCalls = null;
        if (!Object.class.equals(supCls)) {
            supCalls = AnnotationPersistenceMetaDataParser.parseCallbackMethods
                (supCls, null, true, false, _repos);
        }
        if (supCalls != null) {
            for (int event : LifecycleEvent.ALL_EVENTS) {
                if (supCalls[event] == null)
                    continue;
                meta.setNonPCSuperclassCallbacks(event, supCalls[event].toArray
                    (new LifecycleCallbacks[supCalls[event].size()]), 0);
            }
        }
        if (_callbacks == null)
            return;

        for (int event : LifecycleEvent.ALL_EVENTS) {
            if (_callbacks[event] == null)
                continue;
            meta.setDeclaredCallbacks(event, (LifecycleCallbacks[])
                _callbacks[event].toArray
                    (new LifecycleCallbacks[_callbacks[event].size()]),
                _highs[event]);
        }
        _callbacks = null;
        _highs = null;
    }

    protected boolean startOrderColumn(Attributes attrs)
        throws SAXException {
        return true;
    }

    /**
     * Instantiate the given class, taking into account the default package.
	 */
	protected Class<?> classForName(String name)
		throws SAXException {
		if ("Entity".equals(name))
			return PersistenceCapable.class;
		return super.classForName(name, isRuntime());
	}

	/**
	 * Process all deferred embeddables using an unknown access type.
	 */
	protected void addDeferredEmbeddableMetaData() {
	    if (_embeddables != null && _embeddables.size() > 0) {
	        // Reverse iterate the array of remaining deferred embeddables
	        // since elements will be removed as they are processed.
	        Class<?>[] classes = _embeddables.keySet().toArray(
	            new Class<?>[_embeddables.size()]);
	        for (int i = classes.length - 1 ; i >= 0; i--) {
	            try {
	                Integer access = _embeddableAccess.get(classes[i]);
	                if (access == null) {
	                    access = AccessCode.UNKNOWN;
	                }
	                addDeferredEmbeddableMetaData(classes[i],
	                    access);
	            }
	            catch (Exception e) {
	                throw new MetaDataException(
	                    _loc.get("no-embeddable-metadata",
	                        classes[i].getName()), e);
	            }
	        }
	    }	
	}
	
    /**
     * Process all deferred embeddables and embeddable mapping overrides
     * for a given class.  This should only happen after the access type
     * of the embeddable is known.
     *
     * @param embedType embeddable class
     * @param access class level access for embeddable
     * @throws SAXException
     */
    protected void addDeferredEmbeddableMetaData(Class<?> embedType,
        int access) throws SAXException {
        ArrayList<MetaDataContext> fmds = _embeddables.get(embedType);
        if (fmds != null && fmds.size() > 0) {
            for (MetaDataContext md : fmds) {
                if (md instanceof FieldMetaData) {
                    FieldMetaData fmd = (FieldMetaData)md;
                    fmd.addEmbeddedMetaData(access);
                }
                else if (md instanceof ValueMetaData) {
                    ValueMetaData vmd = (ValueMetaData)md;
                    vmd.addEmbeddedMetaData(access);
                }
            }
            applyDeferredEmbeddableOverrides(embedType);
            // Clean up deferrals after they have been processed
            fmds.clear();
            _embeddables.remove(embedType);
        }
    }
    protected void setDeferredEmbeddableAccessType(Class<?> embedType,
        int access) {
        _embeddableAccess.put(embedType, access);
    }

    /*
     * Clear any deferred metadata
     */
    @Override
    protected void clearDeferredMetaData() {
        _embeddables.clear();
        _embeddableAccess.clear();
    }

    /*
     * Determines whether the embeddable type is deferred.
     */
    protected boolean isDeferredEmbeddable(Class<?> embedType,
        MetaDataContext fmd) {
        ArrayList<MetaDataContext> fmds = _embeddables.get(embedType);
        if (fmds != null) {
            return fmds.contains(fmd);
        }
        return false;
    }

    /*
     * Add the fmd to the defer list for for the given embeddable type
     */
    protected void deferEmbeddable(Class<?> embedType, MetaDataContext fmd) {
        ArrayList<MetaDataContext> fmds = _embeddables.get(embedType);
        if (fmds == null) {
            fmds = new ArrayList<MetaDataContext>();
            _embeddables.put(embedType, fmds);
        }
        fmds.add(fmd);
    }

    /*
     * Apply any deferred overrides.
     */
    protected void applyDeferredEmbeddableOverrides(Class<?> cls)
        throws SAXException {
    }

	/*
	 * Add the array of classes to the active parse list.
	 */
    public void addToParseList(ArrayList<Class<?>> parseList) {
        if (parseList == null)
            return;
        _parseList.addAll(parseList);
    }

    /*
     * Add the class to the active parse list.
     */
    public void addToParseList(Class<?> parentCls) {
        if (parentCls == null)
            return;
        _parseList.add(parentCls);
    }

    /*
     * Whether the active parse list contains the specified class.
     */
    public boolean parseListContains(Class<?> cls) {
        if (_parseList.size() == 0)
            return false;
        return _parseList.contains(cls);
    }

    /*
     * Returns the list of classes actively being parsed.
     */
    public ArrayList<Class<?>> getParseList() {
        return _parseList;
    }

    /*
     * Returns class currently being parsed.
     */
    public Class<?> getParseClass() {
        return _cls;
    }

    protected boolean startDelimitedIdentifiers() {
        return false;
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

    /**
     * Determines whether the ClassMetaData has been resolved more than once. Compares the current sourceName and
     * linenumber to the ones used to originally resolve the metadata.
     * 
     * @param meta The ClassMetaData to inspect.
     * @return true if the source was has already been resolved from a different location. Otherwise return false
     */
    protected boolean isDuplicateClass(ClassMetaData meta) {
        if (!StringUtils.equals(getSourceName(), meta.getSourceName())) {
            return true;
        }

        if (getLineNum() != meta.getLineNumber()) {
            return true;
        }
        return false;
    }
    
    /**
     * Determines whether the QueryMetaData has been resolved more than once.
     * @param meta QueryMetaData that has already been resolved. 
     * @return true if the QueryMetaData was defined in a different place - e.g. another line in orm.xml.
     */
    protected boolean isDuplicateQuery(QueryMetaData meta) { 
        if(! StringUtils.equals(getSourceName(), meta.getSourceName())) {
            return true;
        }
        if(getLineNum() != meta.getLineNumber()) { 
            return true;
        }
        return false; 
            
    }
    
    private int getLineNum() { 
        int lineNum = 0;
        Locator loc = getLocation().getLocator();
        if(loc != null ) {
            lineNum = loc.getLineNumber();
        }
        return lineNum;
    }
    
    private boolean startDatastoreId(Attributes attrs) 
            throws SAXException {
        MetaDataRepository repos = getRepository();
        ClassMetaData meta = repos.getCachedMetaData(_cls);
        
        //Set default value if not specified
        String strategy = attrs.getValue("strategy");
        if (StringUtils.isEmpty(strategy)) {
            strategy ="AUTO"    ;
        }
        GenerationType stratType = GenerationType.valueOf(strategy);
        
        AnnotationPersistenceMetaDataParser.parseDataStoreId(meta, stratType, 
            attrs.getValue("generator"));
        
        return true;
    }
    
    private boolean startDataCache(Attributes attrs) 
            throws SAXException {
        String enabledStr = attrs.getValue("enabled");
        boolean enabled = (Boolean) (StringUtils.isEmpty(enabledStr) ? true : 
            Boolean.parseBoolean(enabledStr));
        
        String timeoutStr = attrs.getValue("timeout");
        int timeout = (Integer) (StringUtils.isEmpty(timeoutStr) ? Integer.MIN_VALUE : 
            Integer.parseInt(timeoutStr));
        
        String name = attrs.getValue("name");
        name = StringUtils.isEmpty(name) ? "" : name;
        
        AnnotationPersistenceMetaDataParser.parseDataCache(getRepository().getCachedMetaData(_cls), 
            enabled, name, timeout);
            
        return true;
    }
    
    private boolean startExtendedStrategy(PersistenceStrategy ps, Attributes attrs) 
        throws SAXException {
        
        FieldMetaData fmd = (FieldMetaData) currentElement();
            parseExtendedStrategy(fmd, ps, attrs);
        
        return true;
    }
    
    private void endExtendedStrategy(PersistenceStrategy ps) 
        throws SAXException {
        if (ps == PERS 
            || ps == PERS_COLL
            || ps == PERS_MAP) {
            finishField();
        }
        
    }

    /**
     * Parse strategy specific attributes.
     */
    private void parseExtendedStrategy(FieldMetaData fmd,
        PersistenceStrategy strategy, Attributes attrs)
        throws SAXException {
        
        // The following attributes will be temporarily parsed for all strategy types. This
        // is because it is not clear which attributes should be supported for which strategies.
        // And more testing needs to be done to determine what actually works.
        // Right now they are limited by the schema. But, putting these here allows a temporary schema
        // update by a developer without requiring a corresponding code update.
        parseTypeAttr(fmd, attrs);
        parseElementTypeAttr(fmd, attrs);
        parseKeyTypeAttr(fmd, attrs);
        parseDependentAttr(fmd, attrs);
        parseElementDependentAttr(fmd, attrs);
        parseKeyDependentAttr(fmd, attrs);
        parseElementClassCriteriaAttr(fmd, attrs);
        parseLRSAttr(fmd, attrs);
        parseInverseLogicalAttr(fmd, attrs);
        parseEagerFetchModeAttr(fmd, attrs);
        
        switch (strategy) {
            case BASIC:
                parseExtendedBasic(fmd, attrs);
                break;
            case EMBEDDED:
                parseExtendedEmbedded(fmd, attrs);
                break;
            case ONE_ONE:
                parseExtendedOneToOne(fmd, attrs);
                break;
            case MANY_ONE:
                parseExtendedManyToOne(fmd, attrs);
                break;
            case MANY_MANY:
                parseExtendedManyToMany(fmd, attrs);
                break;
            case ONE_MANY:
                parseExtendedOneToMany(fmd, attrs);
                break;
            case ELEM_COLL:
                parseExtendedElementCollection(fmd, attrs);
        }
    }
    
    private void parseExtendedBasic(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        // TODO: Handle specific attributes
        
    }
    
    private void parseExtendedEmbedded(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs); 
        // TODO: Handle specific attributes
    }
    
    private void parseExtendedOneToOne(FieldMetaData fmd, Attributes attrs) 
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs); 
        // TODO: Handle specific attributes
    }
    
    private void parseExtendedManyToOne(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        // TODO: Handle specific attributes
    }
    
    private void parseExtendedManyToMany(FieldMetaData fmd, Attributes attrs) 
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        // TODO: Handle specific attributes
    }
    
    private void parseExtendedOneToMany(FieldMetaData fmd, Attributes attrs) 
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        // TODO: Handle specific attributes
            
    }
    
    private void parseExtendedElementCollection(FieldMetaData fmd, Attributes attrs) 
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        // TODO: Handle specific attributes
            
    }
    
    private void parsePersistent(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        parseTypeAttr(fmd, attrs);
        // TODO - handle attributes
        String val = attrs.getValue("fetch");
        if (val != null) {
            fmd.setInDefaultFetchGroup("EAGER".equals(val));
        }

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
    }
    
    private void parsePersistentCollection(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        parseElementTypeAttr(fmd, attrs);
        // TODO - handle attributes and field type
    }
    
    private void parsePersistentMap(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        parseCommonExtendedAttributes(fmd, attrs);
        parseElementTypeAttr(fmd, attrs);
        parseKeyTypeAttr(fmd, attrs);
        // TODO - handle attributes and field type
    }
    
    private void parseCommonExtendedAttributes(FieldMetaData fmd, Attributes attrs) {
        String loadFetchGroup = attrs.getValue("load-fetch-group");
        if (!StringUtils.isEmpty(loadFetchGroup)) {
            fmd.setLoadFetchGroup(loadFetchGroup);
        }
        
        String externalizer = attrs.getValue("externalizer");
        if (!StringUtils.isEmpty(externalizer)) {
            fmd.setExternalizer(externalizer);
        }
        
        String factory = attrs.getValue("factory");
        if (!StringUtils.isEmpty(factory)) {
            fmd.setFactory(factory);
        }
        
        parseStrategy(fmd, attrs);
    }
    
    protected void parseStrategy(FieldMetaData fmd, Attributes attrs) {
        
    }
    
    private boolean startReadOnly(Attributes attrs)
        throws SAXException {
        
        FieldMetaData fmd = (FieldMetaData) currentElement();
        String updateAction = attrs.getValue("update-action");
        
        if (updateAction.equalsIgnoreCase("RESTRICT")) {
            fmd.setUpdateStrategy(UpdateStrategies.RESTRICT);
        }
        else if (updateAction.equalsIgnoreCase("IGNORE")) {
            fmd.setUpdateStrategy(UpdateStrategies.IGNORE);
        }
        else
            throw new InternalException();
        
        return true;
    }
    
    private void parseDependentAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String dependentStr = attrs.getValue("dependent");
        if (!StringUtils.isEmpty(dependentStr)) {
            boolean dependent = Boolean.parseBoolean(dependentStr);
            if (dependent) {
                fmd.setCascadeDelete(ValueMetaData.CASCADE_AUTO);
            }
            else {
                fmd.setCascadeDelete(ValueMetaData.CASCADE_NONE);
            }
        }
    }
    
    private void parseElementDependentAttr(FieldMetaData fmd, Attributes attrs) 
        throws SAXException {
        
        String elementDependentStr = attrs.getValue("element-dependent");
        if (!StringUtils.isEmpty(elementDependentStr)) {
            boolean elementDependent = Boolean.parseBoolean(elementDependentStr);
            if (elementDependent) {
                fmd.getElement().setCascadeDelete(ValueMetaData.CASCADE_AUTO);
            }
            else {
                fmd.getElement().setCascadeDelete(ValueMetaData.CASCADE_NONE);
            }
        }
    }
    
    private void parseKeyDependentAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        
        String keyDependentStr = attrs.getValue("key-dependent");
        if (!StringUtils.isEmpty(keyDependentStr)) {
            boolean keyDependent = Boolean.parseBoolean(keyDependentStr);
            if (keyDependent) {
                fmd.getKey().setCascadeDelete(ValueMetaData.CASCADE_AUTO);
            }
            else {
                fmd.getKey().setCascadeDelete(ValueMetaData.CASCADE_NONE);
            }
        }
    }
    
    protected void parseElementClassCriteriaAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        
//        String elementClassCriteriaString = attrs.getValue("element-class-criteria");
//        if (!StringUtils.isEmpty(elementClassCriteriaString)) {
//            FieldMapping fm = (FieldMapping) fmd;
//            boolean elementClassCriteria = Boolean.parseBoolean(elementClassCriteriaString);
//            fm.getElementMapping().getValueInfo().setUseClassCriteria(elementClassCriteria);
//        }
    }
    
    private void parseTypeAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {

        String typeStr = attrs.getValue("type");
        if (!StringUtils.isEmpty(typeStr)) {
            if (StringUtils.endsWithIgnoreCase(typeStr, ".class")) {
                typeStr =
                    typeStr.substring(0, StringUtils.lastIndexOf(typeStr, '.'));
            }
            Class<?> typeCls = parseTypeStr(typeStr);

            fmd.setTypeOverride(typeCls);
        }
    }
    
    private void parseLRSAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        String lrsStr = attrs.getValue("lrs");
        if (!StringUtils.isEmpty(lrsStr)) {
            boolean lrs = Boolean.parseBoolean(lrsStr);
            fmd.setLRS(lrs);
        }
    }
    
    private void parseElementTypeAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {

        String typeStr = attrs.getValue("element-type");
        if (!StringUtils.isEmpty(typeStr)) {
            if (StringUtils.endsWithIgnoreCase(typeStr, ".class")) {
                typeStr =
                    typeStr.substring(0, StringUtils.lastIndexOf(typeStr, '.'));
            }
            Class<?> typeCls = parseTypeStr(typeStr);

            fmd.setTypeOverride(typeCls);
        }
    }
    
    private void parseKeyTypeAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {

        String typeStr = attrs.getValue("key-type");
        if (!StringUtils.isEmpty(typeStr)) {
            if (StringUtils.endsWithIgnoreCase(typeStr, ".class")) {
                typeStr =
                    typeStr.substring(0, StringUtils.lastIndexOf(typeStr, '.'));
            }
            Class<?> typeCls = parseTypeStr(typeStr);

            fmd.setTypeOverride(typeCls);
        }
    }
    
    private Class<?> parseTypeStr(String typeStr) 
        throws SAXException {
        Class<?> typeCls = null;
        try {
            if (typeStr.equalsIgnoreCase("int")) {
                typeCls = int.class;
            }
            else if (typeStr.equalsIgnoreCase("byte")) {
                typeCls = byte.class;
            }
            else if (typeStr.equalsIgnoreCase("short")) {
                typeCls = short.class;
            }
            else if (typeStr.equalsIgnoreCase("long")) {
                typeCls = long.class;
            }
            else if (typeStr.equalsIgnoreCase("float")) {
                typeCls = float.class;
            }
            else if (typeStr.equalsIgnoreCase("double")) {
                typeCls = double.class;
            }
            else if (typeStr.equalsIgnoreCase("boolean")) {
                typeCls = boolean.class;
            }
            else if (typeStr.equalsIgnoreCase("char")) {
                typeCls = char.class;
            }
            else {
                typeCls = Class.forName(typeStr);
            }
        } catch (ClassNotFoundException e) {
            throw new SAXException(e);
        }
        
        return typeCls;
    }
    
    private void parseInverseLogicalAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        
        String inverseLogical = attrs.getValue("inverse-logical");
        if (!StringUtils.isEmpty(inverseLogical)) {
            fmd.setInverse(inverseLogical);
        }
    }
    
    protected void parseEagerFetchModeAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
    }
    
    private boolean startExternalValues(Attributes attrs) 
        throws SAXException {
        
        _externalValues = new StringBuffer(10);
        
        return true;
    }
    
    private void endExternalValues() 
        throws SAXException {
        FieldMetaData fmd = (FieldMetaData) currentElement();
        fmd.setExternalValues(_externalValues.toString());
        _externalValues = null;
    }
    
    private boolean startExternalValue(Attributes attrs) 
        throws SAXException {
        
        if (_externalValues.length() > 0) {
            _externalValues.append(',');
        }
        _externalValues.append(attrs.getValue("java-value"));
        _externalValues.append('=');
        _externalValues.append(attrs.getValue("datastore-value"));
        
        return true;
    }
        
    private boolean startExternalizer(Attributes attrs)
        throws SAXException {
        
        return true;
    }
    
    private void endExternalizer() 
        throws SAXException {
        
        FieldMetaData fmd = (FieldMetaData) currentElement();
        String externalizer = currentText();
        fmd.setExternalizer(externalizer);
    }
    
    private boolean startFactory(Attributes attrs)
        throws SAXException {
        
        return true;
    }
    
    private void endFactory()
        throws SAXException {
        
        FieldMetaData fmd = (FieldMetaData) currentElement();
        String factory = currentText();
        fmd.setFactory(factory);
    }
    
    private boolean startFetchGroups(Attributes attrs) 
        throws SAXException {
        if (_fgList == null) {
            _fgList = new ArrayList<FetchGroupImpl>();
        }
        return true;
    }
    
    private boolean startFetchGroup(Attributes attrs) 
        throws SAXException {
        
        if (_fgList == null) {
            _fgList = new ArrayList<FetchGroupImpl>();
        }
        _currentFg = new AnnotationPersistenceMetaDataParser.FetchGroupImpl(attrs.getValue("name"), 
            Boolean.parseBoolean(attrs.getValue("post-load")));
        
        return true;
    }
    
    private void endFetchGroup()
        throws SAXException {
        
        String[] referencedFetchGroups = {};
        if (_referencedFgList != null &&_referencedFgList.size() > 0) {
            referencedFetchGroups = _referencedFgList.toArray(referencedFetchGroups);
        }
        _currentFg.setFetchGroups(referencedFetchGroups);
        
        FetchAttributeImpl[] fetchAttrs = {};
        if (_fetchAttrList != null && _fetchAttrList.size() > 0) {
            fetchAttrs = _fetchAttrList.toArray(fetchAttrs);
        }
        _currentFg.setAttributes(fetchAttrs);
        
        _fgList.add(_currentFg);
        _currentFg = null;
        _referencedFgList = null;
        _fetchAttrList = null;
    }
    
    private boolean startFetchAttribute(Attributes attrs)
        throws SAXException {
        if (_fetchAttrList == null) {
            _fetchAttrList = new ArrayList<FetchAttributeImpl>();
        }
        
        FetchAttributeImpl fetchAttribute = new FetchAttributeImpl(attrs.getValue("name"),
            Integer.parseInt(attrs.getValue("recursion-depth")));
        
        _fetchAttrList.add(fetchAttribute);
        
        return true;
    }
    
    private boolean startReferencedFetchGroup(Attributes attrs)
        throws SAXException {
        
        if (_referencedFgList == null) {
            _referencedFgList = new ArrayList<String>();
        }
        
        return true;
    }
    
    private void endReferencedFetchGroup()
        throws SAXException {
        
        _referencedFgList.add(currentText());
    }

    @Override
    protected void endExtendedClass(String elem) throws SAXException {
        ClassMetaData meta = (ClassMetaData) peekElement();
        
        if (_fgList != null) {
            // Handle fetch groups
            _fgs = new FetchGroupImpl[]{};
            _fgs = _fgList.toArray(_fgs);
            AnnotationPersistenceMetaDataParser.parseFetchGroups(meta, _fgs);
            _fgList = null;
            _fgs = null;
        }
    }
}
