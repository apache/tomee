package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.HttpListener;

import java.util.List;

/**
 * @author Romain Manni-Bucau
 */
public interface RsRegistry {
    List<String> createRsHttpListener(HttpListener listener, ClassLoader classLoader, String path);
    HttpListener removeListener(String context);
}
