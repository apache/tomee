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

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEvent;

import junit.framework.TestCase;
import org.apache.openejb.jee.oej2.OpenejbJarType;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.RelationshipRoleSource;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.PersistenceType;

/**
 * @version $Rev$ $Date$
 */
public class Cmp2ConversionTest extends TestCase {

    public void testEjbJarDoc() throws Exception {
        marshalAndUnmarshal(EjbJar.class, "ejb-jar-cmp-example1.xml");
    }

    public void testConversion() throws Exception {
        EntityMappings entityMappings = generateEntityMappings("daytrader-ejb-jar.xml");

        JAXBContext ctx = JAXBContext.newInstance(OpenejbJarType.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("daytrader-corrected.xml");
        String expected = readContent(in);

        JAXBElement element = (JAXBElement) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
        OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();

        OpenEjb2CmpConversion openEjb2CmpConversion = new OpenEjb2CmpConversion();
        openEjb2CmpConversion.mergeEntityMappings(entityMappings, openejbJarType);
        String actual = toString(entityMappings);
        System.out.println(actual);
    }


    private String toString(EntityMappings entityMappings) throws JAXBException {
        JAXBContext entityMappingsContext = JAXBContext.newInstance(EntityMappings.class);

        Marshaller marshaller = entityMappingsContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(entityMappings, baos);

        String actual = new String(baos.toByteArray());
        return actual;
    }

    private EntityMappings generateEntityMappings(String fileName) throws IOException, JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(EjbJar.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        String expected = readContent(in);
        EjbJar ejbJar = (EjbJar) unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));

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
            // id: the primary key
            //
            Id id = new Id();
            // todo complex primary key
            // todo unknown primary key
            id.setName(bean.getPrimkeyField());
            attributes.getId().add(id);

            //
            // basic: cmp-fields
            //
            for (CmpField cmpField : bean.getCmpField()) {
                Basic basic = new Basic();
                if (!cmpField.getFieldName().equals(bean.getPrimkeyField())) {
                    basic.setName(cmpField.getFieldName());
                    attributes.getBasic().add(basic);
                }
            }

            // add the entity
            entityMappings.getEntity().add(entity);
            entitiesByName.put(bean.getEjbName(), entity);
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
                boolean leftCascade = leftRole.getCascadeDelete() != null;
                boolean leftIsOne = leftRole.getMultiplicity() == Multiplicity.ONE;

                EjbRelationshipRole rightRole = roles.get(1);
                RelationshipRoleSource rightRoleSource = rightRole.getRelationshipRoleSource();
                String rightEjbName = rightRoleSource == null ? null : rightRoleSource.getEjbName();
                Entity rightEntity = entitiesByName.get(rightEjbName);
                String rightFieldName = null;
                if (rightRole.getCmrField() != null) {
                    rightFieldName = rightRole.getCmrField().getCmrFieldName();
                }
                boolean rightCascade = rightRole.getCascadeDelete() != null;
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
                        if (leftCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            leftOneToOne.setCascade(cascadeType);
                        }
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
                        if (rightCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            rightOneToOne.setCascade(cascadeType);
                        }
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
                        if (leftCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            leftOneToMany.setCascade(cascadeType);
                        }
                        leftEntity.getAttributes().getOneToMany().add(leftOneToMany);
                    }

                    // right
                    ManyToOne rightManyToOne = null;
                    if (rightFieldName != null) {
                        rightManyToOne = new ManyToOne();
                        rightManyToOne.setName(rightFieldName);
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
                        if (rightCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            rightOneToMany.setCascade(cascadeType);
                        }
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
                        if (leftCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            leftManyToMany.setCascade(cascadeType);
                        }
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
                        if (rightCascade) {
                            CascadeType cascadeType = new CascadeType();
                            cascadeType.setCascadeAll(true);
                            rightManyToMany.setCascade(cascadeType);
                        }
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

        //
        // transient: non-persistent fields
        //
        // todo scan class file for fields that are not cmp-fields or cmr-fields and mark them transient
        return entityMappings;
    }

    private <T> void marshalAndUnmarshal(Class<T> type, String xmlFileName) throws JAXBException, IOException {
        JAXBContext ctx = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        InputStream in = this.getClass().getClassLoader().getResourceAsStream(xmlFileName);
        String expected = readContent(in);

        Object object = unmarshaller.unmarshal(new ByteArrayInputStream(expected.getBytes()));
//        JAXBElement element =  (JAXBElement) object;
        unmarshaller.setEventHandler(new TestValidationEventHandler());
//        T app = (T) element.getValue();
//        System.out.println("unmarshalled");

        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);

        String actual = new String(baos.toByteArray());

        assertEquals(expected, actual);
    }

    private static class TestValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent validationEvent) {
            System.out.println(validationEvent.getMessage());
            return true;
        }
    }
    private String readContent(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        in = new BufferedInputStream(in);
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString();
    }

}
