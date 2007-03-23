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
import java.util.ArrayList;
import java.util.List;


/**
 * The security-identityType specifies whether the caller's
 * security identity is to be used for the execution of the
 * methods of the enterprise bean or whether a specific run-as
 * identity is to be used. It contains an optional description
 * and a specification of the security identity to be used.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "security-identityType", propOrder = {
        "description",
        "useCallerIdentity",
        "runAs"
        })
public class SecurityIdentity {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "use-caller-identity")
    protected EmptyType useCallerIdentity;
    @XmlElement(name = "run-as")
    protected RunAs runAs;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    public List<Text> getDescription() {
        if (description == null) {
            description = new ArrayList<Text>();
        }
        return this.description;
    }

    public EmptyType getUseCallerIdentity() {
        return useCallerIdentity;
    }

    public void setUseCallerIdentity(EmptyType value) {
        this.useCallerIdentity = value;
    }

    public String getRunAs() {
        return runAs == null ? null: runAs.getRoleName();
    }

    public void setRunAs(RunAs value) {
        this.runAs = value;
    }

    public void setRunAs(String value) {
        this.runAs = new RunAs(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
