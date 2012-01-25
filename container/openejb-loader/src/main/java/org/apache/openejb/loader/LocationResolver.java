package org.apache.openejb.loader;

public interface LocationResolver {
    String resolve(final String rawLocation) throws Exception;
}
