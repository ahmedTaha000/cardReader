package com.arana.cardreader.nfc


import android.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.nio.charset.StandardCharsets
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESEncryption {

    // Ensure Bouncy Castle is added as a security provider
    // Add implementation 'org.bouncycastle:bcprov-jdk15to18:1.70' to your build.gradle
    init {
        Security.removeProvider("BC")
        Security.addProvider(BouncyCastleProvider())
    }

    private const val SECRET_KEY = "ahKw72Akshf@Hsks" // 16 characters
    private const val INIT_VECTOR = "e16ce888a20dadb8" // 16 characters

    /**
     * Decrypts the given string or returns empty if null/empty.
     */
    fun decrypt(encrypted: String?): String {
        if (encrypted.isNullOrEmpty()) return ""
        return try {
            val keySpec: SecretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(INIT_VECTOR.toByteArray(StandardCharsets.UTF_8))

            // "BC" provider is required for PKCS7Padding support in some Android versions/configs
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

            val decodedBytes = Base64.decode(encrypted, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)

            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            // Return empty or original text depending on fallback strategy
            ""
        }
    }

    /**
     * Encrypts the given string.
     */
    fun encrypt(value: String?): String {
        if (value.isNullOrEmpty()) return "" // Handle empty case appropriately

        return try {
            val keySpec: SecretKey = SecretKeySpec(SECRET_KEY.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivSpec = IvParameterSpec(INIT_VECTOR.toByteArray(StandardCharsets.UTF_8))
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            val encryptedBytes = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

