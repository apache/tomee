package org.apache.tomee.microprofile.jwt.itest;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.junit.Test;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MissingRequiredClaimsTest {

    @Test
    public void testMissingSub() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(MissingRequiredClaimsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded()))
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
//                .update()
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
//                "  \"sub\":\"Jane Awesome\"," +
                "  \"exp\":2552047942" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }

        assertPresent(output, "rejected due to invalid claims");
        assertPresent(output, "No Subject (sub) claim is present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }
    @Test
    public void testMissingExpiration() throws Exception {
        final Tokens tokens = Tokens.rsa(2048, 256);
        final File appJar = Archive.archive()
                .add(MissingRequiredClaimsTest.class)
                .add(ColorService.class)
                .add(Api.class)
                .add("META-INF/microprofile-config.properties", "#\n" +
                        "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded()))
                .asJar();

        final ArrayList<String> output = new ArrayList<>();
        final TomEE tomee = TomEE.microprofile()
                .add("webapps/test/WEB-INF/beans.xml", "")
                .add("webapps/test/WEB-INF/lib/app.jar", appJar)
                .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
//                .update()
                .build();

        final WebClient webClient = createWebClient(tomee.toURI().resolve("/test").toURL());

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        {// invalid token
            final String token = tokens.asToken(claims);
            final Response response = webClient.reset()
                    .path("/movies")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }

        assertPresent(output, "rejected due to invalid claims");
        assertPresent(output, "No Expiration Time (exp) claim present.");
        assertNotPresent(output, "\tat org."); // no stack traces
    }

    public void assertPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(actual.isPresent());
    }
    public void assertNotPresent(final ArrayList<String> output, final String s) {
        final Optional<String> actual = output.stream()
                .filter(line -> line.contains(s))
                .findFirst();

        assertTrue(!actual.isPresent());
    }

    private static WebClient createWebClient(final URL base) {
        return WebClient.create(base.toExternalForm(), singletonList(new JohnzonProvider<>()),
                singletonList(new LoggingFeature()), null);
    }

    @ApplicationPath("/api")
    @LoginConfig(authMethod = "MP-JWT")
    public class Api extends Application {
    }

    @Path("/movies")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestScoped
    public static class ColorService {

        @GET
        @RolesAllowed({"manager", "user"})
        public String getAllMovies() {
            return "Green";
        }
    }

}