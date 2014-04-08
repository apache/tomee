/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb;

public interface JndiConstants {
    public static final String JAVA_OPENEJB_NAMING_CONTEXT = "openejb/";
    public static final String OPENEJB_RESOURCE_JNDI_PREFIX = JAVA_OPENEJB_NAMING_CONTEXT + "Resource/";
    public static final String PERSISTENCE_UNIT_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "PersistenceUnit/";
    public static final String VALIDATOR_FACTORY_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "ValidatorFactory/";
    public static final String VALIDATOR_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "Validator/";
}
