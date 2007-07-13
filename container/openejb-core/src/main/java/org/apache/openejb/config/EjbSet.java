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

import org.apache.openejb.jee.EjbJar;

public class EjbSet implements ValidationResults {

    private final Vector failures = new Vector();
    private final Vector warnings = new Vector();
    private final Vector errors = new Vector();

    private final String jarPath;
    private final EjbJar jar;

    private final ClassLoader classLoader;

    public EjbSet(String jarPath, EjbJar jar, ClassLoader classLoader) {
        this.jarPath = jarPath;
        this.jar = jar;
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public EjbJar getJar() {
        return jar;
    }

    public void addWarning(ValidationWarning warning) {
        warnings.addElement(warning);
    }

    public void addFailure(ValidationFailure failure) {
        failures.addElement(failure);
    }

    public void addError(ValidationError error) {
        errors.addElement(error);
    }

    public ValidationFailure[] getFailures() {
        ValidationFailure[] tmp = new ValidationFailure[failures.size()];
        failures.copyInto(tmp);
        return tmp;
    }

    public ValidationWarning[] getWarnings() {
        ValidationWarning[] tmp = new ValidationWarning[warnings.size()];
        warnings.copyInto(tmp);
        return tmp;
    }

    public ValidationError[] getErrors() {
        ValidationError[] tmp = new ValidationError[errors.size()];
        errors.copyInto(tmp);
        return tmp;
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

    public EjbJar getEjbJar() {
        return jar;
    }

    public String getJarPath() {
        return jarPath;
    }
}
