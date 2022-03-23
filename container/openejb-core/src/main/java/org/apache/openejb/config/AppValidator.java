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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.config.rules.CheckAnnotations;
import org.apache.openejb.config.rules.CheckAssemblyBindings;
import org.apache.openejb.config.rules.CheckAsynchronous;
import org.apache.openejb.config.rules.CheckCallbacks;
import org.apache.openejb.config.rules.CheckCdiEnabled;
import org.apache.openejb.config.rules.CheckClasses;
import org.apache.openejb.config.rules.CheckDependsOn;
import org.apache.openejb.config.rules.CheckDescriptorLocation;
import org.apache.openejb.config.rules.CheckIncorrectPropertyNames;
import org.apache.openejb.config.rules.CheckInjectionPointUsage;
import org.apache.openejb.config.rules.CheckInjectionTargets;
import org.apache.openejb.config.rules.CheckMethods;
import org.apache.openejb.config.rules.CheckPersistenceRefs;
import org.apache.openejb.config.rules.CheckRestMethodArePublic;
import org.apache.openejb.config.rules.CheckUserTransactionRefs;
import org.apache.openejb.config.rules.ValidationBase;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;

import jakarta.enterprise.inject.spi.DefinitionException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class AppValidator {

    protected static final Messages _messages = new Messages("org.apache.openejb.config.rules");

    private int level = 2;
    private boolean printXml;
    private boolean printWarnings = true;
    private boolean printCount;

    private final List<ValidationResults> sets = new ArrayList<>();
    private ValidationBase[] additionalValidators;

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public AppValidator() throws OpenEJBException {
    }

    public AppValidator(final int level, final boolean printXml, final boolean printWarnings, final boolean printCount) {
        this.level = level;
        this.printXml = printXml;
        this.printWarnings = printWarnings;
        this.printCount = printCount;
    }

    public AppValidator(final ValidationBase... additionalValidator) {
        additionalValidators = additionalValidator;
    }

    public void addValidationResults(final ValidationResults set) {
        sets.add(set);
    }

    public ValidationResults[] getValidationResultsSets() {
        final ValidationResults[] ejbSets = new ValidationResults[sets.size()];
        return sets.toArray(ejbSets);
    }

    // START SNIPPET : code2
    public AppModule validate(final AppModule appModule) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader()); // be sure to not mix classloaders
        try {
            final ValidationRule[] rules = getValidationRules();
            for (ValidationRule rule : rules) {
                rule.validate(appModule);
            }
        } catch (final DefinitionException de) {
            throw de;
        } catch (final Throwable e) {
            e.printStackTrace(System.out);
            final ValidationError err = new ValidationError("cannot.validate");
            err.setCause(e);
            err.setDetails(e.getMessage());
            appModule.getValidation().addError(err);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
        return appModule;
    }

    // END SNIPPET : code2
