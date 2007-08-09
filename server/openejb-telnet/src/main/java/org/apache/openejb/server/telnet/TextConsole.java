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

import java.io.InputStream;

import java.io.PrintStream;

import java.util.Properties;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

public class TextConsole {

    Logger logger = Logger.getInstance(LogCategory.OPENEJB_ADMIN, "org.apache.openejb.server.util.resources");

    Properties props;

    public TextConsole() {

    }

    public void init(Properties props) throws Exception {

        this.props = props;

    }

    boolean stop = false;

    DataInputStream in = null;

    PrintStream out = null;

    public static final char ESC = (char) 27;

    public static final String TTY_Reset = ESC + "[0m";

    public static final String TTY_Bright = ESC + "[1m";

    public static final String TTY_Dim = ESC + "[2m";

    public static final String TTY_Underscore = ESC + "[4m";

    public static final String TTY_Blink = ESC + "[5m";

    public static final String TTY_Reverse = ESC + "[7m";

    public static final String TTY_Hidden = ESC + "[8m";

    /* Foreground Colors */

    public static final String TTY_FG_Black = ESC + "[30m";

    public static final String TTY_FG_Red = ESC + "[31m";

    public static final String TTY_FG_Green = ESC + "[32m";

    public static final String TTY_FG_Yellow = ESC + "[33m";

    public static final String TTY_FG_Blue = ESC + "[34m";

    public static final String TTY_FG_Magenta = ESC + "[35m";

    public static final String TTY_FG_Cyan = ESC + "[36m";

    public static final String TTY_FG_White = ESC + "[37m";

    /* Background Colors */

    public static final String TTY_BG_Black = ESC + "[40m";

    public static final String TTY_BG_Red = ESC + "[41m";

    public static final String TTY_BG_Green = ESC + "[42m";

    public static final String TTY_BG_Yellow = ESC + "[43m";

    public static final String TTY_BG_Blue = ESC + "[44m";

    public static final String TTY_BG_Magenta = ESC + "[45m";

    public static final String TTY_BG_Cyan = ESC + "[46m";

    public static final String TTY_BG_White = ESC + "[47m";

    static String PROMPT = TTY_Reset + TTY_Bright + "[openejb]$ " + TTY_Reset;

    protected void exec(InputStream input, PrintStream out) {

        DataInputStream in = new DataInputStream(input);

        while (!stop) {

            prompt(in, out);

        }

    }

    protected void prompt(DataInputStream in, PrintStream out) {

        try {

            out.print(PROMPT);

            out.flush();

            String commandline = in.readLine();

            logger.debug("command: " + commandline);

            commandline = commandline.trim();

            if (commandline.length() < 1) return;

            String command = commandline;

            Command.Arguments args = null;

            int spacePosition = commandline.indexOf(' ');

            int tabPosition = commandline.indexOf('\t');

            if (spacePosition != -1 || tabPosition != -1) {

                int cutPosition = (spacePosition > tabPosition ? spacePosition : tabPosition);

                command = commandline.substring(0, cutPosition);

                args = new Command.Arguments(commandline.substring(cutPosition + 1));

            }

            Command cmd = Command.getCommand(command);

            if (cmd == null) {

                out.print(command);

                out.println(": command not found");

            } else {

                cmd.exec(args, in, out);

            }

        } catch (UnsupportedOperationException e) {

            this.stop = true;

        } catch (Throwable e) {

            e.printStackTrace(new PrintStream(out));

            this.stop = true;

        }

    }

    protected void badCommand(DataInputStream in, PrintStream out) throws IOException

    {

    }

}

