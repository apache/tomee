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

package org.apache.tomee.loader.servlet;

import com.google.gson.Gson;
import org.apache.tomee.loader.dto.TestDTO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@WebServlet(name = "test", urlPatterns = "/ws/test", asyncSupported = false)
public class TestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String json;
        try {
            final Map<String, Object> result = new HashMap<String, Object>();
            result.put("test", get());
            json = new Gson().toJson(result);
        } catch (NamingException e) {
            throw new ServletException(e);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    private List<TestDTO> get() throws NamingException {
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
