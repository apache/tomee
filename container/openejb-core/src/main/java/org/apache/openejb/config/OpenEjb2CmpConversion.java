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
package org.apache.openejb.config;

import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import org.apache.openejb.jee.oejb2.EjbRelationType;
import org.apache.openejb.jee.oejb2.EnterpriseBean;
import org.apache.openejb.jee.oejb2.EntityBeanType;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.EjbRelationshipRoleType;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.Table;
import org.apache.openejb.jee.jpa.Field;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.JoinColumn;
import org.apache.openejb.jee.jpa.JoinTable;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Attributes;

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
                Field field = entityData.ids.get(cmpFieldName);
                if (field == null) {
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

        for (EjbRelationType relation : openejbJarType.getEjbRelation()) {
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
                    if (field == null) {
                        continue;
                    }
                } else {
                    RelationField other = leftEntityData.relations.get(leftFieldName);
                    // todo warn field not found
                    if (other == null) {
                        continue;
                    }
                    field = other.getRelatedField();
                    // todo warn field not found
                    if (field == null) {
                        if (other instanceof OneToMany) {
                            // for a unidirectional oneToMany, the join column declaration
                            // is placed on the oneToMany element instead of manyToOne
                            field = other;
                        } else {
                            continue;
                        }
                    }
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
                JoinTable joinTable = new JoinTable();
                joinTable.setName(relation.getManyToManyTableName());

                //
                // left
                EjbRelationshipRoleType leftRole = roles.get(0);
                RelationField left = null;
                if (leftRole.getRelationshipRoleSource() != null) {
                    String leftEjbName = leftRole.getRelationshipRoleSource().getEjbName();
                    EntityData leftEntityData = entities.get(leftEjbName);
                    if (leftEntityData == null) {
                        // todo warn no such entity in ejb-jar.xml
                        continue;
                    }
                    left = leftEntityData.relations.get(leftRole.getCmrField().getCmrFieldName());
                }

                if (left != null) {
                    left.setJoinTable(joinTable);

                    EjbRelationshipRoleType.RoleMapping roleMapping = leftRole.getRoleMapping();
                    for (EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                        joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                        joinTable.getJoinColumn().add(joinColumn);
                    }
                }

                //
                // right
                if (roles.size() > 1) {
                    EjbRelationshipRoleType rightRole = roles.get(1);

                    // if there wasn't a left cmr field, find the field for the right, so we can add the join table to it
                    if (left == null) {
                        RelationField right = left.getRelatedField();
                        if (right == null) {
                            if (rightRole.getCmrField() == null) {
                                // todo warn no cmr field declared for either role
                                continue;
                            }
                            if (rightRole.getRelationshipRoleSource() != null) {
                                String rightEjbName = rightRole.getRelationshipRoleSource().getEjbName();
                                EntityData rightEntityData = entities.get(rightEjbName);
                                if (rightEntityData == null) {
                                    // todo warn no such entity in ejb-jar.xml
                                    continue;
                                }
                                right = rightEntityData.relations.get(rightRole.getCmrField().getCmrFieldName());
                            }
                        }
                        right.setJoinTable(joinTable);
                    }

                    EjbRelationshipRoleType.RoleMapping roleMapping = rightRole.getRoleMapping();
                    for (EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                        joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                        joinTable.getInverseJoinColumn().add(joinColumn);
                    }
                }
            }
        }
    }

    private class EntityData {
        private final Entity entity;
        private final Map<String, Id> ids = new TreeMap<String, Id>();
        private final Map<String, Basic> fields = new TreeMap<String, Basic>();
        private final Map<String, RelationField> relations = new TreeMap<String, RelationField>();

        public EntityData(Entity entity) {
            this.entity = entity;

            for (Id id : entity.getAttributes().getId()) {
                ids.put(id.getName(), id);
            }

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
