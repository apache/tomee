/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.registry;

//START SNIPPET: code
import static javax.ejb.ConcurrencyManagementType.BEAN;
import javax.ejb.Singleton;
import javax.ejb.ConcurrencyManagement;
import java.util.Properties;

@Singleton
@ConcurrencyManagement(BEAN)
public class PropertyRegistryBean implements PropertyRegistry {

    private final Properties properties = new Properties();

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String setProperty(String key, String value) {
        return (String) properties.setProperty(key, value);
    }

    public String removeProperty(String key) {
        return (String) properties.remove(key);
    }

}
//END SNIPPET: code
