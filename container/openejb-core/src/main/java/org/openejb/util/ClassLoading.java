/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.openejb.util;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Utility class for loading classes by a variety of name variations.
 * <p/>
 * Supported names types are:
 * <p/>
 * 1)  Fully qualified class name (e.g., "java.lang.String", "org.openejb.util.ClassLoading"
 * 2)  Method signature encoding ("Ljava.lang.String;", "J", "I", etc.)
 * 3)  Primitive type names ("int", "boolean", etc.)
 * 4)  Method array signature strings ("[I", "[Ljava.lang.String")
 * 5)  Arrays using Java code format ("int[]", "java.lang.String[][]")
 * <p/>
 * The classes are loaded using the provided class loader.  For the basic types, the primitive
 * reflection types are returned.
 *
 * @version $Rev$
 */
public class ClassLoading {

    /**
     * Table for mapping primitive class names/signatures to the implementing
     * class object
     */
    private static final HashMap PRIMITIVE_CLASS_MAP = new HashMap();

    /**
     * Table for mapping primitive classes back to their name signature type, which
     * allows a reverse mapping to be performed from a class object into a resolvable
     * signature.
     */
    private static final HashMap CLASS_TO_SIGNATURE_MAP = new HashMap();


    /**
     * Setup the primitives map.  We make any entry for each primitive class using both the
     * human readable name and the method signature shorthand type.
     */
    static {
        PRIMITIVE_CLASS_MAP.put("boolean", boolean.class);
        PRIMITIVE_CLASS_MAP.put("Z", boolean.class);
        PRIMITIVE_CLASS_MAP.put("byte", byte.class);
        PRIMITIVE_CLASS_MAP.put("B", byte.class);
        PRIMITIVE_CLASS_MAP.put("char", char.class);
        PRIMITIVE_CLASS_MAP.put("C", char.class);
        PRIMITIVE_CLASS_MAP.put("short", short.class);
        PRIMITIVE_CLASS_MAP.put("S", short.class);
        PRIMITIVE_CLASS_MAP.put("int", int.class);
        PRIMITIVE_CLASS_MAP.put("I", int.class);
        PRIMITIVE_CLASS_MAP.put("long", long.class);
        PRIMITIVE_CLASS_MAP.put("J", long.class);
        PRIMITIVE_CLASS_MAP.put("float", float.class);
        PRIMITIVE_CLASS_MAP.put("F", float.class);
        PRIMITIVE_CLASS_MAP.put("double", double.class);
        PRIMITIVE_CLASS_MAP.put("D", double.class);
        PRIMITIVE_CLASS_MAP.put("void", void.class);
        PRIMITIVE_CLASS_MAP.put("V", void.class);

        // Now build a reverse mapping table.  The table above has a many-to-one mapping for
        // class names.  To do the reverse, we need to pick just one.  As long as the
        // returned name supports "round tripping" of the requests, this will work fine.

        CLASS_TO_SIGNATURE_MAP.put(boolean.class, "Z");
        CLASS_TO_SIGNATURE_MAP.put(byte.class, "B");
        CLASS_TO_SIGNATURE_MAP.put(char.class, "C");
        CLASS_TO_SIGNATURE_MAP.put(short.class, "S");
        CLASS_TO_SIGNATURE_MAP.put(int.class, "I");
        CLASS_TO_SIGNATURE_MAP.put(long.class, "J");
        CLASS_TO_SIGNATURE_MAP.put(float.class, "F");
        CLASS_TO_SIGNATURE_MAP.put(double.class, "D");
        CLASS_TO_SIGNATURE_MAP.put(void.class, "V");
    }


