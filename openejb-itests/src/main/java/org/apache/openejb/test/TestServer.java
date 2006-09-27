package org.apache.openejb.test;

import java.util.Properties;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public interface TestServer {

    public void init(Properties props);
    
    public void start();

    public void stop();

    public Properties getContextEnvironment();

}
