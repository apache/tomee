/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.entity.cmp;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.util.ClassLoading;
import org.tranql.builder.DynamicCommandBuilder;
import org.tranql.builder.GlobalSchemaBuilder;
import org.tranql.builder.StaticCommandBuilder;
import org.tranql.cache.CacheFlushStrategyFactory;
import org.tranql.cache.CacheTable;
import org.tranql.cache.EnforceRelationshipsFlushStrategyFactory;
import org.tranql.cache.GlobalSchema;
import org.tranql.cache.SimpleFlushStrategyFactory;
import org.tranql.ejb.CMPField;
import org.tranql.ejb.CMRField;
import org.tranql.ejb.EJB;
import org.tranql.ejb.EJBProxyFactory;
import org.tranql.ejb.EJBSchema;
import org.tranql.ejb.FKField;
import org.tranql.ejb.FinderEJBQLQuery;
import org.tranql.ejb.Relationship;
import org.tranql.ejb.SelectEJBQLQuery;
import org.tranql.ejbqlcompiler.DerbyDBSyntaxtFactory;
import org.tranql.ejbqlcompiler.DerbyEJBQLCompilerFactory;
import org.tranql.intertxcache.CacheFactory;
import org.tranql.intertxcache.ReadCommittedCacheFactory;
import org.tranql.intertxcache.ReadUncommittedCacheFactory;
import org.tranql.intertxcache.RepeatableReadCacheFactory;
import org.tranql.pkgenerator.SQLPrimaryKeyGenerator;
import org.tranql.pkgenerator.SequenceTablePrimaryKeyGenerator;
import org.tranql.ql.QueryBindingImpl;
import org.tranql.ql.QueryException;
import org.tranql.schema.Association;
import org.tranql.sql.BaseSQLSchema;
import org.tranql.sql.Column;
import org.tranql.sql.DBSyntaxFactory;
import org.tranql.sql.EJBQLCompilerFactory;
import org.tranql.sql.EndTable;
import org.tranql.sql.FKColumn;
import org.tranql.sql.JoinTable;
import org.tranql.sql.SQLSchema;
import org.tranql.sql.Table;
import org.tranql.sql.TypeConverter;
import org.tranql.sql.UpdateCommandBuilder;
import org.tranql.sql.jdbc.SQLTypeLoader;
import org.tranql.sql.jdbc.binding.BindingFactory;
import org.tranql.sql.prefetch.PrefetchGroupDictionary;

public class TranqlSchemaBuilder {
    private final ModuleSchema moduleSchema;
    private final DataSource dataSource;
    private final TransactionManager transactionManager;
    private final ClassLoader classLoader;
    private EJBSchema ejbSchema;
    private SQLSchema sqlSchema;
    private GlobalSchema globalSchema;

    public TranqlSchemaBuilder(ModuleSchema moduleSchema, DataSource dataSource, TransactionManager transactionManager, ClassLoader classLoader) {
        this.moduleSchema = moduleSchema;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.classLoader = classLoader;
    }

    public EJBSchema getEjbSchema() {
        return ejbSchema;
    }

    public SQLSchema getSqlSchema() {
        return sqlSchema;
    }

    public GlobalSchema getGlobalSchema() {
        return globalSchema;
    }

