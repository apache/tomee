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

package org.apache.webbeans.test.annotation.binding;

import org.apache.webbeans.container.BeanCacheKey;
import org.apache.webbeans.test.annotation.binding.AnnotationWithArrayOfBooleanMember;
import org.apache.webbeans.test.annotation.binding.AnnotationWithArrayOfIntMember;
import org.apache.webbeans.test.annotation.binding.AnnotationWithArrayOfStringMember;
import org.apache.webbeans.test.annotation.binding.AnnotationWithBindingMember;
import org.apache.webbeans.test.annotation.binding.AnnotationWithNonBindingMember;
import org.apache.webbeans.test.component.BindingComponent;
import org.apache.webbeans.test.component.NonBindingComponent;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;

public class BeanCacheKeyUnitTest
{

    @AnnotationWithBindingMember(value = "B", number = 3)
    public BindingComponent s1;
    public static Annotation[] a1;

    @AnnotationWithBindingMember(value = "B")
    public BindingComponent s2;
    public static Annotation[] a2;

    @AnnotationWithNonBindingMember(value = "B", arg1 = "1", arg2 = "2")
    public NonBindingComponent s3;
    public static Annotation[] a3;

    @AnnotationWithNonBindingMember(value = "B", arg1 = "11", arg2 = "21")
    public NonBindingComponent s4;
    public static Annotation[] a4;

    @AnnotationWithNonBindingMember(value = "C", arg1 = "11", arg2 = "21")
    public NonBindingComponent s5;
    public static Annotation[] a5;

    @AnnotationWithNonBindingMember(arg2 = "any", arg1 = "any", value = "C")
    public NonBindingComponent s6;
    public static Annotation[] a6;

    @AnnotationWithArrayOfIntMember({1,2,3})
    public NonBindingComponent s7;
    public static Annotation[] a7;

    @AnnotationWithArrayOfIntMember({1,2,4})
    public NonBindingComponent s8;
    public static Annotation[] a8;

    @AnnotationWithArrayOfStringMember({"1","2","3"})
    public NonBindingComponent s9;
    public static Annotation[] a9;

    @AnnotationWithArrayOfStringMember({"1","2","4"})
    public NonBindingComponent sa;
    public static Annotation[] aa;

    @AnnotationWithArrayOfBooleanMember({true, true})
    public NonBindingComponent sb;
    public static Annotation[] ab;

    @AnnotationWithArrayOfBooleanMember({true,false})
    public NonBindingComponent sc;
    public static Annotation[] ac;


    public static Annotation[] a12;
    public static Annotation[] a21;
    public static Annotation[] a13;
    public static Annotation[] a31;
    public static Annotation[] a56;
    public static Annotation[] a65;
    public static Annotation[] a78;
    public static Annotation[] a9a;
    public static Annotation[] abc;

    static {
        try {
            a1 = BeanCacheKeyUnitTest.class.getDeclaredField("s1").getAnnotations();
            a2 = BeanCacheKeyUnitTest.class.getDeclaredField("s2").getAnnotations();
            a3 = BeanCacheKeyUnitTest.class.getDeclaredField("s3").getAnnotations();
            a4 = BeanCacheKeyUnitTest.class.getDeclaredField("s4").getAnnotations();
            a5 = BeanCacheKeyUnitTest.class.getDeclaredField("s5").getAnnotations();
            a6 = BeanCacheKeyUnitTest.class.getDeclaredField("s6").getAnnotations();
            a7 = BeanCacheKeyUnitTest.class.getDeclaredField("s7").getAnnotations();
            a8 = BeanCacheKeyUnitTest.class.getDeclaredField("s8").getAnnotations();
            a9 = BeanCacheKeyUnitTest.class.getDeclaredField("s9").getAnnotations();
            aa = BeanCacheKeyUnitTest.class.getDeclaredField("sa").getAnnotations();
            ab = BeanCacheKeyUnitTest.class.getDeclaredField("sb").getAnnotations();
            ac = BeanCacheKeyUnitTest.class.getDeclaredField("sc").getAnnotations();

            a12 = new Annotation[]{a1[0], a2[0]};
            a21 = new Annotation[]{a2[0], a1[0]};
            a13 = new Annotation[]{a1[0], a3[0]};
            a31 = new Annotation[]{a3[0], a1[0]};
            a56 = new Annotation[]{a5[0], a6[0]};
            a65 = new Annotation[]{a6[0], a5[0]};
            a78 = new Annotation[]{a7[0], a8[0]};
            a9a = new Annotation[]{a9[0], aa[0]};
            abc = new Annotation[]{ab[0], ac[0]};

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEmptyNull()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null);
        BeanCacheKey b = new BeanCacheKey(String.class, null);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEmptyNullNull()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, null);
        BeanCacheKey b = new BeanCacheKey(String.class, null, null);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testTypeUnequal()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null);
        BeanCacheKey b = new BeanCacheKey(Integer.class, null);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testPath()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, "A");
        BeanCacheKey b = new BeanCacheKey(String.class, "A");
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testPathUnequal()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, "A");
        BeanCacheKey b = new BeanCacheKey(String.class, "B");
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testNonEqualsWithBindingMemberParameter()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a1);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a2);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testEqualsWithBindingMember()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a1);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a1);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }


    @Test
    public void testEqualsWithNonBindingMember()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a3);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a3);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals2Annotations()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a12);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a12);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals2AnnotationsUnorderedName()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a13);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a31);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals2AnnotationsUnorderedParam()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a12);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a21);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testDiffMembers()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a4);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a5);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testOnyDiffMembersInNonBinding()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a5);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a6);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testAnnotationOrdering()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a56);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a65);
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testMemberArraysInt()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a7);
        BeanCacheKey b = new BeanCacheKey(String.class, null, a8);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testMemberArraysString()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a9);
        BeanCacheKey b = new BeanCacheKey(String.class, null, aa);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testMemberArraysBoolean()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, ab);
        BeanCacheKey b = new BeanCacheKey(String.class, null, ac);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testDiffArrays()
    {
        BeanCacheKey a = new BeanCacheKey(String.class, null, a9a);
        BeanCacheKey b = new BeanCacheKey(String.class, null, abc);
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.hashCode() == b.hashCode());
    }


}
