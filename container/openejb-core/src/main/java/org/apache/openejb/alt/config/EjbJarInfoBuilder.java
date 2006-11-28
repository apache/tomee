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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EntityBeanInfo;
import org.apache.openejb.assembler.classic.InterceptorInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.LifecycleCallbackInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.MethodInterceptorInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.QueryInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.assembler.classic.StatelessBeanInfo;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.loader.SystemInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarInfoBuilder {

    public static final String DEFAULT_SECURITY_ROLE = "openejb.default.security.role";
    public static List<String> deploymentIds = new ArrayList<String>();
    public static List<String> securityRoles = new ArrayList<String>();
    private List<MethodPermissionInfo> methodPermissionInfos = new ArrayList<MethodPermissionInfo>();
    private List<MethodTransactionInfo> methodTransactionInfos = new ArrayList<MethodTransactionInfo>();
    private List<SecurityRoleInfo> securityRoleInfos = new ArrayList<SecurityRoleInfo>();


    public EjbJarInfo buildInfo(EjbModule jar) throws OpenEJBException {

        int beansDeployed = jar.getOpenejbJar().getEjbDeploymentCount();
        int beansInEjbJar = jar.getEjbJar().getEnterpriseBeans().length;

        if (beansInEjbJar != beansDeployed) {
            ConfigUtils.logger.i18n.warning("conf.0008", jar.getJarURI(), "" + beansInEjbJar, "" + beansDeployed);

            return null;
        }

        Map<String, EjbDeployment> ejbds = jar.getOpenejbJar().getDeploymentsByEjbName();
        Map<String, EnterpriseBeanInfo> infos = new HashMap<String, EnterpriseBeanInfo>();
        Map<String, EnterpriseBean> items = new HashMap<String, EnterpriseBean>();

        EjbJarInfo ejbJar = new EjbJarInfo();
        ejbJar.jarPath = jar.getJarURI();


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
                ConfigUtils.logger.i18n.warning("conf.0100", beanInfo.ejbDeploymentId, jar.getJarURI(), beanInfo.ejbName);

                return null;
            }

            deploymentIds.add(beanInfo.ejbDeploymentId);

            beanInfo.codebase = jar.getJarURI();
            infos.put(beanInfo.ejbName, beanInfo);
            items.put(beanInfo.ejbName, bean);


        }

        initJndiReferences(ejbds, infos, items);

        if (jar.getEjbJar().getAssemblyDescriptor() != null) {
            initInterceptors(jar, ejbJar, infos);
            initSecurityRoles(jar);
            initMethodPermissions(jar, ejbds);
            initMethodTransactions(jar, ejbds);

            for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                resolveRoleLinks(jar, bean, items.get(bean.ejbName));
            }
        }

        if (!"tomcat-webapp".equals(SystemInstance.get().getProperty("openejb.loader"))) {
//            try {
//                File jarFile = new File(jar.getJarURI());
//
//                SystemInstance.get().getClassPath().addJarToPath(jarFile.toURL());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        return ejbJar;
    }

    private void initInterceptors(EjbModule jar, EjbJarInfo ejbJar, Map<String, EnterpriseBeanInfo> beanInfos) throws OpenEJBException {
        if (jar.getEjbJar().getInterceptors() == null) {
            // no interceptors to process
            return;
        }

        Map<String, String> interceptorMethods = new HashMap<String, String>();
        List<Interceptor> interceptors = jar.getEjbJar().getInterceptors().getInterceptor();
        for (Interceptor interceptor : interceptors) {
            AroundInvoke aroundInvoke = interceptor.getAroundInvoke().iterator().next();
            String clazz = aroundInvoke.getClazz();
            String methodName = aroundInvoke.getMethodName();
            interceptorMethods.put(clazz, methodName);
        }

        List<InterceptorBinding> bindings = jar.getEjbJar().getAssemblyDescriptor().getInterceptorBinding();
        for (InterceptorBinding binding : bindings) {
            if (binding.getInterceptorOrder() != null) {
                continue;
            }

            String ejbName = binding.getEjbName();
            List<InterceptorInfo> interceptorInfos = getInterceptorInfos(binding.getInterceptorClass(), interceptorMethods);
            if ("*".equals(ejbName)) {
                ejbJar.defaultInterceptors.addAll(interceptorInfos);
            } else {
                EnterpriseBeanInfo beanInfo = beanInfos.get(ejbName);
                if (beanInfo == null) {
                    throw new OpenEJBException("Interceptor binding defined for a non existant ejb " + ejbName);
                }

                NamedMethod method = binding.getMethod();
                if (method == null) {
                    beanInfo.excludeDefaultInterceptors = binding.getExcludeDefaultInterceptors();
                    beanInfo.classInterceptors.addAll(interceptorInfos);
                } else {
                    MethodInterceptorInfo methodInterceptorInfo = new MethodInterceptorInfo();
                    methodInterceptorInfo.methodInfo = new MethodInfo();
                    methodInterceptorInfo.methodInfo.methodName = method.getMethodName();
                    List<String> methodParam = method.getMethodParams().getMethodParam();
                    methodInterceptorInfo.methodInfo.methodParams.addAll(methodParam);

                    methodInterceptorInfo.excludeDefaultInterceptors = binding.getExcludeDefaultInterceptors();
                    methodInterceptorInfo.excludeClassInterceptors = binding.getExcludeClassInterceptors();
                    methodInterceptorInfo.interceptors.addAll(interceptorInfos);

                    beanInfo.methodInterceptors.add(methodInterceptorInfo);
                }
            }
        }

        // todo add interceptor order
    }

    private static List<InterceptorInfo> getInterceptorInfos(List<String> interceptorClasses, Map<String, String> interceptorMethods) throws OpenEJBException {
        ArrayList<InterceptorInfo> interceptorInfos = new ArrayList<InterceptorInfo>(interceptorClasses.size());
        for (String clazz : interceptorClasses) {
            String methodName = interceptorMethods.get(clazz);
            if (methodName == null) {
                throw new OpenEJBException("Interceptor class is not have an interceptor method defined: " + interceptorClasses);
            }

            InterceptorInfo interceptorInfo = new InterceptorInfo();
            interceptorInfo.clazz = clazz;
            interceptorInfo.methodName = methodName;
            interceptorInfos.add(interceptorInfo);
        }
        return interceptorInfos;
    }

    private void initJndiReferences(Map<String, EjbDeployment> ejbds, Map<String, EnterpriseBeanInfo> beanInfos, Map<String, EnterpriseBean> beanData) throws OpenEJBException {

        JndiEncInfoBuilder jndiEncInfoBuilder = new JndiEncInfoBuilder(beanInfos.values(), null);

        for (EnterpriseBeanInfo beanInfo : beanInfos.values()) {

            String ejbName = beanInfo.ejbName;
            JndiConsumer jndiConsumer = beanData.get(ejbName);

            EjbDeployment ejbDeployment = ejbds.get(ejbName);

            // Link all the resource refs
            List<ResourceRef> resourceRefs = jndiConsumer.getResourceRef();
            for (ResourceRef res : resourceRefs) {
                String resId = ejbDeployment.getResourceLink(res.getResRefName()).getResId();
                if (resId != null /* don't overwrite with null */) {
                    res.setResLink(resId);
                }
            }

            JndiEncInfo jndi = jndiEncInfoBuilder.build(jndiConsumer, ejbName);

            beanInfo.jndiEnc = jndi;
        }
    }

    private void initMethodTransactions(EjbModule jar, Map ejbds) {

        List<ContainerTransaction> containerTransactions = jar.getEjbJar().getAssemblyDescriptor().getContainerTransaction();
        List<MethodTransactionInfo> infos = new ArrayList<MethodTransactionInfo>();
        for (ContainerTransaction cTx : containerTransactions) {
            MethodTransactionInfo info = new MethodTransactionInfo();

            info.description = cTx.getDescription();
            info.transAttribute = cTx.getTransAttribute().toString();
            info.methods.addAll(getMethodInfos(cTx.getMethod(), ejbds));
            infos.add(info);
        }
        getMethodTransactionInfos().addAll(infos);
    }

    private void initSecurityRoles(EjbModule jar) {

        List<SecurityRole> roles = jar.getEjbJar().getAssemblyDescriptor().getSecurityRole();
        List<SecurityRoleInfo> infos = new ArrayList<SecurityRoleInfo>();

        for (SecurityRole sr : roles) {
            SecurityRoleInfo info = new SecurityRoleInfo();

            info.description = sr.getDescription();
            info.roleName = sr.getRoleName();

            if (securityRoles.contains(sr.getRoleName())) {
                ConfigUtils.logger.i18n.warning("conf.0102", jar.getJarURI(), sr.getRoleName());
            } else {
                securityRoles.add(sr.getRoleName());
            }
            infos.add(info);
        }
        getSecurityRoleInfos().addAll(infos);
    }

    private void initMethodPermissions(EjbModule jar, Map ejbds) {

        List<MethodPermission> methodPermissions = jar.getEjbJar().getAssemblyDescriptor().getMethodPermission();
        List<MethodPermissionInfo> infos = new ArrayList<MethodPermissionInfo>();

        for (MethodPermission mp : methodPermissions) {
            MethodPermissionInfo info = new MethodPermissionInfo();

            info.description = mp.getDescription();
            info.roleNames.addAll(mp.getRoleName());
            info.methods.addAll(getMethodInfos(mp.getMethod(), ejbds));

            infos.add(info);
        }

        getMethodPermissionInfos().addAll(infos);
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
                ConfigUtils.logger.i18n.warning("conf.0009", info.roleName, bean.ejbName, jar.getJarURI());
                info.roleLink = DEFAULT_SECURITY_ROLE;
            }
            bean.securityRoleReferences.add(info);
        }
    }

    private List<MethodInfo> getMethodInfos(List<Method> ms, Map ejbds) {
        if (ms == null) return Collections.emptyList();

        List<MethodInfo> mi = new ArrayList<MethodInfo>(ms.size());
        for (Method method : ms) {
            MethodInfo methodInfo = new MethodInfo();

            EjbDeployment d = (EjbDeployment) ejbds.get(method.getEjbName());

            methodInfo.description = method.getDescription();
            methodInfo.ejbDeploymentId = d.getDeploymentId();
            methodInfo.methodIntf = (method.getMethodIntf() == null) ? null : method.getMethodIntf().toString();
            methodInfo.methodName = method.getMethodName();

            MethodParams mp = method.getMethodParams();
            if (mp != null) {
                methodInfo.methodParams.addAll(mp.getMethodParam());
            }
            mi.add(methodInfo);
        }

        return mi;
    }

    private EnterpriseBeanInfo initSessionBean(SessionBean s, Map m) throws OpenEJBException {
        EnterpriseBeanInfo bean = null;

        if (s.getSessionType() == SessionType.STATEFUL) {
            bean = new StatefulBeanInfo();
            copyCallbacks(s.getPostActivate(), ((StatefulBeanInfo) bean).postActivate);
            copyCallbacks(s.getPrePassivate(), ((StatefulBeanInfo) bean).prePassivate);
        } else {
            bean = new StatelessBeanInfo();
        }

        copyCallbacks(s.getPostConstruct(), bean.postConstruct);
        copyCallbacks(s.getPreDestroy(), bean.preDestroy);

        EjbDeployment d = (EjbDeployment) m.get(s.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + s.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

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
        bean.businessLocal = s.getBusinessLocal();
        bean.businessRemote = s.getBusinessRemote();
        TransactionType txType = s.getTransactionType();
        bean.transactionType = (txType != null)?txType.toString(): TransactionType.CONTAINER.toString();

        return bean;
    }

    private EnterpriseBeanInfo initMessageBean(MessageDrivenBean mdb, Map m) throws OpenEJBException {
        EnterpriseBeanInfo bean = new MessageDrivenBeanInfo();

        copyCallbacks(mdb.getPostConstruct(), bean.postConstruct);
        copyCallbacks(mdb.getPreDestroy(), bean.preDestroy);

        EjbDeployment d = (EjbDeployment) m.get(mdb.getEjbName());
        if (d == null) {
            throw new OpenEJBException("No deployment information in openejb-jar.xml for bean "
                    + mdb.getEjbName()
                    + ". Please redeploy the jar");
        }
        bean.ejbDeploymentId = d.getDeploymentId();

        Icon icon = mdb.getIcon();
        bean.largeIcon = (icon == null) ? null : icon.getLargeIcon();
        bean.smallIcon = (icon == null) ? null : icon.getSmallIcon();
        bean.description = mdb.getDescription();
        bean.displayName = mdb.getDisplayName();
        bean.ejbClass = mdb.getEjbClass();
        bean.ejbName = mdb.getEjbName();
        TransactionType txType = mdb.getTransactionType();
        bean.transactionType = (txType != null)?txType.toString(): TransactionType.CONTAINER.toString();

        return bean;
    }

    private void copyCallbacks(List<LifecycleCallback> from, List<LifecycleCallbackInfo> to) {
        for (LifecycleCallback callback : from) {
            LifecycleCallbackInfo info = new LifecycleCallbackInfo();
            info.className = callback.getLifecycleCallbackClass();
            info.method = callback.getLifecycleCallbackMethod();
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

        Icon icon = e.getIcon();
        bean.largeIcon = (icon == null) ? null : icon.getLargeIcon();
        bean.smallIcon = (icon == null) ? null : icon.getSmallIcon();
        bean.description = e.getDescription();
        bean.displayName = e.getDisplayName();
        bean.ejbClass = e.getEjbClass();
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

        CmpVersion cmpVersion = e.getCmpVersion();
        if (cmpVersion != null && cmpVersion == CmpVersion.CMP2){
            bean.cmpVersion = 2;
        } else if (cmpVersion != null && cmpVersion == CmpVersion.CMP1){
            bean.cmpVersion = 1;
        } else if (e.getPersistenceType() == PersistenceType.CONTAINER) {
            bean.cmpVersion = 1;
        }

        List<CmpField> cmpFields = e.getCmpField();
        for (CmpField cmpField : cmpFields) {
            bean.cmpFieldNames.add(cmpField.getFieldName());
        }

        if (bean.persistenceType.equalsIgnoreCase("Container")) {
            for (org.apache.openejb.alt.config.ejb.Query q : d.getQuery()) {
                QueryInfo query = new QueryInfo();
                query.description = q.getDescription();
                query.queryStatement = q.getObjectQl().trim();

                MethodInfo method = new MethodInfo();
                org.apache.openejb.alt.config.ejb.QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                method.methodParams.addAll(qm.getMethodParams().getMethodParam());
                query.method = method;
                bean.queries.add(query);
            }
        }
        return bean;
    }

    public List<MethodPermissionInfo> getMethodPermissionInfos() {
        return methodPermissionInfos;
    }

    public List<MethodTransactionInfo> getMethodTransactionInfos() {
        return methodTransactionInfos;
    }

    public List<SecurityRoleInfo> getSecurityRoleInfos() {
        return securityRoleInfos;
    }
}
