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
package org.apache.openjpa.kernel;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.UserException;

/**
 * Helper class to pack results into the result class set on the query.
 *
 * @author Abe White
 * @author Patrick Linskey
 * @nojavadoc
 */
public class ResultPacker {

    private static final Localizer _loc = Localizer.forPackage
        (ResultPacker.class);
    private static final Set<Class<?>> _stdTypes = new HashSet<Class<?>>();

    static {
        _stdTypes.add(Object[].class);
        _stdTypes.add(Object.class);
        _stdTypes.add(Map.class);
        _stdTypes.add(HashMap.class);
        _stdTypes.add(Character.class);
        _stdTypes.add(Boolean.class);
        _stdTypes.add(Byte.class);
        _stdTypes.add(Short.class);
        _stdTypes.add(Integer.class);
        _stdTypes.add(Long.class);
        _stdTypes.add(Float.class);
        _stdTypes.add(Double.class);
        _stdTypes.add(String.class);
        _stdTypes.add(BigInteger.class);
        _stdTypes.add(BigDecimal.class);
        _stdTypes.add(Date.class);
        _stdTypes.add(java.sql.Date.class);
        _stdTypes.add(java.sql.Time.class);
        _stdTypes.add(java.sql.Timestamp.class);
        _stdTypes.add(Calendar.class);
        _stdTypes.add(GregorianCalendar.class);
    }

    private final Class<?> _resultClass;
    private final String[] _aliases;
    private final Member[] _sets;
    private final Method _put;
    private final Constructor<?> _constructor;
    
    /**
     * Protected constructor to bypass this implementation but allow extension.
     */
    protected ResultPacker() {
        _resultClass = null;
        _aliases = null;
        _sets = null;
        _put = null;
        _constructor = null;
    }

    /**
     * Constructor for result class without a projection.
     */
    public ResultPacker(Class<?> candidate, String alias, Class<?> resultClass) {
        this(candidate, null, new String[]{ alias }, resultClass);
    }

    /**
     * Constructor for standard projection.
     *
     * @param types the projection value types
     * @param aliases the alias for each projection value
     * @param resultClass the class to pack into
     */
    public ResultPacker(Class<?>[] types, String[] aliases, Class<?> resultClass) {
        this(null, types, aliases, resultClass);
    }

    /**
     * Internal constructor.
     */
    private ResultPacker(Class<?> candidate, Class<?>[] types, String[] aliases, Class<?> resultClass) {
        _aliases = aliases;
        if (candidate == resultClass || isInterface(resultClass, candidate) 
         ||(types != null && types.length == 1 && types[0] == resultClass) 
         || resultClass.isArray()) {
            _resultClass = resultClass;
            _sets = null;
            _put = null;
            _constructor = null;
        } else if (resultClass.isPrimitive()) {
            assertConvertable(candidate, types, resultClass);
            _resultClass = Filters.wrap(resultClass);
            _sets = null;
            _put = null;
            _constructor = null;
        } else if (!_stdTypes.contains(_resultClass = resultClass)) {
            // check for a constructor that matches the projection types
            Constructor<?> cons = null;
            if (types != null && types.length > 0) {
                try {
                    cons = _resultClass.getConstructor(types);
                } catch (NoSuchMethodException nsme) {
                }
            }
            _constructor = cons;

            if (cons == null) {
                Method[] methods = _resultClass.getMethods();
                Field[] fields = _resultClass.getFields();
                _put = findPut(methods);
                _sets = new Member[aliases.length];

                Class<?> type;
                for (int i = 0; i < _sets.length; i++) {
                    type = (types == null) ? candidate : types[i];
                    _sets[i] = findSet(aliases[i], type, fields, methods);
                    if (_sets[i] == null && _put == null)
                        throw new UserException(_loc.get("cant-set",
                            resultClass, aliases[i],
                            types == null ? null : Arrays.asList(types)));
                }
            } else {
                _sets = null;
                _put = null;
            }
        } else {
            if (resultClass != Map.class && resultClass != HashMap.class
                && resultClass != Object[].class)
                assertConvertable(candidate, types, resultClass);
            _sets = null;
            _put = null;
            _constructor = null;
        }
    }
    
    boolean isInterface(Class<?> intf, Class<?> actual) {
        if (actual != null) {
            Class<?>[] intfs = actual.getInterfaces();
            for (Class<?> c : intfs) {
                if (c == intf)
                    return true;
            }
        }
        return false;
    }

    /**
     * Ensure that conversion is possible.
     */
    private void assertConvertable(Class<?> candidate, Class<?>[] types,
        Class<?> resultClass) {
        Class<?> c = (types == null) ? candidate : types[0];
        if ((types != null && types.length != 1) || (c != null
            && c != Object.class && !Filters.canConvert(c, resultClass, true)))
            throw new UserException(_loc.get("cant-convert-result",
                c, resultClass));
    }

    /**
     * Pack the given object into an instance of the query's result class.
     */
    public Object pack(Object result) {
        if (result == null || _resultClass == result.getClass())
            return result;
        // special cases for efficient basic types where we want to avoid
        // creating an array for call to general pack method below
        if (_resultClass == Object.class)
            return result;
        if (_resultClass == Object[].class)
            return new Object[]{ result };
        if (_resultClass == HashMap.class || _resultClass == Map.class) {
            HashMap<String,Object> map = new HashMap<String,Object>(1, 1F);
            map.put(_aliases[0], result);
            return map;
        }

        // primitive or simple type?
        if (_constructor == null && _sets == null)
            return Filters.convert(result, _resultClass);

        // this is some complex case, so worth it to create the array and
        // use the general pack method
        return packUserType(new Object[]{ result });
    }

