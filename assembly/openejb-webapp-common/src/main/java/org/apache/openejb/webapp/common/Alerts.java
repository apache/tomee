package org.apache.openejb.webapp.common;

import java.util.ArrayList;
import java.util.List;

public class Alerts {

    private final List<String> errors = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> infos = new ArrayList<String>();

    public void reset() {
        errors.clear();
        warnings.clear();
        infos.clear();
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String message) {
        errors.add(message);
    }

    public void addError(String message, Exception e) {
        // todo add exception somehow
        System.out.println(message);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void addWarning(String message) {
        System.out.println(message);
    }

    public boolean hasInfos() {
        return !infos.isEmpty();
    }

    public List<String> getInfos() {
        return infos;
    }

    public void addInfo(String message) {
        infos.add(message);
    }
}