package org.apache.openejb.util;

public class Enumerator implements java.io.Serializable, java.util.Enumeration {

    private java.util.Vector _list;
    private int _index;

    public Enumerator(java.util.Vector list) {
        _list = (java.util.Vector) list.clone();
        _index = 0;
    }

    public boolean hasMoreElements() {
        return (_index < _list.size());
    }

    public Object nextElement() {
        return _list.elementAt(_index++);
    }
}
