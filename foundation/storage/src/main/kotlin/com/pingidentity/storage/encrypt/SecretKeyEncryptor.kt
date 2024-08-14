/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.storage.encrypt

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import com.pingidentity.android.ContextProvider
import com.pingidentity.logger.LoggerContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Configuration class for SecretKeyEncryptor.
 * It contains various properties that can be set to configure the encryption process.
 */
class SecretKeyEncryptorConfig {
    lateinit var context: Context
    lateinit var keyAlias: String
    var throwWhenEncryptError = true
    var keySize = 256
    var ivLength = 12
    var invalidatedByBiometricEnrollment = true
    var logger = LoggerContext.get()

    /**
     * Initializes the context if it's not already initialized.
     */
    fun init() {
        if (!this::context.isInitialized) {
            context = ContextProvider.context
        }
    }
}

private const val AES_GCM_NO_PADDING = "AES/GCM/NOPADDING"
private const val HMAC_SHA256 = "HmacSHA256"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"

/**
 * An encryptor that uses Android's SecretKey to encrypt and decrypt data.
 * It uses AES/GCM/NoPadding as the cipher and HmacSHA256 for the MAC.
 */
class SecretKeyEncryptor(block: SecretKeyEncryptorConfig.() -> Unit = {}) : Encryptor {
    val config = SecretKeyEncryptorConfig().apply(block)
    private val lock = Mutex()
    private val logger = config.logger
    private val mac: Mac
    private val macLength: Int

    init {
        config.init()
        mac = Mac.getInstance(HMAC_SHA256)
        val sk: SecretKey =
            SecretKeySpec(config.keyAlias.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256)
        mac.init(sk)
        macLength = mac.macLength
    }

    /**
     * Encrypts the given data.
     * It uses a lock to ensure thread safety.
     * @param data The data to encrypt.
     * @return The encrypted data.
     */
    override suspend fun encrypt(data: ByteArray): ByteArray = lock.withLock {
        withRetry(byteArrayOf(), {
            logger.e("Failed to encrypt data, retrying...", it)
            keyStore.deleteEntry(config.keyAlias)
        }) {
            logger.d("Encrypting data...")
            var encryptedData: ByteArray
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING)
            val iv: ByteArray = init(cipher)
            encryptedData = cipher.doFinal(data)
            val mac = mac.doFinal(encryptedData)
            encryptedData = mac + iv + encryptedData
            return encryptedData
        }
    }

    /**
     * Decrypts the given data.
     * It uses a lock to ensure thread safety.
     * @param data The data to decrypt.
     * @return The decrypted data.
     */
    override suspend fun decrypt(data: ByteArray): ByteArray = lock.withLock {

        try {
            logger.d("Decrypting data...")
            val cipher =
                Cipher.getInstance(AES_GCM_NO_PADDING)
            val ivLength: Int = config.ivLength
            val encryptedDataLength: Int = data.size - ivLength - macLength
            val macFromMessage = getArraySubset(data, 0, macLength)
            val iv = getArraySubset(data, macLength, ivLength)
            val encryptedData = getArraySubset(data, macLength + ivLength, encryptedDataLength)
            val mac = mac.doFinal(encryptedData)

            if (!mac.contentEquals(macFromMessage)) {
                throw RuntimeException("MAC signature could not be verified")
            }
            val ivParams: AlgorithmParameterSpec = GCMParameterSpec(128, iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey(), ivParams)
            return cipher.doFinal(encryptedData)
        } catch (e: Throwable) {
            logger.e("Failed to decrypt data", e)
            return byteArrayOf()
        }
    }

    /**
     * Returns a subset of the given array.
     * @param array The array to get a subset from.
     * @param start The start index of the subset.
     * @param length The length of the subset.
     * @return The subset of the array.
     */
    private fun getArraySubset(array: ByteArray, start: Int, length: Int): ByteArray {
        return array.copyOfRange(start, start + length)
    }

    /**
     * Returns the secret key.
     * If the key doesn't exist in the keystore, it generates a new one.
     * @return The secret key.
     */
    private fun secretKey(): SecretKey {
        if (keyStore.containsAlias(config.keyAlias)) {
            return (keyStore.getEntry(config.keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
            )

            val specBuilder: KeyGenParameterSpec.Builder = KeyGenParameterSpec.Builder(
                config.keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(false)
                .setKeySize(config.keySize)

            //Add in Level 24
            specBuilder.setInvalidatedByBiometricEnrollment(config.invalidatedByBiometricEnrollment)

            //Allow access the data during screen lock
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                specBuilder.setUnlockedDeviceRequired(false)
                if (config.context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)) {
                    specBuilder.setIsStrongBoxBacked(true)
                }
            }

            keyGenerator.init(specBuilder.build())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    return keyGenerator.generateKey()
                } catch (e: StrongBoxUnavailableException) {
                    //In case failed to use Strong Box, disable it.
                    logger.w("Strong Box unavailable, recover without strong box", e)
                    specBuilder.setIsStrongBoxBacked(false)
                    return keyGenerator.generateKey()
                }
            } else {
                return keyGenerator.generateKey()
            }
        }
    }

    /**
     * Initializes the cipher and returns the IV.
     * @param cipher The cipher to initialize.
     * @return The IV.
     */
    private fun init(cipher: Cipher): ByteArray {
        //Generate a random IV See KeyGenParameterSpec.Builder.setRandomizedEncryptionRequired
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return cipher.iv
    }

    companion object {
        /**
         * Returns the Android keystore.
         * @return The Android keystore.
         */
        private val keyStore: KeyStore
            get() {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
                keyStore.load(null)
                return keyStore
            }
    }

    /**
     * Executes the given block and retries if an exception is thrown.
     * @param reset A function to execute if an exception is thrown.
     * @param block The block to execute.
     * @return The result of the block.
     */
    private inline fun <T> withRetry(
        default: T,
        reset: (Throwable) -> Unit = {},
        block: () -> T
    ): T {
        return try {
            block()
        } catch (e: Throwable) {
            reset(e)
            try {
                block()
            } catch (e: Throwable) {
                if (config.throwWhenEncryptError) throw e
                return default
            }
        }
    }
}