package org.openejb.assembler.spring;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.assembler.classic.EnterpriseBeanInfo;
import org.openejb.assembler.classic.EntityBeanInfo;
import org.openejb.assembler.classic.QueryInfo;
import org.openejb.core.DeploymentContext;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.util.Messages;
import org.openejb.util.SafeToolkit;

class EnterpriseBeanBuilder {
    protected static final Messages messages = new Messages("org.openejb.util.resources");
    private final EnterpriseBeanInfo bean;
    private final EjbType ejbType;
    private final ClassLoader cl;

    public EnterpriseBeanBuilder(ClassLoader cl, EnterpriseBeanInfo bean) {
        this.bean = bean;

        if (bean.type == EnterpriseBeanInfo.STATEFUL) {
            ejbType = EjbType.STATEFUL;
        } else if (bean.type == EnterpriseBeanInfo.STATELESS) {
            ejbType = EjbType.STATELESS;
        } else if (bean.type == EnterpriseBeanInfo.ENTITY) {
            String persistenceType = ((EntityBeanInfo) bean).persistenceType;
            ejbType = (persistenceType.equalsIgnoreCase("Container")) ? EjbType.CMP_ENTITY : EjbType.BMP_ENTITY;
        } else {
            throw new UnsupportedOperationException("No building support for bean type: " + bean);
        }
        this.cl = cl;
    }

    static class Loader {
        protected static final Messages messages = new Messages("org.openejb.util.resources");
        private final ClassLoader classLoader;
        private final String ejbDeploymentId;

        public Loader(String codebase, String ejbDeploymentId) throws OpenEJBException {
            try {
                this.classLoader = new URLClassLoader(new URL[]{new File(codebase).toURL()});
            } catch (MalformedURLException e) {
                throw new OpenEJBException(messages.format("cl0001", codebase, e.getMessage()));
            }
            this.ejbDeploymentId = ejbDeploymentId;
        }

        public Class load(String className, String artifact) throws OpenEJBException {
            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException(messages.format("classNotFound." + artifact, className, ejbDeploymentId, e.getMessage()));
            }
        }
    }

    public Object build() throws OpenEJBException {
        Class ejbClass = loadClass(bean.ejbClass, "classNotFound.ejbClass");

        Class home = null;
        Class remote = null;
        if (bean.home != null) {
            home = loadClass(bean.home, "classNotFound.home");
            remote = loadClass(bean.remote, "classNotFound.remote");
        }

        Class localhome = null;
        Class local = null;
        if (bean.localHome != null) {
            localhome = loadClass(bean.localHome, "classNotFound.localHome");
            local = loadClass(bean.local, "classNotFound.local");
        }

        Class primaryKey = null;
        if (ejbType.isEntity() && ((EntityBeanInfo) bean).primKeyClass != null) {
            String className = ((EntityBeanInfo) bean).primKeyClass;
            primaryKey = loadClass(className, "classNotFound.primaryKey");
        }
        final String transactionType = bean.transactionType;

        JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(bean.jndiEnc, transactionType, ejbType);
        IvmContext root = (IvmContext) jndiEncBuilder.build();

        DeploymentContext deploymentContext = new DeploymentContext(bean.ejbDeploymentId, ejbClass.getClassLoader(), root);
        DeploymentInfo deployment = new DeploymentInfo(deploymentContext, home, remote, localhome, local, ejbClass, primaryKey, ejbType.getType(), null);

        if (ejbType.isSession()) {
            deployment.setBeanManagedTransaction("Bean".equalsIgnoreCase(bean.transactionType));
        }

        if (ejbType.isEntity()) {
            EntityBeanInfo entity = (EntityBeanInfo) bean;

            deployment.setIsReentrant(entity.reentrant.equalsIgnoreCase("true"));

            if (ejbType == EjbType.CMP_ENTITY) {
                QueryInfo[] queries = (entity.queries == null) ? new QueryInfo[]{} : entity.queries;
                for (int i = 0; i < queries.length; i++) {
                    QueryInfo query = queries[i];

                    Vector finderMethods = new Vector();

                    if (home != null) {
                        AssemblerTool.resolveMethods(finderMethods, home, query.method);
                    }
                    if (localhome != null) {
                        AssemblerTool.resolveMethods(finderMethods, localhome, query.method);
                    }
                    for (int j = 0; j < finderMethods.size(); j++) {
                        deployment.addQuery((Method) finderMethods.elementAt(j), query.queryStatement);
                    }
                }
                deployment.setCmrFields(entity.cmpFieldNames);

                if (entity.primKeyField != null) {
                    try {
                        deployment.setPrimKeyField(entity.primKeyField);
                    } catch (NoSuchFieldException e) {
                        throw new SystemException("Can not set prim-key-field on deployment " + entity.ejbDeploymentId, e);
                    }
                }
            }
        }
        return deployment;
    }

    private Class loadClass(String className, String messageCode) throws OpenEJBException {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            String message = SafeToolkit.messages.format("cl0007", className, bean.codebase);
            throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message));
        }
    }

}
