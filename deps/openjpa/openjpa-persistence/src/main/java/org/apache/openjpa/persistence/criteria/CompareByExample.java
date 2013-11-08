/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.persistence.criteria;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.openjpa.enhance.Reflection;

/**
 * An expression for query-by-example.
 * 
 * @author Pinaki Poddar
 * 
 */
class CompareByExample<T> extends PredicateImpl {

    CompareByExample(CriteriaBuilder builder, ManagedType<T> type, 
            Path<T> from, T instance, ComparisonStyle style,
            Attribute<?, ?>... excludes) {
        super(extractOperator(style));
        List<Attribute<?, ?>> excludeAttr = excludes == null 
            ? new ArrayList<Attribute<?,?>>() : Arrays.asList(excludes);
        
        Set<SingularAttribute<? super T, ?>> attrs = type.getSingularAttributes();
        for (SingularAttribute<? super T, ?> attr : attrs) {
            if (excludeAttr.contains(attr) 
            || (style.excludeIdentity() && attr.isId()) 
            || (style.excludeVersion() && attr.isVersion())) {
                continue;
            }

            Object value = extractValue(instance, attr);
            if ((style.excludeNull() && value == null)
             || (style.excludeDefault() && isDefaultValue(attr.getJavaType(), value)))
                continue;

            Predicate p = null;
            if (value == null) {
                p = from.get(attr).isNull();
                this.add(p);
                continue;
            }
            if (attr.isAssociation()) {
                p = new CompareByExample(builder, (ManagedType<?>)attr.getType(), 
                        from.get(attr), value, style, excludes);
            } else if (attr.getJavaType() == String.class) {
                Expression<String> s = from.get(attr).as(String.class);
                switch (style.getStringComparsionMode()) {
                    case EXACT : p = builder.equal(s, value);
                    break;
                    case CASE_INSENSITIVE : p = builder.equal(builder.upper(s), value.toString());
                    break;
                    case LIKE: p = builder.like(s, value.toString());
                    break;
                }
            } else {
                p = builder.equal(from.get(attr), value);
            }
            this.add(p);
        }
    }
    
    Object extractValue(T instance, SingularAttribute<? super T, ?> attr) {
        Class<?> cls = instance.getClass();
        Method getter = Reflection.findGetter(cls, attr.getName(), false);
        if (getter != null)
            return Reflection.get(instance, getter);
        Field field = Reflection.findField(cls, attr.getName(), false);
        if (field != null)
            return Reflection.get(instance, field);
        return null;
    }
    
    boolean isDefaultValue(Class<?> cls, Object val) {
        if (val == null) {
            return true;
        }
        if (cls == Boolean.class || cls == boolean.class) {
           return Boolean.FALSE.equals(val);
        } else if (cls == Character.class || cls == char.class) {
            return ((Character) val).charValue() == 0;
        } else if (cls == Byte.class || cls == byte.class
                || cls == Double.class || cls == double.class
                || cls == Float.class || cls == float.class
                || cls == Long.class || cls == long.class
                || cls == Integer.class || cls == int.class
                || cls == Short.class || cls == short.class) {
                   return ((Number) val).intValue() == 0;
        } else if (cls == String.class) {
            return "".equals(val);
        } else {
            return false;
        }
    }
    
    static <T> BooleanOperator extractOperator(ComparisonStyle style) {
        return style == null ? BooleanOperator.AND : style.isDisjunction() ? BooleanOperator.OR : BooleanOperator.AND;
    }
}
