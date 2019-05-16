package org.apache.tomee.microprofile.jwt.bval;

import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Colors$$JwtConstraints;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationGeneratorTest {

    @Test
    public void testGetConstrainedMethods() throws Exception {

        final Set<Method> methods = ValidationGenerator.getConstrainedMethods(Colors.class);
        final Map<String, Method> map = methods.stream().collect(Collectors.toMap(Method::getName, method -> method));

        Assert.assertTrue(map.containsKey("red"));
        Assert.assertTrue(map.containsKey("blue"));
        Assert.assertFalse(map.containsKey("green"));
    }

    @Test
    public void testSimple() throws Exception {
        assertGeneration(Colors.class, Colors$$JwtConstraints.class);
    }

    private void assertGeneration(final Class<?> target, final Class<?> expectedClass) throws IOException, ProxyGenerationException {
        final String actual = Asmifier.asmify(ValidationGenerator.generateFor(target));
        final String expected = Asmifier.asmify(Asmifier.readClassFile(expectedClass));

        Assert.assertEquals(expected, actual);
    }

}