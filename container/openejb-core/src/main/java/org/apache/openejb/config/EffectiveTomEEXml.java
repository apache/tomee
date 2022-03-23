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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.util.JavaSecurityManagers;

import jakarta.xml.bind.Marshaller;
import java.io.File;

import static java.util.Arrays.asList;

// TODO: i18n or useless?

/**
 * Will dump properties once resolved.
 */
public final class EffectiveTomEEXml {
    public static void main(final String[] args) throws Exception {
        final CommandLine line = parseCommand(args);
        if (line == null) {
            return;
        }

        final Openejb openejb = JaxbOpenejb.readConfig(findXml(line).getCanonicalPath());
        final ConfigurationFactory configFact = new ConfigurationFactory();

        for (final Resource r : openejb.getResource()) {
            final ResourceInfo ri = configFact.configureService(r, ResourceInfo.class);
            if (!ri.properties.containsKey("SkipImplicitAttributes")) {
                ri.properties.put("SkipImplicitAttributes", "false");
            }
            r.getProperties().clear();
            r.getProperties().putAll(ri.properties);
        }

        // TODO: others

        final Marshaller marshaller = JaxbOpenejb.getContext(Openejb.class).createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(openejb, System.out);
    }

    private static CommandLine parseCommand(final String[] args) throws SystemExitException {
        final Options options = new Options();
        options.addOption(OptionBuilder.hasArg(true).withLongOpt("path").withDescription("[openejb|tomee].xml path").create("p"));

        final CommandLine line;
        try {
            line = new PosixParser().parse(options, args);
        } catch (final ParseException exp) {
            help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            help(options);
            return null;
        }
        return line;
    }

    private static File findXml(final CommandLine line) {
        File xml = null;
        if (line.hasOption("path")) {
            xml = new File(line.getOptionValue("path"));
        } else {
            for(final String config : asList("tomee.xml", "openejb.xml")) {
                xml = new File( // we shouldnt go to catalina.base, just a fallback
                        JavaSecurityManagers.getSystemProperty("openejb.base",
                                JavaSecurityManagers.getSystemProperty("openejb.home",
                                        JavaSecurityManagers.getSystemProperty("catalina.base", "missing"))),
                        config);
                if (xml.isFile()) {
                    break;
                }
            }
        }
        if (xml == null || !xml.isFile()) {
            throw new IllegalArgumentException(xml + " doesnt exist");
        }
        return xml;
    }

    private static void help(final Options options) {
        new HelpFormatter().printHelp("effectivetomee [options] <value>", "\n", options, "\n");
    }

    private EffectiveTomEEXml() {
        // no-op
    }
}
