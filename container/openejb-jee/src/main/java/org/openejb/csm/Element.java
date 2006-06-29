/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.openejb.csm;

import org.apache.xmlbeans.SchemaType;

import javax.xml.namespace.QName;

/**
 * @version $Revision$ $Date$
 */
public class Element {
    private final String name;
    private final SchemaType type;

    public Element(QName qname, SchemaType type) {
        this(qname.getLocalPart(), type);
    }

    public Element(String name, SchemaType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public SchemaType getType() {
        return type;
    }

    public String toString() {
        return "<" + name + "::" + type.getName().getLocalPart() + ">";
    }
}
