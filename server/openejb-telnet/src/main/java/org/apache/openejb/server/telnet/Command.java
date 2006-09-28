/**
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
package org.apache.openejb.server.telnet;

import java.io.DataInputStream;

import java.io.IOException;

import java.io.PrintStream;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.Iterator;

import java.util.StringTokenizer;

public class Command {

    protected static final HashMap commands = new HashMap();

    static

    {

        loadCommandList();

    }

    protected static final Command unknownCommand = new Command();

    protected static void register(String name, Command cmd) {

        commands.put(name, cmd);

    }

    protected static void register(String name, Class cmd) {

        commands.put(name, cmd);

    }

    public static Command getCommand(String name) {

        Object cmd = commands.get(name);

        if (cmd instanceof Class) {

            cmd = loadCommand((Class) cmd);

            register(name, (Command) cmd);

        }

        return (Command) cmd;

    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException

    {

        out.println("not implemented");

    }

    protected static Command loadCommand(Class commandClass) {

        Command cmd = null;

        try {

            cmd = (Command) commandClass.newInstance();

        } catch (Exception e) {

        }

        return cmd;

    }

    /*

    TODO:

    - Create the basic list in ant

    - Add the regexp package to the ant scripts

    - update the loadCommandList to read the list

      made in the ant script

    */

    protected static void loadCommandList() {

        Exit.register();

        Help.register();

        Lookup.register();

        Ls.register();

        Stop.register();

        Version.register();

        GroovySh.register();

    }

    public static class Arguments {

        private String args;

        private String[] argsArray = new String[0];

        private boolean alreadyParsed = false;

        Arguments(String args) {

            this.args = args;

        }

        String get() {

            return args;

        }

        String get(int i) {

            parseArgs();

            return (argsArray != null ? argsArray[i] : null);

        }

        int count() {

            parseArgs();

            return (argsArray != null ? argsArray.length : 0);

        }

        Iterator iterator() {

            return new Iterator() {

                StringTokenizer st = new StringTokenizer(args);

                public boolean hasNext() {

                    return st.hasMoreTokens();

                }

                public Object next() {

                    return st.nextToken();

                }

                public void remove() {

                }

            };

        }

        private void parseArgs() {

            if (!alreadyParsed) {

                ArrayList arrayList = new ArrayList();

                Iterator it = iterator();

                while (it.hasNext()) {

                    arrayList.add(it.next());

                }

                argsArray = (String[]) arrayList.toArray(argsArray);

                alreadyParsed = true;

            }

        }

    }

}

