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
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.QueryInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.SecurityRoleReferenceInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.assembler.classic.StatelessBeanInfo;
import org.apache.openejb.assembler.classic.LifecycleCallbackInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.jee.CmpField;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.Method;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SecurityRole;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.CmpVersion;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


        List<EnterpriseBeanInfo> beanInfos = new ArrayList<EnterpriseBeanInfo>(ejbds.size());
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
            beanInfos.add(beanInfo);

            if (deploymentIds.contains(beanInfo.ejbDeploymentId)) {
                ConfigUtils.logger.i18n.warning("conf.0100", beanInfo.ejbDeploymentId, jar.getJarURI(), beanInfo.ejbName);

                return null;
            }

            deploymentIds.add(beanInfo.ejbDeploymentId);

            beanInfo.codebase = jar.getJarURI();
            infos.put(beanInfo.ejbName, beanInfo);
            items.put(beanInfo.ejbName, bean);


        }

        EnterpriseBeanInfo[] beans = beanInfos.toArray(new EnterpriseBeanInfo[]{});
        ejbJar.enterpriseBeans = beans;

        initJndiReferences(ejbds, infos, items);

        if (jar.getEjbJar().getAssemblyDescriptor() != null) {
            initSecurityRoles(jar);
            initMethodPermissions(jar, ejbds);
            initMethodTransactions(jar, ejbds);

            for (int x = 0; x < beans.length; x++) {
                resolveRoleLinks(jar, beans[x], items.get(beans[x].ejbName));
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
            info.methods = getMethodInfos(cTx.getMethod(), ejbds);
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
            info.roleNames = mp.getRoleName().toArray(new String[]{});
            info.methods = getMethodInfos(mp.getMethod(), ejbds);

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
        List<SecurityRoleReferenceInfo> infos = new ArrayList<SecurityRoleReferenceInfo>();
        for (SecurityRoleRef ref : refs) {
            SecurityRoleReferenceInfo info = new SecurityRoleReferenceInfo();

            info.description = ref.getDescription();
            info.roleLink = ref.getRoleLink();
            info.roleName = ref.getRoleName();

            if (info.roleLink == null) {
                ConfigUtils.logger.i18n.warning("conf.0009", info.roleName, bean.ejbName, jar.getJarURI());
                info.roleLink = DEFAULT_SECURITY_ROLE;
            }
            infos.add(info);
        }
        bean.securityRoleReferences = infos.toArray(new SecurityRoleReferenceInfo[]{});
    }

    private MethodInfo[] getMethodInfos(List<Method> ms, Map ejbds) {
        if (ms == null) return null;

        MethodInfo[] mi = new MethodInfo[ms.size()];
        for (int i = 0; i < mi.length; i++) {

            mi[i] = new MethodInfo();

            Method method = ms.get(i);
            EjbDeployment d = (EjbDeployment) ejbds.get(method.getEjbName());

            mi[i].description = method.getDescription();
            mi[i].ejbDeploymentId = d.getDeploymentId();
            mi[i].methodIntf = (method.getMethodIntf() == null) ? null : method.getMethodIntf().toString();
            mi[i].methodName = method.getMethodName();

            MethodParams mp = method.getMethodParams();
            if (mp != null) {
                mi[i].methodParams = mp.getMethodParam().toArray(new String[]{});
            }
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
        bean.cmpFieldNames = new String[cmpFields.size()];

        for (int i = 0; i < bean.cmpFieldNames.length; i++) {
            bean.cmpFieldNames[i] = cmpFields.get(i).getFieldName();
        }

        if (bean.persistenceType.equalsIgnoreCase("Container")) {
            List<QueryInfo> qi = new ArrayList<QueryInfo>();
            for (org.apache.openejb.alt.config.ejb.Query q : d.getQuery()) {
                QueryInfo query = new QueryInfo();
                query.description = q.getDescription();
                query.queryStatement = q.getObjectQl().trim();

                MethodInfo method = new MethodInfo();
                org.apache.openejb.alt.config.ejb.QueryMethod qm = q.getQueryMethod();
                method.methodName = qm.getMethodName();
                method.methodParams = qm.getMethodParams().getMethodParam().toArray(new String[]{});
                query.method = method;
                qi.add(query);
            }
            bean.queries = qi.toArray(new QueryInfo[]{});
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
