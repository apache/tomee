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
import javax.persistence.Entity;

/**
 *	<p>Persistent type used for testing.</p>
 *
 *	@author		Abe White
 */
@Entity
public class MappingTest1
{
    private int             value                   = 0;
    private int             otherTableValue         = 0;
    private Object          blob                    = null;
	private MappingTest2 	oneOne					= null;
	private MappingTest1 	selfOneOne				= null;
	private MappingTest2 	otherTableOneOne		= null;
	private MappingTest2	inverseOwnerOneOne		= null;
	private MappingTest2	inverseOneOne			= null;
    private Set             collection              = new HashSet ();
    private Set             inverseOwnerOneMany     = new HashSet ();
    private Set             oneMany                 = new HashSet ();
    private List            manyMany                = new LinkedList ();
    private Set             inverseOwnerManyMany    = new HashSet ();
    private Map             map                     = new HashMap ();
    private Map             nManyMap                = new HashMap ();
    private Map             manyManyMap             = new HashMap ();
    private int             transactionalValue      = 0;


	public int getValue ()
	{
		return this.value;
	}


	public void setValue (int value)
	{
		this.value = value;
	}


	public int getOtherTableValue ()
	{
		return this.otherTableValue;
	}


	public void setOtherTableValue (int otherTableValue)
	{
		this.otherTableValue = otherTableValue;
	}


	public Object getBlob ()
	{
		return this.blob;
	}


	public void setBlob (Object blob)
	{
		this.blob = blob;
	}


	public MappingTest2 getOneOne ()
	{
		return this.oneOne;
	}


	public void setOneOne (MappingTest2 oneOne)
	{
		this.oneOne = oneOne;
	}


	public MappingTest1 getSelfOneOne ()
	{
		return this.selfOneOne;
	}


	public void setSelfOneOne (MappingTest1 selfOneOne)
	{
		this.selfOneOne = selfOneOne;
	}


	public MappingTest2 getOtherTableOneOne ()
	{
		return this.otherTableOneOne;
	}


	public void setOtherTableOneOne (MappingTest2 otherTableOneOne)
	{
		this.otherTableOneOne = otherTableOneOne;
	}


	public MappingTest2 getInverseOwnerOneOne ()
	{
		return this.inverseOwnerOneOne;
	}


	public void setInverseOwnerOneOne (MappingTest2 inverseOwnerOneOne)
	{
		this.inverseOwnerOneOne = inverseOwnerOneOne;
	}


	public MappingTest2 getInverseOneOne ()
	{
		return this.inverseOneOne;
	}


	public void setInverseOneOne (MappingTest2 inverseOneOne)
	{
		this.inverseOneOne = inverseOneOne;
	}


	public Set getCollection ()
	{
		return this.collection;
	}


	public void setCollection (Set collection)
	{
		this.collection = collection;
	}


	public Set getInverseOwnerOneMany ()
	{
		return this.inverseOwnerOneMany;
	}


	public void setInverseOwnerOneMany (Set inverseOwnerOneMany)
	{
		this.inverseOwnerOneMany = inverseOwnerOneMany;
	}


	public Set getOneMany ()
	{
		return this.oneMany;
	}


	public void setOneMany (Set oneMany)
	{
		this.oneMany = oneMany;
	}


	public List getManyMany ()
	{
		return this.manyMany;
	}


	public void setManyMany (List manyMany)
	{
		this.manyMany = manyMany;
	}


	public Set getInverseOwnerManyMany ()
	{
		return this.inverseOwnerManyMany;
	}


	public void setInverseOwnerManyMany (Set inverseOwnerManyMany)
	{
		this.inverseOwnerManyMany = inverseOwnerManyMany;
	}


	public Map getMap ()
	{
		return this.map;
	}


	public void setMap (Map map)
	{
		this.map = map;
	}


	public Map getNManyMap ()
	{
		return this.nManyMap;
	}


	public void setNManyMap (Map nManyMap)
	{
		this.nManyMap = nManyMap;
	}


	public Map getManyManyMap ()
	{
		return this.manyManyMap;
	}


	public void setManyManyMap (Map manyManyMap)
	{
		this.manyManyMap = manyManyMap;
	}


	public int getTransactionalValue ()
	{
		return this.transactionalValue;
	}


	public void setTransactionalValue (int transactionalValue)
	{
		this.transactionalValue = transactionalValue;
	}
}
