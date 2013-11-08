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
package org.apache.openjpa.jdbc.meta.strats;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.meta.JavaTypes;


/**
 * Uses multiple version numbers spanning multiple columns for optimistic 
 * versioning.
 * 
 * @since 1.3.0
 *
 * @author Pinaki Poddar
 */
public class MultiColumnVersionStrategy extends NumberVersionStrategy {

    public static final String ALIAS = "version-numbers";
    
    private Number[] _initials = null;
    private Integer[] _javaTypes;
    
    @Override
    public void initialize() {
    	if (_initials == null) {
    		Column[] cols = vers.getColumns();
    		_initials = new Number[cols.length];
    		for (int i = 0; i < cols.length; i++) {
                _initials[i] = nextValue(0, getJavaType(i));
    		}
    	}
    	super.initialize();
    }
    	
    /**
     * Set the initial value for version columns. Defaults to 1 for each column.
     */
    public void setInitialValues(Number[] initial) {
    	_initials = new Number[initial.length];
    	System.arraycopy(initial, 0, _initials, 0, initial.length);
    }
    
    /**
     * Return the initial values for version columns. Defaults to 1 for each 
     * column.
     */
    public Number[] getInitialValues() {
    	return _initials;
    }

    public String getAlias() {
        return ALIAS;
    }
    
    protected int getJavaType() {
        return JavaTypes.ARRAY;
    }
    
    protected int getJavaType(int i) {
    	if (_javaTypes == null) {
            _javaTypes = new Integer[vers.getMappingInfo().getColumns().size()];
    	}
    	if (_javaTypes[i] == null) {
    		Column col = (Column)vers.getMappingInfo().getColumns().get(i);
    		if (!StringUtils.isEmpty(col.getTypeName())) {
    			Class javaType = Schemas.getJavaType(col.getType(), 
    					col.getSize(), col.getDecimalDigits());
    			_javaTypes[i] = JavaTypes.getTypeCode(javaType);
    		} else {
    			_javaTypes[i] = JavaTypes.INT;
    		}
    	}
    	return _javaTypes[i];
    }

    protected Object nextVersion(Object version) {
        if (version == null)
            return _initials;
        Object[] values = (Object[])version;
        Number[] result = new Number[values.length];
        for (int i = 0; i < values.length; i++)
        	result[i] = nextValue(values[i], getJavaType(i));
        return result;
    }
    
    Number nextValue(Object number, int javaTypeCode) {
    	Number result = (number == null) ? 1 : ((Number)number).intValue() + 1;
    	return	(Number)JavaTypes.convert(""+result, javaTypeCode);
    }
}
