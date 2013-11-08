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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.lib.conf.ProductDerivation;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.StringDistance;

/**
 * Manages query hint keys and handles their values on behalf of a owning
 * {@link QueryImpl}. Uses specific knowledge of hint keys declared in
 * different parts of the system.
 * 
 * This receiver collects hint keys from different parts of the system. The
 * keys are implicitly or explicitly declared by several different mechanics.
 * This receiver sets the values on behalf of a owning {@link QueryImpl}
 * based on the its specific knowledge of these keys.
 * 
 * The hint keys from following sources are collected and handled: 
 * 
 * 1. {@link org.apache.openjpa.kernel.QueryHints} interface declares hint keys
 *    as public static final fields. These fields are collected by reflection.
 *    The values are handled by invoking methods on the owning {@link QueryImpl}
 *    
 * 2. Some hint keys are collected from bean-style property names of {@link 
 *    JDBCFetchPlan} by {@link Reflection#getBeanStylePropertyNames(Class) 
 *    reflection} and prefixed with <code>openjpa.FetchPlan</code>. 
 *    Their values are used to set the corresponding property of {@link 
 *    FetchPlan} via {@link #hintToSetter(FetchPlan, String, Object) reflection}
 *      
 * 3. Currently defined <code>javax.persistence.*</code> hint keys have  
 *    a equivalent counterpart to one of these FetchPlan keys. 
 *    The JPA keys are mapped to equivalent FetchPlan hint keys.
 *    
 * 4. Some keys directly invoke setters or add listeners to the owning 
 *    {@link QueryImpl}. These hint keys are statically declared in 
 *    this receiver itself. 
 *    
 * 5. ProductDerivation may introduce their own query hint keys via {@link 
 *    ProductDerivation#getSupportedQueryHints()}. Their values are set in the 
 *    {@link FetchConfiguration#setHint(String, Object)}
 *     
 *  A hint key is classified into one of the following three categories:
 *  
 *  1. Supported: A key is known to this receiver as collected from different 
 *     parts of the system. The value of a supported key is recorded and 
 *     available via {@link #getHints()} method. 
 *  2. Recognized: A key is not known to this receiver but has a prefix which
 *     is known to this receiver. The value of a recognized key is not recorded 
 *     but its value is available via {@link FetchConfiguration#getHint(String)}
 *  3. Unrecognized: A key is neither supported nor recognized. The value of a 
 *     unrecognized key is neither recorded nor set anywhere.
 *  
 *  If an incompatible value is supplied for a supported key, a non-fatal
 *  {@link ArgumentException} is raised.
 *  
 * @author Pinaki Poddar
 *
 * @since 2.0.0
 * 
 * @nojavadoc
 */
public class HintHandler  {
  protected final QueryImpl<?> owner;

    private static final Localizer _loc = Localizer.forPackage(HintHandler.class);
    protected static Set<String> _supportedHints = ProductDerivations.getSupportedQueryHints();

    protected static final String PREFIX_OPENJPA = "openjpa.";
    protected static final String PREFIX_JDBC = PREFIX_OPENJPA + "jdbc.";
    protected static final String PREFIX_FETCHPLAN = PREFIX_OPENJPA + "FetchPlan.";
    private Map<String, Object> _hints;

    
    HintHandler(QueryImpl<?> impl) {
        super();
        owner = impl;
    }
    
    /**
     * Record a key-value pair only only if the given key is supported.
     * 
     * @return FALSE if the key is unrecognized. 
     *         null (i.e. MAY BE) if the key is recognized, but not supported.
     *         TRUE if the key is supported.
     */
    protected Boolean record(String hint, Object value) {
        if (hint == null)
            return Boolean.FALSE;
        if (_supportedHints.contains(hint)) {
            if (_hints == null)
                _hints = new TreeMap<String, Object>();
            _hints.put(hint, value);
            return Boolean.TRUE;
        }
        if (isKnownPrefix(hint)) {
            Log log = owner.getDelegate().getBroker().getConfiguration().getLog(OpenJPAConfiguration.LOG_RUNTIME);
            String possible = StringDistance.getClosestLevenshteinDistance(hint, getSupportedHints());
            if (log.isWarnEnabled())
                log.warn(_loc.get("bad-query-hint", hint, possible));
            return null; // possible but not registered
        }
        return Boolean.FALSE; // not possible
    }

