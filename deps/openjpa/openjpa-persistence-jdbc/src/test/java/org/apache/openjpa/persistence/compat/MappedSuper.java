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
package org.apache.openjpa.persistence.compat;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@MappedSuperclass
public abstract class MappedSuper {
	private static final long serialVersionUID = -3613354849799979342L;
    @Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdateDate;

    @Version
	private int version;
	
    @Id
    private int id;
    
    @OneToOne(mappedBy="entityA") 
    EntityB entityB;
    
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	public int getId() {
	    return id;
	}

	public void setId(int id) {
	    this.id = id;
	}

    public EntityB getEntityB() {
        return entityB;
    }
    
    public void setEntityB(EntityB entityB) {
        this.entityB = entityB;
    }

}
