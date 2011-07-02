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
package org.apache.openejb.jee;

public enum JavaeeSchema {
    
    APPLICATION_6("application_6.xsd"),
    APPLICATION_CLIENT_6("application-client_6.xsd"),
    WEB_APP_3_0("web-app_3_0.xsd"),
    EJB_JAR_3_1("ejb-jar_3_1.xsd"),
    CONNECTOR_1_6("connector_1_6.xsd");
    
    private final String schemaFileName;
    
    JavaeeSchema(String schemaFileName){
        this.schemaFileName = schemaFileName;
    }
    
    public String getSchemaFileName() {
        return schemaFileName;
    }
}
