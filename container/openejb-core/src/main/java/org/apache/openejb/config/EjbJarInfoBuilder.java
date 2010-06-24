/**
 *
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ApplicationExceptionInfo;
import org.apache.openejb.assembler.classic.CallbackInfo;
import org.apache.openejb.assembler.classic.CmrFieldInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityBeanInfo;
import org.apache.openejb.assembler.classic.InitMethodInfo;
import org.apache.openejb.assembler.classic.InterceptorBindingInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.assembler.classic.JndiNameInfo;
import org.apache.openejb.assembler.classic.ManagedBeanInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.MethodConcurrencyInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodScheduleInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.NamedMethodInfo;
import org.apache.openejb.assembler.classic.QueryInfo;
import org.apache.openejb.assembler.classic.RemoveMethodInfo;
import org.apache.openejb.assembler.classic.ScheduleInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.apache.openejb.assembler.classic.SingletonBeanInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.assembler.classic.StatelessBeanInfo;
import org.apache.openejb.assembler.classic.TimeoutInfo;
import org.apache.openejb.jee.ConcurrencyManagementType;
import org.apache.openejb.jee.Timeout;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.CallbackMethod;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.ContainerConcurrency;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbRelation;
import org.apache.openejb.jee.EjbRelationshipRole;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.ExcludeList;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.InitMethod;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.MethodSchedule;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.Query;
import org.apache.openejb.jee.QueryMethod;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.RemoveMethod;
import org.apache.openejb.jee.ResultTypeMapping;
import org.apache.openejb.jee.TimerSchedule;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarInfoBuilder {

    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final List<String> deploymentIds = new ArrayList<String>();
    private final List<String> securityRoles = new ArrayList<String>();


    public EjbJarInfo buildInfo(EjbModule jar) throws OpenEJBException {
        deploymentIds.clear();
        securityRoles.clear();
        int beansDeployed = jar.getOpenejbJar().getEjbDeploymentCount();
        int beansInEjbJar = jar.getEjbJar().getEnterpriseBeans().length;

        if (beansInEjbJar != beansDeployed) {
            Map<String, EjbDeployment> deployed = jar.getOpenejbJar().getDeploymentsByEjbName();
            for (EnterpriseBean bean : jar.getEjbJar().getEnterpriseBeans()) {
                if (!deployed.containsKey(bean.getEjbName())){
                    ConfigUtils.logger.warning("conf.0018", bean.getEjbName(), jar.getJarLocation());
                }
            }
            String message = messages.format("conf.0008", jar.getJarLocation(), "" + beansInEjbJar, "" + beansDeployed);
            logger.warning(message);
            throw new OpenEJBException(message);
        }

        Map<String, EjbDeployment> ejbds = jar.getOpenejbJar().getDeploymentsByEjbName();
        Map<String, EnterpriseBeanInfo> infos = new HashMap<String, EnterpriseBeanInfo>();
        Map<String, EnterpriseBean> items = new HashMap<String, EnterpriseBean>();

        EjbJarInfo ejbJar = new EjbJarInfo();
        ejbJar.jarPath = jar.getJarLocation();
        ejbJar.moduleId = jar.getModuleId();
        if (ejbJar.moduleId == null) {
            ejbJar.moduleId = new File(ejbJar.jarPath).getName().replaceFirst(".jar$","");
        }
        ejbJar.watchedResources.addAll(jar.getWatchedResources());

        ejbJar.properties.putAll(jar.getOpenejbJar().getProperties());

        for (EnterpriseBean bean : jar.getEjbJar().getEnterpriseBeans()) {
            EnterpriseBeanInfo beanInfo;
            if (bean instanceof org.apache.openejb.jee.SessionBean) {
                beanInfo = initSessionBean((SessionBean) bean, ejbds);
            } else if (bean instanceof org.apache.openejb.jee.EntityBean) {
                beanInfo = initEntityBean((EntityBean) bean, ejbds);
            } else if (bean instanceof org.apache.openejb.jee.MessageDrivenBean) {
                beanInfo = initMessageBean((MessageDrivenBean) bean, ejbds);
            } else {
                throw new OpenEJBException("Unknown bean type: "+bean.getClass().getName());
            }
            ejbJar.enterpriseBeans.add(beanInfo);

            if (deploymentIds.contains(beanInfo.ejbDeploymentId)) {
                String message = messages.format("conf.0100", beanInfo.ejbDeploymentId, jar.getJarLocation(), beanInfo.ejbName);
                logger.warning(message);
                throw new OpenEJBException(message);
            }

            deploymentIds.add(beanInfo.ejbDeploymentId);

            beanInfo.codebase = jar.getJarLocation();
            infos.put(beanInfo.ejbName, beanInfo);
            items.put(beanInfo.ejbName, bean);

            if (bean.getSecurityIdentity() != null) {
                beanInfo.runAs = bean.getSecurityIdentity().getRunAs();
            }

            initJndiNames(ejbds, bean, beanInfo);
        }

        if (jar.getEjbJar().getAssemblyDescriptor() != null) {
            initInterceptors(jar, ejbJar, infos);
            initSecurityRoles(jar, ejbJar);
            initMethodPermissions(jar, ejbds, ejbJar);
            initExcludesList(jar, ejbds, ejbJar);
            initMethodTransactions(jar, ejbds, ejbJar);
            initMethodConcurrency(jar, ejbds, ejbJar);
            initMethodSchedules(jar, ejbds, ejbJar);
            initApplicationExceptions(jar, ejbJar);

            for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                resolveRoleLinks(jar, bean, items.get(bean.ejbName));
            }
        }

        if (jar.getEjbJar().getRelationships() != null) {
            initRelationships(jar, infos);
        }

        return ejbJar;
    }

    private void initJndiNames(Map<String, EjbDeployment> ejbds, EnterpriseBean bean, EnterpriseBeanInfo info) {
        EjbDeployment deployment = ejbds.get(info.ejbName);
        if (deployment != null) for (Jndi jndi : deployment.getJndi()) {
            JndiNameInfo jndiNameInfo = new JndiNameInfo();
            jndiNameInfo.intrface = jndi.getInterface();
            jndiNameInfo.name = jndi.getName();
            info.jndiNamess.add(jndiNameInfo);
        }
    }

    private void initRelationships(EjbModule jar, Map<String, EnterpriseBeanInfo> infos) throws OpenEJBException {
        for (EjbRelation ejbRelation : jar.getEjbJar().getRelationships().getEjbRelation()) {
            Iterator<EjbRelationshipRole> iterator = ejbRelation.getEjbRelationshipRole().iterator();
            EjbRelationshipRole left = iterator.next();
            EjbRelationshipRole right = iterator.next();

            // left role info
            CmrFieldInfo leftCmrFieldInfo = initRelationshipRole(left, right, infos);
            CmrFieldInfo rightCmrFieldInfo = initRelationshipRole(right, left, infos);
            leftCmrFieldInfo.mappedBy = rightCmrFieldInfo;
            rightCmrFieldInfo.mappedBy = leftCmrFieldInfo;
        }
    }

    private CmrFieldInfo initRelationshipRole(EjbRelationshipRole role, EjbRelationshipRole relatedRole, Map<String, EnterpriseBeanInfo> infos) throws OpenEJBException {
        CmrFieldInfo cmrFieldInfo = new CmrFieldInfo();

        // find the entityBeanInfo info for this role
        String ejbName = role.getRelationshipRoleSource().getEjbName();
        EnterpriseBeanInfo enterpriseBeanInfo = infos.get(ejbName);
        if (enterpriseBeanInfo == null) {
            throw new OpenEJBException("Relation role source ejb not found " + ejbName);
        }
        if (!(enterpriseBeanInfo instanceof EntityBeanInfo)) {
            throw new OpenEJBException("Relation role source ejb is not an entity bean " + ejbName);
        }
        EntityBeanInfo entityBeanInfo = (EntityBeanInfo) enterpriseBeanInfo;
        cmrFieldInfo.roleSource = entityBeanInfo;

        // RoleName: this may be null
        cmrFieldInfo.roleName = role.getEjbRelationshipRoleName();

        cmrFieldInfo.synthetic = role.getCmrField() == null;

        // CmrFieldName: is null for uni-directional relationships
        if (role.getCmrField() != null) {
            cmrFieldInfo.fieldName = role.getCmrField().getCmrFieldName();
            // CollectionType: java.util.Collection or java.util.Set
            if (role.getCmrField().getCmrFieldType() != null) {
                cmrFieldInfo.fieldType = role.getCmrField().getCmrFieldType().toString();
            }
        } else {
            String relatedEjbName = relatedRole.getRelationshipRoleSource().getEjbName();
            EnterpriseBeanInfo relatedEjb = infos.get(relatedEjbName);
            if (relatedEjb == null) {
                throw new OpenEJBException("Relation role source ejb not found " + relatedEjbName);
            }
            if (!(relatedEjb instanceof EntityBeanInfo)) {
                throw new OpenEJBException("Relation role source ejb is not an entity bean " + relatedEjbName);
            }
            EntityBeanInfo relatedEntity = (EntityBeanInfo) relatedEjb;

            relatedRole.getRelationshipRoleSource();
            cmrFieldInfo.fieldName = relatedEntity.abstractSchemaName + "_" + relatedRole.getCmrField().getCmrFieldName();
            if (relatedRole.getMultiplicity() == Multiplicity.MANY) {
                cmrFieldInfo.fieldType = Collection.class.getName();
            }
        }

        // CascadeDelete
        cmrFieldInfo.cascadeDelete = role.getCascadeDelete();
        // Multiplicity: one or many
        cmrFieldInfo.many = role.getMultiplicity() == Multiplicity.MANY;

        // add the field to the entityBean
        entityBeanInfo.cmrFields.add(cmrFieldInfo);

        return cmrFieldInfo;
    }

    private void initInterceptors(EjbModule jar, EjbJarInfo ejbJar, Map<String, EnterpriseBeanInfo> beanInfos) throws OpenEJBException {
        if (jar.getEjbJar().getInterceptors().length == 0) return;
        if (jar.getEjbJar().getAssemblyDescriptor() == null) return;
        if (jar.getEjbJar().getAssemblyDescriptor().getInterceptorBinding() == null) return;

        for (Interceptor s : jar.getEjbJar().getInterceptors()) {
            InterceptorInfo info = new InterceptorInfo();

            info.clazz = s.getInterceptorClass();

            copyCallbacks(s.getAroundInvoke(), info.aroundInvoke);

            copyCallbacks(s.getPostConstruct(), info.postConstruct);
            copyCallbacks(s.getPreDestroy(), info.preDestroy);

            copyCallbacks(s.getPostActivate(), info.postActivate);
            copyCallbacks(s.getPrePassivate(), info.prePassivate);

            copyCallbacks(s.getAfterBegin(), info.afterBegin);
            copyCallbacks(s.getBeforeCompletion(), info.beforeCompletion);
            copyCallbacks(s.getAfterCompletion(), info.afterCompletion);

            copyCallbacks(s.getAroundTimeout(), info.aroundTimeout);

            ejbJar.interceptors.add(info);
        }

        for (InterceptorBinding binding : jar.getEjbJar().getAssemblyDescriptor().getInterceptorBinding()) {
            InterceptorBindingInfo info = new InterceptorBindingInfo();
            info.ejbName = binding.getEjbName();
            info.excludeClassInterceptors = binding.getExcludeClassInterceptors();
            info.excludeDefaultInterceptors = binding.getExcludeDefaultInterceptors();
            info.interceptors.addAll(binding.getInterceptorClass());
            if (binding.getInterceptorOrder() != null) {
                info.interceptorOrder.addAll(binding.getInterceptorOrder().getInterceptorClass());
            }

            info.method = toInfo(binding.getMethod());
            ejbJar.interceptorBindings.add(info);
        }
    }

    private void initMethodTransactions(EjbModule jar, Map ejbds, EjbJarInfo ejbJarInfo) {

        List<ContainerTransaction> containerTransactions = jar.getEjbJar().getAssemblyDescriptor().getContainerTransaction();
        for (ContainerTransaction cTx : containerTransactions) {
            MethodTransactionInfo info = new MethodTransactionInfo();

            info.description = cTx.getDescription();
            info.transAttribute = cTx.getTransAttribute().toString();
            info.methods.addAll(getMethodInfos(cTx.getMethod(), ejbds));
            ejbJarInfo.methodTransactions.add(info);
        }
    }

    private void initMethodConcurrency(EjbModule jar, Map ejbds, EjbJarInfo ejbJarInfo) {

        List<ContainerConcurrency> containerConcurrency = jar.getEjbJar().getAssemblyDescriptor().getContainerConcurrency();
        for (ContainerConcurrency att : containerConcurrency) {
            MethodConcurrencyInfo info = new MethodConcurrencyInfo();

            info.description = att.getDescription();
            info.concurrencyAttribute = att.getLock().toString();
            info.methods.addAll(getMethodInfos(att.getMethod(), ejbds));
            ejbJarInfo.methodConcurrency.add(info);
        }
    }

    private void initMethodSchedules(EjbModule jar, Map ejbds, EjbJarInfo ejbJarInfo) {

        List<MethodSchedule> methodSchedule = jar.getEjbJar().getAssemblyDescriptor().getMethodSchedule();
        for (MethodSchedule att : methodSchedule) {
            MethodScheduleInfo info = new MethodScheduleInfo();

            info.description = att.getDescription();
            info.method = toInfo(att.getMethod());

            for (TimerSchedule schedule : att.getSchedule()) {
                ScheduleInfo scheduleInfo = new ScheduleInfo();
                scheduleInfo.second = schedule.getSecond();
                scheduleInfo.minute = schedule.getMinute();
                scheduleInfo.hour = schedule.getHour();
                scheduleInfo.dayOfWeek = schedule.getDayOfWeek();
                scheduleInfo.dayOfMonth = schedule.getDayOfMonth();
                scheduleInfo.month = schedule.getMonth();
                scheduleInfo.year = schedule.getYear();
                scheduleInfo.info = schedule.getInfo();
                scheduleInfo.persistent = schedule.isPersistent();
                info.schedules.add(scheduleInfo);
            }

            ejbJarInfo.methodSchedules.add(info);
        }
    }

    private void initApplicationExceptions(EjbModule jar, EjbJarInfo ejbJarInfo) {
        for (ApplicationException applicationException : jar.getEjbJar().getAssemblyDescriptor().getApplicationException()) {
            ApplicationExceptionInfo info = new ApplicationExceptionInfo();
            info.exceptionClass = applicationException.getExceptionClass();
            info.rollback = applicationException.getRollback();
            ejbJarInfo.applicationException.add(info);
        }
    }

    private void initSecurityRoles(EjbModule jar, EjbJarInfo ejbJarInfo) {

        List<SecurityRole> roles = jar.getEjbJar().getAssemblyDescriptor().getSecurityRole();

        for (SecurityRole sr : roles) {
            SecurityRoleInfo info = new SecurityRoleInfo();

            info.description = sr.getDescription();
            info.roleName = sr.getRoleName();

            if (securityRoles.contains(sr.getRoleName())) {
                ConfigUtils.logger.warning("conf.0102", jar.getJarLocation(), sr.getRoleName());
            } else {
                securityRoles.add(sr.getRoleName());
            }
            ejbJarInfo.securityRoles.add(info);
        }
    }

    private void initMethodPermissions(EjbModule jar, Map ejbds, EjbJarInfo ejbJarInfo) {

        List<MethodPermission> methodPermissions = jar.getEjbJar().getAssemblyDescriptor().getMethodPermission();

        for (MethodPermission mp : methodPermissions) {
            MethodPermissionInfo info = new MethodPermissionInfo();

            info.description = mp.getDescription();
            info.roleNames.addAll(mp.getRoleName());
            info.methods.addAll(getMethodInfos(mp.getMethod(), ejbds));
            info.unchecked = mp.getUnchecked();

            ejbJarInfo.methodPermissions.add(info);
        }
    }

    private void initExcludesList(EjbModule jar, Map ejbds, EjbJarInfo ejbJarInfo) {

        ExcludeList methodPermissions = jar.getEjbJar().getAssemblyDescriptor().getExcludeList();

        for (Method excludedMethod : methodPermissions.getMethod()) {
            ejbJarInfo.excludeList.add(getMethodInfo(excludedMethod, ejbds));
        }
    }

    private void resolveRoleLinks(EjbModule jar, EnterpriseBeanInfo bean, JndiConsumer item) {
        if (!(item instanceof RemoteBean)) {
            return;
        }

        RemoteBean rb = (RemoteBean) item;

        List<SecurityRoleRef> refs = rb.getSecurityRoleRef();
        for (SecurityRoleRef ref : refs) {
            SecurityRoleReferenceInfo info = new SecurityRoleReferenceInfo();

            info.description = ref.getDescription();
            info.roleLink = ref.getRoleLink();
            info.roleName = ref.getRoleName();

            if (info.roleLink == null) {
                info.roleLink = info.roleName;
            }
            bean.securityRoleReferences.add(info);
        }
    }

    private List<MethodInfo> getMethodInfos(List<Method> ms, Map ejbds) {
        if (ms == null) return Collections.emptyList();

        List<MethodInfo> mi = new ArrayList<MethodInfo>(ms.size());
        for (Method method : ms) {
            MethodInfo methodInfo = getMethodInfo(method, ejbds);
            mi.add(methodInfo);
        }

        return mi;
    }

    private MethodInfo getMethodInfo(Method method, Map ejbds) {
        MethodInfo methodInfo = new MethodInfo();

        EjbDeployment d = (EjbDeployment) ejbds.get(method.getEjbName());

        methodInfo.description = method.getDescription();
        methodInfo.ejbDeploymentId = (d == null)?null:d.getDeploymentId();
        methodInfo.ejbName = method.getEjbName();
        methodInfo.methodIntf = (method.getMethodIntf() == null) ? null : method.getMethodIntf().toString();
        methodInfo.methodName = method.getMethodName();
        if (methodInfo.methodName == null || methodInfo.methodName.equals("")){
            methodInfo.methodName = "*";
        }
        methodInfo.className = method.getClassName();
        if (methodInfo.className == null || methodInfo.className.equals("")){
            methodInfo.className = "*";
        }

        MethodParams mp = method.getMethodParams();
        if (mp != null) {
            methodInfo.methodParams = mp.getMethodParam();
        }
        return methodInfo;
    }

    private EnterpriseBeanInfo initSessionBean(SessionBean s, Map m) throws OpenEJBException {
        EnterpriseBeanInfo bean = null;

        if (s.getSessionType() == SessionType.STATEFUL) {
            bean = new StatefulBeanInfo();
            StatefulBeanInfo stateful = ((StatefulBeanInfo) bean);

            copyCallbacks(s.getPostActivate(), stateful.postActivate);
            copyCallbacks(s.getPrePassivate(), stateful.prePassivate);

            copyCallbacks(s.getAfterBegin(), stateful.afterBegin);
            copyCallbacks(s.getBeforeCompletion(), stateful.beforeCompletion);
            copyCallbacks(s.getAfterCompletion(), stateful.afterCompletion);

            for (InitMethod initMethod : s.getInitMethod()) {
                InitMethodInfo init = new InitMethodInfo();
                init.beanMethod = toInfo(initMethod.getBeanMethod());
                init.createMethod = toInfo(initMethod.getCreateMethod());
                stateful.initMethods.add(init);
            }

            for (RemoveMethod removeMethod : s.getRemoveMethod()) {
                RemoveMethodInfo remove = new RemoveMethodInfo();
                remove.beanMethod = toInfo(removeMethod.getBeanMethod());
                remove.retainIfException = removeMethod.getRetainIfException();
                stateful.removeMethods.add(remove);
            }

        } else if (s.getSessionType() == SessionType.MANAGED) {
            bean = new ManagedBeanInfo();
            ManagedBeanInfo managed = ((ManagedBeanInfo) bean);

            copyCallbacks(s.getPostActivate(), managed.postActivate);
            copyCallbacks(s.getPrePassivate(), managed.prePassivate);

            for (RemoveMethod removeMethod : s.getRemoveMethod()) {
                RemoveMethodInfo remove = new RemoveMethodInfo();
                remove.beanMethod = toInfo(removeMethod.getBeanMethod());
                remove.retainIfException = removeMethod.getRetainIfException();
                managed.removeMethods.add(remove);
            }

        } else if (s.getSessionType() == SessionType.SINGLETON) {
            bean = new SingletonBeanInfo();
            ConcurrencyManagementType type = s.getConcurrencyManagementType();
            bean.concurrencyType = (type != null) ? type.toString() : ConcurrencyManagementType.CONTAINER.toString();
            bean.loadOnStartup = s.getInitOnStartup();
            // See JndiEncInfoBuilder.buildDependsOnRefs for processing of DependsOn
            // bean.dependsOn.addAll(s.getDependsOn());
        } else {
            bean = new StatelessBeanInfo();
        }

        if (s.getSessionType() != SessionType.STATEFUL) {
            copyCallbacks(s.getAroundTimeout(),bean.aroundTimeout);
        }
        
        bean.localbean = s.getLocalBean() != null;


        bean.timeoutMethod = toInfo(s.getTimeoutMethod());

        copyCallbacks(s.getAroundInvoke(), bean.aroundInvoke);
        copyCallbacks(s.getPostConstruct(), bean.postConstruct);
        copyCallbacks(s.getPreDestroy(), bean.preDestroy);

        EjbDeployment d = (EjbDeployment) m.get(s.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + s.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        Icon icon = s.getIcon();
        bean.largeIcon = (icon == null) ? null : icon.getLargeIcon();
        bean.smallIcon = (icon == null) ? null : icon.getSmallIcon();
        bean.description = s.getDescription();
        bean.displayName = s.getDisplayName();
        bean.ejbClass = s.getEjbClass();
        bean.ejbName = s.getEjbName();
        bean.home = s.getHome();
        bean.remote = s.getRemote();
        bean.localHome = s.getLocalHome();
        bean.local = s.getLocal();
        bean.businessLocal.addAll(s.getBusinessLocal());
        bean.businessRemote.addAll(s.getBusinessRemote());
        TransactionType txType = s.getTransactionType();
        bean.transactionType = (txType != null)?txType.toString(): TransactionType.CONTAINER.toString();
        bean.serviceEndpoint = s.getServiceEndpoint();
        bean.properties.putAll(d.getProperties());

        final Timeout statefulTimeout = s.getStatefulTimeout();
        if(statefulTimeout != null) {
        	bean.statefulTimeout = new TimeoutInfo();
            bean.statefulTimeout.time = statefulTimeout.getTimeout();
            bean.statefulTimeout.unit = statefulTimeout.getUnit().toTimeUnit().toString();
        }

        final Timeout accessTimeout = s.getAccessTimeout();
        if(accessTimeout != null) {
            bean.accessTimeout = new TimeoutInfo();
            bean.accessTimeout.time = accessTimeout.getTimeout();
            bean.accessTimeout.unit = accessTimeout.getUnit().toTimeUnit().toString();
        }

        return bean;
    }

    private EnterpriseBeanInfo initMessageBean(MessageDrivenBean mdb, Map m) throws OpenEJBException {
        MessageDrivenBeanInfo bean = new MessageDrivenBeanInfo();

        bean.timeoutMethod = toInfo(mdb.getTimeoutMethod());

        copyCallbacks(mdb.getAroundInvoke(), bean.aroundInvoke);
        copyCallbacks(mdb.getPostConstruct(), bean.postConstruct);
        copyCallbacks(mdb.getPreDestroy(), bean.preDestroy);

        EjbDeployment d = (EjbDeployment) m.get(mdb.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + mdb.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        Icon icon = mdb.getIcon();
        bean.largeIcon = (icon == null) ? null : icon.getLargeIcon();
        bean.smallIcon = (icon == null) ? null : icon.getSmallIcon();
        bean.description = mdb.getDescription();
        bean.displayName = mdb.getDisplayName();
        bean.ejbClass = mdb.getEjbClass();
        bean.ejbName = mdb.getEjbName();
        TransactionType txType = mdb.getTransactionType();
        bean.transactionType = (txType != null)?txType.toString(): TransactionType.CONTAINER.toString();
        bean.properties.putAll(d.getProperties());

        if (mdb.getMessagingType() != null) {
            bean.mdbInterface = mdb.getMessagingType();
        } else {
            bean.mdbInterface = "javax.jms.MessageListener";
        }

        ResourceLink resourceLink = d.getResourceLink("openejb/destination");
        if (resourceLink != null) {
            bean.destinationId = resourceLink.getResId();
        }

        if (mdb.getMessageDestinationType() != null) {
            bean.activationProperties.put("destinationType", mdb.getMessageDestinationType());
        }

        ActivationConfig activationConfig = mdb.getActivationConfig();
        if (activationConfig != null) {
            for (ActivationConfigProperty property : activationConfig.getActivationConfigProperty()) {
                String name = property.getActivationConfigPropertyName();
                String value = property.getActivationConfigPropertyValue();
                bean.activationProperties.put(name, value);
            }
        }

        return bean;
    }

    private NamedMethodInfo toInfo(NamedMethod method) {
        if (method == null) return null;

        NamedMethodInfo info = new NamedMethodInfo();

        info.methodName = method.getMethodName();

        if (method.getMethodParams() != null) {
            info.methodParams = method.getMethodParams().getMethodParam();
        }

        return info;
    }

    private void copyCallbacks(List<? extends CallbackMethod> from, List<CallbackInfo> to) {
        for (CallbackMethod callback : from) {
            CallbackInfo info = new CallbackInfo();
            info.className = callback.getClassName();
            info.method = callback.getMethodName();
            to.add(info);
        }
    }

    private EnterpriseBeanInfo initEntityBean(EntityBean e, Map m) throws OpenEJBException {
        EntityBeanInfo bean = new EntityBeanInfo();

        EjbDeployment d = (EjbDeployment) m.get(e.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + e.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        Icon icon = e.getIcon();
        bean.largeIcon = (icon == null) ? null : icon.getLargeIcon();
        bean.smallIcon = (icon == null) ? null : icon.getSmallIcon();
        bean.description = e.getDescription();
        bean.displayName = e.getDisplayName();
        bean.ejbClass = e.getEjbClass();
        bean.abstractSchemaName = e.getAbstractSchemaName();
        bean.ejbName = e.getEjbName();
        bean.home = e.getHome();
        bean.remote = e.getRemote();
        bean.localHome = e.getLocalHome();
        bean.local = e.getLocal();
        bean.transactionType = "Container";

        bean.primKeyClass = e.getPrimKeyClass();
        bean.primKeyField = e.getPrimkeyField();
        bean.persistenceType = e.getPersistenceType().toString();
        bean.reentrant = e.getReentrant() + "";
        bean.properties.putAll(d.getProperties());

        CmpVersion cmpVersion = e.getCmpVersion();
        if (e.getPersistenceType() == PersistenceType.CONTAINER) {
            if (cmpVersion != null && cmpVersion == CmpVersion.CMP1){
                bean.cmpVersion = 1;
            } else {
                bean.cmpVersion = 2;
            }
        }

        List<CmpField> cmpFields = e.getCmpField();
        for (CmpField cmpField : cmpFields) {
            bean.cmpFieldNames.add(cmpField.getFieldName());
        }

        if (bean.persistenceType.equalsIgnoreCase("Container")) {
            for (Query q : e.getQuery()) {
                QueryInfo query = new QueryInfo();
                query.queryStatement = q.getEjbQl().trim();

                MethodInfo method = new MethodInfo();
                method.ejbName = bean.ejbName;
                method.className = "*";

                QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                if (qm.getMethodParams() != null) {
                    method.methodParams = qm.getMethodParams().getMethodParam();
                }
                query.method = method;
                ResultTypeMapping resultType = q.getResultTypeMapping();
                if (ResultTypeMapping.REMOTE.equals(resultType)) {
                    query.remoteResultType = true;
                }
                bean.queries.add(query);
            }

            for (org.apache.openejb.jee.oejb3.Query q : d.getQuery()) {
                QueryInfo query = new QueryInfo();
                query.description = q.getDescription();
                query.queryStatement = q.getObjectQl().trim();

                MethodInfo method = new MethodInfo();
                method.ejbName = bean.ejbName;
                method.className = "*";
                org.apache.openejb.jee.oejb3.QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                if (qm.getMethodParams() != null) {
                    method.methodParams = qm.getMethodParams().getMethodParam();
                }
                query.method = method;
                bean.queries.add(query);
            }
        }
        return bean;
    }
}
