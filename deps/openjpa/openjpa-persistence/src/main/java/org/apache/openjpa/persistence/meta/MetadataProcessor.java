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
package org.apache.openjpa.persistence.meta;

import java.util.List;
import java.util.Set;

/**
 * Collection of generic utility functions for extracting persistence related
 * metadata from user specified metadata available in various source 
 * environment.  
 * <br>
 * Persistence metadata needs to be acquired from different sources such as
 * annotated source code, compiled class files, XML descriptors or combinations
 * thereof under different invocation and configuration context. 
 * <br> 
 * Specific implementation of this interface is distinguished by the nature of 
 * the source and the representation available for type system in the source.
 *  
 * @param T the M2 representation of type based on the nature of the source 
 * e.g. {@linkplain TypeElement} for annotation processing of *.java files or 
 * {@link Class} for compiled bytecode. 
 * 
 * @param M the corresponding M2 representation for member of type T
 *
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 */
public interface MetadataProcessor<T,M> {
	/**
	 * Determine the access type of the given type. 
	 * 
	 * @return an integer denoting the type of access. The integer value 
	 * corresponds to {@linkplain ClassMetaData#getAccessType()}.
	 */
	public int determineTypeAccess(T t);
	
	/**
	 * Determine the access type of the given member. 
	 * 
	 * @return an integer denoting the type of access. The integer value 
	 * corresponds to {@linkplain FieldMetaData#getAccessType()}.
	 */
	public int determineMemberAccess(M m);
	
	/**
	 * Get the persistent members of the given type.
	 * 
	 */
	public Set<M> getPersistentMembers(T t);
	
	/**
	 * Gets the violations, if any.
	 * 
	 * @return null or empty list if no exceptions.
	 */
	public List<Exception> validateAccess(T t);
	
	/**
     * Affirms if the members of given type are using both field and property
	 * based access. 
	 */
	public boolean isMixedAccess(T t);
	
	public T getPersistentSupertype(T t);
}
