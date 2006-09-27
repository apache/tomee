package org.apache.openejb.test.stateful;

import java.util.Properties;

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestManager;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public abstract class StatefulTestClient extends org.apache.openejb.test.NamedTestCase{
    
    protected InitialContext initialContext;

    protected EJBMetaData       ejbMetaData;
    protected HomeHandle        ejbHomeHandle;
    protected Handle            ejbHandle;
    protected Integer           ejbPrimaryKey;

    public StatefulTestClient(String name){
        super("Stateful."+name);
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "STATEFUL_test00_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "STATEFUL_test00_CLIENT");
        
        initialContext = new InitialContext(properties);
    }
    
}
