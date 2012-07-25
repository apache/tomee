package org.superbiz.jaxws;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.jws.WebService;

@Lock(LockType.READ)
@Singleton
@WebService
public class Rot13 {
    public String rot13(final String in) {
        final StringBuilder builder = new StringBuilder(in.length());
        for (int b : in.toCharArray()) {
            int cap = b & 32;
            b &= ~cap;
            if (Character.isUpperCase(b)) {
                b = (b - 'A' + 13) % 26 + 'A';
            } else {
                b = cap;
            }
            b |= cap;
            builder.append((char) b);
        }
        return builder.toString();
    }
}
