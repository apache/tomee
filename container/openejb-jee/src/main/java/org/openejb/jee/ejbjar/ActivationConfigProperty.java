/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.ejbjar;

/**
 * @version $Revision$ $Date$
 */
public class ActivationConfigProperty {
    private String activationConfigPropertyName;
    private String activationConfigPropertyValue;

    public String getActivationConfigPropertyName() {
        return activationConfigPropertyName;
    }

    public void setActivationConfigPropertyName(String activationConfigPropertyName) {
        this.activationConfigPropertyName = activationConfigPropertyName;
    }

    public String getActivationConfigPropertyValue() {
        return activationConfigPropertyValue;
    }

    public void setActivationConfigPropertyValue(String activationConfigPropertyValue) {
        this.activationConfigPropertyValue = activationConfigPropertyValue;
    }
}
