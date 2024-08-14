/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci.module

import com.pingidentity.exception.ApiException
import com.pingidentity.davinci.collector.Form
import com.pingidentity.davinci.plugin.Collector
import com.pingidentity.oidc.exception.AuthorizeException
import com.pingidentity.orchestrate.Error
import com.pingidentity.orchestrate.Failure
import com.pingidentity.orchestrate.FlowContext
import com.pingidentity.orchestrate.Module
import com.pingidentity.orchestrate.Node
import com.pingidentity.orchestrate.Session
import com.pingidentity.orchestrate.Success
import com.pingidentity.orchestrate.Workflow
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Module for transforming the response from DaVinci to [Node].
 */
internal val NodeTransform =
    Module.of {
        transform {

            when (it.status()) {
                // DaVinci returns 400 for invalid requests,
                // the previous [Connector] can be used to retry the request.
                HttpStatusCode.BadRequest.value -> {
                    failure(it.body().asJson())
                }

                HttpStatusCode.OK.value -> {
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
    return Failure(json, json["message"]?.jsonPrimitive?.content ?: "")
}

private fun transform(
    context: FlowContext,
    workflow: Workflow,
    json: JsonObject,
): Node {
    // If status is FAILED, return error
    if ("status" in json) {
        if (json["status"]?.jsonPrimitive?.content == "FAILED") {
            return Error(ApiException(HttpStatusCode.OK.value, json.toString()))
        }
    }
    //If authorizeResponse is present, return success
    if ("authorizeResponse" in json) {
        return Success(
            json,
            object : Session {
                override fun value(): String {
                    return json["authorizeResponse"]?.jsonObject?.get("code")?.jsonPrimitive?.content
                        ?: throw AuthorizeException("Authorization code is missing.")
                }
            },
        )
    }

    val collectors = mutableListOf<Collector>()
    if ("form" in json) collectors.addAll(Form.parse(json))

    return DaVinciConnector(context, workflow, json, collectors.toList())
}
