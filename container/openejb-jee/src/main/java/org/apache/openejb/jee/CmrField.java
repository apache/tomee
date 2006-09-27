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
 * The cmr-fieldType describes the bean provider's view of
 * a relationship. It consists of an optional description, and
 * the name and the class type of a field in the source of a
 * role of a relationship. The cmr-field-name element
 * corresponds to the name used for the get and set accessor
 * methods for the relationship. The cmr-field-type element is
 * used only for collection-valued cmr-fields. It specifies the
 * type of the collection that is used.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cmr-fieldType", propOrder = {
        "description",
        "cmrFieldName",
        "cmrFieldType"
        })
public class CmrField {

    @XmlElement(required = true)
    protected List<Text> description;
    @XmlElement(name = "cmr-field-name", required = true)
    protected String cmrFieldName;
    @XmlElement(name = "cmr-field-type")
    protected CmrFieldType cmrFieldType;
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

    public String getCmrFieldName() {
        return cmrFieldName;
    }

    public void setCmrFieldName(String value) {
        this.cmrFieldName = value;
    }

    public CmrFieldType getCmrFieldType() {
        return cmrFieldType;
    }

    public void setCmrFieldType(CmrFieldType value) {
        this.cmrFieldType = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }

}
