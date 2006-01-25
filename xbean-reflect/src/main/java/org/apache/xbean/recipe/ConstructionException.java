/**
 *
 * Copyright 2005-2006 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.xbean.recipe;

/**
 * @version $Rev: 6680 $ $Date: 2005-12-24T04:38:27.427468Z $
 */
public class ConstructionException extends RuntimeException {
    private String beanName;
    private String attributeName;

    public ConstructionException() {
    }

    public ConstructionException(String message) {
        super(message);
    }

    public ConstructionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstructionException(Throwable cause) {
        super(cause);
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setPrependAttributeName(String attributeName) {
        this.attributeName += attributeName;
    }

    public String getMessage() {
        return "Unable to create bean " + beanName + " attribute " + attributeName + ": "  + super.getMessage();

    }
}
