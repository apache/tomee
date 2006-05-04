/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamTiger;
import com.thoughtworks.xstream.mapper.Mapper;
//import com.thoughtworks.xstream.XStreamTiger;
import com.thoughtworks.xstream.converters.enums.CamelcaseEnumFormat;
//import com.thoughtworks.xstream.converters.enums.CamelcaseEnumFormat;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.ConditionalConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import org.openejb.jee.javaee.AbstractEjbRef;
import org.openejb.jee.javaee.EjbLocalRef;
import org.openejb.jee.javaee.EjbRef;
import org.openejb.jee.javaee.EnvEntry;
import org.openejb.jee.javaee.Icon;
import org.openejb.jee.javaee.InitParam;
import org.openejb.jee.javaee.InjectionTarget;
import org.openejb.jee.javaee.JndiEnvironmentRef;
import org.openejb.jee.javaee.LifecycleCallback;
import org.openejb.jee.javaee.MessageDestination;
import org.openejb.jee.javaee.MessageDestinationRef;
import org.openejb.jee.javaee.ParamValue;
import org.openejb.jee.javaee.PersistenceContextRef;
import org.openejb.jee.javaee.PersistenceProperty;
import org.openejb.jee.javaee.PersistenceUnitRef;
import org.openejb.jee.javaee.PostConstruct;
import org.openejb.jee.javaee.PreDestroy;
import org.openejb.jee.javaee.Property;
import org.openejb.jee.javaee.ResourceEnvRef;
import org.openejb.jee.javaee.ResourceRef;
import org.openejb.jee.javaee.RunAs;
import org.openejb.jee.javaee.SecurityRole;
import org.openejb.jee.javaee.SecurityRoleRef;
import org.openejb.jee.webservice.Handler;
import org.openejb.jee.webservice.HandlerChain;
import org.openejb.jee.webservice.ServiceRef;
import org.openejb.jee.webservice.Target;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarMarshaller {


    public EjbJarMarshaller() {
    }

//    public static void main(String[] args) throws Exception {
//        EjbJarMarshaller marshaller = new EjbJarMarshaller();
//
//        XStream xstream = marshaller.getEjb30XStream();
//
//        Object vector = xstream.fromXML(new FileInputStream("/Users/dblevins/work/openejb3/openejb-itests/src/main/resources/META-INF/ejb-jar.xml"));
//        System.out.println("vector = " + vector);
//    }

    public static class SkipEmptyCollectionConverter extends CollectionConverter implements ConditionalConverter {
        public SkipEmptyCollectionConverter(Mapper mapper) {
            super(mapper);
        }

        public boolean shouldConvert(Class type, Object value) {
            return (value != null && ((Collection) value).size() != 0);
        }
    }

    public static class SkipEmptyMapConverter extends MapConverter implements ConditionalConverter {
        public SkipEmptyMapConverter(Mapper mapper) {
            super(mapper);
        }

        public boolean shouldConvert(Class type, Object value) {
            return (value != null && ((Map) value).size() != 0);
        }
    }
    public XStream getEjb30XStream() {
//        XStream xstream = new XStream(new PureJavaReflectionProvider());
        XStreamTiger xstream = new XStreamTiger(new PureJavaReflectionProvider());
        xstream.setDefaultEnumFormat(new CamelcaseEnumFormat());
        xstream.registerConverter(new SkipEmptyCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new SkipEmptyMapConverter(xstream.getMapper()));

        //--------  EjbJar -------

        xstream.alias("ejb-jar", EjbJar.class);
        xstream.addImplicitCollection(EjbJar.class, "description", "description", String.class);
        xstream.addImplicitCollection(EjbJar.class, "displayName", "display-name", String.class);
        xstream.addImplicitCollection(EjbJar.class, "icons");
        xstream.aliasField("enterprise-beans", EjbJar.class, "enterpriseBeans");
        xstream.aliasField("interceptors", EjbJar.class, "interceptors");
        xstream.aliasField("relationships", EjbJar.class, "relationships");
        xstream.aliasField("assembly-descriptor", EjbJar.class, "assemblyDescriptor");
        xstream.aliasField("ejb-client-jar", EjbJar.class, "ejbClientJar");
        xstream.aliasField("version", EjbJar.class, "version");
        xstream.aliasField("metadata-complete", EjbJar.class, "metadataComplete");
        xstream.aliasField("id", EjbJar.class, "id");

        //--------  Icon -------

        xstream.alias("icon", Icon.class);
        xstream.aliasField("id", Icon.class, "id");
        xstream.aliasField("small-icon", Icon.class, "smallIcon");
        xstream.aliasField("large-icon", Icon.class, "largeIcon");

        //--------  EnterpriseBean -------

        xstream.alias("enterprise-bean", EnterpriseBean.class);
        xstream.aliasField("id", EnterpriseBean.class, "id");
        xstream.addImplicitCollection(EnterpriseBean.class, "description", "description", String.class);
        xstream.addImplicitCollection(EnterpriseBean.class, "displayName", "display-name", String.class);
        xstream.addImplicitCollection(EnterpriseBean.class, "icons");
        xstream.aliasField("ejb-name", EnterpriseBean.class, "ejbName");
        xstream.aliasField("mapped-name", EnterpriseBean.class, "mappedName");
        xstream.aliasField("home", EnterpriseBean.class, "home");
        xstream.aliasField("remote", EnterpriseBean.class, "remote");
        xstream.aliasField("local-home", EnterpriseBean.class, "localHome");
        xstream.aliasField("local", EnterpriseBean.class, "local");
        xstream.aliasField("ejb-class", EnterpriseBean.class, "ejbClass");
        xstream.addImplicitCollection(EnterpriseBean.class, "envEntries");
        xstream.addImplicitCollection(EnterpriseBean.class, "ejbRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "ejbLocalRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "serviceRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "resourceEnvRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "resourceRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "messageDestinationRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "persistenceContextRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "persistenceUnitRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "jndiEnvironmentRefs");
        xstream.addImplicitCollection(EnterpriseBean.class, "postConstructs");
        xstream.addImplicitCollection(EnterpriseBean.class, "preDestroys");
        xstream.addImplicitCollection(EnterpriseBean.class, "securityRoleRefs");
        xstream.aliasField("security-identity", EnterpriseBean.class, "securityIdentity");

        //--------  JndiEnvironmentRef -------

        xstream.alias("jndi-environment-ref", JndiEnvironmentRef.class);
        xstream.aliasField("id", JndiEnvironmentRef.class, "id");
        xstream.addImplicitCollection(JndiEnvironmentRef.class, "description", "description", String.class);
        xstream.aliasField("mapped-name", JndiEnvironmentRef.class, "mappedName");
        xstream.addImplicitCollection(JndiEnvironmentRef.class, "injectionTargets");

        //--------  InjectionTarget -------

        xstream.alias("injection-target", InjectionTarget.class);
        xstream.aliasField("injection-target-class", InjectionTarget.class, "injectionTargetClass");
        xstream.aliasField("injection-target-name", InjectionTarget.class, "injectionTargetName");

        //--------  EnvEntry -------

        xstream.alias("env-entry", EnvEntry.class);
        xstream.aliasField("env-entry-name", EnvEntry.class, "envEntryName");
        xstream.aliasField("env-entry-type", EnvEntry.class, "envEntryType");
        xstream.aliasField("env-entry-value", EnvEntry.class, "envEntryValue");

        //--------  AbstractEjbRef -------

        xstream.alias("abstract-ejb-ref", AbstractEjbRef.class);
        xstream.aliasField("ejb-ref-name", AbstractEjbRef.class, "ejbRefName");
        xstream.aliasField("ejb-ref-type", AbstractEjbRef.class, "ejbRefType");
        xstream.aliasField("local-home", AbstractEjbRef.class, "localHome");
        xstream.aliasField("local", AbstractEjbRef.class, "local");
        xstream.aliasField("ejb-link", AbstractEjbRef.class, "ejbLink");

        //--------  EjbRef -------

        xstream.alias("ejb-ref", EjbRef.class);
        xstream.aliasField("home", EjbRef.class, "home");
        xstream.aliasField("remote", EjbRef.class, "remote");

        //--------  EjbLocalRef -------

        xstream.alias("ejb-local-ref", EjbLocalRef.class);
        xstream.aliasField("local-home", EjbLocalRef.class, "localHome");
        xstream.aliasField("local", EjbLocalRef.class, "local");

        //--------  ServiceRef -------

        xstream.alias("service-ref", ServiceRef.class);
        xstream.addImplicitCollection(ServiceRef.class, "displayName", "display-name", String.class);
        xstream.addImplicitCollection(ServiceRef.class, "icons");
        xstream.aliasField("service-ref-name", ServiceRef.class, "serviceRefName");
        xstream.aliasField("service-interface", ServiceRef.class, "serviceInterface");
        xstream.aliasField("service-ref-type", ServiceRef.class, "serviceRefType");
        xstream.aliasField("wsdl-file", ServiceRef.class, "wsdlFile");
        xstream.aliasField("jaxrpc-mapping-file", ServiceRef.class, "jaxrpcMappingFile");
        xstream.aliasField("service-qname", ServiceRef.class, "serviceQname");
        xstream.aliasField("port-component-ref", ServiceRef.class, "portComponentRef");
        xstream.addImplicitCollection(ServiceRef.class, "handlers");
        xstream.addImplicitCollection(ServiceRef.class, "handlerChains");

        //--------  Handler -------

        xstream.alias("handler", Handler.class);
        xstream.aliasField("id", Handler.class, "id");
        xstream.addImplicitCollection(Handler.class, "description", "description", String.class);
        xstream.addImplicitCollection(Handler.class, "displayName", "display-name", String.class);
        xstream.addImplicitCollection(Handler.class, "icons");
        xstream.aliasField("handler-class", Handler.class, "handlerClass");
        xstream.addImplicitCollection(Handler.class, "initParams");
        xstream.addImplicitCollection(Handler.class, "soapHeaders");
        xstream.addImplicitCollection(Handler.class, "soapRoles", "soap-roles", String.class);
        xstream.addImplicitCollection(Handler.class, "portNames", "port-names", String.class);

        //--------  ParamValue -------

        xstream.alias("param-value", ParamValue.class);
        xstream.aliasField("id", ParamValue.class, "id");
        xstream.addImplicitCollection(ParamValue.class, "description", "description", String.class);
        xstream.aliasField("param-name", ParamValue.class, "paramName");
        xstream.aliasField("param-type", ParamValue.class, "paramType");

        //--------  InitParam -------

        xstream.alias("init-param", InitParam.class);

        //--------  HandlerChain -------

        xstream.alias("handler-chain", HandlerChain.class);
        xstream.aliasField("id", HandlerChain.class, "id");
        xstream.aliasField("target", HandlerChain.class, "target");
        xstream.addImplicitCollection(HandlerChain.class, "handlers");

        //--------  Target -------

        xstream.alias("target", Target.class);
        xstream.aliasField("id", Target.class, "id");

        //--------  ResourceEnvRef -------

        xstream.alias("resource-env-ref", ResourceEnvRef.class);
        xstream.aliasField("resource-env-ref-name", ResourceEnvRef.class, "resourceEnvRefName");
        xstream.aliasField("resource-env-ref-type", ResourceEnvRef.class, "resourceEnvRefType");

        //--------  ResourceRef -------

        xstream.alias("resource-ref", ResourceRef.class);
        xstream.aliasField("res-ref-name", ResourceRef.class, "resRefName");
        xstream.aliasField("res-type", ResourceRef.class, "resType");
        xstream.aliasField("res-auth", ResourceRef.class, "resAuth");
        xstream.aliasField("resource-sharing-scope", ResourceRef.class, "resourceSharingScope");

        //--------  MessageDestinationRef -------

        xstream.alias("message-destination-ref", MessageDestinationRef.class);
        xstream.aliasField("message-destination-ref-name", MessageDestinationRef.class, "messageDestinationRefName");
        xstream.aliasField("message-destination-type", MessageDestinationRef.class, "messageDestinationType");
        xstream.aliasField("message-destination-usage", MessageDestinationRef.class, "messageDestinationUsage");
        xstream.aliasField("message-destination-link", MessageDestinationRef.class, "messageDestinationLink");

        //--------  PersistenceContextRef -------

        xstream.alias("persistence-context-ref", PersistenceContextRef.class);
        xstream.aliasField("persistence-context-ref-name", PersistenceContextRef.class, "persistenceContextRefName");
        xstream.aliasField("persistence-unit-name", PersistenceContextRef.class, "persistenceUnitName");
        xstream.aliasField("persistence-context-type", PersistenceContextRef.class, "persistenceContextType");
        xstream.addImplicitCollection(PersistenceContextRef.class, "persistenceProperties");

        //--------  Property -------

        xstream.alias("property", Property.class);
        xstream.aliasField("id", Property.class, "id");
        xstream.aliasField("name", Property.class, "name");
        xstream.aliasField("value", Property.class, "value");

        //--------  PersistenceProperty -------

        xstream.alias("persistence-property", PersistenceProperty.class);

        //--------  PersistenceUnitRef -------

        xstream.alias("persistence-unit-ref", PersistenceUnitRef.class);
        xstream.aliasField("persistence-unit-ref-name", PersistenceUnitRef.class, "persistenceUnitRefName");
        xstream.aliasField("persistence-unit-name", PersistenceUnitRef.class, "persistenceUnitName");

        //--------  LifecycleCallback -------

        xstream.alias("lifecycle-callback", LifecycleCallback.class);
        xstream.aliasField("lifecycle-callback-class", LifecycleCallback.class, "lifecycleCallbackClass");
        xstream.aliasField("lifecycle-callback-method", LifecycleCallback.class, "lifecycleCallbackMethod");

        //--------  PostConstruct -------

        xstream.alias("post-construct", PostConstruct.class);

        //--------  PreDestroy -------

        xstream.alias("pre-destroy", PreDestroy.class);

        //--------  SecurityRoleRef -------

        xstream.alias("security-role-ref", SecurityRoleRef.class);
        xstream.aliasField("id", SecurityRoleRef.class, "id");
        xstream.addImplicitCollection(SecurityRoleRef.class, "description", "description", String.class);
        xstream.aliasField("role-name", SecurityRoleRef.class, "roleName");
        xstream.aliasField("role-link", SecurityRoleRef.class, "roleLink");

        //--------  SecurityIdentity -------

        xstream.alias("security-identity", SecurityIdentity.class);
        xstream.aliasField("id", SecurityIdentity.class, "id");
        xstream.aliasField("use-caller-identity", SecurityIdentity.class, "useCallerIdentity");
        xstream.aliasField("run-as", SecurityIdentity.class, "runAs");

        //--------  RunAs -------

        xstream.alias("run-as", RunAs.class);
        xstream.aliasField("id", RunAs.class, "id");
        xstream.addImplicitCollection(RunAs.class, "description", "description", String.class);
        xstream.aliasField("role-name", RunAs.class, "roleName");

        //--------  Interceptor -------

        xstream.alias("interceptor", Interceptor.class);
        xstream.aliasField("id", Interceptor.class, "id");
        xstream.addImplicitCollection(Interceptor.class, "description", "description", String.class);
        xstream.aliasField("interceptor-class", Interceptor.class, "interceptorClass");
        xstream.addImplicitCollection(Interceptor.class, "aroundInvokes");
        xstream.addImplicitCollection(Interceptor.class, "postActivates");
        xstream.addImplicitCollection(Interceptor.class, "prePassivates");
        xstream.addImplicitCollection(Interceptor.class, "envEntries");
        xstream.addImplicitCollection(Interceptor.class, "ejbRefs");
        xstream.addImplicitCollection(Interceptor.class, "ejbLocalRefs");
        xstream.addImplicitCollection(Interceptor.class, "serviceRefs");
        xstream.addImplicitCollection(Interceptor.class, "resourceEnvRefs");
        xstream.addImplicitCollection(Interceptor.class, "resourceRefs");
        xstream.addImplicitCollection(Interceptor.class, "messageDestinationRefs");
        xstream.addImplicitCollection(Interceptor.class, "persistenceContextRefs");
        xstream.addImplicitCollection(Interceptor.class, "persistenceUnitRefs");
        xstream.addImplicitCollection(Interceptor.class, "jndiEnvironmentRefs");
        xstream.addImplicitCollection(Interceptor.class, "postConstructs");
        xstream.addImplicitCollection(Interceptor.class, "preDestroys");

        //--------  AroundInvoke -------

        xstream.alias("around-invoke", AroundInvoke.class);
        xstream.aliasField("class-name", AroundInvoke.class, "className");
        xstream.aliasField("method-name", AroundInvoke.class, "methodName");

        //--------  PostActivate -------

        xstream.alias("post-activate", PostActivate.class);

        //--------  PrePassivate -------

        xstream.alias("pre-passivate", PrePassivate.class);

        //--------  Relationships -------

        xstream.alias("relationships", Relationships.class);
        xstream.aliasField("id", Relationships.class, "id");
        xstream.addImplicitCollection(Relationships.class, "description", "description", String.class);
        xstream.addImplicitCollection(Relationships.class, "ejbRelations");

        //--------  EjbRelation -------

        xstream.alias("ejb-relation", EjbRelation.class);
        xstream.aliasField("id", EjbRelation.class, "id");
        xstream.addImplicitCollection(EjbRelation.class, "description", "description", String.class);
        xstream.aliasField("ejb-relation-name", EjbRelation.class, "ejbRelationName");
        xstream.aliasField("ejb-relationship-role", EjbRelation.class, "ejbRelationshipRole");

        //--------  EjbRelationshipRole -------

        xstream.alias("ejb-relationship-role", EjbRelationshipRole.class);
        xstream.aliasField("id", EjbRelationshipRole.class, "id");
        xstream.aliasField("ejb-relationship-role-name", EjbRelationshipRole.class, "ejbRelationshipRoleName");
        xstream.aliasField("multiplicity", EjbRelationshipRole.class, "multiplicity");
        xstream.aliasField("cascade-delete", EjbRelationshipRole.class, "cascadeDelete");
        xstream.aliasField("relationship-role-source", EjbRelationshipRole.class, "relationshipRoleSource");
        xstream.aliasField("cmr-field", EjbRelationshipRole.class, "cmrField");

        //--------  RelationshipRoleSource -------

        xstream.alias("relationship-role-source", RelationshipRoleSource.class);
        xstream.aliasField("id", RelationshipRoleSource.class, "id");
        xstream.addImplicitCollection(RelationshipRoleSource.class, "description", "description", String.class);
        xstream.aliasField("ejb-name", RelationshipRoleSource.class, "ejbName");

        //--------  CmrField -------

        xstream.alias("cmr-field", CmrField.class);
        xstream.aliasField("id", CmrField.class, "id");
        xstream.addImplicitCollection(CmrField.class, "description", "description", String.class);
        xstream.aliasField("cmr-field-name", CmrField.class, "cmrFieldName");
        xstream.aliasField("cmr-field-type", CmrField.class, "cmrFieldType");

        //--------  AssemblyDescriptor -------

        xstream.alias("assembly-descriptor", AssemblyDescriptor.class);
        xstream.aliasField("id", AssemblyDescriptor.class, "id");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "securityRoles");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "methodPermissions");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "containerTransactions");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "interceptorBindings");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "messageDestinations");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "excludeLists");
        xstream.addImplicitCollection(AssemblyDescriptor.class, "applicationExceptions");

        //--------  SecurityRole -------

        xstream.alias("security-role", SecurityRole.class);
        xstream.aliasField("id", SecurityRole.class, "id");
        xstream.addImplicitCollection(SecurityRole.class, "description", "description", String.class);
        xstream.aliasField("role-name", SecurityRole.class, "roleName");

        //--------  MethodPermission -------

        xstream.alias("method-permission", MethodPermission.class);
        xstream.aliasField("id", MethodPermission.class, "id");
        xstream.addImplicitCollection(MethodPermission.class, "description", "description", String.class);
        xstream.addImplicitCollection(MethodPermission.class, "roleNames", "role-names", String.class);
        xstream.aliasField("unchecked", MethodPermission.class, "unchecked");

        //--------  ContainerTransaction -------

        xstream.alias("container-transaction", ContainerTransaction.class);
        xstream.aliasField("id", ContainerTransaction.class, "id");
        xstream.addImplicitCollection(ContainerTransaction.class, "description", "description", String.class);
        xstream.aliasField("method", ContainerTransaction.class, "method");
        xstream.aliasField("transaction-attribute", ContainerTransaction.class, "transactionAttribute");

        //--------  InterceptorBinding -------

        xstream.alias("interceptor-binding", InterceptorBinding.class);
        xstream.aliasField("id", InterceptorBinding.class, "id");
        xstream.addImplicitCollection(InterceptorBinding.class, "description", "description", String.class);
        xstream.aliasField("ejb-name", InterceptorBinding.class, "ejbName");
        xstream.addImplicitCollection(InterceptorBinding.class, "interceptorClasses", "interceptor-classes", String.class);
        xstream.aliasField("interceptor-order", InterceptorBinding.class, "interceptorOrder");
        xstream.aliasField("exclude-default-interceptors", InterceptorBinding.class, "excludeDefaultInterceptors");
        xstream.aliasField("method", InterceptorBinding.class, "method");

        //--------  InterceptorOrder -------

        xstream.alias("interceptor-order", InterceptorOrder.class);
        xstream.aliasField("id", InterceptorOrder.class, "id");
        xstream.addImplicitCollection(InterceptorOrder.class, "interceptorClasses", "interceptor-classes", String.class);

        //--------  NamedMethod -------

        xstream.alias("named-method", NamedMethod.class);
        xstream.aliasField("id", NamedMethod.class, "id");
        xstream.aliasField("method-name", NamedMethod.class, "methodName");
        xstream.aliasField("method-params", NamedMethod.class, "methodParams");

        //--------  MethodParams -------

        xstream.alias("method-params", MethodParams.class);
        xstream.aliasField("id", MethodParams.class, "id");
        xstream.addImplicitCollection(MethodParams.class, "methodParam", "method-param", String.class);

        //--------  MessageDestination -------

        xstream.alias("message-destination", MessageDestination.class);
        xstream.aliasField("id", MessageDestination.class, "id");
        xstream.aliasField("message-destination-name", MessageDestination.class, "messageDestinationName");
        xstream.addImplicitCollection(MessageDestination.class, "description", "description", String.class);
        xstream.addImplicitCollection(MessageDestination.class, "displayName", "display-name", String.class);
        xstream.addImplicitCollection(MessageDestination.class, "icons");
        xstream.aliasField("mapped-name", MessageDestination.class, "mappedName");

        //--------  ExcludeList -------

        xstream.alias("exclude-list", ExcludeList.class);
        xstream.aliasField("id", ExcludeList.class, "id");
        xstream.addImplicitCollection(ExcludeList.class, "description", "description", String.class);
        xstream.addImplicitCollection(ExcludeList.class, "methods");

        //--------  Method -------

        xstream.alias("method", Method.class);
        xstream.aliasField("id", Method.class, "id");
        xstream.addImplicitCollection(Method.class, "description", "description", String.class);
        xstream.aliasField("ejb-name", Method.class, "ejbName");
        xstream.aliasField("method-intf", Method.class, "methodIntf");
        xstream.aliasField("method-name", Method.class, "methodName");
        xstream.aliasField("method-params", Method.class, "methodParams");

        //--------  ApplicationException -------

        xstream.alias("application-exception", ApplicationException.class);
        xstream.aliasField("id", ApplicationException.class, "id");
        xstream.aliasField("exception-class", ApplicationException.class, "exceptionClass");
        xstream.aliasField("rollback", ApplicationException.class, "rollback");

        //--------  Entity -------

        xstream.alias("entity", Entity.class);
        xstream.aliasField("persistence-type", Entity.class, "persistenceType");
        xstream.aliasField("prim-key-class", Entity.class, "primaryKeyClass");
        xstream.aliasField("reenterant", Entity.class, "reenterant");
        xstream.aliasField("cmp-version", Entity.class, "cmpVersion");
        xstream.aliasField("abstract-schema-name", Entity.class, "abstractSchemaName");
        xstream.addImplicitCollection(Entity.class, "cmpFields");
        xstream.aliasField("primkey-field", Entity.class, "primkeyField");
        xstream.aliasField("query", Entity.class, "query");

        //--------  CmpField -------

        xstream.alias("cmp-field", CmpField.class);
        xstream.aliasField("id", CmpField.class, "id");
        xstream.addImplicitCollection(CmpField.class, "description", "description", String.class);
        xstream.aliasField("field-name", CmpField.class, "fieldName");

        //--------  Query -------

        xstream.alias("query", Query.class);
        xstream.aliasField("id", Query.class, "id");
        xstream.addImplicitCollection(Query.class, "description", "description", String.class);
        xstream.aliasField("query-method", Query.class, "queryMethod");
        xstream.aliasField("result-type-mapping", Query.class, "resultTypeMapping");
        xstream.aliasField("ejb-ql", Query.class, "ejbQl");

        //--------  QueryMethod -------

        xstream.alias("query-method", QueryMethod.class);
        xstream.aliasField("id", QueryMethod.class, "id");
        xstream.aliasField("method-name", QueryMethod.class, "methodName");
        xstream.aliasField("method-params", QueryMethod.class, "methodParams");

        //--------  Session -------

        xstream.alias("session", Session.class);
        xstream.addImplicitCollection(Session.class, "businessLocal", "business-local", String.class);
        xstream.aliasField("service-endpoint", Session.class, "serviceEndpoint");
        xstream.aliasField("session-type", Session.class, "sessionType");
        xstream.aliasField("timeout-method", Session.class, "timeoutMethod");
        xstream.addImplicitCollection(Session.class, "initMethods");
        xstream.addImplicitCollection(Session.class, "removeMethods");
        xstream.aliasField("transaction-type", Session.class, "transactionType");
        xstream.addImplicitCollection(Session.class, "aroundInvokes");
        xstream.addImplicitCollection(Session.class, "postActivates");
        xstream.addImplicitCollection(Session.class, "prePassivates");
