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
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.metamodel.StaticMetamodel;

import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.GenericConfigurable;
import org.apache.openjpa.lib.meta.ClassAnnotationMetaDataFilter;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.meta.MetaDataFilter;
import org.apache.openjpa.lib.meta.MetaDataParser;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.AbstractCFMetaDataFactory;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataDefaults;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.MetaDataException;

/**
 * {@link MetaDataFactory} for JPA metadata.
 *
 * @author Steve Kim
 * @since 0.4.0
 * @nojavadoc
 */
public class PersistenceMetaDataFactory
    extends AbstractCFMetaDataFactory
    implements Configurable, GenericConfigurable {

    private static final Localizer _loc = Localizer.forPackage
        (PersistenceMetaDataFactory.class);

    private final PersistenceMetaDataDefaults _def = 
        new PersistenceMetaDataDefaults();
    private AnnotationPersistenceMetaDataParser _annoParser = null;
    private AnnotationPersistenceXMLMetaDataParser _annoXMLParser = null;
    private XMLPersistenceMetaDataParser _xmlParser = null;
    private Map<URL, Set<String>> _xml = null; // xml rsrc -> class names
    private Set<URL> _unparsed = null; // xml rsrc
    private boolean _fieldOverride = true;

    protected Stack<XMLPersistenceMetaDataParser> _stack = 
        new Stack<XMLPersistenceMetaDataParser>();

    /**
     * Whether to use field-level override or class-level override.
     * Defaults to true.
     */
    public void setFieldOverride(boolean field) {
        _fieldOverride = field;
    }

    /**
     * Whether to use field-level override or class-level override.
     * Defaults to true.
     */
    public boolean getFieldOverride() {
        return _fieldOverride;
    }

    /**
     * Return metadata parser, creating it if it does not already exist.
     */
    public AnnotationPersistenceMetaDataParser getAnnotationParser() {
        if (_annoParser == null) {
            _annoParser = newAnnotationParser();
            _annoParser.setRepository(repos);
        }
        return _annoParser;
    }

    /**
     * Set the metadata parser.
     */
    public void setAnnotationParser(
        AnnotationPersistenceMetaDataParser parser) {
        if (_annoParser != null)
            _annoParser.setRepository(null);
        if (parser != null)
            parser.setRepository(repos);
        _annoParser = parser;
    }

    /**
     * Create a new metadata parser.
     */
    protected AnnotationPersistenceMetaDataParser newAnnotationParser() {
        return new AnnotationPersistenceMetaDataParser
            (repos.getConfiguration());
    }

    /**
     * Create a new annotation serializer.
     */
    protected AnnotationPersistenceMetaDataSerializer
        newAnnotationSerializer() {
        return new AnnotationPersistenceMetaDataSerializer
            (repos.getConfiguration());
    }

    /**
     * Return XML metadata parser, creating it if it does not already exist or
     * if the existing parser is parsing.
     */
    public XMLPersistenceMetaDataParser getXMLParser() {
        if (_xmlParser == null || _xmlParser.isParsing()) {
            Class<?> parseCls = null;
            ArrayList<Class<?>> parseList = null;
            // If there is an existing parser and it is parsing, push it on
            // the stack and return a new one.
            if (_xmlParser != null) {
                _stack.push(_xmlParser);
                parseCls = _xmlParser.getParseClass();
                parseList = _xmlParser.getParseList();
            }
            _xmlParser = newXMLParser(true);
            _xmlParser.addToParseList(parseList);
            _xmlParser.addToParseList(parseCls);
            _xmlParser.setRepository(repos);
            if (_fieldOverride)
                _xmlParser.setAnnotationParser(getAnnotationParser());
        }
        return _xmlParser;
    }

    public void resetXMLParser() {
        // If a parser was pushed on the stack due to multi-level parsing, 
        // clear the current parser and pop the inner parser off the stack.
        if (!_stack.isEmpty()) {
            _xmlParser.clear();
            _xmlParser = _stack.pop();
        }
    }

    /**
     * Set the metadata parser.
     */
    public void setXMLParser(XMLPersistenceMetaDataParser parser) {
        if (_xmlParser != null)
            _xmlParser.setRepository(null);
        if (parser != null)
            parser.setRepository(repos);
        _xmlParser = parser;
    }

    /**
     * Create a new metadata parser.
     */
    protected XMLPersistenceMetaDataParser newXMLParser(boolean loading) {
        return new XMLPersistenceMetaDataParser(repos.getConfiguration());
    }

    /**
     * Create a new serializer
     */
    protected XMLPersistenceMetaDataSerializer newXMLSerializer() {
        return new XMLPersistenceMetaDataSerializer(repos.getConfiguration());
    }
    
    public void load(Class<?> cls, int mode, ClassLoader envLoader) {
        if (mode == MODE_NONE)
            return;
        if (!strict && (mode & MODE_META) != 0)
            mode |= MODE_MAPPING;

        // getting the list of persistent types runs callbacks to
        // mapPersistentTypeNames if it hasn't been called already, which
        // caches XML resources
        getPersistentTypeNames(false, envLoader);
        URL xml = findXML(cls);

        // we have to parse metadata up-front to register persistence unit
        // defaults and system callbacks
        ClassMetaData meta;
        boolean parsedXML = false;
        if (_unparsed != null && !_unparsed.isEmpty()
            && (mode & MODE_META) != 0) {
            Set<URL> unparsed = new HashSet<URL>(_unparsed);
            for (URL url : unparsed) {
                parseXML(url, cls, mode, envLoader);
            }
            parsedXML = unparsed.contains(xml);
             _unparsed.clear();

            // XML process check
            meta = repos.getCachedMetaData(cls);
            if (meta != null && (meta.getSourceMode() & mode) == mode) {
                validateStrategies(meta);
                return;
            }
        }

        // might have been looking for system-level query
        if (cls == null)
            return;

        // we may still need to parse XML if this is a redeploy of a class, or
        // if we're in strict query-only mode
        if (!parsedXML && xml != null) {
            parseXML(xml, cls, mode, envLoader);
            // XML process check
            meta = repos.getCachedMetaData(cls);
            if (meta != null && (meta.getSourceMode() & mode) == mode) {
                validateStrategies(meta);
                return;
            }
        }

        AnnotationPersistenceMetaDataParser parser = getAnnotationParser();
        parser.setEnvClassLoader(envLoader);
        parser.setMode(mode);
        parser.parse(cls);

        meta = repos.getCachedMetaData(cls);
        if (meta != null && (meta.getSourceMode() & mode) == mode)
            validateStrategies(meta);
    }

    /**
     * Parse the given XML resource.
     */
    private void parseXML(URL xml, Class<?> cls, int mode, 
    	ClassLoader envLoader) {
        // spring needs to use the envLoader first for all class resolution,
        // but we must still fall back on application loader
        ClassLoader loader = repos.getConfiguration().
            getClassResolverInstance().getClassLoader(cls, null);
        if (envLoader != null && envLoader != loader) {
          MultiClassLoader mult = new MultiClassLoader();
          mult.addClassLoader(envLoader);

          // loader from resolver is usually a multi loader itself
          if (loader instanceof MultiClassLoader)
            mult.addClassLoaders((MultiClassLoader)loader);
          else
            mult.addClassLoader(loader);
          loader = mult;
        }
    
        XMLPersistenceMetaDataParser xmlParser = getXMLParser();
        xmlParser.setClassLoader(loader);
        xmlParser.setEnvClassLoader(envLoader);
        xmlParser.setMode(mode);
        try {
            xmlParser.parse(xml);
        } catch (IOException ioe) {
            throw new GeneralException(ioe);
        }
        finally {
            resetXMLParser();
        }
    }

    /**
     * Locate the XML resource for the given class.
     */
    private URL findXML(Class<?> cls) {
        if (_xml != null && cls != null)
            for (Map.Entry<URL, Set<String>> entry : _xml.entrySet())
                if (entry.getValue().contains(cls.getName()))
                    return entry.getKey();
        return null;
    }

    @Override
    protected void mapPersistentTypeNames(Object rsrc, String[] names) {
        if (rsrc.toString().endsWith(".class")) {
            if (log.isTraceEnabled())
                log.trace(
                    _loc.get("map-persistent-types-skipping-class", rsrc));
            return;
        } else if (!(rsrc instanceof URL)) {
            if (log.isTraceEnabled())
                log.trace(
                    _loc.get("map-persistent-types-skipping-non-url", rsrc));
            return;
        } else if (rsrc.toString().endsWith("/")) {
            // OPENJPA-1546 If the rsrc URL is a directory it should not be
            // added to the list of the unparsed XML files
            if (log.isTraceEnabled())
                log.trace(_loc.get("map-persistent-types-skipping-dir", rsrc));
            return;
        }

        if (log.isTraceEnabled())
            log.trace(_loc.get(
                "map-persistent-type-names", rsrc, Arrays.asList(names)));
        
        if (_xml == null)
            _xml = new HashMap<URL, Set<String>>();
        _xml.put((URL) rsrc, new HashSet<String>(Arrays.asList(names)));

        if (_unparsed == null)
            _unparsed = new HashSet<URL>();
        _unparsed.add((URL) rsrc);
    }

    @Override
    public Class<?> getQueryScope(String queryName, ClassLoader loader) {
        if (queryName == null)
            return null;
        Collection<Class<?>> classes = repos.loadPersistentTypes(false, loader);
        for (Class<?> cls :  classes) {
            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, NamedQuery.class)))
                .booleanValue() && hasNamedQuery
                (queryName, (NamedQuery) cls.getAnnotation(NamedQuery.class)))
                return cls;
            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, NamedQueries.class)))
                .booleanValue() &&
                hasNamedQuery(queryName, ((NamedQueries) cls.
                    getAnnotation(NamedQueries.class)).value()))
                return cls;
            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, NamedNativeQuery.class)))
                .booleanValue() &&
                hasNamedNativeQuery(queryName, (NamedNativeQuery) cls.
                    getAnnotation(NamedNativeQuery.class)))
                return cls;
            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, NamedNativeQueries.class)))
                .booleanValue() &&
                hasNamedNativeQuery(queryName, ((NamedNativeQueries) cls.
                    getAnnotation(NamedNativeQueries.class)).value()))
                return cls;
        }
        return null;
    }

    @Override
    public Class<?> getResultSetMappingScope(String rsMappingName,
        ClassLoader loader) {
        if (rsMappingName == null)
            return null;
        
        Collection<Class<?>> classes = repos.loadPersistentTypes(false, loader);
        for (Class<?> cls : classes) {

            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, SqlResultSetMapping.class)))
                .booleanValue() &&
                hasRSMapping(rsMappingName, (SqlResultSetMapping) cls.
                getAnnotation(SqlResultSetMapping.class)))
                return cls;

            if ((AccessController.doPrivileged(J2DoPrivHelper
                .isAnnotationPresentAction(cls, SqlResultSetMappings.class)))
                .booleanValue() &&
                hasRSMapping(rsMappingName, ((SqlResultSetMappings) cls.
                getAnnotation(SqlResultSetMappings.class)).value()))
                return cls;
        }
        return null;
    }

    private boolean hasNamedQuery(String query, NamedQuery... queries) {
        for (NamedQuery q : queries) {
            if (query.equals(q.name()))
                return true;
        }
        return false;
    }

    private boolean hasRSMapping(String rsMapping,
        SqlResultSetMapping... mappings) {
        for (SqlResultSetMapping m : mappings) {
            if (rsMapping.equals(m.name()))
                return true;
        }
        return false;
    }

    private boolean hasNamedNativeQuery(String query,
        NamedNativeQuery... queries) {
        for (NamedNativeQuery q : queries) {
            if (query.equals(q.name()))
                return true;
        }
        return false;
    }

    @Override
    protected MetaDataFilter newMetaDataFilter() {
        ClassAnnotationMetaDataFilter camdf = new ClassAnnotationMetaDataFilter(
                new Class[] { Entity.class, Embeddable.class,
                        MappedSuperclass.class });
        camdf.setLog(log);
        return camdf;
    }

    /**
     * Ensure all fields have declared a strategy.
     */
    private void validateStrategies(ClassMetaData meta) {
        StringBuilder buf = null;
        for (FieldMetaData fmd : meta.getDeclaredFields()) {
            if (!fmd.isExplicit()) {
                if (buf == null)
                    buf = new StringBuilder();
                else
                    buf.append(", ");
                buf.append(fmd);
            }
        }
        if (buf != null)
            throw new MetaDataException(_loc.get("no-pers-strat", buf));
    }

    public MetaDataDefaults getDefaults() {
        return _def;
    }

    @Override
    public ClassArgParser newClassArgParser() {
        ClassArgParser parser = new ClassArgParser();
        parser.setMetaDataStructure("package", null, new String[]{
            "entity", "embeddable", "mapped-superclass" }, "class");
        return parser;
    }

    @Override
    public void clear() {
        super.clear();
        if (_annoParser != null)
            _annoParser.clear();
        if (_xmlParser != null)
            _xmlParser.clear();
        if (_xml != null)
            _xml.clear();
    }

    protected Parser newParser(boolean loading) {
        return newXMLParser(loading);
    }

    protected Serializer newSerializer() {
        return newXMLSerializer();
    }

    @Override
    protected void parse(MetaDataParser parser, Class[] cls) {
        parse(parser, Collections.singleton(defaultXMLFile()));
    }

    protected File defaultSourceFile(ClassMetaData meta) {
        return defaultXMLFile();
    }

    protected File defaultSourceFile(QueryMetaData query, Map clsNames) {
        ClassMetaData meta = getDefiningMetaData(query, clsNames);
        File file = (meta == null) ? null : meta.getSourceFile();
        if (file != null)
            return file;
        return defaultXMLFile();
    }

    protected File defaultSourceFile(SequenceMetaData seq, Map clsNames) {
        return defaultXMLFile();
    }

    /**
     * Look for META-INF/orm.xml, and if it doesn't exist, choose a default.
     */
    private File defaultXMLFile() {
        ClassLoader loader = repos.getConfiguration().
            getClassResolverInstance().getClassLoader(getClass(), null);
        URL rsrc = AccessController.doPrivileged(
            J2DoPrivHelper.getResourceAction(loader, "META-INF/orm.xml"));
        if (rsrc != null) {
            File file = new File(rsrc.getFile());
            if ((AccessController.doPrivileged(
                J2DoPrivHelper.existsAction(file))).booleanValue())
                return file;
        }
        return new File(dir, "orm.xml");
    }

    public void setConfiguration(Configuration conf) {
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
        if (rsrcs == null)
            rsrcs = Collections.singleton("META-INF/orm.xml");
        else
			rsrcs.add("META-INF/orm.xml");
	}

    public void setInto(Options opts) {
        opts.keySet().retainAll(opts.setInto(_def).keySet());
    }

    /**
     * Return JAXB XML annotation parser, 
     * creating it if it does not already exist.
     */
    public AnnotationPersistenceXMLMetaDataParser getXMLAnnotationParser() {
        if (_annoXMLParser == null) {
            _annoXMLParser = newXMLAnnotationParser();
            _annoXMLParser.setRepository(repos);
        }
        return _annoXMLParser;
    }

    /**
     * Set the JAXB XML annotation parser.
     */
    public void setXMLAnnotationParser(
        AnnotationPersistenceXMLMetaDataParser parser) {
        if (_annoXMLParser != null)
            _annoXMLParser.setRepository(null);
        if (parser != null)
            parser.setRepository(repos);
        _annoXMLParser = parser;
    }

    /**
     * Create a new JAXB XML annotation parser.
     */
    protected AnnotationPersistenceXMLMetaDataParser newXMLAnnotationParser() {
        return new AnnotationPersistenceXMLMetaDataParser
            (repos.getConfiguration());
    }

    public void loadXMLMetaData(Class<?> cls) {
        AnnotationPersistenceXMLMetaDataParser parser
            = getXMLAnnotationParser();
        parser.parse(cls);
    }
    
    private static String UNDERSCORE = "_";
    
    public String getManagedClassName(String mmClassName) {
        if (mmClassName == null || mmClassName.length() == 0)
            return null;
        if (mmClassName.endsWith(UNDERSCORE))
            return mmClassName.substring(0, mmClassName.length()-1);
        return mmClassName;
    }

    public String getMetaModelClassName(String managedClassName) {
        if (managedClassName == null || managedClassName.length() == 0)
            return null;
        return managedClassName + UNDERSCORE;
    }

    public boolean isMetaClass(Class<?> c) {
        return c != null && c.getAnnotation(StaticMetamodel.class) != null;
    }
    
    public Class<?> getManagedClass(Class<?> c) {
        if (isMetaClass(c)) {
            return c.getAnnotation(StaticMetamodel.class).value();
        }
        return null;
    }
}
