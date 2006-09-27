package org.apache.openejb.loader;

public class ClassPathFactory {
    public static ClassPath createClassPath(String name) {
        if (name.equalsIgnoreCase("tomcat")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-common")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-system")) {
            return new TomcatClassPath();
        } else if (name.equalsIgnoreCase("tomcat-webapp")) {
            return new WebAppClassPath();
        } else if (name.equalsIgnoreCase("bootstrap")) {
            return new SystemClassPath();
        } else if (name.equalsIgnoreCase("system")) {
            return new SystemClassPath();
        } else if (name.equalsIgnoreCase("thread")) {
            return new ContextClassPath();
        } else if (name.equalsIgnoreCase("context")) {
            return new ContextClassPath();
        } else {
            return new ContextClassPath();
        }
    }
}
