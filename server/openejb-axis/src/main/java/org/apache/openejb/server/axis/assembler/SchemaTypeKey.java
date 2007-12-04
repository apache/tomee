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
package org.apache.openejb.server.axis.assembler;

import javax.xml.namespace.QName;

public final class SchemaTypeKey {
    private final QName qname;

    private final boolean element;
    private final boolean simpleType;
    private final boolean anonymous;

    private final QName elementQName;


    public SchemaTypeKey(QName qname, boolean element, boolean isSimpleType, boolean anonymous, QName elementQName) {
        if (qname == null) throw new NullPointerException("qname is null");

        this.qname = qname;
        this.element = element;
        this.simpleType = isSimpleType;
        this.anonymous = anonymous;
        this.elementQName = elementQName;
    }

    public QName getQName() {
        return qname;
    }

    public boolean isElement() {
        return element;
    }

    public boolean isSimpleType() {
        return simpleType;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public QName getElementQName() {
        return elementQName;
    }

    public int hashCode() {
        return qname.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof SchemaTypeKey)) {
            return false;
        }
        SchemaTypeKey key = (SchemaTypeKey) other;
        return element == key.element && simpleType == key.simpleType && anonymous == key.anonymous && qname.equals(key.qname);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("\nSchemaTypeKey: ");
        buf.append(" isElement: ").append(element);
        buf.append(" isAnonymous: ").append(anonymous);
        buf.append(" isSimpleType: ").append(simpleType);
        buf.append("\n QName: ").append(qname).append("\n");
        return buf.toString();
    }
}
