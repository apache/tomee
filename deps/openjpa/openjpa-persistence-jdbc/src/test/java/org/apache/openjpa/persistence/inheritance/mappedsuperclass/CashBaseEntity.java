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
package org.apache.openjpa.persistence.inheritance.mappedsuperclass;

import java.util.Date;

import javax.persistence.*;

/**
 * Entity used to test MappedSuperClass which does not have IdClass. 
 * 
 * Test case and domain classes were originally part of the reported issue
 * <A href="https://issues.apache.org/jira/browse/OPENJPA-873">OPENJPA-873</A>
 *  
 * @author pioneer_ip@yahoo.com
 * @author Fay Wang
 *
 */

@MappedSuperclass
public abstract class CashBaseEntity extends BaseEntity{
    @Column(name="TRCCRUI")
    private String createUser;

    @Column(name="TRCCRTS")
    private Date createTime;

    @Column(name="TRCLUUID")
    private String updateUser;

    @Column(name="TRCLUTS")
    private Date updateTime;

    @Version
    @Column(name="TRCVER")
    private int version;

    public String getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public String getUpdateUser() {
        return updateUser;
    }
    
    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public int getVersion() {
        return version;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