    public void buildSchema() throws Exception {
        CacheFlushStrategyFactory flushStrategyFactory;
        if (moduleSchema.isEnforceForeignKeyConstraints()) {
            flushStrategyFactory = new EnforceRelationshipsFlushStrategyFactory();
        } else {
            flushStrategyFactory = new SimpleFlushStrategyFactory();
        }

        EJBQLCompilerFactory compilerFactory;
        String compilerFactoryName = moduleSchema.getEjbQlCompilerFactory();
        if (compilerFactoryName != null) {
            Object factory;
            try {
                Class clazz = classLoader.loadClass(compilerFactoryName);
                Constructor constructor = clazz.getConstructor(null);
                factory = constructor.newInstance(null);
            } catch (Exception e) {
                throw new Exception("Unable to initialize ejb-ql-compiler-factory=" + compilerFactoryName, e);
            }
            if (!(factory instanceof EJBQLCompilerFactory)) {
                throw new Exception("EJBQLCompilerFactory expected. was=" + factory);
            }
            compilerFactory = (EJBQLCompilerFactory) factory;
        } else {
            compilerFactory = new DerbyEJBQLCompilerFactory();
        }

        DBSyntaxFactory syntaxFactory;
        String syntaxFactoryName = moduleSchema.getDbSyntaxFactory();
        if (syntaxFactoryName != null) {
            Object factory;
            try {
                Class clazz = classLoader.loadClass(syntaxFactoryName);
                Constructor constructor = clazz.getConstructor(null);
                factory = constructor.newInstance(null);
            } catch (Exception e) {
                throw new Exception("Unable to initialize ejb-ql-compiler-factory=" + syntaxFactoryName, e);
            }
            if (!(factory instanceof DBSyntaxFactory)) {
                throw new Exception("DBSyntaxFactory expected. was=" + factory);
            }
            syntaxFactory = (DBSyntaxFactory) factory;
        } else {
            syntaxFactory = new DerbyDBSyntaxtFactory();
        }

        String moduleName = moduleSchema.getName();
        ejbSchema = new EJBSchema(moduleName);
        sqlSchema = new BaseSQLSchema(moduleName, dataSource, syntaxFactory, compilerFactory);
        globalSchema = new GlobalSchema(moduleName, flushStrategyFactory);

        try {
            processEnterpriseBeans();
            processRelationships();
            processGroups();
            GlobalSchemaBuilder loader = new GlobalSchemaBuilder(globalSchema, ejbSchema, sqlSchema);
            loader.build();
            processEnterpriseBeanCaches();
        } catch (Exception e) {
            throw new Exception("Could not deploy module", e);
        }
    }

