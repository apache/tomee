package org.apache.openejb.junit;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class DescriptorTest {
    @Module
    @Descriptors(@Descriptor(name = "resources.xml", path = "descriptor-resources.xml"))
    public EjbJar classes() {
        return new EjbJar();
    }

    @Resource
    private DataSource ds;

    @Test
    public void checkDDWasHere() throws SQLException {
        assertNotNull(ds);
        final Connection connection = ds.getConnection();
        final String url = connection.getMetaData().getURL();
        assertEquals("jdbc:hsqldb:mem:descriptors", url);
        connection.close();
    }
}
