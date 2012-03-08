package jug.rest.arquillian;

import jug.dao.SubjectDao;
import jug.domain.Subject;
import jug.monitoring.VoteCounter;
import jug.rest.SubjectService;
import org.apache.commons.io.IOUtils;
import org.apache.ziplock.JarLocation;
import org.apache.ziplock.WebModule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedInputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;

@RunAsClient
@RunWith(Arquillian.class)
public class SubjectServiceTomEETest {
    @Deployment
    public static WebArchive archive() {
        return new WebModule(SubjectServiceTomEETest.class).getArchive()
                .addClass(VoteCounter.class)
                .addPackage(Subject.class.getPackage()) // domain
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml"), "persistence.xml")
                .addAsWebInfResource(new ClassLoaderAsset("META-INF/env-entries.properties"), "env-entries.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackage(SubjectDao.class.getPackage()) // core
                .addPackage(SubjectService.class.getPackage()) // front
                .addAsLibrary(JarLocation.jarLocation(IOUtils.class)) // helper for client test
                .addAsLibrary(JarLocation.jarLocation(Test.class)); // junit
    }

    @Test
    public void checkThereIsSomeOutput() throws Exception {
        final String base = "http://localhost:" + System.getProperty("tomee.http.port");
        final URL url = new URL(base + "/SubjectServiceTomEETest/api/subject/list");
        final String output = IOUtils.toString(new BufferedInputStream(url.openStream()));
        assertTrue(output.contains("subject"));
    }
}
