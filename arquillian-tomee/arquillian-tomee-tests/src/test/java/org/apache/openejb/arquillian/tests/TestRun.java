package org.apache.openejb.arquillian.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class TestRun {

	public static void run(ServletRequest req, ServletResponse resp, Object obj) throws IOException {
		final Class<?> clazz = obj.getClass();
        final Method[] methods = clazz.getMethods();

        resp.setContentType("text/plain");
        final PrintWriter writer = resp.getWriter();

        for (Method method : methods) {
            if (method.getName().startsWith("test")) {

                writer.print(method.getName());

                writer.print("=");

                try {
                    method.invoke(obj);
                    writer.println("true");
                } catch (Throwable e) {
                    writer.println("false");
                    writer.println("");
                    writer.println("STACKTRACE");
                    writer.println("");
                    e.printStackTrace(writer);
                }
            }
        }
	}

}
