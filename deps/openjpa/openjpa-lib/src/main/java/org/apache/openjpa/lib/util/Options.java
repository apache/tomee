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
package org.apache.openjpa.lib.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import serp.util.Strings;

/**
 * A specialization of the {@link Properties} map type with the added
 * abilities to read application options from the command line and to
 * use bean patterns to set an object's properties via command-line the
 * stored mappings.
 * A typical use pattern for this class is to construct a new instance
 * in the <code>main</code> method, then call {@link #setFromCmdLine} with the
 * given args. Next, an instanceof the class being invoked is created, and
 * {@link #setInto} is called with that instance as a parameter. With this
 * pattern, the user can configure any bean properties of the class, or even
 * properties of classes reachable from the class, through the command line.
 *
 * @author Abe White
 * @nojavadoc
 */
@SuppressWarnings("serial")
public class Options extends TypedProperties {

    /**
     * Immutable empty instance.
     */
    public static Options EMPTY = new EmptyOptions();

    // maps primitive types to the appropriate wrapper class and default value
    private static Object[][] _primWrappers = new Object[][]{
        { boolean.class, Boolean.class, Boolean.FALSE },
        { byte.class, Byte.class, Byte.valueOf((byte) 0) },
        { char.class, Character.class, Character.valueOf((char) 0) },
        { double.class, Double.class, Double.valueOf(0D) },
        { float.class, Float.class, Float.valueOf(0F) },
        { int.class, Integer.class, Integer.valueOf(0) },
        { long.class, Long.class, Long.valueOf(0L) },
        { short.class, Short.class, Short.valueOf((short) 0) }, };
    
    private static Localizer _loc = Localizer.forPackage(Options.class);

    /**
     * Default constructor.
     */
    public Options() {
        super();
    }

    /**
     * Construct the options instance with the given set of defaults.
     *
     * @see Properties#Properties(Properties)
     */
    public Options(Properties defaults) {
        super(defaults);
    }

    /**
     * Parses the given argument list into flag/value pairs, which are stored
     * as properties. Flags that are present without values are given
     * the value "true". If any flag is found for which there is already
     * a mapping present, the existing mapping will be overwritten.
     * Flags should be of the form:<br />
     * <code>java Foo -flag1 value1 -flag2 value2 ... arg1 arg2 ...</code>
     *
     * @param args the command-line arguments
     * @return all arguments in the original array beyond the
     * flag/value pair list
     * @author Patrick Linskey
     */
    public String[] setFromCmdLine(String[] args) {
        if (args == null || args.length == 0)
            return args;

        String key = null;
        String value = null;
        List<String> remainder = new LinkedList<String>();
        for (int i = 0; i < args.length + 1; i++) {
            if (i == args.length || args[i].startsWith("-")) {
                key = trimQuote(key);
                if (key != null) {
                    if (!StringUtils.isEmpty(value))
                        setProperty(key, trimQuote(value));
                    else
                        setProperty(key, "true");
                }

                if (i == args.length)
                    break;
                else {
                    key = args[i].substring(1);
                    value = null;
                }
            } else if (key != null) {
                setProperty(key, trimQuote(args[i]));
                key = null;
            } else
                remainder.add(args[i]);
        }

        return remainder.toArray(new String[remainder.size()]);
    }

