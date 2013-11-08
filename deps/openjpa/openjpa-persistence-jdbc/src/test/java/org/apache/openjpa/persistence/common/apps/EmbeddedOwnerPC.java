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
package org.apache.openjpa.persistence.common.apps;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import javax.persistence.Table;


/**
 *	<p>Persistent type used in testing embedded instances.</p>
 *
 *	@author		Abe White
 */
@Entity
@Table(name="embownpc")
@IdClass(EmbeddedOwnerPC.EmbKey.class)
public class EmbeddedOwnerPC
{
	@Id
	private int					id1;
	@Id
	private int					id2;
	@Basic
	private String				stringField;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private EmbeddedPC 			embedded;

	@OneToOne(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	private ComplexEmbeddedPC	complexEmbedded;


	protected EmbeddedOwnerPC ()
	{
	}

	public EmbeddedOwnerPC (int id1, int id2)
	{
		this.id1 = id1;
		this.id2 = id2;
	}


	public int getId1 ()
	{
		return id1;
	}


	public int getId2 ()
	{
		return id2;
	}


	public EmbeddedPC getEmbedded ()
	{
		return this.embedded;
	}


	public void setEmbedded (EmbeddedPC embedded)
	{
		this.embedded = embedded;
	}


	public String getStringField ()
	{
		return this.stringField;
	}


	public void setStringField (String stringField)
	{
		this.stringField = stringField;
	}


	public ComplexEmbeddedPC getComplexEmbedded ()
	{
		return this.complexEmbedded;
	}


	public void setComplexEmbedded (ComplexEmbeddedPC complexEmbedded)
	{
		this.complexEmbedded = complexEmbedded;
	}

	public static class EmbKey implements Serializable
	{
		public int					id1;
		public int					id2;

		public EmbKey()
		{
		}

		public EmbKey (String str)
		{
			int index = str.indexOf (":");
			if (index != -1)
			{
                id1 = Integer.parseInt (str.substring (0, index));
                id2 = Integer.parseInt(str.substring (index + 1));
			}
		}

		@Override
		public boolean equals(Object other)
		{
			if(!(other instanceof EmbKey))
				  return false;

	     	EmbKey touse = (EmbKey)other;

			if (id2 == 0 && touse.id2 != 0)
				return false;
			if (id2 != 0 && touse.id2 == 0)
				return false;
			if (!(id1 == touse.id1))
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			return (id1 + id2+"").hashCode();
		}
	}
}
