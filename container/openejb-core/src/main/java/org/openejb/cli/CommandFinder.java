package org.openejb.cli;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CommandFinder {
	private String path;
	private Map classMap = Collections.synchronizedMap(new HashMap());
	
	public CommandFinder(String path) {
		this.path = path;
	}
	
    public Properties doFindCommandProperies(String key) throws IOException {
        String uri = path + key;

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);
        if (in == null) {
            in = CommandFinder.class.getClassLoader().getResourceAsStream(uri);
            if (in == null) {
                throw new IOException("Could not find command in : " + uri);
            }
        }

        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(in);
            Properties properties = new Properties();
            properties.load(reader);

            return properties;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    public Enumeration doFindCommands() throws IOException {
    	return Thread.currentThread().getContextClassLoader().getResources(path);
    }
}