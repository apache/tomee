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
package org.apache.openjpa.conf;

import org.apache.openjpa.lib.conf.ProductDerivation;
import java.util.Map;

/**
 * Adds datastore based extension to ProductDerivation.  
 *
 * @since 0.4.1
 * @author Pinaki Poddar
 */
public interface OpenJPAProductDerivation extends ProductDerivation {

    public static final int TYPE_SPEC = 0;
    public static final int TYPE_STORE = 200;
    public static final int TYPE_SPEC_STORE = 300;
    public static final int TYPE_PRODUCT_STORE = 400;

    /**
     * Load default alias options into the given map.  Each entry maps an
     * alias to a broker factory class name.  Replace anything previously 
     * under the desired keys.
     */
    public void putBrokerFactoryAliases(Map<String, String> aliases);
}
