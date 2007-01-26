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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.TestCase;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.RelationshipRoleSource;
import org.apache.openejb.jee.Relationships;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.OpenejbJarType;

/**
 * @version $Rev$ $Date$
 */
public class Cmp2ConversionTest extends TestCase {
    public void testItests22() throws Exception {
        convert("itest-2.2-ejb-jar.xml", "openejb-jar-2.2.xml", "itest-2.2-orm.xml");
    }

    public void testDaytrader() throws Exception {
        convert("daytrader-ejb-jar.xml", "daytrader-corrected.xml", "daytrader-orm.xml");
    }

    public void testOneToOne() throws Exception {
        convert("oej2/cmp/onetoone/simplepk/");
    }

    public void testOneToOneUni() throws Exception {
        convert("oej2/cmp/onetoone/simplepk/unidirectional-");
    }

    public void testOneToMany() throws Exception {
        convert("oej2/cmp/onetomany/simplepk/ejb-jar.xml", "oej2/cmp/onetomany/simplepk/openejb-jar.xml", null);
    }

    public void testOneToManyUni() throws Exception {
        convert("oej2/cmp/onetomany/simplepk/one-unidirectional-");
    }

    public void testManyToOneUni() throws Exception {
        convert("oej2/cmp/onetomany/simplepk/many-unidirectional-");
    }

    public void testManyToMany() throws Exception {
        convert("oej2/cmp/manytomany/simplepk/");
    }

    public void testManyToManyUni() throws Exception {
        convert("oej2/cmp/manytomany/simplepk/unidirectional-");
    }

    private EntityMappings convert(String prefix) throws Exception {
        return convert(prefix + "ejb-jar.xml", prefix + "openejb-jar.xml", prefix + "orm.xml");
    }

    private EntityMappings convert(String ejbJarFileName, String openejbJarFileName, String expectedFileName) throws Exception {
        EntityMappings entityMappings = generateEntityMappings(ejbJarFileName);

        String openejbJarXml = readContent(getClass().getClassLoader().getResourceAsStream(openejbJarFileName));
        JAXBElement element = (JAXBElement) JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, new ByteArrayInputStream(openejbJarXml.getBytes()));
        OpenejbJarType openejbJarType = (OpenejbJarType) element.getValue();

        OpenEjb2CmpConversion openEjb2CmpConversion = new OpenEjb2CmpConversion();
        openEjb2CmpConversion.mergeEntityMappings(entityMappings, openejbJarType);

        if (expectedFileName != null) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(expectedFileName);
            String expected = readContent(in);
            String actual = toString(entityMappings);
            assertEquals(expected, actual);
        }
        return entityMappings;
    }


    private String toString(EntityMappings entityMappings) throws JAXBException {
        JAXBContext entityMappingsContext = JAXBContext.newInstance(EntityMappings.class);

        Marshaller marshaller = entityMappingsContext.createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(entityMappings, baos);

        String actual = new String(baos.toByteArray());
        return actual.trim();
    }

    private EntityMappings generateEntityMappings(String fileName) throws Exception {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        String expected = readContent(in);

        EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, new ByteArrayInputStream(expected.getBytes()));

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

        //
        // transient: non-persistent fields
        //
        // todo scan class file for fields that are not cmp-fields or cmr-fields and mark them transient
        return entityMappings;
    }

    private void setCascade(EjbRelationshipRole role, RelationField field) {
        if (role.getCascadeDelete()) {
            CascadeType cascadeType = new CascadeType();
            cascadeType.setCascadeAll(true);
            field.setCascade(cascadeType);
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
        return sb.toString().trim();
    }

}