    /**
     * Load a class that matches the requested name, using the provided class loader context.
     * <p/>
     * The class name may be a standard class name, the name of a primitive type Java
     * reflection class (e.g., "boolean" or "int"), or a type in method type signature
     * encoding.  Array classes in either encoding form are also processed.
     *
     * @param className The name of the required class.
     * @param classLoader The class loader used to resolve the class object.
     * @return The Class object resolved from "className".
     * @throws ClassNotFoundException When unable to resolve the class object.
     * @throws IllegalArgumentException If either argument is null.
     */
    public static Class loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {

        // the tests require IllegalArgumentExceptions for null values on either of these.
        if (className == null) {
            throw new IllegalArgumentException("className is null");
        }

        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader is null");
        }
        // The easiest case is a proper class name.  We just have the class loader resolve this.
        // If the class loader throws a ClassNotFoundException, then we need to check each of the
        // special name encodings we support.
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException ignore) {
            // if not found, continue on to the other name forms.
        }


        // The second easiest version to resolve is a direct map to a primitive type name
        // or method signature.  Check our name-to-class map for one of those.
        Class resolvedClass = (Class) PRIMITIVE_CLASS_MAP.get(className);
        if (resolvedClass != null) {
            return resolvedClass;
        }

        // Class names in method signature have the format "Lfully.resolved.name;",
        // so if it ends in a semicolon and begins with an "L", this must be in
        // this format.  Have the class loader try to load this.  There are no other
        // options if this fails, so just allow the class loader to throw the
        // ClassNotFoundException.
        if (className.endsWith(";") && className.startsWith("L")) {
            // pick out the name portion
            String typeName = className.substring(1, className.length() - 1);
            // and delegate the loading to the class loader.
            return classLoader.loadClass(typeName);
        }

        // All we have left now are the array types.  Method signature array types
        // have a series of leading "[" characters to specify the number of dimensions.
        // The other array type we handle uses trailing "[]" for the dimensions, just
        // like the Java language syntax.

        // first check for the signature form ([[[[type).
        if (className.charAt(0) == '[') {
            // we have at least one array marker, now count how many leading '['s we have
            // to get the dimension count.
            int count = 0;
            int nameLen = className.length();

            while (count < nameLen && className.charAt(count) == '[') {
                count++;
            }

            // pull of the name subtype, which is everything after the last '['
            String arrayTypeName = className.substring(count, className.length());
            // resolve the type using a recursive call, which will load any of the primitive signature
            // types as well as class names.
            Class arrayType = loadClass(arrayTypeName, classLoader);

            // Resolving array types require a little more work.  The array classes are
            // created dynamically when the first instance of a given dimension and type is
            // created.  We need to create one using reflection to do this.
            return getArrayClass(arrayType, count);
        }


        // ok, last chance.  Now check for an array specification in Java language
        // syntax.  This will be a type name followed by pairs of "[]" to indicate
        // the number of dimensions.
        if (className.endsWith("[]")) {
            // get the base component class name and the arrayDimensions
            int count = 0;
            int position = className.length();

            while (position > 1 && className.substring(position - 2, position).equals("[]")) {
                // count this dimension
                count++;
                // and step back the probe position.
                position -= 2;
            }

            // position now points at the location of the last successful test.  This makes it
            // easy to pick off the class name.

            String typeName = className.substring(0, position);

            // load the base type, again, doing this recursively
            Class arrayType = loadClass(typeName, classLoader);
            // and turn this into the class object
            return getArrayClass(arrayType, count);
        }

        // We're out of options, just toss an exception over the wall.
        throw new ClassNotFoundException(className);
    }


    /**
     * Map a class object back to a class name.  The returned class object
     * must be "round trippable", which means
     * <p/>
     * type == ClassLoading.loadClass(ClassLoading.getClassName(type), classLoader)
     * <p/>
     * must be true.  To ensure this, the class name is always returned in
     * method signature format.
     *
     * @param type The class object we convert into name form.
     * @return A string representation of the class name, in method signature
     *         format.
     */
    public static String getClassName(Class type) {
        StringBuffer name = new StringBuffer();

        // we test these in reverse order from the resolution steps,
        // first handling arrays, then primitive types, and finally
        // "normal" class objects.

        // First handle arrays.  If a class is an array, the type is
        // element stored at that level.  So, for a 2-dimensional array
        // of ints, the top-level type will be "[I".  We need to loop
        // down the hierarchy until we hit a non-array type.
        while (type.isArray()) {
            // add another array indicator at the front of the name,
            // and continue with the next type.
            name.append('[');
            type = type.getComponentType();
        }

        // we're down to the base type.  If this is a primitive, then
        // we poke in the single-character type specifier.
        if (type.isPrimitive()) {
            name.append((String) CLASS_TO_SIGNATURE_MAP.get(type));
        }
        // a "normal" class.  This gets expressing using the "Lmy.class.name;" syntax.
        else {
            name.append('L');
            name.append(type.getName());
            name.append(';');
        }
        return name.toString();
    }

    private static Class getArrayClass(Class type, int dimension) {
        // Array.newInstance() requires an array of the requested number of dimensions
        // that gives the size for each dimension.  We just request 0 in each of the
        // dimentions, which is not unlike a black hole sigularity.
        int dimensions[] = new int[dimension];
        // create an instance and return the associated class object.
        return Array.newInstance(type, dimensions).getClass();
    }
}
