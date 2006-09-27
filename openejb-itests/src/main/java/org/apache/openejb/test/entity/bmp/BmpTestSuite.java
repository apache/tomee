package org.apache.openejb.test.entity.bmp;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestManager;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class BmpTestSuite extends org.apache.openejb.test.TestSuite{
       
    public BmpTestSuite(){
        super();
        this.addTest(new BmpJndiTests());
        this.addTest(new BmpHomeIntfcTests());
        this.addTest(new BmpEjbHomeTests());
        this.addTest(new BmpEjbObjectTests());    
        this.addTest(new BmpRemoteIntfcTests());
        this.addTest(new BmpHomeHandleTests());
        this.addTest(new BmpHandleTests());
        this.addTest(new BmpEjbMetaDataTests());
        this.addTest(new BmpAllowedOperationsTests());
        this.addTest(new BmpJndiEncTests());
        this.addTest(new BmpRmiIiopTests());
        
    }

    public static junit.framework.Test suite() {
        return new BmpTestSuite();
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
