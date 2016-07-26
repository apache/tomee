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

package org.apache.openejb.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.cipher.PasswordCipher;
import org.apache.openejb.cipher.PasswordCipherException;
import org.apache.openejb.cipher.PasswordCipherFactory;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Messages;
import org.apache.xbean.finder.ResourceFinder;

import java.util.Map;

/**
 * Command line tool on top of the {@link PasswordCipher} interface. Basically,
 * it allows end user to encrypt/decrypt a string (ie. a password) using a cipher
 * implementation.
 */
public class Cipher {

    private static final Messages messages = new Messages(Cipher.class);

    public static void main(final String[] args) throws SystemExitException {

        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();
        options.addOption(option("h", "help", "cmd.cipher.opt.help"));
        options.addOption(option("c", "cipher", "c", "cmd.cipher.opt.impl"));
        options.addOption(option("d", "decrypt", "cmd.cipher.opt.decrypt"));
        options.addOption(option("e", "encrypt", "cmd.cipher.opt.encrypt"));

        final CommandLine line;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (final ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return;
        }

        String cipherName = "Static3DES";
        if (line.hasOption("cipher")) {
            cipherName = line.getOptionValue("cipher");
        }

        if (line.getArgList().size() != 1) {
            System.out.println("Must specify either a plain text to encrypt or a ciphered value to decrypt.");
            help(options);
            return;
        }

        final PasswordCipher cipher;
        try {
            cipher = PasswordCipherFactory.getPasswordCipher(cipherName);

        } catch (final PasswordCipherException e) {
            System.out.println("Could not load password cipher implementation class. Check your classpath.");
            availableCiphers();

            throw new SystemExitException(-1);
        }

        if (line.hasOption("decrypt")) {
            final String pwdArg = (String) line.getArgList().get(0);
            final char[] encryptdPassword = pwdArg.toCharArray();

            System.out.println(cipher.decrypt(encryptdPassword));

        } else { // if option neither encrypt/decrypt is specified, we assume
            // it is encrypt.
            final String plainPassword = (String) line.getArgList().get(0);

            System.out.println(new String(cipher.encrypt(plainPassword)));
        }
    }

    private static void availableCiphers() {
        try {
            final ResourceFinder finder = new ResourceFinder("META-INF/");
            final Map<String, Class<? extends PasswordCipher>> impls = finder.mapAllImplementations(PasswordCipher.class);
            System.out.println("Available ciphers are: " + Join.join(", ", impls.keySet()));

        } catch (final Exception ignore) {
            // no-op
        }
    }

    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cipher [options] <value>", "\n" + i18n("cmd.cipher.description"), options, "\n");
        System.out.println("");
        availableCiphers();
    }

    private static Option option(final String shortOpt, final String longOpt, final String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(i18n(description)).create(shortOpt);
    }

    private static Option option(final String shortOpt, final String longOpt, final String argName, final String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(i18n(description)).create(shortOpt);
    }

    private static String i18n(final String key) {
        return messages.format(key);
    }

}
