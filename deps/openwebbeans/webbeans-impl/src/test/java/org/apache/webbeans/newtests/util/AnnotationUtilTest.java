/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.util;

import org.apache.webbeans.config.DefaultAnnotation;
import org.apache.webbeans.util.AnnotationUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Tests for AnnotationUtil.
 */
public class AnnotationUtilTest
{
    @Test
    public void test_isQualifierEqual_DefaultAnnotation_emptyQualifier()
    {
        Annotation q1 = DefaultAnnotation.of(EmptyQualifier.class);
        Annotation q2 = DefaultAnnotation.of(EmptyQualifier.class);

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_DefaultAnnotation_AnnotationLiteral_emptyQualifier()
    {
        Annotation q1 = DefaultAnnotation.of(EmptyQualifier.class);
        EmptyQualifier q2 = new EmptyQualifierAnnotationLiteral();

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_DefaultAnnotation_nonEmptyQualifier()
    {
        Annotation q1 = DefaultAnnotation.of(TestQualifier.class);
        Annotation q2 = DefaultAnnotation.of(TestQualifier.class);

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_DefaultAnnotation_AnnotationLiteral_nonEmptyQualifier()
    {
        Annotation q1 = DefaultAnnotation.of(TestQualifier.class);
        TestQualifier q2 = new TestQualifierAnnotationLiteral();

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_nonEmptyQualifier()
    {
        TestQualifier q1 = new TestQualifierAnnotationLiteral();
        TestQualifier q2 = new TestQualifierAnnotationLiteral();

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_Different_String()
    {
        TestQualifier q1 = new TestQualifierAnnotationLiteral();
        TestQualifierAnnotationLiteral q2 = new TestQualifierAnnotationLiteral();
    
        q2.setValue("different value");

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_Different_int()
    {
        TestQualifier q1 = new TestQualifierAnnotationLiteral();
        TestQualifierAnnotationLiteral q2 = new TestQualifierAnnotationLiteral();

        q2.setNumber(4711);

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_Different_array()
    {
        TestQualifier q1 = new TestQualifierAnnotationLiteral();
        TestQualifierAnnotationLiteral q2 = new TestQualifierAnnotationLiteral();

        q2.setFloatArray(new float[]{47F, 11F});

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_Different_Enum()
    {
        TestQualifier q1 = new TestQualifierAnnotationLiteral();
        TestQualifierAnnotationLiteral q2 = new TestQualifierAnnotationLiteral();

        q2.setEnumValue(RetentionPolicy.SOURCE);

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_Nonbinding_Different()
    {
        Annotation q1 = DefaultAnnotation.of(TestQualifierNonbinding.class);
        TestQualifierNonbinding q2 = new TestQualifierNonbindingAnnotationLiteral();

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }

    @Test
    public void test_isQualifierEqual_AnnotationLiteral_MultipleNonbinding_Different()
    {
        Annotation q1 = DefaultAnnotation.of(TestQualifierMultipleNonbinding.class);
        TestQualifierMultipleNonbindingAnnotationLiteral q2 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q2.setValue("my value");

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }
    
    @Test
    public void test_isQualifierEqual_AnnotationLiteral_MultipleNonbinding_Equals()
    {
        Annotation q1 = DefaultAnnotation.of(TestQualifierMultipleNonbinding.class);
        TestQualifierMultipleNonbindingAnnotationLiteral q2 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q2.setValue("default-value");

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }
    
    @Test
    public void test_isQualifierEqual_AnnotationLiteralMutliple_MultipleNonbinding_Equals()
    {
        TestQualifierMultipleNonbindingAnnotationLiteral q1 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q1.setValue("hello");
        
        TestQualifierMultipleNonbindingAnnotationLiteral q2 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q2.setValue("hello");

        Assert.assertTrue(AnnotationUtil.isQualifierEqual(q1, q2));
    }
    
    @Test
    public void test_isQualifierEqual_AnnotationLiteralMutliple_MultipleNonbinding_different()
    {
        TestQualifierMultipleNonbindingAnnotationLiteral q1 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q1.setValue("hello_different");
        
        TestQualifierMultipleNonbindingAnnotationLiteral q2 = new TestQualifierMultipleNonbindingAnnotationLiteral();
        q2.setValue("hello");

        Assert.assertFalse(AnnotationUtil.isQualifierEqual(q1, q2));
    }    
}

@Retention(RUNTIME)
@Qualifier
@interface EmptyQualifier
{

}

class EmptyQualifierAnnotationLiteral
        extends AnnotationLiteral<EmptyQualifier>
        implements EmptyQualifier
{
}

@Retention(RUNTIME)
@Qualifier
@interface TestQualifier
{

    String value() default "default-value";

    int number() default -1;

    float[] floatArray() default {1.0F, 1.2F};

    RetentionPolicy enumValue() default RetentionPolicy.RUNTIME;

}

class TestQualifierAnnotationLiteral
        extends AnnotationLiteral<TestQualifier>
        implements TestQualifier
{

    // default values
    private String value = "default-value";
    private int number = -1;
    private float[] floatArray = new float[]{1.0F, 1.2F};
    private RetentionPolicy enumValue = RetentionPolicy.RUNTIME;

    // annotation methods

    public String value()
    {
        return value;
    }

    public int number()
    {
        return number;
    }

    public float[] floatArray()
    {
        return floatArray;
    }

    public RetentionPolicy enumValue()
    {
        return enumValue;
    }

    // setter

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    public void setFloatArray(float[] floatArray)
    {
        this.floatArray = floatArray;
    }

    public void setEnumValue(RetentionPolicy enumValue)
    {
        this.enumValue = enumValue;
    }

}

@Retention(RUNTIME)
@Qualifier
@interface TestQualifierNonbinding
{

    String value() default "default-value";

    @MyCustomAnnotation // to show that there can be more than one annotation here
    @Nonbinding
    int number() default -1;

}

@Retention(RUNTIME)
@Qualifier
@interface TestQualifierMultipleNonbinding
{

    String value() default "default-value";

    @Nonbinding
    int number() default -1;
    
    @Nonbinding
    long card() default -1;    

}

@Retention(RUNTIME)
@interface MyCustomAnnotation
{
}

class TestQualifierMultipleNonbindingAnnotationLiteral
    extends AnnotationLiteral<TestQualifierMultipleNonbinding>
    implements TestQualifierMultipleNonbinding

{
    String value;
    
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }

    @Override
    public int number()
    {
        return 10;
    }

    @Override
    public long card()
    {
        return 20;
    }
    
}

class TestQualifierNonbindingAnnotationLiteral
        extends AnnotationLiteral<TestQualifierNonbinding>
        implements TestQualifierNonbinding
{

    public String value()
    {
        return "default-value";
    }

    public int number()
    {
        return 4711;
    }
}
