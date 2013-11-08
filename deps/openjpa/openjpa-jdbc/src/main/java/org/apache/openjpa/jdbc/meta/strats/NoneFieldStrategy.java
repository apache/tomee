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
package org.apache.openjpa.jdbc.meta.strats;

import org.apache.openjpa.jdbc.meta.FieldMapping;

/**
 * An unmapped field.
 *
 * @author Abe White
 */
public class NoneFieldStrategy
    extends AbstractFieldStrategy {

    public static final String ALIAS = "none";

    private static final NoneFieldStrategy _instance = new NoneFieldStrategy();

    /**
     * Return the singleton instance.
     */
    public static NoneFieldStrategy getInstance() {
        return _instance;
    }

    /**
     * Hide constructor.
     */
    private NoneFieldStrategy() {
    }

    public String getAlias() {
        return ALIAS;
    }

    public void setFieldMapping(FieldMapping owner) {
    }
}
