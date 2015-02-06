package org.apache.openejb.util;

import org.apache.openejb.cipher.PasswordCipher;

public class ReversePasswordCipher implements PasswordCipher {
    @Override
    public char[] encrypt(final String s) {
        return new StringBuffer().append(s).reverse().toString().toCharArray();
    }

    @Override
    public String decrypt(char[] chars) {
        return new StringBuffer().append(chars).reverse().toString();
    }
}
