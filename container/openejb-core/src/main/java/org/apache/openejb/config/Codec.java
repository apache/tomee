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
package org.apache.openejb.config;

import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.resource.jdbc.BasicDataSourceUtil;
import org.apache.openejb.resource.jdbc.PasswordCodec;
import org.apache.openejb.util.Messages;

/**
 * Command line tool on top of the {@link PasswordCodec} interface. Basically,
 * it allows end user to encode/decode a string (ie. a password) using a codec
 * implementation.
 */
public class Codec {

    private static Messages messages = new Messages(Codec.class);

    public static void main(String[] args) throws SystemExitException {

        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(option("h", "help", "cmd.codec.opt.help"));
        options.addOption(option("i", "codec", "i", "cmd.codec.opt.impl"));
        options.addOption(option("d", "decode", "cmd.codec.opt.decode"));
        options.addOption(option("e", "encode", "cmd.codec.opt.encode"));

        CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return;
        }

        if (!line.hasOption("codec")) {
            System.out.println("Must specify the PasswordCodec implementation to use.");
            help(options);
            return;
        }

        if (line.getArgList().size() != 1) {
            System.out.println("Must specify either a plain text to encode, either a ciphered value to decode.");
            help(options);
            return;
        }

        try {
            PasswordCodec codec = BasicDataSourceUtil.getPasswordCodec(line
                    .getOptionValue("codec"));

            if (line.hasOption("decode")) {
                String pwdArg = (String) line.getArgList().get(0);
                char[] encodedPassword = pwdArg.toCharArray();
                System.out.println(
                        "The plain text value for " + pwdArg
                        + " is " + codec.decode(encodedPassword));

            } else { // if option neither encode/decode is specified, we assume
                     // it is encode.
                String plainPassword = (String) line.getArgList().get(0);
                System.out.println(
                        "The encode value for " + plainPassword
                        + " is " + new String(codec.encode(plainPassword)));
            }

        } catch (SQLException e) {
            System.out.println("Could not load password codec implementation class. Check your classpath.");
            throw new SystemExitException(-1);
        }

    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("codec [options] <value>", "\n" + i18n("cmd.codec.description"), options, "\n");
    }

    private static Option option(String shortOpt, String longOpt,
            String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(i18n(description)).create(shortOpt);
    }

    private static Option option(String shortOpt, String longOpt,
            String argName, String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(i18n(description)).create(shortOpt);
    }

    private static String i18n(String key) {
        return messages.format(key);
    }

}
