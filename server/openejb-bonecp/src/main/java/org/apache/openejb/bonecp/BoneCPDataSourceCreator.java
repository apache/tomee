package org.apache.openejb.bonecp;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;

public class BoneCPDataSourceCreator extends PoolDataSourceCreator {
    @Override
    protected void doDestroy(final DataSource dataSource) throws Throwable {
        ((BoneCPDataSource) dataSource).close();
    }

    @Override
    public DataSource pool(final String name, final DataSource ds, final Properties properties) {
        if (properties.containsKey("url")) {
            properties.setProperty("jdbcUrl", properties.getProperty("url"));
        }

        final BoneCPConfig config;
        final BoneCP pool;
        try {
            config = new BoneCPConfig(properties);
            pool = new BoneCP(config);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return new BoneCPDataSourceProvidedPool(pool);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        final BoneCPDataSource ds = build(BoneCPDataSource.class, properties);
        if (ds.getDriverClass() == null || ds.getDriverClass().isEmpty()) {
            ds.setDriverClass(driver);
        }
        if (ds.getPoolName() == null || ds.getPoolName().isEmpty()) {
            ds.setPoolName(name);
        }
        return ds;
    }

    private static final class BoneCPDataSourceProvidedPool extends BoneCPDataSource {
        private static final Field POOL_FIELD;
        static {
            try {
                POOL_FIELD = BoneCPDataSource.class.getDeclaredField("pool");
                POOL_FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }

        public BoneCPDataSourceProvidedPool(final BoneCP pool) {
            try {
                POOL_FIELD.set(this, pool);
            } catch (IllegalAccessException e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }
}
