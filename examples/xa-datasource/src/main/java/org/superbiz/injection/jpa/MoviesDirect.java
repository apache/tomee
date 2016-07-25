package org.superbiz.injection.jpa;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Singleton
public class MoviesDirect {

    @Resource(name="moviesDatabaseUnmanaged")
    private DataSource ds;

    public int count() {
        try (final Connection connection = ds.getConnection()) {
            try (final PreparedStatement ps = connection.prepareStatement("select count(1) from movie")) {
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs != null && rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Unable to execute query against the database");
        }
    }
}
