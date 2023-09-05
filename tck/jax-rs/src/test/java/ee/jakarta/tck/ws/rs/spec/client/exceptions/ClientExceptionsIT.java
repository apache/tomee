/*
 * Copyright (c) 2022 Jeremias Weber. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.spec.client.exceptions;

import static ee.jakarta.tck.ws.rs.common.matchers.IsThrowingMatcher.isThrowing;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

/**
 * Compliance Tests for exceptions thrown by {@link Client}.
 *
 * @author Jeremias Weber
 * @since 4.0
 */
@Timeout(value = 1, unit = TimeUnit.HOURS)
@ExtendWith(ArquillianExtension.class)
public class ClientExceptionsIT {

    private static Client client;

    @ArquillianResource
    private URL baseUrl;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        // given
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs_client_exceptions.war");
        archive.addClasses(ClientExceptionsIT.class);
        return archive;
    }

    /**
     * Compliance Test for the spec requirement that {@link Client} MUST throw the
     * <strong>most specific</strong> subclass of {@link WebApplicationException} for the returned status code.
     */
    @Test
    public final void shouldThrowMostSpecificWebApplicationException() {
        // Verifies that of RedirectionException is thrown if the server returns a 3XX status code.
        assertThat(
                // when
                () -> client.target(uriForStatusCode(345, baseUrl)).request().get(String.class),
                // then
                isThrowing(RedirectionException.class)
        );

        // Verifies that of ClientErrorException is thrown if the server returns a 4XX status code.
        assertThat(
                // when
                () -> client.target(uriForStatusCode(456, baseUrl)).request().get(String.class),
                // then
                isThrowing(ClientErrorException.class)
        );

        // Verifies that of BadRequestException is thrown if the server returns the status code 400.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.BAD_REQUEST, baseUrl)).request().get(String.class),
                // then
                isThrowing(BadRequestException.class)
        );

        // Verifies that of NotAuthorizedException is thrown if the server returns the status code 401.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.UNAUTHORIZED, baseUrl)).request().get(String.class),
                // then
                isThrowing(NotAuthorizedException.class)
        );

        // Verifies that of ForbiddenException is thrown if the server returns the status code 403.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.FORBIDDEN, baseUrl)).request().get(String.class),
                // then
                isThrowing(ForbiddenException.class)
        );

        // Verifies that of NotFoundException is thrown if the server returns the status code 404.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.NOT_FOUND, baseUrl)).request().get(String.class),
                // then
                isThrowing(NotFoundException.class)
        );

        // Verifies that of NotAllowedException is thrown if the server returns the status code 405.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.METHOD_NOT_ALLOWED, baseUrl)).request().get(String.class),
                // then
                isThrowing(NotAllowedException.class)
        );

        // Verifies that of NotAcceptableException is thrown if the server returns the status code 406.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.NOT_ACCEPTABLE, baseUrl)).request().get(String.class),
                // then
                isThrowing(NotAcceptableException.class)
        );

        // Verifies that of NotSupportedException is thrown if the server returns the status code 415.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.UNSUPPORTED_MEDIA_TYPE, baseUrl)).request().get(String.class),
                // then
                isThrowing(NotSupportedException.class)
        );

        // Verifies that of ServerErrorException is thrown if the server returns a 5XX status code.
        assertThat(
                // when
                () -> client.target(uriForStatusCode(567, baseUrl)).request().get(String.class),
                // then
                isThrowing(ServerErrorException.class)
        );

        // Verifies that of InternalServerErrorException is thrown if the server returns the status code 500.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.INTERNAL_SERVER_ERROR, baseUrl)).request().get(String.class),
                // then
                isThrowing(InternalServerErrorException.class)
        );

        // Verifies that of ServiceUnavailableException is thrown if the server returns the status code 503.
        assertThat(
                // when
                () -> client.target(uriForStatus(Status.SERVICE_UNAVAILABLE, baseUrl)).request().get(String.class),
                // then
                isThrowing(ServiceUnavailableException.class)
        );
    }

    @BeforeAll
    static void createClient() {
        ClientExceptionsIT.client = ClientBuilder.newClient();
    }

    @AfterAll
    static void disposeClient() {
        ClientExceptionsIT.client.close();
    }

    @ApplicationPath("")
    public static class StatusApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(StatusResource.class);
        }

        @Path("status")
        public static class StatusResource {

            @GET
            @Path("{statusCode}")
            public Response respondWithStatusCode(@PathParam("statusCode") final int statusCode) {
                return Response.status(statusCode).build();
            }
        }
    }

    private static final URI uriForStatus(Status status, URL baseUrl) {
        return uriForStatusCode(status.getStatusCode(), baseUrl);
    }

    private static final URI uriForStatusCode(int statusCode, URL baseUrl) {
        try {
            return UriBuilder.fromUri(baseUrl.toURI())
                    .path("status/{statusCode}")
                    .build(statusCode);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
