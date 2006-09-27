package org.apache.openejb.test;


/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class NamedTestCase extends NumberedTestCase{
        
    protected String testName;

    public NamedTestCase(String testName){
        super();
        this.testName = testName;
    }
    
    public NamedTestCase(String category, String testName){
        super();
        this.testName = testName;
    }

    public String name(){
        return testName;
    }

}
