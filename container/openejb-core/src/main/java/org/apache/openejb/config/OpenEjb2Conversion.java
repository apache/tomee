/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config;

import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.jpa.AttributeOverride;
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
import org.apache.openejb.jee.oejb2.ActivationConfigPropertyType;
import org.apache.openejb.jee.oejb2.ActivationConfigType;
import org.apache.openejb.jee.oejb2.EjbLocalRefType;
import org.apache.openejb.jee.oejb2.EjbRefType;
import org.apache.openejb.jee.oejb2.EjbRelationType;
import org.apache.openejb.jee.oejb2.EjbRelationshipRoleType;
import org.apache.openejb.jee.oejb2.EntityBeanType;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.JaxbOpenejbJar2;
import org.apache.openejb.jee.oejb2.Jndi;
import org.apache.openejb.jee.oejb2.MessageDrivenBeanType;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.PatternType;
import org.apache.openejb.jee.oejb2.QueryType;
import org.apache.openejb.jee.oejb2.RpcBean;
import org.apache.openejb.jee.oejb2.SessionBeanType;
import org.apache.openejb.jee.oejb2.TssLinkType;
import org.apache.openejb.jee.oejb2.WebServiceBindingType;
import org.apache.openejb.jee.oejb2.WebServiceSecurityType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.EjbLink;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.IO;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class OpenEjb2Conversion implements DynamicDeployer {

    @Override
    public final AppModule deploy(final AppModule appModule) {
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final Object altDD = getOpenejbJarType(ejbModule);
            if (OpenejbJarType.class.isInstance(altDD)) {
                final OpenejbJarType openejbJarType = OpenejbJarType.class.cast(altDD);
                convertEjbRefs(ejbModule.getEjbJar(), ejbModule.getOpenejbJar(), openejbJarType);
                convertMdbConfigs(ejbModule.getEjbJar(), openejbJarType);
                mergeEntityMappings(ejbModule.getModuleId(), appModule.getCmpMappings(), ejbModule.getOpenejbJar(), openejbJarType);
            }
        }
        return appModule;
    }

    private OpenejbJarType getOpenejbJarType(final EjbModule ejbModule) {
        Object altDD = ejbModule.getAltDDs().get("openejb-jar.xml");
        if (altDD instanceof String) {
            try {
                altDD = JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, new ByteArrayInputStream(((String) altDD).getBytes()), false);
            } catch (final Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof URL) {
            try {
                altDD = JaxbOpenejbJar2.unmarshal(OpenejbJarType.class, IO.read((URL) altDD), false);
            } catch (final Exception e) {
                // todo warn about not being able to parse sun descriptor
            }
        }
        if (altDD instanceof JAXBElement) {
            altDD = ((JAXBElement) altDD).getValue();
        }
        if (altDD instanceof OpenejbJarType) {
            return (OpenejbJarType) altDD;
        }
        return null;
    }

    public final void convertEjbRefs(final EjbJar ejbJar, final OpenejbJar openejbJar, final OpenejbJarType openejbJarType) {

        openejbJar.getProperties().putAll(openejbJarType.getProperties());

        final Map<String, EnterpriseBean> ejbs = ejbJar.getEnterpriseBeansByEjbName();
        final Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

        for (final org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            final EnterpriseBean ejb = ejbs.get(enterpriseBean.getEjbName());
            if (ejb == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }
            final Map<String, EjbRef> ejbRefs = ejb.getEjbRefMap();

            final EjbDeployment deployment = deployments.get(enterpriseBean.getEjbName());
            if (deployment == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            // Add WS Security
            if (enterpriseBean instanceof SessionBeanType) {
                final SessionBeanType sessionBean = (SessionBeanType) enterpriseBean;
                final WebServiceSecurityType webServiceSecurityType = sessionBean.getWebServiceSecurity();

                if (webServiceSecurityType != null) {

                    if (webServiceSecurityType.getRealmName() != null) {
                        deployment.addProperty("webservice.security.realm", webServiceSecurityType.getRealmName());
                    }

                    if (webServiceSecurityType.getSecurityRealmName() != null) {
                        deployment.addProperty("webservice.security.securityRealm", webServiceSecurityType.getSecurityRealmName());
                    }

                    if (webServiceSecurityType.getTransportGuarantee() != null) {
                        deployment.addProperty("webservice.security.transportGarantee", webServiceSecurityType.getTransportGuarantee().value());
                    } else {
                        deployment.addProperty("webservice.security.transportGarantee", "NONE");
                    }

                    if (webServiceSecurityType.getAuthMethod() != null) {
                        deployment.addProperty("webservice.security.authMethod", webServiceSecurityType.getAuthMethod().value());
                    } else {
                        deployment.addProperty("webservice.security.authMethod", "NONE");
                    }

                    deployment.getProperties().putAll(webServiceSecurityType.getProperties());
                }

                if (sessionBean.getWebServiceAddress() != null) {
                    deployment.getProperties().put("openejb.webservice.deployment.address", sessionBean.getWebServiceAddress());
                }
            }

            deployment.getProperties().putAll(enterpriseBean.getProperties());

            for (final String name : enterpriseBean.getLocalJndiName()) {
                deployment.getJndi().add(new org.apache.openejb.jee.oejb3.Jndi(name, "LocalHome"));
            }

            for (final String name : enterpriseBean.getJndiName()) {
                deployment.getJndi().add(new org.apache.openejb.jee.oejb3.Jndi(name, "RemoteHome"));
            }

            for (final Jndi jndi : enterpriseBean.getJndi()) {
                deployment.getJndi().add(new org.apache.openejb.jee.oejb3.Jndi(jndi.getName(), jndi.getInterface()));
            }

            final Set<String> ejbLinks = new TreeSet<>();
            for (final EjbLink ejbLink : deployment.getEjbLink()) {
                ejbLinks.add(ejbLink.getEjbRefName());
            }

            for (final EjbRefType refType : enterpriseBean.getEjbRef()) {
                final String refName = refType.getRefName();
                if (ejbLinks.contains(refName)) {
                    // don't overwrite refs that have been already set
                    continue;
                }

                final String nsCorbaloc = refType.getNsCorbaloc();
                if (nsCorbaloc != null) {
                    final EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setMappedName("jndi:" + nsCorbaloc);
                    }
                } else if (refType.getEjbLink() != null) {
                    final EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setEjbLink(refType.getEjbLink());
                    }
                } else {
                    final PatternType pattern = refType.getPattern();
                    addEjbLink(deployment, refName, pattern);
                }
            }

            for (final EjbLocalRefType refType : enterpriseBean.getEjbLocalRef()) {
                final String refName = refType.getRefName();
                if (ejbLinks.contains(refName)) {
                    // don't overwrite refs that have been already set
                    continue;
                }

                if (refType.getEjbLink() != null) {
                    final EjbRef ref = ejbRefs.get(refName);
                    if (ref != null) {
                        ref.setEjbLink(refType.getEjbLink());
                    }
                } else {
                    final PatternType pattern = refType.getPattern();
                    addEjbLink(deployment, refName, pattern);
                }
            }
        }
    }

    private void addEjbLink(final EjbDeployment deployment, final String refName, final PatternType pattern) {
        String module = pattern.getModule();
        if (module == null) {
            module = pattern.getArtifactId();
        }
        final String ejbName = pattern.getName();
        final String deploymentId = module + "/" + ejbName;
        final EjbLink ejbLink = new EjbLink(refName, deploymentId);
        deployment.getEjbLink().add(ejbLink);
    }

    public final void convertMdbConfigs(final EjbJar ejbJar, final OpenejbJarType openejbJarType) {
        final Map<String, MessageDrivenBean> mdbs = new TreeMap<>();
        for (final EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof MessageDrivenBean)) {
                continue;
            }
            mdbs.put(enterpriseBean.getEjbName(), (MessageDrivenBean) enterpriseBean);
        }
        for (final org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof MessageDrivenBeanType)) {
                continue;
            }
            final MessageDrivenBeanType bean = (MessageDrivenBeanType) enterpriseBean;
            final MessageDrivenBean mdb = mdbs.get(bean.getEjbName());
            if (mdb == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }
            final ActivationConfigType activationConfigType = bean.getActivationConfig();
            if (activationConfigType != null) {
                ActivationConfig activationConfig = mdb.getActivationConfig();
                if (activationConfig == null) {
                    activationConfig = new ActivationConfig();
                    mdb.setActivationConfig(activationConfig);
                }
                for (final ActivationConfigPropertyType propertyType : activationConfigType.getActivationConfigProperty()) {
                    final ActivationConfigProperty property = new ActivationConfigProperty(
                        propertyType.getActivationConfigPropertyName(),
                        propertyType.getActivationConfigPropertyValue());
                    activationConfig.getActivationConfigProperty().add(property);
                }
            }
        }
    }

    public final void mergeEntityMappings(final String moduleId, final EntityMappings entityMappings, final OpenejbJar openejbJar, final OpenejbJarType openejbJarType) {
        final Map<String, EntityData> entities = new TreeMap<>();
        if (entityMappings != null) {
            for (final Entity entity : entityMappings.getEntity()) {
                try {
                    entities.put(entity.getDescription(), new EntityData(entity));
                } catch (final IllegalArgumentException e) {
                    LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
                }
            }
        }
        for (final org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof EntityBeanType)) {
                continue;
            }
            final EntityBeanType bean = (EntityBeanType) enterpriseBean;
            final EntityData entityData = entities.get(moduleId + "#" + bean.getEjbName());
            if (entityData == null) {
                // todo warn no such ejb in the ejb-jar.xml
                continue;
            }

            final Table table = new Table();
            table.setName(bean.getTableName());
            entityData.entity.setTable(table);

            for (final EntityBeanType.CmpFieldMapping cmpFieldMapping : bean.getCmpFieldMapping()) {
                final String cmpFieldName = cmpFieldMapping.getCmpFieldName();
                final Field field = entityData.fields.get(cmpFieldName);
                if (field == null) {
                    // todo warn no such cmp-field in the ejb-jar.xml
                    continue;
                }
                final Column column = new Column();
                column.setName(cmpFieldMapping.getTableColumn());
                field.setColumn(column);
            }

            if (bean.getKeyGenerator() != null) {
                // todo support complex primary keys
                final Attributes attributes = entityData.entity.getAttributes();
                if (attributes != null && attributes.getId().size() == 1) {
                    final Id id = attributes.getId().get(0);

                    // todo detect specific generation strategy
                    id.setGeneratedValue(new GeneratedValue(GenerationType.IDENTITY));
                }
            }

            for (final QueryType query : bean.getQuery()) {
                final NamedQuery namedQuery = new NamedQuery();
                final QueryType.QueryMethod queryMethod = query.getQueryMethod();

                // todo deployment id could change in one of the later conversions... use entity name instead, but we need to save it off
                final StringBuilder name = new StringBuilder();
                name.append(entityData.entity.getName()).append(".").append(queryMethod.getMethodName());
                if (queryMethod.getMethodParams() != null && !queryMethod.getMethodParams().getMethodParam().isEmpty()) {
                    name.append('(');
                    boolean first = true;
                    for (final String methodParam : queryMethod.getMethodParams().getMethodParam()) {
                        if (!first) {
                            name.append(",");
                        }
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

        for (final EjbRelationType relation : openejbJarType.getEjbRelation()) {
            final List<EjbRelationshipRoleType> roles = relation.getEjbRelationshipRole();
            if (roles.isEmpty()) {
                continue;
            }

            if (relation.getManyToManyTableName() == null) {
                final EjbRelationshipRoleType leftRole = roles.get(0);
                final EjbRelationshipRoleType.RelationshipRoleSource leftRoleSource = leftRole.getRelationshipRoleSource();
                final String leftEjbName = leftRoleSource == null ? null : leftRoleSource.getEjbName();
                final EntityData leftEntityData = entities.get(moduleId + "#" + leftEjbName);
                final EjbRelationshipRoleType.CmrField cmrField = leftRole.getCmrField();

                final String leftFieldName = null != cmrField ? cmrField.getCmrFieldName() : null;

                RelationField field;
                if (leftRole.isForeignKeyColumnOnSource()) {

                    field = null != leftFieldName && null != leftEntityData ? leftEntityData.relations.get(leftFieldName) : null;

                    // todo warn field not found
                    if (field == null) {
                        continue;
                    }
                } else {
                    final RelationField other = null != leftFieldName && null != leftEntityData ? leftEntityData.relations.get(leftFieldName) : null;
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
                    final OneToOne left = (OneToOne) field;
                    final OneToOne right = (OneToOne) left.getRelatedField();
                    if (right != null) {
                        left.setMappedBy(null);
                        right.setMappedBy(left.getName());
                    }

                }
                final EjbRelationshipRoleType.RoleMapping roleMapping = leftRole.getRoleMapping();
                for (final EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                    final JoinColumn joinColumn = new JoinColumn();
                    joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                    joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                    field.getJoinColumn().add(joinColumn);
                }
            } else {
                final JoinTable joinTable = new JoinTable();
                joinTable.setName(relation.getManyToManyTableName());

                //
                // left
                final EjbRelationshipRoleType leftRole = roles.get(0);
                RelationField left = null;
                if (leftRole.getRelationshipRoleSource() != null) {
                    final String leftEjbName = leftRole.getRelationshipRoleSource().getEjbName();
                    final EntityData leftEntityData = entities.get(moduleId + "#" + leftEjbName);
                    if (leftEntityData == null) {
                        // todo warn no such entity in ejb-jar.xml
                        continue;
                    }
                    final EjbRelationshipRoleType.CmrField lcf = leftRole.getCmrField();
                    left = (null != lcf ? leftEntityData.relations.get(lcf.getCmrFieldName()) : null);
                }

                if (left != null) {
                    left.setJoinTable(joinTable);

                    final EjbRelationshipRoleType.RoleMapping roleMapping = leftRole.getRoleMapping();
                    for (final EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        final JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                        joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                        joinTable.getJoinColumn().add(joinColumn);
                    }
                }

                //
                // right
                if (roles.size() > 1) {
                    final EjbRelationshipRoleType rightRole = roles.get(1);

                    // if there wasn't a left cmr field, find the field for the right, so we can add the join table to it
                    if (left == null) {

                        final EjbRelationshipRoleType.CmrField rcf = rightRole.getCmrField();

                        if (rcf == null) {
                            // todo warn no cmr field declared for either role
                            continue;
                        } else if (rightRole.getRelationshipRoleSource() != null) {
                            final String rightEjbName = rightRole.getRelationshipRoleSource().getEjbName();
                            final EntityData rightEntityData = entities.get(moduleId + "#" + rightEjbName);

                            if (rightEntityData == null) {
                                // todo warn no such entity in ejb-jar.xml
                                continue;
                            }

                            final RelationField right = rightEntityData.relations.get(rcf.getCmrFieldName());
                            right.setJoinTable(joinTable);
                        }

                    }

                    final EjbRelationshipRoleType.RoleMapping roleMapping = rightRole.getRoleMapping();
                    for (final EjbRelationshipRoleType.RoleMapping.CmrFieldMapping cmrFieldMapping : roleMapping.getCmrFieldMapping()) {
                        final JoinColumn joinColumn = new JoinColumn();
                        joinColumn.setName(cmrFieldMapping.getForeignKeyColumn());
                        joinColumn.setReferencedColumnName(cmrFieldMapping.getKeyColumn());
                        joinTable.getInverseJoinColumn().add(joinColumn);
                    }
                }
            }
        }
    }

    /**
     * Actually called from ReadDescriptors as Geronimo needs this info early
     *
     * @param o2 OpenejbJarType
     * @return GeronimoEjbJarType
     */
    public static GeronimoEjbJarType convertToGeronimoOpenejbXml(final OpenejbJarType o2) {
        final GeronimoEjbJarType g2 = new GeronimoEjbJarType();

        g2.setEnvironment(o2.getEnvironment());
        g2.setSecurity(o2.getSecurity());
        g2.getService().addAll(o2.getService());
        g2.getMessageDestination().addAll(o2.getMessageDestination());
        g2.getPersistence().addAll(o2.getPersistence());

        for (final org.apache.openejb.jee.oejb2.EnterpriseBean bean : o2.getEnterpriseBeans()) {
            g2.getAbstractNamingEntry().addAll(bean.getAbstractNamingEntry());
            g2.getPersistenceContextRef().addAll(bean.getPersistenceContextRef());
            g2.getPersistenceUnitRef().addAll(bean.getPersistenceUnitRef());
            g2.getEjbLocalRef().addAll(bean.getEjbLocalRef());
            g2.getEjbRef().addAll(bean.getEjbRef());
            g2.getResourceEnvRef().addAll(bean.getResourceEnvRef());
            g2.getResourceRef().addAll(bean.getResourceRef());
            g2.getServiceRef().addAll(bean.getServiceRef());

            if (bean instanceof RpcBean) {
                final RpcBean rpcBean = (RpcBean) bean;
                if (rpcBean.getTssLink() != null) {
                    g2.getTssLink().add(new TssLinkType(rpcBean.getEjbName(), rpcBean.getTssLink(), rpcBean.getJndiName()));
                }
            }

            if (bean instanceof SessionBeanType) {
                final SessionBeanType sb = (SessionBeanType) bean;
                final WebServiceBindingType b = new WebServiceBindingType();
                b.setEjbName(sb.getEjbName());
                b.setWebServiceAddress(sb.getWebServiceAddress());
                b.setWebServiceVirtualHost(sb.getWebServiceVirtualHost());
                b.setWebServiceSecurity(sb.getWebServiceSecurity());
                if (b.containsData()) {
                    g2.getWebServiceBinding().add(b);
                }
            }
        }
        return g2;
    }

    private static class EntityData {

        private final Entity entity;
        private final Map<String, Field> fields = new TreeMap<>();
        private final Map<String, RelationField> relations = new TreeMap<>();

        public EntityData(final Entity e) {

            this.entity = e;

            if (this.entity == null) {
                throw new IllegalArgumentException("entity is null");
            }

            final Attributes attributes = this.entity.getAttributes();

            if (attributes != null) {
                for (final Id id : attributes.getId()) {
                    this.fields.put(id.getName(), id);
                }

                for (final Basic basic : attributes.getBasic()) {
                    this.fields.put(basic.getName(), basic);
                }

                for (final RelationField relationField : attributes.getOneToOne()) {
                    this.relations.put(relationField.getName(), relationField);
                }

                for (final RelationField relationField : attributes.getOneToMany()) {
                    this.relations.put(relationField.getName(), relationField);
                }

                for (final RelationField relationField : attributes.getManyToOne()) {
                    this.relations.put(relationField.getName(), relationField);
                }

                for (final RelationField relationField : attributes.getManyToMany()) {
                    this.relations.put(relationField.getName(), relationField);
                }
            }

            for (final AttributeOverride attributeOverride : this.entity.getAttributeOverride()) {
                this.fields.put(attributeOverride.getName(), attributeOverride);
            }
        }
    }
}
