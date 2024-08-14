/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci.collector

import com.pingidentity.davinci.plugin.Collectors
import com.pingidentity.davinci.plugin.ContinueTokenCollector
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal fun Collectors.eventType(): String? {
    forEach {
        when (it) {
            is SubmitCollector -> {
                if (it.value.isNotEmpty()) {
                    return it.value
                }
            }

            is FlowCollector -> {
                if (it.value.isNotEmpty()) {
                    return it.value
                }
            }
            else -> {}
        }
    }
    return null
}

internal fun Collectors.continueToken(): String? {
    forEach {
        when (it) {
            is ContinueTokenCollector -> {
                if (it.continueToken() != null) {
                    return it.continueToken()
                }
            }

            else -> {}
        }
    }
    return null
}

/**
 * Represents a list of collectors as a JSON object for posting to the server.
 *
 * This function takes a list of collectors and represents it as a JSON object. It iterates over the list of collectors,
 * adding each collector's key and value to the JSON object if the collector's value is not empty.
 *
 * @param collectors The list of collectors to represent as a JSON object.
 * @return A JSON object representing the list of collectors.
 */
internal fun Collectors.asJson(): JsonObject {
    return buildJsonObject {
        forEach {
            when (it) {
                is SubmitCollector -> {
                    if (it.value.isNotEmpty()) {
                        put("actionKey", it.key)
                    }
                }

                is FlowCollector -> {
                    if (it.value.isNotEmpty()) {
                        put("actionKey", it.key)
                    }
                }

                else -> {}
            }
        }
        putJsonObject("formData") {
            forEach {
                when (it) {
                    is TextCollector -> {
                        if (it.value.isNotEmpty()) put(it.key, it.value)
                    }

                    is PasswordCollector -> {
                        if (it.value.isNotEmpty()) put(it.key, it.value)
                    }

                    else -> {}
                }
            }
        }
    }
}

