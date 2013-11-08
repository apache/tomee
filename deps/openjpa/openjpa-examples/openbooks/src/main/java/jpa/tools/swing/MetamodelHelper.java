/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package jpa.tools.swing;

import java.awt.Color;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 * Static utility for analyzing persistent metadata model using JPA 2.0.
 *  
 * @author Pinaki Poddar
 *
 */
public class MetamodelHelper {
    public static final int ATTR_ID      = 0;
    public static final int ATTR_VERSION = 1;
    public static final int ATTR_BASIC   = 2;
    public static final int ATTR_SINGULAR_RELATION = 3;
    public static final int ATTR_PLURAL_RELATION   = 4;
    
    public static final Color MIDNIGHT_BLUE = new Color(25,25,112);
    public static final Color DARK_GREEN = new Color(0,100,0);
    public static final Color KHAKI = new Color(240, 230, 140);
    
    public static String getDisplayName(Type<?> type) {
        if (type instanceof EntityType) {
            return getDisplayName((EntityType<?>)type);
        }
        return getDisplayName(type.getJavaType());
    }
    
    /**
     * Gets the displayed name of a given entity type.
     * @param type
     * @return
     */
    public static String getDisplayName(EntityType<?> type) {
        return type.getName();
    }
    
    public static String getDisplayName(Class<?> cls) {
      String fullName = cls.getName();
      if (fullName.startsWith("java.") || fullName.startsWith("openbook.domain.")) {
          int i = fullName.lastIndexOf('.');
          return fullName.substring(i+1);
      }
      return fullName;
    }
    
    
    public static String getDisplayName(Attribute<?,?> attr) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getDisplayName(attr.getJavaType()));
        if (attr instanceof MapAttribute) {
            buffer.append("<").append(getDisplayName(((MapAttribute)attr).getKeyType())).append(",")
                  .append(getDisplayName(((MapAttribute)attr).getElementType())).append(">");
        } else if (attr instanceof PluralAttribute) {
            buffer.append("<").append(getDisplayName(((PluralAttribute)attr).getElementType())).append(">");
        } 
        buffer.append(" ").append(attr.getName());
        return buffer.toString();
    }
    
    
    public static <T> List<Attribute<? super T,?>> getAttributes(EntityType<T> type) {
        List<Attribute<? super T,?>> list = new ArrayList<Attribute<? super T,?>>(type.getAttributes());
        Collections.sort(list, new AttributeComparator());
        return list;
    }
    
    public static int getAttributeType(Attribute<?, ?> a) {
        if (a instanceof SingularAttribute) {
            SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>)a;
            if (sa.isId()) return ATTR_ID;
            if (sa.isVersion()) return ATTR_VERSION;
            if (sa.isAssociation()) return ATTR_SINGULAR_RELATION;
            if (sa.isCollection()) return ATTR_PLURAL_RELATION;
        } else {
            if (a.isAssociation()) return ATTR_SINGULAR_RELATION;
            if (a.isCollection()) return ATTR_PLURAL_RELATION;
        }
        return ATTR_BASIC;
        
    }
    
    public static <T> Set<SingularAttribute<? super T, ?>> getIdAttributes(EntityType<T> type) {
        Set<SingularAttribute<? super T,?>> attrs = type.getSingularAttributes();
        Set<SingularAttribute<? super T,?>> idAttrs = new HashSet<SingularAttribute<? super T,?>>();
        for (SingularAttribute<? super T,?> attr : attrs) {
            if (attr.isId()) {
                idAttrs.add(attr);
            }
        }
        return idAttrs;
    }
    
    /**
     * Finds the derived target of the given type, if any. Otherwise null.
     */
    public static <T> EntityType<?> derivedTarget(EntityType<T> type) {
        Set<SingularAttribute<? super T,?>> ids = getIdAttributes(type);
        for (SingularAttribute<? super T,?> id : ids) {
            EntityType<?> derived = getParentType(id);
            if (derived != null) {
                return derived;
            }
        }
        return null;
    }
    
    public static EntityType<?> getParentType(SingularAttribute<?,?> id) {
        if (id.getType() instanceof EntityType) {
            return (EntityType<?>)id.getType();
        }
        return null;
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
    
    public static Color getColor(Attribute<?,?> attr) {
        if (isId(attr))
            return Color.RED;
        if (isVersion(attr))
            return Color.DARK_GRAY;
        return getColor(attr.getPersistentAttributeType());
    }
    
    public static Color getColor(Attribute.PersistentAttributeType type) {
        switch (type) {
        case BASIC :             return Color.BLACK;
        case EMBEDDED:           return Color.GRAY;
        case ONE_TO_ONE:         return Color.BLUE;
        case MANY_TO_ONE:        return MIDNIGHT_BLUE;
        case ONE_TO_MANY:        return DARK_GREEN;
        case MANY_TO_MANY:       return Color.PINK;
        case ELEMENT_COLLECTION: return KHAKI;
        default:                 return Color.BLACK;
        }
    }
    
    public static Integer getAttributeTypeCode(Attribute<?,?> attr) {
        if (isId(attr))
            return 0;
        if (isVersion(attr))
            return 1;
        
      switch (attr.getPersistentAttributeType()) {
      case BASIC : 
      case EMBEDDED:
          return 2;
      case ONE_TO_ONE: 
      case MANY_TO_ONE:
          return 3;
      case ONE_TO_MANY:
      case MANY_TO_MANY:
      case ELEMENT_COLLECTION: return 4;
      default: return 5;
      }
    }
    private static Map<Attribute<?,?>, Method> members = new HashMap<Attribute<?,?>, Method>();
    private static Object[] EMPTY_ARGS = null;
    private static Class<?>[] EMPTY_CLASSES = null;
    /**
     * Gets the value of the given persistent attribute for the given instance.
     * @param attr
     * @param instance
     * @return
     */
    public static Object getValue(Attribute<?,?> attr, Object instance) {
        Member member = attr.getJavaMember();
        Method getter = null;
        if (member instanceof Method) {
            getter = (Method)member;
        } else if (members.containsKey(attr)) {
            getter = members.get(attr);
        } else {
            getter = getMethod(attr.getDeclaringType().getJavaType(), attr.getName());
            members.put(attr, getter);
        }
        if (getter == null)
            return null;
        try {
            return getter.invoke(instance, EMPTY_ARGS);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Method getMethod(Class<?> type, String p) {
        try {
            String getter = "get" + Character.toUpperCase(p.charAt(0))+p.substring(1);
            return type.getMethod(getter, EMPTY_CLASSES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        
    }

    
    /**
     * Compares EntityType by their dependency of derived targets.
     * 
     * @author Pinaki Poddar
     *
     */
    public static class EntityComparator implements Comparator<EntityType<?>> {
        @Override
        public int compare(EntityType<?> o1, EntityType<?> o2) {
            if (derivedTarget(o1) == o2)
               return 1;
            if (derivedTarget(o2) == o1)
                return -1;
            return o1.getName().compareTo(o2.getName());
        }
        
    }

    
    /**
     * Compares attribute by their qualification.
     * Identity 
     * Version
     * Basic
     * Singular association
     * Plural association
     *  
     * @author Pinaki Poddar
     *
     */
    public static class AttributeComparator implements Comparator<Attribute<?,?>> {
        @Override
        public int compare(Attribute<?, ?> a1, Attribute<?, ?> a2) {
            Integer t1 = getAttributeTypeCode(a1);
            Integer t2 = getAttributeTypeCode(a2);
            if (t1.equals(t2)) {
                return a1.getName().compareTo(a2.getName());
            } else {
                return t1.compareTo(t2);
            }
        }
    }
}
