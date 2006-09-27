package org.apache.openejb.test.entity.cmp;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestManager;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class CmpTestSuite extends org.apache.openejb.test.TestSuite{
       
    public CmpTestSuite(){
        super();
        this.addTest(new CmpJndiTests());
        this.addTest(new CmpHomeIntfcTests());
        this.addTest(new CmpEjbHomeTests());
        this.addTest(new CmpEjbObjectTests());    
        this.addTest(new CmpRemoteIntfcTests());
        this.addTest(new CmpHomeHandleTests());
        this.addTest(new CmpHandleTests());
        this.addTest(new CmpEjbMetaDataTests());
        //TODO:0:this.addTest(new CmpAllowedOperationsTests());
        this.addTest(new CmpJndiEncTests());
        this.addTest(new CmpRmiIiopTests());
        
    }

    public static junit.framework.Test suite() {
        return new CmpTestSuite();
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        Properties props = TestManager.getServer().getContextEnvironment();
        props.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        props.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");
        InitialContext initialContext = new InitialContext(props);
        
        /*[2] Create database table */
        TestManager.getDatabase().createEntityTable();
    }
    
    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     */
    protected void tearDown() throws Exception {
        /*[1] Drop database table */
        TestManager.getDatabase().dropEntityTable();
    }
}