// START SNIPPET : code1
    protected ValidationRule[] getValidationRules() {
        // we don't want CheckClassLoading in standalone mode since it doesn't mean anything
        final ValidationRule[] defaultRules = new ValidationRule[]{
            new CheckClasses(),
            new CheckMethods(),
            new CheckCallbacks(),
            new CheckAssemblyBindings(),
            new CheckInjectionTargets(),
            new CheckInjectionPointUsage(),
            new CheckPersistenceRefs(),
            new CheckDependsOn(),
            new CheckUserTransactionRefs(),
            new CheckAsynchronous(),
            new CheckDescriptorLocation(),
            new CheckAnnotations(),
            new CheckIncorrectPropertyNames(),
            new CheckRestMethodArePublic(),
            new CheckCdiEnabled()
        };
        if (additionalValidators == null || additionalValidators.length == 0) {
            return defaultRules;
        }

        final ValidationRule[] rules = new ValidationRule[additionalValidators.length + defaultRules.length];
        System.arraycopy(additionalValidators, 0, rules, 0, additionalValidators.length);
        System.arraycopy(defaultRules, 0, rules, additionalValidators.length, defaultRules.length);
        return rules;
    }

    // END SNIPPET : code1
    public void printResults(final ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!printWarnings || !set.hasWarnings())) {
            return;
        }
        System.out.println("------------------------------------------");
        System.out.println("JAR " + set.getName());
        System.out.println("                                          ");

        printValidationExceptions(set.getErrors());
        printValidationExceptions(set.getFailures());

        if (printWarnings) {
            printValidationExceptions(set.getWarnings());
        }
    }

    protected void printValidationExceptions(final ValidationException[] exceptions) {
        for (ValidationException exception : exceptions) {
            System.out.print(" ");
            System.out.print(exception.getPrefix());
            System.out.print(" ... ");
            if (!(exception instanceof ValidationError)) {
                System.out.print(exception.getComponentName());
                System.out.print(": ");
            }
            if (level > 2) {
                System.out.println(exception.getMessage(1));
                System.out.println();
                System.out.print('\t');
                System.out.println(exception.getMessage(level));
                System.out.println();
            } else {
                System.out.println(exception.getMessage(level));
            }
        }
        if (printCount && exceptions.length > 0) {
            System.out.println();
            System.out.print(" " + exceptions.length + " ");
            System.out.println(exceptions[0].getCategory());
            System.out.println();
        }

    }

    public void printResultsXML(final ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!printWarnings || !set.hasWarnings())) {
            return;
        }

        System.out.println("<jar>");
        System.out.print("  <path>");
        System.out.print(set.getName());
        System.out.println("</path>");

        printValidationExceptionsXML(set.getErrors());
        printValidationExceptionsXML(set.getFailures());

        if (printWarnings) {
            printValidationExceptionsXML(set.getWarnings());
        }
        System.out.println("</jar>");
    }

    protected void printValidationExceptionsXML(final ValidationException[] exceptions) {
        for (ValidationException exception : exceptions) {
            System.out.print("    <");
            System.out.print(exception.getPrefix());
            System.out.println(">");
            if (!(exception instanceof ValidationError)) {
                System.out.print("      <ejb-name>");
                System.out.print(exception.getComponentName());
                System.out.println("</ejb-name>");
            }
            System.out.print("      <summary>");
            System.out.print(exception.getMessage(1));
            System.out.println("</summary>");
            System.out.println("      <description><![CDATA[");
            System.out.println(exception.getMessage(3));
            System.out.println("]]></description>");
            System.out.print("    </");
            System.out.print(exception.getPrefix());
            System.out.println(">");
        }
    }

    public void displayResults(final ValidationResults[] sets) {
        if (printXml) {
            System.out.println("<results>");
            for (ValidationResults set : sets) {
                printResultsXML(set);
            }
            System.out.println("</results>");
        } else {
            for (ValidationResults set : sets) {
                printResults(set);
            }
            for (int i = 0; i < sets.length; i++) {
                if (sets[i].hasErrors() || sets[i].hasFailures()) {
                    if (level < 3) {
                        System.out.println();
                        System.out.println("For more details, use the -vvv option");
                    }
                    i = sets.length;
                }
            }
        }
    }

    public static void main(final String[] args) throws SystemExitException {
        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();
        options.addOption(AppValidator.option("v", "version", "cmd.validate.opt.version"));
        options.addOption(AppValidator.option("h", "help", "cmd.validate.opt.help"));

        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (final ParseException exp) {
            AppValidator.help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            AppValidator.help(options);
            return;
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            return;
        }

        if (line.getArgList().size() == 0) {
            System.out.println("Must specify an module id.");
            AppValidator.help(options);
        }

        final DeploymentLoader deploymentLoader = new DeploymentLoader();

        try {
            final AppValidator validator = new AppValidator();
            for (final Object obj : line.getArgList()) {
                final String module = (String) obj;
                final File file = new File(module);
                final AppModule appModule = deploymentLoader.load(file, null);
                validator.validate(appModule);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void help(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("validate [options] <file> [<file>...]", "\n" + AppValidator.i18n("cmd.validate.description"), options, "\n");
    }

    private static Option option(final String shortOpt, final String longOpt, final String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(AppValidator.i18n(description)).create(shortOpt);
    }

    private static String i18n(final String key) {
        return AppValidator._messages.format(key);
    }
}
