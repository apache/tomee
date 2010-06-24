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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ejb.EJBLocalObject;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.core.cmp.jpa.JpaCmpEngine;
import org.apache.openejb.jee.*;
import org.apache.openejb.jee.jpa.*;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.Properties;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Strings;

public class CmpJpaConversion implements DynamicDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, CmpJpaConversion.class);

    private static final String CMP_PERSISTENCE_UNIT_NAME = "cmp";

    // A specific set of fields that get marked as transient in the superclass mappings 
    private static final Set<String> ENHANCED_FIELDS = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(
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

        if (!hasCmpEntities(appModule)) return appModule;

        // todo scan existing persistence module for all entity mappings and don't generate mappings for them

        // create mappings if no mappings currently exist 
        EntityMappings cmpMappings = appModule.getCmpMappings();
        if (cmpMappings == null) {
            cmpMappings = new EntityMappings();
            cmpMappings.setVersion("1.0");
            appModule.setCmpMappings(cmpMappings);
        }

        // we process this one jar-file at a time...each contributing to the 
        // app mapping data 
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            EjbJar ejbJar = ejbModule.getEjbJar();

            // scan for CMP entity beans and merge the data into the collective set 
            for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
                if (isCmpEntity(enterpriseBean)) {
                    processEntityBean(ejbModule, cmpMappings, (EntityBean) enterpriseBean);
                }
            }

            // if there are relationships defined in this jar, get a list of the defined
            // entities and process the relationship maps. 
            Relationships relationships = ejbJar.getRelationships();
            if (relationships != null) {

                Map<String, Entity> entitiesByEjbName = new TreeMap<String,Entity>();
                for (Entity entity : cmpMappings.getEntity()) {
                    entitiesByEjbName.put(entity.getEjbName(), entity);
                }

                for (EjbRelation relation : relationships.getEjbRelation()) {
                    processRelationship(entitiesByEjbName, relation);
                }
            }

            // Let's warn the user about any declarations we didn't end up using
            // so there can be no misunderstandings.
            EntityMappings userMappings = getUserEntityMappings(ejbModule);
            for (Entity mapping : userMappings.getEntity()) {
                logger.warning("openejb-cmp-orm.xml mapping ignored: module="+ejbModule.getModuleId()+":  <entity class=\""+mapping.getClazz()+"\">");
            }

            for (MappedSuperclass mapping : userMappings.getMappedSuperclass()) {
                logger.warning("openejb-cmp-orm.xml mapping ignored: module="+ejbModule.getModuleId()+":  <mapped-superclass class=\""+mapping.getClazz()+"\">");
            }

        }

        if (!cmpMappings.getEntity().isEmpty()) {
            PersistenceUnit persistenceUnit = getCmpPersistenceUnit(appModule);

            persistenceUnit.getMappingFile().add("META-INF/openejb-cmp-generated-orm.xml");
            for (Entity entity : cmpMappings.getEntity()) {
                persistenceUnit.getClazz().add(entity.getClazz());
            }
        }

        // TODO: This should not be necessary, but having an empty <attributes/> tag
        // causes some of the unit tests to fail.  Not sure why.  Should be fixed.
        for (Entity entity : appModule.getCmpMappings().getEntity()) {
            if (entity.getAttributes() != null && entity.getAttributes().isEmpty()){
                entity.setAttributes(null);
            }
        }
        return appModule;
    }

    private PersistenceUnit getCmpPersistenceUnit(AppModule appModule) {
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
        // if not found create one
        if (persistenceUnit == null) {
            persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName(CMP_PERSISTENCE_UNIT_NAME);
            persistenceUnit.setTransactionType(TransactionType.JTA);
            // Don't set default values here, let the autoconfig do that
            // persistenceUnit.setJtaDataSource("java:openejb/Resource/Default JDBC Database");
            // persistenceUnit.setNonJtaDataSource("java:openejb/Resource/Default Unmanaged JDBC Database");
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
        return persistenceUnit;
    }

    /**
     * Test if a module contains CMP entity beans that will 
     * need a JPA mapping generated. 
     * 
     * @param appModule The source application module.
     * 
     * @return true if the module contains any entity beans 
     *         using container managed persistence.
     */
    private boolean hasCmpEntities(AppModule appModule) {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (isCmpEntity(bean)) return true;
            }
        }
        return false;
    }

    /**
     * Tests if an EJB is an entity bean using container 
     * managed persistence. 
     * 
     * @param bean   The source bean.
     * 
     * @return True if all of the conditions for a CMP bean are met.
     */
    private static boolean isCmpEntity(EnterpriseBean bean) {
        return bean instanceof EntityBean && ((EntityBean) bean).getPersistenceType() == PersistenceType.CONTAINER;
    }

    private void processRelationship(Map<String, Entity> entitiesByEjbName, EjbRelation relation) throws OpenEJBException {
        List<EjbRelationshipRole> roles = relation.getEjbRelationshipRole();
        // if we don't have two roles, the relation is bad so we skip it
        if (roles.size() != 2) {
            return;
        }

        // get left entity
        EjbRelationshipRole leftRole = roles.get(0);
        RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
        String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
        Entity leftEntity = entitiesByEjbName.get(leftEjbName);

        // get right entity
        EjbRelationshipRole rightRole = roles.get(1);
        RelationshipRoleSource rightRoleSource = rightRole.getRelationshipRoleSource();
        String rightEjbName = rightRoleSource == null ? null : rightRoleSource.getEjbName();
        Entity rightEntity = entitiesByEjbName.get(rightEjbName);

        // neither left or right have a mapping which is fine
        if (leftEntity == null && rightEntity == null) {
            return;
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

        final Attributes rightAttributes = rightEntity.getAttributes();
        Map<String, RelationField> rightRelationships = rightAttributes.getRelationshipFieldMap();
        final Attributes leftAttributes = leftEntity.getAttributes();
        Map<String, RelationField> leftRelationships = leftAttributes.getRelationshipFieldMap();

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
            addRelationship(leftOneToOne, leftRelationships, leftAttributes.getOneToOne());

            // right
            OneToOne rightOneToOne = null;
            rightOneToOne = new OneToOne();
            rightOneToOne.setName(rightFieldName);
            rightOneToOne.setSyntheticField(rightSynthetic);
            rightOneToOne.setMappedBy(leftFieldName);
            setCascade(leftRole, rightOneToOne);
            addRelationship(rightOneToOne, rightRelationships, rightAttributes.getOneToOne());

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
            addRelationship(leftOneToMany, leftRelationships, leftAttributes.getOneToMany());

            // right
            ManyToOne rightManyToOne = null;
            rightManyToOne = new ManyToOne();
            rightManyToOne.setName(rightFieldName);
            rightManyToOne.setSyntheticField(rightSynthetic);
            setCascade(leftRole, rightManyToOne);
            addRelationship(rightManyToOne, rightRelationships, rightAttributes.getManyToOne());

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
            addRelationship(leftManyToOne, leftRelationships, leftAttributes.getManyToOne());

            // right
            OneToMany rightOneToMany = null;
            rightOneToMany = new OneToMany();
            rightOneToMany.setName(rightFieldName);
            rightOneToMany.setSyntheticField(rightSynthetic);
            rightOneToMany.setMappedBy(leftFieldName);
            setCascade(leftRole, rightOneToMany);
            addRelationship(rightOneToMany, rightRelationships, rightAttributes.getOneToMany());

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
            addRelationship(leftManyToMany, leftRelationships, leftAttributes.getManyToMany());

            // right
            ManyToMany rightManyToMany = null;
            rightManyToMany = new ManyToMany();
            rightManyToMany.setName(rightFieldName);
            rightManyToMany.setSyntheticField(rightSynthetic);
            rightManyToMany.setMappedBy(leftFieldName);
            setCascade(leftRole, rightManyToMany);
            addRelationship(rightManyToMany, rightRelationships, rightAttributes.getManyToMany());

            // link
            leftManyToMany.setRelatedField(rightManyToMany);
            rightManyToMany.setRelatedField(leftManyToMany);
        }
    }

    private <R extends RelationField> R addRelationship(R relationship, Map<String, RelationField> existing, List<R> relationships) {
        R r = null;

        try {
            r = (R) existing.get(relationship.getKey());
        } catch (ClassCastException e) {
            return relationship;
        }

        if (r == null){
            r = relationship;
            relationships.add(relationship);
        }

        return r;
    }

    /**
     * Generate the CMP mapping data for an individual 
     * EntityBean.  
     * 
     * @param ejbModule The module containing the bean.
     * @param entityMappings
     *                  The accumulated set of entity mappings.
     * @param bean      The been we're generating the mapping for.
     */
    private void processEntityBean(EjbModule ejbModule, EntityMappings entityMappings, EntityBean bean) {
        // try to add a new persistence-context-ref for cmp
        if (!addPersistenceContextRef(bean)) {
            // Bean already has a persistence-context-ref for cmp
            // which means it has a mapping, so skip this bean
            return;
        }

        // get the real bean class 
        Class ejbClass = loadClass(ejbModule.getClassLoader(), bean.getEjbClass());
        // and generate a name for the subclass that will be generated and handed to the JPA 
        // engine as the managed class. 
        String jpaEntityClassName = CmpUtil.getCmpImplClassName(bean.getAbstractSchemaName(), ejbClass.getName());

        // We don't use this mapping directly, instead we pull entries from it
        // the reason being is that we intend to support mappings that aren't
        // exactly correct.  i.e. users should be able to write mappings completely
        // ignorant of the fact that we subclass.  The fact that we subclass means
        // these user supplied mappings might need to be adjusted as the jpa orm.xml
        // file is extremely subclass/supperclass aware and mappings specified in it
        // need to be spot on.
        EntityMappings userMappings = getUserEntityMappings(ejbModule);

        // Look for any existing mapped superclass mappings.  We check the entire inheritance 
        // chain of the bean looking for any that have user defined mappings. 
        for (Class clazz = ejbClass; clazz != null; clazz = clazz.getSuperclass()){

            MappedSuperclass mappedSuperclass = removeMappedSuperclass(userMappings, clazz.getName());

            // We're going to assume that if they bothered to map a superclass
            // that the mapping is correct.  Copy it from their mappings to ours
            if (mappedSuperclass != null) {
                entityMappings.getMappedSuperclass().add(mappedSuperclass);
            }
        }

        // Look for an existing mapping using the openejb generated subclass name
        Entity entity = removeEntity(userMappings, jpaEntityClassName);

        // DMB: For the first iteration, we're not going to allow
        // anything other than the ugly mapping file we generate.
        // So if they supplied an entity, it better be correct
        // because we are going to ignore all other xml metadata.
        if (entity != null) {
            // XmlMetadataComplete is an OpenEJB specific flag that
            // tells all other legacy descriptor converters to keep
            // their hands off.
            entity.setXmlMetadataComplete(true);

            entityMappings.getEntity().add(entity);

            return;
        }

// This section is an in progress TODO
//        if (entity == null){
//            entity = removeEntity(userMappings, ejbClass.getName());
//            // OVERWRITE: class: impl class name
//            if (entity != null) {
//                entity.setClazz(jpaEntityClassName);
//
//                if (Modifier.isAbstract(ejbClass.getModifiers())){
//                    // This is a CMP2 bean and we allowed the user to
//                    // define it via the orm.xml file as an <entity>
//                    // We need to split this definition.  We need
//                    // an <entity> definition for the generated subclass
//                    // and a <mapped-superclass> for the bean class
//
//
//                }
//            }
//        }

        if (entity == null){
            entity = new Entity(jpaEntityClassName);
        }

        // Aggressively add an "Attributes" instance so we don't
        // have to check for null everywhere.
        if (entity.getAttributes() == null){
            entity.setAttributes(new Attributes());
        }

        // add the entity
        entityMappings.getEntity().add(entity);

        // OVERWRITE: description: contains the name of the entity bean
        entity.setDescription(ejbModule.getModuleId() + "#" + bean.getEjbName());


        // PRESERVE has queries: name: the name of the entity in queries
        String entityName = bean.getAbstractSchemaName();
        entity.setName(entityName);
        entity.setEjbName(bean.getEjbName());



        ClassLoader classLoader = ejbModule.getClassLoader();
        Collection<MappedSuperclass> mappedSuperclasses; 
        if (bean.getCmpVersion() == CmpVersion.CMP2) {
            // perform the 2.x class mapping.  This really just identifies the primary key and 
            // other cmp fields that will be generated for the concrete class and identify them 
            // to JPA. 
            mappedSuperclasses = mapClass2x(entity, bean, classLoader);
        } else {
            // map the cmp class, but if we are using a mapped super class, 
            // generate attribute-override instead of id and basic
            mappedSuperclasses = mapClass1x(bean.getEjbClass(), entity, bean, classLoader);
        }
        
        // if we have superclass mappings to process, add those to the
        // configuration. f
        if (mappedSuperclasses != null) {
            // now that things are mapped out, add the superclass mappings to the entity mappings 
            // that will get passed to the JPA engine. 
            for (MappedSuperclass mappedSuperclass : mappedSuperclasses) {
                entityMappings.getMappedSuperclass().add(mappedSuperclass);
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
        OpenejbJar openejbJar = ejbModule.getOpenejbJar();
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

    private Entity removeEntity(EntityMappings userMappings, String className) {
        Entity entity;

        entity = userMappings.getEntityMap().get(className);
        if (entity != null){
            userMappings.getEntityMap().remove(entity.getKey());
        }
        return entity;
    }

    /**
     * Check the user-defined entity mappings for a superclass 
     * mapping for a given named class. If the user mappings exist, 
     * remove them from the user list and return them 
     * for inclusion in our generated mappings. 
     * 
     * @param userMappings
     *                  The current user mapping set.
     * @param className The name of the class of interest.
     * 
     * @return Returns the superclass mapping for the named class, or 
     *         null if the class is not in the mapping set.
     */
    private MappedSuperclass removeMappedSuperclass(EntityMappings userMappings, String className) {
        MappedSuperclass mappedSuperclass;

        mappedSuperclass = userMappings.getMappedSuperclassMap().get(className);
        if (mappedSuperclass != null){
            userMappings.getMappedSuperclassMap().remove(mappedSuperclass.getKey());
        }
        return mappedSuperclass;
    }

    private EntityMappings getUserEntityMappings(EjbModule ejbModule) {
        Object o = ejbModule.getAltDDs().get("openejb-cmp-orm.xml");
        if (o instanceof EntityMappings) {
            return (EntityMappings) o;
        }
        return new EntityMappings();
    }

    /**
     * Generate the JPA mapping for a CMP 2.x bean.  Since 
     * the field accessors are all defined as abstract methods 
     * and the fields will not be defined in the implementation 
     * class, we don't need to deal with mapped superclasses. 
     * All of the fields and concrete methods will be 
     * implemented by the generated subclass, so from 
     * a JPA standpoint, there are no mapped superclasses 
     * required. 
     * 
     * @param mapping The mapping information we're updating.
     * @param bean    The entity bean meta data
     * @param classLoader
     *                The classloader for resolving class references and
     *                primary key classes.
     */
    private Collection<MappedSuperclass> mapClass2x(Mapping mapping, EntityBean bean, ClassLoader classLoader) {
        Set<String> allFields = new TreeSet<String>();
        // get an acculated set of the CMP fields. 
        for (CmpField cmpField : bean.getCmpField()) {
            allFields.add(cmpField.getFieldName());
        }
        
        Class<?> beanClass = null; 

        try {
            beanClass = classLoader.loadClass(bean.getEjbClass());
        } catch (ClassNotFoundException e) {
            // class was already loaded in validation phase, so this should succeed 
            // if it does fail, just return null from here
            return null; 
        }
        
        
        // build a map from the field name to the super class that contains that field.
        // If this is a strictly CMP 2.x class, this is generally an empty map.  However, 
        // we support some migration steps toward EJB3, so this can be defined completely 
        // or partially as a POJO with concrete fields and accessors.  This allows us to 
        // locate and generate the mappings 
        Map<String, MappedSuperclass> superclassByField = mapFields(beanClass, allFields);
        
        // Add the cmp-field declarations for all the cmp fields that
        // weren't explicitly declared in the ejb-jar.xml. 
        // we can identify these by looking for abstract methods that match 
        // the get<Name> or is<Name> pattern. 
            
        for (Method method : beanClass.getMethods()) {
            if (!Modifier.isAbstract(method.getModifiers())) continue;
            if (method.getParameterTypes().length != 0) continue;
            if (method.getReturnType().equals(Void.TYPE)) continue;

            // Skip relationships: anything of type EJBLocalObject or Collection
            if (EJBLocalObject.class.isAssignableFrom(method.getReturnType())) continue;
            if (Collection.class.isAssignableFrom(method.getReturnType())) continue;
            if (Map.class.isAssignableFrom(method.getReturnType())) continue;

            String name = method.getName();

            if (name.startsWith("get")){
                name = name.substring("get".length(), name.length());
            } else if (name.startsWith("is")){
                // Only add this if the return type from an "is" method 
                // boolean. 
                if (method.getReturnType() == Boolean.TYPE) {
                    name = name.substring("is".length(), name.length());
                }
                else { 
                    // not an acceptable "is" method. 
                    continue; 
                }
            } else continue;

            // the property needs the first character lowercased.  Generally, 
            // we'll have this field already in our list, but it might have been 
            // omitted from the meta data. 
            name = Strings.lcfirst(name);
            if (!allFields.contains(name)){
                allFields.add(name);
                bean.addCmpField(name);
            }
        }
        

        //
        // id: the primary key
        //
        Set<String> primaryKeyFields = new HashSet<String>();
        
        
        if (bean.getPrimkeyField() != null) {
            String fieldName = bean.getPrimkeyField();
            MappedSuperclass superclass = superclassByField.get(fieldName);
            // this need not be here...for CMP 2.x, these are generally autogenerated fields. 
            if (superclass != null) {
                // ok, add this field to the superclass mapping 
                superclass.addField(new Id(fieldName));
                // the direct mapping is an over ride 
                mapping.addField(new AttributeOverride(fieldName));
            }
            else {
                // this is a normal generated field, it will be in the main class mapping. 
                mapping.addField(new Id(fieldName));
            }
            primaryKeyFields.add(fieldName);
        } else if ("java.lang.Object".equals(bean.getPrimKeyClass())) {
            // the automatically generated keys use a special property name 
            // and will always be in the generated superclass. 
            String fieldName = "OpenEJB_pk";
            Id field = new Id(fieldName);
            field.setGeneratedValue(new GeneratedValue(GenerationType.AUTO));
            mapping.addField(field);
            primaryKeyFields.add(fieldName);
        } else if (bean.getPrimKeyClass() != null) {
            Class<?> pkClass = null;
            try {
                pkClass = classLoader.loadClass(bean.getPrimKeyClass());
                MappedSuperclass idclass = null; 
                // now validate the primary class fields against the bean cmp fields 
                // to make sure everything maps correctly. 
                for (java.lang.reflect.Field pkField : pkClass.getFields()) {
                    String pkFieldName = pkField.getName();
                    int modifiers = pkField.getModifiers();
                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && allFields.contains(pkFieldName)) {
                        // see if the bean field is concretely defined in one of the superclasses 
                        MappedSuperclass superclass = superclassByField.get(pkFieldName);
                        if (superclass != null) {
                            // ok, we have an override that needs to be specified at the main class level. 
                            superclass.addField(new Id(pkFieldName));
                            mapping.addField(new AttributeOverride(pkFieldName));
                            idclass = resolveIdClass(idclass, superclass, beanClass); 
                        }
                        else {
                            // this field will be autogenerated 
                            mapping.addField(new Id(pkFieldName));
                        }
                        primaryKeyFields.add(pkFieldName);
                    }
                }
                // if we've located an ID class, set it as such 
                if (idclass != null) {
                    idclass.setIdClass(new IdClass(bean.getPrimKeyClass()));
                }
                else {
                    // do this for the toplevel mapping 
                    mapping.setIdClass(new IdClass(bean.getPrimKeyClass()));
                }
            } catch (ClassNotFoundException e) {
                throw (IllegalStateException)new IllegalStateException("Could not find entity primary key class " + bean.getPrimKeyClass()).initCause(e);
            }
        }

        //
        // basic: cmp-fields
        // This again, adds all of the additional cmp-fields to the mapping 
        //
        for (CmpField cmpField : bean.getCmpField()) {
            // only add entries for cmp fields that are not part of the primary key 
            if (!primaryKeyFields.contains(cmpField.getFieldName())) {
                String fieldName = cmpField.getFieldName(); 
                // this will be here if we've already processed this 
                MappedSuperclass superclass = superclassByField.get(fieldName);
                // if this field is defined by one of the superclasses, then 
                // we need to provide a mapping for this. 
                if (superclass != null) {
                    // we need to mark this as being in one of the superclasses 
                    superclass.addField(new Basic(fieldName));
                    mapping.addField(new AttributeOverride(fieldName));
                }
                else {
                    // directly generated. 
                    mapping.addField(new Basic(fieldName));
                }
            }
        }
        // all of the fields should now be identified by type, so return a set of 
        // the field mappings 
        return new HashSet<MappedSuperclass>(superclassByField.values());
    }

    
    /**
     * Create the class mapping for a CMP 1.x entity bean. 
     * Since the fields for 1.x persistence are defined 
     * in the objects directly, we need to create superclass 
     * mappings for each of the defined fields to identify 
     * which classes implement each of the managed fields. 
     * 
     * @param ejbClassName
     *                The name of the class we're processing.
     * @param mapping The mappings we're going to generate.
     * @param bean    The bean metadata for the ejb.
     * @param classLoader
     *                The classloader used to load the bean class for
     *                inspection.
     * 
     * @return The set of mapped superclasses used in this 
     *         bean mapping.
     */
    private Collection<MappedSuperclass> mapClass1x(String ejbClassName, Mapping mapping, EntityBean bean, ClassLoader classLoader) {
        Class ejbClass = loadClass(classLoader, ejbClassName);

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
            // a primary field type of Object is an automatically generated 
            // pk field.  Mark it as such and add it to the mapping.  
            String fieldName = "OpenEJB_pk";
            Id field = new Id(fieldName);
            field.setGeneratedValue(new GeneratedValue(GenerationType.AUTO));
            mapping.addField(field);
        } else if (bean.getPrimKeyClass() != null) {
            // we have a primary key class.  We need to define the mappings between the key class fields 
            // and the bean's managed fields. 
            
            Class<?> pkClass = null;
            try {
                pkClass = classLoader.loadClass(bean.getPrimKeyClass());
                MappedSuperclass superclass = null;
                MappedSuperclass idclass = null; 
                for (java.lang.reflect.Field pkField : pkClass.getFields()) {
                    String fieldName = pkField.getName();
                    int modifiers = pkField.getModifiers();
                    // the primary key fields must be public, non-static, must be defined as a CMP field, 
                    // AND must also exist in the class hierarchy (not enforced by mapFields()); 
                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) && allFields.contains(fieldName)) {
                        superclass = superclassByField.get(fieldName);
                        if (superclass == null) {
                            throw new IllegalStateException("Primary key field " + fieldName + " is not defined in class " + ejbClassName + " or any super classes");
                        }
                        // these are defined ast ID fields because they are part of the primary key 
                        superclass.addField(new Id(fieldName));
                        mapping.addField(new AttributeOverride(fieldName));
                        primaryKeyFields.add(fieldName);
                        idclass = resolveIdClass(idclass, superclass, ejbClass); 
                    }
                }
                
                // if we've located an ID class, set it as such 
                if (idclass != null) {
                    idclass.setIdClass(new IdClass(bean.getPrimKeyClass()));
                }
            } catch (ClassNotFoundException e) {
                throw (IllegalStateException)new IllegalStateException("Could not find entity primary key class " + bean.getPrimKeyClass()).initCause(e);
            }
        }

        //
        // basic: cmp-fields
        //
        for (CmpField cmpField : bean.getCmpField()) {
            String fieldName = cmpField.getFieldName();
            // all of the primary key fields have been processed, so only handle whatever is left over 
            if (!primaryKeyFields.contains(fieldName)) {
                MappedSuperclass superclass = superclassByField.get(fieldName);
                if (superclass == null) {
                    throw new IllegalStateException("CMP field " + fieldName + " is not defined in class " + ejbClassName + " or any super classes");
                }
                superclass.addField(new Basic(fieldName));
                mapping.addField(new AttributeOverride(fieldName));
            }
        }

        // all of the fields should now be identified by type, so return a set of 
        // the field mappings 
        return new HashSet<MappedSuperclass>(superclassByField.values());
    }
    
    
    /**
     * Handle the potential situation where the fields 
     * of a complex primary key are defined at different 
     * levels of the class hierarchy.  We want to define
     * the idClass as the most derived class (i.e., the one 
     * that will contain ALL of the defined fields). 
     * 
     * In practice, most ejbs will define all of the 
     * primary key fields at the same subclass level, so 
     * this should return quickly. 
     * 
     * @param idclass  The currently defined id class (will be null if
     *                 this is the first call).
     * @param current  The current superclass being processed.
     * @param ejbClass The ejbClass we're creating the mapping for.
     * 
     * @return Either idClass or current, depending on which is 
     *         the most derived of the classes.
     */
    private MappedSuperclass resolveIdClass(MappedSuperclass idclass, MappedSuperclass current, Class ejbClass) 
    {
        // None identified yet?  Just use the one we just found 
        if (idclass == null) {
            return current; 
        }
        
        String idClassName = idclass.getClazz(); 
        String currentClassName = current.getClazz(); 
        
        // defined at the same level (common).  Just keep the same id class 
        if (idClassName.equals(currentClassName)) {
            return idclass; 
        }
        
        // we have a split across the hiearchy, we need to figure out which of the classes is 
        // the most derived 
        for (Class clazz = ejbClass; clazz != null; clazz = clazz.getSuperclass()) {
            String name = clazz.getName(); 
            // if we find the current one first, return it 
            if (name.equals(currentClassName)) {
                return current; 
            }
            else if (name.equals(idClassName)) {
                // keeping the same highest level 
                return idclass; 
            }
        }
        
        // this should never happen, but keep the same one if we ever reach here
        return idclass; 
    }
    

    private static Class loadClass(ClassLoader classLoader, String className) {
        Class ejbClass = null;
        try {
            ejbClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return ejbClass;
    }

    
    /**
     * Build a mapping between a bean's CMP fields and the 
     * particular subclass in the inheritance hierarchy that 
     * defines the field. 
     * 
     * @param clazz  The bean implementation class.
     * @param persistantFields
     *               The set of container-managed fields.
     * 
     * @return A map of fieldname-to-defining class relationships. 
     */
    private Map<String, MappedSuperclass> mapFields(Class clazz, Set<String> persistantFields) {
        persistantFields = new TreeSet<String>(persistantFields);
        Map<String,MappedSuperclass> fields = new TreeMap<String,MappedSuperclass>();
        
        // spin down the class hierarchy until we've either processed all of the fields
        // or we've reached the Object class. 
        while (!persistantFields.isEmpty() && !clazz.equals(Object.class)) {
            // This is a single target for the relationship mapping for each 
            // class in the hierarchy. 
            MappedSuperclass superclass = new MappedSuperclass(clazz.getName());
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();
                // if this is one of bean's persistence fields, create the mapping 
                if (persistantFields.contains(fieldName)) {
                    fields.put(fieldName, superclass);
                    persistantFields.remove(fieldName);
                } else if (!ENHANCED_FIELDS.contains(fieldName)){
                    // these are fields we need to identify as transient for the persistence engine. 
                    Transient transientField = new Transient(fieldName);
                    superclass.addField(transientField);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    
    /**
     * Add a persistence context reference for the CMP 
     * persistence contexts to this EntityBean definition. 
     * 
     * @param bean   The bean we're updating.
     * 
     * @return Returns true if the context was added.  Returns false if 
     *         the bean already is associated with the CMP persistence
     *         context.
     */
    private boolean addPersistenceContextRef(EntityBean bean) {
        // if a ref is already defined, skip this bean
        if (bean.getPersistenceContextRefMap().containsKey(JpaCmpEngine.CMP_PERSISTENCE_CONTEXT_REF_NAME)) return false;

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
