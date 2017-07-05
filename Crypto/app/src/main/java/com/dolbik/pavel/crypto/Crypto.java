package com.dolbik.pavel.crypto;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypto {

    /** Минимальное значение рекомендуемое для PKCS#5. <br>
     *  Minimum values recommended by PKCS#5. <br>
     *  https://ru.wikipedia.org/wiki/PKCS */
    private static final int ITERATION_COUNT = 1000;

    /** 256-bits for AES-256, 128-bits for AES-128, etc */
    private static final int KEY_LENGTH = 256;

    /** Алгоритм шифрования PBKDF2. <br>
     *  Encryption Algorithm PBKDF2.
     *  https://ru.wikipedia.org/wiki/PBKDF2 */
    private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";

    /** Имя преобразования для создания шифра. <br>
     *  The name of the transformation to create a cipher. */
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    /** Длина соли исходя из длины ключа (256 / 8 = 32). <br>
     *  Salt length depend from key length (256 / 8 = 32). */
    private static final int PKCS5_SALT_LENGTH = 32;

    private static final String DELIMITER = "]";

    private static final SecureRandom random = new SecureRandom();



    /** @param plaintext текст, которы будет зашифрован. Text to be encrypted.
     *  @param password  пароль для ключа шифрования. The password for the encryption key. */
    public static String encrypt(String plaintext, String password) {
        byte[] salt  = generateSalt();
        SecretKey key = deriveKey(password, salt);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            byte[] iv = generateIv(cipher.getBlockSize());
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            if(salt != null) {
                return String.format("%s%s%s%s%s",
                        toBase64(salt),
                        DELIMITER,
                        toBase64(iv),
                        DELIMITER,
                        toBase64(cipherText));
            }

            return String.format("%s%s%s",
                    toBase64(iv),
                    DELIMITER,
                    toBase64(cipherText));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    /** @param ciphertext зашифрованный текст, который будет подвержен расшифровке.
     *                    Encrypted text, which will be subject to decoding.
     *  @param password пароль для ключа шифрования. The password for the encryption key.*/
    public static String decrypt(String ciphertext, String password) {
        String[] fields = ciphertext.split(DELIMITER);
        if(fields.length != 3) {
            throw new IllegalArgumentException("Invalid encypted text format");
        }
        byte[] salt        = fromBase64(fields[0]);
        byte[] iv          = fromBase64(fields[1]);
        byte[] cipherBytes = fromBase64(fields[2]);
        SecretKey key = deriveKey(password, salt);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            byte[] plaintext = cipher.doFinal(cipherBytes);
            return new String(plaintext, "UTF-8");
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


    /** Используется для удлинения строки пароля, чтобы увеличить сложность взлома.
     *  Salt is random data that is used as an additional input to a one-way function that "hashes" a password or passphrase.*/
    private static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }


    /** Вектор инициализации - обеспечивает семантическую безопастность,
     *  не позволяет вывести отношения между сегментами зашифрованного сообщения. <br>
     *  Initialization vector - to achieve semantic security, does not allow an attacker
     *  to infer relationships between segments of the encrypted message. */
    private static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }


    /** Получаем ключ по паролю. <br>
     *  Use this to derive the key from the password */
    private static SecretKey deriveKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    /** Формируется конечный буквенно-цифровой текст на основе латинского алфавита. <br>
     *  Represent binary data in an ASCII string format*/
    private static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }


    private static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

}
