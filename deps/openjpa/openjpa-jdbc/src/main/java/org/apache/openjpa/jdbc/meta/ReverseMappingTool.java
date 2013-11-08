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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.ApplicationIdTool;
import org.apache.openjpa.enhance.CodeGenerator;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.HandlerFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.MaxEmbeddedBlobFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.MaxEmbeddedClobFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.PrimitiveFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.
        RelationCollectionInverseKeyFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.RelationCollectionTableFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.RelationFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.StateComparisonVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.StringFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.SubclassJoinDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.SuperclassDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.SuperclassVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.PrimaryKey;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGenerator;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.SchemaParser;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.schema.XMLSchemaParser;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.CodeFormat;
import org.apache.openjpa.lib.util.Files;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Options;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.NoneMetaDataFactory;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import serp.bytecode.BCClass;
import serp.bytecode.BCClassLoader;
import serp.bytecode.Project;
import serp.util.Strings;

/**
 * Reverse-maps a schema into class mappings and the associated java
 * code. Generates a Java code files for persistent classes and associated
 * identity classes and metadata.
 *
 * @author Abe White
 */
public class ReverseMappingTool
    implements MetaDataModes, Cloneable {

    /**
     * Unmapped table.
     */
    public static final int TABLE_NONE = 0;

    /**
     * Primary table for a new base class.
     */
    public static final int TABLE_BASE = 1;

    /**
     * Secondary table of an existing class. There is exactly one row in
     * this table for each row in the primary table.
     */
    public static final int TABLE_SECONDARY = 2;

    /**
     * Secondary table of an existing class. There is zero or one row in
     * this table for each row in the primary table.
     */
    public static final int TABLE_SECONDARY_OUTER = 3;

    /**
     * Association table.
     */
    public static final int TABLE_ASSOCIATION = 4;

    /**
     * Subclass table.
     */
    public static final int TABLE_SUBCLASS = 5;

    public static final String LEVEL_NONE = "none";
    public static final String LEVEL_PACKAGE = "package";
    public static final String LEVEL_CLASS = "class";

    /**
     * Access type for generated source, defaults to field-based access.
     */
    public static final String ACCESS_TYPE_FIELD = "field";
    public static final String ACCESS_TYPE_PROPERTY = "property";

    private static Localizer _loc = Localizer.forPackage
        (ReverseMappingTool.class);

    // map java keywords to related words to use in their place
    private static final Map _javaKeywords = new HashMap();

    static {
        InputStream in = ReverseMappingTool.class.getResourceAsStream
            ("java-keywords.rsrc");
        try {
            String[] keywords = Strings.split(new BufferedReader
                (new InputStreamReader(in)).readLine(), ",", 0);

            for (int i = 0; i < keywords.length; i += 2)
                _javaKeywords.put(keywords[i], keywords[i + 1]);
        } catch (IOException ioe) {
            throw new InternalException(ioe);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    private final JDBCConfiguration _conf;
    private final Log _log;
    private final Map _tables = new HashMap();
    private final Project _project = new Project();
    private final BCClassLoader _loader = AccessController
        .doPrivileged(J2DoPrivHelper.newBCClassLoaderAction(_project));
    private StrategyInstaller _strat = null;
    private String _package = null;
    private File _dir = null;
    private MappingRepository _repos = null;
    private SchemaGroup _schema = null;
    private boolean _nullAsObj = false;
    private boolean _blobAsObj = false;
    private boolean _useGenericColl = false;
    private Properties _typeMap = null;
    private boolean _useFK = false;
    private boolean _useSchema = false;
    private boolean _pkOnJoin = false;
    private boolean _datastore = false;
    private boolean _builtin = true;
    private boolean _inner = false;
    private String _idSuffix = "Id";
    private boolean _inverse = true;
    private boolean _detachable = false;
    private boolean _genAnnotations = false;
    private String _accessType = ACCESS_TYPE_FIELD;
    private CodeFormat _format = null;
    private ReverseCustomizer _custom = null;
    private String _discStrat = null;
    private String _versStrat = null;
    private boolean _useSchemaElement = true;

    // we have to track field names that were created but then abandoned by
    // the customizer so that we don't attempt to use them again; doing so can
    // mess up certain customizers (bug 881)
    private Set _abandonedFieldNames = null;

    // generated annotations, key = metadata, val = list of annotations
    private Map _annos = null;

    /**
     * Constructor. Supply configuration.
     */
    public ReverseMappingTool(JDBCConfiguration conf) {
        _conf = conf;
        _log = conf.getLog(JDBCConfiguration.LOG_METADATA);
    }

    /**
     * Return the internal strategy installer.
     */
    private StrategyInstaller getStrategyInstaller() {
        if (_strat == null)
            _strat = new ReverseStrategyInstaller(getRepository());
        return _strat;
    }

    /**
     * Return the configuration provided on construction.
     */
    public JDBCConfiguration getConfiguration() {
        return _conf;
    }

    /**
     * Return the log to write to.
     */
    public Log getLog() {
        return _log;
    }

    /**
     * Return the default package for the generated classes, or null if unset.
     */
    public String getPackageName() {
        return _package;
    }

    /**
     * Set the default package for the generated classes; use null to
     * indicate no package.
     */
    public void setPackageName(String packageName) {
        _package = StringUtils.trimToNull(packageName);
    }

    /**
     * The file to output the generated code to, or null for
     * the current directory. If the directory matches the package, it will
     * be used. Otherwise, the package structure will be created under
     * this directory.
     */
    public File getDirectory() {
        return _dir;
    }

    /**
     * The file to output the generated code to, or null for
     * the current directory. If the directory matches the package, it will
     * be used. Otherwise, the package structure will be created under
     * this directory.
     */
    public void setDirectory(File dir) {
        _dir = dir;
    }

    /**
     * Return true if the schema name will be included in the generated
     * class name for each table. Defaults to false.
     */
    public boolean getUseSchemaName() {
        return _useSchema;
    }

    /**
     * Set whether the schema name will be included in the generated
     * class name for each table. Defaults to false.
     */
    public void setUseSchemaName(boolean useSchema) {
        _useSchema = useSchema;
    }

    /**
     * Return whether the foreign key name will be used to generate
     * relation names. Defaults to false.
     */
    public boolean getUseForeignKeyName() {
        return _useFK;
    }

    /**
     * Set whether the foreign key name will be used to generate
     * relation names. Defaults to false.
     */
    public void setUseForeignKeyName(boolean useFK) {
        _useFK = useFK;
    }

    /**
     * Return whether even nullable columns will be mapped to wrappers
     * rather than primitives. Defaults to false.
     */
    public boolean getNullableAsObject() {
        return _nullAsObj;
    }

    /**
     * Set whether even nullable columns will be mapped to wrappers
     * rather than primitives. Defaults to false.
     */
    public void setNullableAsObject(boolean nullAsObj) {
        _nullAsObj = nullAsObj;
    }

    /**
     * Whether to reverse-map blob columns as containing serialized Java
     * objects, rather than the default of using a byte[] field.
     */
    public boolean getBlobAsObject() {
        return _blobAsObj;
    }

    /**
     * Whether to reverse-map blob columns as containing serialized Java
     * objects, rather than the default of using a byte[] field.
     */
    public void setBlobAsObject(boolean blobAsObj) {
        _blobAsObj = blobAsObj;
    }

    /**
     * Whether to use generic collections on one-to-many and many-to-many
     * relations instead of untyped collections.
     */
    public boolean getUseGenericCollections() {
        return _useGenericColl;
    }

    /**
     * Whether to use generic collections on one-to-many and many-to-many
     * relations instead of untyped collections.
     */
    public void setUseGenericCollections(boolean useGenericCollections) {
        _useGenericColl = useGenericCollections; 
    }

    /**
     * Map of JDBC-name to Java-type-name entries that allows customization
     * of reverse mapping columns to field types.
     */
    public Properties getTypeMap() {
        return _typeMap;
    }

    /**
     * Map of JDBC-name to Java-type-name entries that allows customization
     * of reverse mapping columns to field types.
     */
    public void setTypeMap(Properties typeMap) {
        _typeMap = typeMap;
    }

    /**
     * Return true if join tables are allowed to have primary keys, false
     * if all primary key tables will be mapped as persistent classes.
     * Defaults to false.
     */
    public boolean getPrimaryKeyOnJoin() {
        return _pkOnJoin;
    }

    /**
     * Set to true if join tables are allowed to have primary keys, false
     * if all primary key tables will be mapped as persistent classes.
     * Defaults to false.
     */
    public void setPrimaryKeyOnJoin(boolean pkOnJoin) {
        _pkOnJoin = pkOnJoin;
    }

    /**
     * Whether to use datastore identity when possible. Defaults to false.
     */
    public boolean getUseDataStoreIdentity() {
        return _datastore;
    }

    /**
     * Whether to use datastore identity when possible. Defaults to false.
     */
    public void setUseDataStoreIdentity(boolean datastore) {
        _datastore = datastore;
    }

    /**
     * Whether to use built in identity classes when possible. Defaults to true.
     */
    public boolean getUseBuiltinIdentityClass() {
        return _builtin;
    }

    /**
     * Whether to use built in identity classes when possible. Defaults to true.
     */
    public void setUseBuiltinIdentityClass(boolean builtin) {
        _builtin = builtin;
    }

    /**
     * Whether or not to generate inner classes when creating application
     * identity classes.
     */
    public boolean getInnerIdentityClasses() {
        return _inner;
    }

    /**
     * Whether or not to generate inner classes when creating application
     * identity classes.
     */
    public void setInnerIdentityClasses(boolean inner) {
        _inner = inner;
    }

    /**
     * Suffix used to create application identity class name from a class name,
     * or in the case of inner identity classes, the inner class name.
     */
    public String getIdentityClassSuffix() {
        return _idSuffix;
    }

    /**
     * Suffix used to create application identity class name from a class name,
     * or in the case of inner identity classes, the inner class name.
     */
    public void setIdentityClassSuffix(String suffix) {
        _idSuffix = suffix;
    }

    /**
     * Whether to generate inverse 1-many/1-1 relations for all many-1/1-1
     * relations. Defaults to true.
     */
    public boolean getInverseRelations() {
        return _inverse;
    }

    /**
     * Whether to generate inverse 1-many/1-1 relations for all many-1/1-1
     * relations. Defaults to true.
     */
    public void setInverseRelations(boolean inverse) {
        _inverse = inverse;
    }

    /**
     * Whether to make generated classes detachable. Defaults to false.
     */
    public boolean getDetachable() {
        return _detachable;
    }

    /**
     * Whether to make generated classes detachable. Defaults to false.
     */
    public void setDetachable(boolean detachable) {
        _detachable = detachable;
    }

    /**
     * Default discriminator strategy for base class mappings.
     */
    public String getDiscriminatorStrategy() {
        return _discStrat;
    }

    /**
     * Default discriminator strategy for base class mappings.
     */
    public void setDiscriminatorStrategy(String discStrat) {
        _discStrat = discStrat;
    }

    /**
     * Default version strategy for base class mappings.
     */
    public String getVersionStrategy() {
        return _versStrat;
    }

    /**
     * Default version strategy for base class mappings.
     */
    public void setVersionStrategy(String versionStrat) {
        _versStrat = versionStrat;
    }

    /**
     * Whether to generate annotations along with generated code. Defaults
     * to false.
     */
    public boolean getGenerateAnnotations() {
        return _genAnnotations;
    }

    /**
     * Whether to generate annotations along with generated code. Defaults
     * to false.
     */
    public void setGenerateAnnotations(boolean genAnnotations) {
        _genAnnotations = genAnnotations;
    }

    /**
     * Whether to use field or property-based access on generated code.
     * Defaults to field-based access.
     */
    public String getAccessType() {
        return _accessType;
    }

    /**
     * Whether to use field or property-based access on generated code.
     * Defaults to field-based access.
     */
    public void setAccessType(String accessType) {
        this._accessType = ACCESS_TYPE_PROPERTY.equalsIgnoreCase(accessType) ?
            ACCESS_TYPE_PROPERTY : ACCESS_TYPE_FIELD;
    }

    /**
     * The code formatter for the generated Java code.
     */
    public CodeFormat getCodeFormat() {
        return _format;
    }

    /**
     * Set the code formatter for the generated Java code.
     */
    public void setCodeFormat(CodeFormat format) {
        _format = format;
    }

    /**
     * Return the customizer in use, or null if none.
     */
    public ReverseCustomizer getCustomizer() {
        return _custom;
    }

    /**
     * Set the customizer. The configuration on the customizer, if any,
     * should already be set.
     */
    public void setCustomizer(ReverseCustomizer customizer) {
        if (customizer != null)
            customizer.setTool(this);
        _custom = customizer;
    }
    
    /**
     * Returns whether or not the schema name will be included in the @Table
     * annotation within the generated class for each table, as well as the 
     * corresponding XML mapping files. The initialized value is true (in order
     * to preserve backwards compatibility).
     */
    public boolean getUseSchemaElement() {
    	return _useSchemaElement;
    }
    
    /**
     * Sets whether or not the schema name will be included in the @Table
     * annotation within the generated class for each table, as well as the
     * corresponding XML mapping files.
     */
    public void setUseSchemaElement(boolean useSchemaElement) {
    	_useSchemaElement = useSchemaElement;
    }

    /**
     * Return the mapping repository used to hold generated mappings. You
     * can also use the repository to seed the schema group to generate
     * classes from.
     */
    public MappingRepository getRepository() {
        if (_repos == null) {
            // create empty repository
            _repos = _conf.newMappingRepositoryInstance();
            _repos.setMetaDataFactory(NoneMetaDataFactory.getInstance());
            _repos.setMappingDefaults(NoneMappingDefaults.getInstance());
            _repos.setResolve(MODE_NONE);
            _repos.setValidate(_repos.VALIDATE_NONE);
        }
        return _repos;
    }

    /**
     * Set the repository to use.
     */
    public void setRepository(MappingRepository repos) {
        _repos = repos;
    }

    /**
     * Return the schema group to reverse map. If none has been set, the
     * schema will be generated from the database.
     */
    public SchemaGroup getSchemaGroup() {
        if (_schema == null) {
            SchemaGenerator gen = new SchemaGenerator(_conf);
            try {
                gen.generateSchemas();
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se,
                    _conf.getDBDictionaryInstance());
            }
            _schema = gen.getSchemaGroup();
        }
        return _schema;
    }

    /**
     * Set the schema to reverse map.
     */
    public void setSchemaGroup(SchemaGroup schema) {
        _schema = schema;
    }

    /**
     * Return the generated mappings.
     */
    public ClassMapping[] getMappings() {
        return getRepository().getMappings();
    }

    /**
     * Generate mappings and class code for the current schema group.
     */
    public void run() {
        // map base classes first
        Schema[] schemas = getSchemaGroup().getSchemas();
        Table[] tables;
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++)
                if (isBaseTable(tables[j]))
                    mapBaseClass(tables[j]);
        }

        // map vertical subclasses
        Set subs = null;
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++) {
                if (!_tables.containsKey(tables[j])
                    && getSecondaryType(tables[j], false) == TABLE_SUBCLASS) {
                    if (subs == null)
                        subs = new HashSet();
                    subs.add(tables[j]);
                }
            }
        }
        if (subs != null)
            mapSubclasses(subs);

        // map fields in the primary tables of the classes
        ClassMapping cls;
        for (Iterator itr = _tables.values().iterator(); itr.hasNext();) {
            cls = (ClassMapping) itr.next();
            mapColumns(cls, cls.getTable(), null, false);
        }

        // map association tables, join tables, and secondary tables
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++)
                if (!_tables.containsKey(tables[j]))
                    mapTable(tables[j], getSecondaryType(tables[j], false));
        }

        // map discriminators and versions, make sure identity type is correct,
        // set simple field column java types, and ref schema components so
        // we can tell what is unmapped
        FieldMapping[] fields;
        for (Iterator itr = _tables.values().iterator(); itr.hasNext();) {
            cls = (ClassMapping) itr.next();
            cls.refSchemaComponents();
            if (cls.getDiscriminator().getStrategy() == null)
                getStrategyInstaller().installStrategy
                    (cls.getDiscriminator());
            cls.getDiscriminator().refSchemaComponents();
            if (cls.getVersion().getStrategy() == null)
                getStrategyInstaller().installStrategy(cls.getVersion());
            cls.getVersion().refSchemaComponents();

            // double-check identity type; if it was set for builtin identity
            // it might have to switch to std application identity if pk field
            // not compatible
            if (cls.getPCSuperclass() == null
                && cls.getIdentityType() == ClassMapping.ID_APPLICATION) {
                if (cls.getPrimaryKeyFields().length == 0)
                    throw new MetaDataException(_loc.get("no-pk-fields", cls));
                if (cls.getObjectIdType() == null
                    || (cls.isOpenJPAIdentity() && !isBuiltinIdentity(cls)))
                    setObjectIdType(cls);
            } else if (cls.getIdentityType() == ClassMapping.ID_DATASTORE)
                cls.getPrimaryKeyColumns()[0].setJavaType(JavaTypes.LONG);

            // set java types for simple fields;
            fields = cls.getDeclaredFieldMappings();
            for (int i = 0; i < fields.length; i++) {
                fields[i].refSchemaComponents();
                setColumnJavaType(fields[i]);
                setColumnJavaType(fields[i].getElementMapping());
            }
        }

        // set the java types of foreign key columns; we couldn't do this
        // earlier because we rely on the linked-to columns to do it
        for (Iterator itr = _tables.values().iterator(); itr.hasNext();) {
            cls = (ClassMapping) itr.next();
            setForeignKeyJavaType(cls.getJoinForeignKey());

            fields = cls.getDeclaredFieldMappings();
            for (int i = 0; i < fields.length; i++) {
                setForeignKeyJavaType(fields[i].getJoinForeignKey());
                setForeignKeyJavaType(fields[i].getForeignKey());
                setForeignKeyJavaType(fields[i].getElementMapping().
                    getForeignKey());
            }
        }

        // allow customizer to map unmapped tables, and warn about anything
        // that ends up unmapped
        Column[] cols;
        Collection unmappedCols = new ArrayList(5);
        for (int i = 0; i < schemas.length; i++) {
            tables = schemas[i].getTables();
            for (int j = 0; j < tables.length; j++) {
                unmappedCols.clear();
                cols = tables[j].getColumns();
                for (int k = 0; k < cols.length; k++)
                    if (cols[k].getRefCount() == 0)
                        unmappedCols.add(cols[k]);

                if (unmappedCols.size() == cols.length) {
                    if (_custom == null || !_custom.unmappedTable(tables[j]))
                        _log.info(_loc.get("unmap-table", tables[j]));
                } else if (unmappedCols.size() > 0)
                    _log.info(_loc.get("unmap-cols", tables[j], unmappedCols));
            }
        }
        if (_custom != null)
            _custom.close();

        // resolve mappings
        for (Iterator itr = _tables.values().iterator(); itr.hasNext();)
            ((ClassMapping) itr.next()).resolve(MODE_META | MODE_MAPPING);
    }

    /**
     * Map the table of the given type.
     */
    private void mapTable(Table table, int type) {
        switch (type) {
            case TABLE_SECONDARY:
            case TABLE_SECONDARY_OUTER:
                mapSecondaryTable(table, type != TABLE_SECONDARY);
                break;
            case TABLE_ASSOCIATION:
                mapAssociationTable(table);
                break;
        }
    }

    /**
     * Return true if the given class is compatible with builtin identity.
     */
    private static boolean isBuiltinIdentity(ClassMapping cls) {
        FieldMapping[] fields = cls.getPrimaryKeyFieldMappings();
        if (fields.length != 1)
            return false;
        switch (fields[0].getDeclaredTypeCode()) {
            case JavaTypes.BYTE:
            case JavaTypes.CHAR:
            case JavaTypes.INT:
            case JavaTypes.LONG:
            case JavaTypes.SHORT:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.STRING:
            case JavaTypes.OID:
                return true;
        }
        return false;
    }

    /**
     * Set the Java type of the column for the given value.
     */
    private static void setColumnJavaType(ValueMapping vm) {
        Column[] cols = vm.getColumns();
        if (cols.length == 1)
            cols[0].setJavaType(vm.getDeclaredTypeCode());
    }

    /**
     * Set the Java type of the foreign key columns.
     */
    private static void setForeignKeyJavaType(ForeignKey fk) {
        if (fk == null)
            return;
        Column[] cols = fk.getColumns();
        Column[] pks = fk.getPrimaryKeyColumns();
        for (int i = 0; i < cols.length; i++)
            if (cols[i].getJavaType() == JavaTypes.OBJECT)
                cols[i].setJavaType(pks[i].getJavaType());
    }

    /**
     * Uses {@link CodeGenerator}s to write the Java code for the generated
     * mappings to the proper packages.
     *
     * @return a list of {@link File} instances that were written
     */
    public List recordCode()
        throws IOException {
        return recordCode(null);
    }

    /**
     * Write the code for the tool.
     *
     * @param output if null, then perform the write directly
     * to the filesystem; otherwise, populate the
     * specified map with keys as the generated
     * {@link ClassMapping} and values as a
     * {@link String} that contains the generated code
     * @return a list of {@link File} instances that were written
     */
    public List recordCode(Map output)
        throws IOException {
        List written = new LinkedList();

        ClassMapping[] mappings = getMappings();
        ReverseCodeGenerator gen;
        for (int i = 0; i < mappings.length; i++) {
            if (_log.isInfoEnabled())
                _log.info(_loc.get("class-code", mappings[i]));

            ApplicationIdTool aid = newApplicationIdTool(mappings[i]);
            if (getGenerateAnnotations())
                gen = new AnnotatedCodeGenerator(mappings[i], aid);
            else
                gen = new ReverseCodeGenerator(mappings[i], aid);

            gen.generateCode();

            if (output == null) {
                gen.writeCode();
                written.add(gen.getFile());
                if (aid != null && !aid.isInnerClass())
                    aid.record();
            } else {
                StringWriter writer = new StringWriter();
                gen.writeCode(writer);
                output.put(mappings[i].getDescribedType(), writer.toString());

                if (aid != null && !aid.isInnerClass()) {
                    writer = new StringWriter();
                    aid.setWriter(writer);
                    aid.record();
                    output.put(mappings[i].getObjectIdType(),
                        writer.toString());
                }
            }
        }
        return written;
    }

    /**
     * Write the generated metadata to the proper packages.
     *
     * @return the set of metadata {@link File}s that were written
     */
    public Collection recordMetaData(boolean perClass)
        throws IOException {
        return recordMetaData(perClass, null);
    }

    /**
     * Write the code for the tool.
     *
     * @param output if null, then perform the write directly
     * to the filesystem; otherwise, populate the
     * specified map with keys as the generated
     * {@link ClassMapping} and values as a
     * {@link String} that contains the generated code
     * @return the set of metadata {@link File}s that were written
     */
    public Collection recordMetaData(boolean perClass, Map output)
        throws IOException {
        // pretend mappings are all resolved
        ClassMapping[] mappings = getMappings();
        for (int i = 0; i < mappings.length; i++)
        {
            mappings[i].setResolve(MODE_META | MODE_MAPPING, true);
            mappings[i].setUseSchemaElement(getUseSchemaElement());
        }
        // store in user's configured IO
        MetaDataFactory mdf = _conf.newMetaDataFactoryInstance();
        mdf.setRepository(getRepository());
        mdf.setStoreDirectory(_dir);
        if (perClass)
            mdf.setStoreMode(MetaDataFactory.STORE_PER_CLASS);
        mdf.store(mappings, new QueryMetaData[0], new SequenceMetaData[0],
            MODE_META | MODE_MAPPING, output);

        Set files = new TreeSet();
        for (int i = 0; i < mappings.length; i++)
            if (mappings[i].getSourceFile() != null)
                files.add(mappings[i].getSourceFile());
        return files;
    }

    public void buildAnnotations() {
        Map output = new HashMap();
        // pretend mappings are all resolved
        ClassMapping[] mappings = getMappings();
        for (int i = 0; i < mappings.length; i++)
        {
            mappings[i].setResolve(MODE_META | MODE_MAPPING, true);
            mappings[i].setUseSchemaElement(getUseSchemaElement());
        }
        // store in user's configured IO
        MetaDataFactory mdf = _conf.newMetaDataFactoryInstance();
        mdf.setRepository(getRepository());
        mdf.setStoreDirectory(_dir);
        mdf.store(mappings, new QueryMetaData[0], new SequenceMetaData[0],
            MODE_META | MODE_MAPPING | MODE_ANN_MAPPING, output);
        _annos = output;
    }

    /**
     * Returns a list of stringified annotations for specified meta.
     */
    public List getAnnotationsForMeta(Object meta) {
        if (null == _annos)
            return null;
        return (List) _annos.get(meta);
    }

    /**
     * Generate and write the application identity code.
     */
    private ApplicationIdTool newApplicationIdTool(ClassMapping mapping) {
        ApplicationIdTool tool;
        if (mapping.getIdentityType() == ClassMapping.ID_APPLICATION
            && !mapping.isOpenJPAIdentity()
            && mapping.getPCSuperclass() == null) {
            tool = new ApplicationIdTool(_conf, mapping.getDescribedType(),
                mapping);
            tool.setDirectory(_dir);
            tool.setCodeFormat(_format);
            if (!tool.run())
                return null;
            return tool;
        }
        return null;
    }

    //////////////////////////////////
    // Methods for customizers to use
    //////////////////////////////////

    /**
     * Return the class mapping for the given table, or null if none.
     */
    public ClassMapping getClassMapping(Table table) {
        return (ClassMapping) _tables.get(table);
    }

    /**
     * Create a new class to be mapped to a table. The class will start out
     * with a default application identity class set.
     */
    public ClassMapping newClassMapping(Class cls, Table table) {
        ClassMapping mapping = (ClassMapping) getRepository().addMetaData(cls);
        Class sup = mapping.getDescribedType().getSuperclass();
        if (sup == Object.class)
            setObjectIdType(mapping);
        else
            mapping.setPCSuperclass(sup);
        mapping.setTable(table);
        if (_detachable)
            mapping.setDetachable(true);
        _tables.put(table, mapping);
        return mapping;
    }

    /**
     * Set the given class' objectid-class.
     */
    private void setObjectIdType(ClassMapping cls) {
        String name = cls.getDescribedType().getName();
        if (_inner)
            name += "$";
        name += _idSuffix;
        cls.setObjectIdType(generateClass(name, null), false);
    }

    /**
     * Generate a new class with the given name. If a non-null parent class
     * is given, it will be set as the superclass.
     */
    public Class generateClass(String name, Class parent) {
        BCClass bc = _project.loadClass(name, null);
        if (parent != null)
            bc.setSuperclass(parent);
        bc.addDefaultConstructor();

        try {
            return Class.forName(name, false, _loader);
        } catch (ClassNotFoundException cnfe) {
            throw new InternalException(cnfe.toString(), cnfe);
        }
    }

    /**
     * Return whether the given foreign key is unique.
     */
    public boolean isUnique(ForeignKey fk) {
        PrimaryKey pk = fk.getTable().getPrimaryKey();
        if (pk != null && pk.columnsMatch(fk.getColumns()))
            return true;
        Index[] idx = fk.getTable().getIndexes();
        for (int i = 0; i < idx.length; i++)
            if (idx[i].isUnique() && idx[i].columnsMatch(fk.getColumns()))
                return true;
        Unique[] unq = fk.getTable().getUniques();
        for (int i = 0; i < unq.length; i++)
            if (unq[i].columnsMatch(fk.getColumns()))
                return true;
        return false;
    }

    /**
     * If the given table has a single unique foreign key or a foreign
     * key that matches the primary key, return it. Else return null.
     */
    public ForeignKey getUniqueForeignKey(Table table) {
        ForeignKey[] fks = table.getForeignKeys();
        PrimaryKey pk = table.getPrimaryKey();
        ForeignKey unq = null;
        int count = 0;
        for (int i = 0; i < fks.length; i++) {
            if (pk != null && pk.columnsMatch(fks[i].getColumns()))
                return fks[i];
            if (!isUnique(fks[i]))
                continue;

            count++;
            if (unq == null)
                unq = fks[i];
        }
        return (count == 1) ? unq : null;
    }

    /**
     * Add existing unique constraints and indexes to the given field's join.
     */
    public void addJoinConstraints(FieldMapping field) {
        ForeignKey fk = field.getJoinForeignKey();
        if (fk == null)
            return;

        Index idx = findIndex(fk.getColumns());
        if (idx != null)
            field.setJoinIndex(idx);
        Unique unq = findUnique(fk.getColumns());
        if (unq != null)
            field.setJoinUnique(unq);
    }

    /**
     * Add existing unique constraints and indexes to the given value.
     */
    public void addConstraints(ValueMapping vm) {
        Column[] cols = (vm.getForeignKey() != null)
            ? vm.getForeignKey().getColumns() : vm.getColumns();
        Index idx = findIndex(cols);
        if (idx != null)
            vm.setValueIndex(idx);
        Unique unq = findUnique(cols);
        if (unq != null)
            vm.setValueUnique(unq);
    }

    /**
     * Return the index with the given columns.
     */
    private Index findIndex(Column[] cols) {
        if (cols == null || cols.length == 0)
            return null;

        Table table = cols[0].getTable();
        Index[] idxs = table.getIndexes();
        for (int i = 0; i < idxs.length; i++)
            if (idxs[i].columnsMatch(cols))
                return idxs[i];
        return null;
    }

    /**
     * Return the unique constriant with the given columns.
     */
    private Unique findUnique(Column[] cols) {
        if (cols == null || cols.length == 0)
            return null;

        Table table = cols[0].getTable();
        Unique[] unqs = table.getUniques();
        for (int i = 0; i < unqs.length; i++)
            if (unqs[i].columnsMatch(cols))
                return unqs[i];
        return null;
    }

    /////////////
    // Utilities
    /////////////

    /**
     * Return whether the given table is a base class table.
     */
    public boolean isBaseTable(Table table) {
        if (table.getPrimaryKey() == null)
            return false;
        int type = getSecondaryType(table, true);
        if (type != -1)
            return type == TABLE_BASE;
        if (_custom != null)
            return _custom.getTableType(table, TABLE_BASE) == TABLE_BASE;
        return true;
    }

    /**
     * Calculate the type of the secondary given table.
     */
    private int getSecondaryType(Table table, boolean maybeBase) {
        int type;
        ForeignKey[] fks = table.getForeignKeys();
        if (fks.length == 2
            && (table.getPrimaryKey() == null || _pkOnJoin)
            && fks[0].getColumns().length + fks[1].getColumns().length
            == table.getColumns().length
            && (!isUnique(fks[0]) || !isUnique(fks[1])))
            type = TABLE_ASSOCIATION;
        else if (maybeBase && table.getPrimaryKey() != null)
            type = -1;
        else if (getUniqueForeignKey(table) != null)
            type = TABLE_SECONDARY;
        else if (fks.length == 1)
            type = TABLE_NONE;
        else
            type = -1;

        if (_custom != null && type != -1)
            type = _custom.getTableType(table, type);
        return type;
    }

    /**
     * Attempt to create a base class from the given table.
     */
    private void mapBaseClass(Table table) {
        ClassMapping cls = newClassMapping(table, null);
        if (cls == null)
            return;

        // check for datastore identity and builtin identity; for now
        // we assume that any non-datastore single primary key column will use
        // builtin identity; if we discover that the primary key field is
        // not compatible with builtin identity later, then we'll assign
        // an application identity class
        Column[] pks = table.getPrimaryKey().getColumns();
        cls.setPrimaryKeyColumns(pks);
        if (pks.length == 1 && _datastore
            && pks[0].isCompatible(Types.BIGINT, null, 0, 0)) {
            cls.setObjectIdType(null, false);
            cls.setIdentityType(ClassMapping.ID_DATASTORE);
        } else if (pks.length == 1 && _builtin)
            cls.setObjectIdType(null, false);
        cls.setStrategy(new FullClassStrategy(), null);
        if (_custom != null)
            _custom.customize(cls);
    }

    /**
     * Attempt to create a vertical subclasses from the given tables.
     */
    private void mapSubclasses(Set tables) {
        // loop through tables until either all are mapped or none link to
        // a mapped base class table
        ClassMapping base, sub;
        Table table = null;
        ForeignKey fk = null;
        while (!tables.isEmpty()) {
            // find a table with a foreign key linking to a mapped table
            base = null;
            for (Iterator itr = tables.iterator(); itr.hasNext();) {
                table = (Table) itr.next();
                fk = getUniqueForeignKey(table);
                if (fk == null && table.getForeignKeys().length == 1)
                    fk = table.getForeignKeys()[0];
                else if (fk == null)
                    itr.remove();
                else {
                    base = (ClassMapping) _tables.get(fk.getPrimaryKeyTable());
                    if (base != null) {
                        itr.remove();
                        break;
                    }
                }
            }
            // if no tables link to a base table, nothing left to do
            if (base == null)
                return;

            sub = newClassMapping(table, base.getDescribedType());
            sub.setJoinForeignKey(fk);
            sub.setPrimaryKeyColumns(fk.getColumns());
            sub.setIdentityType(base.getIdentityType());
            sub.setStrategy(new VerticalClassStrategy(), null);
            if (_custom != null)
                _custom.customize(sub);
        }
    }

    /**
     * Attempt to reverse map the given table as an association table.
     */
    private void mapAssociationTable(Table table) {
        ForeignKey[] fks = table.getForeignKeys();
        if (fks.length != 2)
            return;

        ClassMapping cls1 = (ClassMapping) _tables.get
            (fks[0].getPrimaryKeyTable());
        ClassMapping cls2 = (ClassMapping) _tables.get
            (fks[1].getPrimaryKeyTable());
        if (cls1 == null || cls2 == null)
            return;

        // add a relation from each class to the other through the
        // association table
        String name = getRelationName(cls2.getDescribedType(), true, fks[1],
            false, cls1);
        FieldMapping field1 = newFieldMapping(name, Set.class, null, fks[1],
            cls1);
        if (field1 != null) {
            field1.setJoinForeignKey(fks[0]);
            addJoinConstraints(field1);
            ValueMapping vm = field1.getElementMapping();
            vm.setDeclaredType(cls2.getDescribedType());
            vm.setForeignKey(fks[1]);
            addConstraints(vm);
            field1.setStrategy(new RelationCollectionTableFieldStrategy(),
                null);
            if (_custom != null)
                _custom.customize(field1);
        }

        name = getRelationName(cls1.getDescribedType(), true, fks[0],
            false, cls2);
        FieldMapping field2 = newFieldMapping(name, Set.class, null, fks[0],
            cls2);
        if (field2 == null)
            return;

        field2.setJoinForeignKey(fks[1]);
        addJoinConstraints(field2);
        ValueMapping vm = field2.getElementMapping();
        vm.setDeclaredType(cls1.getDescribedType());
        vm.setForeignKey(fks[0]);
        addConstraints(vm);
        if (field1 != null && field1.getMappedBy() == null)
            field2.setMappedBy(field1.getName());
        field2.setStrategy(new RelationCollectionTableFieldStrategy(), null);
        if (_custom != null)
            _custom.customize(field2);
    }

    /**
     * Attempt to reverse map the given table as a secondary table.
     */
    private void mapSecondaryTable(Table table, boolean outer) {
        ForeignKey fk = getUniqueForeignKey(table);
        if (fk == null && table.getForeignKeys().length == 1)
            fk = table.getForeignKeys()[0];
        else if (fk == null)
            return;
        ClassMapping cls = (ClassMapping) _tables.get(fk.getPrimaryKeyTable());
        if (cls == null)
            return;
        mapColumns(cls, table, fk, outer);
    }

    /**
     * Map the columns of the given table to fields of the given type.
     */
    private void mapColumns(ClassMapping cls, Table table, ForeignKey join,
        boolean outer) {
        // first map foreign keys to relations
        ForeignKey[] fks = table.getForeignKeys();
        for (int i = 0; i < fks.length; i++)
            if (fks[i] != join && fks[i] != cls.getJoinForeignKey())
                mapForeignKey(cls, fks[i], join, outer);

        // map any columns not controlled by foreign keys; also force primary
        // key cols to get mapped to simple fields even if the columns are
        // also foreign key columns
        PrimaryKey pk = (join != null) ? null : table.getPrimaryKey();
        boolean pkcol;
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            pkcol = pk != null && pk.containsColumn(cols[i]);
            if (pkcol && cls.getIdentityType() == ClassMapping.ID_DATASTORE)
                continue;
            if ((cls.getPCSuperclass() == null && pkcol)
                || !isForeignKeyColumn(cols[i]))
                mapColumn(cls, cols[i], join, outer);
        }
    }

    /**
     * Whether the given column appears in any foreign keys.
     */
    private static boolean isForeignKeyColumn(Column col) {
        ForeignKey[] fks = col.getTable().getForeignKeys();
        for (int i = 0; i < fks.length; i++)
            if (fks[i].containsColumn(col))
                return true;
        return false;
    }

    /**
     * Map a foreign key to a relation.
     */
    private void mapForeignKey(ClassMapping cls, ForeignKey fk,
        ForeignKey join, boolean outer) {
        ClassMapping rel = (ClassMapping) _tables.get(fk.getPrimaryKeyTable());
        if (rel == null)
            return;

        String name = getRelationName(rel.getDescribedType(), false, fk,
            false, cls);
        FieldMapping field1 = newFieldMapping(name, rel.getDescribedType(),
            null, fk, cls);
        if (field1 != null) {
            field1.setJoinForeignKey(join);
            field1.setJoinOuter(outer);
            addJoinConstraints(field1);
            field1.setForeignKey(fk);
            addConstraints(field1);
            field1.setStrategy(new RelationFieldStrategy(), null);
            if (_custom != null)
                _custom.customize(field1);
        }
        if (!_inverse || join != null)
            return;

        // create inverse relation
        boolean unq = isUnique(fk);
        name = getRelationName(cls.getDescribedType(), !unq, fk, true, rel);
        Class type = (unq) ? cls.getDescribedType() : Set.class;
        FieldMapping field2 = newFieldMapping(name, type, null, fk, rel);
        if (field2 == null)
            return;
        if (field1 != null)
            field2.setMappedBy(field1.getName());
        if (unq) {
            field2.setForeignKey(fk);
            field2.setJoinDirection(field2.JOIN_INVERSE);
            field2.setStrategy(new RelationFieldStrategy(), null);
        } else {
            ValueMapping vm = field2.getElementMapping();
            vm.setDeclaredType(cls.getDescribedType());
            vm.setForeignKey(fk);
            vm.setJoinDirection(vm.JOIN_EXPECTED_INVERSE);
            field2.setStrategy(new RelationCollectionInverseKeyFieldStrategy(),
                null);
        }
        if (_custom != null)
            _custom.customize(field2);
    }

    /**
     * Map a column to a simple field.
     */
    private void mapColumn(ClassMapping cls, Column col, ForeignKey join,
        boolean outer) {
        String name = getFieldName(col.getName(), cls);
        Class type = getFieldType(col, false);
        FieldMapping field = newFieldMapping(name, type, col, null, cls);
        field.setSerialized(type == Object.class);
        field.setJoinForeignKey(join);
        field.setJoinOuter(outer);
        addJoinConstraints(field);
        field.setColumns(new Column[]{ col });
        addConstraints(field);
        if (col.isPrimaryKey()
            && cls.getIdentityType() != ClassMapping.ID_DATASTORE)
            field.setPrimaryKey(true);

        FieldStrategy strat;
        if (type.isPrimitive())
            strat = new PrimitiveFieldStrategy();
        else if (col.getType() == Types.CLOB
            && _conf.getDBDictionaryInstance().maxEmbeddedClobSize != -1)
            strat = new MaxEmbeddedClobFieldStrategy();
        else if (col.isLob()
            && _conf.getDBDictionaryInstance().maxEmbeddedBlobSize != -1)
            strat = new MaxEmbeddedBlobFieldStrategy();
        else if (type == String.class)
            strat = new StringFieldStrategy();
        else
            strat = new HandlerFieldStrategy();
        field.setStrategy(strat, null);
        if (_custom != null)
            _custom.customize(field);
    }

    /**
     * Create a class mapping for the given table, or return null if
     * customizer rejects.
     */
    private ClassMapping newClassMapping(Table table, Class parent) {
        String name = getClassName(table);
        if (_custom != null)
            name = _custom.getClassName(table, name);
        if (name == null)
            return null;
        return newClassMapping(generateClass(name, parent), table);
    }

    /**
     * Create a field mapping for the given info, or return null if
     * customizer rejects.
     */
    public FieldMapping newFieldMapping(String name, Class type, Column col,
        ForeignKey fk, ClassMapping dec) {
        if (_custom != null) {
            Column[] cols = (fk == null) ? new Column[]{ col }
                : fk.getColumns();
            String newName = _custom.getFieldName(dec, cols, fk, name);
            if (newName == null || !newName.equals(name)) {
                if (_abandonedFieldNames == null)
                    _abandonedFieldNames = new HashSet();
                _abandonedFieldNames.add(dec.getDescribedType().getName()
                    + "." + name);
                if (newName == null)
                    return null;
                name = newName;
            }
        }

        FieldMapping field = dec.addDeclaredFieldMapping(name, type);
        field.setExplicit(true);
        return field;
    }

    /**
     * Return a Java identifier-formatted name for the given table
     * name, using the default package.
     */
    private String getClassName(Table table) {
        StringBuilder buf = new StringBuilder();
        if (getPackageName() != null)
            buf.append(getPackageName()).append(".");

        String[] subs;
        String name = replaceInvalidCharacters(table.getSchemaName());
        if (_useSchema && name != null) {
            if (allUpperCase(name))
                name = name.toLowerCase();
            subs = Strings.split(name, "_", 0);
            for (int i = 0; i < subs.length; i++)
                buf.append(StringUtils.capitalise(subs[i]));
        }

        name = replaceInvalidCharacters(table.getName());
        if (allUpperCase(name))
            name = name.toLowerCase();
        subs = Strings.split(name, "_", 0);
        for (int i = 0; i < subs.length; i++) {
            // make sure the name can't conflict with generated id class names;
            // if the name would end in 'Id', make it end in 'Ident'
            if (i == subs.length - 1 && subs[i].equalsIgnoreCase("id"))
                subs[i] = "ident";
            buf.append(StringUtils.capitalise(subs[i]));
        }

        return buf.toString();
    }

    /**
     * Return a default Java identifier-formatted name for the given
     * column/table name.
     */
    public String getFieldName(String name, ClassMapping dec) {
        name = replaceInvalidCharacters(name);
        if (allUpperCase(name))
            name = name.toLowerCase();
        else
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        StringBuilder buf = new StringBuilder();
        String[] subs = Strings.split(name, "_", 0);
        for (int i = 0; i < subs.length; i++) {
            if (i > 0)
                subs[i] = StringUtils.capitalise(subs[i]);
            buf.append(subs[i]);
        }
        return getUniqueName(buf.toString(), dec);
    }

    /**
     * Return a default java identifier-formatted field relation name
     * for the given class name.
     */
    private String getRelationName(Class fieldType, boolean coll,
        ForeignKey fk, boolean inverse, ClassMapping dec) {
        if (_useFK && fk.getName() != null) {
            String name = getFieldName(fk.getName(), dec);
            if (inverse && coll)
                name = name + "Inverses";
            else if (inverse)
                name = name + "Inverse";
            return getUniqueName(name, dec);
        }

        // get just the class name, w/o package
        String name = fieldType.getName();
        name = name.substring(name.lastIndexOf('.') + 1);

        // make the first character lowercase and pluralize if a collection
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        if (coll && !name.endsWith("s"))
            name += "s";

        return getUniqueName(name, dec);
    }

    /**
     * Return true if the given string is all uppercase letters.
     */
    private static boolean allUpperCase(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))
                && !Character.isUpperCase(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Replace characters not allowed in Java names with an underscore;
     * package-private for testing.
     */
    static String replaceInvalidCharacters(String str) {
        if (StringUtils.isEmpty(str))
            return str;

        StringBuilder buf = new StringBuilder(str);
        char c;
        for (int i = 0; i < buf.length(); i++) {
            c = buf.charAt(i);
            if (c == '$' || !Character.isJavaIdentifierPart(str.charAt(i)))
                buf.setCharAt(i, '_');
        }

        // strip leading and trailing underscores
        int start = 0;
        while (start < buf.length() && buf.charAt(start) == '_')
            start++;
        int end = buf.length() - 1;
        while (end >= 0 && buf.charAt(end) == '_')
            end--;

        // possible that all chars in name are invalid
        if (start > end)
            return "x";
        return buf.substring(start, end + 1);
    }

    /**
     * Modifies the given name as necessary to ensure that it isn't already
     * taken by a field in the given parent.
     */
    private String getUniqueName(String name, ClassMapping dec) {
        // make sure the name isn't a keyword
        if (_javaKeywords.containsKey(name))
            name = (String) _javaKeywords.get(name);

        // this is the same algorithm used in DBDictionary to get unique names
        String prefix = dec.getDescribedType().getName() + ".";
        for (int version = 2, chars = 1;
            dec.getDeclaredField(name) != null
                || (_abandonedFieldNames != null
                && _abandonedFieldNames.contains(prefix + name)); version++) {
            if (version > 2)
                name = name.substring(0, name.length() - chars);
            if (version >= Math.pow(10, chars))
                chars++;
            name = name + version;
        }
        return name;
    }

    /**
     * Return the default field type for the given column.
     */
    public Class getFieldType(Column col, boolean forceObject) {
        // check the custom type map to see if we've overridden the
        // default type to create for a raw SQL type name
        Class type = null;
        if (_typeMap != null) {
            // first try "TYPE(SIZE,DECIMALS)", then "TYPE(SIZE), then "TYPE"
            String[] propNames = new String[]{
                col.getTypeName() + "(" + col.getSize() + ","
                    + col.getDecimalDigits() + ")",
                col.getTypeName() + "(" + col.getSize() + ")",
                col.getTypeName()
            };

            String typeName = null;
            String typeSpec = null;
            int nameIdx = 0;
            for (; typeSpec == null && nameIdx < propNames.length; nameIdx++) {
                if (propNames[nameIdx] == null)
                    continue;

                typeSpec = StringUtils.trimToNull(_typeMap.getProperty
                    (propNames[nameIdx]));
                if (typeSpec != null) 
                    typeName = propNames[nameIdx];
            }
            if (typeSpec != null)
                _log.info(_loc.get("reverse-type", typeName, typeSpec));
            else
                _log.trace(_loc.get("no-reverse-type",
                    propNames[propNames.length - 1]));

            if (typeSpec != null)
                type = Strings.toClass(typeSpec, _conf.
                    getClassResolverInstance().getClassLoader
                    (ReverseMappingTool.class, null));
        }

        if (type == null)
            type = Schemas.getJavaType(col.getType(), col.getSize(),
                col.getDecimalDigits());
        if (type == Object.class) {
            if (!_blobAsObj)
                return byte[].class;
            return type;
        }

        // treat chars specially; if the dictionary has storeCharsAsNumbers
        // set, then we can't reverse map into a char; use a string and tell
        // the user why we are doing so
        if (type == char.class
            && _conf.getDBDictionaryInstance().storeCharsAsNumbers) {
            type = String.class;
            if (_log.isWarnEnabled())
                _log.warn(_loc.get("cant-use-char", col.getFullName()));
        }

        if (!type.isPrimitive())
            return type;
        if (!forceObject && (col.isNotNull() || !_nullAsObj))
            return type;

        // convert the type into the appropriate wrapper class
        switch (type.getName().charAt(0)) {
            case'b':
                if (type == boolean.class)
                    return Boolean.class;
                return Byte.class;
            case'c':
                return Character.class;
            case'd':
                return Double.class;
            case'f':
                return Float.class;
            case'i':
                return Integer.class;
            case'l':
                return Long.class;
            case's':
                return Short.class;
            default:
                throw new InternalException();
        }
    }

    /**
     * Return a new tool with the same settings as this one. Used in workbench.
     */
    public Object clone() {
        ReverseMappingTool tool = new ReverseMappingTool(_conf);
        tool.setSchemaGroup(getSchemaGroup());
        tool.setPackageName(getPackageName());
        tool.setDirectory(getDirectory());
        tool.setUseSchemaName(getUseSchemaName());
        tool.setUseForeignKeyName(getUseForeignKeyName());
        tool.setNullableAsObject(getNullableAsObject());
        tool.setBlobAsObject(getBlobAsObject());
        tool.setUseGenericCollections(getUseGenericCollections());
        tool.setPrimaryKeyOnJoin(getPrimaryKeyOnJoin());
        tool.setUseDataStoreIdentity(getUseDataStoreIdentity());
        tool.setUseBuiltinIdentityClass(getUseBuiltinIdentityClass());
        tool.setInnerIdentityClasses(getInnerIdentityClasses());
        tool.setIdentityClassSuffix(getIdentityClassSuffix());
        tool.setInverseRelations(getInverseRelations());
        tool.setDetachable(getDetachable());
        tool.setGenerateAnnotations(getGenerateAnnotations());
        tool.setCustomizer(getCustomizer());
        tool.setCodeFormat(getCodeFormat());
        tool.setUseSchemaElement(getUseSchemaElement());
        return tool;
    }

    ////////
    // Main
    ////////

    /**
     * Usage: java org.apache.openjpa.jdbc.meta.ReverseMappingTool
     * [option]* &lt;.schema file or resource&gt;*
     *  Where the following options are recognized.
     * <ul>
     * <li><i>-properties/-p &lt;properties file or resource&gt;</i>: The
     * path or resource name of a OpenJPA properties file containing
     * information such as the license key	data as outlined in
     * {@link OpenJPAConfiguration}. Optional.</li>
     * <li><i>-&lt;property name&gt; &lt;property value&gt;</i>: All bean
     * properties of the OpenJPA {@link JDBCConfiguration} can be set by
     * using their	names and supplying a value. For example:
     * <code>-licenseKey adslfja83r3lkadf</code></li>
     * <li><i>-schemas/-s &lt;schemas and tables&gt;</i>: Comma-separated
     * list of schemas and tables to reverse-map.</li>
     * <li><i>-package/-pkg &lt;package name&gt;</i>: The name of the package
     * for all generated classes. Defaults to no package.</li>
     * <li><i>-directory/-d &lt;output directory&gt;</i>: The directory where
     * all generated code should be placed. Defaults to the current
     * directory.</li>
     * <li><i>-useSchemaName/-sn &lt;true/t | false/f&gt;</i>: Set this flag to
     * true to include the schema name as part of the generated class name
     * for each table.</li>
     * <li><i>-useSchemaElement/-se &lt;true/t | false/f&gt;</i>: Set this
     * flag to false to exclude the schema name from the @Table annotation
     * in the generated class for each table. If set to false, the schema 
     * name will also be removed from the corresponding XML mapping files
     * (orm.xml) that are generated by the tool. The initialized value is
     * true (in order to preserve backwards compatibility). </li>
     * <li><i>-useForeignKeyName/-fkn &lt;true/t | false/f&gt;</i>: Set this
     * flag to true to use the foreign key name to generate fields
     * representing relations between classes.</li>
     * <li><i>-nullableAsObject/-no &lt;true/t | false/f&gt;</i>: Set to true
     * to make all nullable columns map to object types; columns that would
     * normally map to a primitive will map to the appropriate wrapper
     * type instead.</li>
     * <li><i>-blobAsObject/-bo &lt;true/t | false/f&gt;</i>: Set to true
     * to make all binary columns map to Object rather than byte[].</li>
     * <li><i>-useGenericCollections/-gc &lt;true/t | false/f&gt;</i>: Set to
     * true to use generic collections on OneToMany and ManyToMany relations
     * (requires JDK 1.5 or higher).</li>
     * <li><i>-typeMap/-typ &lt;types&gt;</i>: Default mapping of SQL type
     * names to Java classes.</li>
     * <li><i>-primaryKeyOnJoin/-pkj &lt;true/t | false/f&gt;</i>: Set to true
     * to allow primary keys on join tables.</li>
     * <li><i>-useDatastoreIdentity/-ds &lt;true/t | false/f&gt;</i>: Set to
     * true to use datastore identity where possible.</li>
     * <li><i>-useBuiltinIdentityClass/-bic &lt;true/t | false/f&gt;</i>: Set
     * to false to never use OpenJPA's builtin application identity
     * classes.</li>
     * <li><i>-innerIdentityClasses/-inn &lt;true/t | false/f&gt;</i>: Set to
     * true to generate the application identity classes as inner classes.</li>
     * <li><i>-identityClassSuffix/-is &lt;suffix&gt;</i>: Suffix to append
     * to class names to create identity class name, or for inner identity
     * classes, the inner class name.</li>
     * <li><i>-inverseRelations/-ir &lt;true/t | false/f&gt;</i>: Set to
     * false to prevent the creation of inverse 1-many/1-1 relations for
     * each encountered many-1/1-1 relation.</li>
     * <li><i>-detachable/-det &lt;true/t | false/f&gt;</i>: Set to
     * true to make generated classes detachable.</li>
     * <li><i>-discriminatorStrategy/-ds &lt;strategy&gt;</i>: The default
     * discriminator strategy to place on base classes.</li>
     * <li><i>-versionStrategy/-vs &lt;strategy&gt;</i>: The default
     * version strategy to place on base classes.</li>
     * <li><i>-metadata/-md &lt;class | package | none&gt;</i>: Specify the
     * level the metadata should be generated at. Defaults to generating a
     * single package-level metadata file.</li>
     * <li><i>-annotations/-ann &lt;true/t | false/f&gt;</i>: Set to true to
     * generate JPA annotations in generated code.</li>
     * <li><i>-accessType/-access &lt;field | property&gt;</i>: Change access
     * type for generated annotations. Defaults to field access.</li>
     * <li><i>-customizerClass/-cc &lt;class name&gt;</i>: The full class
     * name of a {@link ReverseCustomizer} implementation to use to
     * customize the reverse mapping process. Optional.</li>
     * <li><i>-customizerProperties/-cp &lt;properties file or resource&gt;</i>
     * : The path or resource name of a properties file that will be
     * passed to the reverse customizer on initialization. Optional.</li>
     * <li><i>-customizer/-c.&lt;property name&gt; &lt; property value&gt;</i>
     * : Arguments like this will be used to configure the bean
     * properties of the {@link ReverseCustomizer}.</li>
     * <li><i>-codeFormat/-cf.&lt;property name&gt; &lt; property value&gt;</i>
     * : Arguments like this will be used to configure the bean
     * properties of the internal {@link CodeFormat}.</li>
     * </ul>
     *  Each schema given as an argument will be reverse-mapped into
     * persistent classes and associated metadata. If no arguments are given,
     * the database schemas defined by the system configuration will be
     * reverse-mapped.
     */
    public static void main(String[] args)
        throws IOException, SQLException {
        Options opts = new Options();
        final String[] arguments = opts.setFromCmdLine(args);
        boolean ret = Configurations.runAgainstAllAnchors(opts,
            new Configurations.Runnable() {
            public boolean run(Options opts) throws Exception {
                JDBCConfiguration conf = new JDBCConfigurationImpl();
                try {
                    return ReverseMappingTool.run(conf, arguments, opts);
                } finally {
                    conf.close();
                }
            }
        });
        if (!ret) {
            // START - ALLOW PRINT STATEMENTS
            System.out.println(_loc.get("revtool-usage"));
            // STOP - ALLOW PRINT STATEMENTS
        }
    }

    /**
     * Run the tool. Returns false if invalid options were given.
     *
     * @see #main
     */
    public static boolean run(JDBCConfiguration conf, String[] args,
        Options opts)
        throws IOException, SQLException {
        // flags
        Flags flags = new Flags();
        flags.packageName = opts.removeProperty
            ("package", "pkg", flags.packageName);
        flags.directory = Files.getFile
            (opts.removeProperty("directory", "d", null), null);
        flags.useSchemaName = opts.removeBooleanProperty
            ("useSchemaName", "sn", flags.useSchemaName);
        flags.useForeignKeyName = opts.removeBooleanProperty
            ("useForeignKeyName", "fkn", flags.useForeignKeyName);
        flags.nullableAsObject = opts.removeBooleanProperty
            ("nullableAsObject", "no", flags.nullableAsObject);
        flags.blobAsObject = opts.removeBooleanProperty
            ("blobAsObject", "bo", flags.blobAsObject);
        flags.useGenericCollections = opts.removeBooleanProperty
            ("useGenericCollections", "gc", flags.useGenericCollections);
        flags.primaryKeyOnJoin = opts.removeBooleanProperty
            ("primaryKeyOnJoin", "pkj", flags.primaryKeyOnJoin);
        flags.useDataStoreIdentity = opts.removeBooleanProperty
            ("useDatastoreIdentity", "ds", flags.useDataStoreIdentity);
        flags.useBuiltinIdentityClass = opts.removeBooleanProperty
            ("useBuiltinIdentityClass", "bic", flags.useBuiltinIdentityClass);
        flags.innerIdentityClasses = opts.removeBooleanProperty
            ("innerIdentityClasses", "inn", flags.innerIdentityClasses);
        flags.identityClassSuffix = opts.removeProperty
            ("identityClassSuffix", "is", flags.identityClassSuffix);
        flags.inverseRelations = opts.removeBooleanProperty
            ("inverseRelations", "ir", flags.inverseRelations);
        flags.detachable = opts.removeBooleanProperty
            ("detachable", "det", flags.detachable);
        flags.discriminatorStrategy = opts.removeProperty
            ("discriminatorStrategy", "ds", flags.discriminatorStrategy);
        flags.versionStrategy = opts.removeProperty
            ("versionStrategy", "vs", flags.versionStrategy);
        flags.metaDataLevel = opts.removeProperty
            ("metadata", "md", flags.metaDataLevel);        
        flags.generateAnnotations = opts.removeBooleanProperty
            ("annotations", "ann", flags.generateAnnotations);
        flags.accessType = opts.removeProperty
            ("accessType", "access", flags.accessType);
        flags.useSchemaElement = opts.removeBooleanProperty
        	("useSchemaElement", "se", flags.useSchemaElement);

        String typeMap = opts.removeProperty("typeMap", "typ", null);
        if (typeMap != null)
            flags.typeMap = Configurations.parseProperties(typeMap);

        // remap the -s shortcut to the "schemas" property name so that it
        // gets set into the configuration
        if (opts.containsKey("s"))
            opts.put("schemas", opts.get("s"));

        // customizer
        String customCls = opts.removeProperty("customizerClass", "cc",
            PropertiesReverseCustomizer.class.getName());
        File customFile = Files.getFile
            (opts.removeProperty("customizerProperties", "cp", null), null);
        Properties customProps = new Properties();
        if (customFile != null && (AccessController.doPrivileged(
            J2DoPrivHelper.existsAction(customFile))).booleanValue()) {
            FileInputStream fis = null;
            try {
                fis = AccessController.doPrivileged(
                    J2DoPrivHelper.newFileInputStreamAction(customFile));
            } catch (PrivilegedActionException pae) {
                 throw (FileNotFoundException) pae.getException();
            }
            customProps.load(fis);
        }
        
        // separate the properties for the customizer and code format
        Options customOpts = new Options();
        Options formatOpts = new Options();
        Map.Entry entry;
        String key;
        for (Iterator itr = opts.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            key = (String) entry.getKey();
            if (key.startsWith("customizer.")) {
                customOpts.put(key.substring(11), entry.getValue());
                itr.remove();
            } else if (key.startsWith("c.")) {
                customOpts.put(key.substring(2), entry.getValue());
                itr.remove();
            } else if (key.startsWith("codeFormat.")) {
                formatOpts.put(key.substring(11), entry.getValue());
                itr.remove();
            } else if (key.startsWith("cf.")) {
                formatOpts.put(key.substring(3), entry.getValue());
                itr.remove();
            }
        }

        // code format
        if (!formatOpts.isEmpty()) {
            flags.format = new CodeFormat();
            formatOpts.setInto(flags.format);
        }

        // setup a configuration instance with cmd-line info
        Configurations.populateConfiguration(conf, opts);
        ClassLoader loader = conf.getClassResolverInstance().
            getClassLoader(ReverseMappingTool.class, null);

        // customizer
        flags.customizer = (ReverseCustomizer) Configurations.
            newInstance(customCls, loader);
        if (flags.customizer != null) {
            Configurations.configureInstance(flags.customizer, conf,
                customOpts);
            flags.customizer.setConfiguration(customProps);
        }

        run(conf, args, flags, loader);
        return true;
    }

    /**
     * Run the tool.
     */
    public static void run(JDBCConfiguration conf, String[] args,
        Flags flags, ClassLoader loader)
        throws IOException, SQLException {
        // parse the schema to reverse-map
        Log log = conf.getLog(OpenJPAConfiguration.LOG_TOOL);
        SchemaGroup schema;
        if (args.length == 0) {
            log.info(_loc.get("revtool-running"));
            SchemaGenerator gen = new SchemaGenerator(conf);
            gen.generateSchemas();
            schema = gen.getSchemaGroup();
        } else {
            SchemaParser parser = new XMLSchemaParser(conf);
            File file;
            for (int i = 0; i < args.length; i++) {
                file = Files.getFile(args[i], loader);
                log.info(_loc.get("revtool-running-file", file));
                parser.parse(file);
            }
            schema = parser.getSchemaGroup();
        }

        // flags
        ReverseMappingTool tool = new ReverseMappingTool(conf);
        tool.setSchemaGroup(schema);
        tool.setPackageName(flags.packageName);
        tool.setDirectory(flags.directory);
        tool.setUseSchemaName(flags.useSchemaName);
        tool.setUseForeignKeyName(flags.useForeignKeyName);
        tool.setNullableAsObject(flags.nullableAsObject);
        tool.setBlobAsObject(flags.blobAsObject);
        tool.setUseGenericCollections(flags.useGenericCollections);
        tool.setTypeMap(flags.typeMap);
        tool.setPrimaryKeyOnJoin(flags.primaryKeyOnJoin);
        tool.setUseDataStoreIdentity(flags.useDataStoreIdentity);
        tool.setUseBuiltinIdentityClass(flags.useBuiltinIdentityClass);
        tool.setInnerIdentityClasses(flags.innerIdentityClasses);
        tool.setIdentityClassSuffix(flags.identityClassSuffix);
        tool.setInverseRelations(flags.inverseRelations);
        tool.setDetachable(flags.detachable);
        tool.setGenerateAnnotations(flags.generateAnnotations);
        tool.setAccessType(flags.accessType);
        tool.setCustomizer(flags.customizer);
        tool.setCodeFormat(flags.format);
        tool.setUseSchemaElement(flags.useSchemaElement);

        // run
        log.info(_loc.get("revtool-map"));
        tool.run();
        if (flags.generateAnnotations) {
            log.info(_loc.get("revtool-gen-annos"));
            tool.buildAnnotations();
        }
        log.info(_loc.get("revtool-write-code"));
        tool.recordCode();
        if (!LEVEL_NONE.equals(flags.metaDataLevel)) {
            log.info(_loc.get("revtool-write-metadata"));
            tool.recordMetaData(LEVEL_CLASS.equals(flags.metaDataLevel));
        }
    }

    /**
     * Holder for run flags.
     */
    public static class Flags {

        public String packageName = null;
        public File directory = null;
        public boolean useSchemaName = false;
        public boolean useForeignKeyName = false;
        public boolean nullableAsObject = false;
        public boolean blobAsObject = false;
        public boolean useGenericCollections = false;
        public Properties typeMap = null;
        public boolean primaryKeyOnJoin = false;
        public boolean useDataStoreIdentity = false;
        public boolean useBuiltinIdentityClass = true;
        public boolean innerIdentityClasses = false;
        public String identityClassSuffix = "Id";
        public boolean inverseRelations = true;
        public boolean detachable = false;
        public boolean generateAnnotations = false;
        public String accessType = ACCESS_TYPE_FIELD;
        public String metaDataLevel = LEVEL_PACKAGE;
        public String discriminatorStrategy = null;
        public String versionStrategy = null;
        public ReverseCustomizer customizer = null;
        public CodeFormat format = null;
        public boolean useSchemaElement = true;
    }

    /**
     * Used to install discriminator and version strategies on
     * reverse-mapped classes.
     */
    private class ReverseStrategyInstaller
        extends StrategyInstaller {

        public ReverseStrategyInstaller(MappingRepository repos) {
            super(repos);
        }

        public void installStrategy(ClassMapping cls) {
            throw new InternalException();
        }

        public void installStrategy(FieldMapping field) {
            throw new InternalException();
        }

        public void installStrategy(Version version) {
            ClassMapping cls = version.getClassMapping();
            if (cls.getPCSuperclass() != null)
                version.setStrategy(new SuperclassVersionStrategy(), null);
            else if (_versStrat != null) {
                VersionStrategy strat = repos.instantiateVersionStrategy
                    (_versStrat, version);
                version.setStrategy(strat, null);
            } else
                version.setStrategy(new StateComparisonVersionStrategy(),
                    null);
        }

        public void installStrategy(Discriminator discrim) {
            ClassMapping cls = discrim.getClassMapping();
            if (cls.getPCSuperclass() != null) {
                discrim.setStrategy(new SuperclassDiscriminatorStrategy(),
                    null);
            } else if (!hasSubclasses(cls)) {
                discrim.setStrategy(NoneDiscriminatorStrategy.getInstance(),
                    null);
            } else if (_discStrat != null) {
                DiscriminatorStrategy strat = repos.
                    instantiateDiscriminatorStrategy(_discStrat, discrim);
                discrim.setStrategy(strat, null);
            } else
                discrim.setStrategy(new SubclassJoinDiscriminatorStrategy(),
                    null);
        }

        /**
         * Return whether the given class has any mapped persistent subclasses.
         */
        private boolean hasSubclasses(ClassMapping cls) {
            ClassMetaData[] metas = repos.getMetaDatas();
            for (int i = 0; i < metas.length; i++)
                if (metas[i].getPCSuperclass() == cls.getDescribedType())
                    return true;
            return false;
        }
    }

    /**
     * Extension of the {@link CodeGenerator} to allow users to customize
     * the formatting of their generated classes.
     */
    private class ReverseCodeGenerator
        extends CodeGenerator {

        protected final ClassMapping _mapping;
        protected final ApplicationIdTool _appid;

        public ReverseCodeGenerator(ClassMapping mapping,
            ApplicationIdTool aid) {
            super(mapping);
            super.setDirectory(_dir);
            super.setCodeFormat(_format);

            _mapping = mapping;
            if (aid != null && aid.isInnerClass())
                _appid = aid;
            else
                _appid = null;
        }

        /**
         * If there is an inner application identity class, then
         * add it to the bottom of the class code.
         */
        protected void closeClassBrace(CodeFormat code) {
            if (_appid != null) {
                code.afterSection();
                code.append(_appid.getCode());
                code.endl();
            }

            super.closeClassBrace(code);
        }

        /**
         * Add the list of imports for any inner app id classes
         *
         * @return
         */
        public Set getImportPackages() {
            Set pkgs = super.getImportPackages();
            if (_appid != null)
                pkgs.addAll(_appid.getImportPackages());
            return pkgs;
        }

        protected String getClassCode() {
            return (_custom == null) ? null : _custom.getClassCode(_mapping);
        }

        protected String getInitialValue(FieldMetaData field) {
            if (_custom == null)
                return null;
            return _custom.getInitialValue((FieldMapping) field);
        }

        protected String getDeclaration(FieldMetaData field) {
            if (_custom == null)
                return null;
            return _custom.getDeclaration((FieldMapping) field);
        }

        protected String getFieldCode(FieldMetaData field) {
            if (_custom == null)
                return null;
            return _custom.getFieldCode((FieldMapping) field);
        }

        protected boolean useGenericCollections() {
            return _useGenericColl;
        }
    }

    private class AnnotatedCodeGenerator
        extends ReverseCodeGenerator {

        public AnnotatedCodeGenerator (ClassMapping mapping,
            ApplicationIdTool aid) {
            super (mapping, aid);
        }

        public Set getImportPackages() {
            Set pkgs = super.getImportPackages();
            pkgs.add("javax.persistence");
            return pkgs;
        }

        protected List getClassAnnotations() {
            return getAnnotationsForMeta(_mapping);
        }

        protected List getFieldAnnotations(FieldMetaData field) {
            return getAnnotationsForMeta(field);
        }

        protected boolean usePropertyBasedAccess () {
            return ACCESS_TYPE_PROPERTY.equals(_accessType);
        }

    }
}
