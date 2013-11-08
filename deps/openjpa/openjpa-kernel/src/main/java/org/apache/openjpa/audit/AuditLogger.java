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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.openjpa.kernel.Audited;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.lib.conf.Configuration;

/**
 * A default auditor that simply prints the audited instances.
 * The output could be directed to a file, defaults to <tt>System.out</tt>.
 *  
 * @author Pinaki Poddar
 *
 */
public class AuditLogger implements Auditor {
	private PrintStream _out = System.out;
	private String _file;
	
	@Override
	public void audit(Broker broker, Collection<Audited> newObjects, Collection<Audited> updates, 
			Collection<Audited> deletes) {
		for (Audited audited : newObjects) {
			_out.print(audited.getType() + ": [" + audited.getManagedObject() + "]");
			_out.println(" Fields:" + Arrays.toString(audited.getUpdatedFields()));
		}
		for (Audited audited : updates) {
			_out.print(audited.getType() + ": [" + audited.getOriginalObject() + "] to [" 
					+ audited.getManagedObject() + "]");
			_out.println(" Fields:" + Arrays.toString(audited.getUpdatedFields()));
		}
		for (Audited audited : deletes) {
			_out.print(audited.getType() + ": [" + audited.getOriginalObject() + "]");
			_out.println(" Fields:" + Arrays.toString(audited.getUpdatedFields()));
		}
	}
	
	public void setFile(String file) throws FileNotFoundException {
		_out = new PrintStream(new FileOutputStream(_file = file), true);
	}
	
	public String getFile() {
		return _file;
	}
	
	public boolean isRollbackOnError() {
		return false;
	}

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
		if (_out != System.out) {
			_out.close();
		}
	}
}
