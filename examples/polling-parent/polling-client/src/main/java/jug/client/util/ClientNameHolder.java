package jug.client.util;

public class ClientNameHolder {
    private static String current = null;

    public static String getCurrent() {
        return current;
    }

    public static void setCurrent(String current) {
        ClientNameHolder.current = current;
    }
}
