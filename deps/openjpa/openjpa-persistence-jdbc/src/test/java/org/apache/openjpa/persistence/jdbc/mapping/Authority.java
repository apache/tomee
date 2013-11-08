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
package org.apache.openjpa.persistence.jdbc.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import org.apache.openjpa.persistence.Persistent;
import org.apache.openjpa.persistence.jdbc.Strategy;

@Entity
@Table(name="authority")
@NamedQueries( {
       @NamedQuery(name = "AllIonAuthorities", query = "SELECT x FROM IonAuthority x")
})
public class Authority {
@Id
       @GeneratedValue(strategy = GenerationType.AUTO)
       @Column(name = "ID")
       private Integer id;

       @Enumerated( EnumType.STRING )
       @Column(nullable=false, length=128, updatable=true, insertable=true)
       @Persistent
       @Strategy("org.apache.openjpa.jdbc.meta.strats.EnumValueHandler")
       private AuthorityValues authorityName;

       
       @XmlType(name = "IonAuthorityValues")
       @XmlEnum
       public enum AuthorityValues {

          AUTH1,
          AUTH2,
       }

       public Authority() {}
       public Authority(AuthorityValues auth) {
           authorityName = auth;
       }

       public Integer getId() {
           return id;
       }

       public void setAuthorityName(AuthorityValues auth) {
           authorityName = auth;
       }
       
       public AuthorityValues getAuthorityName() {
           return authorityName;
       }
}
