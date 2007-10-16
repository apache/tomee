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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The variableType provides information on the scripting
 * variables defined by using this tag.  It is a (translation
 * time) error for a tag that has one or more variable
 * subelements to have a TagExtraInfo class that returns a
 * non-null value from a call to getVariableInfo().
 * <p/>
 * The subelements of variableType are of the form:
 * <p/>
 * description              Optional description of this
 * variable
 * <p/>
 * name-given               The variable name as a constant
 * <p/>
 * name-from-attribute      The name of an attribute whose
 * (translation time) value will
 * give the name of the
 * variable.  One of name-given or
 * name-from-attribute is required.
 * <p/>
 * variable-class           Name of the class of the variable.
 * java.lang.String is default.
 * <p/>
 * declare                  Whether the variable is declared
 * or not.  True is the default.
 * <p/>
 * scope                    The scope of the scripting varaible
 * defined.  NESTED is default.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variableType", propOrder = {
    "descriptions",
    "nameGiven",
    "nameFromAttribute",
    "variableClass",
    "declare",
    "scope"
})
public class Variable {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(name = "name-given")
    protected String nameGiven;
    @XmlElement(name = "name-from-attribute")
    protected String nameFromAttribute;
    @XmlElement(name = "variable-class")
    protected String variableClass;
    protected String declare;
    protected String scope;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    @XmlElement(name = "description", required = true)
    public Text[] getDescriptions() {
        return description.toArray();
    }

    public void setDescriptions(Text[] text) {
        description.set(text);
    }

    public String getDescription() {
        return description.get();
    }

    public String getNameGiven() {
        return nameGiven;
    }

    public void setNameGiven(String value) {
        this.nameGiven = value;
    }

    public String getNameFromAttribute() {
        return nameFromAttribute;
    }

    public void setNameFromAttribute(String value) {
        this.nameFromAttribute = value;
    }

    public String getVariableClass() {
        return variableClass;
    }

    public void setVariableClass(String value) {
        this.variableClass = value;
    }

    public String getDeclare() {
        return declare;
    }

    public void setDeclare(String value) {
        this.declare = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        this.scope = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
