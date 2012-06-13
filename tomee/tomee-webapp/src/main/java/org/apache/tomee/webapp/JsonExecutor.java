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

package org.apache.tomee.webapp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class JsonExecutor {

    private JsonExecutor() {
        //NO INSTANCE
    }

    public interface Executor {
        void call(Map<String, Object> json) throws Exception;
    }

    public static void execute(final HttpServletRequest req, final HttpServletResponse resp, final Executor executor) {
        try {
            final Map<String, Object> result = new HashMap<String, Object>();

            executor.call(result);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            final Gson gson;
            if (Boolean.valueOf(req.getParameter("pretty"))) {
                gson = new GsonBuilder().setPrettyPrinting().create();
            } else {
                gson = new Gson();
            }
            resp.getWriter().write(gson.toJson(result));

        } catch (Throwable e) {
            //this will redirect the result to the ErrorServlet
            throw new TomeeException(e);
        }
    }
}
