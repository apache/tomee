package org.apache.openejb.core.ivm.naming;

public class ParsedName implements java.io.Serializable {
    final static int IS_EQUAL = 0;
    final static int IS_LESS = -1;
    final static int IS_GREATER = 1;

    String [] components;
    int pos = 0;
    int hashcode;

    public ParsedName(String path) {
        path = normalize(path);

        if (path == null || path.equals("/")) {

            components = new String[1];
            components[0] = "";
            hashcode = 0;
        } else if (path.length() > 0) {
            java.util.StringTokenizer st = new java.util.StringTokenizer(path, "/");
            components = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens() && i < components.length; i++)
                components[i] = st.nextToken();
            hashcode = components[0].hashCode();
        } else {

            components = new String[1];
            components[0] = "";
            hashcode = 0;
        }
    }

    public String getComponent() {
        return components[pos];
    }

    public boolean next() {
        if (components.length > pos + 1) {
            hashcode = components[++pos].hashCode();
            return true;
        } else {
            return false;// maintain position
        }
    }

    public void reset() {
        pos = 0;
        hashcode = components[0].hashCode();
    }

    public int compareTo(int otherHash) {
        if (hashcode == otherHash)
            return 0;
        else if (hashcode > otherHash)
            return 1;
        else
            return -1;
    }

    public int getComponentHashCode() {
        return hashcode;
    }

    public int compareTo(String other) {
        int otherHash = other.hashCode();
        return compareTo(otherHash);
    }

    public static void main(String [] args) {

        ParsedName name = new ParsedName("comp/env/jdbc/mydatabase");
        while (name.next()) System.out.println(name.getComponent());
    }

    public String toString() {
        if (components.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer(components[0]);
        for (int i = 1; i < components.length; ++i) {
            buffer.append('/');
            buffer.append(components[i]);
        }
        return buffer.toString();
    }

    /* A normal Unix pathname contains no duplicate slashes and does not end
with a slash.  It may be the empty string. */

    /* Normalize the given pathname, whose length is len, starting at the given
       offset; everything before this offset is already normal. */
    private String normalize(String pathname, int len, int off) {
        if (len == 0) return pathname;
        int n = len;
        while ((n > 0) && (pathname.charAt(n - 1) == '/')) n--;
        if (n == 0) return "/";
        StringBuffer sb = new StringBuffer(pathname.length());
        if (off > 0) sb.append(pathname.substring(0, off));
        char prevChar = 0;
        for (int i = off; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/')) continue;
            sb.append(c);
            prevChar = c;
        }
        return sb.toString();
    }

    /* Check that the given pathname is normal.  If not, invoke the real
       normalizer on the part of the pathname that requires normalization.
       This way we iterate through the whole pathname string only once. */
    private String normalize(String pathname) {
        int n = pathname.length();
        char prevChar = 0;
        for (int i = 0; i < n; i++) {
            char c = pathname.charAt(i);
            if ((prevChar == '/') && (c == '/'))
                return normalize(pathname, n, i - 1);
            prevChar = c;
        }
        if (prevChar == '/') return normalize(pathname, n, n - 1);
        return pathname;
    }

}
