package org.apache.openejb.util.proxy;

public interface LocalBeanProxyGenerator {
	public byte[] generateProxy(Class<?> clsToProxy) throws ProxyGenerationException;

	public byte[] generateProxy(Class<?> clsToProxy, String targetClassName) throws ProxyGenerationException;
}
