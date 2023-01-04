/*
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
package org.apache.tomee.installer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstallerTest {

    @Test
    public void normalizeVersion() {

        assertVersion("openejb-api-8.0.7-SNAPSHOT.jar", "openejb-api-8.0.7-20210418.032427-163.jar");
        assertVersion("openejb-client-8.0.7-SNAPSHOT.jar", "openejb-client-8.0.7-20210418.032621-163.jar");
        assertVersion("openejb-core-8.0.7-SNAPSHOT.jar", "openejb-core-8.0.7-20210418.032600-163.jar");
        assertVersion("openejb-core-eclipselink-8.0.7-SNAPSHOT.jar", "openejb-core-eclipselink-8.0.7-20210418.033130-163.jar");
        assertVersion("openejb-cxf-8.0.7-SNAPSHOT.jar", "openejb-cxf-8.0.7-20210418.032810-163.jar");
        assertVersion("openejb-cxf-rs-8.0.7-SNAPSHOT.jar", "openejb-cxf-rs-8.0.7-20210418.032736-163.jar");
        assertVersion("openejb-cxf-transport-8.0.7-SNAPSHOT.jar", "openejb-cxf-transport-8.0.7-20210418.032721-163.jar");
        assertVersion("openejb-derbynet-8.0.7-SNAPSHOT.jar", "openejb-derbynet-8.0.7-SNAPSHOT.jar");
        assertVersion("openejb-ejbd-8.0.7-SNAPSHOT.jar", "openejb-ejbd-8.0.7-20210418.032656-163.jar");
        assertVersion("openejb-hsql-8.0.7-SNAPSHOT.jar", "openejb-hsql-8.0.7-20210418.032814-163.jar");
        assertVersion("openejb-http-8.0.7-SNAPSHOT.jar", "openejb-http-8.0.7-20210418.032703-163.jar");
        assertVersion("openejb-javaagent-8.0.7-SNAPSHOT.jar", "openejb-javaagent-8.0.7-20210418.032436-163.jar");
        assertVersion("openejb-javaagent.jar", "openejb-javaagent.jar");
        assertVersion("openejb-jee-8.0.7-SNAPSHOT.jar", "openejb-jee-8.0.7-20210418.032447-163.jar");
        assertVersion("openejb-jee-accessors-8.0.7-SNAPSHOT.jar", "openejb-jee-accessors-8.0.7-20210418.032458-163.jar");
        assertVersion("openejb-jpa-integration-8.0.7-SNAPSHOT.jar", "openejb-jpa-integration-8.0.7-20210418.032423-164.jar");
        assertVersion("openejb-loader-8.0.7-SNAPSHOT.jar", "openejb-loader-8.0.7-20210418.032432-163.jar");
        assertVersion("openejb-rest-8.0.7-SNAPSHOT.jar", "openejb-rest-8.0.7-20210418.032713-163.jar");
        assertVersion("openejb-server-8.0.7-SNAPSHOT.jar", "openejb-server-8.0.7-20210418.032647-163.jar");
        assertVersion("openejb-webservices-8.0.7-SNAPSHOT.jar", "openejb-webservices-8.0.7-20210418.032756-163.jar");
        assertVersion("tomee-catalina-8.0.7-SNAPSHOT.jar", "tomee-catalina-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-common-8.0.7-SNAPSHOT.jar", "tomee-common-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-config-8.0.7-SNAPSHOT.jar", "tomee-config-8.0.7-20210418.032613-163.jar");
        assertVersion("tomee-jaxrs-8.0.7-SNAPSHOT.jar", "tomee-jaxrs-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-jdbc-8.0.7-SNAPSHOT.jar", "tomee-jdbc-8.0.7-20210418.032642-163.jar");
        assertVersion("tomee-loader-8.0.7-SNAPSHOT.jar", "tomee-loader-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-mojarra-8.0.7-SNAPSHOT.jar", "tomee-mojarra-8.0.7-20210418.032823-163.jar");
        assertVersion("tomee-plume-webapp-9.0.0-M7-SNAPSHOT.jar", "tomee-plume-webapp-9.0.0-M7-SNAPSHOT.jar");
        assertVersion("tomee-security-8.0.7-SNAPSHOT.jar", "tomee-security-8.0.7-20210418.033034-163.jar");
        assertVersion("tomee-webservices-8.0.7-SNAPSHOT.jar", "tomee-webservices-8.0.7-20210418.032838-163.jar");

    }

    @Test
    public void normalizeOddVersion() {

        assertVersion("openejb-api-8.0.7-SNAPSHOT.jar", "openejb-api-8.0.7-20210418.032427-163.jar");
        assertVersion("openejb-client-8.0.7-SNAPSHOT.jar", "openejb-client-8.0.7-20210418.032621-163.jar");
        assertVersion("openejb-core-8.0.7-SNAPSHOT.jar", "openejb-core-8.0.7-20210418.032600-163.jar");
        assertVersion("openejb-core-eclipselink-8.0.7-SNAPSHOT.jar", "openejb-core-eclipselink-8.0.7-20210418.033130-163.jar");
        assertVersion("openejb-cxf-8.0.7-SNAPSHOT.jar", "openejb-cxf-8.0.7-20210418.032810-163.jar");
        assertVersion("openejb-cxf-rs-8.0.7-SNAPSHOT.jar", "openejb-cxf-rs-8.0.7-20210418.032736-163.jar");
        assertVersion("openejb-cxf-transport-8.0.7-SNAPSHOT.jar", "openejb-cxf-transport-8.0.7-20210418.032721-163.jar");
        assertVersion("openejb-derbynet-8.0.7-SNAPSHOT.jar", "openejb-derbynet-8.0.7-SNAPSHOT.jar");
        assertVersion("openejb-ejbd-8.0.7-SNAPSHOT.jar", "openejb-ejbd-8.0.7-20210418.032656-163.jar");
        assertVersion("openejb-hsql-8.0.7-SNAPSHOT.jar", "openejb-hsql-8.0.7-20210418.032814-163.jar");
        assertVersion("openejb-http-8.0.7-SNAPSHOT.jar", "openejb-http-8.0.7-20210418.032703-163.jar");
        assertVersion("openejb-javaagent-8.0.7-SNAPSHOT.jar", "openejb-javaagent-8.0.7-20210418.032436-163.jar");
        assertVersion("openejb-javaagent.jar", "openejb-javaagent.jar");
        assertVersion("openejb-jee-8.0.7-SNAPSHOT.jar", "openejb-jee-8.0.7-20210418.032447-163.jar");
        assertVersion("openejb-jee-accessors-8.0.7-SNAPSHOT.jar", "openejb-jee-accessors-8.0.7-20210418.032458-163.jar");
        assertVersion("openejb-jpa-integration-8.0.7-SNAPSHOT.jar", "openejb-jpa-integration-8.0.7-20210418.032423-164.jar");
        assertVersion("openejb-loader-8.0.7-SNAPSHOT.jar", "openejb-loader-8.0.7-20210418.032432-163.jar");
        assertVersion("openejb-rest-8.0.7-SNAPSHOT.jar", "openejb-rest-8.0.7-20210418.032713-163.jar");
        assertVersion("openejb-server-8.0.7-SNAPSHOT.jar", "openejb-server-8.0.7-20210418.032647-163.jar");
        assertVersion("openejb-webservices-8.0.7-SNAPSHOT.jar", "openejb-webservices-8.0.7-20210418.032756-163.jar");
        assertVersion("tomee-catalina-8.0.7-SNAPSHOT.jar", "tomee-catalina-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-common-8.0.7-SNAPSHOT.jar", "tomee-common-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-config-8.0.7-SNAPSHOT.jar", "tomee-config-8.0.7-20210418.032613-163.jar");
        assertVersion("tomee-jaxrs-8.0.7-SNAPSHOT.jar", "tomee-jaxrs-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-jdbc-8.0.7-SNAPSHOT.jar", "tomee-jdbc-8.0.7-20210418.032642-163.jar");
        assertVersion("tomee-loader-8.0.7-SNAPSHOT.jar", "tomee-loader-8.0.7-SNAPSHOT.jar");
        assertVersion("tomee-mojarra-8.0.7-SNAPSHOT.jar", "tomee-mojarra-8.0.7-20210418.032823-163.jar");
        assertVersion("tomee-plume-webapp-9.0.0-M7-SNAPSHOT.jar", "tomee-plume-webapp-9.0.0-M7-SNAPSHOT.jar");
        assertVersion("tomee-security-8.0.7-SNAPSHOT.jar", "tomee-security-8.0.7-20210418.033034-163.jar");
        assertVersion("tomee-webservices-8.0.7-SNAPSHOT.jar", "tomee-webservices-8.0.7-20210418.032838-163.jar");

    }

    private static void assertVersion(final String expected, final String jarName) {
        final String s = Installer.removeDatestamp("8.0.7-SNAPSHOT", jarName);
        assertEquals(expected, s);
    }
}
