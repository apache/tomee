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

package org.apache.tomee.webapp.command;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.tomee.webapp.Application;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    private Gson gson = new Gson();
    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private static final String PATH = "org.apache.tomee.webapp.command.impl.";

    public Map<String, Object> execute(final String raw) {
        final Map<String, Object> result = new HashMap<String, Object>();

        final long start = System.currentTimeMillis();
        result.put("start", start);

        try {
            final Map<String, Object> params = gson.fromJson(raw, mapType);
            result.put("params", params);

            // Remove the cmdName from this list.
            final String cmdName = (String) params.remove("cmdName");
            final Class<?> cls = Class.forName(PATH + cmdName);
            final IsProtected isProtected = cls.getAnnotation(IsProtected.class);
            if (isProtected != null) {
                final String sessionId = (String) params.get("sessionId");
                if (sessionId == null || "".equals(sessionId.trim())) {
                    throw new UserNotAuthenticated();
                }
                final Application.Session session = Application.getInstance().getSession(sessionId);
                session.assertAuthenticated();
            }

            final Command cmd = (Command) cls.newInstance();

            result.put("cmdName", cmdName);
            result.put("output", cmd.execute(params));
            result.put("success", Boolean.TRUE);

        } catch (Throwable e) {
            result.put("success", Boolean.FALSE);

            final Writer writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);

            result.put("output", writer.toString());
        }

        result.put("timeSpent", (System.currentTimeMillis() - start));
        return result;
    }
}
