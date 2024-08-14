/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey.plugin

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CallbackFactory {
    private val callbacks: MutableMap<String, () -> Callback> = HashMap()

    fun register(type: String, block: () -> Callback) {
        callbacks[type] = block
    }

    fun callback(array: JsonArray): List<Callback> {
        val list = mutableListOf<Callback>()
        array.forEach { item ->
            val jsonObject = item.jsonObject
            val type = jsonObject["type"]?.jsonPrimitive?.content
            callbacks[type]?.let { list.add(it().apply { init(jsonObject) }) }
        }
        return list
    }

}