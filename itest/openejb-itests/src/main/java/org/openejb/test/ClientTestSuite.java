package org.openejb.test;

import junit.framework.TestSuite;

import org.openejb.test.entity.bmp.BmpTestSuite;
import org.openejb.test.entity.cmp.CmpTestSuite;
import org.openejb.test.stateful.StatefulTestSuite;
import org.openejb.test.stateless.StatelessTestSuite;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class ClientTestSuite extends junit.framework.TestCase {
    
    public ClientTestSuite(String name){
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( StatelessTestSuite.suite() );
        suite.addTest( StatefulTestSuite.suite() );
        suite.addTest( BmpTestSuite.suite() );
        suite.addTest( CmpTestSuite.suite() );
        return suite;
    }
}
