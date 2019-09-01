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
package jug.client;

import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.SimpleCompletor;
import jug.client.command.api.AbstractCommand;
import jug.client.util.CommandManager;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import java.io.OutputStreamWriter;
import java.util.Map;

public class Client {

    private static final String PROMPT = System.getProperty("user.name") + " @ jug > ";
    private static final String EXIT_CMD = "exit";

    private Client() {
        // no-op
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the base url as parameter");
            return;
        }

        final ConsoleReader reader = new ConsoleReader(System.in, new OutputStreamWriter(System.out));
        reader.addCompletor(new FileNameCompletor());
        reader.addCompletor(new SimpleCompletor(CommandManager.keys().toArray(new String[CommandManager.size()])));

        String line;
        while ((line = reader.readLine(PROMPT)) != null) {
            if (EXIT_CMD.equals(line)) {
                break;
            }

            Class<?> cmdClass = null;
            for (Map.Entry<String, Class<?>> cmd : CommandManager.getCommands().entrySet()) {
                if (line.startsWith(cmd.getKey())) {
                    cmdClass = cmd.getValue();
                    break;
                }
            }

            if (cmdClass != null) {
                final ObjectRecipe recipe = new ObjectRecipe(cmdClass);
                recipe.setProperty("url", args[0]);
                recipe.setProperty("command", line);
                recipe.setProperty("commands", CommandManager.getCommands());

                recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                recipe.allow(Option.NAMED_PARAMETERS);

                try {
                    final AbstractCommand cmdInstance = (AbstractCommand) recipe.create();
                    cmdInstance.execute(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("sorry i don't understand '" + line + "'");
            }
        }
    }
}
