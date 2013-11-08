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

/** Denotes a composite identity combining a String and Integer field.
 * 
 * @author <A HREF="mailto:pinaki.poddar@gmail.com>Pinaki Poddar</A>
 *
 */
public class CompositeId implements Serializable {
	public Integer id;
	public String  name;
	
	public CompositeId () 
	{
	}

	public CompositeId (String idString)
	{
		int index = idString.indexOf(':');
		id = Integer.parseInt(idString.substring(0,index));
		name = idString.substring(index+1);
	}

	public CompositeId (Integer id, String name)
	{
		this.id   = id;
		this.name = name;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public String toString() 
	{
		return id + ":" + name;
	}
	
	@Override
	public boolean equals (Object other)
	{
		if (other instanceof CompositeId==false)
			return false;
		
		CompositeId that = (CompositeId) other;
		return id.equals(that.id) && name.equals(that.name);
	}
	
	@Override
	public int hashCode () 
	{
		return id.hashCode()+name.hashCode();
	}
}
