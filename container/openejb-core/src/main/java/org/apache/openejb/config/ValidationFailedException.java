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

import org.apache.openejb.OpenEJBException;

/**
 * @version $Rev$ $Date$
 */
public class ValidationFailedException extends OpenEJBException implements ValidationResults {
    private final ValidationError[] errors;
    private final ValidationFailure[] failures;
    private final ValidationWarning[] warnings;
    private final String jarPath;
    private final String moduleType;

    public ValidationFailedException(String message, ValidationResults set) {
        this(message, set, null);
    }

    public ValidationFailedException(String message, ValidationResults set, ValidationFailedException e) {
        super(message, e);
        jarPath = set.getJarPath();
        errors = set.getErrors();
        failures = set.getFailures();
        warnings = set.getWarnings();
        moduleType = set.getModuleType();
    }

    public String getJarPath() {
        return jarPath;
    }

    public ValidationError[] getErrors() {
        return errors;
    }

    public ValidationFailure[] getFailures() {
        return failures;
    }

    public ValidationWarning[] getWarnings() {
        return warnings;
    }

    public boolean hasWarnings() {
        return warnings.length > 0;
    }

    public boolean hasFailures() {
        return failures.length > 0;
    }

    public boolean hasErrors() {
        return errors.length > 0;
    }

    public String getModuleType() {
        return moduleType;
    }
}
