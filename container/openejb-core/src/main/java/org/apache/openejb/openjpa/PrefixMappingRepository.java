package org.apache.openejb.openjpa;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.jdbc.PersistenceMappingDefaults;

public class PrefixMappingRepository extends MappingRepository {
    private String prefix;

    public PrefixMappingRepository() {
        setMappingDefaults(new PrefixMappingDefaults());
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void endConfiguration() {
        super.endConfiguration();
        Configurations.configureInstance(getMappingDefaults(), getConfiguration(), "jdbc.MappingDefaults");
        ((PrefixMappingDefaults) getMappingDefaults()).endConfiguration();
    }

    private class PrefixMappingDefaults extends PersistenceMappingDefaults {
        @Override
        public String getTableName(final ClassMapping cls, final Schema schema) {
            return prefix + super.getTableName(cls, schema);
        }

        @Override
        public String getTableName(final FieldMapping fm, final Schema schema) {
            return prefix + super.getTableName(fm, schema);
        }
    }
}
