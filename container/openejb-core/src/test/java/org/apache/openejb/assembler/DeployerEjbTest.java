package org.apache.openejb.assembler;

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
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.TRUE.toString());
    }

    @After
    public void after() throws Exception {
        System.setProperty(OPENEJB_DEPLOYER_SAVE_DEPLOYMENTS, Boolean.FALSE.toString());
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

    }

    @Test
    public void testDeploy() throws Exception {

        final File deployments = new File(SystemInstance.get().getBase().getDirectory("conf", false), "deployments.xml");
        Assert.assertFalse("Found existing: " + deployments.getAbsolutePath(), deployments.exists());

        final Deployer deployer = getDeployer();

        final File war = warArchive.get();
        if (!war.exists()) {
            Assert.fail("War file does not exist: " + war.getAbsolutePath());
        }

        deployer.deploy(war.getAbsolutePath());
        Assert.assertTrue("Failed to find: " + deployments.getAbsolutePath(), deployments.exists());
    }

    @Test
    public void testDeploy1() throws Exception {

    }

    @Test
    public void testDeploy2() throws Exception {

    }

    @Test
    public void testUndeploy() throws Exception {

    }

    @Test
    public void testReload() throws Exception {

    }

    public static class TestClass {
        public TestClass() {
        }
    }
}