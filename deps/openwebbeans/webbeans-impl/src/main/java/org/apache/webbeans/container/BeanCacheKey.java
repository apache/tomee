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
package org.apache.webbeans.container;

import org.apache.webbeans.util.AnnotationUtil;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;

public final class BeanCacheKey
{
    private final Type type;
    private final String path;
    private final Annotation qualifier;
    private final Annotation qualifiers[];
    private final int hashCode;
    private static final Comparator<Annotation> ANNOTATION_COMPARATOR = new AnnotationComparator();

    public BeanCacheKey( Type type, String path, Annotation... qualifiers )
    {
        this.type = type;
        this.path = path;
        final int length = qualifiers != null ? qualifiers.length : 0;
        if (length == 0)
        {
            this.qualifier = null;
            this.qualifiers = null;
        }
        else if (length == 1)
        {
            this.qualifier = qualifiers[0];
            this.qualifiers = null;
        }
        else
        {
            this.qualifier = null;
            // to save array creations, we only create an array, if we have more than one annotation
            this.qualifiers = new Annotation[length];
            System.arraycopy(qualifiers, 0, this.qualifiers, 0, length);
            Arrays.sort(this.qualifiers, ANNOTATION_COMPARATOR);
        }

        // this class is directly used in ConcurrentHashMap.get() so simply init the hasCode here
        hashCode = computeHashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        BeanCacheKey cacheKey = (BeanCacheKey) o;

        if (!type.equals(cacheKey.type))
        {
            return false;
        }
        if (qualifier != null ? !qualifierEquals(qualifier, cacheKey.qualifier) : cacheKey.qualifier != null)
        {
            return false;
        }
        if (!qualifierArrayEquals(qualifiers, cacheKey.qualifiers))
        {
            return false;
        }
        if (path != null ? !path.equals(cacheKey.path) : cacheKey.path != null)
        {
            return false;
        }

        return true;
    }

