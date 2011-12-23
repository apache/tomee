package org.superbiz.calculator.client;

public final class RemoteClientMain {
    private RemoteClientMain() {
        // no-op
    }

    public static void main(String[] args) throws Exception {
        ClientUtil.invoke();
    }
}
