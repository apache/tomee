
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
package org.apache.openjpa.persistence.xml;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class XmlOverrideToOneEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @OneToOne
    @JoinColumn(name="o2o")
    XmlOverrideToOneEntity otherO2O;

    @ManyToOne
    @JoinColumn(name="m2o")
    XmlOverrideToOneEntity otherM2O;
    
    @Version
    int version;
    
    public long getId() {
        return id;
    }

    public XmlOverrideToOneEntity getOtherO2O() {
        return otherO2O;
    }

    public void setOtherO2O(XmlOverrideToOneEntity otherO2O) {
        this.otherO2O = otherO2O;
    }

    public XmlOverrideToOneEntity getOtherM2O() {
        return otherM2O;
    }

    public void setOtherM2O(XmlOverrideToOneEntity otherM2O) {
        this.otherM2O = otherM2O;
    }

}
