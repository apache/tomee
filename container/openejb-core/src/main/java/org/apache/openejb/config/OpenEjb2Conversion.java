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

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.JAXBElement;

import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.Column;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Field;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.JoinColumn;
import org.apache.openejb.jee.jpa.JoinTable;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.Table;
import org.apache.openejb.jee.jpa.AttributeOverride;
import org.apache.openejb.jee.oejb2.EjbRelationType;
import org.apache.openejb.jee.oejb2.EjbRelationshipRoleType;
import org.apache.openejb.jee.oejb2.EntityBeanType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.QueryType;
import org.apache.openejb.jee.oejb2.MessageDrivenBeanType;
import org.apache.openejb.jee.oejb2.ActivationConfigType;
import org.apache.openejb.jee.oejb2.ActivationConfigPropertyType;
import org.apache.openejb.jee.oejb2.EjbRefType;
import org.apache.openejb.jee.oejb2.PatternType;
import org.apache.openejb.jee.oejb2.EjbLocalRefType;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.EjbRef;

public class OpenEjb2Conversion implements DynamicDeployer {
    public AppModule deploy(AppModule appModule) {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            Object altDD = getOpenejbJarType(ejbModule);
            if (altDD instanceof OpenejbJarType) {
                convertEjbRefs(ejbModule.getEjbJar(), ejbModule.getOpenejbJar(), (OpenejbJarType) altDD);
                convertMdbConfigs(ejbModule.getEjbJar(), (OpenejbJarType) altDD);
                mergeEntityMappings(ejbModule.getModuleId(), appModule.getCmpMappings(), ejbModule.getOpenejbJar(), (OpenejbJarType) altDD);
            }
        }
        return appModule;
    }

    private OpenejbJarType getOpenejbJarType(EjbModule ejbModule) {
        Object altDD = ejbModule.getAltDDs().get("openejb-jar.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, new ByteArrayInputStream(((String)altDD).getBytes()), false);
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, ((URL)altDD).openStream(), false);
            } catch (Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof JAXBElement) {
            altDD = ((JAXBElement)altDD).getValue();
        }
        if (altDD instanceof OpenejbJarType) {
            return (OpenejbJarType) altDD;
        }
        return null;
    }

    public void convertEjbRefs(EjbJar ejbJar, OpenejbJar openejbJar, OpenejbJarType openejbJarType) {
        Map<String, EnterpriseBean> ejbs = ejbJar.getEnterpriseBeansByEjbName();
        Map<String, EjbDeployment> deployments =  openejbJar.getDeploymentsByEjbName();

        for (org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            EnterpriseBean ejb = ejbs.get(enterpriseBean.getEjbName());
            if (ejb == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }
            Map<String,EjbRef> ejbRefs =  ejb.getEjbRefMap();

            EjbDeployment deployment = deployments.get(enterpriseBean.getEjbName());
            if (deployment == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Set<String> ejbLinks =  new TreeSet<String>();
            for (EjbLink ejbLink : deployment.getEjbLink()) {
                ejbLinks.add(ejbLink.getEjbRefName());
            }

            for (EjbRefType refType : enterpriseBean.getEjbRef()) {
                String refName = refType.getRefName();
                if (ejbLinks.contains(refName)) {
                    // don't overwrite refs that have been already set
                    continue;
                }

                String nsCorbaloc = refType.getNsCorbaloc();
                if (nsCorbaloc != null) {
                    EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setMappedName("jndi:" + nsCorbaloc);
                    }
                } else if (refType.getEjbLink() != null) {
                    EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setEjbLink(refType.getEjbLink());
                    }
                } else {
                    PatternType pattern = refType.getPattern();
                    addEjbLink(deployment, refName, pattern);
                }
            }

            for (EjbLocalRefType refType : enterpriseBean.getEjbLocalRef()) {
                String refName = refType.getRefName();
                if (ejbLinks.contains(refName)) {
                    // don't overwrite refs that have been already set
                    continue;
                }

                if (refType.getEjbLink() != null) {
                    EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setEjbLink(refType.getEjbLink());
                    }
                } else {
                    PatternType pattern = refType.getPattern();
                    addEjbLink(deployment, refName, pattern);
                }
            }
        }
    }

    private void addEjbLink(EjbDeployment deployment, String refName, PatternType pattern) {
        String module = pattern.getModule();
        if (module == null) {
            module = pattern.getArtifactId();
        }
        String ejbName = pattern.getName();
        String deploymentId = module + "/" + ejbName;
        EjbLink ejbLink = new EjbLink(refName, deploymentId);
        deployment.getEjbLink().add(ejbLink);
    }

    public void convertMdbConfigs(EjbJar ejbJar, OpenejbJarType openejbJarType) {
        Map<String, MessageDrivenBean> mdbs =  new TreeMap<String, MessageDrivenBean>();
        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof MessageDrivenBean)) {
                continue;
            }
            mdbs.put(enterpriseBean.getEjbName(), (MessageDrivenBean) enterpriseBean);
        }
        for (org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof MessageDrivenBeanType)) {
                continue;
            }
            MessageDrivenBeanType bean = (MessageDrivenBeanType) enterpriseBean;
            MessageDrivenBean mdb = mdbs.get(bean.getEjbName());
            if (mdb == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }
            ActivationConfigType activationConfigType = bean.getActivationConfig();
            if (activationConfigType != null) {
                ActivationConfig activationConfig = mdb.getActivationConfig();
                if (activationConfig == null) {
                    activationConfig = new ActivationConfig();
                    mdb.setActivationConfig(activationConfig);
                }
                for (ActivationConfigPropertyType propertyType : activationConfigType.getActivationConfigProperty()) {
                    ActivationConfigProperty property = new ActivationConfigProperty(
                            propertyType.getActivationConfigPropertyName(),
                            propertyType.getActivationConfigPropertyValue());
                    activationConfig.getActivationConfigProperty().add(property);
                }
            }
        }
    }

    public void mergeEntityMappings(String moduleId, EntityMappings entityMappings, OpenejbJar openejbJar, OpenejbJarType openejbJarType) {
        Map<String, EntityData> entities =  new TreeMap<String, EntityData>();
        for (Entity entity : entityMappings.getEntity()) {
            entities.put(entity.getDescription(), new EntityData(entity));
        }
        for (org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof EntityBeanType)) {
                continue;
            }
            EntityBeanType bean = (EntityBeanType) enterpriseBean;
            EntityData entityData = entities.get(moduleId + "#" + bean.getEjbName());
            if (entityData == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            Table table = new Table();
            table.setName(bean.getTableName());
            entityData.entity.setTable(table);

            for (EntityBeanType.CmpFieldMapping cmpFieldMapping : bean.getCmpFieldMapping()) {
                String cmpFieldName = cmpFieldMapping.getCmpFieldName();
                Field field = entityData.fields.get(cmpFieldName);
                if (field == null) {
                    // todo warn no such cmp-field in the ejb-jar.xml
                    continue;
                }
                Column column = new Column();
                column.setName(cmpFieldMapping.getTableColumn());
                field.setColumn(column);
            }

            if (bean.getKeyGenerator() != null) {
                // todo support complex primary keys
                Attributes attributes = entityData.entity.getAttributes();
                if (attributes != null && attributes.getId().size() == 1) {
                    Id id = attributes.getId().get(0);

                    // todo detect specific generation strategy
                    id.setGeneratedValue(new GeneratedValue(GenerationType.IDENTITY));
                }
            }

            for (QueryType query : bean.getQuery()) {
                NamedQuery namedQuery = new NamedQuery();
                QueryType.QueryMethod queryMethod = query.getQueryMethod();

                // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                StringBuilder name = new StringBuilder();
                name.append(entityData.entity.getName()).append(".").append(queryMethod.getMethodName());
                if (queryMethod.getMethodParams() != null && !queryMethod.getMethodParams().getMethodParam().isEmpty()) {
                    name.append('(');
                    boolean first = true;
                    for (String methodParam : queryMethod.getMethodParams().getMethodParam()) {
                        if (!first) name.append(",");
                        name.append(methodParam);
                        first = false;
                    }
                    name.append(')');
                }
                namedQuery.setName(name.toString());

                namedQuery.setQuery(query.getEjbQl());
                entityData.entity.getNamedQuery().add(namedQuery);
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
                EntityData leftEntityData = entities.get(moduleId + "#" + leftEjbName);
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
                    EntityData leftEntityData = entities.get(moduleId + "#" + leftEjbName);
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
                                EntityData rightEntityData = entities.get(moduleId + "#" + rightEjbName);
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
        private final Map<String, Field> fields = new TreeMap<String, Field>();
        private final Map<String, RelationField> relations = new TreeMap<String, RelationField>();

        public EntityData(Entity entity) {
            if (entity == null) throw new NullPointerException("entity is null");
            this.entity = entity;

            Attributes attributes = entity.getAttributes();
            if (attributes != null) {
                for (Id id : attributes.getId()) {
                    String name = id.getName();
                    fields.put(name, id);
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

            for (AttributeOverride attributeOverride : entity.getAttributeOverride()) {
                String name = attributeOverride.getName();
                fields.put(name, attributeOverride);
            }
        }
    }
}
