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
package org.apache.openjpa.lib.conf;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Abstract no-op product derivation for easy extension.
 *
 * @author Pinaki Poddar
 * @since 0.4.1
 */
public abstract class AbstractProductDerivation
    implements ProductDerivation {

    public String getConfigurationPrefix() {
        return null;
    }

    public void validate() 
        throws Exception {
    }

    public ConfigurationProvider loadGlobals(ClassLoader loader)
        throws Exception {
        return null;
    }

    public ConfigurationProvider loadDefaults(ClassLoader loader)
        throws Exception {
        return null;
    }

    public ConfigurationProvider load(String resource, String anchor,
        ClassLoader loader) 
        throws Exception {
        return null;
    }

    public ConfigurationProvider load(File file, String anchor)
        throws Exception {
        return null;
    }

    public String getDefaultResourceLocation() {
        return null;
    }

    public List<String> getAnchorsInFile(File file) throws Exception {
        return null;
    }

    public List<String> getAnchorsInResource(String resource) throws Exception {
        return null;
    }

    public boolean beforeConfigurationConstruct(ConfigurationProvider cp) {
        return false;
    }

    public boolean beforeConfigurationLoad(Configuration conf) {
        return false;
    }

    public boolean afterSpecificationSet(Configuration conf) {
        return false;
    }
    
    public void beforeConfigurationClose(Configuration conf) {
    }
        
    public Set<String> getSupportedQueryHints() {
        return Collections.EMPTY_SET;
    }
}
