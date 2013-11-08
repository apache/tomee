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
package org.apache.openjpa.jdbc.meta;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.kernel.JDBCSeq;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.DynamicSchemaFactory;
import org.apache.openjpa.jdbc.schema.LazySchemaFactory;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGenerator;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.SchemaSerializer;
import org.apache.openjpa.jdbc.schema.SchemaTool;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.XMLSchemaSerializer;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.kernel.Seq;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.ClassArgParser;
import org.apache.openjpa.lib.meta.MetaDataSerializer;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.lib.util.Services;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Tool for manipulating class mappings and associated schema.
 *
 * @author Abe White
 */
public class MappingTool
    implements MetaDataModes {

    public static final String SCHEMA_ACTION_NONE = "none";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_REFRESH = "refresh";
    public static final String ACTION_BUILD_SCHEMA = "buildSchema";
    public static final String ACTION_DROP = "drop";
    public static final String ACTION_VALIDATE = "validate";
    public static final String ACTION_EXPORT = "export";
    public static final String ACTION_IMPORT = "import";

    public static final String[] ACTIONS = new String[]{
        ACTION_ADD,
        ACTION_REFRESH,
        ACTION_BUILD_SCHEMA,
        ACTION_DROP,
        ACTION_VALIDATE,
        ACTION_EXPORT,
        ACTION_IMPORT,
    };

    private static final Localizer _loc = Localizer.forPackage(MappingTool.class);

    private final JDBCConfiguration _conf;
    private final Log _log;
    private final String _action;
    private final boolean _meta;
    private final int _mode;
    private final DBDictionary _dict;

    private MappingRepository _repos = null;
    private SchemaGroup _schema = null;
    private SchemaTool _schemaTool = null;
    private String _schemaActions = SchemaTool.ACTION_ADD;
    private boolean _readSchema = false;
    private boolean _pks = false;
    private boolean _fks = false;
    private boolean _indexes = false;
    private boolean _seqs = true;
    private boolean _dropUnused = true;
    private boolean _ignoreErrors = false;
    private File _file = null;
    private Writer _mappingWriter = null;
    private Writer _schemaWriter = null;

    // buffer metadatas to be dropped
    private Set<Class<?>> _dropCls = null;
    private Set<ClassMapping> _dropMap = null;
    private boolean _flush = false;
    private boolean _flushSchema = false;

    /**
     * Constructor. Supply configuration and action.
     */
    public MappingTool(JDBCConfiguration conf, String action, boolean meta) {
        _conf = conf;
        _log = conf.getLog(JDBCConfiguration.LOG_METADATA);
        _meta = meta;

        if (action == null)
            _action = ACTION_REFRESH;
        else if (!Arrays.asList(ACTIONS).contains(action))
            throw new IllegalArgumentException("action == " + action);
        else
            _action = action;

        if (meta && ACTION_ADD.equals(_action))
            _mode = MODE_META;
        else if (meta && ACTION_DROP.equals(_action))
            _mode = MODE_META | MODE_MAPPING | MODE_QUERY;
        else
            _mode = MODE_MAPPING;

        _dict = _conf.getDBDictionaryInstance();
    }

    /**
     * The action supplied on construction.
     */
    public String getAction() {
        return _action;
    }

    /**
     * Whether the action works on metadata as well as mappings.
     */
    public boolean isMetaDataAction() {
        return _meta;
    }

    /**
     * The schema modification policy, or <code>none</code>. See the
     * ACTION constants in {@link SchemaTool}. May be a comma-separated
     * list of values. Defaults to {@link SchemaTool#ACTION_ADD}.
     */
    public String getSchemaAction() {
        return _schemaActions;
    }

    /**
     * The schema modification policy, or <code>none</code>. See the
     * ACTION constants in {@link SchemaTool}. May be a comma-separated
     * list of values. Defaults to {@link SchemaTool#ACTION_ADD}.
     */
    public void setSchemaAction(String schemaAction) {
        _schemaActions = schemaAction;
    }

    /**
     * Set to true to read the entire schema before mapping.
     * Leaving this option false saves time, but is dangerous when adding
     * new mappings, because without full knowledge of the existing schema the
     * mapping tool might create tables or indexes that conflict with
     * existing components.
     */
    public boolean getReadSchema() {
        return _readSchema;
    }

    /**
     * Set to true to read the entire schema before mapping.
     * Leaving this option false saves time, but is dangerous when adding
     * new mappings, because without full knowledge of the existing schema the
     * mapping tool might create tables or indexes that conflict with
     * existing components.
     */
    public void setReadSchema(boolean readSchema) {
        _readSchema = readSchema;
    }

    /**
     * Whether to manipulate sequences. Defaults to true.
     */
    public boolean getSequences() {
        return _seqs;
    }

    /**
     * Whether to manipulate sequences. Defaults to true.
     */
    public void setSequences(boolean seqs) {
        _seqs = seqs;
    }

    /**
     * Whether indexes on existing tables should be manipulated.
     * Defaults to false.
     */
    public boolean getIndexes() {
        return _indexes;
    }

    /**
     * Whether indexes on existing tables should be manipulated.
     * Defaults to false.
     */
    public void setIndexes(boolean indexes) {
        _indexes = indexes;
    }

    /**
     * Whether foreign keys on existing tables should be manipulated.
     * Defaults to false.
     */
    public boolean getForeignKeys() {
        return _fks;
    }

    /**
     * Whether foreign keys on existing tables should be manipulated.
     * Defaults to false.
     */
    public void setForeignKeys(boolean fks) {
        _fks = fks;
    }

    /**
     * Whether primary keys on existing tables should be manipulated.
     * Defaults to false.
     */
    public boolean getPrimaryKeys() {
        return _pks;
    }

    /**
     * Whether primary keys on existing tables should be manipulated.
     * Defaults to false.
     */
    public void setPrimaryKeys(boolean pks) {
        _pks = pks;
    }

    /**
     * Whether schema components that are unused by any mapping will be
     * dropped from this tool's {@link SchemaGroup}, and, depending on
     * the schema action, from the database. Defaults to true.
     */
    public boolean getDropUnusedComponents() {
        return _dropUnused;
    }

    /**
     * Whether schema components that are unused by any mapping will be
     * dropped from this tool's {@link SchemaGroup}, and, depending on
     * the schema action, from the database. Defaults to true.
     */
    public void setDropUnusedComponents(boolean dropUnused) {
        _dropUnused = dropUnused;
    }

    /**
     * Whether and SQL errors should cause a failure or just issue a warning.
     */
    public void setIgnoreErrors(boolean ignoreErrors) {
        _ignoreErrors = ignoreErrors;
    }

    /**
     * Whether and SQL errors should cause a failure or just issue a warning.
     */
    public boolean getIgnoreErrors() {
        return _ignoreErrors;
    }

    /**
     * Return the schema tool to use for schema modification.
     */
    private SchemaTool newSchemaTool(String action) {
        if (SCHEMA_ACTION_NONE.equals(action))
            action = null;
        SchemaTool tool = new SchemaTool(_conf, action);
        tool.setIgnoreErrors(getIgnoreErrors());
        tool.setPrimaryKeys(getPrimaryKeys());
        tool.setForeignKeys(getForeignKeys());
        tool.setIndexes(getIndexes());
        tool.setSequences(getSequences());
        return tool;
    }

    /**
     * Set the schema tool to use for schema modification.
     */
    public void setSchemaTool(SchemaTool tool) {
        _schemaTool = tool;
    }

    /**
     * The stream to export the planned schema to as an XML document.
     * If non-null, then the database schema will not be altered.
     */
    public Writer getSchemaWriter() {
        return _schemaWriter;
    }

    /**
     * The stream to export the planned schema to as an XML document.
     * If non-null, then the database schema will not be altered.
     */
    public void setSchemaWriter(Writer schemaWriter) {
        _schemaWriter = schemaWriter;
    }

    /**
     * The stream to export the planned mappings to as an XML document.
     * If non-null, then the mapping repository will not be altered.
     */
    public Writer getMappingWriter() {
        return _mappingWriter;
    }

    /**
     * The stream to export the planned mappings to as an XML document.
     * If non-null, then the mapping repository will not be altered.
     */
    public void setMappingWriter(Writer mappingWriter) {
        _mappingWriter = mappingWriter;
    }

    /**
     * If adding metadata, the metadata file to add to.
     */
    public File getMetaDataFile() {
        return _file;
    }

    /**
     * If adding metadata, the metadata file to add to.
     */
    public void setMetaDataFile(File file) {
        _file = file;
    }

    /**
     * Return the repository to use to access mapping information.
     * Defaults to a new {@link MappingRepository}.
     */
    public MappingRepository getRepository() {
        if (_repos == null) {
            _repos = _conf.newMappingRepositoryInstance();
            _repos.setSchemaGroup(getSchemaGroup());
            _repos.setValidate(MetaDataRepository.VALIDATE_UNENHANCED, false);
        }
        return _repos;
    }

    /**
     * Set the repository to use to access mapping information.
     */
    public void setRepository(MappingRepository repos) {
        _repos = repos;
    }

    /**
     * Return the schema group to use in mapping. If none has been set, the
     * schema will be generated from the database.
     */
    public SchemaGroup getSchemaGroup() {
        if (_schema == null) {
            if (_action.indexOf(ACTION_BUILD_SCHEMA) != -1) {
                DynamicSchemaFactory factory = new DynamicSchemaFactory();
                factory.setConfiguration(_conf);
                _schema = factory;
            } else if (_readSchema 
                || contains(_schemaActions,SchemaTool.ACTION_RETAIN)
                || contains(_schemaActions,SchemaTool.ACTION_REFRESH)) {
                _schema = (SchemaGroup) newSchemaTool(null).getDBSchemaGroup().
                    clone();
            } else {
                // with this we'll just read tables as different mappings
                // look for them
                LazySchemaFactory factory = new LazySchemaFactory();
                factory.setConfiguration(_conf);
                factory.setPrimaryKeys(getPrimaryKeys());
                factory.setForeignKeys(getForeignKeys());
                factory.setIndexes(getIndexes());
                _schema = factory;
            }

            if (_schema.getSchemas().length == 0)
                _schema.addSchema();
        }
        return _schema;
    }

    /**
     * Set the schema to use in mapping.
     */
    public void setSchemaGroup(SchemaGroup schema) {
        _schema = schema;
    }

    /**
     * Reset the internal repository. This is called automatically after
     * every {@link #record}.
     */
    public void clear() {
        _repos = null;
        _schema = null;
        _schemaTool = null;
        _flush = false;
        _flushSchema = false;
        if (_dropCls != null)
            _dropCls.clear();
        if (_dropMap != null)
            _dropMap.clear();
    }

    /**
     * Records the changes that have been made to both the mappings and the
     * associated schema, and clears the tool for further use. This also
     * involves clearing the internal mapping repository.
     */
    public void record() {
        record(null);
    }
    
    public void record(MappingTool.Flags flags) {
        MappingRepository repos = getRepository();
        MetaDataFactory io = repos.getMetaDataFactory();
        ClassMapping[] mappings;
        
        if (!ACTION_DROP.equals(_action))
            mappings = repos.getMappings();
        else if (_dropMap != null)
            mappings = (ClassMapping[]) _dropMap.toArray
                (new ClassMapping[_dropMap.size()]);
        else
            mappings = new ClassMapping[0];

        try {
            if (_dropCls != null && !_dropCls.isEmpty()) {
                Class<?>[] cls = (Class[]) _dropCls.toArray
                    (new Class[_dropCls.size()]);
                if (!io.drop(cls, _mode, null))
                    _log.warn(_loc.get("bad-drop", _dropCls));
            }

            if (_flushSchema) {
                // drop portions of the known schema that no mapping uses, and
                // add sequences used for value generation
                if (_dropUnused)
                    dropUnusedSchemaComponents(mappings);
                addSequenceComponents(mappings);

                // now run the schematool as long as we're doing some schema
                // action and the user doesn't just want an xml output
                String[] schemaActions = _schemaActions.split(",");
                for (int i = 0; i < schemaActions.length; i++) {
                    if (!SCHEMA_ACTION_NONE.equals(schemaActions[i])
                        && (_schemaWriter == null || (_schemaTool != null
                            && _schemaTool.getWriter() != null))) {
                        SchemaTool tool = newSchemaTool(schemaActions[i]);

                        // configure the tool with additional settings
                        if (flags != null) {
                            tool.setDropTables(flags.dropTables);
                            tool.setDropSequences(flags.dropSequences);
                            tool.setWriter(flags.sqlWriter);
                            tool.setOpenJPATables(flags.openjpaTables);
                            tool.setSQLTerminator(flags.sqlTerminator);
                        }

                        tool.setSchemaGroup(getSchemaGroup());
                        tool.run();
                        tool.record();
                        tool.clear();
                    }
                }

                // xml output of schema?
                if (_schemaWriter != null) {
                    // serialize the planned schema to the stream
                    SchemaSerializer ser = new XMLSchemaSerializer(_conf);
                    ser.addAll(getSchemaGroup());
                    ser.serialize(_schemaWriter, MetaDataSerializer.PRETTY);
                    _schemaWriter.flush();
                }
            }
            if (!_flush)
                return;

            QueryMetaData[] queries = repos.getQueryMetaDatas();
            SequenceMetaData[] seqs = repos.getSequenceMetaDatas();
            Map<File, String> output = null;

            // if we're outputting to stream, set all metas to same file so
            // they get placed in single string
            if (_mappingWriter != null) {
                output = new HashMap<File, String>();
                File tmp = new File("openjpatmp");
                for (int i = 0; i < mappings.length; i++) {
                    mappings[i].setSource(tmp, SourceTracker.SRC_OTHER, "openjpatmp");
                }
                for (int i = 0; i < queries.length; i++) {
                    queries[i].setSource(tmp, queries[i].getSourceScope(), SourceTracker.SRC_OTHER, "openjpatmp");
                }
                for (int i = 0; i < seqs.length; i++)
                    seqs[i].setSource(tmp, seqs[i].getSourceScope(),
                        SourceTracker.SRC_OTHER);
            }

            // store
            if (!io.store(mappings, queries, seqs, _mode, output))
                throw new MetaDataException(_loc.get("bad-store"));

            // write to stream
            if (_mappingWriter != null) {
                PrintWriter out = new PrintWriter(_mappingWriter);
                for (Iterator<String> itr = output.values().iterator();
                    itr.hasNext();)
                    out.println(itr.next());
                out.flush();
            }
        }
        catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new GeneralException(e);
        } finally {
            clear();
        }
    }

    /**
     * Drops schema components that appear to be unused from the local
     * copy of the schema group.
     */
    private void dropUnusedSchemaComponents(ClassMapping[] mappings) {
        FieldMapping[] fields;
        for (int i = 0; i < mappings.length; i++) {
            mappings[i].refSchemaComponents();
            mappings[i].getDiscriminator().refSchemaComponents();
            mappings[i].getVersion().refSchemaComponents();
            fields = mappings[i].getDefinedFieldMappings();
            for (int j = 0; j < fields.length; j++)
                fields[j].refSchemaComponents();
        }

        // also allow the dbdictionary to ref any schema components that
        // it adds apart from mappings
        SchemaGroup group = getSchemaGroup();
        Schema[] schemas = group.getSchemas();
        Table[] tables;
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++)
                _dict.refSchemaComponents(tables[j]);
        }

        group.removeUnusedComponents();
    }

    /**
     * Add tables used by sequences to the given schema.
     */
    private void addSequenceComponents(ClassMapping[] mappings) {
        SchemaGroup group = getSchemaGroup();
        for (int i = 0; i < mappings.length; i++)
            addSequenceComponents(mappings[i], group);
    }

    /**
     * Add tables used by sequences to the given schema.
     */
    private void addSequenceComponents(ClassMapping mapping,
        SchemaGroup group) {
        SequenceMetaData smd = mapping.getIdentitySequenceMetaData();
        Seq seq = null;
        if (smd != null)
            seq = smd.getInstance(null);
        else if (mapping.getIdentityStrategy() == ValueStrategies.NATIVE
            || (mapping.getIdentityStrategy() == ValueStrategies.NONE
            && mapping.getIdentityType() == ClassMapping.ID_DATASTORE))
            seq = _conf.getSequenceInstance();

        if (seq instanceof JDBCSeq)
            ((JDBCSeq) seq).addSchema(mapping, group);

        FieldMapping[] fmds;
        if (mapping.getEmbeddingMetaData() == null)
            fmds = mapping.getDefinedFieldMappings();
        else
            fmds = mapping.getFieldMappings();
        for (int i = 0; i < fmds.length; i++) {
            smd = fmds[i].getValueSequenceMetaData();
            if (smd != null) {
                seq = smd.getInstance(null);
                if (seq instanceof JDBCSeq)
                    ((JDBCSeq) seq).addSchema(mapping, group);
            } else if (fmds[i].getEmbeddedMapping() != null)
                addSequenceComponents(fmds[i].getEmbeddedMapping(), group);
        }
    }

    ///////////
    // Actions
    ///////////

    /**
     * Run the configured action on the given instance.
     */
    public void run(Class<?> cls) {
        if (ACTION_ADD.equals(_action)) {
            if (_meta)
                addMeta(cls);
            else
                add(cls);
        } else if (ACTION_REFRESH.equals(_action))
            refresh(cls);
        else if (ACTION_BUILD_SCHEMA.equals(_action))
            buildSchema(cls);
        else if (ACTION_DROP.equals(_action))
            drop(cls);
        else if (ACTION_VALIDATE.equals(_action))
            validate(cls);
    }

    /**
     * Add the mapping for the given instance.
     */
    private void add(Class<?> cls) {
        if (cls == null)
            return;

        MappingRepository repos = getRepository();
        repos.setStrategyInstaller(new MappingStrategyInstaller(repos));
        if (getMapping(repos, cls, true) != null) {
            _flush = true;
            _flushSchema = true;
        }
    }

    /**
     * Return the mapping for the given type, or null if the type is
     * persistence-aware.
     */
    private static ClassMapping getMapping(MappingRepository repos, Class<?> cls,
        boolean validate) {
        // this will parse all possible metadata rsrcs looking for cls, so
        // will detect if p-aware
        ClassMapping mapping = repos.getMapping(cls, null, false);
        if (mapping != null)
            return mapping;
        if (!validate || cls.isInterface() 
            || repos.getPersistenceAware(cls) != null)
            return null;
        throw new MetaDataException(_loc.get("no-meta", cls, cls.getClassLoader()));
    }

    /**
     * Create a metadata for the given instance.
     */
    private void addMeta(Class<?> cls) {
        if (cls == null)
            return;

        _flush = true;
        MappingRepository repos = getRepository();
        repos.setResolve(MODE_MAPPING, false);
        MetaDataFactory factory = repos.getMetaDataFactory();
        factory.getDefaults().setIgnoreNonPersistent(false);
        factory.setStoreMode(MetaDataFactory.STORE_VERBOSE);

        ClassMetaData meta = repos.addMetaData(cls);
        FieldMetaData[] fmds = meta.getDeclaredFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].getDeclaredTypeCode() == JavaTypes.OBJECT
                && fmds[i].getDeclaredType() != Object.class)
                fmds[i].setDeclaredTypeCode(JavaTypes.PC);
        }
        meta.setSource(_file, meta.getSourceType(), _file == null ? "": _file.getPath() );
        meta.setResolve(MODE_META, true);
    }

    /**
     * Refresh or add the mapping for the given instance.
     */
    private void refresh(Class<?> cls) {
        if (cls == null)
            return;

        MappingRepository repos = getRepository();
        repos.setStrategyInstaller(new RefreshStrategyInstaller(repos));
        if (getMapping(repos, cls, true) != null) {
            _flush = true;
            _flushSchema = true;
        }
    }

    /**
     * Validate the mappings for the given class and its fields.
     */
    private void validate(Class<?> cls) {
        if (cls == null)
            return;

        MappingRepository repos = getRepository();
        repos.setStrategyInstaller(new RuntimeStrategyInstaller(repos));
        if (getMapping(repos, cls, true) != null)
            _flushSchema = !contains(_schemaActions,SCHEMA_ACTION_NONE)
                && !contains(_schemaActions,SchemaTool.ACTION_ADD);
    }

    /**
     * Create the schema using the mapping for the given instance.
     */
    private void buildSchema(Class<?> cls) {
        if (cls == null)
            return;

        MappingRepository repos = getRepository();
        repos.setStrategyInstaller(new RuntimeStrategyInstaller(repos));
        if (getMapping(repos, cls, true) == null)
            return;

        // set any logical pks to non-logical so they get flushed
        _flushSchema = true;
        Schema[] schemas = _schema.getSchemas();
        Table[] tables;
        Column[] cols;
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++) {
                if (tables[j].getPrimaryKey() == null)
                    continue;

                tables[j].getPrimaryKey().setLogical(false);
                cols = tables[j].getPrimaryKey().getColumns();
                for (int k = 0; k < cols.length; k++)
                    cols[k].setNotNull(true);
            }
        }
    }

    /**
     * Drop mapping for given class.
     */
    private void drop(Class<?> cls) {
        if (cls == null)
            return;

        if (_dropCls == null)
            _dropCls = new HashSet<Class<?>>();
        _dropCls.add(cls);
        if (!contains(_schemaActions,SchemaTool.ACTION_DROP))
            return;

        MappingRepository repos = getRepository();
        repos.setStrategyInstaller(new RuntimeStrategyInstaller(repos));
        ClassMapping mapping = null;
        try {
            mapping = repos.getMapping(cls, null, false);
        } catch (Exception e) {
        }

        if (mapping != null) {
            _flushSchema = true;
            if (_dropMap == null)
                _dropMap = new HashSet<ClassMapping>();
            _dropMap.add(mapping);
        } else
            _log.warn(_loc.get("no-drop-meta", cls));
    }

    ////////
    // Main
    ////////

    /**
     * Usage: java org.apache.openjpa.jdbc.meta.MappingTool [option]* 
     * [-action/-a &lt;refresh | add | buildSchema | drop | validate | import 
     * | export&gt;] &lt;class name | .java file | .class file | .jdo file&gt;*
     * Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The
     * path or resource name of a OpenJPA properties file containing
     * information as outlined in {@link OpenJPAConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link JDBCConfiguration} can be set by
     * using their	names and supplying a value. For example:
     * <code>-licenseKey adslfja83r3lkadf</code></li>
     * <li><i>-file/-f &lt;stdout | output file or resource&gt;</i>: Use
     * this option to write the planned mappings to an XML document rather
     * than store them in the repository. This option also specifies the
     * metadata file to write to if using the <code>add</code> action with
     * the <code>-meta true</code> flag, or the file to dump to if using
     * the <code>export</code> action.</li>
     * <li><i>-schemaAction/-sa &lt;schema action | none&gt;</i>: The
     * {@link SchemaTool} defines the actions possible. The actions will
     * apply to all schema components used by the mappings involved.
     * Unless you are running the mapping tool on all of your persistent
     * types at once, be careful running schema actions that can drop data.
     * It is possible to accidentally drop schema components that are
     * used by classes you aren't currently running the tool over. The
     * action defaults to <code>add</code>.</li>
     * <li><i>-schemaFile/-sf &lt;stdout | output file or resource&gt;</i>: Use
     * this option to write the planned schema to an XML document rather
     * than modify the data store.</li>
     * <li><i>-sqlFile/-sql &lt;stdout | output file or resource&gt;</i>: Use
     * this option to write the planned schema changes as a SQL
     * script rather than modifying the data store.</li>
     * <li><i>-sqlEncode/-se &lt;encoding&gt;</i>: Use
     * this option with the <code>-sqlFile</code> flag to write the SQL script
     * in a different Java character set encoding than the default JVM locale,
     * such as <code>UTF-8</code>.</li>
     * <li><i>-dropTables/-dt &lt;true/t | false/f&gt;</i>: Corresponds to the
     * same-named option in the {@link SchemaTool}.</li>
     * <li><i>-dropSequences/-dsq &lt;true/t | false/f&gt;</i>: Corresponds
     * to the same-named option in the {@link SchemaTool}.</li>
     * <li><i>-openjpaTables/-kt &lt;true/t | false/f&gt;</i>: Corresponds to
     * the same-named option in the {@link SchemaTool}.</li>
     * <li><i>-ignoreErrors/-i &lt;true/t | false/f&gt;</i>: Corresponds to the
     * same-named option in the {@link SchemaTool}.</li>
     * <li><i>-readSchema/-rs &lt;true/t | false/f&gt;</i>: Set this to true
     * to read the entire existing schema (even when false the parts of
     * the schema used by classes the tool is run on will still be read).
     * Turning on schema reading can ensure that no naming conflicts will
     * occur, but it can take a long time.</li>
     * <li><i>-primaryKeys/-pk &lt;true/t | false/f&gt;</i>: Whether primary
     * keys on existing tables are manipulated. Defaults to false.</li>
     * <li><i>-foreignKeys/-fk &lt;true/t | false/f&gt;</i>: Whether foreign
     * keys on existing tables are manipulated. Defaults to false.</li>
     * <li><i>-indexes/-ix &lt;true/t | false/f&gt;</i>: Whether indexes on
     * existing tables are manipulated. Defaults to false.</li>
     * <li><i>-sequences/-sq &lt;true/t | false/f&gt;</i>: Whether sequences
     * are manipulated. Defaults to true.</li>
     * <li><i>-schemas/-s &lt;schema and table names&gt;</i>: A list of schemas
     * and/or tables to read. Corresponds to the
     * same-named option in the {@link SchemaGenerator}. This option
     * is ignored if <code>readSchema</code> is false.</li>
     * <li><i>-meta/-m &lt;true/t | false/f&gt;</i>: Whether the given action
     * applies to metadata as well as mappings.</li>
     * </ul>
     *  The various actions are as follows.
     * <ul>
     * <li><i>refresh</i>: Bring the mapping information up-to-date
     * with the class definitions. OpenJPA will attempt to use any provided
     * mapping information, and fill in missing information. If the
     * provided information conflicts with the class definition, the
     * conflicting information will be discarded and the class/field will
     * be re-mapped to new columns/tables. This is the default action.</li>
     * <li><i>add</i>: If used with the <code>-meta</code> option, adds new
     * default metadata for the given class(es). Otherwise, brings the
     * mapping information up-to-date with the class
     * definitions. OpenJPA will attempt to use any provided mapping
     * information, and fill in missing information. OpenJPA will fail if
     * the provided information conflicts with the class definition.</li>
     * <li><i>buildSchema</i>: Create the schema matching the existing
     * mappings for the given class(es). Any invalid mapping information
     * will cause an exception.</li>
     * <li><i>drop</i>: Delete mappings for the given classes. If used with
     * the <code>-meta</code> option, also deletes metadata.</li>
     * <li><i>validate</i>: Validate the given mappings. The mapping
     * repository and schema will not be affected.</li>
     * <li><i>import</i>: Import mappings from an XML document and store
     * them as the current system mappings.</li>
     * <li><i>export</i>: Dump the current mappings for the given classes to
     * an XML document specified by the <code>file</code> option.</li>
     * If used with the <code>-meta</code> option, the metadata will be
     * included in the export.
     * </ul>
     *  Each class supplied as an argument must have valid metadata. If
     * no class arguments are given, the tool runs on all metadata files in
     * the CLASSPATH.
     *  Examples:
     * <ul>
     * <li>Refresh the mappings for given package, without dropping any
     * schema components:<br />
     * <code>java org.apache.openjpa.jdbc.meta.MappingTool 
     *      mypackage.jdo</code></li>
     * <li>Refresh the mappings for all persistent classes in the classpath,
     * dropping any unused columns and even tables:<br />
     * <code>java org.apache.openjpa.jdbc.meta.MappingTool -sa refresh
     * -dt true</code></li>
     * <li>Make sure the mappings you've created by hand match the object
     * model and schema:<br />
     * <code>java org.apache.openjpa.jbdc.meta.MappingTool
     * -a validate Person.java</code></li>
     * <li>Remove the recorded mapping for a given class:<br />
     * <code>java org.apache.openjpa.jbdc.meta.MappingTool
     * -a drop Person.java</code></li>
     * <li>Record the current mappings in an XML file:<br />
     * <code>java org.apache.openjpa.jdbc.meta.MappingTool
     * -f mypackage.orm -a export mypackage.jdo</code></li>
     * </ul>
     */
    public static void main(String[] arguments)
        throws IOException, SQLException {
        Options opts = new Options();
        final String[] args = opts.setFromCmdLine(arguments);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws IOException, SQLException {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                try {
                    return MappingTool.run(conf, args, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.err.println(_loc.get("tool-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if invalid options are given.
     *
     * @see #main
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts)
        throws IOException, SQLException {
        // flags
        Flags flags = new Flags();
        flags.action = opts.removeProperty("action", "a", flags.action);
        flags.schemaAction = opts.removeProperty("schemaAction", "sa",
            flags.schemaAction);
        flags.dropTables = opts.removeBooleanProperty
            ("dropTables", "dt", flags.dropTables);
        flags.openjpaTables = opts.removeBooleanProperty
            ("openjpaTables", "ot", flags.openjpaTables);
        flags.dropSequences = opts.removeBooleanProperty
            ("dropSequences", "dsq", flags.dropSequences);
        flags.readSchema = opts.removeBooleanProperty
            ("readSchema", "rs", flags.readSchema);
        flags.primaryKeys = opts.removeBooleanProperty
            ("primaryKeys", "pk", flags.primaryKeys);
        flags.indexes = opts.removeBooleanProperty("indexes", "ix",
            flags.indexes);
        flags.foreignKeys = opts.removeBooleanProperty("foreignKeys", "fk",
            flags.foreignKeys);
        flags.sequences = opts.removeBooleanProperty("sequences", "sq",
            flags.sequences);
        flags.ignoreErrors = opts.removeBooleanProperty
            ("ignoreErrors", "i", flags.ignoreErrors);
        flags.meta = opts.removeBooleanProperty("meta", "m", flags.meta);
        String fileName = opts.removeProperty("file", "f", null);
        String schemaFileName = opts.removeProperty("schemaFile", "sf", null);
        String sqlFileName = opts.removeProperty("sqlFile", "sql", null);
        String sqlEncoding = opts.removeProperty("sqlEncode", "se", null);
        String sqlTerminator = opts.removeProperty("sqlTerminator", "st", ";");
        String schemas = opts.removeProperty("s");
        if (schemas != null)
            opts.setProperty("schemas", schemas);

        Configurations.populateConfiguration(conf, opts);
        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(MappingTool.class, null);
        if (flags.meta && ACTION_ADD.equals(flags.action))
            flags.metaDataFile = Files.getFile(fileName, loader);
        else
            flags.mappingWriter = Files.getWriter(fileName, loader);
        flags.schemaWriter = Files.getWriter(schemaFileName, loader);
        if (sqlEncoding != null)
            flags.sqlWriter = Files.getWriter(sqlFileName, loader, sqlEncoding);
        else
            flags.sqlWriter = Files.getWriter(sqlFileName, loader);
        flags.sqlTerminator = sqlTerminator;
        return run(conf, args, flags, loader);
    }

    /**
     * Run the tool. Return false if an invalid option was given.
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Flags flags, ClassLoader loader)
        throws IOException, SQLException {
        // default action based on whether the mapping defaults fills in
        // missing info
        if (flags.action == null) {
            if (conf.getMappingDefaultsInstance().defaultMissingInfo())
                flags.action = ACTION_BUILD_SCHEMA;
            else
                flags.action = ACTION_REFRESH;
        }

        // collect the classes to act on
        Log log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
        Collection<Class<?>> classes = null;
        if (args.length == 0) {
            if (ACTION_IMPORT.equals(flags.action))
                return false;
            log.info(_loc.get("running-all-classes"));
            classes = conf.getMappingRepositoryInstance().
                loadPersistentTypes(true, loader);
        } else {
            classes = new HashSet<Class<?>>();
            ClassArgParser classParser = conf.getMetaDataRepositoryInstance().
                getMetaDataFactory().newClassArgParser();
            classParser.setClassLoader(loader);
            Class<?>[] parsed;
            for (int i = 0; i < args.length; i++) {
                parsed = classParser.parseTypes(args[i]);
                classes.addAll(Arrays.asList(parsed));
            }
        }

        Class<?>[] act = (Class[]) classes.toArray(new Class[classes.size()]);
        if (ACTION_EXPORT.equals(flags.action)) {
            // run exports until the first export succeeds
            ImportExport[] instances = newImportExports();
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].exportMappings(conf, act, flags.meta, log,
                    flags.mappingWriter))
                    return true;
            }
            return false;
        }
        if (ACTION_IMPORT.equals(flags.action)) {
            // run exports until the first export succeeds
            ImportExport[] instances = newImportExports();
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].importMappings(conf, act, args, flags.meta,
                    log, loader))
                    return true;
            }
            return false;
        }

        MappingTool tool;
        try {
            tool = new MappingTool(conf, flags.action, flags.meta);
        } catch (IllegalArgumentException iae) {
            return false;
        }

        // setup the tool
        tool.setIgnoreErrors(flags.ignoreErrors);
        tool.setMetaDataFile(flags.metaDataFile);
        tool.setMappingWriter(flags.mappingWriter);
        tool.setSchemaAction(flags.schemaAction);
        tool.setSchemaWriter(flags.schemaWriter);
        tool.setReadSchema(flags.readSchema
            && !ACTION_VALIDATE.equals(flags.action));
        tool.setPrimaryKeys(flags.primaryKeys);
        tool.setForeignKeys(flags.foreignKeys);
        tool.setIndexes(flags.indexes);
        tool.setSequences(flags.sequences || flags.dropSequences);

        // and run the action
        for (int i = 0; i < act.length; i++) {
            log.info(_loc.get("tool-running", act[i], flags.action));
            if (i == 0 && flags.readSchema)
                log.info(_loc.get("tool-time"));
            tool.run(act[i]);
        }
        log.info(_loc.get("tool-record"));
        tool.record(flags);
        return true;
    }

    /**
     * Create an {@link ImportExport} instance.
     */
    private static ImportExport[] newImportExports() {
        try {
            Class<?>[] types = Services.getImplementorClasses(ImportExport.class);
            ImportExport[] instances = new ImportExport[types.length];
            for (int i = 0; i < types.length; i++)
                instances[i] = (ImportExport) AccessController.doPrivileged(
                    J2DoPrivHelper.newInstanceAction(types[i]));
            return instances;
        } catch (Throwable t) {
            if (t instanceof PrivilegedActionException)
                t = ((PrivilegedActionException) t).getException();
            throw new InternalException(_loc.get("importexport-instantiate"),t);
        }
    }
    
    private static boolean contains(String list, String key) {
    	return (list == null) ? false : list.indexOf(key) != -1;
    }

    /**
     * Run flags.
     */
    public static class Flags {

        public String action = null;
        public boolean meta = false;
        public String schemaAction = SchemaTool.ACTION_ADD;
        public File metaDataFile = null;
        public Writer mappingWriter = null;
        public Writer schemaWriter = null;
        public Writer sqlWriter = null;
        public boolean ignoreErrors = false;
        public boolean readSchema = false;
        public boolean dropTables = false;
        public boolean openjpaTables = false;
        public boolean dropSequences = false;
        public boolean sequences = true;
        public boolean primaryKeys = false;
        public boolean foreignKeys = false;
        public boolean indexes = false;
        public String  sqlTerminator = ";";
    }

    /**
     * Helper used to import and export mapping data.
     */
    public static interface ImportExport {

        /**
         * Import mappings for the given classes based on the given arguments.
         */
        public boolean importMappings(JDBCConfiguration conf, Class<?>[] act,
            String[] args, boolean meta, Log log, ClassLoader loader)
            throws IOException;

        /**
         * Export mappings for the given classes based on the given arguments.
         */
        public boolean exportMappings(JDBCConfiguration conf, Class<?>[] act,
            boolean meta, Log log, Writer writer)
            throws IOException;
    }
}
