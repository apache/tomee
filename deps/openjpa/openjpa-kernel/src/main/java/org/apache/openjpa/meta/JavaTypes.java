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
package org.apache.openjpa.meta;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.lib.meta.CFMetaDataParser;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;
import serp.util.Strings;

/**
 * Type constants for managed fields.
 *
 * @author Abe White
 */
public class JavaTypes {

    public static final int BOOLEAN = 0;
    public static final int BYTE = 1;
    public static final int CHAR = 2;
    public static final int DOUBLE = 3;
    public static final int FLOAT = 4;
    public static final int INT = 5;
    public static final int LONG = 6;
    public static final int SHORT = 7;
    // keep OBJECT as first non-primitive type code; other code relies on it
    public static final int OBJECT = 8;
    public static final int STRING = 9;
    public static final int NUMBER = 10;
    public static final int ARRAY = 11;
    public static final int COLLECTION = 12;
    public static final int MAP = 13;
    public static final int DATE = 14;
    public static final int PC = 15;
    public static final int BOOLEAN_OBJ = 16;
    public static final int BYTE_OBJ = 17;
    public static final int CHAR_OBJ = 18;
    public static final int DOUBLE_OBJ = 19;
    public static final int FLOAT_OBJ = 20;
    public static final int INT_OBJ = 21;
    public static final int LONG_OBJ = 22;
    public static final int SHORT_OBJ = 23;
    public static final int BIGDECIMAL = 24;
    public static final int BIGINTEGER = 25;
    public static final int LOCALE = 26;
    public static final int PC_UNTYPED = 27;
    public static final int CALENDAR = 28;
    public static final int OID = 29;
    public static final int INPUT_STREAM = 30;
    public static final int INPUT_READER = 31;
    public static final int ENUM = 32;

    private static final Localizer _loc = Localizer.forPackage(JavaTypes.class);

    private static final Map<Class<?>, Integer> _typeCodes = new HashMap<Class<?>, Integer>();

    static {
        _typeCodes.put(String.class, STRING);
        _typeCodes.put(Boolean.class, BOOLEAN_OBJ);
        _typeCodes.put(Byte.class, BYTE_OBJ);
        _typeCodes.put(Character.class, CHAR_OBJ);
        _typeCodes.put(Double.class, DOUBLE_OBJ);
        _typeCodes.put(Float.class, FLOAT_OBJ);
        _typeCodes.put(Integer.class, INT_OBJ);
        _typeCodes.put(Long.class, LONG_OBJ);
        _typeCodes.put(Short.class, SHORT_OBJ);
        _typeCodes.put(Date.class, DATE);
        _typeCodes.put(java.sql.Date.class, DATE);
        _typeCodes.put(java.sql.Timestamp.class, DATE);
        _typeCodes.put(java.sql.Time.class, DATE);
        _typeCodes.put(BigInteger.class, BIGINTEGER);
        _typeCodes.put(BigDecimal.class, BIGDECIMAL);
        _typeCodes.put(Number.class, NUMBER);
        _typeCodes.put(Locale.class, LOCALE);
        _typeCodes.put(Object.class, OBJECT);
        _typeCodes.put(PersistenceCapable.class, PC_UNTYPED);
        _typeCodes.put(Properties.class, MAP);
        _typeCodes.put(Calendar.class, CALENDAR);
    }

    /**
     * Return the field metadata type code for the given class. First class
     * objects are not recognized in this method.
     */
    public static int getTypeCode(Class<?> type) {
        if (type == null)
            return OBJECT;

        if (type.isPrimitive()) {
            switch (type.getName().charAt(0)) {
                case 'b':
                    return (type == boolean.class) ? BOOLEAN : BYTE;
                case 'c':
                    return CHAR;
                case 'd':
                    return DOUBLE;
                case 'f':
                    return FLOAT;
                case 'i':
                    return INT;
                case 'l':
                    return LONG;
                case 's':
                    return SHORT;
            }
        }

        Integer code = (Integer) _typeCodes.get(type);
        if (code != null)
            return code.intValue();

        // have to do this first to catch custom collection and map types;
        // on resolve we figure out if these custom types are
        // persistence-capable
        if (Collection.class.isAssignableFrom(type))
            return COLLECTION;
        if (Map.class.isAssignableFrom(type))
            return MAP;
        if (type.isArray())
            return ARRAY;
        if (Calendar.class.isAssignableFrom(type))
            return CALENDAR;

        if (type.isInterface()) {
            if (Serializable.class.isAssignableFrom(type))
                return OBJECT;
            return PC_UNTYPED;
        }
        if (type.isAssignableFrom(Reader.class))
            return INPUT_READER;
        if (type.isAssignableFrom (InputStream.class))
            return INPUT_STREAM;
        if (Enum.class.isAssignableFrom(type))
            return ENUM;
            
        return OBJECT;
    }
 
    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     */
    public static Class<?> classForName(String name, ClassMetaData context) {
        return classForName(name, context, null);
    }

    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     */
    public static Class<?> classForName(String name, ClassMetaData context, ClassLoader loader) {
        return classForName(name, context, context.getDescribedType(), null,
            loader);
    }
    
    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     * 
     * @param mustExist Whether the supplied loader <b>must</b> be able to load the class. If true no attempt to use a
     * different classloader will be made. If false the ClassResolver from the configuration will be used. 
     */
    public static Class<?> classForName(String name, ClassMetaData context, ClassLoader loader, boolean mustExist) {
        return classForName(name, context, context.getDescribedType(), null, loader, mustExist);
    }

    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     */
    public static Class<?> classForName(String name, ValueMetaData context) {
        return classForName(name, context, null);
    }

    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     */
    public static Class<?> classForName(String name, ValueMetaData context, ClassLoader loader) {
        return classForName(name,
            context.getFieldMetaData().getDefiningMetaData(),
            context.getFieldMetaData().getDeclaringType(), context, loader);
    }

