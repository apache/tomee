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
package org.apache.openejb.assembler;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.sys.AdditionalDeployments;
import org.apache.openejb.config.sys.Deployments;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.WebArchives;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.naming.Context;
import javax.naming.NamingException;

import static org.apache.openejb.config.ConfigurationFactory.ADDITIONAL_DEPLOYMENTS;
import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class DeployerEjbTest {

    private static final AtomicReference<String> property = new AtomicReference<String>(null);
    private static final AtomicReference<File> warArchive = new AtomicReference<File>(null);
    private static final String OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS = "openejb.deployer.save-deployments";

    @BeforeClass
    public static void beforeClass() throws Exception {

        final FileUtils base = SystemInstance.get().getBase();
        final File conf = base.getDirectory("conf", false);
        Files.delete(conf);

        final File apps = base.getDirectory("apps", true);
        Files.delete(apps);

        base.getDirectory("apps", true);
        base.getDirectory("conf", true);

        property.set(System.getProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS));
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.TRUE.toString());
        warArchive.set(WebArchives.warArchive(TestClass.class));
    }

    @AfterClass
    public static void afterClass() {

        final String s = property.get();
        if (null != s) {
            System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, "true");
        } else {
            System.clearProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS);
        }

        final File file = warArchive.get();
        if (file != null && file.exists()) {
            if (!file.delete()) {
                file.deleteOnExit();
            }
        }

        try { // will make other tests failling otherwise since it would leak config
            Files.delete(SystemInstance.get().getBase().getDirectory("conf", false));
            Files.delete(SystemInstance.get().getBase().getDirectory("apps", false));
        } catch (final IOException e) {
            // no-op
        }
    }

    @Module
    @Classes(value = {DeployerEjb.class})
    public WebApp war() {
        return new WebApp().contextRoot("/initial");
    }

    @AppResource
    private Context context;

    @Before
    public void before() throws Exception {
        removeDeployments();
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.TRUE.toString());
    }

    private void removeDeployments() throws IOException {
        final File deployments = new File(SystemInstance.get().getBase().getDirectory("conf", false), "deployments.xml");
        if (deployments.exists()) {
            System.out.println(IO.slurp(deployments));
            Files.delete(deployments);
        }
    }

    @After
    public void after() throws Exception {
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.FALSE.toString());
        OpenEJB.destroy();
        removeDeployments();
    }

    private Deployer getDeployer() throws NamingException {
        return (Deployer) context.lookup("openejb/DeployerRemote");
    }

    @Test
    public void testGetUniqueFile() throws Exception {

        final String uniqueFile = getDeployer().getUniqueFile();
        Assert.assertTrue(new File(uniqueFile).exists());
    }

    @Test
    public void testGetDeployedApps() throws Exception {
        getAppInfos();
    }

    private Collection<AppInfo> getAppInfos() throws Exception {
        final Deployer deployer = getDeployer();
        Collection<AppInfo> deployedApps = deployer.getDeployedApps();

        if (null == deployedApps) {
            deployedApps = new ArrayList<>();
        }

        if (deployedApps.size() < 1) {
            getAppInfo();
            deployedApps.addAll(deployer.getDeployedApps());
        }

        Assert.assertTrue("Found no deployed apps", deployedApps.size() > 0);
        return deployedApps;
    }

    @Test
    public void testDeployWarSave() throws Exception {

        final Collection<AppInfo> deployedApps = getDeployer().getDeployedApps();
        Assert.assertTrue("Found more than one app", deployedApps.size() < 2);

        final File deployments = new File(SystemInstance.get().getBase().getDirectory("conf", false), "deployments.xml");
        Assert.assertFalse("Found existing: " + deployments.getAbsolutePath(), deployments.exists());

        getAppInfo();

        Assert.assertTrue("Failed to find: " + deployments.getAbsolutePath(), deployments.exists());
    }

    @Test
    public void removeDeploymentsLogic() throws Exception {
        final File dir1 = Files.mkdirs(new File("target/DeployerEjbTest/removeDeploymentsLogic/app1/"));

        final File file = SystemInstance.get().getBase().getFile(ADDITIONAL_DEPLOYMENTS, false);
        final Method save = DeployerEjb.class.getDeclaredMethod("saveDeployment", File.class, boolean.class);
        save.setAccessible(true);

        {
            final AdditionalDeployments deployments = new AdditionalDeployments();

            final Deployments d1 = new Deployments();
            d1.setDir(dir1.getCanonicalPath());
            deployments.getDeployments().add(d1);

            final Deployments d12 = new Deployments();
            d12.setDir(dir1.getCanonicalPath());
            deployments.getDeployments().add(d12);

            final Deployments d2 = new Deployments();
            d2.setFile(new File(File.listRoots()[0], "/foo/bar/app.war").getAbsolutePath());
            deployments.getDeployments().add(d2);

            try (final FileOutputStream fos = new FileOutputStream(file)) {
                JaxbOpenejb.marshal(AdditionalDeployments.class, deployments, fos);
            }
            assertDeployementsSize(file, 3);
        }
        {
            save.invoke(new DeployerEjb(), dir1, false);
            assertDeployementsSize(file, 2);
        }
        {
            save.invoke(new DeployerEjb(), new File(dir1.getParentFile(), dir1.getName() + ".war"), false);
            assertDeployementsSize(file, 1);
        }
        {
            save.invoke(new DeployerEjb(), new File(File.listRoots()[0], "/foo/bar/app.war"), false);
            assertDeployementsSize(file, 0);
        }
    }

    private void assertDeployementsSize(final File file, final int i) throws Exception {
        try (final FileInputStream fis = new FileInputStream(file)) {
            assertEquals(i, JaxbOpenejb.unmarshal(AdditionalDeployments.class, fis).getDeployments().size());
        }
    }

    private AppInfo getAppInfo() throws IOException, NamingException, OpenEJBException {
        return getAppInfo(null);
    }

    private AppInfo getAppInfo(final Properties p) throws IOException, NamingException, OpenEJBException {

        final Deployer deployer = getDeployer();

        final File war = warArchive.get();
        if (!war.exists()) {
            Assert.fail("War file does not exist: " + war.getAbsolutePath());
        }

        return (null != p ? deployer.deploy(war.getAbsolutePath(), p) : deployer.deploy(war.getAbsolutePath()));
    }

    @Test
    public void testDeployWarNoSave() throws Exception {
        final Collection<AppInfo> deployedApps = getDeployer().getDeployedApps();
        Assert.assertTrue("Found more than one app", deployedApps.size() < 2);

        final File deployments = new File(SystemInstance.get().getBase().getDirectory("conf", false), "deployments.xml");
        if (deployments.exists()) {
            Files.delete(deployments);
        }

        Assert.assertFalse("Found existing: " + deployments.getAbsolutePath(), deployments.exists());

        final Properties p = new Properties();
        p.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.FALSE.toString());
        getAppInfo(p);

        Assert.assertFalse("Found existing: " + deployments.getAbsolutePath(), deployments.exists());
    }

    @Test
    public void testDeployProperties() throws Exception {
        final Properties p = new Properties();
        final String path = warArchive.get().getAbsolutePath();

        p.setProperty(Deployer.FILENAME, path);
        p.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.FALSE.toString());

        final Deployer deployer = getDeployer();
        final AppInfo appInfo = deployer.deploy(p);
        Assert.assertTrue("Paths do not match: " + path + " - " + appInfo.path, path.equals(appInfo.path));
    }

    @Test
    public void testUndeploy() throws Exception {
        final AppInfo appInfo = getDeployedApp();

        Assert.assertNotNull("Failed to deploy app", appInfo);

        final Deployer deployer = getDeployer();
        deployer.undeploy(appInfo.path);

        final Collection<AppInfo> appInfos = getAppInfos();
        Assert.assertTrue("Failed to undeploy app", appInfos.size() < 2);
    }

    private AppInfo getDeployedApp() throws Exception {
        final Collection<AppInfo> appInfos = getAppInfos();

        AppInfo appInfo = null;
        final File file = warArchive.get();

        if (appInfos.size() < 2) {
            appInfo = getAppInfo();
        } else {

            final String name = file.getName().toLowerCase();

            for (final AppInfo info : appInfos) {
                if (name.contains(info.appId.toLowerCase())) {
                    appInfo = info;
                }
            }
        }
        return appInfo;
    }

    @Test
    public void testReload() throws Exception {

        final AppInfo appInfo = getDeployedApp();

        final Deployer deployer = getDeployer();
        deployer.reload(appInfo.path);

        final Collection<AppInfo> deployedApps = deployer.getDeployedApps();
        boolean found = false;
        for (final AppInfo app : deployedApps) {
            if (app.path.equals(appInfo.path)) {
                found = true;
            }
        }

        Assert.assertTrue("Failed to find app after redeploy", found);
    }

    public static class TestClass {
        public TestClass() {
        }
    }
}