/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.xbean.propertyeditor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.xbean.ClassLoading;

/**
 * The property editor manager.  This orchestrates Geronimo usage of
 * property editors, allowing additional search paths to be added and
 * specific editors to be registered.
 *
 * @version $Rev: 6687 $
 */
public class PropertyEditors {
    private static final Map registry = Collections.synchronizedMap(new ReferenceIdentityMap());
    private static final Map PRIMITIVE_TO_WRAPPER;
    private static final Map WRAPPER_TO_PRIMITIVE;

    /**
     * Register all of the built in converters
     */
    static {
        Map map = new HashMap();
        map.put(boolean.class, Boolean.class);
        map.put(char.class, Character.class);
        map.put(byte.class, Byte.class);
        map.put(short.class, Short.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(map);


        map = new HashMap();
        map.put(Boolean.class, boolean.class);
        map.put(Character.class, char.class);
        map.put(Byte.class, byte.class);
        map.put(Short.class, short.class);
        map.put(Integer.class, int.class);
        map.put(Long.class, long.class);
        map.put(Float.class, float.class);
        map.put(Double.class, double.class);
        WRAPPER_TO_PRIMITIVE = Collections.unmodifiableMap(map);

        // Explicitly register the types
        registerConverter(new ArrayListEditor());
        registerConverter(new BigDecimalEditor());
        registerConverter(new BigIntegerEditor());
        registerConverter(new BooleanEditor());
        registerConverter(new ByteEditor());
        registerConverter(new CharacterEditor());
        registerConverter(new ClassEditor());
        registerConverter(new DateEditor());
        registerConverter(new DoubleEditor());
        registerConverter(new FileEditor());
        registerConverter(new FloatEditor());
        registerConverter(new HashMapEditor());
        registerConverter(new HashtableEditor());
        registerConverter(new IdentityHashMapEditor());
        registerConverter(new Inet4AddressEditor());
        registerConverter(new Inet6AddressEditor());
        registerConverter(new InetAddressEditor());
        registerConverter(new IntegerEditor());
        registerConverter(new LinkedHashMapEditor());
        registerConverter(new LinkedHashSetEditor());
        registerConverter(new LinkedListEditor());
        registerConverter(new ListEditor());
        registerConverter(new LongEditor());
        registerConverter(new MapEditor());
        registerConverter(new ObjectNameEditor());
        registerConverter(new PropertiesEditor());
        registerConverter(new SetEditor());
        registerConverter(new ShortEditor());
        registerConverter(new SortedMapEditor());
        registerConverter(new SortedSetEditor());
        registerConverter(new StringEditor());
        registerConverter(new TreeMapEditor());
        registerConverter(new TreeSetEditor());
        registerConverter(new URIEditor());
        registerConverter(new URLEditor());
        registerConverter(new VectorEditor());
        registerConverter(new WeakHashMapEditor());
    }

    public static void registerConverter(Converter converter) {
        if (converter == null) throw new NullPointerException("editor is null");
        Class type = converter.getType();
        registry.put(type, converter);
        PropertyEditorManager.registerEditor(type, converter.getClass());

        if (PRIMITIVE_TO_WRAPPER.containsKey(type)) {
            Class wrapperType = (Class) PRIMITIVE_TO_WRAPPER.get(type);
            registry.put(wrapperType, converter);
            PropertyEditorManager.registerEditor(wrapperType, converter.getClass());
        } else if (WRAPPER_TO_PRIMITIVE.containsKey(type)) {
            Class primitiveType = (Class) WRAPPER_TO_PRIMITIVE.get(type);
            registry.put(primitiveType, converter);
            PropertyEditorManager.registerEditor(primitiveType, converter.getClass());
        }
    }

    public static boolean canConvert(String type, ClassLoader classLoader) {
        if (type == null) throw new NullPointerException("type is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        // load using the ClassLoading utility, which also manages arrays and primitive classes.
        Class typeClass = null;
        try {
            typeClass = ClassLoading.loadClass(type, classLoader);
        } catch (ClassNotFoundException e) {
            throw new PropertyEditorException("Type class could not be found: " + type);
        }

        return canConvert(typeClass);

    }

    public static boolean canConvert(Class type) {
        Converter converter = findConverter(type);
        if (converter != null) {
            return true;
        }

        // fall back to a property editor
        PropertyEditor editor = findEditor(type);
        if (editor != null) {
            return true;
        }

        return false;
    }

    public static String toString(Object value) throws PropertyEditorException {
        if (value == null) throw new NullPointerException("value is null");

        // get an editor for this type
        Class type = value.getClass();

        // try to get a converter from our registry as they are way faster and easier to use
        Converter converter = findConverter(type);
        if (converter != null) {
            return converter.toString(value);
        }

        // fall back to a property editor
        PropertyEditor editor = findEditor(type);
        if (editor == null) {
            throw new PropertyEditorException("Unable to find PropertyEditor for " + ClassLoading.getClassName(type, true));
        }

        // create the string value
        editor.setValue(value);
        String textValue = null;
        try {
            textValue = editor.getAsText();
        } catch (Exception e) {
            throw new PropertyEditorException("Error while converting a \"" + ClassLoading.getClassName(type, true) + "\" to text " +
                    " using the property editor " + ClassLoading.getClassName(editor.getClass(), true), e);
        }
        return textValue;
    }

    public static Object getValue(String type, String value, ClassLoader classLoader) throws PropertyEditorException {
        if (type == null) throw new NullPointerException("type is null");
        if (value == null) throw new NullPointerException("value is null");
        if (classLoader == null) throw new NullPointerException("classLoader is null");

        // load using the ClassLoading utility, which also manages arrays and primitive classes.
        Class typeClass = null;
        try {
            typeClass = ClassLoading.loadClass(type, classLoader);
        } catch (ClassNotFoundException e) {
            throw new PropertyEditorException("Type class could not be found: " + type);
        }

        return getValue(typeClass, value);

    }

    public static Object getValue(Class type, String value) throws PropertyEditorException {
        if (type == null) throw new NullPointerException("type is null");
        if (value == null) throw new NullPointerException("value is null");

        // try to get a converter from our registry as they are way faster and easier to use
        Converter converter = findConverter(type);
        if (converter != null) {
            return converter.toObject(value);
        }

        // fall back to a property editor
        PropertyEditor editor = findEditor(type);
        if (editor == null) {
            throw new PropertyEditorException("Unable to find PropertyEditor for " + ClassLoading.getClassName(type, true));
        }

        // create the object value
        editor.setAsText(value);
        Object objectValue = null;
        try {
            objectValue = editor.getValue();
        } catch (Exception e) {
            throw new PropertyEditorException("Error while converting \"" + value + "\" to a " + ClassLoading.getClassName(type, true) +
                    " using the property editor " + ClassLoading.getClassName(editor.getClass(), true), e);
        }
        return objectValue;
    }

    private static Converter findConverter(Class type) {
        if (type == null) throw new NullPointerException("type is null");

        Converter converter = (Converter) registry.get(type);

        // we're outta here if we got one.
        if (converter != null) {
            return converter;
        }

        Class[] declaredClasses = type.getDeclaredClasses();
        for (int i = 0; i < declaredClasses.length; i++) {
            Class declaredClass = declaredClasses[i];
            if (Converter.class.isAssignableFrom(declaredClass)) {
                try {
                    converter = (Converter) declaredClass.newInstance();
                    registerConverter(converter);

                    // try to get the converter from the registry... the converter
                    // created above may have been for another class
                    converter = (Converter) registry.get(type);
                    if (converter != null) {
                        return converter;
                    }
                } catch (Exception e) {
                }

            }
        }

        // it's possible this was a request for an array class.  We might not
        // recognize the array type directly, but the component type might be
        // resolvable
        if (type.isArray() && !type.getComponentType().isArray()) {
            // do a recursive lookup on the base type
            converter = findConverter(type.getComponentType());
            // if we found a suitable editor for the base component type,
            // wrapper this in an array adaptor for real use
            if (converter != null) {
                return new ArrayConverter(type, converter);
            }
        }

        // nothing found
        return null;
    }

    /**
     * Locate a property editor for qiven class of object.
     *
     * @param type The target object class of the property.
     * @return The resolved editor, if any.  Returns null if a suitable editor
     *         could not be located.
     */
    private static PropertyEditor findEditor(Class type) {
        if (type == null) throw new NullPointerException("type is null");

        // try to locate this directly from the editor manager first.
        PropertyEditor editor = PropertyEditorManager.findEditor(type);

        // we're outta here if we got one.
        if (editor != null) {
            return editor;
        }

        // it's possible this was a request for an array class.  We might not
        // recognize the array type directly, but the component type might be
        // resolvable
        if (type.isArray() && !type.getComponentType().isArray()) {
            // do a recursive lookup on the base type
            editor = findEditor(type.getComponentType());
            // if we found a suitable editor for the base component type,
            // wrapper this in an array adaptor for real use
            if (editor != null) {
                return new ArrayConverter(type, editor);
            }
        }

        // nothing found
        return null;
    }
}
