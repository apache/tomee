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
package org.apache.openjpa.persistence.criteria;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compares a pair of String ignoring case-sensitivity of set of reserved words.
 *
 */
public class StringComparison {
	private static List<String> _reserved = Arrays.asList(new String[] {
			"ALL", "AND", "ANY", "AS", "ASC", "AVG",  
			"BETWEEN", "BIT_LENGTH", "BY", 
            "CASE", "CHAR_LENGTH", "CHARACTER_LENGTH", "CLASS", "COALESCE",
            "COUNT","CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
			"DELETE", "DESC", "DISTINCT", 
			"EMPTY", "ENTRY", "EXISTS",
			"FALSE", "FETCH", "FROM", 
			"GROUP",
			"HAVING",
			"IN", "INDEX", "INNER", "IS",
			"JOIN",
			"KEY",
			"LEFT", "LIKE", "LOWER",
			"MAX", "MEMBER", "MIN", "MOD", 
			"NEW", "NOT", "NULL", "NULLIF",
			"OBJECT", "OF", "OR", "ORDER", "OUTER",
			"POSITION",
			"SELECT", "SOME", "SUM",
			"THEN", "TRIM", "TRUE", "TYPE",
			"UNKNOWN", "UPDATE", "UPPER",
			"VALUE",
			"WHEN", "WHERE", 
	});
	
	private boolean isReservedWord(String s) {
		return _reserved.contains(s.toUpperCase());
	}
	
	public List<String> tokenize(String s) throws IOException {
		List<String> list = new ArrayList<String>();
		StreamTokenizer tok = new StreamTokenizer(new StringReader(s));
		tok.resetSyntax();
		tok.wordChars('a', 'z');
		tok.wordChars('0', '9');
		tok.wordChars('A', 'Z');
		tok.wordChars('\'', '\'');
		tok.wordChars('=', '=');
		tok.wordChars('>', '>');
		tok.wordChars('<', '<');
		tok.wordChars('!', '!');
		tok.wordChars('.', '.');
        for (int ttype; (ttype = tok.nextToken()) != StreamTokenizer.TT_EOF;) {
			if (ttype == StreamTokenizer.TT_WORD)
				list.add(tok.sval);
		}
		return list;
	}
	
	public boolean compare(String s1, String s2) {
		try {
			List<String> list1 = tokenize(s1);
			List<String> list2 = tokenize(s2);
			if (list1.size() != list2.size()) {
                System.err.println("Unequal tokens " + list1.size() + "!="
                        + list2.size());
				return false;
			}
			for (int i = 0; i < list1.size(); i++) {
				String a = list1.get(i);
				String b = list2.get(i);
                boolean match =
                    isReservedWord(a) ? a.equalsIgnoreCase(b) : a.equals(b);
				if (!match) {
                    System.err.println("[" + a + "] does not match [" + b
                            + "]");
					return false;
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		StringComparison c = new StringComparison();
        String s1 = "SELECT DISTINCT o FROM Order AS o JOIN o.lineItems AS l "
            + "WHERE l.shipped != FALSE and l.name like 'hello'";
        String s2 = "select DISTINCT o FROM Order AS o  JOIN o.lineItems AS  l "
            + "WHERE l.shipped !=  FALSE and l.name like 'hello'";
		boolean match = c.compare(s1, s2);
		if (!match) {
			System.err.println(s1);
			System.err.println(c.tokenize(s1));
			System.err.println(s2);
			System.err.println(c.tokenize(s2));
		}
	}

}
