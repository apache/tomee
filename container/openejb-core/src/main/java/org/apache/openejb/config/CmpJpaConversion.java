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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.cmp.jpa.JpaCmpEngine;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.RelationshipRoleSource;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.Query;
import org.apache.openejb.jee.QueryMethod;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.CascadeType;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.ManyToMany;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.Transient;
import org.apache.openejb.jee.jpa.MappedSuperclass;
import org.apache.openejb.jee.jpa.Mapping;
import org.apache.openejb.jee.jpa.AttributeOverride;
import org.apache.openejb.jee.jpa.Field;
import org.apache.openejb.jee.jpa.NamedQuery;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.jee.jpa.unit.Properties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class CmpJpaConversion implements DynamicDeployer {
    private static final String CMP_PERSISTENCE_UNIT_NAME = "cmp";

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        // search for the cmp persistence unit
        PersistenceUnit persistenceUnit = null;
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            Persistence persistence = persistenceModule.getPersistence();
            for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                if (CMP_PERSISTENCE_UNIT_NAME.equals(unit.getName())) {
                    persistenceUnit = unit;
                    break;
                }

            }
        }

        // todo scan existing persistence module for all entity mappings and don't generate mappings for them

        // create mappings
        EntityMappings cmpMappings = appModule.getCmpMappings();
        if (cmpMappings == null) {
            cmpMappings = new EntityMappings();
            cmpMappings.setVersion("1.0");
            appModule.setCmpMappings(cmpMappings);
        }

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            generateEntityMappings(ejbModule, cmpMappings);
        }

        if (!cmpMappings.getEntity().isEmpty()) {
            // if not found create one
            if (persistenceUnit == null) {
                persistenceUnit = new PersistenceUnit();
                persistenceUnit.setName(CMP_PERSISTENCE_UNIT_NAME);
                persistenceUnit.setTransactionType(TransactionType.JTA);
                persistenceUnit.setJtaDataSource("java:openejb/Connector/Default JDBC Database");
                persistenceUnit.setNonJtaDataSource("java:openejb/Connector/Default Unmanaged JDBC Database");
                // todo paramterize this
                Properties properties = new Properties();
                 properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
                // properties.setProperty("openjpa.DataCache", "false");
                // properties.setProperty("openjpa.Log", "DefaultLevel=TRACE");
                persistenceUnit.setProperties(properties);

                Persistence persistence = new Persistence();
                persistence.setVersion("1.0");
                persistence.getPersistenceUnit().add(persistenceUnit);

                PersistenceModule persistenceModule = new PersistenceModule(appModule.getModuleId(), persistence);
                appModule.getPersistenceModules().add(persistenceModule);
            }
            persistenceUnit.getMappingFile().add("META-INF/openejb-cmp-generated-orm.xml");
            for (Entity entity : cmpMappings.getEntity()) {
                persistenceUnit.getClazz().add(entity.getClazz());
            }
        }

        return appModule;
    }

    public EntityMappings generateEntityMappings(EjbModule ejbModule) throws OpenEJBException {
        AppModule appModule = new AppModule(ejbModule.getClassLoader(), ejbModule.getJarLocation());
        appModule.getEjbModules().add(ejbModule);

        EntityMappings entityMappings = new EntityMappings();
        generateEntityMappings(ejbModule, entityMappings);
        return entityMappings;
    }

    public void generateEntityMappings(EjbModule ejbModule, EntityMappings entityMappings) throws OpenEJBException {
        EjbJar ejbJar = ejbModule.getEjbJar();
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();
        ClassLoader classLoader = ejbModule.getClassLoader();

        Map<String, Entity> entitiesByName = new HashMap<String,Entity>();
        for (org.apache.openejb.jee.EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            // skip all non-CMP beans
            if (!(enterpriseBean instanceof EntityBean) ||
                    ((EntityBean) enterpriseBean).getPersistenceType() != PersistenceType.CONTAINER) {
                continue;
            }
            EntityBean bean = (EntityBean) enterpriseBean;
            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null) {
                throw new OpenEJBException("No deploument info found for ejb " + bean.getEjbName());
            }

            // Always set the abstract schema name
            if (bean.getAbstractSchemaName() == null) {
                String abstractSchemaName = bean.getEjbName().trim().replaceAll("[ \\t\\n\\r-]+", "_");
                bean.setAbstractSchemaName(abstractSchemaName);
            }

            // try to add a new persistence-context-ref for cmp
            if (!addPersistenceContextRef(bean)) {
                // Bean already has a persistence-context-ref for cmp
                // which means it has a mapping, so skip this bean
                continue;
            }

            Entity entity = new Entity();

            // description: contains the name of the entity bean
            entity.setDescription(bean.getEjbName());

            // name: the name of the entity in queries
            String entityName = bean.getAbstractSchemaName();
            if (entityName == null) {
                entityName = bean.getEjbName().trim().replaceAll("[ \\t\\n\\r-]+", "_");
            }
            entity.setName(entityName);

            // add the entity
            entityMappings.getEntity().add(entity);
            entitiesByName.put(bean.getEjbName(), entity);

            // for CMP 1 we also need apply the mappings to a super-mappings
            // declaration since fields are on the super-class
            String cmpImplClassName = CmpUtil.getCmpImplClassName(bean.getAbstractSchemaName(), bean.getEjbClass());
            boolean generateMappedSuperclass = bean.getCmpVersion() == CmpVersion.CMP1 && !bean.getEjbClass().equals(cmpImplClassName);

            // map the cmp class, but if we are using a mapped super class, generate attribute-override instead of id and basic
            mapClass(cmpImplClassName, entity, bean, classLoader, generateMappedSuperclass);

            if (generateMappedSuperclass) {
                // finally declare the mapped super class, if needed
                MappedSuperclass mappedSuperclass = new MappedSuperclass();
                mapClass(bean.getEjbClass(), mappedSuperclass, bean, classLoader, false);
                entityMappings.getMappedSuperclass().add(mappedSuperclass);
            }

            // process queries
            for (Query query : bean.getQuery()) {
                NamedQuery namedQuery = new NamedQuery();
                QueryMethod queryMethod = query.getQueryMethod();

                // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                StringBuilder name = new StringBuilder();
                name.append(ejbDeployment.getDeploymentId()).append(".").append(queryMethod.getMethodName());
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
                entity.getNamedQuery().add(namedQuery);
            }
            // todo: there should be a common interface between ejb query object and openejb query object
            for (org.apache.openejb.jee.oejb3.Query query : ejbDeployment.getQuery()) {
                NamedQuery namedQuery = new NamedQuery();
                org.apache.openejb.jee.oejb3.QueryMethod queryMethod = query.getQueryMethod();

                // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                StringBuilder name = new StringBuilder();
                name.append(ejbDeployment.getDeploymentId()).append(".").append(queryMethod.getMethodName());
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

                namedQuery.setQuery(query.getObjectQl());
                entity.getNamedQuery().add(namedQuery);
            }
        }

        Relationships relationships = ejbJar.getRelationships();
        if (relationships != null) {
            for (EjbRelation relation : relationships.getEjbRelation()) {
                List<EjbRelationshipRole> roles = relation.getEjbRelationshipRole();
                // if we don't have two roles, the relation is bad so we skip it
                if (roles.size() != 2) {
                    continue;
                }

                // get left entity
                EjbRelationshipRole leftRole = roles.get(0);
                RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
                // todo simplify role source using a wrapper
                String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
                Entity leftEntity = entitiesByName.get(leftEjbName);

                // get right entity
                EjbRelationshipRole rightRole = roles.get(1);
                RelationshipRoleSource rightRoleSource = rightRole.getRelationshipRoleSource();
                String rightEjbName = rightRoleSource == null ? null : rightRoleSource.getEjbName();
                Entity rightEntity = entitiesByName.get(rightEjbName);

                // neither left or right have a mapping which is fine
                if (leftEntity == null && rightEntity == null) {
                    continue;
                }
                // left not found?
                if (leftEntity == null) {
                    throw new OpenEJBException("Role source " + leftEjbName + " defined in relationship role " +
                            relation.getEjbRelationName() + "::" + leftRole.getEjbRelationshipRoleName() + " not found");
                }
                // right not found?
                if (rightEntity == null) {
                    throw new OpenEJBException("Role source " + rightEjbName + " defined in relationship role " +
                            relation.getEjbRelationName() + "::" + rightRole.getEjbRelationshipRoleName() + " not found");
                }

                String leftFieldName = null;
                if (leftRole.getCmrField() != null) {
                    leftFieldName = leftRole.getCmrField().getCmrFieldName();
                }
                boolean leftIsOne = leftRole.getMultiplicity() == Multiplicity.ONE;

                String rightFieldName = null;
                if (rightRole.getCmrField() != null) {
                    rightFieldName = rightRole.getCmrField().getCmrFieldName();
                }
                boolean rightIsOne = rightRole.getMultiplicity() == Multiplicity.ONE;

                if (leftIsOne && rightIsOne) {
                    //
                    // one-to-one
                    //

                    // left
                    OneToOne leftOneToOne = null;
                    if (leftFieldName != null) {
                        leftOneToOne = new OneToOne();
                        leftOneToOne.setName(leftFieldName);
                        setCascade(leftRole, leftOneToOne);
                        leftEntity.getAttributes().getOneToOne().add(leftOneToOne);
                    }

                    // right
                    OneToOne rightOneToOne = null;
                    if (rightFieldName != null) {
                        rightOneToOne = new OneToOne();
                        rightOneToOne.setName(rightFieldName);
                        // mapped by only required for bi-directional
                        if (leftFieldName != null) {
                            rightOneToOne.setMappedBy(leftFieldName);
                        }
                        setCascade(rightRole, rightOneToOne);
                        rightEntity.getAttributes().getOneToOne().add(rightOneToOne);
                    }

                    // link
                    if (leftFieldName != null && rightFieldName != null) {
                        leftOneToOne.setRelatedField(rightOneToOne);
                        rightOneToOne.setRelatedField(leftOneToOne);
                    }
                } else if (leftIsOne && !rightIsOne) {
                    //
                    // one-to-many
                    //

                    // left
                    OneToMany leftOneToMany = null;
                    if (leftFieldName != null) {
                        leftOneToMany = new OneToMany();
                        leftOneToMany.setName(leftFieldName);
                        // mapped by only required for bi-directional
                        if (rightFieldName != null) {
                            leftOneToMany.setMappedBy(rightFieldName);
                        }
                        setCascade(leftRole, leftOneToMany);
                        leftEntity.getAttributes().getOneToMany().add(leftOneToMany);
                    }

                    // right
                    ManyToOne rightManyToOne = null;
                    if (rightFieldName != null) {
                        rightManyToOne = new ManyToOne();
                        rightManyToOne.setName(rightFieldName);
                        setCascade(rightRole, rightManyToOne);
                        rightEntity.getAttributes().getManyToOne().add(rightManyToOne);
                    }

                    // link
                    if (leftFieldName != null && rightFieldName != null) {
                        leftOneToMany.setRelatedField(rightManyToOne);
                        rightManyToOne.setRelatedField(leftOneToMany);
                    }
                } else if (!leftIsOne && rightIsOne) {
                    //
                    // many-to-one
                    //

                    // left
                    ManyToOne leftManyToOne = null;
                    if (leftFieldName != null) {
                        leftManyToOne = new ManyToOne();
                        leftManyToOne.setName(leftFieldName);
                        setCascade(leftRole, leftManyToOne);
                        leftEntity.getAttributes().getManyToOne().add(leftManyToOne);
                    }

                    // right
                    OneToMany rightOneToMany = null;
                    if (rightFieldName != null) {
                        rightOneToMany = new OneToMany();
                        rightOneToMany.setName(rightFieldName);
                        // mapped by only required for bi-directional
                        if (leftFieldName != null) {
                            rightOneToMany.setMappedBy(leftFieldName);
                        }
                        setCascade(rightRole, rightOneToMany);
                        rightEntity.getAttributes().getOneToMany().add(rightOneToMany);
                    }

                    // link
                    if (leftFieldName != null && rightFieldName != null) {
                        leftManyToOne.setRelatedField(rightOneToMany);
                        rightOneToMany.setRelatedField(leftManyToOne);
                    }
                } else if (!leftIsOne && !rightIsOne) {
                    //
                    // many-to-many
                    //

                    // left
                    ManyToMany leftManyToMany = null;
                    if (leftFieldName != null) {
                        leftManyToMany = new ManyToMany();
                        leftManyToMany.setName(leftFieldName);
                        setCascade(leftRole, leftManyToMany);
                        leftEntity.getAttributes().getManyToMany().add(leftManyToMany);
                    }

                    // right
                    ManyToMany rightManyToMany = null;
                    if (rightFieldName != null) {
                        rightManyToMany = new ManyToMany();
                        rightManyToMany.setName(rightFieldName);
                        // mapped by only required for bi-directional
                        if (leftFieldName != null) {
                            rightManyToMany.setMappedBy(leftFieldName);
                        }
                        setCascade(rightRole, rightManyToMany);
                        rightEntity.getAttributes().getManyToMany().add(rightManyToMany);
                    }

                    // link
                    if (leftFieldName != null && rightFieldName != null) {
                        leftManyToMany.setRelatedField(rightManyToMany);
                        rightManyToMany.setRelatedField(leftManyToMany);
                    }
                }
            }
        }
    }

    private void mapClass(String className, Mapping mapping, EntityBean bean, ClassLoader classLoader, boolean createOverrides) {
        // class: the java class for the entity
        mapping.setClazz(className);

        //
        // Map fields remembering the names of the mapped fields
        //
        Set<String> persistantFields = new TreeSet<String>();

        //
        // id: the primary key
        //
        if (bean.getPrimkeyField() != null) {
            Field field;
            if (!createOverrides) {
                field = new Id(bean.getPrimkeyField());
            } else {
                field = new AttributeOverride(bean.getPrimkeyField());
            }
            mapping.addField(field);
            persistantFields.add(bean.getPrimkeyField());
        } else {
            // todo complex primary key
            // todo unknown primary key
        }

        //
        // basic: cmp-fields
        //
        for (CmpField cmpField : bean.getCmpField()) {
            if (!cmpField.getFieldName().equals(bean.getPrimkeyField())) {
                Field field;
                if (!createOverrides) {
                    field = new Basic(cmpField.getFieldName());
                } else {
                    field = new AttributeOverride(cmpField.getFieldName());
                }
                mapping.addField(field);
                persistantFields.add(cmpField.getFieldName());
            }
        }

        //
        // transient: non-persistent fields
        //
        if (classLoader != null && bean.getCmpVersion() == CmpVersion.CMP1) {
            try {
                Class ejbClass = classLoader.loadClass(className);
                for (java.lang.reflect.Field field : ejbClass.getDeclaredFields()) {
                    if (!persistantFields.contains(field.getName())) {
                        Transient transientField = new Transient(field.getName());
                        mapping.addField(transientField);
                    }
                }
            } catch (ClassNotFoundException e) {
                // todo warn
            }
        }
    }

    private boolean addPersistenceContextRef(EntityBean bean) {
        for (PersistenceContextRef ref : bean.getPersistenceContextRef()) {
            // if a ref is already defined, skip this bean
            if (ref.getName().equals(JpaCmpEngine.CMP_PERSISTENCE_CONTEXT_REF_NAME)) {
                return false;
            }
        }
        PersistenceContextRef persistenceContextRef = new PersistenceContextRef();
        persistenceContextRef.setName(JpaCmpEngine.CMP_PERSISTENCE_CONTEXT_REF_NAME);
        persistenceContextRef.setPersistenceUnitName(CMP_PERSISTENCE_UNIT_NAME);
        bean.getPersistenceContextRef().add(persistenceContextRef);
        return true;
    }

    private void setCascade(EjbRelationshipRole role, RelationField field) {
        if (role.getCascadeDelete()) {
            CascadeType cascadeType = new CascadeType();
            cascadeType.setCascadeAll(true);
            field.setCascade(cascadeType);
        }
    }
}