    private void processEnterpriseBeans() throws Exception {
        for (Iterator iterator = moduleSchema.getEntities().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String ejbName = (String) entry.getKey();
            EntitySchema entitySchema = (EntitySchema) entry.getValue();

            String abstractSchemaName = entitySchema.getAbstractSchemaName();

            EJBProxyFactory proxyFactory = buildEJBProxyFactory(entitySchema.getContainerId(),
                    entitySchema.getRemoteInterfaceName(),
                    entitySchema.getHomeInterfaceName(),
                    entitySchema.getLocalInterfaceName(),
                    entitySchema.getLocalHomeInterfaceName(),
                    classLoader);

            boolean unknownPk = entitySchema.isUnknownPk();

            Class pkClass;
            try {
                String pkClassName = entitySchema.getPkClassName();
                pkClass = classLoader.loadClass(pkClassName);
            } catch (ClassNotFoundException e) {
                throw new Exception("Could not load cmp primary key class: ejbName=" + ejbName + " pkClass=" + entitySchema.getPkClassName());
            }

            PrimaryKeyGenerator primaryKeyGenerator = entitySchema.getPrimaryKeyGenerator();
            org.tranql.pkgenerator.PrimaryKeyGenerator keyGenerator = null;
            if (primaryKeyGenerator != null) {
                try {
                    keyGenerator = configurePrimaryKeyGenerator(primaryKeyGenerator, pkClass);
                } catch (QueryException e) {
                    throw new Exception("Unable to load PK Generator for EJB " + ejbName, e);
                }
            }

            EJB ejb = new EJB(ejbName, abstractSchemaName, pkClass, proxyFactory, keyGenerator, unknownPk);
            Table table = new Table(ejbName, entitySchema.getTableName());

            UpdateCommandBuilder commandBuilder;
            if (entitySchema.isStaticSql()) {
                commandBuilder = new StaticCommandBuilder(ejbName, ejbSchema, sqlSchema, globalSchema);
            } else {
                commandBuilder = new DynamicCommandBuilder(ejbName, ejbSchema, sqlSchema, globalSchema);
            }
            table.setCommandBuilder(commandBuilder);

            for (Iterator cmpFieldsIterator = entitySchema.getCmpFields().entrySet().iterator(); cmpFieldsIterator.hasNext();) {
                Map.Entry entry1 = (Map.Entry) cmpFieldsIterator.next();
                String cmpFieldName = (String) entry1.getKey();
                CmpFieldSchema cmpFieldSchema = (CmpFieldSchema) entry1.getValue();
                String fieldTypeName = cmpFieldSchema.getFieldTypeName();
                Class fieldType = loadClass(fieldTypeName, true, "cmp-field " + cmpFieldName);
                boolean isPkField = cmpFieldSchema.isPkField();

                // -- add a field to the ejb
                CMPField cmpField = new CMPField(cmpFieldName, cmpFieldName, fieldType, isPkField);
                if (cmpFieldSchema.isVirtual()) {
                    ejb.addVirtualCMPField(cmpField);
                } else {
                    ejb.addCMPField(cmpField);
                }

                // -- add a column to the table
                Column column = new Column(cmpFieldName, cmpFieldSchema.getColumnName(), fieldType, isPkField);

                // sqlType
                String sqlType = cmpFieldSchema.getSqlType();
                if (sqlType != null) {
                    column.setSQLType(SQLTypeLoader.getSQLType(sqlType));
                }

                // typeConverter
                String typeConverterClassName = cmpFieldSchema.getTypeConverterClassName();
                if (typeConverterClassName != null) {
                    TypeConverter typeConverter;
                    try {
                        Class typeConverterClass = classLoader.loadClass(typeConverterClassName);
                        typeConverter = (TypeConverter) typeConverterClass.newInstance();
                    } catch (Exception e) {
                        throw new Exception("Cannot create type converter " + typeConverterClassName, e);
                    }
                    column.setTypeConverter(typeConverter);
                }
                table.addColumn(column);
            }

            processQuery(entitySchema, ejb);

            ejbSchema.addEJB(ejb);
            sqlSchema.addTable(table);
        }
    }

    protected org.tranql.pkgenerator.PrimaryKeyGenerator configurePrimaryKeyGenerator(PrimaryKeyGenerator primaryKeyGenerator, Class pkClass) throws QueryException {
        //todo: Handle a PK Class with multiple fields?
        if (primaryKeyGenerator instanceof CustomPrimaryKeyGenerator) {
            CustomPrimaryKeyGenerator custom = (CustomPrimaryKeyGenerator) primaryKeyGenerator;
            String generatorName = custom.getGeneratorName();
            ObjectName generatorObjectName = null;
            try {
                generatorObjectName = new ObjectName(generatorName);
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("CustomPrimaryKeyGenerator name is not a valid ObjectName: " + generatorName);
            }
            org.tranql.pkgenerator.PrimaryKeyGenerator generator = null; //TODO: (org.tranql.pkgenerator.PrimaryKeyGenerator) kernel.getProxyManager().createProxy(generatorObjectName, org.tranql.pkgenerator.PrimaryKeyGenerator.class);
            return generator;
        } else if (primaryKeyGenerator instanceof SqlPrimaryKeyGenerator) {
            SqlPrimaryKeyGenerator sqlGen = (SqlPrimaryKeyGenerator) primaryKeyGenerator;
            String sql = sqlGen.getSql();
            return new SQLPrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, pkClass)));
        } else if (primaryKeyGenerator instanceof SequenceTableKeyGenerator) {
            SequenceTableKeyGenerator seq = (SequenceTableKeyGenerator) primaryKeyGenerator;
            String tableName = seq.getTableName();
            String sequenceName = seq.getSequenceName();
            int batchSize = seq.getBatchSize();
            SequenceTablePrimaryKeyGenerator generator = new SequenceTablePrimaryKeyGenerator(transactionManager, dataSource, tableName, sequenceName, batchSize);
            return generator;
        } else if (primaryKeyGenerator instanceof AutoIncrementTablePrimaryKeyGenerator) {
            AutoIncrementTablePrimaryKeyGenerator auto = (AutoIncrementTablePrimaryKeyGenerator) primaryKeyGenerator;
            String sql = auto.getSql();
            return new org.tranql.pkgenerator.AutoIncrementTablePrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, pkClass)));
