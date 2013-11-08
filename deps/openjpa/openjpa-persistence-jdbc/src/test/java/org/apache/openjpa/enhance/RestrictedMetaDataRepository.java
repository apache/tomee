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
package org.apache.openjpa.enhance;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.openjpa.meta.MetaDataRepository;

/**
 * A simple metadata repository wrapper which removes selected types
 * from the set of names returned by getPersistentTypeNames. 
 * This MDR provides a simple method to skip extraneous and more important, 
 * purposefully erroneous classes during enhancement.  This 
 * especially useful in the case where all pu's are enhanced generically,
 * automatically picking up all entities in the classpath.
 */
@SuppressWarnings("serial")
public class RestrictedMetaDataRepository extends MetaDataRepository {

    private Set<String> _excludedTypes = new HashSet<String>();
    
    public String getExcludedTypes() {
        return _excludedTypes.toString();
    }
    
    public void setExcludedTypes(String types) {
        StringTokenizer strTok = new StringTokenizer(types,",");
        while (strTok.hasMoreTokens()) {
            _excludedTypes.add(strTok.nextToken());
        }
    }
    
    public Set<String> getPersistentTypeNames(boolean devpath, ClassLoader envLoader) {
        Set<String> types = super.getPersistentTypeNames(devpath, envLoader);
        String[] typeArray = types.toArray(new String[types.size()]);
        for (String type : typeArray) {
            if(_excludedTypes.contains(type)) {
                types.remove(type);
            }
        }
        return types;
    }
}
