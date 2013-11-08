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
package org.apache.openjpa.persistence.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.regexp.RE;

/**
 * Utility class to verify whether a set of fragments appear in a list of
 * possible SQL statement.
 * 
 * @author Pinaki Poddar
 *
 */
public class SQLSniffer {
	
	private static Map<String, RE> cache = new HashMap<String, RE>();
	/**
     * Checks that the given set of regular expressions occur in at least one of
	 * the given input SQL.
	 */
	public static boolean matches(List<String> SQLs, String...regexes) {
		if (SQLs == null || regexes == null)
			return false;
		for (String sql : SQLs) {
			boolean matched = true;
			for (String key : regexes) {
				RE regex = getRegularExpression(key);
				if (!regex.match(sql)) {
					matched = false;
					break;
				}
			}
			if (matched)
				return true;
		}
		return false;
	}
	
	private static RE getRegularExpression(String regex) {
		if (cache.containsKey(regex)) 
			return cache.get(regex);
		RE re = new RE(regex);
		cache.put(regex, re);
		return re;
	}
}
