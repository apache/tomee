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
package org.openejb.ejbjar;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class EjbJar {

    List<String> description;
    List<String> displayName;
    List<Javaee.Icon> icons;
    List<EnterpriseBean> enterpriseBeans;
    List<Interceptor> interceptors;
    List<Relationships> relationships;
    AssemblyDescriptor assemblyDescriptor;
    String ejbClientJar;
    String version;
    Boolean metadataComplete;
    String id;

    public static class ActivationConfigProperty {
        String activationConfigPropertyName;
        String activationConfigPropertyValue;
    }

    public static class ActivationConfig {
        String id;
        List<String> description;
        List<ActivationConfigProperty> activationConfigProperties;
    }

    public static class ApplicationException {
        String id;
        String exceptionClass;
        boolean rollback;
    }

    public static class AroundInvoke {
        String className;
        String methodName;
    }

    public static class AssemblyDescriptor {
        String id;
        List<Javaee.SecurityRole> securityRoles;
        List<MethodPermission> methodPermissions;
        List<ContainerTransaction> containerTransactions;
        List<InterceptorBinding> interceptorBindings;
        List<Javaee.MessageDestination> messageDestinations;
        List<ExcludeList> excludeLists;
        List<ApplicationException> applicationExceptions;
    }


    public static class CmpField {
        String id;
        List<String> description;
        String fieldName;
    }

    public static class CmrField {
        String id;
        List<String> description;
        String cmrFieldName;
        String cmrFieldType;
    }

    public static class ContainerTransaction {
        String id;
        List<String> description;
        String method;
        String transactionAttribute;

    }

    public static class EjbRelation {
        String id;
        List<String> description;
        String ejbRelationName;
        //TODO
        EjbRelationshipRole ejbRelationshipRole;
    }

    public static class EjbRelationshipRole {
        String id;
        String ejbRelationshipRoleName;
        Multiplicity multiplicity;
        boolean cascadeDelete;

//        String ejbName;
        RelationshipRoleSource relationshipRoleSource;
        CmrField cmrField;
    }

    public static enum Multiplicity {
        ONE,
        MANY;
    }

    public static class EnterpriseBean {
        String id;
        List<String> description;
        List<String> displayName;
        List<Javaee.Icon> icons;
        String ejbName;
        String mappedName;
        String home;
        String remote;
        String localHome;
        String local;
        String ejbClass;
        List<Javaee.EnvEntry> envEntries;
        List<Javaee.EjbRef> ejbRefs;
        List<Javaee.EjbLocalRef> ejbLocalRefs;
        List<WsClient.ServiceRef> serviceRefs;
        List<Javaee.ResourceEnvRef> resourceEnvRefs;
        List<Javaee.ResourceRef> resourceRefs;
        List<Javaee.MessageDestinationRef> messageDestinationRefs;
        List<Javaee.PersistenceContextRef> persistenceContextRefs;
        List<Javaee.PersistenceUnitRef> persistenceUnitRefs;

        // For aggregation
        List<Javaee.JndiEnvironmentRef> jndiEnvironmentRefs;

        List<Javaee.PostConstruct> postConstructs;
        List<Javaee.PreDestroy> preDestroys;

        List<Javaee.SecurityRoleRef> securityRoleRefs;
        SecurityIdentity securityIdentity;
    }

    public static class Entity extends EnterpriseBean {
        String id;
        PersistenceType persistenceType;
        String primaryKeyClass;
        boolean reenterant;
        String cmpVersion;
        String abstractSchemaName;
        List<CmpField> cmpFields;
        String primkeyField;
        Query query;
    }

    public static enum PersistenceType {
        BEAN,
        CONTAINER;
    }

    public static class ExcludeList {
        String id;
        List<String> description;
        List<Method> methods;
    }

    public static class InitMethod {
        String id;
        NamedMethod createMethod;
        NamedMethod beanMethod;
    }

    public static class InterceptorBinding {
        String id;
        List<String> description;
        String ejbName;
        List<String> interceptorClasses;
        InterceptorOrder interceptorOrder;
        boolean excludeDefaultInterceptors;
        NamedMethod method;
    }

    public static class InterceptorOrder {
        String id;
        List<String> interceptorClasses;
    }

    public static class Interceptor {
        String id;
        List<String> description;
        String interceptorClass;
        List<AroundInvoke> aroundInvokes;
        List<PostActivate> postActivates;
        List<PrePassivate> prePassivates;
        List<Javaee.EnvEntry> envEntries;
        List<Javaee.EjbRef> ejbRefs;
        List<Javaee.EjbLocalRef> ejbLocalRefs;
        List<WsClient.ServiceRef> serviceRefs;
        List<Javaee.ResourceEnvRef> resourceEnvRefs;
        List<Javaee.ResourceRef> resourceRefs;
        List<Javaee.MessageDestinationRef> messageDestinationRefs;
        List<Javaee.PersistenceContextRef> persistenceContextRefs;
        List<Javaee.PersistenceUnitRef> persistenceUnitRefs;

        // For aggregation
        List<Javaee.JndiEnvironmentRef> jndiEnvironmentRefs;

        List<Javaee.PostConstruct> postConstructs;
        List<Javaee.PreDestroy> preDestroys;
    }

    public static class MessageDrivenBean extends EnterpriseBean {
        String id;
        String messagingType;
        NamedMethod timeoutMethod;
        String transactionMethod;
        String messageDestinationType;
        String messageDestinationLink;
        ActivationConfig activationConfig;
        List<AroundInvoke> aroundInvokes;
    }

    public static class MethodParams {
        String id;
        List<String> methodParam;
    }

    public static class MethodPermission {
        String id;
        List<String> description;
        List<String> roleNames;
        boolean unchecked;
    }

    public static class Method {
        String id;
        List<String> description;
        String ejbName;
        MethodIntfType methodIntf;
        String methodName;
        MethodParams methodParams;
    }

    public static enum MethodIntfType {
        HOME,
        REMOTE,
        LOCALHOME,
        LOCAL,
        SERVICEENDPOINT;
    }

    public static class NamedMethod {
        String id;
        String methodName;
        MethodParams methodParams;
    }

    public static class QueryMethod {
        String id;
        String methodName;
        MethodParams methodParams;
    }

    public static class Query {
        String id;
        List<String> description;
        QueryMethod queryMethod;
        ResultTypeMapping resultTypeMapping;
        String ejbQl;
    }

    public static enum ResultTypeMapping {
        LOCAL,
        REMOTE;
    }

    public static class RelationshipRoleSource {
        String id;
        List<String> description;
        String ejbName;
    }

    public static class Relationships {
        String id;
        List<String> description;
        List<EjbRelation> ejbRelations;
    }

    public static class RemoveMethod {
        String id;
        NamedMethod beanMethod;
        boolean retainIfException;
    }

    public static class SecurityIdentity {
        String id;
        boolean useCallerIdentity;
        Javaee.RunAs runAs;
    }

    public static class Session extends EnterpriseBean {
        String id;
        List<String> businessLocal;
        String serviceEndpoint;
        SessionType sessionType;
        NamedMethod timeoutMethod;
        List<InitMethod> initMethods;
        List<RemoveMethod> removeMethods;
        TransactionType transactionType;
        List<AroundInvoke> aroundInvokes;
        List<PostActivate> postActivates;
        List<PrePassivate> prePassivates;
    }

    public static class PostActivate extends Javaee.LifecycleCallback {
    }

    public static class PrePassivate extends Javaee.LifecycleCallback {
    }

    public static enum SessionType {
        STATEFUL,
        STATELESS;
    }

    public static enum TransactionType {
        BEAN,
        CONTAINER;
    }

    public static enum TransactionAttributeType {
        MANDATORY,
        REQUIRED,
        REQUIRES_NEW,
        SUPPORTS,
        NOT_SUPPORTED,
        NEVER;
    }

}