//        } else if(config.isSetDatabaseGenerated()) {
            //todo: need to somehow implement this behavior in TranQL
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    private void processQuery(EntitySchema entitySchema, EJB ejb) throws Exception {
        for (Iterator iterator = entitySchema.getQueries().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature methodSignature = (MethodSignature) entry.getKey();
            QuerySpec querySpec = (QuerySpec) entry.getValue();

            String methodName = methodSignature.getMethodName();
            Class[] parameterTypes = loadClasses(methodSignature.getParameterTypes(), methodName);
            if (methodName.startsWith("find")) {
                FinderEJBQLQuery query = new FinderEJBQLQuery(methodName, parameterTypes, querySpec.getEjbQl());
                query.setFlushCacheBeforeQuery(querySpec.isFlushCacheBeforeQuery());
                query.setPrefetchGroup(querySpec.getPrefetchGroup());
                ejb.addFinder(query);
            } else if (methodName.startsWith("ejbSelect")) {
                boolean isLocal = querySpec.isLocal();
                SelectEJBQLQuery query = new SelectEJBQLQuery(methodName, parameterTypes, querySpec.getEjbQl(), isLocal);
                query.setFlushCacheBeforeQuery(querySpec.isFlushCacheBeforeQuery());
                query.setPrefetchGroup(querySpec.getPrefetchGroup());
                ejb.addSelect(query);
            } else {
                throw new Exception("Method " + methodName + " is neiher a finder nor a select.");
            }
        }
    }

    private void processRelationships() throws Exception {
        int id = 0;
        for (Iterator iterator = moduleSchema.getRelations().iterator(); iterator.hasNext();) {
            RelationSchema relationSchema = (RelationSchema) iterator.next();
            if (relationSchema instanceof OneToManyRelationSchema) {
                OneToManyRelationSchema oneToManyRelationSchema = (OneToManyRelationSchema) relationSchema;
                buildSchemaForJoin(oneToManyRelationSchema, id);

            } else {
                ManyToManyRelationSchema manyToManyRelationSchema = (ManyToManyRelationSchema) relationSchema;
                buildSchemaForJoin(manyToManyRelationSchema, id);
            }
            id++;
        }
    }

    private void buildSchemaForJoin(OneToManyRelationSchema oneToManyRelationSchema, int id) throws Exception {
        RoleSchema pkRole = oneToManyRelationSchema.getPkRole();
        RoleSchema fkRole = oneToManyRelationSchema.getFkRole();

        // get the pk ejb and table
        String pkEntityName = pkRole.getEjbName();
        EJB pkEjb = ejbSchema.getEJB(pkEntityName);
        Table pkTable = sqlSchema.getTable(pkEntityName);

        // get the fk ejb and table
        String fkEntityName = fkRole.getEjbName();
        EJB fkEjb = ejbSchema.getEJB(fkEntityName);
        Table fkTable = sqlSchema.getTable(fkEntityName);

        // map the fields and columns
        RelationMapping relationMapping = createRelationMapping(pkRole, pkEjb, pkTable, fkTable);

        // create the ejb relation and sql join objects
        Relationship relationship = new Relationship(new Association.JoinDefinition(pkEjb, fkEjb, relationMapping.getEjbFieldMappings()));
        JoinTable joinTable = new JoinTable(new Association.JoinDefinition(pkTable, fkTable, relationMapping.getColumnMappings()));

        // add the pk cmr field and column
        boolean isPkRoleVirtual = null == pkRole.getCmrFieldName();
        String pkCmrFieldName = isPkRoleVirtual ? "$VirtualEnd" + id : pkRole.getCmrFieldName();
        pkEjb.addCMRField(new CMRField(pkCmrFieldName, fkEjb, fkRole.isOne(), fkRole.isCascadeDelete(), relationship, isPkRoleVirtual, true));
        pkTable.addEndTable(new EndTable(pkCmrFieldName, fkTable, fkRole.isOne(), fkRole.isCascadeDelete(), joinTable, isPkRoleVirtual, true));

        // add the fk cmr field and column
        boolean isFkRoleVirtual = null == fkRole.getCmrFieldName();
        String fkCmrFieldName = isFkRoleVirtual ? "$VirtualEnd" + id : fkRole.getCmrFieldName();
        fkEjb.addCMRField(new CMRField(fkCmrFieldName, pkEjb, pkRole.isOne(), pkRole.isCascadeDelete(), relationship, isFkRoleVirtual, false));
        fkTable.addEndTable(new EndTable(fkCmrFieldName, pkTable, pkRole.isOne(), pkRole.isCascadeDelete(), joinTable, isFkRoleVirtual, false));
    }


    private void buildSchemaForJoin(ManyToManyRelationSchema manyToManyRelationSchema, int id) throws Exception {
        RoleSchema leftRole = manyToManyRelationSchema.getLeftRole();
        RoleSchema rightRole = manyToManyRelationSchema.getRightRole();

        // get the left ejb and table
        String leftEntityName = leftRole.getEjbName();
        EJB leftEjb = ejbSchema.getEJB(leftEntityName);
        Table leftTable = sqlSchema.getTable(leftEntityName);

        // get the right ejb and table
        String rightEntityName = rightRole.getEjbName();
        EJB rightEjb = ejbSchema.getEJB(rightEntityName);
        ;
        Table rightTable = sqlSchema.getTable(rightEntityName);
        ;

        // get the middle ejb and table
        String middleEjbName = manyToManyRelationSchema.getManyToManyTableName();
        EJB middleEjb = ejbSchema.getEJB(middleEjbName);
        if (null == middleEjb) {
            middleEjb = new EJB(middleEjbName, middleEjbName);
            ejbSchema.addEJB(middleEjb);
        }
        Table middleTable = sqlSchema.getTable(middleEjbName);
        if (null == middleTable) {
            middleTable = new Table(middleEjbName);
            sqlSchema.addTable(middleTable);
        }

        // map the fields and columns
        RelationMapping leftToMiddleRelationMapping = createRelationMapping(leftRole, leftEjb, leftTable, middleTable);
        RelationMapping rightToMiddleRelationMapping = createRelationMapping(rightRole, rightEjb, rightTable, middleTable);

        // create the ejb relation and sql join objects
        Relationship relationship = new Relationship(middleEjb,
                new Association.JoinDefinition(leftEjb, rightEjb, leftToMiddleRelationMapping.getEjbFieldMappings()),
                new Association.JoinDefinition(rightEjb, leftEjb, rightToMiddleRelationMapping.getEjbFieldMappings()));
        JoinTable joinTable = new JoinTable(middleTable,
                new Association.JoinDefinition(leftTable, rightTable, leftToMiddleRelationMapping.getColumnMappings()),
                new Association.JoinDefinition(rightTable, leftTable, rightToMiddleRelationMapping.getColumnMappings()));

        // add the left cmr field and column
        boolean isLeftRoleVirtual = null == leftRole.getCmrFieldName();
        String leftCmrFieldName = isLeftRoleVirtual ? "$VirtualEnd" + id : leftRole.getCmrFieldName();
        leftEjb.addCMRField(new CMRField(leftCmrFieldName, rightEjb, rightRole.isOne(), rightRole.isCascadeDelete(), relationship, isLeftRoleVirtual, true));
        leftTable.addEndTable(new EndTable(leftCmrFieldName, rightTable, rightRole.isOne(), rightRole.isCascadeDelete(), joinTable, isLeftRoleVirtual, true));

        // add the right cmr field and column
        boolean isRightRoleVirtual = null == rightRole.getCmrFieldName();
        String rightCmrFieldName = isRightRoleVirtual ? "$VirtualEnd" + id : rightRole.getCmrFieldName();
        rightEjb.addCMRField(new CMRField(rightCmrFieldName, leftEjb, leftRole.isOne(), leftRole.isCascadeDelete(), relationship, isRightRoleVirtual, true));
        rightTable.addEndTable(new EndTable(rightCmrFieldName, leftTable, leftRole.isOne(), leftRole.isCascadeDelete(), joinTable, isRightRoleVirtual, true));

        // middle to left cmr field and column
        Relationship middleToLeftRelationship = new Relationship(relationship.getLeftJoinDefinition());
        JoinTable middleToLeftJoinTable = new JoinTable(joinTable.getLeftJoinDefinition());
        middleEjb.addCMRField(new CMRField(leftCmrFieldName, leftEjb, true, false, middleToLeftRelationship, true, false));
        middleTable.addEndTable(new EndTable(leftCmrFieldName, leftTable, true, false, middleToLeftJoinTable, true, false));
        middleToLeftRelationship.addAssociationEnd(leftEjb.getAssociationEnd(leftCmrFieldName));
        middleToLeftJoinTable.addAssociationEnd(leftTable.getAssociationEnd(leftCmrFieldName));

        // middle to right cmr field and column
        Relationship middleToRightRelationship = new Relationship(relationship.getRightJoinDefinition());
        JoinTable middleToRightJoinTable = new JoinTable(joinTable.getRightJoinDefinition());
        middleEjb.addCMRField(new CMRField(rightCmrFieldName, rightEjb, true, false, middleToRightRelationship, true, false));
        middleTable.addEndTable(new EndTable(rightCmrFieldName, rightTable, true, false, middleToRightJoinTable, true, false));
        middleToRightRelationship.addAssociationEnd(rightEjb.getAssociationEnd(rightCmrFieldName));
        middleToRightJoinTable.addAssociationEnd(rightTable.getAssociationEnd(rightCmrFieldName));
    }

    private RelationMapping createRelationMapping(RoleSchema role, EJB ejb, Table table, Table relatedTable) throws Exception {
        // -- build the field and column mappings
        Map columnNameMapping = role.getPkMapping();
        LinkedHashMap ejbFieldMappings = new LinkedHashMap();
        LinkedHashMap columnMappings = new LinkedHashMap();
        for (Iterator iterator = table.getPrimaryKeyFields().iterator(); iterator.hasNext();) {
            Column column = (Column) iterator.next();

            // -- column names
            String columnName = column.getPhysicalName();
            String relatedColumnName = (String) columnNameMapping.get(columnName);
            if (null == relatedColumnName) {
                throw new Exception("Role " + role + " is misconfigured: primary key column [" +
                        columnName + "] is not mapped to a foreign key.");
            }

            // -- field Names
            String ejbFieldName = column.getName();
            String relatedEjbFieldName = relatedColumnName;
            for (Iterator iter = relatedTable.getAttributes().iterator(); iter.hasNext();) {
                Column relatedTableColumn = (Column) iter.next();
                if (relatedTableColumn.getPhysicalName().equals(relatedColumnName)) {
                    relatedEjbFieldName = relatedTableColumn.getName();
                    break;
                }
            }

            // -- create related ejb field
            FKField relatedEjbField = new FKField(relatedEjbFieldName, relatedColumnName, column.getType());
            ejbFieldMappings.put(ejb.getAttribute(ejbFieldName), relatedEjbField);


            // -- create related column
            FKColumn relatedcolumn = new FKColumn(relatedEjbFieldName, relatedColumnName, column.getType());
            if (column.isSQLTypeSet()) {
                relatedcolumn.setSQLType(column.getSQLType());
            }
            if (column.isTypeConverterSet()) {
                relatedcolumn.setTypeConverter(column.getTypeConverter());
            }
            columnMappings.put(column, relatedcolumn);
        }
        return new RelationMapping(ejbFieldMappings, columnMappings);
    }

    private void processGroups() throws Exception {
        PrefetchGroupDictionary groupDictionary = sqlSchema.getGroupDictionary();
        for (Iterator iterator = moduleSchema.getEntities().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String ejbName = (String) entry.getKey();
            EntitySchema entitySchema = (EntitySchema) entry.getValue();

            for (Iterator iterator1 = entitySchema.getPrefetchGroups().entrySet().iterator(); iterator1.hasNext();) {
                Map.Entry entry1 = (Map.Entry) iterator1.next();
                String groupName = (String) entry1.getKey();
                PrefetchGroup group = (PrefetchGroup) entry1.getValue();

                Set cmpFields = group.getCmpFields();
                String[] cmpFieldsArray = (String[]) cmpFields.toArray(new String[cmpFields.size()]);

                Map cmrFields = group.getCmrFields();
                PrefetchGroupDictionary.AssociationEndDesc[] endTableDescs = new PrefetchGroupDictionary.AssociationEndDesc[cmrFields.size()];
                int i = 0;
                for (Iterator iterator2 = cmrFields.entrySet().iterator(); iterator2.hasNext();) {
                    Map.Entry entry2 = (Map.Entry) iterator2.next();
                    String cmrFieldName = (String) entry2.getKey();
                    String cmrGroupName = (String) entry2.getValue();
                    endTableDescs[i++] = new PrefetchGroupDictionary.AssociationEndDesc(cmrFieldName, cmrGroupName);
                }
                groupDictionary.addPrefetchGroup(groupName, ejbName, cmpFieldsArray, endTableDescs);
            }

            EJB ejb = ejbSchema.getEJB(ejbName);

            String prefetchGroupName = entitySchema.getPrefetchGroupName();
            if (prefetchGroupName != null) {
                ejb.setPrefetchGroup(prefetchGroupName);
            }

            for (Iterator iterator1 = entitySchema.getCmpFields().entrySet().iterator(); iterator1.hasNext();) {
                Map.Entry entry1 = (Map.Entry) iterator1.next();
                String cmpFieldName = (String) entry1.getKey();
                CmpFieldSchema cmpFieldSchema = (CmpFieldSchema) entry1.getValue();

                String prefetchGroup = cmpFieldSchema.getPrefetchGroup();
                if (prefetchGroup != null) {
                    CMPField cmpField = (CMPField) ejb.getAttribute(cmpFieldName);
                    if (cmpField == null) {
                        throw new Exception("EJB [" + ejbName + "] does not define the CMP field [" + cmpFieldName + "].");
                    }
                    cmpField.setPrefetchGroup(prefetchGroup);
                }
            }

            for (Iterator iterator1 = entitySchema.getCmrPrefetchGroups().entrySet().iterator(); iterator1.hasNext();) {
                Map.Entry entry1 = (Map.Entry) iterator1.next();
                String cmrFieldName = (String) entry1.getKey();
                String prefetchGroup = (String) entry1.getValue();

                CMRField cmrField = (CMRField) ejb.getAssociationEnd(cmrFieldName);
                if (null == cmrField) {
                    throw new Exception("EJB [" + ejbName + "] does not define the CMR field [" + cmrFieldName + "].");
                }
                cmrField.setPrefetchGroup(prefetchGroup);
            }
        }
    }

    private void processEnterpriseBeanCaches() {
        for (Iterator iterator = moduleSchema.getEntities().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String ejbName = (String) entry.getKey();
            EntitySchema entitySchema = (EntitySchema) entry.getValue();

            String isolationLevel = entitySchema.getIsolationLevel();
            if (isolationLevel != null) {
                CacheFactory factory;
                int size = entitySchema.getCacheSize();
                if ("READ_COMMITTED".equals(isolationLevel)) {
                    factory = new ReadCommittedCacheFactory(size);
                } else if ("READ_UNCOMMITTED".equals(isolationLevel)) {
                    factory = new ReadUncommittedCacheFactory(size);
                } else if ("REPEATABLE_READ".equals(isolationLevel)) {
                    factory = new RepeatableReadCacheFactory(size);
                } else {
                    throw new AssertionError();
                }

                CacheTable cacheTable = globalSchema.getCacheTable(ejbName);
                cacheTable.setCacheFactory(factory);
            }
        }
    }

    private static class RelationMapping {
        private final LinkedHashMap ejbFieldMappings;
        private final LinkedHashMap columnMappings;

        private RelationMapping(LinkedHashMap ejbFieldMappings, LinkedHashMap columnMappings) {
            this.ejbFieldMappings = ejbFieldMappings;
            this.columnMappings = columnMappings;
        }

        public LinkedHashMap getEjbFieldMappings() {
            return ejbFieldMappings;
        }

        public LinkedHashMap getColumnMappings() {
            return columnMappings;
        }
    }

    protected EJBProxyFactory buildEJBProxyFactory(String containerId, String remoteInterfaceName, String homeInterfaceName, String localInterfaceName, String localHomeInterfaceName, ClassLoader cl) throws Exception {
        Class remoteInterface = loadClass(remoteInterfaceName, false, "remote interface");
        Class homeInterface = loadClass(homeInterfaceName, false, "home interface");
        Class localInterface = loadClass(localInterfaceName, false, "local interface");
        Class localHomeInterface = loadClass(localHomeInterfaceName, false, "local home interface");
        return new TranqlEjbProxyFactory(new org.apache.openejb.proxy.EJBProxyFactory(containerId,
                false,
                remoteInterface,
                homeInterface,
                localInterface,
                localHomeInterface));
    }

    public static class TranqlEjbProxyFactory implements EJBProxyFactory {
        private final org.apache.openejb.proxy.EJBProxyFactory ejbProxyFactory;

        public TranqlEjbProxyFactory(org.apache.openejb.proxy.EJBProxyFactory ejbProxyFactory) {
            this.ejbProxyFactory = ejbProxyFactory;
        }

        public Class getLocalInterfaceClass() {
            return ejbProxyFactory.getLocalInterfaceClass();
        }

        public Class getRemoteInterfaceClass() {
            return ejbProxyFactory.getRemoteInterfaceClass();
        }

        public EJBLocalObject getEJBLocalObject(Object primaryKey) {
            return ejbProxyFactory.getEJBLocalObject(primaryKey);
        }

        public EJBObject getEJBObject(Object primaryKey) {
            return ejbProxyFactory.getEJBObject(primaryKey);
        }
    }
    private Class loadClass(String name, boolean required, String descriptiveName) throws Exception {
        if (name == null) {
            if (required) {
                throw new Exception(descriptiveName + " is null");
            }
            return null;
        }
        try {
            return ClassLoading.loadClass(name, classLoader);
        } catch (ClassNotFoundException e) {
            throw new Exception("Unable to load " + descriptiveName + ": " + name);
        }
    }

    private Class[] loadClasses(String[] names, String methodName) throws Exception {
        Class[] classes = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            Class clazz = loadClass(name, true, "query method " + methodName + " parameter " + i);
            classes[i] = clazz;
        }
        return classes;
    }
}
