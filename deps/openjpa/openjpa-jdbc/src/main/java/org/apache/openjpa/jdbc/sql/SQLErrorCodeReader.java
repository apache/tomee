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
package org.apache.openjpa.jdbc.sql;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.xml.XMLFactory;
import org.apache.openjpa.util.StoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parses XML content of SQL Error State codes to populate error codes for
 * a given Database Dictionary.
 * 
 * @author Pinaki Poddar
 * 
 */
public class SQLErrorCodeReader {
	private Log log = null;
	public static final String ERROR_CODE_DELIMITER = ",";
	public static final Map<String, Integer> storeErrorTypes = 
		new HashMap<String, Integer>();
	static {
		storeErrorTypes.put("lock", StoreException.LOCK);
        storeErrorTypes.put("object-exists", StoreException.OBJECT_EXISTS);
        storeErrorTypes.put("object-not-found",
                StoreException.OBJECT_NOT_FOUND);
		storeErrorTypes.put("optimistic", StoreException.OPTIMISTIC);
		storeErrorTypes.put("referential-integrity",
				StoreException.REFERENTIAL_INTEGRITY);
		storeErrorTypes.put("query", StoreException.QUERY);
	}
	
	private static final Localizer _loc = 
		Localizer.forPackage(SQLErrorCodeReader.class);
	
	public List<String> getDictionaries(InputStream in) {
		List<String> result = new ArrayList<String>();
		DocumentBuilder builder = XMLFactory.getDOMParser(false, false);
		try {
			Document doc = builder.parse(in);
			Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("dictionary");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node dictionary = attrs.getNamedItem("class");
				if (dictionary != null) {
				   result.add(dictionary.getNodeValue());
				}
			}
		} catch (Throwable e) {
			if (log.isWarnEnabled()) {
				log.error(_loc.get("error-code-parse-error"));
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return result;
	}

	/**
     * Parses given stream of XML content for error codes of the given database
     * dictionary name. Populates the given dictionary with the error codes.
	 * 
	 */
	public void parse(InputStream in, String dictName, DBDictionary dict) {
		if (in == null || dict == null)
			return;
		log = dict.conf.getLog(JDBCConfiguration.LOG_JDBC);
		DocumentBuilder builder = XMLFactory.getDOMParser(false, false);
		try {
			Document doc = builder.parse(in);
			Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("dictionary");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				NamedNodeMap attrs = node.getAttributes();
				Node dictionary = attrs.getNamedItem("class");
				if (dictionary != null 
                        && dictionary.getNodeValue().equals(dictName)) {
					readErrorCodes(node, dict);
				}
			}
		} catch (Throwable e) {
			if (log.isWarnEnabled()) {
				log.error(_loc.get("error-code-parse-error"));
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	static void readErrorCodes(Node node, DBDictionary dict) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			short nodeType = child.getNodeType();
			if (nodeType == Node.ELEMENT_NODE) {
				String errorType = child.getNodeName();
				Node textNode = child.getFirstChild();
                if (storeErrorTypes.containsKey(errorType) && textNode != null){
                    String errorCodes = textNode.getNodeValue();
                    if (!StringUtils.isEmpty(errorCodes)) {
                        String[] codes = errorCodes.split(ERROR_CODE_DELIMITER);
                        for (String code : codes) {
                            dict.addErrorCode(storeErrorTypes.get(errorType),
                                    code.trim());
						}
					}
				}
			}
		}
	}
}
