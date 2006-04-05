/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.entity.cmp;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.io.Serializable;

import org.openejb.dispatch.MethodSignature;

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