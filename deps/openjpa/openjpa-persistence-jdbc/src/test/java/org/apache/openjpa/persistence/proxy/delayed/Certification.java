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
package org.apache.openjpa.persistence.proxy.delayed;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class Certification  implements Serializable, Comparable<Certification> {

    private static final long serialVersionUID = 4989402309885734073L;

    private String name;
    
    @Column(name="DC_CERT_LVL")
    private String level;
    
    @Temporal(TemporalType.DATE)
    private Date certDate;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setCertDate(Date certDate) {
        this.certDate = certDate;
    }

    public Date getCertDate() {
        return certDate;
    }

    @Override
    public int compareTo(Certification o) {
        String nameLevelDate = name+level+certDate;
        String nameLevelDate2 = o.getName()+o.getLevel()+o.getCertDate();
        return nameLevelDate.compareTo(nameLevelDate2);
    }
}
