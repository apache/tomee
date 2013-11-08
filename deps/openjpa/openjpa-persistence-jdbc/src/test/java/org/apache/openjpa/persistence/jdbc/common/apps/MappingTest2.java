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
package org.apache.openjpa.persistence.jdbc.common.apps;


import java.util.*;
import javax.persistence.*;

/**
 *	<p>Persistent class used in testing.</p>
 *
 *	@author		Abe White
 */

@Entity
public class MappingTest2
{
    private int             pk1             = 0;
    private int             pk2             = 0;
	private MappingTest1	oneOneOwner		= null;
	private MappingTest1	oneManyOwner	= null;
    private Set             manyManyOwner   = new HashSet ();


	public int getPk1 ()
	{
		return this.pk1;
	}


	public void setPk1 (int pk1)
	{
		this.pk1 = pk1;
	}


	public int getPk2 ()
	{
		return this.pk2;
	}


	public void setPk2 (int pk2)
	{
		this.pk2 = pk2;
	}


	public MappingTest1 getOneOneOwner ()
	{
		return this.oneOneOwner;
	}


	public void setOneOneOwner (MappingTest1 oneOneOwner)
	{
		this.oneOneOwner = oneOneOwner;
	}


	public MappingTest1 getOneManyOwner ()
	{
		return this.oneManyOwner;
	}


	public void setOneManyOwner (MappingTest1 oneManyOwner)
	{
		this.oneManyOwner = oneManyOwner;
	}


	public Set getManyManyOwner ()
	{
		return this.manyManyOwner;
	}


	public void setManyManyOwner (Set manyManyOwner)
	{
		this.manyManyOwner = manyManyOwner;
	}
}
