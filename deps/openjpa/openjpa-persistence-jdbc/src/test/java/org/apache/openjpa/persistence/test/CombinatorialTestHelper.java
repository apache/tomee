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
package org.apache.openjpa.persistence.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.openjpa.persistence.jdbc.update.TestParentChild;

/**
 * Aids to run a single test under different combination of configuration
 * parameters.
 * 
 * Each configurable property can be registered to this receiver with all its
 * possible values. This class generates all combinations of all the possible
 * property values as configuration and invokes the same test with each 
 * configuration combination.
 * The properties can be designated as <em>runtime</code> to be included in
 * combination for execution but excluded from configuration.
 * 
 *  For example,
 *  @see TestParentChild
 *  
 * @author Pinaki Poddar
 *
 */
public class CombinatorialTestHelper {
	private CombinationGenerator geneartor;
	private List<String> propertyKeys;
	private List currentOption;
	private BitSet runtimeKeys = new BitSet();
	
	private List[] combos;
	private int cursor;
	
	public CombinatorialTestHelper() {
		geneartor = new CombinationGenerator();
		propertyKeys = new ArrayList<String>();
		currentOption = null;
		runtimeKeys = new BitSet();
		combos = null;
		cursor = 0;
	}
	
	/**
	 * Generates the key-value property array as expected by its superclass
	 * by appending the current combinatorially generated properties.
	 * 
	 * The important side effect of this method is to set the current 
	 * configuration options.
	 * 
     * If no property is configured for combinatorial generation then returns
	 * the given list as it is.
	 * 
	 */
	Object[] setCombinatorialOption(Object[] props) {
		if (propertyKeys.isEmpty() || 
			propertyKeys.size() == runtimeKeys.cardinality())
			return props;
		
		if (combos == null) {
			combos = geneartor.generate();
			cursor = 0;
		}
		// Each non-runtime property contributes a key-value pair
		Object[] options = new Object[2*(propertyKeys.size()- 
				runtimeKeys.cardinality())];
		currentOption = combos[cursor++];
		int k = 0;
		for (int i = 0; i < propertyKeys.size(); i++) {
			if (runtimeKeys.get(i))
				continue;
			options[k++] = propertyKeys.get(i);
			options[k++] = currentOption.get(i);
		}
		if (props == null || props.length == 0)
			return options;
		
		Object[] newProps = new Object[props.length + options.length];
		System.arraycopy(props, 0, newProps, 0, props.length);
        System.arraycopy(options, 0, newProps, props.length, options.length);
		return newProps;
	}
	
	/**
	 * Adds options for the given configuration property.
	 */
	public void addOption(String property, Object[] options) {
		addOption(property, options, false);
	}
	
	/**
	 * Adds options for the given configuration property.
	 */
	public void addOption(String property, List options) {
		addOption(property, options, false);
	}
	
	/**
	 * Adds options for the given property.
	 * If runtime is true then this property is not added to configuration.
	 */
    public void addOption(String property, Object[] options, boolean runtime) {
		addOption(property, Arrays.asList(options), runtime);
	}

	/**
	 * Adds options for the given property.
	 * If runtime is true then this property is not added to configuration.
	 */
	public void addOption(String property, List options, boolean runtime) {
		if (geneartor == null) {
			geneartor = new CombinationGenerator();
		}
		if (propertyKeys == null) {
			propertyKeys = new ArrayList<String>();
		}
		if (!propertyKeys.contains(property)) {
			geneartor.addDimension(options);
			propertyKeys.add(property);
			if (runtime) runtimeKeys.set(propertyKeys.size()-1);
		}
	}

	/**
	 * Gets the value of current option for the given key.
	 * Raises exception if the given key is not an option.
	 */
	public Object getOption(String key) {
		int index = propertyKeys.indexOf(key);
		if (index == -1)
            throw new IllegalArgumentException("Unknown option " + key);
		return currentOption.get(index);
	}
	
	/**
	 * Gets the string value of current option for the given key.
	 * Raises exception if the given key is not an option.
	 */
	public String getOptionAsString(String key) {
		return getOption(key).toString();
	}

	/**
	 * Gets the values of the current options.
	 */
	public List getOptions() {
		return currentOption;
	}
	
	/**
	 * Gets the key and values of the current options as printable string.
	 */
	public String getOptionsAsString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i <propertyKeys.size(); i++) {
			String key = propertyKeys.get(i);
			if (!runtimeKeys.get(i))
                buf.append(key +  " : " + getOption(key)).append("\r\n");
		}
		for (int i = 0; i <propertyKeys.size(); i++) {
			String key = propertyKeys.get(i);
			if (runtimeKeys.get(i))
                buf.append("* " + key +  " : " + getOption(key)).append("\r\n");
		}
		return buf.toString();
	}
	
	/**
	 * Affirms if this receiver has more combinations.
	 */
	public boolean hasMoreCombination() {
		return cursor < combos.length;
	}
	
	/**
	 * Gets total number of combinations.
	 */
	public int getCombinationSize() {
		return geneartor == null ? 0 : geneartor.getSize();
	}
}
