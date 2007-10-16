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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jee.sun;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ejbName",
    "tableName",
    "cmpFieldMapping",
    "cmrFieldMapping",
    "secondaryTable",
    "consistency"
})
public class EntityMapping {
    @XmlElement(name = "ejb-name", required = true)
    protected String ejbName;
    @XmlElement(name = "table-name", required = true)
    protected String tableName;
    @XmlElement(name = "cmp-field-mapping", required = true)
    protected List<CmpFieldMapping> cmpFieldMapping;
    @XmlElement(name = "cmr-field-mapping")
    protected List<CmrFieldMapping> cmrFieldMapping;
    @XmlElement(name = "secondary-table")
    protected List<SecondaryTable> secondaryTable;
    protected Consistency consistency;

    public String getEjbName() {
        return ejbName;
    }

    public void setEjbName(String value) {
        this.ejbName = value;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String value) {
        this.tableName = value;
    }

    public List<CmpFieldMapping> getCmpFieldMapping() {
        if (cmpFieldMapping == null) {
            cmpFieldMapping = new ArrayList<CmpFieldMapping>();
        }
        return this.cmpFieldMapping;
    }

    public List<CmrFieldMapping> getCmrFieldMapping() {
        if (cmrFieldMapping == null) {
            cmrFieldMapping = new ArrayList<CmrFieldMapping>();
        }
        return this.cmrFieldMapping;
    }

    public List<SecondaryTable> getSecondaryTable() {
        if (secondaryTable == null) {
            secondaryTable = new ArrayList<SecondaryTable>();
        }
        return this.secondaryTable;
    }

    public Consistency getConsistency() {
        return consistency;
    }

    public void setConsistency(Consistency value) {
        this.consistency = value;
    }
}
