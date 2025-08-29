package com.hqumath.demo.utils;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import io.reactivex.exceptions.Exceptions;


/**
 * AES加密
 */
public class AESUtils {
    /**
     * 密钥
     */
    private static final String KEY = "123";
    /**
     * 加密模式
     */
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     */
    public static String encrypt(String input) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] bytes = cipher.doFinal(input.getBytes());
            return Base64Util.encode(bytes);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }

    }

    /**
     * 解密
     */
    public static String decrypt(String input) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytes = Base64Util.decode(input);
            bytes = cipher.doFinal(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }
}
