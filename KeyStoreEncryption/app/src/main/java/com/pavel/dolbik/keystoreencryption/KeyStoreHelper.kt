package com.pavel.dolbik.keystoreencryption

import android.annotation.TargetApi
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.CertificateEncodingException
import java.security.spec.RSAKeyGenParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


class KeyStoreHelper {

    companion object {

        const val ALIAS         = "TEST_PROJECT"
        const val TYPE_RSA      = "RSA"
        const val BLOCKING_MODE = "NONE"
        const val PADDING_TYPE  = "PKCS1Padding"
        const val KEY_STORE     = "AndroidKeyStore"


        /** KeyStore доступен с API >18 и имеет разную реализацию. <br>
         *  KeyStore is available with API> 18 and has a different implementation. */
        fun createKeys() {
            if (!isSigningKey()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createKeysM()
                } else {
                    createKeysJBMR2()
                }
            }
        }


        /** Если ключ с определенным alias существует, возращаем true иначе false. <br>
         *  If Key with the default alias exists, returns true, else false. */
        private fun isSigningKey(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                try {
                    val keyStore = KeyStore.getInstance(KEY_STORE)
                    keyStore.load(null)
                    return keyStore.containsAlias(ALIAS)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            } else {
                return false
            }
        }


        @TargetApi(Build.VERSION_CODES.M)
        private fun createKeysM() {
            try {
                // Задаем свойства для ключей (скрок действия и прочее).
                // Set properties for keys (validity period and so on).
                val spec = KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                        .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4))
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                        .setUserAuthenticationRequired(false)
                        .build()

                // Алгоритм для шифрования RSA, ключи будут сохранены в AndroidKeyStore.
                // The algorithm for encrypting RSA, the keys will be stored in AndroidKeyStore.
                val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE)

                // Устанавливаем свойства для ключей.
                // Set the properties for the keys.
                keyPairGenerator.initialize(spec)

                // Генерируем пару public/private ключей.
                // Generate a pair of public/private keys.
                keyPairGenerator.genKeyPair()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("QA", "Error in createKeysM() ${e.localizedMessage}")
            }
        }


        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        private fun createKeysJBMR2() {
            try {
                val start = Calendar.getInstance()
                val end   = Calendar.getInstance()
                end.add(Calendar.YEAR, 30)
                val spec = KeyPairGeneratorSpec.Builder(MyApplication.instance)
                        .setAlias(ALIAS)
                        .setSubject(X500Principal("CN=" + ALIAS))
                        .setSerialNumber(BigInteger.valueOf(Math.abs(ALIAS.hashCode()).toLong()))
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
                val keyPairGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEY_STORE)
                keyPairGenerator.initialize(spec)
                keyPairGenerator.generateKeyPair()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("QA", "Error in createKeysJBMR2() ${e.localizedMessage}")
            }
        }


        /** Возвращаем private key. <br> Returns the private key. */
        fun getSigningKey(): String? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                if (getPrivateKeyEntry() != null) {
                    val certificate = getPrivateKeyEntry()?.certificate
                    if (certificate != null) {
                        try {
                            return Base64.encodeToString(certificate.encoded, Base64.NO_WRAP)
                        } catch (e: CertificateEncodingException) {
                            e.printStackTrace()
                            return null
                        }
                    }
                }

                return null
            } else {
                return null
            }
        }


        private fun getPrivateKeyEntry(): KeyStore.PrivateKeyEntry?  {
            try {
                val keyStore = KeyStore.getInstance(KEY_STORE)
                keyStore.load(null)
                val entry = keyStore.getEntry(ALIAS, null)

                if (entry == null) {
                    Log.e("QA", "No key found under alias: $ALIAS")
                    return null
                }

                if (entry !is KeyStore.PrivateKeyEntry) {
                    Log.e("QA", "Not an instance of a PrivateKeyEntry")
                    return null
                }

                return entry
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("QA", "Error in getPrivateKeyEntry() ${e.localizedMessage}")
                return null
            }
        }


        fun encrypt(planText: String): String {
            try {
                val publicKey = getPrivateKeyEntry()?.certificate?.publicKey
                val cipher = getCipher()
                cipher?.init(Cipher.ENCRYPT_MODE, publicKey)
                return Base64.encodeToString(cipher?.doFinal(planText.toByteArray()), Base64.NO_WRAP)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }


        fun decrypt(cipherText: String): String {
            try {
                val privateKey = getPrivateKeyEntry()!!.privateKey
                val cipher = getCipher()
                cipher!!.init(Cipher.DECRYPT_MODE, privateKey)
                return String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }


        private fun getCipher(): Cipher? {
            try {
                return Cipher.getInstance(String.format("%s/%s/%s", TYPE_RSA, BLOCKING_MODE, PADDING_TYPE))
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }


    }

}