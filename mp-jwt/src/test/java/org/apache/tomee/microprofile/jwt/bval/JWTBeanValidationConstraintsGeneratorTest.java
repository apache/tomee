package org.apache.tomee.microprofile.jwt.bval;

import org.apache.tomee.microprofile.jwt.bval.ann.Issuer;
import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Colors$$JwtConstraints;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class JWTBeanValidationConstraintsGeneratorTest {

    @Test
    public void testGetConstrainedMethods() throws Exception {

        final List<Method> methods = JWTBeanValidationConstraintsGenerator.getConstrainedMethods(Colors.class);
        final Map<String, Method> map = methods.stream().collect(Collectors.toMap(Method::getName, method -> method));

        Assert.assertTrue(map.containsKey("red"));
        Assert.assertTrue(map.containsKey("blue"));
        Assert.assertFalse(map.containsKey("green"));
    }

    @Test
    public void test() throws Exception {
        final String actual = Asmifier.asmify(JWTBeanValidationConstraintsGenerator.generateFor(Colors.class));
        final String expected = Asmifier.asmify(Asmifier.readClassFile(Colors$$JwtConstraints.class));

        Assert.assertEquals(expected, actual);
    }

    @Documented
    @javax.validation.Constraint(validatedBy = {Issuer.Constraint.class})
    @Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
    @Retention(RUNTIME)
    public @interface Color {

    }

}