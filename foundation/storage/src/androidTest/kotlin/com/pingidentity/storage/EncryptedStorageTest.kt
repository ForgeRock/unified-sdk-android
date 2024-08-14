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
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@SmallTest
class EncryptedStorageTest {
    private val context: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @AfterTest
    fun tearDown() =
        runTest {
            val storage = EncryptedStorage<Data>(filename = "1", context = context)
            storage.delete()
        }

    @Test
    fun testDataStore() =
        runTest {
            val storage = EncryptedStorage<Data>(filename = "1", context = context)
            storage.save(Data(1, "test"))
            val storedData = storage.get()
            assertEquals(1, storedData!!.a)
            assertEquals("test", storedData.b)
        }

    @Test
    fun testMultipleData() =
        runTest {
            val storage = EncryptedStorage<List<Data>>("1", context = context)
            val dataList = listOf(Data(1, "test1"), Data(2, "test2"))
            storage.save(dataList)
            val storedData = storage.get()
            assertEquals(dataList, storedData)
        }

    @Test
    fun testDeleteData() =
        runTest {
            val storage = EncryptedStorage<Data>(filename = "1", context = context)
            val data = Data(1, "test")
            storage.save(data)
            storage.delete()
            val storedData = storage.get()
            assertEquals(null, storedData)
        }

    @Test
    fun testDifferentDataObjectsWithSameStorage() =
        runTest {
            val storageData = EncryptedStorage<Data>("1", context = context)
            val storageData2 = EncryptedStorage<Data2>("1", context = context)

            val data = Data(1, "test")
            val data2 = Data2(2, "test1")

            storageData.save(data)
            storageData2.save(data2)

            val storedData = storageData.get()
            val storedData2 = storageData2.get()

            assertEquals(data, storedData)
            assertEquals(data2, storedData2)

            storageData.delete()
            assertNull(storageData.get())
            assertNotNull(storageData2.get())
        }
}
