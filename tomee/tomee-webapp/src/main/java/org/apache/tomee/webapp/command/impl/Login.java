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

package org.apache.tomee.webapp.command.impl;

import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.CommandSession;

import java.util.Map;

public class Login implements Command {

    @Override
    public Object execute(CommandSession session, Map<String, Object> params) throws Exception {
        final String user = (String) params.get("user");
        final String pass = (String) params.get("pass");
        final boolean result = session.login(user, pass);

        if (result) {
            session.set("user", user);
            session.set("pass", pass);
        } else {
            session.set("user", null);
            session.set("pass", null);
        }

        return result;
    }
}
