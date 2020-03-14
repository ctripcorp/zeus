package com.ctrip.zeus.util;

import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by fanqq on 2016/8/11.
 */
public class AesEncryptionUtil {
    private DynamicPropertyFactory factory = DynamicPropertyFactory.getInstance();
    private static AesEncryptionUtil instance = new AesEncryptionUtil();
    private final static String DEFAULT_KEY = "a78406a1ab5759f7";
    private final static byte[] IV_BYTES = new byte[]{0x69, (byte) 0xD2, 0x55, (byte) 0xB8, 0x32, (byte) 0x9E, (byte) 0xAC, (byte) 0xD4, 0x0C, 0x2A, (byte) 0x9C, (byte) 0x8B, 0x68, 0x75, (byte) 0x87, 0x05};

    private static final Logger logger = LoggerFactory.getLogger(AesEncryptionUtil.class);


    public static AesEncryptionUtil getInstance() {
        return instance;
    }

    public String decrypt(String src) {
        String keyStr = factory.getStringProperty("Aes.Encryption.key", DEFAULT_KEY).get();
        if (keyStr.length() < 16) {
            keyStr += DEFAULT_KEY;
        }
        byte[] key = keyStr.substring(0, 16).getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV_BYTES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] result = cipher.doFinal(new Base64(-1).decode(src));
            return new String(result);
        } catch (Exception e) {
            logger.error("Cipher Error.Can not get Cipher.", e);
            return null;
        }
    }

    public String encrypt(String src) {
        String keyStr = factory.getStringProperty("Aes.Encryption.key", DEFAULT_KEY).get();
        if (keyStr.length() < 16) {
            keyStr += DEFAULT_KEY;
        }
        byte[] key = keyStr.substring(0, 16).getBytes();
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV_BYTES);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] result = cipher.doFinal(src.getBytes());
            return new Base64(-1).encodeToString(result);
        } catch (Exception e) {
            logger.error("Cipher Error.Can not get Cipher.", e);
            return null;
        }
    }
}
