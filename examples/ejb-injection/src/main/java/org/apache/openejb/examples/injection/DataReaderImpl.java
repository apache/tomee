/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.examples.injection;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * This is an EJB 3 style pojo stateless session bean
 * Every stateless session bean implementation must be annotated
 * using the annotation @Stateless
 * This EJB has 2 business interfaces: DataReaderRemote, a remote business
 * interface, and DataReaderLocal, a local business interface
 * 
 * The instance variables 'dataStoreRemote' is annotated with the @EJB annotation:
 * this means that the application server, at runtime, will inject in this instance
 * variable a reference to the EJB DataStoreRemote
 * 
 * The instance variables 'dataStoreLocal' is annotated with the @EJB annotation:
 * this means that the application server, at runtime, will inject in this instance
 * variable a reference to the EJB DataStoreLocal
 */
//START SNIPPET: code
@Stateless
public class DataReaderImpl implements DataReaderLocal, DataReaderRemote {
	
	@EJB private DataStoreRemote dataStoreRemote;
	@EJB private DataStoreLocal dataStoreLocal;
	
	public String readDataFromLocalStore() {
		return "LOCAL:"+dataStoreLocal.getData();
	}
	
	public String readDataFromRemoteStore() {
		return "REMOTE:"+dataStoreRemote.getData();
	}
}
//END SNIPPET: code