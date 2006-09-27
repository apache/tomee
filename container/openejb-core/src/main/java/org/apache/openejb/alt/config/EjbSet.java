package org.apache.openejb.alt.config;

import java.util.Vector;

import org.apache.openejb.jee.EjbJar;

public class EjbSet {

    private final Vector failures = new Vector();
    private final Vector warnings = new Vector();
    private final Vector errors = new Vector();

    private final String jarPath;
    private final EjbJar jar;
    private final Bean[] beans;

    private final ClassLoader classLoader;

    public EjbSet(String jarPath, EjbJar jar, Bean[] beans, ClassLoader classLoader) {
        this.jarPath = jarPath;
        this.jar = jar;
        this.beans = beans;
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public EjbJar getJar() {
        return jar;
    }

    public void addWarning(ValidationWarning warning) {
        warnings.addElement(warning);
    }

    public void addFailure(ValidationFailure failure) {
        failures.addElement(failure);
    }

    public void addError(ValidationError error) {
        errors.addElement(error);
    }

    public ValidationFailure[] getFailures() {
        ValidationFailure[] tmp = new ValidationFailure[failures.size()];
        failures.copyInto(tmp);
        return tmp;
    }

    public ValidationWarning[] getWarnings() {
        ValidationWarning[] tmp = new ValidationWarning[warnings.size()];
        warnings.copyInto(tmp);
        return tmp;
    }

    public ValidationError[] getErrors() {
        ValidationError[] tmp = new ValidationError[errors.size()];
        errors.copyInto(tmp);
        return tmp;
    }

    public boolean hasWarnings() {
        return warnings.size() > 0;
    }

    public boolean hasFailures() {
        return failures.size() > 0;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public Bean[] getBeans() {
        return beans;
    }

    public EjbJar getEjbJar() {
        return jar;
    }

    public String getJarPath() {
        return jarPath;
    }
}