     /**
     * Try to load a class using the provided loader. Optionally tries the 
     * configuration's ClassResolver if the supplied loader cannot find the class.
     *
     * @param name Name of the class to load. 
     * @param context 
     * @param loader ClassLoader to use. If null, the configuration's ClassResolver will be used. 
     * @param mustExist Whether the supplied loader <b>must</b> be able to load the class. If true no attempt to use a 
     *        different classloader will be made. If false the ClassResolver from the configuration will be used. 
     */
    public static Class<?> classForName(String name, ValueMetaData context,
            ClassLoader loader, boolean mustExist) {    	
            return classForName(name,
                context.getFieldMetaData().getDefiningMetaData(),
                context.getFieldMetaData().getDeclaringType(), context, loader, mustExist);
        }

    /**
     * OJ-758: Delegates to the final classForName.  This is needed
     * to maintain the existing code path prior to OJ-758.
     */
    private static Class<?> classForName(String name, ClassMetaData meta,
            Class<?> dec, ValueMetaData vmd, ClassLoader loader) {
    	return classForName(name, meta, dec, vmd,  loader, true);
    }

    /**
     * Check the given name against the same set of standard packages used
     * when parsing metadata.
     */
    private static Class<?> classForName(String name, ClassMetaData meta, Class<?> dec, ValueMetaData vmd, 
        ClassLoader loader, boolean mustExist) {
        // special case for PersistenceCapable and Object
        if ("PersistenceCapable".equals(name)
            || "javax.jdo.PersistenceCapable".equals(name)) // backwards compatibility
            return PersistenceCapable.class;
        if ("Object".equals(name))
            return Object.class;

        MetaDataRepository rep = meta.getRepository();
        boolean runtime = (rep.getValidate() & MetaDataRepository.VALIDATE_RUNTIME) != 0;
        if (loader == null)
            loader = rep.getConfiguration().getClassResolverInstance().
                getClassLoader(dec, meta.getEnvClassLoader());

        // try the owner's package
        String pkg = Strings.getPackageName(dec);
        Class<?> cls = CFMetaDataParser.classForName(name, pkg, runtime, loader);
        if (cls == null && vmd != null) {
            // try against this value type's package too
            pkg = Strings.getPackageName(vmd.getDeclaredType());
            cls = CFMetaDataParser.classForName(name, pkg, runtime, loader);
        }

        //OJ-758 start: If the class is still null, as a last/final attempt to 
        //load the class, check with the ClassResolver to get a loader
        //and use it to attempt to load the class. 
        if (cls == null  && !mustExist){
            loader = rep.getConfiguration().getClassResolverInstance().
            getClassLoader(dec, meta.getEnvClassLoader());
            cls = CFMetaDataParser.classForName(name, pkg, runtime, loader);
        }         	
        //OJ-758 end  
    
        if (cls == null)
            throw new MetaDataException(_loc.get("bad-class", name,
                (vmd == null) ? (Object) meta : (Object) vmd));
        
        return cls;
    }

