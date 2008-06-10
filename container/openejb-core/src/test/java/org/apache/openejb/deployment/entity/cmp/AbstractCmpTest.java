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
package org.apache.openejb.deployment.entity.cmp;

import java.sql.Connection;
import java.io.File;
import java.util.Collections;

import javax.sql.DataSource;

import org.apache.openejb.deployment.DeploymentHelper;
import org.apache.openejb.deployment.XmlBeansHelper;
import org.apache.openejb.deployment.CmpSchemaBuilder;
import org.apache.openejb.deployment.TranqlCmpSchemaBuilder;
import org.apache.openejb.deployment.CmpBuilder;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.axiondb.jdbc.AxionDataSource;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractCmpTest extends DeploymentHelper {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    protected final AbstractName moduleName = naming.createRootName(TEST_CONFIGURATION_ID, "MockModule", NameFactory.EJB_MODULE);
    protected DataSource ds;
    private AbstractName moduleCmpEngineName;
    private ConfigurationData configurationData;

    protected abstract void buildDBSchema(Connection c) throws Exception;

    protected abstract String getEjbJarDD();

    protected abstract String getOpenEjbJarDD();

    protected void initCmpModule() throws Exception {
        ds = new AxionDataSource("jdbc:axiondb:testdb");
        Connection c = ds.getConnection("root", null);
        buildDBSchema(c);

        File ejbJarFile = new File(basedir, getEjbJarDD());
        File openejbJarFile = new File(basedir, getOpenEjbJarDD());

        EjbJarType ejbJarType= XmlBeansHelper.loadEjbJar(ejbJarFile);
        OpenejbOpenejbJarType openejbJarType = XmlBeansHelper.loadOpenEjbJar(openejbJarFile);

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    null,
                    TEST_ENVIRONMENT,
                    ConfigurationModuleType.EJB,
                    kernel.getNaming(),
                    configurationManager,
                    Collections.EMPTY_SET,
                    new AbstractNameQuery(serverName),
                    moduleName,
                    new AbstractNameQuery(tmName),
                    new AbstractNameQuery(ctcName),
                    new AbstractNameQuery(txTimerName),
                    new AbstractNameQuery(nonTxTimerName),
                    null
            );

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // create module cmp engine GBeanData
            EJBModule ejbModule = new EJBModule(true, moduleName, TEST_ENVIRONMENT, null, tempDir.getAbsoluteFile().toURI().toString(), ejbJarType, openejbJarType, "", Collections.EMPTY_MAP);
            CmpSchemaBuilder cmpSchemaBuilder = new TranqlCmpSchemaBuilder();
            cmpSchemaBuilder.initContext(earContext, ejbModule, classLoader);
            cmpSchemaBuilder.addBeans(earContext, ejbModule, classLoader);
            moduleCmpEngineName = ejbModule.getModuleCmpEngineName();

            // initialize the contifuation
            configurationData = earContext.getConfigurationData();
            configurationData.getEnvironment().addDependency(new Dependency(BOOTSTRAP_ID, ImportType.ALL));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    protected void addCmpEjb(String ejbName, Class beanClass, Class homeClass, Class remoteClass, Class localHomeClass, Class localClass, Class primaryKeyClass, AbstractName containerName) throws Exception {
        CmpBuilder builder = new CmpBuilder();
        builder.setContainerId(containerName.toString());
        builder.setEjbName(ejbName);
        builder.setEjbContainerName(cmpEjbContainerName);
        builder.setBeanClassName(getName(beanClass));
        builder.setHomeInterfaceName(getName(homeClass));
        builder.setRemoteInterfaceName(getName(remoteClass));
        builder.setLocalHomeInterfaceName(getName(localHomeClass));
        builder.setLocalInterfaceName(getName(localClass));
        builder.setPrimaryKeyClassName(getName(primaryKeyClass));
        builder.setModuleCmpEngineName(moduleCmpEngineName);
        builder.setCmp2(true);

        GBeanData deployment = builder.createConfiguration();
        configurationData.addGBean(deployment);
    }

    private String getName(Class clazz) {
        if (clazz == null) return null;
        return clazz.getName();
    }

    protected void startConfiguration() throws NoSuchConfigException, LifecycleException {
        configurationManager.loadConfiguration(configurationData);
        configurationManager.startConfiguration(TEST_CONFIGURATION_ID);
    }

    protected void tearDown() throws Exception {
        Connection c = ds.getConnection();
        c.createStatement().execute("SHUTDOWN");
        super.tearDown();
    }
}
