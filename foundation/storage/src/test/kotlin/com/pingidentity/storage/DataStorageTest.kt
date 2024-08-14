/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.storage

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class DataStorageTest {
    private val context: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }

    private val Context.dataStore: DataStore<Data?> by dataStore("test", DataStoreSerializer())
    private val Context.dataStoreList: DataStore<List<Data>?> by dataStore("test-list", DataStoreSerializer())

    @AfterTest
    fun tearDown() =
        runTest {
            context.dataStore.updateData { null }
        }

    @Test
    fun testDataStore() =
        runTest {
            val storage = DataStoreStorage(context.dataStore)
            storage.save(Data(1, "test"))
            val storedData = storage.get()
            assertEquals(1, storedData!!.a)
            assertEquals("test", storedData.b)
        }

    @Test
    fun testMultipleData() =
        runTest {
            val storage = DataStoreStorage(context.dataStoreList)
            val dataList = listOf(Data(1, "test1"), Data(2, "test2"))
            storage.save(dataList)
            val storedData = storage.get()
            assertEquals(dataList, storedData)
        }

    @Test
    fun testDeleteData() =
        runTest {
            val storage = DataStoreStorage(context.dataStore)
            val data = Data(1, "test")
            storage.save(data)
            storage.delete()
            val storedData = storage.get()
            assertEquals(null, storedData)
        }
}
