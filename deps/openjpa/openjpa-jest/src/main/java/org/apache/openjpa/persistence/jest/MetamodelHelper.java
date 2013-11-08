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

package org.apache.openjpa.persistence.jest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.meta.Members;

/**
 * @author Pinaki Poddar
 *
 */
public class MetamodelHelper {
    public static final char DASH = '-';
    public static final char UNDERSCORE = '_';
    
    /**
     * Attribute Category makes a finer distinction over PersistentAttributeType declared in
     * {@link Attribute.PersistentAttributeType} such as id, version, lob or enum.
     * <br>
     * <b>Important</b>: The name of the enumerated elements is important because 
     * a) some of these names are same as in Attribute.PersistentAttributeType enumeration 
     * b) names are used by XML serialization with underscores replaced by dash and decapitalized
     *
     */
    public static enum AttributeCategory {
        ID, VERSION, BASIC, ENUM, EMBEDDED, LOB, 
        ONE_TO_ONE, MANY_TO_ONE, ONE_TO_MANY, ELEMENT_COLLECTION, MANY_TO_MANY
    }
    
    public static List<Attribute<?,?>> getAttributesInOrder(Class<?> cls, Metamodel model) {
        return getAttributesInOrder(model.managedType(cls));
    }
    
    public static List<Attribute<?,?>> getAttributesInOrder(ClassMetaData meta, Metamodel model) {
        return getAttributesInOrder(meta.getDescribedType(), model);
    }
    
    /**
     * Gets the attributes of the given type in defined order.
     * @param type
     * @return
     */
    public static List<Attribute<?,?>> getAttributesInOrder(ManagedType<?> type) {
        List<Attribute<?,?>> list = new ArrayList<Attribute<?,?>>(type.getAttributes());
        Collections.sort(list, new AttributeComparator());
        return list;
    }

    public static boolean isId(Attribute<?,?> a) {
        if (a instanceof SingularAttribute)
            return ((SingularAttribute<?,?>)a).isId();
        return false;
    }
    
    public static boolean isVersion(Attribute<?,?> a) {
        if (a instanceof SingularAttribute)
            return ((SingularAttribute<?,?>)a).isVersion();
        return false;
    }
    
    public static boolean isEnum(Attribute<?,?> a) {
        if (a instanceof Members.Member) {
            int type = ((Members.Member<?,?>)a).fmd.getDeclaredTypeCode();
            return type == JavaTypes.ENUM;
        }
        return false;
    }
    
    public static boolean isLob(Attribute<?,?> a) {
        if (a instanceof Members.Member) {
            int type = ((Members.Member<?,?>)a).fmd.getDeclaredTypeCode();
            return type == JavaTypes.INPUT_READER || type == JavaTypes.INPUT_STREAM;
        }
        return false;
    }

    /**
     * Gets a ordinal value of enumerated persistent attribute category.
     *  
     * @param attr
     * @return
     */
    public static AttributeCategory getAttributeCategory(Attribute<?,?> attr) {
        if (isId(attr))
            return AttributeCategory.ID;
        if (isVersion(attr))
            return AttributeCategory.VERSION;
        if (isLob(attr))
            return AttributeCategory.LOB;
        if (isEnum(attr))
            return AttributeCategory.ENUM;
       switch (attr.getPersistentAttributeType()) {
           case BASIC : 
               return AttributeCategory.BASIC;
           case EMBEDDED:
               return AttributeCategory.EMBEDDED;
           case ONE_TO_ONE: 
               return AttributeCategory.ONE_TO_ONE;
           case MANY_TO_ONE:
               return AttributeCategory.MANY_TO_ONE;
           case ONE_TO_MANY:
           case ELEMENT_COLLECTION:
               return AttributeCategory.ONE_TO_MANY;
           case MANY_TO_MANY:
               return AttributeCategory.MANY_TO_MANY;
      }
       throw new RuntimeException(attr.toString());
    }
    
    public static String getTagByAttributeType(Attribute<?, ?> attr) {
        return getAttributeCategory(attr).name().replace(UNDERSCORE, DASH).toLowerCase();
    }
    
    /**
     * Gets name of the attribute type. For collection and map type attribute, the name is
     * appended with generic type argument names.
     * @param attr
     * @return
     */
    public static String getAttributeTypeName(Attribute<?, ?> attr) {
        StringBuilder name = new StringBuilder(attr.getJavaType().getSimpleName());
        switch (attr.getPersistentAttributeType()) {
            case ONE_TO_MANY:
            case ELEMENT_COLLECTION:
                name.append("&lt;")
                    .append(((PluralAttribute<?,?,?>)attr).getBindableJavaType().getSimpleName())
                    .append("&gt;");
                break;
            case MANY_TO_MANY:
                name.append("&lt;")
                .append(((MapAttribute<?,?,?>)attr).getKeyJavaType().getSimpleName())
                .append(',')
                .append(((MapAttribute<?,?,?>)attr).getBindableJavaType().getSimpleName())
                .append("&gt;");
            break;
            default:
        }
        return name.toString();
    }
    
    /**
     * Compares attribute by their category and within the same category by name.
     *
     */
    public static class AttributeComparator implements Comparator<Attribute<?,?>> {
        public int compare(Attribute<?, ?> a1, Attribute<?, ?> a2) {
            AttributeCategory t1 = getAttributeCategory(a1);
            AttributeCategory t2 = getAttributeCategory(a2);
            if (t1.equals(t2)) {
                return a1.getName().compareTo(a2.getName());
            } else {
                return t1.compareTo(t2);
            }
        }
    }
}
