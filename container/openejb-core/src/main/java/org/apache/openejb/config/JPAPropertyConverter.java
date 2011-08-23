package org.apache.openejb.config;

import org.apache.openejb.loader.SystemInstance;

import java.util.Properties;

/**
 * @author rmannibucau
 */
public final class JPAPropertyConverter {
    private JPAPropertyConverter() {
        // no-op
    }

    public static class Pair {
        private String key;
        private String value;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override public String toString() {
            return key + '=' + value;
        }
    }

    // TODO: manage more properties
    public static Pair toOpenJPAValue(String key, String value, Properties properties) {
        if (!Boolean.parseBoolean(SystemInstance.get().getProperty("openejb.convert-jpa-properties", "false"))) {
            return null;
        }

        if (key.startsWith("eclipselink.ddl-generation") && !properties.containsKey("openjpa.jdbc.SchemaFactory")) {
            if ("create-tables".equals(value)) {
                return new Pair("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            } else if ("drop-and-create-tables".equals("value")) {
                return new Pair("openjpa.jdbc.SynchronizeMappings", "buildSchema(SchemaAction='add,deleteTableContents')");
            }
        }
        return null;
    }
}
