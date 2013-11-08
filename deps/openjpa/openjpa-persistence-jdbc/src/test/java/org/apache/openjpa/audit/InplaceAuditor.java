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
import org.apache.openjpa.lib.conf.Configuration;

/**
 * Example of an {@link Auditor auditor} that records the audit entries in the same database
 * of the managed entities being audited.
 * 
 * @author Pinaki Poddar
 *
 */
public class InplaceAuditor implements Auditor {

	@Override
	public void audit(Broker broker, Collection<Audited> newObjects, Collection<Audited> updates,
			Collection<Audited> deletes) {
		recordAudits(broker, newObjects);
		recordAudits(broker, updates);
		recordAudits(broker, deletes);
	}

	@Override
	public boolean isRollbackOnError() {
		return false;
	}
	
	/**
	 * Recording an audit is simply persisting an {@link AuditedEntry} with 
	 * the available {@link Broker persistence context}.
	 * @param broker
	 * @param audits
	 */
	private void recordAudits(Broker broker, Collection<Audited> audits) {
		for (Audited a : audits) {
			broker.persist(new AuditedEntry(a), null);
		}
	}

	// -------------------------------------------------------------
	// Configurable implementation that does nothing.
	// -------------------------------------------------------------
	@Override
	public void setConfiguration(Configuration conf) {
	}

	@Override
	public void startConfiguration() {
	}

	@Override
	public void endConfiguration() {
	}

	@Override
	public void close() throws Exception {
	}

}
