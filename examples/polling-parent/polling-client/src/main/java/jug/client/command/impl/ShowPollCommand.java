/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import jakarta.ws.rs.core.Response;

import static jug.client.command.impl.ShowPollCommand.SHOW_POLL_CMD;

@Command(name = SHOW_POLL_CMD, usage = SHOW_POLL_CMD + " <name>", description = "show a poll")
public class ShowPollCommand extends AbstractCommand {

    public static final String SHOW_POLL_CMD = "show-poll";

    @Override
    protected Response invoke(String cmd) {
        if (SHOW_POLL_CMD.length() + 1 >= cmd.length()) {
            System.err.println("please specify a poll name");
            return null;
        }

        return client.path("api/subject/find/".concat(cmd.substring(SHOW_POLL_CMD.length() + 1))).get();
    }
}
