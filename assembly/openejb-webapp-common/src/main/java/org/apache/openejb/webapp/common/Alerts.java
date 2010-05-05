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
package org.apache.openejb.webapp.common;

import java.util.ArrayList;
import java.util.List;

public class Alerts {

    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> infos = new ArrayList<String>();

    public void reset() {
        errors.clear();
        warnings.clear();
        infos.clear();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String message) {
        errors.add(message);
    }

    public void addError(String message, Exception e) {
        // todo add exception somehow
        System.out.println(message);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addWarning(String message) {
        System.out.println(message);
    }

    public boolean hasInfos() {
        return !infos.isEmpty();
    }

    public List<String> getInfos() {
        return infos;
    }

    public void addInfo(String message) {
        infos.add(message);
    }
}