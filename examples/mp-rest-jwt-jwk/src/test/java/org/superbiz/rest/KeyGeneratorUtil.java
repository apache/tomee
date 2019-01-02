package org.superbiz.rest;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.UUID;

public class KeyGeneratorUtil {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        generateKeyPair("RSA", 2048);
    }

    public static void generateKeyPair(String keyAlgorithm, int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(keyAlgorithm); // RSA
        kpg.initialize(keySize); // 2048
        KeyPair kp = kpg.generateKeyPair();

        System.out.println("-----BEGIN PRIVATE KEY-----");
        System.out.println(Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded()));
        System.out.println("-----END PRIVATE KEY-----");
        System.out.println("-----BEGIN PUBLIC KEY-----");
        System.out.println(Base64.getMimeEncoder().encodeToString(kp.getPublic().getEncoded()));
        System.out.println("-----END PUBLIC KEY-----");

        RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();

        RSAKey jwk = new RSAKey.Builder(publicKey)
                .privateKey((RSAPrivateKey) kp.getPrivate())
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build();

        System.out.println(jwk.toJSONObject().toJSONString());
    }
}
