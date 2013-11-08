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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.ParameterExpression;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.QueryLanguages;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.persistence.criteria.BindableParameter;

/**
 * An abstract implementation of the Query interface.
 */
public abstract class AbstractQuery<X> implements OpenJPAQuerySPI<X> {
    private static final Localizer _loc = Localizer.forPackage(AbstractQuery.class);

    protected boolean _relaxBindParameterTypeChecking;
    protected boolean _convertPositionalParams;

    // Will be null if this isn't a NamedQuery
    protected final QueryMetaData _qmd;

    protected transient EntityManagerImpl _em;

    protected Map<Parameter<?>, Object> _boundParams;
    protected Map<Object, Parameter<?>> _declaredParams;

    public AbstractQuery(QueryMetaData qmd, EntityManagerImpl em) {
        _qmd = qmd;
        _em = em;

        _boundParams = new HashMap<Parameter<?>, Object>();
    }

    /**
     * Gets a map of values of each parameter indexed by their <em>original</em> key.
     * 
     * @return an empty map if no parameter is declared for this query. The unbound parameters has a value of null which
     *         is indistinguishable from the value being bound to null.
     */
    Map<Object, Object> getParameterValues() {
        Map<Object, Object> result = new HashMap<Object, Object>();
        if (_boundParams == null)
            return result;
        for (Map.Entry<Object, Parameter<?>> entry : getDeclaredParameters().entrySet()) {
            Object paramKey = entry.getKey();
            Parameter<?> param = entry.getValue();
            result.put(paramKey, _boundParams.get(param));
        }
        return result;
    }

    public boolean isNative() {
        return QueryLanguages.LANG_SQL.equals(getLanguage());
    }

    protected abstract void assertOpen();

    protected abstract void lock();

    protected abstract void unlock();

   /**
    * @return a map of parameter name to type for this query.
    */
    protected abstract OrderedMap<Object, Class<?>> getParamTypes();

    // =================================================================================
    // Parameter processing routines
    // =================================================================================

    /**
     * Binds the parameter identified by the given position to the given value. The parameter are bound to a value in
     * the context of this query. The same parameter may be bound to a different value in the context of another 
     * query. <br>
     * For non-native queries, the given position must be a valid position in the declared parameters. <br>
     * As native queries may not be parsed and hence their declared parameters may not be known, setting an positional
     * parameter has the side-effect of a positional parameter being declared.
     * 
     * @param position
     *            positive, integer position of the parameter
     * @param value
     *            an assignment compatible value
     * @return the same query instance
     * @throws IllegalArgumentException
     *             if position does not correspond to a positional parameter of the query or if the argument is of
     *             incorrect type
     */
    public OpenJPAQuery<X> setParameter(int pos, Object value) {
        if (_convertPositionalParams == true) {
            return setParameter("_" + String.valueOf(pos), value);
        }

        assertOpen();
        _em.assertNotCloseInvoked();
        lock();
        try {
            if (pos < 1) {
                throw new IllegalArgumentException(_loc.get("illegal-index", pos).getMessage());
            }
            Parameter<?> param = null;
            if (isNative()) {
                param = new ParameterImpl<Object>(pos, Object.class);
                declareParameter(pos, param);
            } else {
                param = getParameter(pos);
            }
            bindValue(param, value);

            return this;
        } finally {
            unlock();
        }
    }

    /**
     * Sets the value of the given positional parameter after conversion of the given value to the given Temporal Type.
     */
    public OpenJPAQuery<X> setParameter(int position, Calendar value, TemporalType t) {
        return setParameter(position, convertTemporalType(value, t));
    }

    /**
     * Sets the value of the given named parameter after conversion of the given value to the given Temporal Type.
     */
    public OpenJPAQuery<X> setParameter(int position, Date value, TemporalType type) {
        return setParameter(position, convertTemporalType(value, type));
    }

    /**
     * Converts the given Date to a value corresponding to given temporal type.
     */
    Object convertTemporalType(Date value, TemporalType type) {
        switch (type) {
        case DATE:
            return value;
        case TIME:
            return new Time(value.getTime());
        case TIMESTAMP:
            return new Timestamp(value.getTime());
        default:
            return null;
        }
    }

    Object convertTemporalType(Calendar value, TemporalType type) {
        return convertTemporalType(value.getTime(), type);
    }

    /**
     * Affirms if declared parameters use position identifier.
     */
    public boolean hasPositionalParameters() {
        return !getDeclaredParameterKeys(Integer.class).isEmpty();
    }

