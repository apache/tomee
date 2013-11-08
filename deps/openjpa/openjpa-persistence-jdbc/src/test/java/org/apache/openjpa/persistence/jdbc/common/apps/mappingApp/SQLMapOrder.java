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
package org.apache.openjpa.persistence.jdbc.common.apps.mappingApp;

import java.io.Serializable;

import javax.persistence.*;

/** Exemplifies a mapping that is overwritten in orm.xml file.
 * 
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 *
 */
@SqlResultSetMapping(name="Overwritten by Descriptor",
		entities={
			@EntityResult(entityClass=SQLMapOrder.class)
		},
		columns={
			@ColumnResult(name="id")
		}
	)

@Entity
@Table(name = "SQLMAP_ORDER")
public class SQLMapOrder implements Serializable {
	@Id
	int id;

	int quantity;

	@OneToOne
	SQLMapItem item;

	protected SQLMapOrder() {
	}

	public SQLMapOrder(int id) {
		this(id, 1);
	}

	public SQLMapOrder(int id, int quantity) {
		this.id = id;
		this.quantity = 1;
		this.item = null;
	}

	public SQLMapItem getItem() {
		return item;
	}

	public void setItem(SQLMapItem item) {
		this.item = item;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}
	@PostLoad
	protected void inform() {
		System.out.println("Loaded" + this);
	}
}
