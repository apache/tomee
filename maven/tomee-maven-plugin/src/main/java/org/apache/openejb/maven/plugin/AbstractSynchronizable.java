package org.apache.openejb.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSynchronizable {
    protected int updateInterval;
    protected List<String> extensions;
    protected List<String> updateOnlyExtensions;
    protected String regex;
    protected Map<File, File> updates;

    public abstract Map<File, File> updates();

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public List<String> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<String>();
        }
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public List<String> getUpdateOnlyExtenions() {
        if (updateOnlyExtensions == null) {
            updateOnlyExtensions = new ArrayList<String>();
        }
        return updateOnlyExtensions;
    }

    public void setUpdateOnlyExtensions(List<String> updateOnlyExtensions) {
        this.updateOnlyExtensions = updateOnlyExtensions;
    }
}
