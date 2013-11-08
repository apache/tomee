/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.validation;

import javax.persistence.ValidationMode;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.validation.ValidationUnavailableException;


/**
 * Validation helper routines and wrappers to remove runtime dependencies
 * on the Bean Valdiation APIs or a implementation.
 * 
 * Note:  This class should have no direct dependency on the javax.validation
 *        packages, which should only occur in the ValidatorImpl class.
 *
 * @version $Rev$ $Date$
 */
public class ValidationUtils {

    private static final Localizer _loc = Localizer.forPackage(
        ValidationUtils.class);

    /**
     * Setup Validation support by determining if the javax.validation APIs
     * are available and then create a Validator if required by the
     * provided configuration.
     * @param conf
     * @return true if a Validator was created, false otherwise.
     * @throws If a Validator was required but could not be created.
     */
    public static boolean setupValidation(OpenJPAConfiguration conf) {
        Log log = conf.getConfigurationLog();
        boolean brc = false;
        
        // only try creating a Validator for JPA2 and if not mode==NONE
        if (conf.getSpecificationInstance().getVersion() < 2) {
            if (log.isTraceEnabled()) {
                log.trace("Not creating a ValidatorImpl because " +
                    "this app is using the JPA 1.0 Spec");
            }
        }
        else if (!(String.valueOf(ValidationMode.NONE)
                .equalsIgnoreCase(conf.getValidationMode()))) {
            // we'll use this in the exception handlers
            boolean bValRequired = String.valueOf(ValidationMode.CALLBACK)
                .equalsIgnoreCase(conf.getValidationMode());
            try {
                // see if the javax.validation spec api is available
                if (log.isTraceEnabled()) {
                    log.trace("Trying to load javax.validation APIs " +
                        "based on the ValidationMode="
                        + conf.getValidationMode());
                }
                @SuppressWarnings("unused")
                Class<?> c = Class.forName(
                    "javax.validation.ValidationException");
            } catch (ClassNotFoundException e) {
                if (bValRequired) {
                    // fatal error - ValidationMode requires a validator
                    Message msg = _loc.get("vlem-creation-error");
                    log.error(msg, e);
                    // rethrow as a more descriptive/identifiable exception
                    throw new ValidationUnavailableException(
                        msg.getMessage(),
                        new RuntimeException(e), true);
                } else {
                    // no optional validation provider, so just trace output
                    if (log.isTraceEnabled()) {
                        log.trace(_loc.get("vlem-creation-warn",
                            "No available javax.validation APIs"));
                    }
                    return brc;
                }
            }
            // we have the javax.validation APIs
            try {
                // try loading a validation provider
                ValidatorImpl validator = new ValidatorImpl(conf);
                // set the Validator into the config
                conf.setValidatorInstance(validator);
                // update the LifecycleEventManager plugin to use it
                conf.setLifecycleEventManager("validating");
                // all done, so return good rc if anyone cares
                brc = true;
            } catch (RuntimeException e) {
                if (bValRequired) {
                    // fatal error - ValidationMode requires a validator
                    // rethrow as a WrappedException
                    Message msg = _loc.get("vlem-creation-error");
                    log.error(msg, e);
                    // rethrow as a more descriptive/identifiable exception
                    throw new ValidationUnavailableException(
                        msg.getMessage(),
                        e, true);

                } else {
                    // unexpected, but validation is optional,
                    // so just log it as a warning
                    String msg = e.getMessage();
                    log.warn(_loc.get("vlem-creation-warn", msg == null ? e : msg ));
                    return brc;
                }
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Not creating a ValidatorImpl because " +
                    "ValidationMode=" + conf.getValidationMode());
            }
        }
        return brc;
    }

    /**
     * Determines whether an exception is a constraint violation exception via
     * class name. Does not require JSR-303 API to be in classpath.
     * @param e exception to check
     * @return true of the exception is a constraint violation exception
     */
    public static boolean isConstraintViolationException(Exception e) {
        if (e == null)
            return false;
        
        if (e.getClass().getName().equals(
            "javax.validation.ConstraintViolationException"))
            return true;
        
        return false;       
    }
}
