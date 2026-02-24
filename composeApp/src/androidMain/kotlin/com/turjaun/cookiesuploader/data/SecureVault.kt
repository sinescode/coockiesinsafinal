package com.turjaun.cookiesuploader.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import java.security.MessageDigest
import javax.crypto.Mac

class SecureVault {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "CookieUploaderKey"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val INTERNAL_SALT = "SKYSYS_PRO_SALT_99821_Bokachondro985"
        private const val ITERATIONS = 2000
        private const val KEY_LENGTH = 256

        private fun getSecretKey(password: String): SecretKey {
            // Use PBKDF2 for key derivation (compatible with Dart crypto)
            val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(
                password.toCharArray(), 
                INTERNAL_SALT.toByteArray(), 
                ITERATIONS, 
                KEY_LENGTH
            )
            val tmp = factory.generateSecret(spec)
            return SecretKeySpec(tmp.encoded, "AES")
        }

        fun pack(plainText: String, password: String): String {
            val key = getSecretKey(password)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV + encrypted data
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            
            // Format: base64(iv:encrypted)
            val result = "${Base64.encodeToString(iv, Base64.NO_WRAP)}:${Base64.encodeToString(encrypted, Base64.NO_WRAP)}"
            return Base64.encodeToString(result.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }

        fun unpack(packedData: String, password: String): String {
            return try {
                val decoded = String(Base64.decode(packedData, Base64.NO_WRAP), Charsets.UTF_8)
                val parts = decoded.split(":")
                if (parts.size != 2) return "ERROR: Invalid file format"

                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
                
                val key = getSecretKey(password)
                val cipher = Cipher.getInstance(AES_MODE)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
                
                val decrypted = cipher.doFinal(encrypted)
                String(decrypted, Charsets.UTF_8)
            } catch (e: Exception) {
                "ERROR: Decryption failed (Wrong password or corrupted file)"
            }
        }
    }
}