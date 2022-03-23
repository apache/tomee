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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cmrFieldName",
    "columnPair",
    "fetchedWith"
})
public class CmrFieldMapping {
    @XmlElement(name = "cmr-field-name", required = true)
    protected String cmrFieldName;
    @XmlElement(name = "column-pair", required = true)
    protected List<ColumnPair> columnPair;
    @XmlElement(name = "fetched-with")
    protected FetchedWith fetchedWith;

    public String getCmrFieldName() {
        return cmrFieldName;
    }

    public void setCmrFieldName(final String value) {
        this.cmrFieldName = value;
    }

    public List<ColumnPair> getColumnPair() {
        if (columnPair == null) {
            columnPair = new ArrayList<ColumnPair>();
        }
        return this.columnPair;
    }

    public FetchedWith getFetchedWith() {
        return fetchedWith;
    }

    public void setFetchedWith(final FetchedWith value) {
        this.fetchedWith = value;
    }
}
