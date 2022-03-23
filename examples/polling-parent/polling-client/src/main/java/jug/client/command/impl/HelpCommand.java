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
import java.util.Map;

@Command(name = "help", usage = "help", description = "print this help")
public class HelpCommand extends AbstractCommand {

    private Map<String, Class<?>> commands;

    @Override
    public void execute(final String cmd) {
        for (Map.Entry<String, Class<?>> command : commands.entrySet()) {
            try {
                final Class<?> clazz = command.getValue();
                final Command annotation = clazz.getAnnotation(Command.class);
                System.out.println(annotation.name() + ": " + annotation.description());
                System.out.println("\tUsage: " + annotation.usage());
            } catch (Exception e) {
                // ignored = command not available
            }
        }
    }

    @Override
    protected Response invoke(String cmd) {
        return null;
    }

    public void setCommands(Map<String, Class<?>> commands) {
        this.commands = commands;
    }
}

