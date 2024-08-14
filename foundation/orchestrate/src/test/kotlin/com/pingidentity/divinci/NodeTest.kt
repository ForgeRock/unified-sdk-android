/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.divinci

import com.pingidentity.orchestrate.Action
import com.pingidentity.orchestrate.Closeable
import com.pingidentity.orchestrate.Connector
import com.pingidentity.orchestrate.EmptySession
import com.pingidentity.orchestrate.Error
import com.pingidentity.orchestrate.FlowContext
import com.pingidentity.orchestrate.Node
import com.pingidentity.orchestrate.Request
import com.pingidentity.orchestrate.Success
import com.pingidentity.orchestrate.Workflow
import com.pingidentity.orchestrate.catch
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeTest {

    @Test
    fun `catch should return Error node for thrown exception`() {
        val exception = RuntimeException("Test exception")
        val node = catch { throw exception }
        assertTrue(node is Error)
        assertEquals(exception, node.cause)
    }

    @Test
    fun `catch should return Node for successful block execution`() {
        val successNode = Success(session = EmptySession)
        val node = catch { successNode }
        assertEquals(successNode, node)
    }

    @Test
    fun `Connector next should return next Node in workflow`() = runTest {
        val mockWorkflow = mockk<Workflow>()
        val mockContext = mockk<FlowContext>()
        val mockNode = mockk<Node>()
        coEvery { mockWorkflow.next(mockContext, any()) } returns mockNode

        val connector = object : Connector(mockContext, mockWorkflow, buildJsonObject {}, emptyList()) {
            override fun asRequest(): Request {
                return mockk()
            }
        }

        val nextNode = connector.next()
        assertEquals(mockNode, nextNode)
    }

    @Test
    fun `Connector close should close all closeable actions`() {
        val o = object : Action, Closeable {
            override fun close() {
            }
        }
        val closeableAction = spyk(o)
        val connector = object : Connector(mockk(), mockk(), buildJsonObject {}, listOf(closeableAction)) {
            override fun asRequest(): Request {
                return mockk()
            }
        }

        connector.close()
        verify { (closeableAction as Closeable).close() }
    }
}