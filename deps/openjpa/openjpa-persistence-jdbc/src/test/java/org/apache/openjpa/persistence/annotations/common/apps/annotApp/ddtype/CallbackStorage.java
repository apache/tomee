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
package org.apache.openjpa.persistence.annotations.common.apps.annotApp.ddtype;

import java.util.List;
import java.util.ArrayList;

/**
 * Singleton class to store callback invocation hits
 * @author aokeke
 *
 */
public class CallbackStorage
{
	private static CallbackStorage store = new CallbackStorage();

	private static List<String> clist = new ArrayList<String>();

	private CallbackStorage()
	{}

	public static CallbackStorage getInstance()
	{
		if(store == null)
			store = new CallbackStorage();
		return store;
	}

	public List<String> getClist() {
		return clist;
	}

	public void setClist(List<String> clist) {
		CallbackStorage.clist = clist;
	}

	public static void clearStore()
	{
		clist = new ArrayList<String>();
	}

	public static boolean isEmpty()
	{
		return clist.isEmpty();
	}

	public static int size()
	{
		return clist.size();
	}
}
