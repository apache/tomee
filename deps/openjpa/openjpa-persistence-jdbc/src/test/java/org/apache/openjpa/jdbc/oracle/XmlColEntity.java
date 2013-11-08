/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.jdbc.oracle;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Version;

@Entity
public class XmlColEntity {

    @Id
    @Column(name = "ID")
    private int id;

    @Version
    @Column(name = "VERSION")
    private int version;

    @Lob @Basic
    @Column(name = "XMLCOLUMN")
    private String xmlColumn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }  
    
    public void setVersion(int version) {
        this.version = version;
    }

    public String getXmlColumn() {
        return xmlColumn;
    }

    public void setXmlColumn(String xmlColumn) {
        this.xmlColumn = xmlColumn;
    }
}