    public void setHint(String key, Object value) {
        Boolean status = record(key, value);
        if (Boolean.FALSE.equals(status))
            return;
        FetchPlan plan = owner.getFetchPlan();
        if (status == null) {
            plan.setHint(key, value);
            return;
        }
        
        ClassLoader loader = owner.getDelegate().getBroker().getClassLoader();
        if (QueryHints.HINT_SUBCLASSES.equals(key)) {
            if (value instanceof String)
                value = Boolean.valueOf((String) value);
            owner.setSubclasses(((Boolean) value).booleanValue());
        } else if (QueryHints.HINT_RELAX_BIND_PARAM_TYPE_CHECK.equals(key)) {
            owner.setRelaxBindParameterTypeChecking(value);
        } else if (QueryHints.HINT_FILTER_LISTENER.equals(key)) {
            owner.addFilterListener(Filters.hintToFilterListener(value, loader));
        } else if (QueryHints.HINT_FILTER_LISTENERS.equals(key)) {
            FilterListener[] arr = Filters.hintToFilterListeners(value, loader);
            for (int i = 0; i < arr.length; i++)
                owner.addFilterListener(arr[i]);
        } else if (QueryHints.HINT_AGGREGATE_LISTENER.equals(key)) {
            owner.addAggregateListener(Filters.hintToAggregateListener(value, loader));
        } else if (QueryHints.HINT_AGGREGATE_LISTENERS.equals(key)) {
            AggregateListener[] arr = Filters.hintToAggregateListeners(value, loader);
            for (int i = 0; i < arr.length; i++) {
                owner.addAggregateListener(arr[i]);
            }
        } else if (QueryHints.HINT_RESULT_COUNT.equals(key)) {
            int v = (Integer) Filters.convert(value, Integer.class);
            if (v < 0) {
                throw new IllegalArgumentException(_loc.get("bad-query-hint-value", key, value).toString());
            }
            plan.setHint(key, v);
        } else if (QueryHints.HINT_INVALIDATE_PREPARED_QUERY.equals(key)) {
            plan.setHint(key, Filters.convert(value, Boolean.class));
            owner.invalidatePreparedQuery();
        } else if (QueryHints.HINT_IGNORE_PREPARED_QUERY.equals(key)) {
            plan.setHint(key, Filters.convert(value, Boolean.class));
            owner.ignorePreparedQuery();
        } else if (QueryHints.HINT_USE_LITERAL_IN_SQL.equals(key)) {
            Boolean convertedValue = (Boolean)Filters.convert(value, Boolean.class);
            plan.setHint(key, convertedValue);
        } else { // default 
            plan.setHint(key, value);
        }
    }
    
    /**
     * Affirms if the given key starts with any of the known prefix.
     * @param key
     * @return
     */
    protected boolean isKnownPrefix(String key) {
        if (key == null)
            return false;
        for (String prefix : ProductDerivations.getConfigurationPrefixes()) {
            if (key.startsWith(prefix))
                return true;
        }
        return false;
    }

    
    
//    protected boolean hasPrecedent(String key) {
//        boolean hasPrecedent = true;
//        String[] list = precedenceMap.get(key);
//        if (list != null) {
//            for (String hint : list) {
//                if (hint.equals(key))
//                    break;
//                // stop if a higher precedence hint has already defined 
//                if (getHints().containsKey(hint)) {
//                    hasPrecedent = false;
//                    break;
//                }
//            }
//        }
//        return hasPrecedent;
//    }

//    private Integer toLockLevel(Object value) {
//        Object origValue = value;
//        if (value instanceof String) {
//            // to accommodate alias name input in relationship with enum values
//            //  e.g. "optimistic-force-increment" == LockModeType.OPTIMISTIC_FORCE_INCREMENT
//            String strValue = ((String) value).toUpperCase().replace('-', '_');
//            value = Enum.valueOf(LockModeType.class, strValue);
//        }
//        if (value instanceof LockModeType)
//            value = MixedLockLevelsHelper.toLockLevel((LockModeType) value);
//
//        Integer intValue = null;
//        if (value instanceof Integer)
//            intValue = (Integer) value;
//        if (intValue == null
//            || (intValue != MixedLockLevels.LOCK_NONE
//                && intValue != MixedLockLevels.LOCK_READ
//                && intValue != MixedLockLevels.LOCK_OPTIMISTIC
//                && intValue != MixedLockLevels.LOCK_WRITE
//                && intValue != MixedLockLevels.LOCK_OPTIMISTIC_FORCE_INCREMENT
//                && intValue != MixedLockLevels.LOCK_PESSIMISTIC_READ
//                && intValue != MixedLockLevels.LOCK_PESSIMISTIC_WRITE
//                && intValue != MixedLockLevels.LOCK_PESSIMISTIC_FORCE_INCREMENT)
//                )
//            throw new IllegalArgumentException(_loc.get("bad-lock-level", origValue).getMessage());
//        return intValue;
//    }
    
    /**
     * Gets all the supported hint keys. The set of supported hint keys is
     * statically determined by collecting hint keys from the ProductDerivations.
     */
    public Set<String> getSupportedHints() {
        return _supportedHints;
    }
    
    /**
     * Gets all the recorded hint keys and their values.
     */
    public Map<String, Object> getHints() {
        if (_hints == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(_hints);
    }

}

