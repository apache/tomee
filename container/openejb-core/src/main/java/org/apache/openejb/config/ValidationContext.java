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

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class ValidationContext implements ValidationResults {
    private final List<ValidationFailure> failures = new ArrayList<ValidationFailure>();
    private final List<ValidationWarning> warnings = new ArrayList<ValidationWarning>();
    private final List<ValidationError> errors = new ArrayList<ValidationError>();

    private final String moduleType;
    private final String name;
    private final DeploymentModule module;

    public ValidationContext(Class<? extends DeploymentModule> moduleType, String name) {
        this.moduleType = moduleType.getSimpleName();
        this.name = name;
        this.module = null;
    }

    public ValidationContext(DeploymentModule module) {
        this.moduleType = module.getClass().getSimpleName();
        this.module = module;
        this.name = null;
    }

    public DeploymentModule getModule() {
        return module;
    }

    public void fail(String component, String key, Object... details) {
        ValidationFailure failure = new ValidationFailure(key);
        failure.setDetails(details);
        failure.setComponentName(component);

        addFailure(failure);
    }

    public void warn(String component, String key, Object... details) {
        ValidationWarning warning = new ValidationWarning(key);
        warning.setDetails(details);
        warning.setComponentName(component);

        addWarning(warning);
    }

    public void error(String component, String key, Object... details) {
        ValidationError error = new ValidationError(key);
        error.setDetails(details);
        error.setComponentName(component);

        addError(error);
    }

    public void addWarning(ValidationWarning warning) {
        warnings.add(warning);
    }

    public void addFailure(ValidationFailure failure) {
        failures.add(failure);
    }

    public void addError(ValidationError error) {
        errors.add(error);
    }

    public ValidationFailure[] getFailures() {
        return failures.toArray(new ValidationFailure[failures.size()]);
    }

    public ValidationWarning[] getWarnings() {
        return warnings.toArray(new ValidationWarning[warnings.size()]);
    }

    public ValidationError[] getErrors() {
        return errors.toArray(new ValidationError[errors.size()]);
    }

    public boolean hasWarnings() {
        return warnings.size() > 0;
    }

    public boolean hasFailures() {
        return failures.size() > 0;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public String getName() {
        return (module == null) ? name : module.getModuleId();
    }

    public String getModuleType() {
        return moduleType;
    }

}
