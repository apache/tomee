/**
 *
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

package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * The application-exceptionType declares an application
 * exception. The declaration consists of:
 * <p/>
 * - the exception class. When the container receives
 * an exception of this type, it is required to
 * forward this exception as an applcation exception
 * to the client regardless of whether it is a checked
 * or unchecked exception.
 * - an optional rollback element. If this element is
 * set to true, the container must rollback the current
 * transaction before forwarding the exception to the
 * client.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "application-exceptionType", propOrder = {
        "exceptionClass",
        "rollback"
        })
public class ApplicationException {

    @XmlElement(name = "exception-class", required = true)
    protected String exceptionClass;
    protected boolean rollback;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public ApplicationException() {
    }

    public ApplicationException(String exceptionClass, boolean rollback) {
        this.exceptionClass = exceptionClass;
        this.rollback = rollback;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String value) {
        this.exceptionClass = value;
    }

    public boolean getRollback() {
        return rollback;
    }

    public void setRollback(boolean value) {
        this.rollback = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
