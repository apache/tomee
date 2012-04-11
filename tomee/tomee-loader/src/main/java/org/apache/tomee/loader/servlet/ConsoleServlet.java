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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConsoleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ScriptEngineManager manager = new ScriptEngineManager();

        String engineName = req.getParameter("engineName");
        if (engineName == null || "".equals(engineName.trim())) {
            engineName = "JavaScript";
        }
        final ScriptEngine engine = manager.getEngineByName(engineName);

        engine.put("req", req);
        engine.put("resp", resp);

        engine.put("util", new Utility() {


            @Override
            public void write(Object obj) throws Exception {
                resp.getWriter().write(String.valueOf(obj));
            }

            @Override
            public String getJson(Object obj) {
                return new Gson().toJson(obj);
            }
        });

        String scriptCode = req.getParameter("scriptCode");
        if (scriptCode == null || "".equals(scriptCode.trim())) {
            scriptCode = "var a = 0;";
        }
        try {
            engine.eval(scriptCode);
        } catch (ScriptException e) {
            throw new ServletException(e);
        }
    }

    private interface Utility {
        void write(Object obj) throws Exception;
        String getJson(Object obj);
    }
}
