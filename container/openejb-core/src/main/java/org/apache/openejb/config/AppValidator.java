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

import java.util.Vector;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.rules.CheckClasses;
import org.apache.openejb.config.rules.CheckMethods;
import org.apache.openejb.config.rules.CheckAssemblyBindings;
import org.apache.openejb.config.rules.CheckCallbacks;
import org.apache.openejb.config.rules.CheckInjectionTargets;
import org.apache.openejb.config.rules.CheckPersistenceRefs;
import org.apache.openejb.util.Messages;

public class AppValidator {

    protected static final Messages _messages = new Messages("org.apache.openejb.config.rules");

    int LEVEL = 2;
    boolean PRINT_XML = false;
    boolean PRINT_WARNINGS = true;
    boolean PRINT_COUNT = false;

    private Vector sets = new Vector();

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public AppValidator() throws OpenEJBException {
    }

    public AppValidator(int LEVEL, boolean PRINT_XML, boolean PRINT_WARNINGS, boolean PRINT_COUNT) {
        this.LEVEL = LEVEL;
        this.PRINT_XML = PRINT_XML;
        this.PRINT_WARNINGS = PRINT_WARNINGS;
        this.PRINT_COUNT = PRINT_COUNT;
    }

    public void addValidationResults(ValidationResults set) {
        sets.add(set);
    }

    public ValidationResults[] getValidationResultsSets() {
        ValidationResults[] ejbSets = new ValidationResults[sets.size()];
        sets.copyInto(ejbSets);
        return ejbSets;
    }

    public AppModule validate(final AppModule appModule) {
        try {
            ValidationRule[] rules = getValidationRules();
            for (int i = 0; i < rules.length; i++) {
                rules[i].validate(appModule);
            }
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            ValidationError err = new ValidationError("cannot.validate");
            err.setCause(e);
            err.setDetails(e.getMessage());
            appModule.getValidation().addError(err);
        }
        return appModule;
    }

    protected ValidationRule[] getValidationRules() {
        ValidationRule[] rules = new ValidationRule[]{
                new CheckClasses(),
                new CheckMethods(),
                new CheckCallbacks(),
                new CheckAssemblyBindings(),
                new CheckInjectionTargets(),
                new CheckPersistenceRefs()
        };
        return rules;
    }

    public void printResults(ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())) {
            return;
        }
        System.out.println("------------------------------------------");
        System.out.println("JAR " + set.getJarPath());
        System.out.println("                                          ");

        printValidationExceptions(set.getErrors());
        printValidationExceptions(set.getFailures());

        if (PRINT_WARNINGS) {
            printValidationExceptions(set.getWarnings());
        }
    }

    protected void printValidationExceptions(ValidationException[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            System.out.print(" ");
            System.out.print(exceptions[i].getPrefix());
            System.out.print(" ... ");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print(exceptions[i].getComponentName());
                System.out.print(": ");
            }
            if (LEVEL > 2) {
                System.out.println(exceptions[i].getMessage(1));
                System.out.println();
                System.out.print('\t');
                System.out.println(exceptions[i].getMessage(LEVEL));
                System.out.println();
            } else {
                System.out.println(exceptions[i].getMessage(LEVEL));
            }
        }
        if (PRINT_COUNT && exceptions.length > 0) {
            System.out.println();
            System.out.print(" " + exceptions.length + " ");
            System.out.println(exceptions[0].getCategory());
            System.out.println();
        }

    }

    public void printResultsXML(ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())) {
            return;
        }

        System.out.println("<jar>");
        System.out.print("  <path>");
        System.out.print(set.getJarPath());
        System.out.println("</path>");

        printValidationExceptionsXML(set.getErrors());
        printValidationExceptionsXML(set.getFailures());

        if (PRINT_WARNINGS) {
            printValidationExceptionsXML(set.getWarnings());
        }
        System.out.println("</jar>");
    }

    protected void printValidationExceptionsXML(ValidationException[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            System.out.print("    <");
            System.out.print(exceptions[i].getPrefix());
            System.out.println(">");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print("      <ejb-name>");
                System.out.print(exceptions[i].getComponentName());
                System.out.println("</ejb-name>");
            }
            System.out.print("      <summary>");
            System.out.print(exceptions[i].getMessage(1));
            System.out.println("</summary>");
            System.out.println("      <description><![CDATA[");
            System.out.println(exceptions[i].getMessage(3));
            System.out.println("]]></description>");
            System.out.print("    </");
            System.out.print(exceptions[i].getPrefix());
            System.out.println(">");
        }
    }

    public void displayResults(ValidationResults[] sets) {
        if (PRINT_XML) {
            System.out.println("<results>");
            for (int i = 0; i < sets.length; i++) {
                printResultsXML(sets[i]);
            }
            System.out.println("</results>");
        } else {
            for (int i = 0; i < sets.length; i++) {
                printResults(sets[i]);
            }
            for (int i = 0; i < sets.length; i++) {
                if (sets[i].hasErrors() || sets[i].hasFailures()) {
                    if (LEVEL < 3) {
                        System.out.println();
                        System.out.println("For more details, use the -vvv option");
                    }
                    i = sets.length;
                }
            }
        }

    }
}
