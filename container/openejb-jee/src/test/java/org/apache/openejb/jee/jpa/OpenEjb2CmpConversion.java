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
        Map<String, Entity> entities = createEntityMappings(entityMappings);
        for (EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof EntityBeanType)) {
                continue;
            }
            EntityBeanType bean = (EntityBeanType) enterpriseBean;
            Entity entity = entities.get(bean.getEjbName());
            if (entity == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Map<String, Basic> fields = createFieldMappings(entity);
            for (EntityBeanType.CmpFieldMapping cmpFieldMapping : bean.getCmpFieldMapping()) {
                String cmpFieldName = cmpFieldMapping.getCmpFieldName();
                Basic field = fields.get(cmpFieldName);
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
                Id id = entity.getAttributes().getId().get(0);

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

                EjbRelationshipRoleType leftRole = roles.get(0);
                EjbRelationshipRoleType.RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
                String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
                Entity leftEntity = entities.get(leftEjbName);
                EjbRelationshipRoleType.CmrField leftCmrField = leftRole.getCmrField();
                String leftFieldName = leftCmrField.getCmrFieldName();

                if (leftRole.isForeignKeyColumnOnSource()) {
                    EjbRelationshipRoleType.RoleMapping roleMapping = leftRole.getRoleMapping();
                    for (EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        JoinColumn joinColumn = new JoinColumn();
                        String keyColumn = cmrFieldMapping.getKeyColumn();
                        String foreignKeyColumn = cmrFieldMapping.getForeignKeyColumn();
                    }
                }

                if (roles.size() > 1) {
                    EjbRelationshipRoleType rightRole = roles.get(1);
                    EjbRelationshipRoleType.RelationshipRoleSource rightRoleSource = rightRole.getRelationshipRoleSource();
                    String rightEjbName = rightRoleSource == null ? null : rightRoleSource.getEjbName();
                    Entity rightEntity = entities.get(rightEjbName);
                    EjbRelationshipRoleType.CmrField  rightCmrField = rightRole.getCmrField();
                    String rightFieldName = rightCmrField.getCmrFieldName();

//                    boolean rightCascade = rightRole.getCascadeDelete() != null;
//                    boolean rightIsOne = rightRole.getMultiplicity() == Multiplicity.ONE;
                }

            }
        }
    }

    private Map<String, Entity> createEntityMappings(EntityMappings entityMappings) {
        Map<String, Entity> entities = new TreeMap<String, Entity>();
        for (Entity entity : entityMappings.getEntity()) {
            // raw ejb name is stored in the description field
            String ejbName = entity.getDescription();
            entities.put(ejbName, entity);
        }
        return entities;
    }

    private Map<String, Basic> createFieldMappings(Entity entity) {
        Map<String, Basic> fields = new TreeMap<String, Basic>();
        if (entity.getAttributes() != null) {
            for (Basic basic : entity.getAttributes().getBasic()) {
                String name = basic.getName();
                fields.put(name, basic);
            }
        }
        return fields;
    }
}
