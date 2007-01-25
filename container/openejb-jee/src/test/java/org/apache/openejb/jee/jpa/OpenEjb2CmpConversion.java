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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee.jpa;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import org.apache.openejb.jee.oej2.EjbRelationType;
import org.apache.openejb.jee.oej2.EnterpriseBean;
import org.apache.openejb.jee.oej2.EntityBeanType;
import org.apache.openejb.jee.oej2.OpenejbJarType;
import org.apache.openejb.jee.oej2.RelationshipsType;
import org.apache.openejb.jee.oej2.EjbRelationshipRoleType;

public class OpenEjb2CmpConversion {

    public void mergeEntityMappings(EntityMappings entityMappings, OpenejbJarType openejbJarType) {
        Map<String, EntityData> entities =  new TreeMap<String, EntityData>();
        for (Entity entity : entityMappings.getEntity()) {
            entities.put(entity.getDescription(), new EntityData(entity));
        }
        for (EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof EntityBeanType)) {
                continue;
            }
            EntityBeanType bean = (EntityBeanType) enterpriseBean;
            EntityData entityData = entities.get(bean.getEjbName());
            if (entityData == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Table table = new Table();
            table.setName(bean.getTableName());
            entityData.entity.setTable(table);

            for (EntityBeanType.CmpFieldMapping cmpFieldMapping : bean.getCmpFieldMapping()) {
                String cmpFieldName = cmpFieldMapping.getCmpFieldName();
                Field field;
                if (cmpFieldName.equals(entityData.id.getName())) {
                    field = entityData.id;
                } else {
                    field = entityData.fields.get(cmpFieldName);
                }

                if (field == null) {
                    // todo warn no such cmp-field in the ejb-jar.xml
                    continue;
                }
                Column column = new Column();
                column.setName(cmpFieldMapping.getTableColumn());
                field.setColumn(column);
            }

            // todo this doesn't seem to parse?
            if (bean.getKeyGenerator() != null) {
                Id id = entityData.entity.getAttributes().getId().get(0);

                // todo detect specific generation strategy
                GeneratedValue generatedValue = new GeneratedValue();
                generatedValue.setGenerator("IDENTITY");
                id.setGeneratedValue(generatedValue);
            }
        }

        RelationshipsType relationships = openejbJarType.getRelationships();
        if (relationships != null) {
            // todo simplify
            for (EjbRelationType relation : relationships.getEjbRelation()) {
                List<EjbRelationshipRoleType> roles = relation.getEjbRelationshipRole();
                if (roles.isEmpty()) {
                    continue;
                }

                if (relation.getManyToManyTableName() == null) {
                    EjbRelationshipRoleType leftRole = roles.get(0);
                    EjbRelationshipRoleType.RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
                    String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
                    EntityData leftEntityData = entities.get(leftEjbName);
                    String leftFieldName = leftRole.getCmrField().getCmrFieldName();

                    RelationField field;
                    if (leftRole.isForeignKeyColumnOnSource()) {
                        field = leftEntityData.relations.get(leftFieldName);
                        // todo warn field not found
                        if (field == null) continue;
                    } else {
                        RelationField other = leftEntityData.relations.get(leftFieldName);
                        // todo warn field not found
                        if (other == null) continue;
                        field = other.getRelatedField();
                        // todo warn field not found
                        if (field == null) continue;
                    }

                    // For one-to-one, make sure that the field to recieve the FK
                    // is marked as the owning field
                    if (field instanceof OneToOne) {
                        OneToOne left = (OneToOne) field;
                        OneToOne right = (OneToOne) left.getRelatedField();
                        if (right != null) {
                            left.setMappedBy(null);
                            right.setMappedBy(left.getName());
                        }

                    }
                    EjbRelationshipRoleType.RoleMapping roleMapping = leftRole.getRoleMapping();
                    for (EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                        joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                        field.getJoinColumn().add(joinColumn);
                    }
                } else {
                    // todo support many-to-many
                }
            }
        }
    }

    private class EntityData {
        public final Entity entity;
        private final Id id;
        public final Map<String, Basic> fields = new TreeMap<String, Basic>();
        public final Map<String, RelationField> relations = new TreeMap<String, RelationField>();

        public EntityData(Entity entity) {
            this.entity = entity;

            id = entity.getAttributes().getId().get(0);

            Attributes attributes = entity.getAttributes();
            if (attributes == null) {
                return;
            }

            for (Basic basic : attributes.getBasic()) {
                String name = basic.getName();
                fields.put(name, basic);
            }

            for (RelationField relationField : attributes.getOneToOne()) {
                String name = relationField.getName();
                relations.put(name, relationField);
            }

            for (RelationField relationField : attributes.getOneToMany()) {
                String name = relationField.getName();
                relations.put(name, relationField);
            }

            for (RelationField relationField : attributes.getManyToOne()) {
                String name = relationField.getName();
                relations.put(name, relationField);
            }

            for (RelationField relationField : attributes.getManyToMany()) {
                String name = relationField.getName();
                relations.put(name, relationField);
            }
        }
    }
}
