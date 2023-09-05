/*
 * Copyright (c) 2020 Markus Karg. All rights reserved.
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

package ee.jakarta.tck.ws.rs.uribuilder;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;

/**
 * Compliance Test for URI Builder API of Jakarta REST API
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.1
 */
@Timeout(value = 1, unit = HOURS)
public final class UriBuilderIT {

    /**
     * Verifies that a valid instance can be created from scratch.
     * 
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldBuildValidInstanceFromScratch()
            throws InterruptedException, ExecutionException {
        // given
        final UriBuilder uriBuilder = UriBuilder.newInstance();

        // when
        final URI uri = uriBuilder.scheme("scheme").host("host").port(1).build();

        // then
        assertThat(uri.toString(), is("scheme://host:1"));
    }

    /**
     * Verifies that {@code UriBuilder#build()} creates an empty {@code URI}
     * no other methods are called on it. The created {@code URI} should be
     * equivalent to {@code URI.create("")}.
     * 
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void emptyUriBuilderBuildsEmptyUri() throws InterruptedException, ExecutionException {
        // given
        final URI uri = UriBuilder.newInstance().build();
        // then
        assertEquals(URI.create(""), /* when */ uri);
    }

    /**
     * Verifies that {@code UriBuilder#build()} throws a {@link UriBuilderException}
     * when it would be asked to create an invalid URI.
     * 
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldThrowUriBuilderExceptionOnSchemeOnlyUri() throws InterruptedException, ExecutionException {
        // given
        final UriBuilder uriBuilder = UriBuilder.newInstance().scheme("http");
        // then
        assertThrows(UriBuilderException.class, /* when */ uriBuilder::build);
    }

    /**
     * Verifies that {@code UriBuilder#build()} throws a {@code IllegalArgumentException}
     * when it would be asked to create a URI with unresolved template variables.
     * 
     * @throws ExecutionException   if the instance didn't boot correctly
     * @throws InterruptedException if the test took much longer than usually
     *                              expected
     */
    @Test
    public final void shouldThrowIllegalArgumentExceptionForUnresolvedTemplates() throws InterruptedException, 
                                                                                         ExecutionException {
        // given
        final UriBuilder uriBuilder = UriBuilder.newInstance().scheme("http")
                                                              .host("localhost")
                                                              .path("contextroot")
                                                              .path("{var}");
        // then
        assertThrows(IllegalArgumentException.class, /* when */ uriBuilder::build);
    }
}
