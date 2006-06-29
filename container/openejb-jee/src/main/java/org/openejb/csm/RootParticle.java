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

import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlAnySimpleType;

import javax.xml.namespace.QName;
import java.math.BigInteger;

/**
 * @version $Revision$ $Date$
 */
public class RootParticle implements SchemaParticle {
    private final SchemaType type;
    private final QName qname;

    public RootParticle(SchemaType type, QName qname) {
        this.type = type;
        this.qname = qname;
    }

    public int getParticleType() {
        return 0;
    }

    public BigInteger getMinOccurs() {
        return new BigInteger("1");
    }

    public BigInteger getMaxOccurs() {
        return new BigInteger("1");
    }

    public int getIntMinOccurs() {
        return 1;
    }

    public int getIntMaxOccurs() {
        return 1;
    }

    public boolean isSingleton() {
        return false;
    }

    public SchemaParticle[] getParticleChildren() {
        return new SchemaParticle[0];
    }

    public SchemaParticle getParticleChild(int i) {
        return null;
    }

    public int countOfParticleChild() {
        return 0;
    }

    public boolean canStartWithElement(QName qName) {
        return false;
    }

    public QNameSet acceptedStartNames() {
        return null;
    }

    public boolean isSkippable() {
        return false;
    }

    public QNameSet getWildcardSet() {
        return null;
    }

    public int getWildcardProcess() {
        return 0;
    }

    public QName getName() {
        return qname;
    }

    public SchemaType getType() {
        return type;
    }

    public boolean isNillable() {
        return false;
    }

    public String getDefaultText() {
        return null;
    }

    public XmlAnySimpleType getDefaultValue() {
        return null;
    }

    public boolean isDefault() {
        return false;
    }

    public boolean isFixed() {
        return false;
    }
}
