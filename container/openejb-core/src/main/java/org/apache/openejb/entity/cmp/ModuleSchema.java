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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ModuleSchema implements Serializable {
    private static final long serialVersionUID = 6744154273253141071L;
    private final String name;
    private boolean enforceForeignKeyConstraints;
    private String ejbQlCompilerFactory;
    private String dbSyntaxFactory;
    private final Map entities = new TreeMap();
    private final Set relations = new LinkedHashSet();

    public ModuleSchema(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnforceForeignKeyConstraints() {
        return enforceForeignKeyConstraints;
    }

    public void setEnforceForeignKeyConstraints(boolean enforceForeignKeyConstraints) {
        this.enforceForeignKeyConstraints = enforceForeignKeyConstraints;
    }

    public String getEjbQlCompilerFactory() {
        return ejbQlCompilerFactory;
    }

    public void setEjbQlCompilerFactory(String ejbQlCompilerFactory) {
        this.ejbQlCompilerFactory = ejbQlCompilerFactory;
    }

    public String getDbSyntaxFactory() {
        return dbSyntaxFactory;
    }

    public void setDbSyntaxFactory(String dbSyntaxFactory) {
        this.dbSyntaxFactory = dbSyntaxFactory;
    }

    public EntitySchema addEntity(String ejbName) {
        if (entities.containsKey(ejbName)) {
            throw new IllegalStateException("Entity " + ejbName + " already exists");
        }
        EntitySchema entitySchema = new EntitySchema(ejbName);
        entities.put(ejbName, entitySchema);
        return entitySchema;
    }

    public void addEntity(EntitySchema entitySchema) {
        if (entities.containsKey(entitySchema.getEjbName())) {
            throw new IllegalStateException("Entity " + entitySchema.getEjbName() + " already exists");
        }
        entities.put(entitySchema.getEjbName(), entitySchema);
    }

    public EntitySchema getEntity(String ejbName) {
        return (EntitySchema) entities.get(ejbName);
    }

    public Map getEntities() {
        return Collections.unmodifiableMap(entities);
    }

    public void setEntities(Collection entities) {
        this.entities.clear();
        for (Iterator iterator = entities.iterator(); iterator.hasNext();) {
            EntitySchema entitySchema = (EntitySchema) iterator.next();
            addEntity(entitySchema);
        }
    }


    public void setEntities(Map entities) {
        this.entities.clear();
        for (Iterator iterator = entities.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String ejbName = (String) entry.getKey();
            EntitySchema entitySchema = (EntitySchema) entry.getValue();
            this.entities.put(ejbName, entitySchema);
        }
    }

    public void addRelation(RelationSchema relationSchema) {
        // todo consider adding cmr fields to the entity schema object
        this.relations.add(relationSchema);
    }

    public Set getRelations() {
        return Collections.unmodifiableSet(relations);
    }

    public void setRelations(Set relations) {
        this.relations.clear();
        for (Iterator iterator = relations.iterator(); iterator.hasNext();) {
            RelationSchema relationSchema = (RelationSchema) iterator.next();
            addRelation(relationSchema);
        }
    }

}