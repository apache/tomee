package org.apache.openejb.test;

import junit.framework.AssertionFailedError;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class TestFailureException extends Exception{

    public AssertionFailedError error;

    public TestFailureException(AssertionFailedError afe){
        error = afe;
    }
    
    public String getMessage() {
	if (error == null) {
	    return super.getMessage();
	} else {
	    return super.getMessage() + "; nested exception is: \n\t" +
		error.toString();
	}
    }

}
