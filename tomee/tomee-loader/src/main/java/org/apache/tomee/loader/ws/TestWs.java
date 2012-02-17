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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.loader.ws;


import org.apache.tomee.loader.dto.TestDTO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Path("/ws/test")
@Produces({"application/json"})
public class TestWs {

    @Path("/test")
    @GET
    public List<TestDTO> get() throws NamingException {
        final List<TestDTO> result = new ArrayList<TestDTO>();

        {
            final String homePath = System.getProperty("openejb.home");
            result.add(createDTO("homeSet", !(homePath == null)));

            final File openejbHome = new File(homePath);
            result.add(createDTO("homeExists", openejbHome.exists()));

            result.add(createDTO("homeDirectory", openejbHome.isDirectory()));

            final File openejbHomeLib;
            if (org.apache.tomee.common.TomcatVersion.v6.isTheVersion()
                    || org.apache.tomee.common.TomcatVersion.v7.isTheVersion()) {
                openejbHomeLib = new File(openejbHome, "lib");
            } else {
                final File common = new File(openejbHome, "common");
                openejbHomeLib = new File(common, "lib");
            }
            result.add(createDTO("libDirectory", openejbHomeLib.exists()));
        }

        {
            ClassLoader myLoader = this.getClass().getClassLoader();

            try {
                Class openejb = Class.forName("org.apache.openejb.OpenEJB", true, myLoader);
                result.add(createDTO("openEjbInstalled", true));

                try {
                    Method isInitialized = openejb.getDeclaredMethod("isInitialized");
                    Boolean running = (Boolean) isInitialized.invoke(openejb);
                    result.add(createDTO("openEjbStarted", running));
                } catch (Exception e) {
                    result.add(createDTO("openEjbStarted", false));
                }
            } catch (Exception e) {
                result.add(createDTO("openEjbInstalled", false));
            }

            try {
                Class.forName("javax.ejb.EJBHome", true, myLoader);
                result.add(createDTO("ejbsInstalled", true));
            } catch (Exception e) {
                result.add(createDTO("ejbsInstalled", false));
            }

            try {
                final Properties properties = new Properties();
                properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
                properties.put("openejb.loader", "embed");

                final InitialContext ctx = new InitialContext(properties);
                Object obj = ctx.lookup("");

                if (obj.getClass().getName().equals("org.apache.openejb.core.ivm.naming.IvmContext")) {
                    result.add(createDTO("testLookup", true));
                } else {
                    result.add(createDTO("testLookup", false));
                }

            } catch (Exception e) {
                result.add(createDTO("testLookup", false));
            }
        }


        return result;
    }

    private TestDTO createDTO(String key, boolean success) {
        TestDTO result = new TestDTO();
        result.key = key;
        result.success = success;
        return result;
    }
}
