package org.apache.openejb.loader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Loader {

    public void init(ServletConfig servletConfig) throws ServletException;

    void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}

