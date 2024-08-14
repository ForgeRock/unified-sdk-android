/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey.module

import com.pingidentity.exception.ApiException
import com.pingidentity.journey.journeyConfig
import com.pingidentity.journey.plugin.Callback
import com.pingidentity.journey.plugin.CallbackFactory
import com.pingidentity.orchestrate.Connector
import com.pingidentity.orchestrate.EmptySession
import com.pingidentity.orchestrate.Error
import com.pingidentity.orchestrate.Failure
import com.pingidentity.orchestrate.FlowContext
import com.pingidentity.orchestrate.Module
import com.pingidentity.orchestrate.Node
import com.pingidentity.orchestrate.Request
import com.pingidentity.orchestrate.Session
import com.pingidentity.orchestrate.Success
import com.pingidentity.orchestrate.Workflow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

internal val NodeTransform =
    Module.of {
        transform {

            when (it.status()) {
                400 -> {
                    failure(it.body().asJson())
                }

                200 -> {
                    transform(this, workflow, it.body().asJson())
                }

                else -> {
                    Error(ApiException(it.status(), it.body()))
                }
            }
        }
    }

private fun String.asJson(): JsonObject {
    return Json.parseToJsonElement(this).jsonObject
}

private fun failure(json: JsonObject): Failure {
    return Failure(json, json["message"]?.jsonPrimitive?.content ?: "" )
}

private fun transform(
    context: FlowContext,
    workflow: Workflow,
    json: JsonObject,
): Node {
    val callbacks = mutableListOf<Callback>()
    if ("authId" in json) {
        json["callbacks"]?.jsonArray?.let {
            callbacks.addAll(CallbackFactory.callback(it))
        }
        return object : Connector(context, workflow, json, callbacks) {
            private fun asJson(): JsonObject {
                return buildJsonObject {
                    put("authId", json["authId"]?.jsonPrimitive?.content ?: "")
                    putJsonArray("callbacks") {
                        callbacks.forEach {
                            add(it.asJson())
                        }
                    }
                }
            }

            override fun asRequest(): Request {
                return Request().apply {
                    url(
                        "${workflow.journeyConfig().serverUrl}/json/realms/${workflow.journeyConfig().realm}/authenticate"
                    )
                    header("Content-Type", "application/json")
                    if (workflow.journeyConfig().noSession) parameter("noSession", "true")
                    body(asJson())
                }
            }
        }
    }
    if ("tokenId" in json && json["tokenId"]?.jsonPrimitive?.content?.isNotEmpty() == true) {
        return Success(json,
            object : Session {
                override fun value(): String {
                    return json["tokenId"]?.jsonPrimitive?.content ?: ""
                }
            }
        )
    } else {
        return Success(json, EmptySession)
    }
}
