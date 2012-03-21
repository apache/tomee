package jug.rest;

import jug.routing.DataSourceInitializer;
import jug.routing.PollingRouter;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.loader.IO;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class SubjectServiceTest {
    private static EJBContainer container;

    @Inject
    private DataSourceInitializer init;

    @Resource(name = "ClientRouter", type = PollingRouter.class)
    private PollingRouter router;

    @BeforeClass
    public static void start() {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(EJBContainer.APP_NAME, "polling/api");
        properties.setProperty(EJBContainer.PROVIDER, "openejb");
        container = EJBContainer.createEJBContainer(properties);
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
        init.init();
    }

    @AfterClass
    public static void stop() {
        container.close();
    }

    @Test
    public void createVote() throws IOException {
        final Response response = WebClient.create("http://localhost:4204/polling/")
                                    .path("api/subject/create")
                                    .accept("application/json")
                                    .query("name", "TOMEE_JUG_JSON")
                                    .post("was it cool?");
        final String output = IO.slurp((InputStream) response.getEntity());
        assertTrue("output doesn't contain TOMEE_JUG_JSON '" + output + "'", output.contains("TOMEE_JUG_JSON"));
    }
}
