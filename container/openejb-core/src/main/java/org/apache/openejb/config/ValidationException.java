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

import org.apache.openejb.util.Messages;

public class ValidationException extends java.lang.Exception {
    protected static final Messages messages = new Messages("org.apache.openejb.config.rules");
    protected Object[] details;
    protected String message;
    protected String prefix;
    protected String componentName;

    public ValidationException(String message) {
        this.message = message;
    }

    public void setDetails(Object... details) {
        this.details = details;
        if (details == null){
            details = new Object[]{};
        }
    }

    public Object[] getDetails() {
        return details;
    }

    public String getSummary() {
        return getMessage(1);
    }

    public String getMessage() {
        return getMessage(2);
    }

    public String getMessage(int level) {
        return messages.format(level + "." + message, details);
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getPrefix() {
        return "";
    }

    public String getCategory() {
        return "";
    }
}
