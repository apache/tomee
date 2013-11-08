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
 *	<p>Persistent type used in testing.</p>
 *
 *	@author		Abe White
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class EagerPC
{
	@Id
	private int id;

	@Column(name="strngfld", length=50)
	private String		stringField;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private HelperPC 	eager;
	@Column(name="eagsub")
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private HelperPC4	eagerSub;
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private HelperPC2	recurse;
	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private HelperPC 	helper;
    @Transient private List eagerCollection   = new LinkedList ();
    @Transient private List recurseCollection = new LinkedList ();
    @Transient private List helperCollection  = new LinkedList ();

	public EagerPC()
	{}

	public EagerPC(int id)
	{
		this.id = id;
	}

	public String getStringField ()
	{
		return this.stringField;
	}

	public void setStringField (String stringField)
	{
		this.stringField = stringField;
	}


	public HelperPC getEager ()
	{
		return this.eager;
	}


	public void setEager (HelperPC eager)
	{
		this.eager = eager;
	}


	public HelperPC2 getRecurse ()
	{
		return this.recurse;
	}


	public void setRecurse (HelperPC2 recurse)
	{
		this.recurse = recurse;
	}


	public HelperPC getHelper ()
	{
		return this.helper;
	}


	public void setHelper (HelperPC helper)
	{
		this.helper = helper;
	}


	public List getEagerCollection ()
	{
		return this.eagerCollection;
	}


	public void setEagerCollection (List eagerCollection)
	{
		this.eagerCollection = eagerCollection;
	}


	public List getRecurseCollection ()
	{
		return this.recurseCollection;
	}


	public void setRecurseCollection (List recurseCollection)
	{
		this.recurseCollection = recurseCollection;
	}


	public List getHelperCollection ()
	{
		return this.helperCollection;
	}


	public void setHelperCollection (List helperCollection)
	{
		this.helperCollection = helperCollection;
	}


	public HelperPC4 getEagerSub ()
	{
		return this.eagerSub;
	}


	public void setEagerSub (HelperPC4 eagerSub)
	{
		this.eagerSub = eagerSub;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