//        xstream.aliasField("ejb-name", Session.class, "ejbName");
//        xstream.aliasField("mapped-name", Session.class, "mappedName");
//        xstream.aliasField("home", Session.class, "home");
//        xstream.aliasField("remote", Session.class, "remote");
//        xstream.aliasField("local-home", Session.class, "localHome");
//        xstream.aliasField("local", Session.class, "local");
//        xstream.aliasField("ejb-class", Session.class, "ejbClass");

        //--------  InitMethod -------

        xstream.alias("init-method", InitMethod.class);
        xstream.aliasField("id", InitMethod.class, "id");
        xstream.aliasField("create-method", InitMethod.class, "createMethod");
        xstream.aliasField("bean-method", InitMethod.class, "beanMethod");

        //--------  RemoveMethod -------

        xstream.alias("remove-method", RemoveMethod.class);
        xstream.aliasField("id", RemoveMethod.class, "id");
        xstream.aliasField("bean-method", RemoveMethod.class, "beanMethod");
        xstream.aliasField("retain-if-exception", RemoveMethod.class, "retainIfException");

        //--------  MessageDrivenBean -------

        xstream.alias("message-driven-bean", MessageDrivenBean.class);
        xstream.aliasField("id", MessageDrivenBean.class, "id");
        xstream.aliasField("messaging-type", MessageDrivenBean.class, "messagingType");
        xstream.aliasField("timeout-method", MessageDrivenBean.class, "timeoutMethod");
        xstream.aliasField("transaction-method", MessageDrivenBean.class, "transactionMethod");
        xstream.aliasField("message-destination-type", MessageDrivenBean.class, "messageDestinationType");
        xstream.aliasField("message-destination-link", MessageDrivenBean.class, "messageDestinationLink");
        xstream.aliasField("activation-config", MessageDrivenBean.class, "activationConfig");
        xstream.addImplicitCollection(MessageDrivenBean.class, "aroundInvokes");

        //--------  ActivationConfig -------

        xstream.alias("activation-config", ActivationConfig.class);
        xstream.aliasField("id", ActivationConfig.class, "id");
        xstream.addImplicitCollection(ActivationConfig.class, "description", "description", String.class);
        xstream.addImplicitCollection(ActivationConfig.class, "activationConfigProperties");

        //--------  ActivationConfigProperty -------

        xstream.alias("activation-config-property", ActivationConfigProperty.class);
        xstream.aliasField("activation-config-property-name", ActivationConfigProperty.class, "activationConfigPropertyName");
        xstream.aliasField("activation-config-property-value", ActivationConfigProperty.class, "activationConfigPropertyValue");

        return xstream;
    }

}
