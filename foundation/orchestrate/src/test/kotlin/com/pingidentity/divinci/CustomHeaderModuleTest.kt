/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.divinci

import com.pingidentity.orchestrate.Workflow
import com.pingidentity.orchestrate.module.CustomHeader
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomHeaderModuleTest {

    private lateinit var mockEngine: MockEngine

    @Test
    fun `Test custom header added to request`() = runTest {
        mockEngine = MockEngine { request ->
            assertTrue(request.headers.contains("X-Custom-Header"))
            assertEquals("CustomValue", request.headers["X-Custom-Header"])
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headers { }
            )
        }

        val workflow = Workflow {
            httpClient = HttpClient(mockEngine)
            module(CustomHeader) {
                header("X-Custom-Header", "CustomValue")
            }
        }

        workflow.start()
    }
}