    /**
     * This method uses reflection to set all the properties in the given
     * object that are named by the keys in this map. For a given key 'foo',
     * the algorithm will look for a 'setFoo' method in the given instance.
     * For a given key 'foo.bar', the algorithm will first look for a
     * 'getFoo' method in the given instance, then will recurse on the return
     * value of that method, now looking for the 'bar' property. This allows
     * the setting of nested object properties. If in the above example the
     * 'getFoo' method is not present or returns null, the algorithm will
     * look for a 'setFoo' method; if found it will constrct a new instance
     * of the correct type, set it using the 'setFoo' method, then recurse on
     * it as above. Property names can be nested in this way to an arbitrary
     * depth. For setter methods that take multiple parameters, the value
     * mapped to the key can use the ',' as an argument separator character.
     * If not enough values are present for a given method after splitting
     * the string on ',', the remaining arguments will receive default
     * values. All arguments are converted from string form to the
     * correct type if possible(i.e. if the type is primitive,
     * java.lang.Clas, or has a constructor that takes a single string
     * argument). Examples:
     * <ul>
     * <li>Map Entry: <code>"age"-&gt;"12"</code><br />
     * Resultant method call: <code>obj.setAge(12)</code></li>
     * <li>Map Entry: <code>"range"-&gt;"1,20"</code><br />
     * Resultant method call: <code>obj.setRange(1, 20)</code></li>
     * <li>Map Entry: <code>"range"-&gt;"10"</code><br />
     * Resultant method call: <code>obj.setRange(10, 10)</code></li>
     * <li>Map Entry: <code>"brother.name"-&gt;"Bob"</code><br />
     * Resultant method call: <code>obj.getBrother().setName("Bob")
     * <code></li>
     * </ul> 
     * Any keys present in the map for which there is no
     * corresponding property in the given object will be ignored,
     * and will be returned in the {@link Map} returned by this method.
     *
     * @return an {@link Options} of key-value pairs in this object
     * for which no setters could be found.
     * @throws RuntimeException on parse error
     */
    public Options setInto(Object obj) {
        // set all defaults that have no explicit value
        Map.Entry entry = null;
        if (defaults != null) {
            for (Iterator<?> itr = defaults.entrySet().iterator(); itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                if (!containsKey(entry.getKey()))
                    setInto(obj, entry);
            }
        }

        // set from main map
        Options invalidEntries = null;
        Map.Entry e;
        for (Iterator<?> itr = entrySet().iterator(); itr.hasNext();) {
            e = (Map.Entry) itr.next();
            if (!setInto(obj, e)) {
                if (invalidEntries == null)
                    invalidEntries = new Options();
                invalidEntries.put(e.getKey(), e.getValue());
            }
        }
        return (invalidEntries == null) ? EMPTY : invalidEntries;
    }

    /**
     * Sets the property named by the key of the given entry in the
     * given object.
     *
     * @return <code>true</code> if the set succeeded, or
     * <code>false</code> if no method could be found for this property.
     */
    private boolean setInto(Object obj, Map.Entry entry) {
        if (entry.getKey() == null)
            return false;

        try {
            // look for matching parameter of object
            Object[] match = new Object[]{ obj, null };
            if (!matchOptionToMember(entry.getKey().toString(), match))
                return false;

            Class[] type = getType(match[1]);
            Object[] values = new Object[type.length];
            String[] strValues;
            if (entry.getValue() == null)
                strValues = new String[1];
            else if (values.length == 1)
                strValues = new String[]{ entry.getValue().toString() };
            else
                strValues = Strings.split(entry.getValue().toString(), ",", 0);

            // convert the string values into parameter values, if not
            // enough string values repeat last one for rest
            for (int i = 0; i < strValues.length; i++)
                values[i] = stringToObject(strValues[i].trim(), type[i]);
            for (int i = strValues.length; i < values.length; i++)
                values[i] = getDefaultValue(type[i]);

            // invoke the setter / set the field
            invoke(match[0], match[1], values);
            return true;
        } catch (Throwable t) {
            throw new ParseException(obj + "." + entry.getKey() + " = " + entry.getValue(), t);
        }
    }

    /**
     * Removes leading and trailing single quotes from the given String, if any.
     */
    private static String trimQuote(String val) {
        if (val != null && val.startsWith("'") && val.endsWith("'"))
            return val.substring(1, val.length() - 1);
        return val;
    }

    /**
     * Finds all the options that can be set on the provided class. This does
     * not look for path-traversal expressions.
     *
     * @param type The class for which available options should be listed.
     * @return The available option names in <code>type</code>. The
     * names will have initial caps. They will be ordered alphabetically.
     */
    public static Collection<String> findOptionsFor(Class<?> type) {
        Collection<String> names = new TreeSet<String>();
        // look for a setter method matching the key
        Method[] meths = type.getMethods();
        Class<?>[] params;
        for (int i = 0; i < meths.length; i++) {
            if (meths[i].getName().startsWith("set")) {
                params = meths[i].getParameterTypes();
                if (params.length == 0)
                    continue;
                if (params[0].isArray())
                    continue;

                names.add(StringUtils.capitalize(
                    meths[i].getName().substring(3)));
            }
        }

        // check for public fields
        Field[] fields = type.getFields();
        for (int i = 0; i < fields.length; i++)
            names.add(StringUtils.capitalize(fields[i].getName()));

        return names;
    }

