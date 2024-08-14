/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.storage

import kotlinx.serialization.Serializable

/**
 * Interface to persist and retrieve [Serializable] object.
 */
interface Storage<T : @Serializable Any> {
    suspend fun save(item: T)

    suspend fun get(): T?

    suspend fun delete()
}
