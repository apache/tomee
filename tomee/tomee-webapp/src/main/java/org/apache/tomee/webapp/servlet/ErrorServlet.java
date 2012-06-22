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

package org.apache.tomee.webapp.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.tomee.webapp.TomeeException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


public class ErrorServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        writeJson(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeJson(req, resp);
    }

    private void writeJson(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Map<String, Object> result = new HashMap<String, Object>();

        result.put("status_code", String.valueOf(req.getAttribute("javax.servlet.error.status_code")));
        result.put("message", String.valueOf(req.getAttribute("javax.servlet.error.message")));

        {
            Throwable throwable = (Throwable) req.getAttribute("javax.servlet.error.exception");
            if(TomeeException.class.isInstance(throwable)) {
                throwable =  throwable.getCause();
            }

            final Writer writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);

            result.put("stackTrace", writer.toString());
            result.put("exception_type", throwable.getClass().getName());
        }

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        final Gson gson;
        if(Boolean.valueOf(req.getParameter("pretty"))) {
            gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new Gson();
        }
        resp.getWriter().write(gson.toJson(result));
    }
}