    /**
     * Matches a key to an object/setter pair.
     *
     * @param key the key given at the command line; may be of the form
     * 'foo.bar' to signify the 'bar' property of the 'foo' owned object
     * @param match an array of length 2, where the first index is set
     * to the object to retrieve the setter for
     * @return true if a match was made, false otherwise; additionally,
     * the first index of the match array will be set to
     * the matching object and the second index will be
     * set to the setter method or public field for the
     * property named by the key
     */
    private static boolean matchOptionToMember(String key, Object[] match)
        throws Exception {
        if (StringUtils.isEmpty(key))
            return false;

        // unfortunately we can't use bean properties for setters; any
        // setter with more than 1 argument is ignored; calculate setter and getter
        // name to look for
        String[] find = Strings.split(key, ".", 2);
        String base = StringUtils.capitalize(find[0]);
        String set = "set" + base;
        String get = "get" + base;

        // look for a setter/getter matching the key; look for methods first
        Class<? extends Object> type = match[0].getClass();
        Method[] meths = type.getMethods();
        Method setMeth = null;
        Method getMeth = null;
        Class[] params;
        for (int i = 0; i < meths.length; i++) {
            if (meths[i].getName().equals(set)) {
                params = meths[i].getParameterTypes();
                if (params.length == 0)
                    continue;
                if (params[0].isArray())
                    continue;

                // use this method if we haven't found any other setter, if
                // it has less parameters than any other setter, or if it uses
                // string parameters
                if (setMeth == null)
                    setMeth = meths[i];
                else if (params.length < setMeth.getParameterTypes().length)
                    setMeth = meths[i];
                else if (params.length == setMeth.getParameterTypes().length
                    && params[0] == String.class)
                    setMeth = meths[i];
            } else if (meths[i].getName().equals(get))
                getMeth = meths[i];
        }

        // if no methods found, check for public field
        Member setter = setMeth;
        Member getter = getMeth;
        if (setter == null) {
            Field[] fields = type.getFields();
            String uncapBase = StringUtils.uncapitalize(find[0]);
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().equals(base)
                    || fields[i].getName().equals(uncapBase)) {
                    setter = fields[i];
                    getter = fields[i];
                    break;
                }
            }
        }

        // if no way to access property, give up
        if (setter == null && getter == null)
            return false;

        // recurse on inner object with remainder of key?
        if (find.length > 1) {
            Object inner = null;
            if (getter != null)
                inner = invoke(match[0], getter, null);

            // if no getter or current inner is null, try to create a new
            // inner instance and set it in object
            if (inner == null && setter != null) {
                Class<?> innerType = getType(setter)[0];
                try {
                    inner = AccessController.doPrivileged(
                        J2DoPrivHelper.newInstanceAction(innerType));
                } catch (PrivilegedActionException pae) {
                    throw pae.getException();
                }
                invoke(match[0], setter, new Object[]{ inner });
            }
            match[0] = inner;
            return matchOptionToMember(find[1], match);
        }

        // got match; find setter for property
        match[1] = setter;
        return match[1] != null;
    }

    /**
     * Return the types of the parameters needed to set the given member.
     */
    private static Class<?>[] getType(Object member) {
        if (member instanceof Method)
            return ((Method) member).getParameterTypes();
        return new Class[]{ ((Field) member).getType() };
    }

    /**
     * Set the given member to the given value(s).
     */
    private static Object invoke(Object target, Object member, Object[] values)
        throws Exception {
        if (member instanceof Method)
            return ((Method) member).invoke(target, values);
        if (values == null || values.length == 0)
            return ((Field) member).get(target);
        ((Field) member).set(target, values[0]);
        return null;
    }

    /**
     * Converts the given string into an object of the given type, or its
     * wrapper type if it is primitive.
     */
    private Object stringToObject(String str, Class<?> type) throws Exception {
        // special case for null and for strings
        if (str == null || type == String.class)
            return str;

        // special case for creating Class instances
        if (type == Class.class)
            return Class.forName(str, false, getClass().getClassLoader());

        // special case for numeric types that end in .0; strip the decimal
        // places because it can kill int, short, long parsing
        if (type.isPrimitive() || Number.class.isAssignableFrom(type))
            if (str.length() > 2 && str.endsWith(".0"))
                str = str.substring(0, str.length() - 2);

        // for primitives, recurse on wrapper type
        if (type.isPrimitive())
            for (int i = 0; i < _primWrappers.length; i++)
                if (type == _primWrappers[i][0])
                    return stringToObject(str, (Class<?>) _primWrappers[i][1]);

        // look for a string constructor
        Exception err = null;
        try {
            Constructor<?> cons = type.getConstructor(new Class[]{ String.class });
            if (type == Boolean.class && "t".equalsIgnoreCase(str))
                str = "true";
            return cons.newInstance(new Object[]{ str });
        } catch (Exception e) {
            err = new ParseException(_loc.get("conf-no-constructor", str, type), e);
        }

        // special case: the argument value is a subtype name and a new instance
        // of that type should be set as the object
        Class<?> subType = null;
        try {
            subType = Class.forName(str);
        } catch (Exception e) {
            err = e;
            throw new ParseException(_loc.get("conf-no-type", str, type), e);
        }
        if (!type.isAssignableFrom(subType))
            throw err;
        try {
            return AccessController.doPrivileged(J2DoPrivHelper.newInstanceAction(subType));
        } catch (PrivilegedActionException pae) {
            throw pae.getException();
        }
    }

    /**
     * Returns the default value for the given parameter type.
     */
    private Object getDefaultValue(Class<?> type) {
        for (int i = 0; i < _primWrappers.length; i++)
            if (_primWrappers[i][0] == type)
                return _primWrappers[i][2];

        return null;
    }

    /**
     * Specialization of {@link #getBooleanProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public boolean getBooleanProperty(String key, String key2, boolean def) {
        String val = getProperty(key);
        if (val == null)
            val = getProperty(key2);
        if (val == null)
            return def;
        return "t".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val);
    }

    /**
     * Specialization of {@link TypedProperties#getFloatProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public float getFloatProperty(String key, String key2, float def) {
        String val = getProperty(key);
        if (val == null)
            val = getProperty(key2);
        return (val == null) ? def : Float.parseFloat(val);
    }

    /**
     * Specialization of {@link TypedProperties#getDoubleProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public double getDoubleProperty(String key, String key2, double def) {
        String val = getProperty(key);
        if (val == null)
            val = getProperty(key2);
        return (val == null) ? def : Double.parseDouble(val);
    }

    /**
     * Specialization of {@link TypedProperties#getLongProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public long getLongProperty(String key, String key2, long def) {
        String val = getProperty(key);
        if (val == null)
            val = getProperty(key2);
        return (val == null) ? def : Long.parseLong(val);
    }

    /**
     * Specialization of {@link TypedProperties#getIntProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public int getIntProperty(String key, String key2, int def) {
        String val = getProperty(key);
        if (val == null)
            val = getProperty(key2);
        return (val == null) ? def : Integer.parseInt(val);
    }

    /**
     * Specialization of {@link Properties#getProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public String getProperty(String key, String key2, String def) {
        String val = getProperty(key);
        return (val == null) ? getProperty(key2, def) : val;
    }

    /**
     * Specialization of {@link TypedProperties#removeBooleanProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public boolean removeBooleanProperty(String key, String key2, boolean def) {
        String val = removeProperty(key);
        if (val == null)
            val = removeProperty(key2);
        else
            removeProperty(key2);
        if (val == null)
            return def;
        return "t".equalsIgnoreCase(val) || "true".equalsIgnoreCase(val);
    }

    /**
     * Specialization of {@link TypedProperties#removeFloatProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public float removeFloatProperty(String key, String key2, float def) {
        String val = removeProperty(key);
        if (val == null)
            val = removeProperty(key2);
        else
            removeProperty(key2);
        return (val == null) ? def : Float.parseFloat(val);
    }

    /**
     * Specialization of {@link TypedProperties#removeDoubleProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public double removeDoubleProperty(String key, String key2, double def) {
        String val = removeProperty(key);
        if (val == null)
            val = removeProperty(key2);
        else
            removeProperty(key2);
        return (val == null) ? def : Double.parseDouble(val);
    }

    /**
     * Specialization of {@link TypedProperties#removeLongProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public long removeLongProperty(String key, String key2, long def) {
        String val = removeProperty(key);
        if (val == null)
            val = removeProperty(key2);
        else
            removeProperty(key2);
        return (val == null) ? def : Long.parseLong(val);
    }

    /**
     * Specialization of {@link TypedProperties#removeIntProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public int removeIntProperty(String key, String key2, int def) {
        String val = removeProperty(key);
        if (val == null)
            val = removeProperty(key2);
        else
            removeProperty(key2);
        return (val == null) ? def : Integer.parseInt(val);
    }

    /**
     * Specialization of {@link Properties#removeProperty} to allow
     * a value to appear under either of two keys; useful for short and
     * long versions of command-line flags.
     */
    public String removeProperty(String key, String key2, String def) {
        String val = removeProperty(key);
        return (val == null) ? removeProperty(key2, def) : val;
    }

    /**
     * Immutable empty options.
     */
    private static class EmptyOptions extends Options {

        public Object setProperty(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public Object put(Object key, Object value) {
            throw new UnsupportedOperationException();
        }
    }
}
