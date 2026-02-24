package com.turjaun.cookiesuploader.security

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Base64
import javax.crypto.SecretKeyFactory

class SecureVault {
    
    companion object {
        private const val INTERNAL_SALT = "SKYSYS_PRO_SALT_99821_Bokachondro985"
        private const val PBKDF2_ITERATIONS = 2000
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 12 // GCM recommended IV length
        private const val GCM_TAG_LENGTH = 128
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    }
    
    private fun deriveKey(password: String): SecretKey {
        val salt = INTERNAL_SALT.toByteArray()
        val keySpec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH
        )
        val secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val keyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
    
    fun pack(plainText: String, password: String): String {
        try {
            val key = deriveKey(password)
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)
            
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            
            // Combine IV + encrypted data
            val combined = iv + encryptedBytes
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            return "ERROR: Encryption failed - ${e.message}"
        }
    }
    
    fun unpack(packedData: String, password: String): String {
        return try {
            val combined = Base64.getDecoder().decode(packedData)
            
            // Extract IV and encrypted data
            val iv = combined.copyOfRange(0, IV_LENGTH)
            val encryptedBytes = combined.copyOfRange(IV_LENGTH, combined.size)
            
            val key = deriveKey(password)
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "ERROR: Decryption failed (Wrong password or corrupted file)"
        }
    }
}