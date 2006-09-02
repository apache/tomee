package org.openejb.test.stateless;

import junit.framework.TestSuite;

/**
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatelessLocalTestSuite extends junit.framework.TestCase{

    public StatelessLocalTestSuite(String name){
        super(name);
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new StatelessJndiTests());
        suite.addTest(new StatelessPojoRemoteJndiTests());
        suite.addTest(new StatelessPojoLocalJndiTests());
        suite.addTest(new StatelessHomeIntfcTests());
        suite.addTest(new StatelessEjbHomeTests() );
        suite.addTest(new StatelessEjbObjectTests());
        suite.addTest(new StatelessRemoteIntfcTests());
        suite.addTest(new StatelessHomeHandleTests());
        suite.addTest(new StatelessHandleTests());
        suite.addTest(new StatelessEjbMetaDataTests());
        suite.addTest(new StatelessAllowedOperationsTests());
        suite.addTest(new BMTStatelessAllowedOperationsTests());
        suite.addTest(new StatelessBeanTxTests());
        suite.addTest(new StatelessJndiEncTests());
        suite.addTest(new StatelessRmiIiopTests());
        suite.addTest(new MiscEjbTests());
        /* TO DO
        suite.addTest(new StatelessEjbContextTests());
        suite.addTest(new BMTStatelessEjbContextTests());
        suite.addTest(new BMTStatelessEncTests());
        suite.addTest(new StatelessContainerManagedTransactionTests());
        */
        return suite;
    }
}
