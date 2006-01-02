package org.openejb.loader;

import java.util.Hashtable;

public interface Loader {

    public void load(Hashtable env) throws Exception;

}

