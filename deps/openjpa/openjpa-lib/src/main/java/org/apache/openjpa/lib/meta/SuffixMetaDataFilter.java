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
package org.apache.openjpa.lib.meta;

/**
 * Filters metadata iteration based on resource name suffix.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SuffixMetaDataFilter implements MetaDataFilter {

    private final String _suffix;

    /**
     * Constructor; supply suffix to match against.
     */
    public SuffixMetaDataFilter(String suffix) {
        _suffix = suffix;
    }

    public boolean matches(Resource rsrc) {
        String name = rsrc.getName();
        return name != null && name.endsWith(_suffix);
    }
}
