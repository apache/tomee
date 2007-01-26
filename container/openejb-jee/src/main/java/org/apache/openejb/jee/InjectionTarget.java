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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * An injection target specifies a class and a name within
 * that class into which a resource should be injected.
 * <p/>
 * The injection target class specifies the fully qualified
 * class name that is the target of the injection.  The
 * Java EE specifications describe which classes can be an
 * injection target.
 * <p/>
 * The injection target name specifies the target within
 * the specified class.  The target is first looked for as a
 * JavaBeans property name.  If not found, the target is
 * looked for as a field name.
 * <p/>
 * The specified resource will be injected into the target
 * during initialization of the class by either calling the
 * set method for the target property or by setting a value
 * into the named field.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "injection-targetType", propOrder = {
        "injectionTargetClass",
        "injectionTargetName"
        })
public class InjectionTarget {

    @XmlElement(name = "injection-target-class", required = true)
    protected String injectionTargetClass;
    @XmlElement(name = "injection-target-name", required = true)
    protected String injectionTargetName;

    public InjectionTarget() {
    }

    public InjectionTarget(String injectionTargetClass, String injectionTargetName) {
        this.injectionTargetClass = injectionTargetClass;
        this.injectionTargetName = injectionTargetName;
    }

    public String getInjectionTargetClass() {
        return injectionTargetClass;
    }

    public void setInjectionTargetClass(String value) {
        this.injectionTargetClass = value;
    }

    public String getInjectionTargetName() {
        return injectionTargetName;
    }

    public void setInjectionTargetName(String value) {
        this.injectionTargetName = value;
    }

}
