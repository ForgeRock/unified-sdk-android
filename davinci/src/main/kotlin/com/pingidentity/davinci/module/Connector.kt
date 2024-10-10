/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci.module

import android.net.Uri
import com.pingidentity.davinci.collector.asJson
import com.pingidentity.davinci.collector.continueToken
import com.pingidentity.davinci.collector.eventType
import com.pingidentity.davinci.plugin.Collectors
import com.pingidentity.orchestrate.ContinueNode
import com.pingidentity.orchestrate.FlowContext
import com.pingidentity.orchestrate.Request
import com.pingidentity.orchestrate.Workflow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URL

/**
 * Extension property to get the id of a Connector.
 */
val ContinueNode.id: String
    get() = (this as DaVinciConnector).id

/**
 * Extension property to get the name of a Connector.
 */
val ContinueNode.name: String
    get() = (this as DaVinciConnector).name

/**
 * Extension property to get the description of a Connector.
 */
val ContinueNode.description: String
    get() = (this as DaVinciConnector).description

/**
 * Extension property to get the category of a Connector.
 */
val ContinueNode.category: String
    get() = (this as DaVinciConnector).category

/**
 * Class representing a DaVinciConnector.
 *
 * @property context The FlowContext of the connector.
 * @property workflow The Workflow of the connector.
 * @property input The input JsonObject of the connector.
 * @property collectors The collectors of the connector.
 */
internal class DaVinciConnector(
    context: FlowContext, workflow: Workflow, input: JsonObject, private val collectors: Collectors
) : ContinueNode(
    context, workflow, input, collectors
) {

    /**
     * Function to convert the connector to a JsonObject.
     *
     * @return The connector as a JsonObject.
     */
    private fun asJson(): JsonObject {
        return buildJsonObject {
            put("id", input["id"]?.jsonPrimitive?.content ?: "")
            put("eventName", input["eventName"]?.jsonPrimitive?.content ?: "")
            put("parameters", buildJsonObject {
                collectors.eventType()?.let {
                    put("eventType", it)
                }
                put("data", collectors.asJson())
            })
        }
    }

    /**
     * Lazy property to get the id of the connector.
     */
    val id: String by lazy {
        input["id"]?.jsonPrimitive?.content ?: ""
    }

    /**
     * Lazy property to get the name of the connector.
     */
    val name: String by lazy {
        input["form"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""
    }

    /**
     * Lazy property to get the description of the connector.
     */
    val description: String by lazy {
        input["form"]?.jsonObject?.get("description")?.jsonPrimitive?.content ?: ""
    }

    /**
     * Lazy property to get the category of the connector.
     */
    val category: String by lazy {
        input["form"]?.jsonObject?.get("category")?.jsonPrimitive?.content ?: ""
    }

    /**
     * Function to convert the connector to a [Request].
     *
     * @return The connector as a Request.
     */
    override fun asRequest(): Request {
        //Check if there is a continue token, if so, use it to continue the request with /continue url
        collectors.continueToken()?.let {
            return Request().apply {
                url(
                    continueTokenUrl(
                        input["_links"]?.jsonObject?.get("next")?.jsonObject?.get("href")?.jsonPrimitive?.content
                            ?: "", workflow.oidcClientConfig().clientId
                    )
                )
                header("Authorization", "Bearer $it")
                body()
            }
        } ?: return Request().apply {
            url(
                input["_links"]?.jsonObject?.get("next")?.jsonObject?.get("href")?.jsonPrimitive?.content
                    ?: "",
            )
            header("Content-Type", "application/json")
            body(asJson())
        }
    }

    private fun continueTokenUrl(next: String, clientId: String): String {
        val url = URL(next)
        val pathParts = url.path.split("/")
        val envId = pathParts[1] // env id
        val application = pathParts[2] // davinci

        return Uri.Builder().apply {
            scheme(url.protocol)
            encodedAuthority(url.authority)
            appendPath(envId)
            appendPath(application)
            appendPath("policy")
            appendPath(clientId)
            appendPath("continue")
            query(url.query)
        }.build().toString()

    }

}