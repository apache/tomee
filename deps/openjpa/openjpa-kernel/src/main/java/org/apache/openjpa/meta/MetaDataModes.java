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
package org.apache.openjpa.meta;

/**
 * Mode constants used to track the initialization status of metadata.
 * These constants can be used as bit flags.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface MetaDataModes {

    public static final int MODE_NONE = 0;
    public static final int MODE_META = 1;
    public static final int MODE_MAPPING = 2;
    public static final int MODE_QUERY = 4;
    public static final int MODE_MAPPING_INIT = 8;
    public static final int MODE_ANN_MAPPING = 16;

    public static final int MODE_ALL = MODE_META | MODE_MAPPING | MODE_QUERY 
        | MODE_MAPPING_INIT | MODE_ANN_MAPPING;
}
