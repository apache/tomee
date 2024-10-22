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
package org.apache.tomee.security.http.openid;

import org.apache.tomee.security.AbstractTomEESecurityTest;
import org.apache.tomee.security.TomEEELInvocationHandler;
import org.junit.Test;

import jakarta.el.ELProcessor;
import jakarta.enterprise.inject.Vetoed;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdProviderMetadata;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class OpenIdAuthenticationMechanismDefinitionDelegateTest extends AbstractTomEESecurityTest {
    @Test
    public void noProviderUri() {
        OpenIdAuthenticationMechanismDefinition annotation = getAnnotation(AnnotationHolderNoProviderUri.class,
                Collections.emptyMap());

        assertEquals("https://server.example.com", annotation.providerMetadata().issuer());
    }

    @Test
    public void withProviderUri() {
        OpenIdAuthenticationMechanismDefinition annotation = getAnnotation(AnnotationHolderProviderUri.class,
                Map.of("providerUri", getAppUrl() + "/openidtest1/providerinfo"));

        assertEquals("https://override.example.com", annotation.providerMetadata().issuer());
        assertEquals("https://server.example.com/authorize", annotation.providerMetadata().authorizationEndpoint());
    }

    @Test
    public void withProviderUriTrailingSlash() {
        OpenIdAuthenticationMechanismDefinition annotation = getAnnotation(AnnotationHolderProviderUri.class,
                Map.of("providerUri", getAppUrl() + "/openidtest1/providerinfo/"));

        assertEquals("https://override.example.com", annotation.providerMetadata().issuer());
        assertEquals("https://server.example.com/authorize", annotation.providerMetadata().authorizationEndpoint());
    }

    @Test
    public void withFullProviderUri() {
        OpenIdAuthenticationMechanismDefinition annotation = getAnnotation(AnnotationHolderProviderUri.class,
                Map.of("providerUri", getAppUrl() + "/openidtest1/providerinfo/.well-known/openid-configuration"));

        assertEquals("https://override.example.com", annotation.providerMetadata().issuer());
        assertEquals("https://server.example.com/authorize", annotation.providerMetadata().authorizationEndpoint());
    }

    private OpenIdAuthenticationMechanismDefinition getAnnotation(Class<?> annotationHolder, Map<String, Object> elProperties) {
        ELProcessor elProcessor = new ELProcessor();
        for (Map.Entry<String, Object> elProperty : elProperties.entrySet()) {
            elProcessor.setValue(elProperty.getKey(), elProperty.getValue());
        }

        return new OpenIdAuthenticationMechanismDefinitionDelegate.AutoResolvingProviderMetadata(
                TomEEELInvocationHandler.of(
                        OpenIdAuthenticationMechanismDefinition.class,
                        annotationHolder.getAnnotation(OpenIdAuthenticationMechanismDefinition.class),
                        elProcessor));
    }

    @Vetoed // to not break other tests
    @OpenIdAuthenticationMechanismDefinition(
            providerURI = "",
            providerMetadata = @OpenIdProviderMetadata(issuer = "https://server.example.com"))
    private static class AnnotationHolderNoProviderUri {}

    @Vetoed // to not break other tests
    @OpenIdAuthenticationMechanismDefinition(
            providerURI = "#{providerUri}",
            providerMetadata = @OpenIdProviderMetadata(issuer = "https://override.example.com"))
    private static class AnnotationHolderProviderUri {}

    @Path("openidtest1")
    public static class OpenidProvider {
        @GET @Path("providerinfo/.well-known/openid-configuration")
        public String providerInfo() {
            return """
                    {
                      "issuer": "https://server.exmaple.com",
                      "authorization_endpoint": "https://server.example.com/authorize"
                    }
                    """;
        }
    }
}