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
 * The attribute element defines an attribute for the nesting
 * tag.  The attribute element may have several subelements
 * defining:
 * <p/>
 * description     a description of the attribute
 * <p/>
 * name            the name of the attribute
 * <p/>
 * required        whether the attribute is required or
 * optional
 * <p/>
 * rtexprvalue     whether the attribute is a runtime attribute
 * <p/>
 * type            the type of the attributes
 * <p/>
 * fragment        whether this attribute is a fragment
 * <p/>
 * deferred-value  present if this attribute is to be parsed as a
 * javax.el.ValueExpression
 * <p/>
 * deferred-method present if this attribute is to be parsed as a
 * javax.el.MethodExpression
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tld-attributeType", propOrder = {
    "descriptions",
    "name",
    "required",
    "rtexprvalue",
    "type",
    "deferredValue",
    "deferredMethod",
    "fragment"
})
public class TldAttribute {
    @XmlTransient
    protected TextMap description = new TextMap();
    @XmlElement(required = true)
    protected String name;
    protected String required;
    protected String rtexprvalue;
    protected String type;
    @XmlElement(name = "deferred-value")
    protected TldDeferredValue deferredValue;
    @XmlElement(name = "deferred-method")
    protected TldDeferredMethod deferredMethod;
    protected String fragment;
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

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String value) {
        this.required = value;
    }

    public String getRtexprvalue() {
        return rtexprvalue;
    }

    public void setRtexprvalue(String value) {
        this.rtexprvalue = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public TldDeferredValue getDeferredValue() {
        return deferredValue;
    }

    public void setDeferredValue(TldDeferredValue value) {
        this.deferredValue = value;
    }

    public TldDeferredMethod getDeferredMethod() {
        return deferredMethod;
    }

    public void setDeferredMethod(TldDeferredMethod value) {
        this.deferredMethod = value;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String value) {
        this.fragment = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
