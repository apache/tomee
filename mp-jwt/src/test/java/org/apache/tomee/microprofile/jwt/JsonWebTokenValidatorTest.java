package org.apache.tomee.microprofile.jwt;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonWebTokenValidatorTest {

    @Test
    public void testValidate() throws Exception {

        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlivFI8qB4D0y2jy0CfEqFyy46R0o7S8TKpsx5xbHKoU1VWg6QkQm+ntyIv1p4kE1sPEQO73+HY8+Bzs75XwRTYL1BmR1w8J5hmjVWjc6R2BTBGAYRPFRhor3kpM6ni2SPmNNhurEAHw7TaqszP5eUF/F9+KEBWkwVta+PZ37bwqSE4sCb1soZFrVz/UT/LF4tYpuVYt3YbqToZ3pZOZ9AX2o1GCG3xwOjkc4x0W7ezbQZdC9iftPxVHR8irOijJRRjcPDtA6vPKpzLl6CyYnsIYPd99ltwxTHjr3npfv/3Lw50bAkbT4HeLFxTx4flEoZLKO/g0bAoV2uqBhkA9xnQIDAQAB")
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"https://server.example.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        assertEquals("Jane Awesome", jwt.getSubject());
        assertEquals("https://server.example.com", jwt.getIssuer());
        assertEquals(2552047942l, jwt.getExpirationTime());
    }
}