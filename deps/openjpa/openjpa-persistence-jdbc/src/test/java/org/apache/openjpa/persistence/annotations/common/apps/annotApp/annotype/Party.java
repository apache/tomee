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
package
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype;

import java.util.Date;

import javax.persistence.Id;

@javax.persistence.MappedSuperclass
public class Party {

    @Id
    protected Long PartyId;

    protected String Status;
    protected String ArchiveStatus;
    protected Date CreateDate;


    public Long getPartyId() {
        return this.PartyId;
    }

    public void setPartyId(Long id){
        this.PartyId = id;
    }

    public void setArchiveStatus(String s){
        this.ArchiveStatus = s;

    }

    public void setStatus(String s) {
        this.Status = s;
    }


    public String getStatus() {
        return this.Status;
    }


    public String getArchiveStatus() {
        return this.ArchiveStatus;
    }

    public void setCreateDate(Date d) {
        this.CreateDate = d;
    }


    public Date getCreateDate() {
        return this.CreateDate;
    }
}
