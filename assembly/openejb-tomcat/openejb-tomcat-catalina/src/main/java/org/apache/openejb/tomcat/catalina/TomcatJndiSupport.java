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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.tomcat.catalina;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.RpcContainerWrapper;

import javax.naming.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TomcatJndiSupport extends RpcContainerWrapper {
    private final Method bindContext;
    private final Method bindThread;
    private final Method unbindThread;

    public TomcatJndiSupport(RpcContainer container) throws OpenEJBException {
        super(container);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Class contextBindings = classLoader.loadClass("org.apache.naming.ContextBindings");
            bindContext = contextBindings.getMethod("bindContext", Object.class, Context.class, Object.class);
            bindThread = contextBindings.getMethod("bindThread", Object.class, Object.class);
            unbindThread = contextBindings.getMethod("unbindThread", Object.class, Object.class);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Support requires the org.apache.naming.ContextBindings class to be available.");
        } catch (NoSuchMethodException e) {
            throw new OpenEJBException("Unable to setup Tomcat JNDI support.  Method of org.apache.naming.ContextBindings was not found:" + e.getMessage());
        }
        DeploymentInfo[] deploymentInfos = container.deployments();
        for (DeploymentInfo deploymentInfo : deploymentInfos) {
            CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentInfo;
            setupDeployment(deployment);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void init(Object containerId, HashMap deployments, Properties properties) throws OpenEJBException {
    }

    public void deploy(DeploymentInfo info) throws OpenEJBException {
        super.deploy(info);
        setupDeployment((org.apache.openejb.core.CoreDeploymentInfo) info);
    }

    public static Map<Object, Context> contexts = new HashMap<Object, Context>();

    private void setupDeployment(org.apache.openejb.core.CoreDeploymentInfo deployment) {

        deployment.setContainer(this);

        Object deploymentID = deployment.getDeploymentID();
        Context jndiEnc = deployment.getJndiEnc();
        bindContext(deploymentID, jndiEnc);
        contexts.put(deploymentID, jndiEnc);
    }

    public Object invoke(Object deployID, Method callMethod, Object[] args, Object primKey, Object securityIdentity) throws OpenEJBException {
        try {

            bindThread(deployID);
            return super.invoke(deployID, callMethod, args, primKey, securityIdentity);
        } finally {
            unbindThread(deployID);
        }
    }

    public void bindContext(Object name, Context context) {
        try {
            bindContext.invoke(null, name, context, name);
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindContext");
        }
    }

    public void bindThread(Object name) {
        try {
            bindThread.invoke(null, name, name);
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "bindThread");
        }
    }

    public void unbindThread(Object name) {
        try {
            unbindThread.invoke(null, name, name);
        } catch (Throwable e) {
            throw convertToRuntimeException(e, "unbindThread");
        }
    }

    private RuntimeException convertToRuntimeException(Throwable e, String methodName) {
        if (e instanceof InvocationTargetException) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                return (RuntimeException) cause;
            } else {
                e = cause;
            }
        }
        return new RuntimeException("ContextBindings." + methodName, e);
    }
}
