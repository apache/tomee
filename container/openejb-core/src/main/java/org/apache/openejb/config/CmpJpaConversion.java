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
import org.apache.openejb.jee.jpa.IdClass;
import org.apache.openejb.jee.jpa.GeneratedValue;
import org.apache.openejb.jee.jpa.GenerationType;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.jee.jpa.unit.Properties;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.TreeMap;
import java.lang.reflect.Modifier;

public class CmpJpaConversion implements DynamicDeployer {
    private static final String CMP_PERSISTENCE_UNIT_NAME = "cmp";

    private static final Set<String> ENHANCEED_FIELDS = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(
            "pcInheritedFieldCount",
            "pcFieldNames",
            "pcFieldTypes",
            "pcFieldFlags",
            "pcPCSuperclass",
            "pcStateManager",
            "class$Ljava$lang$String",
            "class$Ljava$lang$Integer",
            "class$Lcom$sun$ts$tests$common$ejb$wrappers$CMP11Wrapper",
            "pcDetachedState",
            "serialVersionUID"
    )));

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
                persistenceUnit.setJtaDataSource("java:openejb/Resource/Default JDBC Database");
                persistenceUnit.setNonJtaDataSource("java:openejb/Resource/Default Unmanaged JDBC Database");
                // todo paramterize this
                Properties properties = new Properties();
                properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true, Indexes=false, IgnoreErrors=true)");
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

        Map<String, MappedSuperclass> mappedSuperclassByClass = new TreeMap<String,MappedSuperclass>();
        for (MappedSuperclass mappedSuperclass : entityMappings.getMappedSuperclass()) {
            mappedSuperclassByClass.put(mappedSuperclass.getClazz(), mappedSuperclass);
        }

        Map<String, Entity> entitiesByName = new TreeMap<String,Entity>();
        for (Entity entity : entityMappings.getEntity()) {
            entitiesByName.put(entity.getName(), entity);
        }
        
        for (org.apache.openejb.jee.EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            // skip all non-CMP beans
            if (!(enterpriseBean instanceof EntityBean) ||
                    ((EntityBean) enterpriseBean).getPersistenceType() != PersistenceType.CONTAINER) {
                continue;
            }
            EntityBean bean = (EntityBean) enterpriseBean;

            // Always set the abstract schema name
            if (bean.getAbstractSchemaName() == null) {
                String abstractSchemaName = bean.getEjbName().trim().replaceAll("[ \\t\\n\\r-]+", "_");
                if (entitiesByName.containsKey(abstractSchemaName)) {
                    int i = 2;
                    while (entitiesByName.containsKey(abstractSchemaName + i)) {
                         i++;
                    }
                    abstractSchemaName = abstractSchemaName + i;
                }
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
            entity.setName(entityName);

            // class: impl class name
            String cmpImplClassName = CmpUtil.getCmpImplClassName(bean.getAbstractSchemaName(), bean.getEjbClass());
            entity.setClazz(cmpImplClassName);

            // add the entity
            entityMappings.getEntity().add(entity);
            entitiesByName.put(bean.getEjbName(), entity);

            if (bean.getCmpVersion() == CmpVersion.CMP2) {
                mapClass2x(entity, bean, classLoader);
            } else {
                // map the cmp class, but if we are using a mapped super class, generate attribute-override instead of id and basic
                Collection<MappedSuperclass> mappedSuperclasses = mapClass1x(bean.getEjbClass(), entity, bean, classLoader);
                for (MappedSuperclass mappedSuperclass : mappedSuperclasses) {
                    mappedSuperclassByClass.put(mappedSuperclass.getClazz(), mappedSuperclass);
                }
            }

            // process queries
            for (Query query : bean.getQuery()) {
                NamedQuery namedQuery = new NamedQuery();
                QueryMethod queryMethod = query.getQueryMethod();

                // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                StringBuilder name = new StringBuilder();
                name.append(entityName).append(".").append(queryMethod.getMethodName());
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
            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment != null) {
                for (org.apache.openejb.jee.oejb3.Query query : ejbDeployment.getQuery()) {
                    NamedQuery namedQuery = new NamedQuery();
                    org.apache.openejb.jee.oejb3.QueryMethod queryMethod = query.getQueryMethod();

                    // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                    StringBuilder name = new StringBuilder();
                    name.append(entityName).append(".").append(queryMethod.getMethodName());
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
        }
        entityMappings.getMappedSuperclass().addAll(mappedSuperclassByClass.values());

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
                boolean leftSynthetic = false;
                if (leftRole.getCmrField() != null) {
                    leftFieldName = leftRole.getCmrField().getCmrFieldName();
                } else {
                    leftFieldName = rightEntity.getName() + "_" + rightRole.getCmrField().getCmrFieldName();
                    leftSynthetic = true;
                }
                boolean leftIsOne = leftRole.getMultiplicity() == Multiplicity.ONE;

                String rightFieldName = null;
                boolean rightSynthetic = false;
                if (rightRole.getCmrField() != null) {
                    rightFieldName = rightRole.getCmrField().getCmrFieldName();
                } else {
                    rightFieldName = leftEntity.getName() + "_" + leftRole.getCmrField().getCmrFieldName();
                    rightSynthetic = true;
                }
                boolean rightIsOne = rightRole.getMultiplicity() == Multiplicity.ONE;

                if (leftIsOne && rightIsOne) {
                    //
                    // one-to-one
                    //

                    // left
                    OneToOne leftOneToOne = null;
                    leftOneToOne = new OneToOne();
                    leftOneToOne.setName(leftFieldName);
                    leftOneToOne.setSyntheticField(leftSynthetic);
                    setCascade(rightRole, leftOneToOne);
                    leftEntity.getAttributes().getOneToOne().add(leftOneToOne);

                    // right
                    OneToOne rightOneToOne = null;
                    rightOneToOne = new OneToOne();
                    rightOneToOne.setName(rightFieldName);
                    rightOneToOne.setSyntheticField(rightSynthetic);
                    rightOneToOne.setMappedBy(leftFieldName);
                    setCascade(leftRole, rightOneToOne);
                    rightEntity.getAttributes().getOneToOne().add(rightOneToOne);

                    // link
                    leftOneToOne.setRelatedField(rightOneToOne);
                    rightOneToOne.setRelatedField(leftOneToOne);
                } else if (leftIsOne && !rightIsOne) {
                    //
                    // one-to-many
                    //

                    // left
                    OneToMany leftOneToMany = null;
                    leftOneToMany = new OneToMany();
                    leftOneToMany.setName(leftFieldName);
                    leftOneToMany.setSyntheticField(leftSynthetic);
                    leftOneToMany.setMappedBy(rightFieldName);
                    setCascade(rightRole, leftOneToMany);
                    leftEntity.getAttributes().getOneToMany().add(leftOneToMany);

                    // right
                    ManyToOne rightManyToOne = null;
                    rightManyToOne = new ManyToOne();
                    rightManyToOne.setName(rightFieldName);
                    rightManyToOne.setSyntheticField(rightSynthetic);
                    setCascade(leftRole, rightManyToOne);
                    rightEntity.getAttributes().getManyToOne().add(rightManyToOne);

                    // link
                    leftOneToMany.setRelatedField(rightManyToOne);
                    rightManyToOne.setRelatedField(leftOneToMany);
                } else if (!leftIsOne && rightIsOne) {
                    //
                    // many-to-one
                    //

                    // left
                    ManyToOne leftManyToOne = null;
                    leftManyToOne = new ManyToOne();
                    leftManyToOne.setName(leftFieldName);
                    leftManyToOne.setSyntheticField(leftSynthetic);
                    setCascade(rightRole, leftManyToOne);
                    leftEntity.getAttributes().getManyToOne().add(leftManyToOne);

                    // right
                    OneToMany rightOneToMany = null;
                    rightOneToMany = new OneToMany();
                    rightOneToMany.setName(rightFieldName);
                    rightOneToMany.setSyntheticField(rightSynthetic);
                    rightOneToMany.setMappedBy(leftFieldName);
                    setCascade(leftRole, rightOneToMany);
                    rightEntity.getAttributes().getOneToMany().add(rightOneToMany);

                    // link
                    leftManyToOne.setRelatedField(rightOneToMany);
                    rightOneToMany.setRelatedField(leftManyToOne);
                } else if (!leftIsOne && !rightIsOne) {
                    //
                    // many-to-many
                    //

                    // left
                    ManyToMany leftManyToMany = null;
                    leftManyToMany = new ManyToMany();
                    leftManyToMany.setName(leftFieldName);
                    leftManyToMany.setSyntheticField(leftSynthetic);
                    setCascade(rightRole, leftManyToMany);
                    leftEntity.getAttributes().getManyToMany().add(leftManyToMany);

                    // right
                    ManyToMany rightManyToMany = null;
                    rightManyToMany = new ManyToMany();
                    rightManyToMany.setName(rightFieldName);
                    rightManyToMany.setSyntheticField(rightSynthetic);
                    rightManyToMany.setMappedBy(leftFieldName);
                    setCascade(leftRole, rightManyToMany);
                    rightEntity.getAttributes().getManyToMany().add(rightManyToMany);

                    // link
                    leftManyToMany.setRelatedField(rightManyToMany);
                    rightManyToMany.setRelatedField(leftManyToMany);
                }
            }
        }
    }

    private void mapClass2x(Mapping mapping, EntityBean bean, ClassLoader classLoader) {
        Set<String> allFields = new TreeSet<String>();
        for (CmpField cmpField : bean.getCmpField()) {
            allFields.add(cmpField.getFieldName());
        }

        //
        // id: the primary key
        //
        Set<String> primaryKeyFields = new HashSet<String>();
        if (bean.getPrimkeyField() != null) {
            String fieldName = bean.getPrimkeyField();
            Field field = new Id(fieldName);
            mapping.addField(field);
            primaryKeyFields.add(fieldName);
        } else if ("java.lang.Object".equals(bean.getPrimKeyClass())) {
            String fieldName = "OpenEJB_pk";
            Id field = new Id(fieldName);
            field.setGeneratedValue(new GeneratedValue(GenerationType.AUTO));
            mapping.addField(field);
            primaryKeyFields.add(fieldName);
        } else if (bean.getPrimKeyClass() != null) {
            Class<?> pkClass = null;
            try {
                pkClass = classLoader.loadClass(bean.getPrimKeyClass());
                mapping.setIdClass(new IdClass(bean.getPrimKeyClass()));
                for (java.lang.reflect.Field pkField : pkClass.getFields()) {
                    String pkFieldName = pkField.getName();
                    int modifiers = pkField.getModifiers();
                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && allFields.contains(pkFieldName)) {
                        Field field = new Id(pkFieldName);
                        mapping.addField(field);
                        primaryKeyFields.add(pkFieldName);
                    }
                }
            } catch (ClassNotFoundException e) {
                // todo throw exception
            }
        }

        //
        // basic: cmp-fields
        //
        for (CmpField cmpField : bean.getCmpField()) {
            if (!primaryKeyFields.contains(cmpField.getFieldName())) {
                Field field = new Basic(cmpField.getFieldName());
                mapping.addField(field);
            }
        }
    }

    private Collection<MappedSuperclass> mapClass1x(String ejbClassName, Mapping mapping, EntityBean bean, ClassLoader classLoader) {
        Class ejbClass = null;
        try {
            ejbClass = classLoader.loadClass(ejbClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        // build a set of all field names
        Set<String> allFields = new TreeSet<String>();
        for (CmpField cmpField : bean.getCmpField()) {
            allFields.add(cmpField.getFieldName());
        }

        // build a map from the field name to the super class that contains that field
        Map<String, MappedSuperclass> superclassByField = mapFields(ejbClass, allFields);
        //
        // id: the primary key
        //
        Set<String> primaryKeyFields = new HashSet<String>();
        if (bean.getPrimkeyField() != null) {
            String fieldName = bean.getPrimkeyField();
            MappedSuperclass superclass = superclassByField.get(fieldName);
            if (superclass == null) {
                throw new IllegalStateException("Primary key field " + fieldName + " is not defined in class " + ejbClassName + " or any super classes");
            }
            superclass.addField(new Id(fieldName));
            mapping.addField(new AttributeOverride(fieldName));
            primaryKeyFields.add(fieldName);
        } else if ("java.lang.Object".equals(bean.getPrimKeyClass())) {
            String fieldName = "OpenEJB_pk";
            Id field = new Id(fieldName);
            field.setGeneratedValue(new GeneratedValue(GenerationType.AUTO));
            mapping.addField(field);
        } else if (bean.getPrimKeyClass() != null) {
            Class<?> pkClass = null;
            try {
                pkClass = classLoader.loadClass(bean.getPrimKeyClass());
                MappedSuperclass superclass = null;
                for (java.lang.reflect.Field pkField : pkClass.getFields()) {
                    String fieldName = pkField.getName();
                    int modifiers = pkField.getModifiers();
                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && allFields.contains(fieldName)) {
                        superclass = superclassByField.get(fieldName);
                        if (superclass == null) {
                            throw new IllegalStateException("Primary key field " + fieldName + " is not defined in class " + ejbClassName + " or any super classes");
                        }
                        superclass.addField(new Id(fieldName));
                        mapping.addField(new AttributeOverride(fieldName));
                        primaryKeyFields.add(fieldName);
                    }
                }
                if (superclass != null) {
                    superclass.setIdClass(new IdClass(bean.getPrimKeyClass()));
                }
            } catch (ClassNotFoundException e) {
                // todo throw exception
            }
        }

        //
        // basic: cmp-fields
        //
        for (CmpField cmpField : bean.getCmpField()) {
            String fieldName = cmpField.getFieldName();
            if (!primaryKeyFields.contains(fieldName)) {
                MappedSuperclass superclass = superclassByField.get(fieldName);
                if (superclass == null) {
                    throw new IllegalStateException("Primary key field " + fieldName + " is not defined in class " + ejbClassName + " or any super classes");
                }
                superclass.addField(new Basic(fieldName));
                mapping.addField(new AttributeOverride(fieldName));
            }
        }

        return new HashSet<MappedSuperclass>(superclassByField.values());
    }

    private Map<String, MappedSuperclass> mapFields(Class clazz, Set<String> persistantFields) {
        persistantFields = new TreeSet<String>(persistantFields);
        Map<String,MappedSuperclass> fields = new TreeMap<String,MappedSuperclass>();

        while (!persistantFields.isEmpty() && !clazz.equals(Object.class)) {
            MappedSuperclass superclass = new MappedSuperclass(clazz.getName());
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();
                if (persistantFields.contains(fieldName)) {
                    fields.put(fieldName, superclass);
                    persistantFields.remove(fieldName);
                } else if (!ENHANCEED_FIELDS.contains(fieldName)){
                    Transient transientField = new Transient(fieldName);
                    superclass.addField(transientField);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return fields;
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
