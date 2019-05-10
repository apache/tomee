package org.apache.tomee.microprofile.jwt.bval;

import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Colors$$JwtConstraints;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValidationGeneratorTest {

    @Test
    public void testGetConstrainedMethods() throws Exception {

        final List<Method> methods = ValidationGenerator.getConstrainedMethods(Colors.class);
        final Map<String, Method> map = methods.stream().collect(Collectors.toMap(Method::getName, method -> method));

        Assert.assertTrue(map.containsKey("red"));
        Assert.assertTrue(map.containsKey("blue"));
        Assert.assertFalse(map.containsKey("green"));
    }

    @Test
    public void test() throws Exception {
        final String actual = Asmifier.asmify(ValidationGenerator.generateFor(Colors.class));
        final String expected = Asmifier.asmify(Asmifier.readClassFile(Colors$$JwtConstraints.class));

        Assert.assertEquals(expected, actual);
    }

}