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
package org.apache.openjpa.persistence.jdbc.unique;

import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name="N_UNIQUE_B",
	   uniqueConstraints={@UniqueConstraint(name="ucb_f1_f2", columnNames={"f1","f2"})})
public class NamedUniqueB {
	@Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="namedTestGenerator")
	@TableGenerator(name="namedTestGenerator", table="N_UNIQUE_GENERATOR", 
			pkColumnName="GEN1", valueColumnName="GEN2",
            uniqueConstraints={@UniqueConstraint(name="ucb_gen1_gen2", columnNames={"GEN1","GEN2"})})
	private int bid;
	
	// Same named field in UniqueA also is defined as unique
	@Column(unique=true, nullable=false)
	private int f1;
	
	@Column(nullable=false)
	private int f2;
	
	@CollectionTable(name="N_U_COLL_TBL", uniqueConstraints=
	    {@UniqueConstraint(name="ucb_f3", columnNames="f3")})
	@ElementCollection
	@Column(name="f3", nullable=false)
	private Set<String> f3;
}
