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

import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;


/**
 * Adds ability to run tests under combinations of options.
 * 
 * @author Pinaki Poddar
 *
 */
public abstract class CombinatorialPersistenceTestCase 
	extends SQLListenerTestCase {
	
	protected static CombinatorialTestHelper helper;
	
	public void setUp(Object...props) {
		super.setUp(getHelper().setCombinatorialOption(props));
	}
	
	@Override
	public int countTestCases() {
        return super.countTestCases() * getHelper().getCombinationSize();
	}
	   
    @Override
    public void runBare() throws Throwable {
    	Map<String, Throwable> errors = new HashMap<String, Throwable>();
        Map<String, AssertionFailedError> failures =
            new HashMap<String, AssertionFailedError>();
    	do  {
    		try {
    			super.runBare();
    		} catch (Throwable t) {
    			if (t instanceof AssertionFailedError) {
                    failures.put(getHelper().getOptionsAsString(),
                            (AssertionFailedError)t);
                    testResult.addFailure(this, (AssertionFailedError)t);
    			} else {
    				errors.put(getHelper().getOptionsAsString(), t);
    				testResult.addError(this, t);
    			}
    		}
    	} while (getHelper().hasMoreCombination());
    	
    	if (testResult.errorCount() + testResult.failureCount() > 0) {
    		if (!failures.isEmpty())
                System.err.println(failures.size() + " assertion failures");
    		for (String o : failures.keySet()) {
    			System.err.println("Combination:\r\n" + o);
    			failures.get(o).printStackTrace();
    		}
    		if (!errors.isEmpty())
    			System.err.println(errors.size() + " errors");
    		for (String o : errors.keySet()) {
    			System.err.println("Combination:\r\n" + o);
    			errors.get(o).printStackTrace();
    		}
            throw new Throwable(getName() + ": "
                + getHelper().getCombinationSize() + " combinations, "
                + errors.size() + " errors, " + failures.size()
                + " failures\r\n"
                + "Stack trace for each error/failure is printed on console");
    	}
    }
    
    
   
    public static CombinatorialTestHelper getHelper() {
		if (helper == null)
			helper = new CombinatorialTestHelper();
		return helper;
    }
    
    public void assertSQL(String sqlExp) {
    	try {
    		super.assertSQL(sqlExp);
    	} catch (AssertionFailedError e) {
            String newMessage = "Combination\r\n"
                + getHelper().getOptionsAsString()
                + " failed \r\n " + e.getMessage();
    		fail(newMessage);
    	}
    }

}
