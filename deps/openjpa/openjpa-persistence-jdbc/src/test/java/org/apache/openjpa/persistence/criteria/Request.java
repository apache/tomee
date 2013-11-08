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
package org.apache.openjpa.persistence.criteria;

import java.sql.Date;

import javax.persistence.*;

@Entity
public class Request {
     @Id
     int id;
     
     private short status;
    
     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     private Account account;

     Date requestTime;

     public int getId() {
         return id;
     }
     
     public void setId(int id) {
         this.id = id;
     }
     
     public short getStatus() {
         return status;
     }

     public void setStatus(short status) {
         this.status = status;
     }

     
     public Account getAccount() {
         return account;
     }

     public void setAccount(Account account) {
         this.account = account;
     }
    
     public Date getRequestTime() {
         return requestTime;
     }
     
     public void setRequestTime(Date requestTime) {
         this.requestTime = requestTime;
     }
     
}