    /**
     * Pack the given array into an instance of the query's result class.
     */
    public Object pack(Object[] result) {
        if (result == null || result.length == 0)
            return null;

        // special cases for object arrays and maps
        if (_resultClass == Object[].class) {
            // the result might contain extra data at the end
            if (result.length > _aliases.length) {
                Object[] trim = new Object[_aliases.length];
                System.arraycopy(result, 0, trim, 0, trim.length);
                return trim;
            }
            return result;
        }
        if (_resultClass.isArray()) {
            Class<?> elementType = _resultClass.getComponentType();
            Object castResult = Array.newInstance(elementType, result.length);
            for (int i = 0; i < result.length; i++)
                Array.set(castResult, i, elementType.cast(result[i]));
            return castResult;
        }
        if (_resultClass == Object.class)
            return result[0];
        if (_resultClass == HashMap.class || _resultClass == Map.class) {
            Map<String,Object> map = new HashMap<String,Object>(result.length);
            for (int i = 0; i < _aliases.length; i++)
                map.put(_aliases[i], result[i]);
            return map;
        }

        // primitive or simple type?
        if (_sets == null && _constructor == null)
            return Filters.convert(result[0], _resultClass);

        // must be a user-defined type
        return packUserType(result);
    }

    /**
     * Pack the given result into the user-defined result class.
     */
    private Object packUserType(Object[] result) {
        try {
            // use the constructor first, if we have one
            if (_constructor != null)
                return _constructor.newInstance(result);

            Object user = AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(_resultClass));
            for (int i = 0; i < _aliases.length; i++) {
                if (_sets[i] instanceof Method) {
                    Method meth = (Method) _sets[i];
                    meth.invoke(user, new Object[]{ Filters.convert
                        (result[i], meth.getParameterTypes()[0]) });
                } else if (_sets[i] instanceof Field) {
                    Field field = (Field) _sets[i];
                    field.set(user, Filters.convert(result[i],
                        field.getType()));
                } else if (_put != null) {
                    _put.invoke(user, new Object[]{ _aliases[i], result[i] });
                }
            }
            return user;
        } catch (OpenJPAException ke) {
            throw ke;
        } catch (PrivilegedActionException pae) {
            throw new UserException(_loc.get("pack-instantiation-err",
                _resultClass), pae.getException());
        } catch (InstantiationException ie) {
            throw new UserException(_loc.get("pack-instantiation-err",
                _resultClass), ie);
        } catch (Exception e) {
            throw new UserException(_loc.get("pack-err", _resultClass), e);
        }
    }

    /**
     * Return the set method for the given property.
     */
    private static Member findSet(String alias, Class<?> type, Field[] fields,
        Method[] methods) {
        if (StringUtils.isEmpty(alias))
            return null;
        if (type == Object.class)
            type = null;

        // check public fields first
        Field field = null;
        for (int i = 0; i < fields.length; i++) {
            // if we find a field with the exact name, either return it
            // if it's the right type or give up if it's not
            if (fields[i].getName().equals(alias)) {
                if (type == null
                    || Filters.canConvert(type, fields[i].getType(), true))
                    return fields[i];
                break;
            }

            // otherwise if we find a field with the right name but the
            // wrong case, record it and if we don't find an exact match
            // for a field or setter we'll use it
            if (field == null && fields[i].getName().equalsIgnoreCase(alias)
                && (type == null
                || Filters.canConvert(type, fields[i].getType(), true)))
                field = fields[i];
        }

        // check setter methods
        String setName = "set" + StringUtils.capitalize(alias);
        Method method = null;
        boolean eqName = false;
        Class<?>[] params;
        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].getName().equalsIgnoreCase(setName))
                continue;
            params = methods[i].getParameterTypes();
            if (params.length != 1)
                continue;

            if (type != null && params[0] == Object.class) {
                // we found a generic object setter; now see if the name
                // is an exact match, and if so record this setter.  if we
                // don't find an exact type match later, we'll use it.  if
                // the names are not an exact match, only record this setter
                // if we haven't found any others that match at all
                if (methods[i].getName().equals(setName)) {
                    eqName = true;
                    method = methods[i];
                } else if (method == null)
                    method = methods[i];
            } else
            if (type == null || Filters.canConvert(type, params[0], true)) {
                // we found a setter with the right type; now see if the name
                // is an exact match.  if so, return the setter.  if not,
                // record the setter only if we haven't found a generic one
                // with an exact name match
                if (methods[i].getName().equals(setName))
                    return methods[i];
                if (method == null || !eqName)
                    method = methods[i];
            }
        }

        // if we have an exact method name match, return it; otherwise favor
        // an inexact field to an inexact method
        if (eqName || field == null)
            return method;
        return field;
    }

    /**
     * Return the put method if one exists.
     */
    private static Method findPut(Method[] methods) {
        Class<?>[] params;
        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].getName().equals("put"))
                continue;

            params = methods[i].getParameterTypes();
            if (params.length == 2
                && params[0] == Object.class
                && params[1] == Object.class)
                return methods[i];
        }
		return null;
	}
}