    private boolean qualifierArrayEquals(Annotation[] qualifiers1, Annotation[] qualifiers2)
    {
        if (qualifiers1 == qualifiers2)
        {
            return true;
        }
        else if (qualifiers1 == null || qualifiers2 == null)
        {
            return false;
        }
        if (qualifiers1.length != qualifiers2.length)
        {
            return false;
        }
        for (int i = 0; i < qualifiers1.length; i++)
        {
            Annotation a1 = qualifiers1[i];
            Annotation a2 = qualifiers2[i];
            if (a1 == null ? a2 != null : !qualifierEquals(a1, a2))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * We need this method as some weird JVMs return 0 as hashCode for classes.
     * In that case we return the hashCode of the String.
     */
    private int getTypeHashCode(Type type)
    {
        int typeHash = type.hashCode();
        if (typeHash == 0 && type instanceof Class)
        {
            return ((Class)type).getName().hashCode();
            // the type.toString() is always the same: "java.lang.Class@<hexid>"
            // was: return type.toString().hashCode();
        }

        return typeHash;
    }

    /**
     * Compute the HashCode. This should be called only in the constructor.
     */
    private int computeHashCode()
    {
        int computedHashCode = 31 * getTypeHashCode(type) + (path != null ? path.hashCode() : 0);
        if (qualifier != null)
        {
            computedHashCode = 31 * computedHashCode + getQualifierHashCode(qualifier);
        }
        if (qualifiers != null)
        {
            for (int i = 0; i < qualifiers.length; i++)
            {
                computedHashCode = 31 * computedHashCode + getQualifierHashCode(qualifiers[i]);
            }
        }
        return computedHashCode;
    }

    /**
     * Calculate the hashCode() of a qualifier, which ignores {@link Nonbinding} members.
     */
    private int getQualifierHashCode(Annotation a)
    {
        Class annotationClass = getAnnotationClass(a.getClass());

        if (annotationClass == null)
        {
            return getTypeHashCode(a.getClass());
        }

        // the hashCode of an Annotation is calculated solely via the hashCodes
        // of it's members. If there are no members, it is 0.
        // thus we first need to get the annotation-class hashCode
        int hashCode = getTypeHashCode(annotationClass);

        // and now add the hashCode of all it's Nonbinding members
        // the following algorithm is defined by the Annotation class definition
        // see the JavaDoc for Annotation!
        // we only change it so far that we skip evaluating @Nonbinding members
        final Method[] members = annotationClass.getDeclaredMethods();

        for (Method member : members)
        {
            if (member.isAnnotationPresent(Nonbinding.class))
            {
                // ignore the non binding
                continue;
            }

            // Member value
            final Object object = callMethod(a, member);
            final int value;
            if(object.getClass().isArray())
            {
                Class<?> type = object.getClass().getComponentType();
                if(type.isPrimitive())
                {
                    if(Long.TYPE == type)
                    {
                        value = Arrays.hashCode((long[]) object);
                    }
                    else if(Integer.TYPE == type)
                    {
                        value = Arrays.hashCode((int[])object);
                    }
                    else if(Short.TYPE == type)
                    {
                        value = Arrays.hashCode((short[])object);
                    }
                    else if(Double.TYPE == type)
                    {
                        value = Arrays.hashCode((double[])object);
                    }
                    else if(Float.TYPE == type)
                    {
                        value = Arrays.hashCode((float[])object);
                    }
                    else if(Boolean.TYPE == type)
                    {
                        value = Arrays.hashCode((boolean[])object);
                    }
                    else if(Byte.TYPE == type)
                    {
                        value = Arrays.hashCode((byte[])object);
                    }
                    else if(Character.TYPE == type)
                    {
                        value = Arrays.hashCode((char[])object);
                    }
                    else
                    {
                        value = 0;
                    }
                }
                else
                {
                    value = Arrays.hashCode((Object[])object);
                }
            }
            else
            {
                value = object.hashCode();
            }

            hashCode = 29 * hashCode + value;
            hashCode = 29 * hashCode + member.getName().hashCode();
        }

        return hashCode;
    }

    /**
     * Implements the equals() method for qualifiers, which ignores {@link Nonbinding} members.
     */
    private boolean qualifierEquals(Annotation qualifier1, Annotation qualifier2)
    {
        return ANNOTATION_COMPARATOR.compare(qualifier1, qualifier2) == 0;
    }

    private static Class getAnnotationClass(Class a)
    {
        for (Class i : a.getInterfaces())
        {
            if (i.isAnnotation())
            {
                return i;
            }
        }
        return null;
    }

    /**
     * Helper method for calculating the hashCode of an annotation.
     */
    private static Object callMethod(Object instance, Method method)
    {
        try
        {
            if (!method.isAccessible())
            {
                method.setAccessible(true);
            }

            return method.invoke(instance, AnnotationUtil.EMPTY_OBJECT_ARRAY);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Exception in method call : " + method.getName(), e);
        }

    }

    /**
     * for debugging ...
     */
    @Override
    public String toString()
    {
        return "BeanCacheKey{" + "type=" + type + ", path='" + path + '\''
                + ", qualifiers="
                + (qualifiers == null ? qualifier : Arrays.asList(qualifiers)) + ", hashCode=" + hashCode + '}';
    }

    /**
     * to keep the annotations ordered.
     */
    private static class AnnotationComparator implements Comparator<Annotation>
    {

        // Notice: Sorting is a bit costly, but the use of this code is very rar.
        public int compare(Annotation annotation1, Annotation annotation2)
        {
            final Class<? extends Annotation> type1 = annotation1.annotationType();
            final Class<? extends Annotation> type2 = annotation2.annotationType();
            final int temp = type1.getName().compareTo(type2.getName());
            if (temp != 0)
            {
                return temp;
            }
            final Method[] member1 = type1.getDeclaredMethods();
            final Method[] member2 = type2.getDeclaredMethods();

            // TBD: the order of the list of members seems to be deterministic

            int i = 0;
            int j = 0;
            final int length1 = member1.length;
            final int length2 = member2.length;

            // find next nonbinding
            for (;; i++, j++)
            {
                while (i < length1 && member1[i].isAnnotationPresent(Nonbinding.class))
                {
                    i++;
                }
                while (j < length2 && member2[j].isAnnotationPresent(Nonbinding.class))
                {
                    j++;
                }
                if (i >= length1 && j >= length2)
                { // both ended
                    return 0;
                }
                else if (i >= length1)
                { // #1 ended
                    return 1;
                }
                else if (j >= length2)
                { // #2 ended
                    return -1;
                }
                else
                { // not ended
                    int c = member1[i].getName().compareTo(member2[j].getName());
                    if (c != 0)
                    {
                        return c;
                    }
                    final Object value1 = callMethod(annotation1, member1[i]);
                    final Object value2 = callMethod(annotation2, member2[j]);
                    assert value1.getClass().equals(value2.getClass());

                    if (value1 instanceof Comparable)
                    {
                        c = ((Comparable)value1).compareTo(value2);
                        if (c != 0)
                        {
                            return c;
                        }
                    }
                    else if (value1.getClass().isArray())
                    {
                        c = value1.getClass().getComponentType().getName()
                                .compareTo(value2.getClass().getComponentType().getName());
                        if (c != 0)
                        {
                            return c;
                        }

                        final int length = Array.getLength(value1);
                        c = length - Array.getLength(value2);
                        if (c != 0)
                        {
                            return c;
                        }
                        for (int k = 0; k < length; k++)
                        {
                            c = ((Comparable)Array.get(value1, k)).compareTo(Array.get(value2, k));
                            if (c != 0)
                            {
                                return c;
                            }
                        }

                    }
                    else if (value1 instanceof Class)
                    {

                        c = ((Class)value1).getName().compareTo(((Class) value2).getName());
                        if (c != 0)
                        {
                            return c;
                        }
                    }
                    else
                    {
                        // valid types for members are only Comparable, Arrays, or Class
                        assert false;
                    }
                }
            }
        }
    }
}
