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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.io.Serializable;

import org.apache.openejb.dispatch.MethodSignature;

public class EntitySchema implements Serializable {
    private static final long serialVersionUID = -8231327229379986149L;
    private final String ejbName;
    private String containerId;
    private boolean cmp2;
    private String abstractSchemaName;
    private String tableName;
    private String remoteInterfaceName;
    private String homeInterfaceName;
    private String localInterfaceName;
    private String localHomeInterfaceName;
    private String ejbClassName;
    private boolean unknownPk;
    private String pkClassName;
    private String pkFieldName;
    private boolean staticSql;
    private PrimaryKeyGenerator primaryKeyGenerator;
    private int cacheSize;
    private String isolationLevel;
    private final Map cmpFields = new TreeMap();
    private final Map queries = new TreeMap();
    private final Map prefetchGroups = new TreeMap();
    private String prefetchGroupName;
    private final Map cmrPrefetchGroups = new TreeMap();

    public EntitySchema(String ejbName) {
        this.ejbName = ejbName;
    }

    public String getEjbName() {
        return ejbName;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public boolean isCmp2() {
        return cmp2;
    }

    public void setCmp2(boolean cmp2) {
        this.cmp2 = cmp2;
    }

    public String getAbstractSchemaName() {
        return abstractSchemaName;
    }

    public void setAbstractSchemaName(String abstractSchemaName) {
        this.abstractSchemaName = abstractSchemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String remoteInterfaceName) {
        this.remoteInterfaceName = remoteInterfaceName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public void setHomeInterfaceName(String homeInterfaceName) {
        this.homeInterfaceName = homeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public void setLocalInterfaceName(String localInterfaceName) {
        this.localInterfaceName = localInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public void setLocalHomeInterfaceName(String localHomeInterfaceName) {
        this.localHomeInterfaceName = localHomeInterfaceName;
    }

    public String getEjbClassName() {
        return ejbClassName;
    }

    public void setEjbClassName(String ejbClassName) {
        this.ejbClassName = ejbClassName;
    }

    public boolean isUnknownPk() {
        return unknownPk;
    }

    public void setUnknownPk(boolean unknownPk) {
        this.unknownPk = unknownPk;
    }

    public String getPkClassName() {
        return pkClassName;
    }

    public void setPkClassName(String pkClassName) {
        this.pkClassName = pkClassName;
    }

    public String getPkFieldName() {
        return pkFieldName;
    }

    public void setPkFieldName(String pkFieldName) {
        this.pkFieldName = pkFieldName;
    }

    public boolean isStaticSql() {
        return staticSql;
    }

    public void setStaticSql(boolean staticSql) {
        this.staticSql = staticSql;
    }

    public PrimaryKeyGenerator getPrimaryKeyGenerator() {
        return primaryKeyGenerator;
    }

    public void setPrimaryKeyGenerator(PrimaryKeyGenerator primaryKeyGenerator) {
        this.primaryKeyGenerator = primaryKeyGenerator;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(String isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public CmpFieldSchema addCmpField(String cmpFieldName, String columnName, Class type, boolean pkField) {
        if (cmpFields.containsKey(cmpFieldName)) {
            throw new IllegalStateException("Cmp field " + cmpFieldName + " already exists on entitiy " + ejbName);
        }
        CmpFieldSchema cmpFieldSchema = new CmpFieldSchema(cmpFieldName);
        cmpFieldSchema.setFieldTypeName(type.getName());
        cmpFieldSchema.setColumnName(columnName);
        cmpFieldSchema.setPkField(pkField);
        cmpFields.put(cmpFieldName, cmpFieldSchema);
        return cmpFieldSchema;
    }

    public CmpFieldSchema addCmpField(String cmpFieldName) {
        if (cmpFields.containsKey(cmpFieldName)) {
            throw new IllegalStateException("Cmp field " + cmpFieldName + " already exists on entitiy " + ejbName);
        }
        CmpFieldSchema cmpFieldSchema = new CmpFieldSchema(cmpFieldName);
        cmpFields.put(cmpFieldName, cmpFieldSchema);
        return cmpFieldSchema;
    }

    public CmpFieldSchema getCmpField(String fieldName) {
        return (CmpFieldSchema) cmpFields.get(fieldName);
    }

    public Map getCmpFields() {
        return Collections.unmodifiableMap(cmpFields);
    }

    public void setCmpFields(Map cmpFields) {
        this.cmpFields.clear();
        for (Iterator iterator = cmpFields.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String cmpFieldName = (String) entry.getKey();
            CmpFieldSchema cmpFieldSchema = (CmpFieldSchema) entry.getValue();
            this.cmpFields.put(cmpFieldName, cmpFieldSchema);
        }
    }

    public QuerySpec addQuery(MethodSignature methodSignature) {
        if (queries.containsKey(methodSignature)) {
            throw new IllegalStateException("Query for '" + methodSignature + "' already exists on entitiy " + ejbName);
        }
        QuerySpec querySchema = new QuerySpec(methodSignature);
        queries.put(methodSignature, querySchema);
        return querySchema;
    }

    public QuerySpec getQuery(MethodSignature methodSignature) {
        return (QuerySpec) queries.get(methodSignature);
    }

    public Map getQueries() {
        return Collections.unmodifiableMap(queries);
    }

    public void setQueries(Map queries) {
        this.queries.clear();
        for (Iterator iterator = queries.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            MethodSignature methodSignature = (MethodSignature) entry.getKey();
            QuerySpec querySchema = (QuerySpec) entry.getValue();
            this.queries.put(methodSignature, querySchema);
        }
    }

    public PrefetchGroup addPrefetchGroup(String groupName) {
        if (prefetchGroups.containsKey(groupName)) {
            throw new IllegalStateException("PrefetchGroup '" + groupName + "' already exists on entitiy " + ejbName);
        }
        PrefetchGroup prefetchGroup = new PrefetchGroup(groupName);
        prefetchGroups.put(groupName, prefetchGroup);
        return prefetchGroup;
    }

    public PrefetchGroup getPrefetchGroup(String groupName) {
        return (PrefetchGroup) prefetchGroups.get(groupName);
    }

    public Map getPrefetchGroups() {
        return Collections.unmodifiableMap(prefetchGroups);
    }

    public void setPrefetchGroups(Map prefetchGroups) {
        this.prefetchGroups.clear();
        for (Iterator iterator = prefetchGroups.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String groupName = (String) entry.getKey();
            PrefetchGroup prefetchGroup = (PrefetchGroup) entry.getValue();
            this.prefetchGroups.put(groupName, prefetchGroup);
        }
    }

    public String getPrefetchGroupName() {
        return prefetchGroupName;
    }

    public void setPrefetchGroupName(String prefetchGroupName) {
        this.prefetchGroupName = prefetchGroupName;
    }

    public String getCmrPrefetchGroup(String cmrFieldName) {
        return (String) cmrPrefetchGroups.get(cmrFieldName);
    }

    public void setCmrPrefetchGroup(String cmrFieldName, String groupName) {
        cmrPrefetchGroups.put(cmrFieldName, groupName);
    }

    public Map getCmrPrefetchGroups() {
        return Collections.unmodifiableMap(cmrPrefetchGroups);
    }

    public void setCmrPrefetchGroups(Map prefetchGroups) {
        this.cmrPrefetchGroups.clear();
        for (Iterator iterator = prefetchGroups.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String cmrFieldName = (String) entry.getKey();
            String groupName = (String) entry.getValue();
            setCmrPrefetchGroup(cmrFieldName, groupName);
        }
    }

    public int hashCode() {
        return ejbName.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof EntitySchema) {
            EntitySchema entitySchema = (EntitySchema) obj;
            return ejbName.equals(entitySchema.ejbName);
        }
        return false;
    }

    public String toString() {
        return ejbName;
    }
}