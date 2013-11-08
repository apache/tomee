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
package org.apache.openjpa.persistence.strategy.value;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.strats.AbstractValueHandler;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.meta.JavaTypes;

public class ValueStrategyHandler extends AbstractValueHandler {

	private static final long serialVersionUID = 8371304701543038775L;
	
	private static final ValueStrategyHandler _instance = new ValueStrategyHandler();
	
	public static ValueStrategyHandler getInstance(){
		return _instance;
	}
	
	@Override
	public Column[] map(ValueMapping arg0, String name, ColumnIO arg2,
			boolean arg3) {

		Column col = new Column();
		col.setName(name);
		col.setJavaType(JavaTypes.STRING);
		
		return new Column[]{col};
	}
	
	public Object toDataStoreValue(ValueMapping vm, Object val, JDBCStore store){
		
		if(val == null){
			return null;
		}
		
		return val.toString();
	}
	
	public Object toObjectValue(ValueMapping vm, Object val){
		if(val == null){
			return null;
		}
		
		return val.toString();
	}

}
