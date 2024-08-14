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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
@SmallTest
class DataStoreStorageTest {

    private val context: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }
    private val Context.dataStore: DataStore<Data?> by dataStore("test", DataStoreSerializer())

    @AfterTest
    fun tearDown() =
        runTest {
            context.dataStore.updateData { null }
        }

    @Test
    fun testDataStore() =
        runTest(timeout = Duration.INFINITE) {
            val storage = DataStoreStorage(context.dataStore)
            val v = storage.get()
            assertNull(v)
            storage.save(Data(1, "test"))
            val storedData = storage.get()
            assertEquals(1, storedData!!.a)
            assertEquals("test", storedData.b)
        }

}
