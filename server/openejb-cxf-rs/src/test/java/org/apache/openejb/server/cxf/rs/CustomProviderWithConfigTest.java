package org.apache.openejb.server.cxf.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;
import javax.ejb.Singleton;
import javax.ejb.embeddable.EJBContainer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CustomProviderWithConfigTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY, ConfigurableProvider.class.getName());
        properties.setProperty(CxfRsHttpListener.OPENEJB_CXF_JAXRS_PROVIDERS_KEY + ".str", "done!");
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void config() {
        final String response = WebClient.create("http://localhost:4204/openejb-cxf-rs").accept("openejb/conf")
                .path("/customized/").get(String.class);
        assertEquals("done!", response);
    }

    @Singleton
    @Path("/customized")
    public static class CustomizedService {
        @GET
        @Produces("openejb/conf")
        public String go() {
            return "";
        }
    }

    @Provider
    @Produces("openejb/conf")
    public static class ConfigurableProvider<T> implements MessageBodyWriter<T> {
        private String str;

        @Override
        public long getSize(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public boolean isWriteable(Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public void writeTo(T t, Class<?> rawType, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
            entityStream.write(str.getBytes());
        }
    }
}
