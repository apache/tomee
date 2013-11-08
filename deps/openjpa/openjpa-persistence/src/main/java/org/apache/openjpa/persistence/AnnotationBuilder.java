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
package org.apache.openjpa.persistence;

import serp.util.Strings;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.EnumSet;
import java.lang.annotation.Annotation;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class to stringify annotation declarations.
 *
 * @author Gokhan Ergul
 * @since 1.0.0
 * @nojavadoc
 */
public class AnnotationBuilder {

    private Class<? extends Annotation> type;
    private List<AnnotationEntry> components =
        new ArrayList<AnnotationEntry>();

    protected AnnotationBuilder(Class<? extends Annotation> type) {
        this.type = type;
    }

    public Class<? extends Annotation> getType() {
        return this.type;
    }

    public AnnotationBuilder add(String key, String val) {
        return doAdd(key, val);
    }

    public AnnotationBuilder add(String key, boolean val) {
        return doAdd(key, val);
    }

    public AnnotationBuilder add(String key, int val) {
        return doAdd(key, val);
    }

    public AnnotationBuilder add(String key, Class val) {
        return doAdd(key, val);
    }

    public AnnotationBuilder add(String key, EnumSet val) {
        return doAdd(key, val);
    }

    public AnnotationBuilder add(String key, Enum val) {
        return doAdd(key, val);
    }

    @SuppressWarnings("unchecked")
    public AnnotationBuilder add(String key, AnnotationBuilder val) {
        if (null == val)
            return this;
        AnnotationEntry ae = find(key);
        if (null == ae) {
            doAdd(key, val);
        } else {
            List<AnnotationBuilder> list;
            if (ae.value instanceof List) {
                list = (List<AnnotationBuilder>) ae.value;
            } else if (ae.value instanceof AnnotationBuilder) {
                list = new ArrayList<AnnotationBuilder> ();
                list.add((AnnotationBuilder) ae.value);
                ae.value = list;
            } else {
                throw new IllegalArgumentException(
                    "Unexpected type: " + ae.value);
            }
            list.add(val);
        }
        return this;
    }

    public boolean hasComponents() {
        return components.size() > 0;
    }

    private AnnotationBuilder doAdd (String key, Object val) {
        if (null != val)
            components.add(new AnnotationEntry(key, val));
        return this;        
    }

    private AnnotationEntry find(String key) {
        for(AnnotationEntry ae: components) {
            // null key references considered equal
            if (StringUtils.equals(ae.key, key))
                return ae;
        }
        return null;
    }

    static String enumToString(Enum e) {
        StringBuilder sb = new StringBuilder();
        sb.append(Strings.getClassName(e.getClass())).
            append(".").append(e);
        return sb.toString();
    }

    static String enumSetToString(EnumSet set) {
        StringBuilder sb = new StringBuilder();
        for (Iterator i = set.iterator(); i.hasNext();) {
            Object e =  i.next();
            sb.append(Strings.getClassName(e.getClass())).
                append(".").append(e);
            if (i.hasNext())
                sb.append(", ");
        }
        return sb.toString();
    }

    protected void toString(StringBuilder sb) {
        sb.append("@").append(Strings.getClassName(type));
        if (components.size() == 0)
            return;
        sb.append("(");
        for (Iterator<AnnotationEntry> i = components.iterator(); i.hasNext();) 
        {
            AnnotationEntry e = i.next();
            e.toString(sb);
            if (i.hasNext())
                sb.append(", ");
        }
        sb.append(")");        
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    class AnnotationEntry {

        String key;
        Object value;

        AnnotationEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        void toString(StringBuilder sb) {
            if (null != key)
                sb.append(key).append("=");

            List.class.getTypeParameters();
            if (value instanceof List) {
                sb.append("{");
                List<AnnotationBuilder> l = (List<AnnotationBuilder>) value;
                for (Iterator<AnnotationBuilder> i = l.iterator(); i.hasNext();)
                {
                    AnnotationBuilder ab =  i.next();
                    sb.append(ab.toString());
                    if (i.hasNext())
                        sb.append(", ");
                }
                sb.append("}");
            } else if (value instanceof Class) {
                String cls = ((Class) value).getName().replace('$', '.');
                sb.append(cls).append(".class");
            } else if (value instanceof String) {
                sb.append('"').append(value).append('"');
            } else if (value instanceof Enum) {
                sb.append(AnnotationBuilder.enumToString((Enum) value));
            } else if (value instanceof EnumSet) {
                sb.append(AnnotationBuilder.enumSetToString((EnumSet) value));
            } else {
                sb.append(value);
            }
        }

    }
}