    /**
     * Gets the array of positional parameter values. The n-th array element represents (n+1)-th positional parameter.
     * If a parameter has been declared but not bound to a value then the value is null and hence is indistinguishable
     * from the value being actually null. If the parameter indexing is not contiguous then the unspecified parameters
     * are considered as null.
     */
    public Object[] getPositionalParameters() {
        lock();
        try {
            Set<Integer> positionalKeys = getDeclaredParameterKeys(Integer.class);
            Object[] result = new Object[calculateMaxKey(positionalKeys)];
            for (Integer pos : positionalKeys) {
                Parameter<?> param = getParameter(pos);
                result[pos.intValue() - 1] = isBound(param) ? getParameterValue(pos) : null;
            }
            return result;
        } finally {
            unlock();
        }
    }

    /**
     * Calculate the maximum value of the given set.
     */
    int calculateMaxKey(Set<Integer> p) {
        if (p == null)
            return 0;
        int max = Integer.MIN_VALUE;
        for (Integer i : p)
            max = Math.max(max, i);
        return max;
    }

    /**
     * Binds the given values as positional parameters. The n-th array element value is set to a Parameter with (n+1)-th
     * positional identifier.
     */
    public OpenJPAQuery<X> setParameters(Object... params) {
        assertOpen();
        _em.assertNotCloseInvoked();
        lock();
        try {
            clearBinding();
            for (int i = 0; params != null && i < params.length; i++) {
                setParameter(i + 1, params[i]);
            }
            return this;
        } finally {
            unlock();
        }
    }

    void clearBinding() {
        if (_boundParams != null)
            _boundParams.clear();
    }

    /**
     * Gets the value of all the named parameters.
     * 
     * If a parameter has been declared but not bound to a value then the value is null and hence is indistinguishable
     * from the value being actually null.
     */
    public Map<String, Object> getNamedParameters() {
        lock();
        try {
            Map<String, Object> result = new HashMap<String, Object>();
            Set<String> namedKeys = getDeclaredParameterKeys(String.class);
            for (String name : namedKeys) {
                Parameter<?> param = getParameter(name);
                result.put(name, isBound(param) ? getParameterValue(name) : null);
            }
            return result;
        } finally {
            unlock();
        }
    }

    /**
     * Sets the values of the parameters from the given Map. The keys of the given map designate the name of the
     * declared parameter.
     */
    public OpenJPAQuery<X> setParameters(Map params) {
        assertOpen();
        _em.assertNotCloseInvoked();
        lock();
        try {
            clearBinding();
            if (params != null)
                for (Map.Entry e : (Set<Map.Entry>) params.entrySet())
                    setParameter((String) e.getKey(), e.getValue());
            return this;
        } finally {
            unlock();
        }
    }

