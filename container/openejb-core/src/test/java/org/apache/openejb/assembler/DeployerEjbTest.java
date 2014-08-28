package org.apache.openejb.assembler;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.FileUtils;
import org.apache.openejb.loader.Files;
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

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

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
        final File deployments = new File(SystemInstance.get().getBase().getDirectory("conf", false), "deployments.xml");
        if (deployments.exists()) {
            Files.delete(deployments);
        }
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.TRUE.toString());
    }

    @After
    public void after() throws Exception {
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.FALSE.toString());
        OpenEJB.destroy();
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
            deployedApps = new ArrayList<AppInfo>();
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