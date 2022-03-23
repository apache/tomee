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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ApplicationExceptionInfo;
import org.apache.openejb.assembler.classic.BeansInfo;
import org.apache.openejb.assembler.classic.CallbackInfo;
import org.apache.openejb.assembler.classic.CmrFieldInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityBeanInfo;
import org.apache.openejb.assembler.classic.ExclusionInfo;
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
import org.apache.openejb.cdi.CompositeBeans;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AsyncMethod;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.CallbackMethod;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.ConcurrencyManagementType;
import org.apache.openejb.jee.ConcurrentMethod;
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
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.Multiplicity;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.Query;
import org.apache.openejb.jee.QueryMethod;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.RemoveMethod;
import org.apache.openejb.jee.ResultTypeMapping;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.Timeout;
import org.apache.openejb.jee.Timer;
import org.apache.openejb.jee.TimerSchedule;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.Jndi;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.jee.oejb3.RoleMapping;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.webbeans.spi.BeanArchiveService;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarInfoBuilder {
    private static final URL DEFAULT_BEANS_XML_KEY;
    static {
        try {
            DEFAULT_BEANS_XML_KEY = new URL("jar:file://!/META-INF/beans.xml");
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB, "org.apache.openejb.util.resources");

    private final List<String> deploymentIds = new ArrayList<>();
    private final List<String> securityRoles = new ArrayList<>();


    public EjbJarInfo buildInfo(final EjbModule jar) throws OpenEJBException {
        deploymentIds.clear();
        securityRoles.clear();

        final Map<String, EjbDeployment> ejbds = jar.getOpenejbJar().getDeploymentsByEjbName();
        final int beansDeployed = jar.getOpenejbJar().getEjbDeploymentCount();
        final int beansInEjbJar = jar.getEjbJar().getEnterpriseBeans().length;

        if (beansInEjbJar != beansDeployed) {

            for (final EnterpriseBean bean : jar.getEjbJar().getEnterpriseBeans()) {
                if (!ejbds.containsKey(bean.getEjbName())) {
                    ConfigUtils.logger.warning("conf.0018", bean.getEjbName(), jar.getJarLocation());
                }
            }

            final String message = messages.format("conf.0008", jar.getJarLocation(), String.valueOf(beansInEjbJar), String.valueOf(beansDeployed));
            logger.warning(message);
            throw new OpenEJBException(message);
        }

        final Map<String, EnterpriseBeanInfo> infos = new HashMap<>();
        final Map<String, EnterpriseBean> items = new HashMap<>();

        final EjbJarInfo ejbJar = new EjbJarInfo();
        ejbJar.path = jar.getJarLocation();
        ejbJar.moduleUri = jar.getModuleUri();
        ejbJar.moduleId = jar.getModuleId();
        if (jar.getEjbJar() != null && jar.getEjbJar().getModuleName() != null) {
            ejbJar.moduleName = jar.getEjbJar().getModuleName();
        } else {
            ejbJar.moduleName = jar.getModuleId();
        }

        ejbJar.watchedResources.addAll(jar.getWatchedResources());

        ejbJar.properties.putAll(jar.getProperties());
        ejbJar.properties.putAll(jar.getOpenejbJar().getProperties());

        for (final EnterpriseBean bean : jar.getEjbJar().getEnterpriseBeans()) {
            final EnterpriseBeanInfo beanInfo;
            if (bean instanceof SessionBean) {
                beanInfo = initSessionBean((SessionBean) bean, ejbJar, ejbds);
            } else if (bean instanceof EntityBean) {
                beanInfo = initEntityBean((EntityBean) bean, ejbds);
            } else if (bean instanceof MessageDrivenBean) {
                beanInfo = initMessageBean((MessageDrivenBean) bean, ejbds);
            } else {
                throw new OpenEJBException("Unknown bean type: " + bean.getClass().getName());
            }
            ejbJar.enterpriseBeans.add(beanInfo);

            if (deploymentIds.contains(beanInfo.ejbDeploymentId)) {
                final String message = messages.format("conf.0100", beanInfo.ejbDeploymentId, jar.getJarLocation(), beanInfo.ejbName);
                logger.warning(message);
                throw new OpenEJBException(message);
            }

            deploymentIds.add(beanInfo.ejbDeploymentId);

            beanInfo.codebase = jar.getJarLocation();
            infos.put(beanInfo.ejbName, beanInfo);
            items.put(beanInfo.ejbName, bean);

            if (bean.getSecurityIdentity() != null) {
                beanInfo.runAs = bean.getSecurityIdentity().getRunAs();

                final EjbDeployment deployment = ejbds.get(beanInfo.ejbName);
                if (deployment != null) {
                    for (final RoleMapping mapping : deployment.getRoleMapping()) {
                        if (mapping.getRoleName().equals(beanInfo.runAs)) {
                            beanInfo.runAsUser = mapping.getPrincipalName();
                            break;
                        }
                    }
                }
            }

            initJndiNames(ejbds, beanInfo);
        }

        if (jar.getEjbJar().getAssemblyDescriptor() != null) {
            initInterceptors(jar, ejbJar);
            initSecurityRoles(jar, ejbJar);
            initMethodPermissions(jar, ejbds, ejbJar);
            initExcludesList(jar, ejbds, ejbJar);
            initMethodTransactions(jar, ejbds, ejbJar);
            initMethodConcurrency(jar, ejbds, ejbJar);
            initApplicationExceptions(jar, ejbJar);

            for (final EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                resolveRoleLinks(bean, items.get(bean.ejbName));
            }
        }

        if (jar.getEjbJar().getRelationships() != null) {
            initRelationships(jar, infos);
        }

        final Beans beans = jar.getBeans();
        if (beans != null) {
            ejbJar.beans = new BeansInfo();
            ejbJar.beans.version = beans.getVersion();
            ejbJar.beans.discoveryMode = beans.getBeanDiscoveryMode();
            if (beans.getScan() != null) {
                for (final Beans.Scan.Exclude exclude : beans.getScan().getExclude()) {
                    final ExclusionInfo exclusionInfo = new ExclusionInfo();
                    for (final Object config : exclude.getIfClassAvailableOrIfClassNotAvailableOrIfSystemProperty()) {
                        if (Beans.Scan.Exclude.IfAvailableClassCondition.class.isInstance(config)) {
                            exclusionInfo.availableClasses.add(Beans.Scan.Exclude.ClassCondition.class.cast(config).getName());
                        } else if (Beans.Scan.Exclude.IfNotAvailableClassCondition.class.isInstance(config)) {
                            exclusionInfo.notAvailableClasses.add(Beans.Scan.Exclude.ClassCondition.class.cast(config).getName());
                        } else if (Beans.Scan.Exclude.IfSystemProperty.class.isInstance(config)) {
                            final Beans.Scan.Exclude.IfSystemProperty systemProperty = Beans.Scan.Exclude.IfSystemProperty.class.cast(config);
                            if (systemProperty.getValue() == null) {
                                exclusionInfo.systemPropertiesPresence.add(systemProperty.getName());
                            } else {
                                exclusionInfo.systemProperties.put(systemProperty.getName(), systemProperty.getValue());
                            }
                        } else {
                            throw new IllegalArgumentException("Not supported: " + config);
                        }
                    }

                    final BeansInfo.ExclusionEntryInfo exclusionEntryInfo = new BeansInfo.ExclusionEntryInfo();
                    exclusionEntryInfo.name = exclude.getName();
                    exclusionEntryInfo.exclusion = exclusionInfo;
                    ejbJar.beans.excludes.add(exclusionEntryInfo);
                }
            }

            ejbJar.beans.duplicatedAlternativeClasses.addAll(beans.getDuplicatedAlternatives().getClasses());
            ejbJar.beans.duplicatedAlternativeStereotypes.addAll(beans.getDuplicatedAlternatives().getStereotypes());
            ejbJar.beans.duplicatedInterceptors.addAll(beans.getDuplicatedInterceptors());
            ejbJar.beans.duplicatedDecorators.addAll(beans.getDuplicatedDecorators());

            ejbJar.beans.startupClasses.addAll(beans.getStartupBeans());

            final Map<URL, String> discoveryModeByUrl = new HashMap<>();
            final CompositeBeans composite;
            final boolean isComposite = CompositeBeans.class.isInstance(beans);
            if (isComposite) {
                composite = CompositeBeans.class.cast(beans);
                discoveryModeByUrl.putAll(composite.getDiscoveryByUrl());
            } else {
                composite = null;
                URL key = DEFAULT_BEANS_XML_KEY;
                if (beans.getUri() != null) {
                    try {
                        key = new URL(beans.getUri());
                    } catch (final MalformedURLException e) {
                        // no-op
                    }
                }
                discoveryModeByUrl.put(key, beans.getBeanDiscoveryMode());
            }
            for (final Map.Entry<URL, List<String>> next : beans.getManagedClasses().entrySet()) {
                final URL key = next.getKey();

                final BeansInfo.BDAInfo bdaInfo = new BeansInfo.BDAInfo();
                bdaInfo.discoveryMode = discoveryModeByUrl.get(key);
                merge(composite, key == null ? DEFAULT_BEANS_XML_KEY : key, bdaInfo, next.getValue());
                ejbJar.beans.bdas.add(bdaInfo);
            }
            for (final Map.Entry<URL, List<String>> next : beans.getNotManagedClasses().entrySet()) {
                final URL key = next.getKey();

                final BeansInfo.BDAInfo bdaInfo = new BeansInfo.BDAInfo();
                bdaInfo.discoveryMode = BeanArchiveService.BeanDiscoveryMode.ANNOTATED.name();
                merge(composite, key == null ? DEFAULT_BEANS_XML_KEY : key, bdaInfo, next.getValue());
                ejbJar.beans.noDescriptorBdas.add(bdaInfo);
            }

            // app composer case mainly,we should really not use it anywhere else
            if (composite == null && ejbJar.beans.bdas.size() == 1) {
                final BeansInfo.BDAInfo bda = ejbJar.beans.bdas.iterator().next();
                bda.alternatives.addAll(beans.getAlternativeClasses());
                bda.interceptors.addAll(beans.getInterceptors());
                bda.decorators.addAll(beans.getDecorators());
                bda.stereotypeAlternatives.addAll(beans.getAlternativeStereotypes());
            }
        }

        return ejbJar;
    }

    private void merge(final CompositeBeans composite, final URL key, final BeansInfo.BDAInfo bdaInfo, final List<String> managedClasses) {
        bdaInfo.managedClasses.addAll(managedClasses);
        try {
            bdaInfo.uri = key == null ? null : key.toURI();
        } catch (final URISyntaxException e) {
            bdaInfo.uri = null;
        }
        if (composite != null) {
            final Collection<String> interceptors = composite.getInterceptorsByUrl().get(key);
            if (interceptors != null) {
                bdaInfo.interceptors.addAll(interceptors);
            }
            final Collection<String> decorators = composite.getDecoratorsByUrl().get(key);
            if (decorators != null) {
                bdaInfo.decorators.addAll(decorators);
            }
            final Collection<String> alternatives = composite.getAlternativesByUrl().get(key);
            if (alternatives != null) {
                bdaInfo.alternatives.addAll(alternatives);
            }
            final Collection<String> alternativeStereotypes = composite.getAlternativeStereotypesByUrl().get(key);
            if (alternativeStereotypes != null) {
                bdaInfo.stereotypeAlternatives.addAll(alternativeStereotypes);
            }
        }
    }

    private void initJndiNames(final Map<String, EjbDeployment> ejbds, final EnterpriseBeanInfo info) {
        final EjbDeployment deployment = ejbds.get(info.ejbName);
        if (deployment != null) {
            for (final Jndi jndi : deployment.getJndi()) {
                final JndiNameInfo jndiNameInfo = new JndiNameInfo();
                jndiNameInfo.intrface = jndi.getInterface();
                jndiNameInfo.name = jndi.getName();
                info.jndiNamess.add(jndiNameInfo);
            }
        }
    }

    private void initRelationships(final EjbModule jar, final Map<String, EnterpriseBeanInfo> infos) throws OpenEJBException {
        for (final EjbRelation ejbRelation : jar.getEjbJar().getRelationships().getEjbRelation()) {
            final Iterator<EjbRelationshipRole> iterator = ejbRelation.getEjbRelationshipRole().iterator();
            final EjbRelationshipRole left = iterator.next();
            final EjbRelationshipRole right = iterator.next();

            // left role info
            final CmrFieldInfo leftCmrFieldInfo = initRelationshipRole(left, right, infos);
            final CmrFieldInfo rightCmrFieldInfo = initRelationshipRole(right, left, infos);
            leftCmrFieldInfo.mappedBy = rightCmrFieldInfo;
            rightCmrFieldInfo.mappedBy = leftCmrFieldInfo;
        }
    }

    private CmrFieldInfo initRelationshipRole(final EjbRelationshipRole role, final EjbRelationshipRole relatedRole, final Map<String, EnterpriseBeanInfo> infos) throws OpenEJBException {
        final CmrFieldInfo cmrFieldInfo = new CmrFieldInfo();

        // find the entityBeanInfo info for this role
        final String ejbName = role.getRelationshipRoleSource().getEjbName();
        final EnterpriseBeanInfo enterpriseBeanInfo = infos.get(ejbName);
        if (enterpriseBeanInfo == null) {
            throw new OpenEJBException("Relation role source ejb not found " + ejbName);
        }
        if (!(enterpriseBeanInfo instanceof EntityBeanInfo)) {
            throw new OpenEJBException("Relation role source ejb is not an entity bean " + ejbName);
        }
        final EntityBeanInfo entityBeanInfo = (EntityBeanInfo) enterpriseBeanInfo;
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

            if (cmrFieldInfo.fieldType == null && relatedRole.getMultiplicity() == Multiplicity.MANY) {
                cmrFieldInfo.fieldType = Collection.class.getName();
            }
        } else {
            final String relatedEjbName = relatedRole.getRelationshipRoleSource().getEjbName();
            final EnterpriseBeanInfo relatedEjb = infos.get(relatedEjbName);
            if (relatedEjb == null) {
                throw new OpenEJBException("Relation role source ejb not found " + relatedEjbName);
            }
            if (!(relatedEjb instanceof EntityBeanInfo)) {
                throw new OpenEJBException("Relation role source ejb is not an entity bean " + relatedEjbName);
            }
            final EntityBeanInfo relatedEntity = (EntityBeanInfo) relatedEjb;

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

    private void initInterceptors(final EjbModule jar, final EjbJarInfo ejbJar) throws OpenEJBException {
        if (jar.getEjbJar().getInterceptors().length == 0) {
            return;
        }
        if (jar.getEjbJar().getAssemblyDescriptor() == null) {
            return;
        }
        if (jar.getEjbJar().getAssemblyDescriptor().getInterceptorBinding() == null) {
            return;
        }

        for (final Interceptor s : jar.getEjbJar().getInterceptors()) {
            final InterceptorInfo info = new InterceptorInfo();

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

        for (final InterceptorBinding binding : jar.getEjbJar().getAssemblyDescriptor().getInterceptorBinding()) {
            final InterceptorBindingInfo info = new InterceptorBindingInfo();
            info.ejbName = binding.getEjbName();
            info.excludeClassInterceptors = binding.getExcludeClassInterceptors();
            info.excludeDefaultInterceptors = binding.getExcludeDefaultInterceptors();
            info.interceptors.addAll(binding.getInterceptorClass());
            if (binding.getInterceptorOrder() != null) {
                info.interceptorOrder.addAll(binding.getInterceptorOrder().getInterceptorClass());
            }

            info.method = toInfo(binding.getMethod());
            info.className = binding.getClassName();
            ejbJar.interceptorBindings.add(info);
        }
    }

    private void initMethodTransactions(final EjbModule jar, final Map ejbds, final EjbJarInfo ejbJarInfo) {
        final List<ContainerTransaction> containerTransactions = jar.getEjbJar().getAssemblyDescriptor().getContainerTransaction();
        for (final ContainerTransaction cTx : containerTransactions) {
            final MethodTransactionInfo info = new MethodTransactionInfo();

            info.description = cTx.getDescription();
            info.transAttribute = cTx.getTransAttribute().toString();
            info.methods.addAll(getMethodInfos(cTx.getMethod(), ejbds));
            ejbJarInfo.methodTransactions.add(info);
        }
    }

    private void initMethodConcurrency(final EjbModule jar, final Map ejbds, final EjbJarInfo ejbJarInfo) {
        final List<ContainerConcurrency> containerConcurrency = jar.getEjbJar().getAssemblyDescriptor().getContainerConcurrency();
        for (final ContainerConcurrency att : containerConcurrency) {
            final MethodConcurrencyInfo info = new MethodConcurrencyInfo();

            info.description = att.getDescription();
            if (att.getLock() != null) {
                info.concurrencyAttribute = att.getLock().toString();
            }
            info.accessTimeout = toInfo(att.getAccessTimeout());

            info.methods.addAll(getMethodInfos(att.getMethod(), ejbds));
            ejbJarInfo.methodConcurrency.add(info);
        }
    }

    private void copyConcurrentMethods(final SessionBean bean, final EjbJarInfo ejbJarInfo, final Map ejbds) {
        for (final ConcurrentMethod method : bean.getConcurrentMethod()) {
            final MethodConcurrencyInfo info = new MethodConcurrencyInfo();

            if (method.getLock() != null) {
                info.concurrencyAttribute = method.getLock().toString();
            }
            info.accessTimeout = toInfo(method.getAccessTimeout());

            final Method m = new Method(bean.getEjbName(), null, method.getMethod().getMethodName());
            m.setMethodParams(method.getMethod().getMethodParams());
            info.methods.add(getMethodInfo(m, ejbds));
            ejbJarInfo.methodConcurrency.add(info);
        }
    }

    private void copySchedules(final List<Timer> timers, final List<MethodScheduleInfo> scheduleInfos) {
        final Map<NamedMethod, MethodScheduleInfo> methodScheduleInfoMap = new HashMap<>();
        for (final Timer timer : timers) {
            final NamedMethod timeoutMethod = timer.getTimeoutMethod();
            MethodScheduleInfo methodScheduleInfo = methodScheduleInfoMap.get(timer.getTimeoutMethod());
            if (methodScheduleInfo == null) {
                methodScheduleInfo = new MethodScheduleInfo();
                methodScheduleInfoMap.put(timeoutMethod, methodScheduleInfo);
                methodScheduleInfo.method = toInfo(timeoutMethod);
            }
            final ScheduleInfo scheduleInfo = new ScheduleInfo();
            //Copy TimerSchedule
            final TimerSchedule timerSchedule = timer.getSchedule();
            if (timerSchedule != null) {
                scheduleInfo.second = timerSchedule.getSecond();
                scheduleInfo.minute = timerSchedule.getMinute();
                scheduleInfo.hour = timerSchedule.getHour();
                scheduleInfo.dayOfWeek = timerSchedule.getDayOfWeek();
                scheduleInfo.dayOfMonth = timerSchedule.getDayOfMonth();
                scheduleInfo.month = timerSchedule.getMonth();
                scheduleInfo.year = timerSchedule.getYear();
            }
            //Copy other attributes
            scheduleInfo.timezone = timer.getTimezone();
            if (timer.getStart() != null) {
                scheduleInfo.start = timer.getStart().toGregorianCalendar().getTime();
            }
            if (timer.getEnd() != null) {
                scheduleInfo.end = timer.getEnd().toGregorianCalendar().getTime();
            }

            scheduleInfo.info = timer.getInfo();
            if (timer.getPersistent() != null) {
                scheduleInfo.persistent = timer.getPersistent();
            }

            methodScheduleInfo.schedules.add(scheduleInfo);
        }
        scheduleInfos.addAll(methodScheduleInfoMap.values());
    }

    private void initApplicationExceptions(final EjbModule jar, final EjbJarInfo ejbJarInfo) {
        for (final ApplicationException applicationException : jar.getEjbJar().getAssemblyDescriptor().getApplicationException()) {
            final ApplicationExceptionInfo info = new ApplicationExceptionInfo();
            info.exceptionClass = applicationException.getExceptionClass();
            info.rollback = applicationException.isRollback();
            info.inherited = applicationException.isInherited();
            ejbJarInfo.applicationException.add(info);
        }
    }

    private void initSecurityRoles(final EjbModule jar, final EjbJarInfo ejbJarInfo) {

        final List<SecurityRole> roles = jar.getEjbJar().getAssemblyDescriptor().getSecurityRole();

        for (final SecurityRole sr : roles) {
            final SecurityRoleInfo info = new SecurityRoleInfo();

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

    private void initMethodPermissions(final EjbModule jar, final Map ejbds, final EjbJarInfo ejbJarInfo) {

        final List<MethodPermission> methodPermissions = jar.getEjbJar().getAssemblyDescriptor().getMethodPermission();

        for (final MethodPermission mp : methodPermissions) {
            final MethodPermissionInfo info = new MethodPermissionInfo();

            info.description = mp.getDescription();
            info.roleNames.addAll(mp.getRoleName());
            info.methods.addAll(getMethodInfos(mp.getMethod(), ejbds));
            info.unchecked = mp.getUnchecked();

            ejbJarInfo.methodPermissions.add(info);
        }
    }

    private void initExcludesList(final EjbModule jar, final Map ejbds, final EjbJarInfo ejbJarInfo) {

        final ExcludeList methodPermissions = jar.getEjbJar().getAssemblyDescriptor().getExcludeList();

        for (final Method excludedMethod : methodPermissions.getMethod()) {
            ejbJarInfo.excludeList.add(getMethodInfo(excludedMethod, ejbds));
        }
    }

    private void resolveRoleLinks(final EnterpriseBeanInfo bean, final JndiConsumer item) {
        if (!(item instanceof RemoteBean)) {
            return;
        }

        final RemoteBean rb = (RemoteBean) item;

        final List<SecurityRoleRef> refs = rb.getSecurityRoleRef();
        for (final SecurityRoleRef ref : refs) {
            final SecurityRoleReferenceInfo info = new SecurityRoleReferenceInfo();

            info.description = ref.getDescription();
            info.roleLink = ref.getRoleLink();
            info.roleName = ref.getRoleName();

            if (info.roleLink == null) {
                info.roleLink = info.roleName;
            }
            bean.securityRoleReferences.add(info);
        }
    }

    private List<MethodInfo> getMethodInfos(final List<Method> ms, final Map ejbds) {
        if (ms == null) {
            return Collections.emptyList();
        }

        final List<MethodInfo> mi = new ArrayList<>(ms.size());
        for (final Method method : ms) {
            final MethodInfo methodInfo = getMethodInfo(method, ejbds);
            mi.add(methodInfo);
        }

        return mi;
    }

    private MethodInfo getMethodInfo(final Method method, final Map ejbds) {
        final MethodInfo methodInfo = new MethodInfo();

        final EjbDeployment d = (EjbDeployment) ejbds.get(method.getEjbName());

        methodInfo.description = method.getDescription();
        methodInfo.ejbDeploymentId = d == null ? null : d.getDeploymentId();
        methodInfo.ejbName = method.getEjbName();
        methodInfo.methodIntf = method.getMethodIntf() == null ? null : method.getMethodIntf().toString();
        methodInfo.methodName = method.getMethodName();
        if (methodInfo.methodName == null || methodInfo.methodName.isEmpty()) {
            methodInfo.methodName = "*";
        }
        methodInfo.className = method.getClassName();
        if (methodInfo.className == null || methodInfo.className.isEmpty()) {
            methodInfo.className = "*";
        }

        final MethodParams mp = method.getMethodParams();
        if (mp != null) {
            methodInfo.methodParams = mp.getMethodParam();
        }
        return methodInfo;
    }

    private EnterpriseBeanInfo initSessionBean(final SessionBean s, final EjbJarInfo ejbJar, final Map m) throws OpenEJBException {
        EnterpriseBeanInfo bean;

        if (s.getSessionType() == SessionType.STATEFUL) {
            bean = new StatefulBeanInfo();
            bean.passivable = s.getPassivationCapable() == null || s.getPassivationCapable();
            final StatefulBeanInfo stateful = (StatefulBeanInfo) bean;

            copyCallbacks(s.getPostActivate(), stateful.postActivate);
            copyCallbacks(s.getPrePassivate(), stateful.prePassivate);

            copyCallbacks(s.getAfterBegin(), stateful.afterBegin);
            copyCallbacks(s.getBeforeCompletion(), stateful.beforeCompletion);
            copyCallbacks(s.getAfterCompletion(), stateful.afterCompletion);

            for (final InitMethod initMethod : s.getInitMethod()) {
                final InitMethodInfo init = new InitMethodInfo();
                init.beanMethod = toInfo(initMethod.getBeanMethod());
                init.createMethod = toInfo(initMethod.getCreateMethod());
                stateful.initMethods.add(init);
            }

            for (final RemoveMethod removeMethod : s.getRemoveMethod()) {
                final RemoveMethodInfo remove = new RemoveMethodInfo();
                remove.beanMethod = toInfo(removeMethod.getBeanMethod());
                remove.retainIfException = removeMethod.getRetainIfException();
                stateful.removeMethods.add(remove);
            }

            copyConcurrentMethods(s, ejbJar, m);

        } else if (s.getSessionType() == SessionType.MANAGED) {
            bean = new ManagedBeanInfo();
            final ManagedBeanInfo managed = (ManagedBeanInfo) bean;
            // this way we support managed beans in ejb-jar.xml (not in the spec but can be useful)
            managed.hidden = !(s instanceof ManagedBean) || ((ManagedBean) s).isHidden();

            copyCallbacks(s.getPostActivate(), managed.postActivate);
            copyCallbacks(s.getPrePassivate(), managed.prePassivate);

            for (final RemoveMethod removeMethod : s.getRemoveMethod()) {
                final RemoveMethodInfo remove = new RemoveMethodInfo();
                remove.beanMethod = toInfo(removeMethod.getBeanMethod());
                remove.retainIfException = removeMethod.getRetainIfException();
                managed.removeMethods.add(remove);
            }

        } else if (s.getSessionType() == SessionType.SINGLETON) {
            bean = new SingletonBeanInfo();
            final ConcurrencyManagementType type = s.getConcurrencyManagementType();
            bean.concurrencyType = type != null ? type.toString() : ConcurrencyManagementType.CONTAINER.toString();
            bean.loadOnStartup = s.getInitOnStartup();

            copyCallbacks(s.getAroundTimeout(), bean.aroundTimeout);
            copySchedules(s.getTimer(), bean.methodScheduleInfos);
            // See JndiEncInfoBuilder.buildDependsOnRefs for processing of DependsOn
            // bean.dependsOn.addAll(s.getDependsOn());

            copyConcurrentMethods(s, ejbJar, m);
        } else {
            bean = new StatelessBeanInfo();
            copySchedules(s.getTimer(), bean.methodScheduleInfos);
        }

        if (s.getSessionType() != SessionType.STATEFUL) {
            copyCallbacks(s.getAroundTimeout(), bean.aroundTimeout);
        }

        bean.localbean = s.getLocalBean() != null;


        bean.timeoutMethod = toInfo(s.getTimeoutMethod());

        copyCallbacks(s.getAroundInvoke(), bean.aroundInvoke);
        copyCallbacks(s.getPostConstruct(), bean.postConstruct);
        copyCallbacks(s.getPreDestroy(), bean.preDestroy);

        copyAsynchronous(s.getAsyncMethod(), bean.asynchronous);
        bean.asynchronousClasses.addAll(s.getAsynchronousClasses());


        final EjbDeployment d = (EjbDeployment) m.get(s.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                + s.getEjbName()
                + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        final Icon icon = s.getIcon();
        bean.largeIcon = icon == null ? null : icon.getLargeIcon();
        bean.smallIcon = icon == null ? null : icon.getSmallIcon();
        bean.description = s.getDescription();
        bean.displayName = s.getDisplayName();
        bean.ejbClass = s.getEjbClass();
        bean.ejbName = s.getEjbName();
        bean.home = s.getHome();
        bean.remote = s.getRemote();
        bean.localHome = s.getLocalHome();
        bean.local = s.getLocal();
        bean.proxy = s.getProxy();
        bean.parents.addAll(s.getParents());
        bean.businessLocal.addAll(s.getBusinessLocal());
        bean.businessRemote.addAll(s.getBusinessRemote());
        final TransactionType txType = s.getTransactionType();
        bean.transactionType = txType != null ? txType.toString() : TransactionType.CONTAINER.toString();
        bean.serviceEndpoint = s.getServiceEndpoint();
        bean.properties.putAll(d.getProperties());

        bean.statefulTimeout = toInfo(s.getStatefulTimeout());

        bean.restService = s.isRestService() && !(s instanceof StatefulBean);

        return bean;
    }

    private void copyAsynchronous(final List<AsyncMethod> methods, final List<NamedMethodInfo> methodInfos) {
        for (final AsyncMethod asyncMethod : methods) {
            final NamedMethodInfo info = new NamedMethodInfo();
            info.methodName = asyncMethod.getMethodName();
            if (asyncMethod.getMethodParams() != null) {
                info.methodParams = asyncMethod.getMethodParams().getMethodParam();
            }
            methodInfos.add(info);
        }
    }

    private TimeoutInfo toInfo(final Timeout timeout) {
        if (timeout == null) {
            return null;
        }

        final TimeoutInfo accessTimeout = new TimeoutInfo();
        accessTimeout.time = timeout.getTimeout();
        accessTimeout.unit = timeout.getUnit().toString();
        return accessTimeout;
    }

    private EnterpriseBeanInfo initMessageBean(final MessageDrivenBean mdb, final Map m) throws OpenEJBException {
        final MessageDrivenBeanInfo bean = new MessageDrivenBeanInfo();

        bean.timeoutMethod = toInfo(mdb.getTimeoutMethod());
        copyCallbacks(mdb.getAroundTimeout(), bean.aroundTimeout);

        copyCallbacks(mdb.getAroundInvoke(), bean.aroundInvoke);
        copyCallbacks(mdb.getPostConstruct(), bean.postConstruct);
        copyCallbacks(mdb.getPreDestroy(), bean.preDestroy);

        copySchedules(mdb.getTimer(), bean.methodScheduleInfos);

        final EjbDeployment d = (EjbDeployment) m.get(mdb.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                + mdb.getEjbName()
                + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        final Icon icon = mdb.getIcon();
        bean.largeIcon = icon == null ? null : icon.getLargeIcon();
        bean.smallIcon = icon == null ? null : icon.getSmallIcon();
        bean.description = mdb.getDescription();
        bean.displayName = mdb.getDisplayName();
        bean.ejbClass = mdb.getEjbClass();
        bean.ejbName = mdb.getEjbName();
        final TransactionType txType = mdb.getTransactionType();
        bean.transactionType = txType != null ? txType.toString() : TransactionType.CONTAINER.toString();
        bean.properties.putAll(d.getProperties());

        if (mdb.getMessagingType() != null) {
            bean.mdbInterface = mdb.getMessagingType();
        } else {
            bean.mdbInterface = "jakarta.jms.MessageListener";
        }

        final ResourceLink resourceLink = d.getResourceLink("openejb/destination");
        if (resourceLink != null) {
            bean.destinationId = resourceLink.getResId();
        }

        if (mdb.getMessageDestinationType() != null) {
            bean.activationProperties.put("destinationType", mdb.getMessageDestinationType());
        }

        final ActivationConfig activationConfig = mdb.getActivationConfig();
        if (activationConfig != null) {
            for (final ActivationConfigProperty property : activationConfig.getActivationConfigProperty()) {
                final String name = property.getActivationConfigPropertyName();
                final String value = property.getActivationConfigPropertyValue();
                bean.activationProperties.put(name, value);
            }
        }

        return bean;
    }

    private NamedMethodInfo toInfo(final NamedMethod method) {
        if (method == null) {
            return null;
        }

        final NamedMethodInfo info = new NamedMethodInfo();

        info.methodName = method.getMethodName();

        if (method.getMethodParams() != null) {
            info.methodParams = method.getMethodParams().getMethodParam();
        }

        return info;
    }

    private void copyCallbacks(final List<? extends CallbackMethod> from, final List<CallbackInfo> to) {
        for (final CallbackMethod callback : from) {
            final CallbackInfo info = new CallbackInfo();
            info.className = callback.getClassName();
            info.method = callback.getMethodName();
            to.add(info);
        }
    }

    private EnterpriseBeanInfo initEntityBean(final EntityBean e, final Map m) throws OpenEJBException {
        final EntityBeanInfo bean = new EntityBeanInfo();

        final EjbDeployment d = (EjbDeployment) m.get(e.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                + e.getEjbName()
                + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();
        bean.containerId = d.getContainerId();

        final Icon icon = e.getIcon();
        bean.largeIcon = icon == null ? null : icon.getLargeIcon();
        bean.smallIcon = icon == null ? null : icon.getSmallIcon();
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
        bean.reentrant = String.valueOf(e.getReentrant());
        bean.properties.putAll(d.getProperties());

        final CmpVersion cmpVersion = e.getCmpVersion();
        if (e.getPersistenceType() == PersistenceType.CONTAINER) {
            if (cmpVersion != null && cmpVersion == CmpVersion.CMP1) {
                bean.cmpVersion = 1;
            } else {
                bean.cmpVersion = 2;
            }
        }

        final List<CmpField> cmpFields = e.getCmpField();
        for (final CmpField cmpField : cmpFields) {
            bean.cmpFieldNames.add(cmpField.getFieldName());
        }

        if (bean.persistenceType.equalsIgnoreCase("Container")) {
            for (final Query q : e.getQuery()) {
                final QueryInfo query = new QueryInfo();
                query.queryStatement = q.getEjbQl().trim();

                final MethodInfo method = new MethodInfo();
                method.ejbName = bean.ejbName;
                method.className = "*";

                final QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                if (qm.getMethodParams() != null) {
                    method.methodParams = qm.getMethodParams().getMethodParam();
                }
                query.method = method;
                final ResultTypeMapping resultType = q.getResultTypeMapping();
                if (ResultTypeMapping.REMOTE.equals(resultType)) {
                    query.remoteResultType = true;
                }
                bean.queries.add(query);
            }

            for (final org.apache.openejb.jee.oejb3.Query q : d.getQuery()) {
                final QueryInfo query = new QueryInfo();
                query.description = q.getDescription();
                query.queryStatement = q.getObjectQl().trim();

                final MethodInfo method = new MethodInfo();
                method.ejbName = bean.ejbName;
                method.className = "*";
                final org.apache.openejb.jee.oejb3.QueryMethod qm = q.getQueryMethod();
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
