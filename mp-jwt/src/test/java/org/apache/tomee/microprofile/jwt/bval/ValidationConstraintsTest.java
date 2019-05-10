package org.apache.tomee.microprofile.jwt.bval;

import org.apache.tomee.microprofile.jwt.JsonWebTokenValidator;
import org.apache.tomee.microprofile.jwt.Tokens;
import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Shapes;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ValidationConstraintsTest {

    @Test
    public void testValidate() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Colors.class);

        final Method red = Colors.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        final Set<ConstraintViolation<Object>> violations = constraints.validate(red, jwt);

        assertEquals(1, violations.size());
        final ConstraintViolation<Object> next = violations.iterator().next();
        assertEquals("The 'iss' claim must be 'http://foo.bar.com'", next.getMessage());
    }

    @Test
    public void testAudienceIsJoe() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Shapes.class);

        final Method method = Shapes.class.getMethod("square");
        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"jim\"]," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        final Set<ConstraintViolation<Object>> violations = constraints.validate(method, jwt);

        assertEquals(1, violations.size());
        final ConstraintViolation<Object> next = violations.iterator().next();
        assertEquals("The 'aud' claim must contain 'joe'", next.getMessage());
    }


}