/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.datacache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ClassMetaData;

import serp.util.Strings;

/**
 * A cache distribution policy based on the type of the managed objects.
 * <br>
 * The policy is configured by specifying list of included or excluded types.
 * The lists are specified as fully-qualified persistence class names separated by semicolon.
 * <br>
 * The policy checks for the given instance by its type whether the class name appears in
 * exclusion or inclusion lists. If the class name appears in exclusion list then the
 * instance is not cached. Otherwise, if an inclusion list exists and the class name appears in inclusion list 
 * or @DataCache annotation is specified on the class meta data, then the instance is cached.
 *  
 * @author Pinaki Poddar
 *
 */
public class TypeBasedCacheDistributionPolicy extends DefaultCacheDistributionPolicy 
    implements CacheDistributionPolicy {
    private Set<String> _excludedTypes;
    private Set<String> _includedTypes;
    
    
    /**
     * Gets the excluded types, if configured.
     */
    public Set<String> getExcludedTypes() {
        return _excludedTypes;
    }
    
    /**
     * Sets excluded types from a semicolon separated list of type names.
     */
    public void setExcludedTypes(String types) {
        _excludedTypes = parseNames(types);
    }

    /**
     * Gets the included types, if configured.
     */
    public Set<String> getIncludedTypes() {
        return _includedTypes;
    }
    
    /**
     * Sets included types from a semicolon separated list of type names.
     */
    public void setIncludedTypes(String types) {
        _includedTypes = parseNames(types);
    }
    
    private Set<String> parseNames(String types) {
        if (StringUtils.isEmpty(types))
            return Collections.emptySet();
        String[] names = Strings.split(types, ";", 0);
        Set<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(names));
        
        return  Collections.unmodifiableSet(set);
    }

    @Override
    public String selectCache(OpenJPAStateManager sm, Object context) {
        ClassMetaData meta = sm.getMetaData();
        String className = meta.getDescribedType().getName();
        if (_excludedTypes != null && _excludedTypes.contains(className)) {  
            return null;
        } 
        if (_includedTypes != null && !_includedTypes.isEmpty()) {
            if (_includedTypes.contains(className))
                return meta.getDataCacheName();
            return (meta.getDataCacheEnabled()) ? meta.getDataCacheName() : null;
                
        } else {
            return super.selectCache(sm, context);
        }
    }
}
