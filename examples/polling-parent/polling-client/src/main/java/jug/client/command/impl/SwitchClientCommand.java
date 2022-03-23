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
import jug.client.util.ClientNameHolder;

import jakarta.ws.rs.core.Response;
import java.util.Map;

@Command(name = "client", usage = "client <name>", description = "change client")
public class SwitchClientCommand extends AbstractCommand {

    private Map<String, Class<?>> commands;

    @Override
    public void execute(final String cmd) {
        if (cmd.length() <= "client ".length()) {
            System.err.println("please specify a client name (client1 or client2)");
            return;
        }

        final String client = cmd.substring(7);
        ClientNameHolder.setCurrent(client);
    }

    @Override
    protected Response invoke(String cmd) {
        return null;
    }
}

