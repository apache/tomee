
/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.test.stateful;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class StatefulTestSuite extends junit.framework.TestCase{
        
    public StatefulTestSuite(String name){
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new StatefulJndiTests());
        suite.addTest(new StatefulPojoRemoteJndiTests());
        suite.addTest(new StatefulHomeIntfcTests());
        suite.addTest(new StatefulEjbHomeTests());
        suite.addTest(new StatefulEjbObjectTests());    
        suite.addTest(new StatefulRemoteIntfcTests());
        suite.addTest(new StatefulHomeHandleTests());
        suite.addTest(new StatefulHandleTests());
        suite.addTest(new StatefulEjbMetaDataTests());
        suite.addTest(new StatefulBeanTxTests());
        //suite.addTest(new StatefulAllowedOperationsTests());
        //suite.addTest(new BMTStatefulAllowedOperationsTests());
        suite.addTest(new StatefulJndiEncTests());
        suite.addTest(new StatefulRmiIiopTests());
        /* TO DO 
        suite.addTest(new StatefulEjbContextTests());
        suite.addTest(new BMTStatefulEjbContextTests());
        suite.addTest(new BMTStatefulEncTests());
        suite.addTest(new StatefulContainerManagedTransactionTests());
        */

        return suite;
    }

}
