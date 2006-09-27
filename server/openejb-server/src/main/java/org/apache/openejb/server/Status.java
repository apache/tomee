package org.apache.openejb.server;

import java.io.OutputStream;
import java.net.Socket;

public class Status {

    public static void main(String[] args) {
//        System.exit(new Start().start()?0:1);
        new Status().status();
    }

    public boolean status() {
        if (!connect()) {
            System.out.println(":: server is stopped ::");
            return false;
        } else {
            System.out.println(":: server is started ::");
            return true;
        }
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        try {
            Socket socket = new Socket("localhost", 4201);
            OutputStream out = socket.getOutputStream();
        } catch (Exception e) {
            if (tries < 2) {
                return false;
            } else {
                try {
                    Thread.sleep(2000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }

        return true;
    }
}
