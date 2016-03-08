package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Classes(innerClassesAsBean = true)
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class JSonStreamingOutputTest {
    @RandomPort("http")
    private URL root;

    @Test
    public void run() {
        assertEquals("[{\"id\":1}]", ClientBuilder.newClient().target(root.toExternalForm()).path("/openejb/streamTest").request().get(String.class));
    }

    @Path("streamTest")
    public static class En {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response touch() {
            return Response.ok().entity(new StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException, WebApplicationException {
                    try (final JsonGenerator jg = Json.createGenerator(os)) { // in real life use the factory
                        jg.writeStartArray();
                        jg.writeStartObject().write("id", 1).writeEnd(); // simple for the assert
                        jg.writeEnd().close();
                    }
                }
            }).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
