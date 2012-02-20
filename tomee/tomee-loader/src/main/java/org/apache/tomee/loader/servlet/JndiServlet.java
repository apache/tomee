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
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.tomee.loader.dto.JndiDTO;
import org.apache.tomee.loader.dto.TestDTO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
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

@WebServlet(name = "jndi", urlPatterns = "/ws/jndi", asyncSupported = false)
public class JndiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String json;
        try {
            final Map<String, Object> result = new HashMap<String, Object>();
            result.put("jndi", get());
            json = new Gson().toJson(result);
        } catch (NamingException e) {
            throw new ServletException(e);
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    public List<JndiDTO> get() throws NamingException {
        final List<JndiDTO> result = new ArrayList<JndiDTO>();
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        mountJndiList(result, containerSystem.getJNDIContext(), "java:global");

        return result;
    }

    private void mountJndiList(List<JndiDTO> jndi, Context context, String root) throws NamingException {
        final NamingEnumeration namingEnumeration;
        try {
            namingEnumeration = context.list(root);
        } catch (NamingException e) {
            //not found?
            return;
        }
        while (namingEnumeration.hasMoreElements()) {
            final NameClassPair pair = (NameClassPair) namingEnumeration.next();
            final String key = root + "/" + pair.getName();
            final Object obj;
            try {
                obj = context.lookup(key);
            } catch (NamingException e) {
                //not found?
                continue;
            }

            if (Context.class.isInstance(obj)) {
                mountJndiList(jndi, Context.class.cast(obj), key);
            } else {
                final JndiDTO dto = new JndiDTO();
                dto.path = key;
                dto.name = pair.getName();
                dto.value = String.valueOf(obj);
                jndi.add(dto);
            }
        }

    }
}
