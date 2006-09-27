package org.apache.openejb.test;

import java.util.Properties;

/**
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public interface TestDatabase {

    public void createEntityTable() throws java.sql.SQLException;
    public void dropEntityTable() throws java.sql.SQLException;
    public void createAccountTable() throws java.sql.SQLException;
    public void dropAccountTable() throws java.sql.SQLException;
    public void start() throws IllegalStateException;
    public void stop() throws IllegalStateException;
    public void init(Properties props) throws IllegalStateException;

}
