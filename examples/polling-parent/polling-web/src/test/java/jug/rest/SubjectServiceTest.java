package jug.rest;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.loader.IO;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class SubjectServiceTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(EJBContainer.APP_NAME, "polling/api");
        container = EJBContainer.createEJBContainer(properties);
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
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
        assertTrue(IO.slurp((InputStream) response.getEntity()).contains("TOMEE_JUG_JSON"));
    }
}
