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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.monitoring;

import java.util.Collection;
import java.util.Properties;
import javax.management.Description;
import javax.management.MBean;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.core.LocalInitialContextFactory;

@MBean
@Description("OpenEJB Deployer")
public class JMXDeployer {
    @ManagedOperation
    @Description("Deploy the specified application")
    public String deploy(final String location) {
        try {
            deployer().deploy(location);
            return "OK";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    @ManagedOperation
    @Description("Undeploy the specified application")
    public String undeploy(final String moduleId) {
        try {
            deployer().undeploy(moduleId);
            return "OK";
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    @ManagedAttribute
    @Description("List available applications")
    public String[] getDeployedApplications() {
        try {
            final Collection<AppInfo> apps = deployer().getDeployedApps();
            final String[] appsNames = new String[apps.size()];
            int i = 0;
            for (AppInfo info : apps) {
                appsNames[i++] = info.path;
            }
            return appsNames;
        } catch (Exception e) {
            return new String[] { "ERR:" + e.getMessage() };
        }
    }

    private static Deployer deployer() throws NamingException {
        final Properties p = new Properties();
        p.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DeployerEjb.class.getClassLoader());
        try {
            return (Deployer) new InitialContext(p).lookup("openejb/DeployerBusinessRemote");
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
