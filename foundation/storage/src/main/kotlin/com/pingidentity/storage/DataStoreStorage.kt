/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.storage

import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.flow.first
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

/**
 * A repository for storing serializable objects in DataStore.
 *
 * @param T The type of the object to be stored. Must be serializable.
 * @param dataStore The DataStore instance to use for storing the object.
 */
class DataStoreStorage<T : @Serializable Any>(
    private val dataStore: DataStore<T?>,
) : Storage<T> {
    /**
     * Saves the given item in the DataStore.
     *
     * @param item The item to save.
     */
    override suspend fun save(item: T) {
        dataStore.updateData {
            item
        }
    }

    /**
     * Retrieves the item from the DataStore.
     *
     * @return The item if it exists, null otherwise.
     */
    override suspend fun get(): T? {
        return dataStore.data.first()
    }

    /**
     * Deletes the item from the DataStore.
     */
    override suspend fun delete() {
        dataStore.updateData { null }
    }
}

/**
 * Creates a new Serializer instance for a given type T.
 *
 * @param T The type of the object to be serialized/deserialized.
 *
 * @return A new Serializer instance.
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T : Any> DataStoreSerializer(): Serializer<T?> {
    return object : Serializer<T?> {
        override val defaultValue: T? = null

        /**
         * Reads the object from the given InputStream.
         *
         * @param input The InputStream to read from.
         *
         * @return The object if it exists, null otherwise.
         */
        override suspend fun readFrom(input: InputStream): T? {
            return if (input.isNotEmpty()) {
                Json.decodeFromStream(input)
            } else {
                null
            }
        }

        /**
         * Writes the object to the given OutputStream.
         *
         * @param t The object to write.
         * @param output The OutputStream to write to.
         */
        override suspend fun writeTo(
            t: T?,
            output: OutputStream,
        ) {
            if (t != null) Json.encodeToStream(t, output)
        }

        /**
         * Checks if the InputStream is not empty.
         *
         * @return True if the InputStream is not empty, false otherwise.
         */
        private fun InputStream.isNotEmpty() = available() > 0
    }
}

/**
 * Creates a new Storage instance for storing serializable objects in DataStore.
 *
 * @param T The type of the object to be stored. Must be serializable.
 * @param dataStore The DataStore instance to use for storing the object.
 * @param cacheable Whether the storage should cache the object in memory.
 *
 * @return A new Storage instance.
 */
inline fun <reified T : @Serializable Any> DataStoreStorage(
    dataStore: DataStore<T?>,
    cacheable: Boolean = false,
): StorageDelegate<T> {


    return StorageDelegate(
        DataStoreStorage(dataStore = dataStore),
        cacheable,
    )
}