/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.tomee.microprofile.jwt.itest.bval;

import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.tomee.microprofile.jwt.itest.Output;
import org.apache.tomee.microprofile.jwt.itest.Tokens;
import org.apache.tomee.server.composer.Archive;
import org.apache.tomee.server.composer.TomEE;
import org.eclipse.microprofile.auth.LoginConfig;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Base64;
import java.util.Set;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ValidationConstraintsTest {

    @Test
    public void testMissingSub() throws Exception {
        final Scenario scenario = Scenario.setup();

        final String claims = "{" +
//                "  \"sub\":\"Jane Awesome\"," +
                "  \"exp\":2552047942" +
                "}";

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }

        scenario.output()
                .assertPresent("rejected due to invalid claims")
                .assertPresent("No Subject (sub) claim is present.")
                .assertNotPresent("\tat org."); // no stack traces
    }

    @Test
    public void testMissingExpiration() throws Exception {

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(401, response.getStatus());
        }

        scenario.output()
                .assertPresent("rejected due to invalid claims")
                .assertPresent("No Expiration Time (exp) claim present.")
                .assertNotPresent("\tat org."); // no stack traces
    }

    @Test
    public void valid() throws Exception {
        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"aud\":[\"bar\",\"user\"]," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(200, response.getStatus());
        }

        scenario.output()
                .assertNotPresent("rejected due to invalid claims") // no stack traces
                .assertNotPresent("\tat org."); // no stack traces
    }


    @Test
    public void invalidAudAndIss() throws Exception {
        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        scenario.output()
                .assertPresent("The 'aud' claim is required")
                .assertPresent("The 'aud' claim must contain 'bar'")
                .assertPresent("The 'iss' claim must be 'http://foo.bar.com'")
                .assertNotPresent("\tat org."); // no stack traces
    }

    @Test
    public void invalidIss() throws Exception {
        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"bar\",\"user\"]," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        scenario.output()
                .assertPresent("The 'iss' claim must be 'http://foo.bar.com'")
                .assertNotPresent("\tat org."); // no stack traces
    }

    @Test
    public void invalidAud() throws Exception {
        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"foo\",\"user\"]," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        scenario.output()
                .assertPresent("The 'aud' claim must contain 'bar'")
                .assertNotPresent("\tat org."); // no stack traces
    }

    @Test
    public void missingAud() throws Exception {

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";

        final Scenario scenario = Scenario.setup();

        {// invalid token
            final String token = scenario.tokens.asToken(claims);
            final Response response = scenario.webClient.reset()
                    .path("/colors/red")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .get();
            assertEquals(403, response.getStatus());
        }

        scenario.output()
                .assertPresent("The 'aud' claim is required")
                .assertPresent("The 'aud' claim must contain 'bar'")
                .assertNotPresent("\tat org."); // no stack traces
    }

    private static class Scenario {
        private final Tokens tokens;
        private final WebClient webClient;
        private final Output output;

        public Scenario(final Tokens tokens, final WebClient webClient, final Output output) {
            this.tokens = tokens;
            this.webClient = webClient;
            this.output = output;
        }

        public static Scenario setup() throws Exception {
            final Tokens tokens = Tokens.rsa(2048, 256);

            final Output output = new Output();
            final TomEE tomee = TomEE.microprofile()
                    .add("webapps/test/WEB-INF/beans.xml", "")
                    .add("webapps/test/WEB-INF/lib/app.jar", Archive.archive()
                            .add(ValidationConstraintsTest.class)
                            .add(ColorService.class)
                            .add(Color.class)
                            .add(RequireClaim.class)
                            .add(Audience.class)
                            .add(Issuer.class)
                            .add(False.class)
                            .add(RequireClaim.Constraint.class)
                            .add(Audience.Constraint.class)
                            .add(Issuer.Constraint.class)
                            .add(False.Constraint.class)
                            .add(Api.class)
                            .add("META-INF/microprofile-config.properties", "#\n" +
                                    "mp.jwt.verify.publickey=" + Base64.getEncoder().encodeToString(tokens.getPublicKey().getEncoded()))
                            .asJar())
                    .watch("org.apache.tomee.microprofile.jwt.", "\n", output::add)
                    //                .update()
                    .build();

            final WebClient webClient = WebClient.create(tomee.toURI().resolve("/test").toURL().toExternalForm(),
                    singletonList(new JohnzonProvider<>()),
                    singletonList(new LoggingFeature()), null);
            return new Scenario(tokens, webClient, output);
        }

        public Output output() {
            return output;
        }
    }


    @ApplicationPath("/api")
    @LoginConfig(authMethod = "MP-JWT")
    public class Api extends Application {
    }

    @Path("/colors")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RequestScoped
    public static class ColorService {

        @GET
        @Path("red")
        @Audience("bar")
        @Issuer("http://foo.bar.com")
        @False
        public Color red() {
            return new Color("red");
        }

        @GET
        @Path("green")
        public Color green() {
            return new Color("green");
        }

        /**
         * To ensure non-public methods do not cause errors
         */
        @GET
        @Path("blue")
        private Color blue() {
            return new Color("blue");
        }
    }

    public static class Color {
        private String color;

        public Color() {
        }

        public Color(final String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }

        public void setColor(final String color) {
            this.color = color;
        }
    }

    @Documented
    @jakarta.validation.Constraint(validatedBy = {RequireClaim.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface RequireClaim {

        String value();

        Class<?>[] groups() default {};

        String message() default "The '{value}' claim is required";

        Class<? extends Payload>[] payload() default {};

        class Constraint implements ConstraintValidator<RequireClaim, JsonWebToken> {

            private RequireClaim claim;

            @Override
            public void initialize(final RequireClaim claim) {
                this.claim = claim;
            }

            @Override
            public boolean isValid(final JsonWebToken jsonWebToken, final ConstraintValidatorContext context) {
                return jsonWebToken.claim(claim.value()).isPresent();
            }
        }
    }

    @RequireClaim("aud")
    @Documented
    @jakarta.validation.Constraint(validatedBy = {Audience.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface Audience {

        String value();

        Class<?>[] groups() default {};

        String message() default "The 'aud' claim must contain '{value}'";

        Class<? extends Payload>[] payload() default {};


        class Constraint implements ConstraintValidator<Audience, JsonWebToken> {
            private Audience audience;

            @Override
            public void initialize(final Audience constraint) {
                this.audience = constraint;
            }

            @Override
            public boolean isValid(final JsonWebToken value, final ConstraintValidatorContext context) {
                final Set<String> audience = value.getAudience();
                return audience != null && audience.contains(this.audience.value());
            }
        }
    }

    @Documented
    @jakarta.validation.Constraint(validatedBy = {Issuer.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface Issuer {

        String value();

        Class<?>[] groups() default {};

        String message() default "The 'iss' claim must be '{value}'";

        Class<? extends Payload>[] payload() default {};


        class Constraint implements ConstraintValidator<Issuer, JsonWebToken> {
            private Issuer issuer;

            @Override
            public void initialize(final Issuer constraint) {
                this.issuer = constraint;
            }

            @Override
            public boolean isValid(final JsonWebToken value, final ConstraintValidatorContext context) {
                final String issuer = value.getIssuer();
                return this.issuer.value().equals(issuer);
            }
        }
    }

    /**
     * Regular bean validation annotations should not affect the JWT Validation
     */
    @Documented
    @jakarta.validation.Constraint(validatedBy = {False.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface False {

        Class<?>[] groups() default {};

        String message() default "This will never pass";

        Class<? extends Payload>[] payload() default {};


        class Constraint implements ConstraintValidator<False, Color> {
            @Override
            public boolean isValid(final Color value, final ConstraintValidatorContext context) {
                return false;
            }
        }
    }

}