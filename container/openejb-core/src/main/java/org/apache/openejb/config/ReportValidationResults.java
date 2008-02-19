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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import static org.apache.openejb.util.Join.join;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Join;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class ReportValidationResults implements DynamicDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_VALIDATION, "org.apache.openejb.config.rules");

    private static final String VALIDATION_LEVEL = "openejb.validation.output.level";

    private enum Level {
        TERSE,
        MEDIUM,
        VERBOSE
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        String levelString = SystemInstance.get().getProperty(VALIDATION_LEVEL, Level.MEDIUM.toString());

        Level level;
        try {
            level = Level.valueOf(levelString.toUpperCase());
        } catch (IllegalArgumentException noSuchEnumConstant) {
            try {
                int i = Integer.parseInt(levelString) - 1;
                level = Level.values()[i];
            } catch (Exception e) {
                level = Level.MEDIUM;
            }
        }


        if (!appModule.hasErrors() && !appModule.hasFailures()) return appModule;

        ValidationFailedException validationFailedException = null;

        List<ValidationContext> contexts = appModule.getValidationContexts();

        for (ValidationContext context : contexts) {
            logResults(context, level);
        }

        ValidationContext uberContext = new ValidationContext(AppModule.class, appModule.getValidation().getJarPath());
        for (ValidationContext context : contexts) {
            for (ValidationError error : context.getErrors()) {
                uberContext.addError(error);
            }
            for (ValidationFailure error : context.getFailures()) {
                uberContext.addFailure(error);
            }
            for (ValidationWarning error : context.getWarnings()) {
                uberContext.addWarning(error);
            }
        }

        if (level != Level.VERBOSE){
            List<Level> levels = Arrays.asList(Level.values());
            levels = levels.subList(level.ordinal() + 1, levels.size());

            logger.info("Set the '"+VALIDATION_LEVEL+"' system property to "+ join(" or ", levels) +" for increased validation details.");
        }

        validationFailedException = new ValidationFailedException("Module failed validation. "+uberContext.getModuleType()+"(path="+uberContext.getJarPath()+")", uberContext, validationFailedException);

        if (validationFailedException != null) throw validationFailedException;

        return appModule;
    }

    private void logResults(ValidationContext context, Level level) {
        if (context.hasErrors() || context.hasFailures()) {

            ValidationError[] errors = context.getErrors();
            ValidationFailure[] failures = context.getFailures();

            for (int j = 0; j < errors.length; j++) {
                ValidationError e = errors[j];
                String ejbName = e.getComponentName();
                logger.error(e.getPrefix() + " ... " + ejbName + ":\t" + e.getMessage(level.ordinal() + 1));
            }

            for (int j = 0; j < failures.length; j++) {
                ValidationFailure e = failures[j];
                logger.error(e.getPrefix() + " ... " + e.getComponentName() + ":\t" + e.getMessage(level.ordinal() + 1));
            }

            logger.error("Invalid "+context.getModuleType()+"(path="+context.getJarPath()+")");
//            logger.error("Validation: "+errors.length + " errors, "+failures.length+ " failures, in "+context.getModuleType()+"(path="+context.getJarPath()+")");
        }
    }

}
