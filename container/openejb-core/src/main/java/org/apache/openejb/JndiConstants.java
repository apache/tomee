package org.apache.openejb;

/**
 * @author rmannibucau
 */
public interface JndiConstants {
    public static final String JAVA_OPENEJB_NAMING_CONTEXT = "openejb/";
    public static final String OPENEJB_RESOURCE_JNDI_PREFIX = JAVA_OPENEJB_NAMING_CONTEXT + "Resource/";
    public static final String PERSISTENCE_UNIT_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "PersistenceUnit/";
    public static final String VALIDATOR_FACTORY_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "ValidatorFactory/";
    public static final String VALIDATOR_NAMING_CONTEXT = JAVA_OPENEJB_NAMING_CONTEXT + "Validator/";
}
