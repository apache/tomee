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
package org.apache.openjpa.persistence.jdbc.query.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.apache.openjpa.persistence.Generator;

/**
 * The persistent class for the DtaSrcField database table.
 * 
 */
@Entity
@NamedQueries({
@NamedQuery(name = "getDataSourceFieldById", 
    query = "SELECT new org.apache.openjpa.persistence.jdbc.query.domain.DtaSrcFieldBean(d) " +
                "FROM DtaSrcField d WHERE d.id=:id")
})
public class DtaSrcField implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator=Generator.UUID_STRING)
	private String id;

	@ManyToOne
	@JoinColumn(name="dataSourceId", referencedColumnName="id")
	private DtaSrc dataSource;

	@Column(length=100, nullable=false)
	private String name;

    public DtaSrcField() {
    }

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DtaSrc getDatasource() {
		return this.dataSource;
	}

	public void setDataSourceId(DtaSrc datasource) {
		this.dataSource = datasource;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DtaSrcField)) {
			return false;
		}
		DtaSrcField other = (DtaSrcField) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
}
