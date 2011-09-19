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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.AppContext;
import org.apache.openejb.Container;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.util.Messages;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class EjbJarBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");

    private final Properties props;
    private AppContext context;

    public EjbJarBuilder(Properties props, AppContext context) {
        this.props = props;
        this.context = context;
    }

    public HashMap<String, BeanContext> build(EjbJarInfo ejbJar, Collection<Injection> appInjections) throws OpenEJBException {
        InjectionBuilder injectionBuilder = new InjectionBuilder(context.getClassLoader());
        List<Injection> moduleInjections = injectionBuilder.buildInjections(ejbJar.moduleJndiEnc);
        moduleInjections.addAll(appInjections);
        Context moduleJndiContext = new JndiEncBuilder(ejbJar.moduleJndiEnc, moduleInjections, null, ejbJar.moduleName, ejbJar.moduleUri, ejbJar.uniqueId, context.getClassLoader())
            .build(JndiEncBuilder.JndiScope.module);

        HashMap<String, BeanContext> deployments = new HashMap<String, BeanContext>();

        ModuleContext moduleContext = new ModuleContext(ejbJar.moduleName, ejbJar.moduleUri, ejbJar.uniqueId, context, moduleJndiContext);
        InterceptorBindingBuilder interceptorBindingBuilder = new InterceptorBindingBuilder(context.getClassLoader(), ejbJar);

        MethodScheduleBuilder methodScheduleBuilder = new MethodScheduleBuilder();
        
        for (EnterpriseBeanInfo ejbInfo : ejbJar.enterpriseBeans) {
            try {
                EnterpriseBeanBuilder deploymentBuilder = new EnterpriseBeanBuilder(ejbInfo, new ArrayList<String>(), moduleContext, moduleInjections);
                BeanContext bean = deploymentBuilder.build();

                interceptorBindingBuilder.build(bean, ejbInfo);

                methodScheduleBuilder.build(bean, ejbInfo);
                
                deployments.put(ejbInfo.ejbDeploymentId, bean);

                // TODO: replace with get() on application context or parent
                Container container = (Container) props.get(ejbInfo.containerId);

                if (container == null) throw new IllegalStateException("Container does not exist: " + ejbInfo.containerId + ".  Referenced by deployment: " + bean.getDeploymentID());
                // Don't deploy to the container, yet. That will be done by deploy() once Assembler as finished configuring the DeploymentInfo
                bean.setContainer(container);
            } catch (Throwable e) {
                throw new OpenEJBException("Error building bean '" + ejbInfo.ejbName + "'.  Exception: " + e.getClass() + ": " + e.getMessage(), e);
            }
        }
        return deployments;
    }

}