    /**
     * Get the parameter of the given name and type.
     * 
     * @throws IllegalArgumentException
     *             if the parameter of the specified name does not exist or is not assignable to the type
     * @throws IllegalStateException
     *             if invoked on a native query
     */
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        Parameter<?> param = getParameter(name);
        if (param.getParameterType().isAssignableFrom(type))
            throw new IllegalArgumentException(param + " does not match the requested type " + type);
        return (Parameter<T>) param;
    }

    /**
     * Get the positional parameter with the given position and type.
     * 
     * @throws IllegalArgumentException
     *             if the parameter with the specified position does not exist or is not assignable to the type
     * @throws IllegalStateException
     *             if invoked on a native query unless the same parameter position is bound already.
     */
    public <T> Parameter<T> getParameter(int pos, Class<T> type) {
        if (_convertPositionalParams == true) {
            return getParameter("_" + String.valueOf(pos), type);
        }
        Parameter<?> param = getParameter(pos);
        if (param.getParameterType().isAssignableFrom(type))
            throw new IllegalArgumentException(param + " does not match the requested type " + type);
        return (Parameter<T>) param;
    }

    /**
     * Return the value bound to the parameter.
     * 
     * @param param
     *            parameter object
     * @return parameter value
     * @throws IllegalStateException
     *             if the parameter has not been been bound
     * @throws IllegalArgumentException
     *             if the parameter does not belong to this query
     */
    public <T> T getParameterValue(Parameter<T> p) {
        if (!isBound(p)) {
            throw new IllegalArgumentException(_loc.get("param-missing", p, getQueryString(), getBoundParameterKeys())
                .getMessage());
        }
        return (T) _boundParams.get(p);
    }

    /**
     * Gets the parameters declared in this query.
     */
    public Set<Parameter<?>> getParameters() {
        Set<Parameter<?>> result = new HashSet<Parameter<?>>();
        result.addAll(getDeclaredParameters().values());
        return result;
    }

    public <T> OpenJPAQuery<X> setParameter(Parameter<T> p, T arg1) {
        bindValue(p, arg1);
        if (BindableParameter.class.isInstance(p)) {
            BindableParameter.class.cast(p).setValue(arg1);
        }
        return this;
    }

    public OpenJPAQuery<X> setParameter(Parameter<Date> p, Date date, TemporalType type) {
        return setParameter(p, (Date) convertTemporalType(date, type));
    }

    public TypedQuery<X> setParameter(Parameter<Calendar> p, Calendar cal, TemporalType type) {
        return setParameter(p, (Calendar) convertTemporalType(cal, type));
    }

    /**
     * Get the parameter object corresponding to the declared parameter of the given name. This method is not required
     * to be supported for native queries.
     * 
     * @throws IllegalArgumentException
     *             if the parameter of the specified name does not exist
     * @throws IllegalStateException
     *             if invoked on a native query
     */
    public Parameter<?> getParameter(String name) {
        if (isNative()) {
            throw new IllegalStateException(_loc.get("param-named-non-native", name).getMessage());
        }
        Parameter<?> param = getDeclaredParameters().get(name);
        if (param == null) {
            Set<ParameterExpression> exps = getDeclaredParameterKeys(ParameterExpression.class);
            for (ParameterExpression<?> e : exps) {
                if (name.equals(e.getName()))
                    return e;
            }
            throw new IllegalArgumentException(_loc.get("param-missing-name", name, getQueryString(),
                getDeclaredParameterKeys()).getMessage());
        }
        return param;
    }

    /**
     * Get the positional parameter with the given position. The parameter may just have been declared and not bound to
     * a value.
     * 
     * @param position
     *            specified in the user query.
     * @return parameter object
     * @throws IllegalArgumentException
     *             if the parameter with the given position does not exist
     */
    public Parameter<?> getParameter(int pos) {
        if (_convertPositionalParams == true) {
            return getParameter("_" + String.valueOf(pos));
        }
        Parameter<?> param = getDeclaredParameters().get(pos);
        if (param == null)
            throw new IllegalArgumentException(_loc.get("param-missing-pos", pos, getQueryString(),
                getDeclaredParameterKeys()).getMessage());
        return param;
    }

    /**
     * Return the value bound to the parameter.
     * 
     * @param name
     *            name of the parameter
     * @return parameter value
     * 
     * @throws IllegalStateException
     *             if this parameter has not been bound
     */
    public Object getParameterValue(String name) {
        return _boundParams.get(getParameter(name));
    }

    /**
     * Return the value bound to the parameter.
     * 
     * @param pos
     *            position of the parameter
     * @return parameter value
     * 
     * @throws IllegalStateException
     *             if this parameter has not been bound
     */
    public Object getParameterValue(int pos) {
        Parameter<?> param = getParameter(pos);
        assertBound(param);
        return _boundParams.get(param);
    }

    /**
     * Gets the parameter keys bound with this query. Parameter key can be Integer, String or a ParameterExpression
     * itself but all parameters keys of a particular query are of the same type.
     */
    public Set<?> getBoundParameterKeys() {
        if (_boundParams == null)
            return Collections.EMPTY_SET;
        getDeclaredParameters();
        Set<Object> result = new HashSet<Object>();
        for (Map.Entry<Object, Parameter<?>> entry : _declaredParams.entrySet()) {
            if (isBound(entry.getValue())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Gets the declared parameter keys in the given query. This information is only available after the query has been
     * parsed. As native language queries are not parsed, this information is not available for them.
     * 
     * @return set of parameter identifiers in a parsed query
     */
    public Set<?> getDeclaredParameterKeys() {
        return getDeclaredParameters().keySet();
    }

    public <T> Set<T> getDeclaredParameterKeys(Class<T> keyType) {
        Set<T> result = new HashSet<T>();
        for (Object key : getDeclaredParameterKeys()) {
            if (keyType.isInstance(key))
                result.add((T) key);
        }
        return result;
    }

    /**
     * Gets the parameter instances declared in this query. All parameter keys are of the same type. It is not allowed
     * to mix keys of different type such as named and positional keys.
     * 
     * For string-based queries, the parser supplies the information about the declared parameters as a LinkedMap of
     * expected parameter value type indexed by parameter identifier. For non string-based queries that a facade itself
     * may construct (e.g. CriteriaQuery), the parameters must be declared by the caller. This receiver constructs
     * concrete Parameter instances from the given parameter identifiers.
     * 
     * @return a Map where the key represents the original identifier of the parameter (can be a String, Integer or a
     *         ParameterExpression itself) and the value is the concrete Parameter instance either constructed as a
     *         result of this call or supplied by declaring the parameter explicitly via
     *         {@linkplain #declareParameter(Parameter)}.
     */
    public Map<Object, Parameter<?>> getDeclaredParameters() {
        if (_declaredParams == null) {
            _declaredParams = new HashMap<Object, Parameter<?>>();

            OrderedMap<Object, Class<?>> paramTypes = null;
            // Check to see if we have a cached version of the paramTypes in QueryMetaData.
            if (_qmd != null) {
                paramTypes = _qmd.getParamTypes();
            }
            if (paramTypes == null) {
                paramTypes = getParamTypes();
                // Cache the param types as they haven't been set yet.
                if (_qmd != null) {
                    _qmd.setParamTypes(paramTypes);
                }
            }
            for (Entry<Object, Class<?>> entry : paramTypes.entrySet()) {
                Object key = entry.getKey();
                Class<?> expectedValueType = entry.getValue();
                Parameter<?> param;

                if (key instanceof Integer) {
                    param = new ParameterImpl((Integer) key, expectedValueType);
                } else if (key instanceof String) {
                    param = new ParameterImpl((String) key, expectedValueType);
                } else if (key instanceof Parameter) {
                    param = (Parameter<?>) key;
                } else {
                    throw new IllegalArgumentException("parameter identifier " + key + " unrecognized");
                }
                declareParameter(key, param);
            }
        }
        return _declaredParams;
    }

    /**
     * Declares the given parameter for this query. Used by non-string based queries that are constructed by the facade
     * itself rather than OpenJPA parsing the query to detect the declared parameters.
     * 
     * @param key
     *            this is the key to identify the parameter later in the context of this query. Valid key types are
     *            Integer, String or ParameterExpression itself.
     * @param the
     *            parameter instance to be declared
     */
    public void declareParameter(Object key, Parameter<?> param) {
        if (_declaredParams == null) {
            _declaredParams = new HashMap<Object, Parameter<?>>();
        }
        _declaredParams.put(key, param);
    }

    /**
     * Affirms if the given parameter is bound to a value for this query.
     */
    public boolean isBound(Parameter<?> param) {
        return _boundParams != null && _boundParams.containsKey(param);
    }

    void assertBound(Parameter<?> param) {
        if (!isBound(param)) {
            throw new IllegalStateException(_loc.get("param-not-bound", param, getQueryString(),
                getBoundParameterKeys()).getMessage());
        }
    }

    /**
     * Binds the given value to the given parameter. Validates if the parameter can accept the value by its type.
     */
    void bindValue(Parameter<?> param, Object value) {
        Object bindVal = assertValueAssignable(param, value);
        _boundParams.put(param, bindVal);
    }

    public OpenJPAQuery<X> setParameter(String name, Calendar value, TemporalType type) {
        return setParameter(name, convertTemporalType(value, type));
    }

    public OpenJPAQuery<X> setParameter(String name, Date value, TemporalType type) {
        return setParameter(name, convertTemporalType(value, type));
    }

    /**
     * Sets the parameter of the given name to the given value.
     */
    public OpenJPAQuery<X> setParameter(String name, Object value) {
        assertOpen();
        _em.assertNotCloseInvoked();
        lock();
        try {
            // native queries can not have named parameters
            if (isNative()) {
                throw new IllegalArgumentException(_loc.get("no-named-params", name, getQueryString()).toString());
            } else {
                bindValue(getParameter(name), value);
            }

            return this;
        } finally {
            unlock();
        }
    }

    /**
     * Convert the given value to match the given parameter type, if possible.
     * 
     * @param param
     *            a query parameter
     * @param v
     *            a user-supplied value for the parameter
     */
    Object assertValueAssignable(Parameter<?> param, Object v) {
        Class<?> expectedType = param.getParameterType();
        if (v == null) {
            if (expectedType.isPrimitive())
                throw new IllegalArgumentException(_loc.get("param-null-primitive", param).getMessage());
            return v;
        }
        if (getRelaxBindParameterTypeChecking()) {
            try {
                return Filters.convert(v, expectedType);
            } catch (Exception e) {
                throw new IllegalArgumentException(_loc.get("param-type-mismatch",
                    new Object[] { param, getQueryString(), v, v.getClass().getName(), expectedType.getName() })
                    .getMessage());
            }
        } else {
            if (!Filters.canConvert(v.getClass(), expectedType, true)) {
                throw new IllegalArgumentException(_loc.get("param-type-mismatch",
                    new Object[] { param, getQueryString(), v, v.getClass().getName(), expectedType.getName() })
                    .getMessage());
            } else {
                return v;
            }
        }
    }

    // ================== End of Parameter Processing routines ================================

    @Override
    public boolean getRelaxBindParameterTypeChecking() {
        return _relaxBindParameterTypeChecking;
    }

    public void setRelaxBindParameterTypeChecking(Object value) {
        if (value != null) {
            if (value instanceof String) {
                _relaxBindParameterTypeChecking = "true".equalsIgnoreCase(value.toString());
            } else if (value instanceof Boolean) {
                _relaxBindParameterTypeChecking = ((Boolean) value).booleanValue();
            }
        }
    }
}
