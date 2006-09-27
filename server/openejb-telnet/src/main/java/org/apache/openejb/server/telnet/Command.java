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