    /**
     * Convert the given object to the given type if possible. If the type is
     * a numeric primitive, this method only guarantees that the return value
     * is a {@link Number}. If no known conversion or the value is null,
     * returns the original value.
     */
    public static Object convert(Object val, int typeCode) {
        if (val == null)
            return null;

        switch (typeCode) {
            case BIGDECIMAL:
                if (val instanceof BigDecimal)
                    return val;
                if (val instanceof Number)
                    return new BigDecimal(((Number) val).doubleValue());
                if (val instanceof String)
                    return new BigDecimal(val.toString());
                return val;
            case BIGINTEGER:
                if (val instanceof BigInteger)
                    return val;
                if (val instanceof Number || val instanceof String)
                    return new BigInteger(val.toString());
                return val;
            case BOOLEAN:
            case BOOLEAN_OBJ:
                if (val instanceof String)
                    return Boolean.valueOf(val.toString());
                return val;
            case BYTE_OBJ:
                if (val instanceof Byte)
                    return val;
                if (val instanceof Number)
                    return Byte.valueOf(((Number) val).byteValue());
                // no break
            case BYTE:
                if (val instanceof String)
                    return Byte.valueOf((String)val);
                return val;
            case CHAR:
            case CHAR_OBJ:
                if (val instanceof Character)
                    return val;
                if (val instanceof String)
                    return Character.valueOf(val.toString().charAt(0));
                if (val instanceof Number)
                    return Character.valueOf((char) ((Number) val).intValue());
                return val;
            case DATE:
                if (val instanceof String)
                    return new Date(val.toString());
                return val;
            case DOUBLE_OBJ:
                if (val instanceof Double)
                    return val;
                if (val instanceof Number)
                    return Double.valueOf(((Number) val).doubleValue());
                // no break
            case DOUBLE:
                if (val instanceof String)
                    return Double.valueOf(val.toString());
                return val;
            case FLOAT_OBJ:
                if (val instanceof Float)
                    return val;
                if (val instanceof Number)
                    return Float.valueOf(((Number) val).floatValue());
                // no break
            case FLOAT:
                if (val instanceof String)
                    return Float.valueOf(val.toString());
                return val;
            case INT_OBJ:
                if (val instanceof Integer)
                    return val;
                if (val instanceof Number)
                    return ((Number) val).intValue();
                // no break
            case INT:
                if (val instanceof String)
                    return Integer.valueOf(val.toString());
                return val;
            case LONG_OBJ:
                if (val instanceof Long)
                    return val;
                if (val instanceof Number)
                    return ((Number) val).longValue();
                // no break
            case LONG:
                if (val instanceof String)
                    return Long.valueOf(val.toString());
                return val;
            case NUMBER:
                if (val instanceof Number)
                    return val;
                if (val instanceof String)
                    return new BigDecimal(val.toString());
                return val;
            case SHORT_OBJ:
                if (val instanceof Short)
                    return val;
                if (val instanceof Number)
                    return Short.valueOf(((Number) val).shortValue());
                // no break
            case SHORT:
                if (val instanceof String)
                    return Short.valueOf(val.toString());
                return val;
            case STRING:
                return val.toString();
            default:
                return val;
        }
    }

    /**
     * Return true if the (possibly unresolved) field or its elements might be
     * persistence capable objects.
     */
    public static boolean maybePC(FieldMetaData field) {
        switch (field.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
                return maybePC(field.getElement());
            case JavaTypes.MAP:
                return maybePC(field.getKey()) || maybePC(field.getElement());
            default:
                return maybePC((ValueMetaData) field);
        }
    }

    /**
     * Return true if the (possibly unresolved) value might be a first class
     * object.
     */
    public static boolean maybePC(ValueMetaData val) {
        return maybePC(val.getDeclaredTypeCode(), val.getDeclaredType());
    }

    /**
     * Return true if the given unresolved typecode/type pair may represent a
     * persistent object.
     */
    static boolean maybePC(int typeCode, Class<?> type) {
        if (type == null)
            return false;
        switch (typeCode) {
            case JavaTypes.OBJECT:
            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                return true;
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
                return !type.getName().startsWith("java.util.");
            default:
                return false;
        }
    }

    /**
     * Helper method to return the given array value as a collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Object val, Class<T> elem, boolean mutable) {
        if (val == null)
            return null;

        List<T> l;
        if (!elem.isPrimitive()) {
            // if an object array, use built-in list function
            l = Arrays.asList((T[]) val);
            if (mutable)
                l = new ArrayList<T>(l);
        } else {
            // convert to list of wrapper objects
            int length = Array.getLength(val);
            l = new ArrayList<T>(length);
            for (int i = 0; i < length; i++)
                l.add((T)Array.get(val, i));
        }
        return l;
    }

    /**
     * Helper method to return the given collection as an array.
     */
    public static Object toArray(Collection<?> coll, Class<?> elem) {
        if (coll == null)
            return null;

        Object array = Array.newInstance(elem, coll.size());
        int idx = 0;
        for (Iterator<?> itr = coll.iterator(); itr.hasNext(); idx++)
            Array.set(array, idx, itr.next ());
		return array;
	}
    
    /**
     * Determine whether or not the provided Object value is the default for the provided typeCode.
     * 
     * For example: If o = Integer(0) and typeCode = JavaTypes.INT, this method will return true.
     */
    public static boolean isPrimitiveDefault(Object o, int typeCode) {
        switch (typeCode) {
            case BOOLEAN:
                return ((Boolean) o).equals(Boolean.FALSE) ? true : false;
            case BYTE:
                return ((Byte) o) == 0 ? true : false;
            case SHORT:
                return ((Short) o) == 0 ? true : false;
            case INT:
                return ((Integer) o) == 0 ? true : false;
            case LONG:
                return ((Long) o) == 0L ? true : false;
            case FLOAT:
                return ((Float) o) == 0.0F ? true : false;
            case CHAR:
                return ((Character) o) == '\u0000' ? true : false;
            case DOUBLE:
                return ((Double) o) == 0.0d ? true : false;
        }
        return false;
    }
}
