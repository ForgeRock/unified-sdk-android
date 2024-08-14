/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.storage

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.pingidentity.storage.encrypt.SecretKeyEncryptor
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.security.KeyStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@SmallTest
class SecretKeyEncryptorTest {
    private val application: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @BeforeTest
    fun setUp() = runTest {
        clear()
    }

    @AfterTest
    fun tearDown() =
        runTest {
            clear()
        }

    private fun clear() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        keyStore.deleteEntry(SecretKeyEncryptorTest::class.java.simpleName)
    }

    @Test
    fun testEncryptDecrypt() = runTest {
        val encryptor = SecretKeyEncryptor {
            keyAlias = SecretKeyEncryptorTest::class.java.simpleName
        }
        val encrypted = encryptor.encrypt("test".toByteArray())
        assertEquals("test", String(encryptor.decrypt(encrypted)))
    }

}
