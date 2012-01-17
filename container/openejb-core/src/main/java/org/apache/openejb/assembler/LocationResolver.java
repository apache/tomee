package org.apache.openejb.assembler;

public interface LocationResolver {
    String resolve(final String rawLocation) throws Exception;
}
