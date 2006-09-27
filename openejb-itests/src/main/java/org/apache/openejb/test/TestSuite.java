package org.apache.openejb.test;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestResult;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class TestSuite extends junit.framework.TestSuite {
    
    public TestSuite() {
        super();
    }
    
    /**
     * Runs the tests and collects their result in a TestResult.
     */
    public void run(TestResult result) {
        try{
            setUp();

            for (Enumeration e= tests(); e.hasMoreElements(); ) {
                if ( result.shouldStop() ) break;
                Test test= (Test)e.nextElement();
                test.run(result);
            }

            tearDown();
        } catch (Exception e) {
            result.addError(this, e);
        }
    }

    protected void setUp() throws Exception{
    }

    protected void tearDown() throws Exception{
    }

}
    

