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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.lang.reflect.Field;

import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.RelationshipRoleSource;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.Entity;
import org.apache.openejb.jee.jpa.Attributes;
import org.apache.openejb.jee.jpa.Id;
import org.apache.openejb.jee.jpa.Basic;
import org.apache.openejb.jee.jpa.OneToOne;
import org.apache.openejb.jee.jpa.OneToMany;
import org.apache.openejb.jee.jpa.ManyToOne;
import org.apache.openejb.jee.jpa.ManyToMany;
import org.apache.openejb.jee.jpa.RelationField;
import org.apache.openejb.jee.jpa.CascadeType;
import org.apache.openejb.jee.jpa.Transient;

public class CmpJpaConversion {
    public EntityMappings generateEntityMappings(EjbJar ejbJar, ClassLoader classLoader) {
        EntityMappings entityMappings = new EntityMappings();
        Map<String, Entity> entitiesByName = new HashMap<String,Entity>();
        for (org.apache.openejb.jee.EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            // skip all non-CMP beans
            if (!(enterpriseBean instanceof EntityBean) ||
                    ((EntityBean) enterpriseBean).getPersistenceType() != PersistenceType.CONTAINER) {
                continue;
            }
            EntityBean bean = (EntityBean) enterpriseBean;

            Entity entity = new Entity();

            // description: contains the name of the entity bean
            entity.setDescription(bean.getEjbName());

            // class: the java class for the entity
            entity.setClazz(bean.getEjbClass());

            // name: the name of the entity in queries
            if (bean.getAbstractSchemaName() != null) {
                entity.setName(bean.getAbstractSchemaName());
            } else {
                String name = bean.getEjbName().trim().replaceAll("[ \\t\\n\\r]+", "_");
                entity.setName(name);
            }

            //
            // atributes: holds id, basic, oneToMany, manyToOne and manyToMany
            //
            Attributes attributes = new Attributes();
            entity.setAttributes(attributes);

            //
            // Map fields remembering the names of the mapped fields
            //
            Set<String> persistantFields = new TreeSet<String>();

            //
            // id: the primary key
            //
            if (bean.getPrimkeyField() != null) {
                Id id = new Id();
                id.setName(bean.getPrimkeyField());
                attributes.getId().add(id);
                persistantFields.add(bean.getPrimkeyField());
            } else {
                // todo complex primary key
                // todo unknown primary key                
            }

            //
            // basic: cmp-fields
            //
            for (CmpField cmpField : bean.getCmpField()) {
                Basic basic = new Basic();
                if (!cmpField.getFieldName().equals(bean.getPrimkeyField())) {
                    basic.setName(cmpField.getFieldName());
                    attributes.getBasic().add(basic);
                    persistantFields.add(cmpField.getFieldName());
                }
            }

            // add the entity
            entityMappings.getEntity().add(entity);
            entitiesByName.put(bean.getEjbName(), entity);

            // add the persistence-context-ref
            addPersistenceContextRef(bean);

            //
            // transient: non-persistent fields
            //
            if (classLoader != null) {
                String ejbClassName = bean.getEjbClass();
                try {
                    Class ejbClass = classLoader.loadClass(ejbClassName);
                    for (Field field : ejbClass.getDeclaredFields()) {
                        if (!persistantFields.contains(field.getName())) {
                            Transient transientField = new Transient();
                            transientField.setName(field.getName());
                            attributes.getTransient().add(transientField);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // todo warn
                }
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

                EjbRelationshipRole leftRole = roles.get(0);
                RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
                String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
                Entity leftEntity = entitiesByName.get(leftEjbName);
                String leftFieldName = null;
                if (leftRole.getCmrField() != null) {
                    leftFieldName = leftRole.getCmrField().getCmrFieldName();
                }
                boolean leftIsOne = leftRole.getMultiplicity() == Multiplicity.ONE;

                EjbRelationshipRole rightRole = roles.get(1);
                RelationshipRoleSource rightRoleSource = rightRole.getRelationshipRoleSource();
                String rightEjbName = rightRoleSource == null ? null : rightRoleSource.getEjbName();
                Entity rightEntity = entitiesByName.get(rightEjbName);
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


        return entityMappings;
    }

    private void addPersistenceContextRef(EntityBean bean) {
        for (PersistenceContextRef ref : bean.getPersistenceContextRef()) {
            // if a ref is already defined, skip this bean
            if (ref.getName().equals("openejb/cmp")) return;
        }
        PersistenceContextRef persistenceContextRef = new PersistenceContextRef();
        persistenceContextRef.setName("openejb/cmp");
        persistenceContextRef.setPersistenceUnitName("cmp");
        bean.getPersistenceContextRef().add(persistenceContextRef);
    }

    private void setCascade(EjbRelationshipRole role, RelationField field) {
        if (role.getCascadeDelete()) {
            CascadeType cascadeType = new CascadeType();
            cascadeType.setCascadeAll(true);
            field.setCascade(cascadeType);
        }
    }
}
