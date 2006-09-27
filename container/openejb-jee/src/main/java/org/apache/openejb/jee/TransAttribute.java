/**
 *
 * Copyright 2006 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlEnumValue;


/**
 * The trans-attributeType specifies how the container must
 * manage the transaction boundaries when delegating a method
 * invocation to an enterprise bean's business method.
 * <p/>
 * The value must be one of the following:
 * <p/>
 * NotSupported
 * Supports
 * Required
 * RequiresNew
 * Mandatory
 * Never
 */
public enum TransAttribute {
    @XmlEnumValue("NotSupported") NOT_SUPPORTED,
    @XmlEnumValue("Supports") SUPPORTS,
    @XmlEnumValue("Required") REQUIRED,
    @XmlEnumValue("RequiresNew") REQUIRES_NEW,
    @XmlEnumValue("Mandatory") MANDATORY,
    @XmlEnumValue("Never") NEVER;
}
