package org.apache.openejb.server;

public class Restart {

    public static void main(String[] args) throws Exception {
//        System.exit(new Start().start()?0:1);
        new Stop().stop();
        Thread.sleep(1000);
        new Start().start();

    }
}
