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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MappedSuperclass;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.QueryHint;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.JavaVersions;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataInheritanceComparator;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.Order;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.InternalException;

import serp.util.Strings;

//@todo: javadocs

/**
 * Serializes persistence metadata as annotations.
 * This class processes all object level tags that are store-agnostic.
 * However, it provides hooks for the subclasses to include store-specific
 * tags to be serialized both at &lt;entity-mappings&gt; and
 * &lt;entity&gt; level.
 *
 * @since 1.0.0
 * @author Steve Kim
 * @author Gokhan Ergul
 * @nojavadoc
 */
public class AnnotationPersistenceMetaDataSerializer
    implements PersistenceMetaDataFactory.Serializer {

    // NOTE: order is important! these constants must be maintained in
    // serialization order. constants are spaced so that subclasses can
    // slip tags in-between
    protected static final int TYPE_SEQ = 10;
    protected static final int TYPE_QUERY = 20;
    protected static final int TYPE_META = 30;
    protected static final int TYPE_CLASS_SEQS = 40;
    protected static final int TYPE_CLASS_QUERIES = 50;

    private static final Localizer _loc = Localizer.forPackage
        (AnnotationPersistenceMetaDataSerializer.class);

    private Log _log = null;
    
    private final OpenJPAConfiguration _conf;
    private Map<String, ClassMetaData> _metas = null;
    private Map<String, List> _queries = null;
    private Map<String, List> _seqs = null;
    private int _mode = MetaDataModes.MODE_NONE;
    private SerializationComparator _comp = null;

    private Map<ClassMetaData, List<AnnotationBuilder>> _clsAnnos = null;
    private Map<FieldMetaData, List<AnnotationBuilder>> _fldAnnos = null;
    private Map<SequenceMetaData, List<AnnotationBuilder>> _seqAnnos = null;
    private Map<QueryMetaData, List<AnnotationBuilder>> _qryAnnos = null; 

    /**
     * Constructor. Supply configuration.
     */
    public AnnotationPersistenceMetaDataSerializer(OpenJPAConfiguration conf) {
        _conf = conf;
        setLog(conf.getLog(OpenJPAConfiguration.LOG_METADATA));
        setMode(MetaDataModes.MODE_META | MetaDataModes.MODE_MAPPING |
            MetaDataModes.MODE_QUERY);
    }

    /**
     * Configuration.
     */
    public OpenJPAConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * The log to write to.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * The log to write to.
     */
    public void setLog(Log log) {
        _log = log;
    }
    
    /**
     * The serialization mode according to the expected document type. The
     * mode constants act as bit flags, and therefore can be combined.
     */
    public int getMode() {
        return _mode;
    }

    /**
     * The serialization mode according to the expected document type. The
     * mode constants act as bit flags, and therefore can be combined.
     */
    public void setMode(int mode) {
        _mode = mode;
    }

    /**
     * The serialization mode according to the expected document type.
     */
    public void setMode(int mode, boolean on) {
        if (mode == MetaDataModes.MODE_NONE)
            setMode(MetaDataModes.MODE_NONE);
        else if (on)
            setMode(_mode | mode);
        else
            setMode(_mode & ~mode);
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isMetaDataMode() {
        return (_mode & MetaDataModes.MODE_META) != 0;
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isQueryMode() {
        return (_mode & MetaDataModes.MODE_QUERY) != 0;
    }

    /**
     * Convenience method for interpreting {@link #getMode}.
     */
    protected boolean isMappingMode() {
        return (_mode & MetaDataModes.MODE_MAPPING) != 0;
    }

    /**
     * Convenience method for interpreting {@link #getMode}. Takes into
     * account whether mapping information is loaded for the given instance.
     * OPENJPA-1360 - Allow @Column attributes when meta.isEmbeddedOnly()
     */
    protected boolean isMappingMode(ClassMetaData meta) {
        return isMappingMode() && (meta.getSourceMode()
            & MetaDataModes.MODE_MAPPING) != 0
            && (meta.getEmbeddingMetaData() == null
            || isMappingMode(meta.getEmbeddingMetaData()));
    }

    /**
     * Convenience method for interpreting {@link #getMode}. Takes into
     * account whether mapping information is loaded for the given instance.
     */
    protected boolean isMappingMode(ValueMetaData vmd) {
        return isMappingMode(vmd.getFieldMetaData().getDefiningMetaData());
    }

    /**
     * Add a class meta data to the set to be serialized.
     */
    public void addMetaData(ClassMetaData meta) {
        if (meta == null)
            return;

        if (_metas == null)
            _metas = new HashMap<String, ClassMetaData>();
        _metas.put(meta.getDescribedType().getName(), meta);
    }

    /**
     * Add a sequence meta data to the set to be serialized.
     */
    public void addSequenceMetaData(SequenceMetaData meta) {
        if (meta == null)
            return;

        List seqs = null;
        String defName = null;
        if (meta.getSourceScope() instanceof Class)
            defName = ((Class) meta.getSourceScope()).getName();
        if (_seqs == null)
            _seqs = new HashMap<String, List>();
        else
            seqs = _seqs.get(defName);

        if (seqs == null) {
            seqs = new ArrayList(3); // don't expect many seqs / class
            seqs.add(meta);
            _seqs.put(defName, seqs);
        } else if (!seqs.contains(meta))
            seqs.add(meta);
    }

    /**
     * Add a query meta data to the set to be serialized.
     */
    public void addQueryMetaData(QueryMetaData meta) {
        if (meta == null)
            return;

        List queries = null;
        String defName = null;
        if (meta.getSourceScope() instanceof Class)
            defName = ((Class) meta.getSourceScope()).getName();
        if (_queries == null)
            _queries = new HashMap<String, List>();
        else
            queries = _queries.get(defName);

        if (queries == null) {
            queries = new ArrayList(3); // don't expect many queries / class
            queries.add(meta);
            _queries.put(defName, queries);
        } else if (!queries.contains(meta))
            queries.add(meta);
    }

    /**
     * Add all components in the given repository to the set to be serialized.
     */
    public void addAll(MetaDataRepository repos) {
        if (repos == null)
            return;

        for (ClassMetaData meta : repos.getMetaDatas())
            addMetaData(meta);
        for (SequenceMetaData seq : repos.getSequenceMetaDatas())
            addSequenceMetaData(seq);
        for (QueryMetaData query : repos.getQueryMetaDatas())
            addQueryMetaData(query);
    }

    /**
     * Remove a metadata from the set to be serialized.
     *
     * @return true if removed, false if not in set
     */
    public boolean removeMetaData(ClassMetaData meta) {
        return _metas != null && meta != null
            && _metas.remove(meta.getDescribedType().getName()) != null;
    }

    /**
     * Remove a sequence metadata from the set to be serialized.
     *
     * @return true if removed, false if not in set
     */
    public boolean removeSequenceMetaData(SequenceMetaData meta) {
        if (_seqs == null || meta == null)
            return false;
        String defName = null;
        if (meta.getSourceScope() instanceof Class)
            defName = ((Class) meta.getSourceScope()).getName();
        List seqs = _seqs.get(defName);
        if (seqs == null)
            return false;
        if (!seqs.remove(meta))
            return false;
        if (seqs.isEmpty())
            _seqs.remove(defName);
        return true;
    }

    /**
     * Remove a query metadata from the set to be serialized.
     *
     * @return true if removed, false if not in set
     */
    public boolean removeQueryMetaData(QueryMetaData meta) {
        if (_queries == null || meta == null)
            return false;
        String defName = null;
        if (meta.getSourceScope() instanceof Class)
            defName = ((Class) meta.getSourceScope()).getName();
        List queries = _queries.get(defName);
        if (queries == null)
            return false;
        if (!queries.remove(meta))
            return false;
        if (queries.isEmpty())
            _queries.remove(defName);
        return true;
    }

    /**
     * Remove all the components in the given repository from the set to be
     * serialized.
     *
     * @return true if any components removed, false if none in set
     */
    public boolean removeAll(MetaDataRepository repos) {
        if (repos == null)
            return false;

        boolean removed = false;
        ClassMetaData[] metas = repos.getMetaDatas();
        for (int i = 0; i < metas.length; i++)
            removed |= removeMetaData(metas[i]);
        SequenceMetaData[] seqs = repos.getSequenceMetaDatas();
        for (int i = 0; i < seqs.length; i++)
            removed |= removeSequenceMetaData(seqs[i]);
        QueryMetaData[] queries = repos.getQueryMetaDatas();
        for (int i = 0; i < queries.length; i++)
            removed |= removeQueryMetaData(queries[i]);
        return removed;
    }

    /**
     * Clear the set of metadatas to be serialized.
     */
    public void clear() {
        if (_metas != null)
            _metas.clear();
        if (_seqs != null)
            _seqs.clear();
        if (_queries != null)
            _queries.clear();
    }

    /**
     * Add system-level mapping elements to be serialized. Does nothing
     * by default.
     */
    protected void addSystemMappingElements(Collection toSerialize) {
    }

    /**
     * Sort the given collection of objects to be serialized.
     */
    private void serializationSort(List objs) {
        if (objs == null || objs.isEmpty())
            return;
        if (_comp == null)
            _comp = newSerializationComparator();
        Collections.sort(objs, _comp);
    }

    /**
     * Create a new comparator for ordering objects that are to be serialized.
     */
    protected SerializationComparator newSerializationComparator() {
        return _comp;
    }

    /**
     * Add sequence metadata to the given metadatas collection.
     */
    private void addSequenceMetaDatas(Collection all) {
        if (_seqs == null)
            return;

        for (Map.Entry entry : _seqs.entrySet()) {
            if (entry.getKey() == null)
                all.addAll((List) entry.getValue());
            else if (_metas == null || !_metas.containsKey(entry.getKey()))
                all.add(new ClassSeqs((List<SequenceMetaData>)
                    entry.getValue()));
        }
    }

    /**
     * Add query metadata to the given metadatas collection.
     */
    private void addQueryMetaDatas(Collection all) {
        if (_queries == null)
            return;

        for (Map.Entry entry : _queries.entrySet()) {
            if (entry.getKey() == null)
                all.addAll((List) entry.getValue());
            else if (_mode == MetaDataModes.MODE_QUERY || _metas == null
                || !_metas.containsKey(entry.getKey()))
                all.add(new ClassQueries((List<QueryMetaData>)
                    entry.getValue()));
        }
    }

    /**
     * Creates a new annotation builder for the specified annotation type.
     * @return
     */
    protected AnnotationBuilder newAnnotationBuilder(
        Class<? extends Annotation> annType) {
        return new AnnotationBuilder(annType);        
    }

    protected void addAnnotation(AnnotationBuilder ab, Object meta) {
        if (meta instanceof ClassMetaData)
            addAnnotation(ab, (ClassMetaData) meta);
        else if (meta instanceof FieldMetaData)
            addAnnotation(ab, (FieldMetaData) meta);
        else if (meta instanceof SequenceMetaData)
            addAnnotation(ab, (SequenceMetaData) meta);
        else if (meta instanceof QueryMetaData)
            addAnnotation(ab, (QueryMetaData) meta);
    }

    /**
     * Add an annotation builder to list of builders for the specified
     * class metadata.
     */
    protected void addAnnotation(AnnotationBuilder ab, ClassMetaData meta) {
        if (_clsAnnos == null)
            _clsAnnos = new HashMap<ClassMetaData, List<AnnotationBuilder>>();
        List<AnnotationBuilder> list = _clsAnnos.get(meta);
        if (list == null) {
            list = new ArrayList<AnnotationBuilder>();
            _clsAnnos.put(meta, list);
        }
        list.add(ab);        
    }

    /**
     * Add an annotation builder to list of builders for the specified
     * field metadata.
     */
    protected void addAnnotation(AnnotationBuilder ab, FieldMetaData meta) {
        if (_fldAnnos == null)
            _fldAnnos = new HashMap<FieldMetaData, List<AnnotationBuilder>>();
        List<AnnotationBuilder> list = _fldAnnos.get(meta);
        if (list == null) {
            list = new ArrayList<AnnotationBuilder>();
            _fldAnnos.put(meta, list);
        }
        list.add(ab);
    }

    /**
     * Add an annotation builder to list of builders for the specified
     * sequence metadata.
     */
    protected void addAnnotation(AnnotationBuilder ab, SequenceMetaData meta) {
        if (_seqAnnos == null)
            _seqAnnos = new HashMap<SequenceMetaData,
                    List<AnnotationBuilder>>();
        List<AnnotationBuilder> list = _seqAnnos.get(meta);
        if (list == null) {
            list = new ArrayList<AnnotationBuilder>();
            _seqAnnos.put(meta, list);
        }
        list.add(ab);
    }

    /**
     * Add an annotation builder to list of builders for the specified
     * query metadata.
     */
    protected void addAnnotation(AnnotationBuilder ab, QueryMetaData meta) {
        if (_qryAnnos == null)
            _qryAnnos = new HashMap<QueryMetaData, List<AnnotationBuilder>>();
        List<AnnotationBuilder> list = _qryAnnos.get(meta);
        if (list == null) {
            list = new ArrayList<AnnotationBuilder>();
            _qryAnnos.put(meta, list);
        }
        list.add(ab);
    }

    /**
     * Creates an an annotation builder for the specified class metadata
     * and adds it to list of builders.
     */
    protected AnnotationBuilder addAnnotation(
        Class<? extends Annotation> annType, ClassMetaData meta) {
        AnnotationBuilder ab = newAnnotationBuilder(annType);
        if (meta == null)
            return ab;
        addAnnotation(ab, meta);
        return ab;
    }

    /**
     * Creates an an annotation builder for the specified class metadata
     * and adds it to list of builders.
     */
    protected AnnotationBuilder addAnnotation(
        Class<? extends Annotation> annType, FieldMetaData meta) {
        AnnotationBuilder ab = newAnnotationBuilder(annType);
        if (meta == null)
            return ab;
        addAnnotation(ab, meta);
        return ab;
    }

    /**
     * Creates an an annotation builder for the specified class metadata
     * and adds it to list of builders.
     */
    protected AnnotationBuilder addAnnotation(
        Class<? extends Annotation> annType, SequenceMetaData meta) {
        AnnotationBuilder ab = newAnnotationBuilder(annType);
        if (meta == null)
            return ab;
        addAnnotation(ab, meta);
        return ab;
    }

    /**
     * Creates an an annotation builder for the specified class metadata
     * and adds it to list of builders.
     */
    protected AnnotationBuilder addAnnotation(
        Class<? extends Annotation> annType, QueryMetaData meta) {
        AnnotationBuilder ab = newAnnotationBuilder(annType);
        if (meta == null)
            return ab;
        addAnnotation(ab, meta);
        return ab;
    }
    
    protected void serialize(Collection objects) {
        for (Object obj : objects) {
            int type = type(obj);
            switch (type) {
                case TYPE_META:
                    serializeClass((ClassMetaData) obj);
                    break;
                case TYPE_SEQ:
                    if (isMappingMode())
                        serializeSequence((SequenceMetaData) obj);
                    break;
                case TYPE_QUERY:
                    serializeQuery((QueryMetaData) obj);
                    break;
                case TYPE_CLASS_QUERIES:
                    for (QueryMetaData query : ((ClassQueries) obj)
                        .getQueries())
                        serializeQuery(query);
                    break;
                case TYPE_CLASS_SEQS:
                    if (isMappingMode())
                        for (SequenceMetaData seq : ((ClassSeqs) obj)
                            .getSequences())
                            serializeSequence(seq);
                    break;
                default:
                    if (isMappingMode())
                        serializeSystemMappingElement(obj);
                    break;
            }
        }
    }

    /**
     * Return the type constant for the given object based on its runtime
     * class. If the runtime class does not correspond to any of the known
     * types then returns -1. This can happen for tags
     * that are not handled at this store-agnostic level.
     */
    protected int type(Object o) {
        if (o instanceof ClassMetaData)
            return TYPE_META;
        if (o instanceof QueryMetaData)
            return TYPE_QUERY;
        if (o instanceof SequenceMetaData)
            return TYPE_SEQ;
        if (o instanceof ClassQueries)
            return TYPE_CLASS_QUERIES;
        if (o instanceof ClassSeqs)
            return TYPE_CLASS_SEQS;
        return -1;
    }

    /**
     * Serialize unknown mapping element at system level.
     */
    protected void serializeSystemMappingElement(Object obj) {
    }

    /**
     * Serialize query metadata.
     */
    private void serializeQuery(QueryMetaData meta) {
        Log log = getLog();
        if (log.isInfoEnabled()) {
            if (meta.getSourceScope() instanceof Class)
                log.info(_loc.get("ser-cls-query",
                    meta.getSourceScope(), meta.getName()));
            else
                log.info(_loc.get("ser-query", meta.getName()));
        }

        Class<? extends Annotation> ann =
            QueryLanguages.LANG_SQL.equals(meta.getLanguage()) ?
            NamedNativeQuery.class : NamedQuery.class;
        AnnotationBuilder abQry = addAnnotation(ann, meta);
        abQry.add("name", meta.getName());
        abQry.add("query", meta.getQueryString());
        if (QueryLanguages.LANG_SQL.equals(meta.getLanguage())) {
            if (meta.getResultType() != null)
                abQry.add("resultClass", meta.getResultType());
        }
        serializeQueryHints(meta, abQry);
    }

    /**
     * Serialize query hints.
     */
    private void serializeQueryHints(QueryMetaData meta, AnnotationBuilder ab) {
        String[] hints = meta.getHintKeys();
        Object[] values = meta.getHintValues();
        for (int i = 0; i < hints.length; i++) {
            AnnotationBuilder abHint = newAnnotationBuilder(QueryHint.class);
            abHint.add("name", hints[i]);
            abHint.add("value", String.valueOf(values[i]));
            ab.add("hints", abHint);
        }
    }

    /**
     * Serialize sequence metadata.
     */
    protected void serializeSequence(SequenceMetaData meta) {
        Log log = getLog();
        if (log.isInfoEnabled())
            log.info(_loc.get("ser-sequence", meta.getName()));

        AnnotationBuilder ab = addAnnotation(SequenceGenerator.class, meta);
        ab.add("name", meta.getName());

        // parse out the datastore sequence name, if any
        String plugin = meta.getSequencePlugin();
        String clsName = Configurations.getClassName(plugin);
        String props = Configurations.getProperties(plugin);
        String ds = null;
        if (props != null) {
            Properties map = Configurations.parseProperties(props);
            ds = (String) map.remove("Sequence");
            if (ds != null) {
                props = Configurations.serializeProperties(map);
                plugin = Configurations.getPlugin(clsName, props);
            }
        }

        if (ds != null)
            ab.add("sequenceName", ds);
        else if (plugin != null && !SequenceMetaData.IMPL_NATIVE.equals
            (plugin))
            ab.add("sequenceName", plugin);
        if (meta.getInitialValue() != 0 && meta.getInitialValue() != -1)
            ab.add("initialValue", meta.getInitialValue());
        if (meta.getAllocate() != 50 && meta.getAllocate() != -1)
            ab.add("allocationSize", meta.getAllocate());
        if (meta.getSchema() != null)
            ab.add("schema", meta.getSchema());
        if (meta.getCatalog() != null)
            ab.add("catalog", meta.getCatalog());
    }

    /**
     * Serialize class metadata.
     */
    protected void serializeClass(ClassMetaData meta) {
        Log log = getLog();
        if (log.isInfoEnabled())
            log.info(_loc.get("ser-class", meta));

        AnnotationBuilder abEntity = addAnnotation(
            getEntityAnnotationType(meta), meta);
        if (isMetaDataMode()
            && !meta.getTypeAlias().equals(Strings.getClassName(meta.
            getDescribedType())))
            abEntity.add("name", meta.getTypeAlias());
        
        if (isMappingMode())
            addClassMappingAnnotations(meta);

        if (isMappingMode())
            serializeClassMappingContent(meta);
        if (isMetaDataMode())
            serializeIdClass(meta);
        if (isMappingMode())
            serializeInheritanceContent(meta);

        if (isMappingMode()) {
            List seqs = (_seqs == null) ? null : _seqs.get
                (meta.getDescribedType().getName());
            if (seqs != null) {
                serializationSort(seqs);
                for (int i = 0; i < seqs.size(); i++)
                    serializeSequence((SequenceMetaData) seqs.get(i));
            }
        }

        if (isQueryMode()) {
            List queries = (_queries == null) ? null : _queries.get
                (meta.getDescribedType().getName());
            if (queries != null) {
                serializationSort(queries);
                for (int i = 0; i < queries.size(); i++)
                    serializeQuery((QueryMetaData) queries.get(i));
            }
            if (isMappingMode())
                serializeQueryMappings(meta);
        }

        List<FieldMetaData> fields = new ArrayList(Arrays.asList
            (meta.getDefinedFieldsInListingOrder()));
        Collections.sort(fields, new FieldComparator());

        // serialize attr-override
        if (isMappingMode()) {
            FieldMetaData fmd;
            FieldMetaData orig;
            for (Iterator<FieldMetaData> it = fields.iterator(); it.hasNext();)
            {
                fmd = it.next();
                if (meta.getDefinedSuperclassField(fmd.getName()) == null)
                    continue;
                orig = meta.getPCSuperclassMetaData().getField(fmd.getName());
                if (serializeAttributeOverride(fmd, orig))
                    serializeAttributeOverrideContent(fmd, orig);
                it.remove();
            }
        }

        if (fields.size() > 0 && (isMetaDataMode() || isMappingMode())) {
            FieldMetaData orig;
            for (FieldMetaData fmd : fields) {
                if (fmd.getDeclaringType() != fmd.getDefiningMetaData().
                    getDescribedType()) {
                    orig = fmd.getDeclaringMetaData().getDeclaredField
                        (fmd.getName());
                } else
                    orig = null;
                serializeField(fmd, orig);
            }
        }
    }

    /**
     * Return entity annotation type.
     */
    private static Class<? extends Annotation> getEntityAnnotationType(
        ClassMetaData meta) {
        switch (getEntityTag(meta)) {
            case ENTITY:
                return Entity.class;
            case EMBEDDABLE:
                return Embeddable.class;
            case MAPPED_SUPERCLASS:
                return MappedSuperclass.class;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Return field annotation type.
     */
    private static Class<? extends Annotation> getFieldAnnotationType (
        FieldMetaData fmd, PersistenceStrategy strat) {
        Class<? extends Annotation> ann = null;
        if (fmd.isPrimaryKey() && strat == PersistenceStrategy.EMBEDDED)
            ann = EmbeddedId.class;
        else if (fmd.isPrimaryKey())
            ann = Id.class;
        else if (fmd.isVersion())
            ann = Version.class;
        else {
            switch (strat) {
                case TRANSIENT:
                    ann = Transient.class;
                    break;
                case BASIC:
                    ann = Basic.class;
                    break;
                case EMBEDDED:
                    ann = Embedded.class;
                    break;
                case MANY_ONE:
                    ann = ManyToOne.class;
                    break;
                case ONE_ONE:
                    ann = OneToOne.class;
                    break;
                case ONE_MANY:
                    ann = OneToMany.class;
                    break;
                case MANY_MANY:
                    ann = ManyToMany.class;
                    break;
            }
        }
        return ann;
    }

    /**
     * Return the MetaDataTag for the given class meta data.
     */
    private static MetaDataTag getEntityTag(ClassMetaData meta) {
        if (meta.isAbstract())
            return MetaDataTag.MAPPED_SUPERCLASS;
        // @Embeddable classes can't declare Id fields
        if (meta.isEmbeddedOnly() && meta.getPrimaryKeyFields().length == 0)
            return MetaDataTag.EMBEDDABLE;
        if (meta.isMapped())
            return MetaDataTag.ENTITY;
        return MetaDataTag.MAPPED_SUPERCLASS;
    }

    /**
     * Add mapping attributes for the given class. Does nothing by default
     */
    protected void addClassMappingAnnotations(ClassMetaData mapping) {
    }

    /**
     * Serialize id-class.
     */
    private void serializeIdClass(ClassMetaData meta) {
        if (meta.getIdentityType() != ClassMetaData.ID_APPLICATION
            || meta.isOpenJPAIdentity())
            return;

        ClassMetaData sup = meta.getPCSuperclassMetaData();
        Class oid = meta.getObjectIdType();
        if (oid != null && (sup == null || oid != sup.getObjectIdType())) {
            AnnotationBuilder ab = addAnnotation(IdClass.class, meta);
            ab.add(null, oid);
        }
    }

    /**
     * Serialize class mapping content. Does nothing by default.
     */
    protected void serializeClassMappingContent(ClassMetaData mapping) {
    }

    /**
     * Serialize inheritance content. Does nothing by default.
     */
    protected void serializeInheritanceContent(ClassMetaData mapping) {
    }

    /**
     * Serialize query mappings. Does nothing by default.
     */
    protected void serializeQueryMappings(ClassMetaData meta) {
    }

    /**
     * Serialize the given field.
     */
    private void serializeField(FieldMetaData fmd, FieldMetaData orig) {
        if (fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT
            && !fmd.isExplicit())
            return;

        PersistenceStrategy strat = getStrategy(fmd);
        ValueMetaData cascades = null;
        AnnotationBuilder ab = addAnnotation(
            getFieldAnnotationType (fmd, strat), fmd);        
        if (fmd.isPrimaryKey() && strat == PersistenceStrategy.EMBEDDED)
            ; // noop
        else if (fmd.isPrimaryKey())
            ; // noop
        else if (fmd.isVersion())
            ; // noop
        else {
            switch (strat) {
                case BASIC:
                    if (isMetaDataMode())
                        addBasicAttributes(fmd, ab);
                    break;
                case MANY_ONE:
                    if (isMetaDataMode())
                        addManyToOneAttributes(fmd, ab);
                    cascades = fmd;
                    break;
                case ONE_ONE:
                    if (isMetaDataMode())
                        addOneToOneAttributes(fmd, ab);
                    cascades = fmd;
                    break;
                case ONE_MANY:
                    if (isMetaDataMode())
                        addOneToManyAttributes(fmd, ab);
                    cascades = fmd.getElement();
                    break;
                case MANY_MANY:
                    if (isMetaDataMode())
                        addManyToManyAttributes(fmd, ab);
                    cascades = fmd.getElement();
                    break;
            }
            if (isMappingMode())
                addStrategyMappingAttributes(fmd, ab);
        }
        if (isMappingMode(fmd))
            addFieldMappingAttributes(fmd, orig, ab);

        if (fmd.getOrderDeclaration() != null) {
            if (!(Order.ELEMENT + " asc").equals(fmd.getOrderDeclaration()))
                addAnnotation(OrderBy.class, fmd).
                    add (null, fmd.getOrderDeclaration());
        }
        if (isMappingMode() && fmd.getKey().getValueMappedBy() != null) {
            AnnotationBuilder abMapKey = addAnnotation(MapKey.class, fmd);
            FieldMetaData mapBy = fmd.getKey().getValueMappedByMetaData();
            if (!mapBy.isPrimaryKey() ||
                mapBy.getDefiningMetaData().getPrimaryKeyFields().length != 1) {
                abMapKey.add("name", fmd.getKey().getValueMappedBy());
            }
        }
        if (isMappingMode(fmd))
            serializeFieldMappingContent(fmd, strat, ab);
        if (cascades != null && isMetaDataMode())
            serializeCascades(cascades, ab);
    }

    /**
     * Add mapping attributes for the given field. Does nothing by default.
     */
    protected void addFieldMappingAttributes(FieldMetaData fmd,
        FieldMetaData orig, AnnotationBuilder ab) {
    }
    
    /**
     * Always returns false by default.
     */
    protected boolean serializeAttributeOverride(FieldMetaData fmd,
        FieldMetaData orig) {
        return false;
    }

    /**
     * Serialize attribute override content.
     */
    private void serializeAttributeOverrideContent(FieldMetaData fmd,
        FieldMetaData orig) {
        AnnotationBuilder ab = addAnnotation(AttributeOverride.class, fmd);
        ab.add("name", fmd.getName());
        serializeAttributeOverrideMappingContent(fmd, orig, ab);
    }

    /**
     * Serialize attribute override mapping content. Does nothing by default,
     */
    protected void serializeAttributeOverrideMappingContent
        (FieldMetaData fmd, FieldMetaData orig, AnnotationBuilder ab) {
    }


    /**
     * Serialize cascades.
     */
    private void serializeCascades(ValueMetaData vmd, AnnotationBuilder ab) {
        EnumSet<CascadeType> cascades = EnumSet.noneOf(CascadeType.class);
        if (vmd.getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE) {
            cascades.add(CascadeType.PERSIST);
        }
        if (vmd.getCascadeAttach() == ValueMetaData.CASCADE_IMMEDIATE) {
            cascades.add(CascadeType.MERGE);
        }
        if (vmd.getCascadeDelete() == ValueMetaData.CASCADE_IMMEDIATE) {
            cascades.add(CascadeType.REMOVE);
        }
        if (vmd.getCascadeRefresh() == ValueMetaData.CASCADE_IMMEDIATE) {
            cascades.add(CascadeType.REFRESH);
        }
        if (vmd.getCascadeDetach() == ValueMetaData.CASCADE_IMMEDIATE) {
            cascades.add(CascadeType.DETACH);
        }
        if (cascades.size() == 5) // ALL
        {
            cascades.clear();
            cascades.add(CascadeType.ALL);
        }
        if (!cascades.isEmpty()) {
            ab.add("cascade", cascades);
        }
    }

    /**
     * Return the serialized strategy name.
     */
    protected PersistenceStrategy getStrategy(FieldMetaData fmd) {
        if (fmd.getManagement() == fmd.MANAGE_NONE)
            return PersistenceStrategy.TRANSIENT;

        if (fmd.isSerialized()
            || fmd.getDeclaredType() == byte[].class
            || fmd.getDeclaredType() == Byte[].class
            || fmd.getDeclaredType() == char[].class
            || fmd.getDeclaredType() == Character[].class)
            return PersistenceStrategy.BASIC;

        FieldMetaData mappedBy;
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.PC:
                if (fmd.isEmbedded())
                    return PersistenceStrategy.EMBEDDED;
                if (fmd.getMappedBy() != null)
                    return PersistenceStrategy.ONE_ONE;
                FieldMetaData[] inverses = fmd.getInverseMetaDatas();
                if (inverses.length == 1 &&
                    inverses[0].getTypeCode() == JavaTypes.PC &&
                    inverses[0].getMappedByMetaData() == fmd) {
                    return PersistenceStrategy.ONE_ONE;
                }
                return PersistenceStrategy.MANY_ONE;
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                mappedBy = fmd.getMappedByMetaData();
                if (mappedBy == null || mappedBy.getTypeCode() != JavaTypes.PC)
                    return PersistenceStrategy.MANY_MANY;
                return PersistenceStrategy.ONE_MANY;
            case JavaTypes.OID:
                return PersistenceStrategy.EMBEDDED;
            default:
                return PersistenceStrategy.BASIC;
        }
    }

    /**
     * Add basic attributes.
     */
    private void addBasicAttributes(FieldMetaData fmd, AnnotationBuilder ab) {
        if (!fmd.isInDefaultFetchGroup())
            ab.add("fetch", FetchType.LAZY);
        if (fmd.getNullValue() == FieldMetaData.NULL_EXCEPTION)
            ab.add("optional", false);
    }

    /**
     * Add many-to-one attributes.
     */
    private void addManyToOneAttributes(FieldMetaData fmd,
        AnnotationBuilder ab) {
        if (!fmd.isInDefaultFetchGroup())
            ab.add("fetch", FetchType.LAZY);
        if (fmd.getNullValue() == FieldMetaData.NULL_EXCEPTION)
            ab.add("optional", false);
    }

    /**
     * Add one-to-one attributes.
     */
    private void addOneToOneAttributes(FieldMetaData fmd,
        AnnotationBuilder ab) {
        if (!fmd.isInDefaultFetchGroup())
            ab.add("fetch", FetchType.LAZY);
        if (fmd.getNullValue() == FieldMetaData.NULL_EXCEPTION)
            ab.add("optional", false);
    }

    /**
     * Add one-to-many attributes.
     */
    private void addOneToManyAttributes(FieldMetaData fmd,
        AnnotationBuilder ab) {
        if (fmd.isInDefaultFetchGroup())
            ab.add("fetch", FetchType.EAGER);
        addTargetEntityAttribute(fmd, ab);
    }

    /**
     * Add many-to-many attributes.
     */
    private void addManyToManyAttributes(FieldMetaData fmd,
        AnnotationBuilder ab) {
        if (fmd.isInDefaultFetchGroup())
            ab.add("fetch", FetchType.EAGER);
        addTargetEntityAttribute(fmd, ab);
    }

    /**
     * Add a target-entity attribute to collection and map fields that do
     * not use generics.
     */
    private void addTargetEntityAttribute(FieldMetaData fmd,
        AnnotationBuilder ab) {
        Member member = fmd.getBackingMember();
        Class[] types;
        if (member instanceof Field)
            types = JavaVersions.getParameterizedTypes((Field) member);
        else if (member instanceof Method)
            types = JavaVersions.getParameterizedTypes((Method) member);
        else
            types = new Class[0];

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.COLLECTION:
                if (types.length != 1)
                    ab.add("targetEntity", fmd.getElement().getDeclaredType());
                break;
            case JavaTypes.MAP:
                if (types.length != 2)
                    ab.add("targetEntity", fmd.getElement().getDeclaredType());
                break;
        }
    }

    /**
     * Serialize field mapping content; this will be called before
     * {@link #serializeValueMappingContent}. Does nothing by default.
     */
    protected void serializeFieldMappingContent(FieldMetaData fmd,
        PersistenceStrategy strategy, AnnotationBuilder ab) {
    }

    /**
     * Set mapping attributes for strategy. Sets mapped-by by default.
     */
    protected void addStrategyMappingAttributes(FieldMetaData fmd,
        AnnotationBuilder ab) {
        if (fmd.getMappedBy() != null)
            ab.add("mappedBy", fmd.getMappedBy());
    }
    
    protected Collection getObjects() {
        List all = new ArrayList();
        if (isQueryMode())
            addQueryMetaDatas(all);
        if (isMappingMode())
            addSequenceMetaDatas(all);
        if ((isMetaDataMode() || isMappingMode()) && _metas != null)
            all.addAll(_metas.values());
        if (isMappingMode())
            addSystemMappingElements(all);
        serializationSort(all);
        return all;
    }

    protected void writeAnnotations(Object meta,
        List<AnnotationBuilder> builders, Map output) {
        List<String> annos = new ArrayList<String>();
        for(AnnotationBuilder ab: builders)
            annos.add(ab.toString());
        output.put(meta, annos);        
    }

    public void serialize(Map output, int flags) throws IOException {
        Collection all = getObjects();
        serialize(all);

        if (_clsAnnos != null)
            for (ClassMetaData meta : _clsAnnos.keySet())
                writeAnnotations(meta, _clsAnnos.get(meta), output);
        if (_fldAnnos != null)
            for (FieldMetaData meta : _fldAnnos.keySet())
                writeAnnotations(meta, _fldAnnos.get(meta), output);
        if (_seqAnnos != null)
            for (SequenceMetaData meta : _seqAnnos.keySet())
                writeAnnotations(meta, _seqAnnos.get(meta), output);
        if (_qryAnnos != null)
            for (QueryMetaData meta : _qryAnnos.keySet())
                writeAnnotations(meta, _qryAnnos.get(meta), output);
    }

    public void serialize(File file, int flags) throws IOException {
        try {
            FileWriter out = new FileWriter(AccessController
                .doPrivileged(J2DoPrivHelper.getCanonicalPathAction(file)),
                (flags & APPEND) > 0);
            serialize(out, flags);
            out.close();
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        }
    }

    @SuppressWarnings("unchecked")
    public void serialize(Writer out, int flags) throws IOException {
        Map output = new HashMap();
        serialize(output, flags);

        Set<Entry> entrySet = output.entrySet();
        for(Entry entry : entrySet) {
            Object meta = entry.getKey();
            List<String> annos = (List<String>) entry.getValue();

            out.write("--"+meta.toString());
            out.write("\n");
            for(String ann: annos) {
                out.write("\t");
                out.write(ann);
                out.write("\n");
            }
        }
    }

    public void serialize(int flags) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Represents ordered set of 
     * {@link org.apache.openjpa.meta.SequenceMetaData}s with a common class
     * scope.
     *
     * @author Stephen Kim
     * @author Pinaki Poddar
     */
    private static class ClassSeqs
        implements SourceTracker, Comparable<ClassSeqs>,
        Comparator<SequenceMetaData> {

        private final SequenceMetaData[] _seqs;

        public ClassSeqs(List<SequenceMetaData> seqs) {
            if (seqs == null || seqs.isEmpty())
                throw new InternalException();

            _seqs = (SequenceMetaData[]) seqs.toArray
                (new SequenceMetaData[seqs.size()]);
            Arrays.sort(_seqs, this);
        }

        public SequenceMetaData[] getSequences() {
            return _seqs;
        }

        /**
         * Compare sequence metadata on name.
         */
        public int compare(SequenceMetaData o1, SequenceMetaData o2) {
            return o1.getName().compareTo(o2.getName());
        }

        public File getSourceFile() {
            return _seqs[0].getSourceFile();
        }

        public Object getSourceScope() {
            return _seqs[0].getSourceScope();
        }

        public int getSourceType() {
            return _seqs[0].getSourceType();
        }

        public String getResourceName() {
            return _seqs[0].getResourceName();
        }

        public int getLineNumber() {
            return _seqs[0].getLineNumber();
        }

        public int getColNumber() {
            return _seqs[0].getColNumber();
        }
        
        public int compareTo(ClassSeqs other) {
            if (other == this)
                return 0;
            if (other == null)
                return -1;
            Class scope = (Class) getSourceScope();
            Class oscope = (Class) other.getSourceScope();
            return scope.getName().compareTo(oscope.getName());
        }
    }

    /**
     * Represents ordered set of {@link org.apache.openjpa.meta.QueryMetaData}s
     * with a common class scope.
     *
     * @author Stephen Kim
     * @author Pinaki Poddar
     */
    private static class ClassQueries
        implements SourceTracker, Comparable<ClassQueries>,
        Comparator<QueryMetaData> {

        private final QueryMetaData[] _queries;

        public ClassQueries(List<QueryMetaData> queries) {
            if (queries == null || queries.isEmpty())
                throw new InternalException();

            _queries = (QueryMetaData[]) queries.toArray
                (new QueryMetaData[queries.size()]);
            Arrays.sort(_queries, this);
        }

        public QueryMetaData[] getQueries() {
            return _queries;
        }

        /**
         * Compare query metadata. Normal queries appear before native queries.
         * If the given queries use same language, then their names are
         * compared.
         */
        public int compare(QueryMetaData o1, QueryMetaData o2) {
            // normal queries before native
            if (!StringUtils.equals(o1.getLanguage(), o2.getLanguage())) {
                if (QueryLanguages.LANG_SQL.equals(o1.getLanguage()))
                    return 1;
                else
                    return -1;
            }
            return o1.getName().compareTo(o2.getName());
        }

        public File getSourceFile() {
            return _queries[0].getSourceFile();
        }

        public Object getSourceScope() {
            return _queries[0].getSourceScope();
        }

        public int getSourceType() {
            return _queries[0].getSourceType();
        }

        public String getResourceName() {
            return _queries[0].getResourceName();
        }

        public int getLineNumber() {
            return _queries[0].getLineNumber();
        }

        public int getColNumber() {
            return _queries[0].getColNumber();
        }
        
        public int compareTo(ClassQueries other) {
            if (other == this)
                return 0;
            if (other == null)
                return -1;
            Class scope = (Class) getSourceScope();
            Class oscope = (Class) other.getSourceScope();
            return scope.getName().compareTo(oscope.getName());
        }
    }

    /**
     * Compares clases, sequences, and queries to order them for serialization.
     * Places sequences first, then classes, then queries. Sequences and
     * queries are ordered alphabetically by name. Classes are placed in
     * listing order, in inheritance order within that, and in alphabetical
     * order within that.
     *
     * @author Stephen Kim
     */
    protected class SerializationComparator
        extends MetaDataInheritanceComparator {

        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;

            int t1 = type(o1);
            int t2 = type(o2);
            if (t1 != t2)
                return t1 - t2;

            switch (t1) {
                case TYPE_META:
                    return compare((ClassMetaData) o1, (ClassMetaData) o2);
                case TYPE_QUERY:
                    return compare((QueryMetaData) o1, (QueryMetaData) o2);
                case TYPE_SEQ:
                    return compare((SequenceMetaData) o1,
                        (SequenceMetaData) o2);
                case TYPE_CLASS_QUERIES:
                    return ((Comparable) o1).compareTo(o2);
                case TYPE_CLASS_SEQS:
                    return ((Comparable) o1).compareTo(o2);
                default:
                    return compareUnknown(o1, o2);
            }
        }

        /**
         * Compare two unrecognized elements of the same type. Throws
         * exception by default.
         */
        protected int compareUnknown(Object o1, Object o2) {
            throw new InternalException();
        }

        /**
         * Compare between two class metadata.
         */
        private int compare(ClassMetaData o1, ClassMetaData o2) {
            int li1 = o1.getListingIndex();
            int li2 = o2.getListingIndex();
            if (li1 == -1 && li2 == -1) {
                MetaDataTag t1 = getEntityTag(o1);
                MetaDataTag t2 = getEntityTag(o2);
                if (t1.compareTo(t2) != 0)
                    return t1.compareTo(t2);
                int inher = super.compare(o1, o2);
                if (inher != 0)
                    return inher;
                return o1.getDescribedType().getName().compareTo
                    (o2.getDescribedType().getName());
            }

            if (li1 == -1)
                return 1;
            if (li2 == -1)
                return -1;
            return li1 - li2;
        }

        /**
         * Compare query metadata.
         */
        private int compare(QueryMetaData o1, QueryMetaData o2) {
            // normal queries before native
            if (!StringUtils.equals(o1.getLanguage(), o2.getLanguage())) {
                if (QueryLanguages.LANG_SQL.equals(o1.getLanguage()))
                    return 1;
                else
                    return -1;
            }
            return o1.getName().compareTo(o2.getName());
        }

        /**
         * Compare sequence metadata.
         */
        private int compare(SequenceMetaData o1, SequenceMetaData o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * Sorts fields according to listing order, then XSD strategy order,
     * then name order.
     */
    private class FieldComparator
        implements Comparator {

        public int compare(Object o1, Object o2) {
            FieldMetaData fmd1 = (FieldMetaData) o1;
            FieldMetaData fmd2 = (FieldMetaData) o2;
            if (fmd1.isPrimaryKey()) {
                if (fmd2.isPrimaryKey())
                    return fmd1.compareTo(fmd2);
                return -1;
            }
            if (fmd2.isPrimaryKey())
                return 1;

            if (fmd1.isVersion()) {
                if (fmd2.isVersion())
                    return compareListingOrder(fmd1, fmd2);
                return getStrategy(fmd2) == PersistenceStrategy.BASIC ? 1 : -1;
			}
			if (fmd2.isVersion())
                return getStrategy(fmd1) == PersistenceStrategy.BASIC ? -1 : 1;

            int stcmp = getStrategy(fmd1).compareTo(getStrategy(fmd2));
            if (stcmp != 0)
                return stcmp;
            return compareListingOrder(fmd1, fmd2);
        }

        private int compareListingOrder(FieldMetaData fmd1, FieldMetaData fmd2){
            int lcmp = fmd1.getListingIndex() - fmd2.getListingIndex();
            if (lcmp != 0)
                return lcmp;
            return fmd1.compareTo(fmd2);
		}
	}
	
	/**
	 * Returns the stored ClassMetaData
	 */
	public Map<String, ClassMetaData> getClassMetaData() {
	    return _metas;
	}
}
