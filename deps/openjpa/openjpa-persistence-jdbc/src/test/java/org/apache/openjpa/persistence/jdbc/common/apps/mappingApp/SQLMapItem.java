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


/** This simple domain class without any relationship is for testing basic 
 * SQL Result Set mapping functionality.
 * This class uses Application Identity.
 * The annotation specified herein examplifies test cases of accessing
 * with or without identity fields, aliased column names etc. 
 */ 
@SqlResultSetMappings(value = {
	/**
	 *  This mapping is the same as the native query that
	 *  takes a class argument.
	 *  <code>SELECT id,name,price FROM ITEM WHERE name='WINE'</code>
	 */ 		
	@SqlResultSetMapping(name = "MappingEquivalentToClassQuery", 
		entities = { @EntityResult(entityClass = SQLMapItem.class) 
	}),

	/**
     * This mapping is similar to the native query that takes a class argument
	 * but the query specifies aliases for the projection columns.
	 *  <code>SELECT id AS ITEM_ID,name AS ITEM_NAME FROM ITEM
	 *  WHERE NAME='WINE'</code>
	 */				
	@SqlResultSetMapping(name = "MappingWithAliasedColumnName", 
		entities = { @EntityResult(entityClass = SQLMapItem.class, 
			fields = {
				@FieldResult(name = "id",   column = "ITEM_ID"),
                @FieldResult(name = "name", column = "ITEM_NAME") })
			}),

	/**
	 * This mapping specifes only few fields of the application class.
	 * 
	 */ 		
	@SqlResultSetMapping(name="MappingWithPartialFields",
		entities={@EntityResult(entityClass=SQLMapItem.class,
			fields={
				@FieldResult(name="id", column="id")
			})
		}),

	@SqlResultSetMapping(name="MappingWithPartialFieldsExcludingIdField",
		entities={@EntityResult(entityClass=SQLMapItem.class,
			fields={
				@FieldResult(name="name", column="name")
				})
	
		})
})

@Entity
@Table(name = "SQLMAP_ITEM")
public class SQLMapItem
	implements Serializable
{

	private int id;
	private String name;
	private float  price;


	protected SQLMapItem()
	{
	}


	public SQLMapItem(int id)
	{
		this.id = id;
	}


	public SQLMapItem(int id, String name)
	{
		this.id = id;
		this.name = name;
	}


	public SQLMapItem(int id, String name, int price)
	{
		this.id = id;
		this.name = name;
		this.price = price;
	}


	public String toString()
	{
		return getId() + ":"+getName();
		//System.identityHashCode(this) + ":[" + id + "." + name + "]";
	}


	@Id
	@Column(name="ID")
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		System.out.println("setId " + id);
		this.id = id;
	}

	@Column(name="NAME")
	public String getName()
	{
		return name;
	}


	public void setName(String name)
	{
		System.out.println("setName " + name);
		this.name = name;
	}


	@Column(name="PRICE")
	public float getPrice()
	{
		return price;
	}


	public void setPrice(float price)
	{
		this.price = price;
	}
}
