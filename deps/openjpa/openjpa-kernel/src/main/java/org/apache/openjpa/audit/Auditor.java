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
package org.apache.openjpa.audit;

import java.util.Collection;

import org.apache.openjpa.kernel.Audited;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.util.Closeable;

/**
 * An auditor is responsible for recoding the audited information.
 * OpenJPA runtime tracks the {@link Auditable auditable} instances and invoke
 * implementation of this interface within a transaction.
 * <br>
 * The user implementation of this interface is configurable via
 * standard OpenJPA plug-in configuration framework.
 * 
 * @author Pinaki Poddar
 * @since 2.2.0
 */
public interface Auditor extends Configurable, Closeable {
	/**
	 * OpenJPA runtime will invoke this method with the given parameters
	 * within a transaction.
	 * 
	 * @param broker the active persistence context.
	 * @param newObjects the set of auditable objects being created. Can be empty, but never null.
	 * @param updates the set of auditable objects being updated. Can be empty, but never null.
	 * @param deletes the set of auditable objects being deleted. Can be empty, but never null.
	 */
	public void audit(Broker broker, Collection<Audited> newObjects, Collection<Audited> updates,
			Collection<Audited> deletes);
	
	/**
	 * Affirm if the transaction be rolled back if {@link #audit(Broker, Collection, Collection, Collection) audit}
	 * operation fails with an exception.
	 */
	public boolean isRollbackOnError();
}
