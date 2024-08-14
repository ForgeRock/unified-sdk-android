/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.orchestrate

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * Sealed interface for Node. Represents a node in the workflow.
 */
sealed interface Node

/**
 * Represents an error node in the workflow.
 * @property cause The cause of the error.
 */
data class Error(val cause: Throwable) : Node

/**
 * Abstract class for a Connector node in the workflow.
 * @property context The context of the flow.
 * @property workflow The workflow the connector is part of.
 * @property input The input JSON object.
 * @property actions The list of actions to be performed by the connector.
 */
abstract class Connector(
    val context: FlowContext,
    val workflow: Workflow,
    val input: JsonObject,
    val actions: List<Action>,
) : Node, Closeable {
    /**
     * Moves to the next node in the workflow.
     * @return The next Node.
     */
    suspend fun next(): Node {
        return workflow.next(context, this)
    }

    /**
     * Converts the connector to a Request.
     * @return The Request representation of the connector.
     */
    abstract fun asRequest(): Request

    /**
     * Closes all closeable actions.
     */
    override fun close() {
        actions.filterIsInstance<Closeable>().forEach { it.close() }
    }
}

/**
 * Represents a success node in the workflow.
 * @property session The session associated with the success.
 */
data class Success(val input: JsonObject = buildJsonObject {}, val session: Session) : Node

/**
 * Represents a failure node in the workflow.
 * @property input The input JSON object.
 * @property message The failure message.
 */
data class Failure(val input: JsonObject = buildJsonObject { }, val message: String) : Node

/**
 * Tries to execute the given block and returns an Error node if an exception is thrown.
 * @param block The block to be executed.
 * @return The Node resulting from the execution of the block, or an Error node if an exception is thrown.
 */
inline fun catch(block: () -> Node): Node {
    return try {
        block()
    } catch (e: Throwable) {
        Error(e)
    }
}