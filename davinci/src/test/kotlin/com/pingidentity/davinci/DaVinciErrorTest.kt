/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci

import com.pingidentity.testrail.TestRailCase
import com.pingidentity.exception.ApiException
import com.pingidentity.davinci.collector.PasswordCollector
import com.pingidentity.davinci.collector.SubmitCollector
import com.pingidentity.davinci.collector.TextCollector
import com.pingidentity.davinci.module.Oidc
import com.pingidentity.davinci.plugin.collectors
import com.pingidentity.logger.Logger
import com.pingidentity.logger.STANDARD
import com.pingidentity.orchestrate.Connector
import com.pingidentity.orchestrate.module.Cookie
import com.pingidentity.storage.MemoryStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue
import com.pingidentity.orchestrate.Error
import com.pingidentity.orchestrate.Failure
import kotlin.test.BeforeTest
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DaVinciErrorTest {

    private lateinit var mockEngine: MockEngine

    @BeforeTest
    fun setup() {
        CollectorRegistry().initialize()
    }

    @AfterTest
    fun tearDown() {
        mockEngine.close()
    }

    @TestRailCase(21285)
    @Test
    fun `DaVinci well-known endpoint failed`() =
        runTest {

            mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/.well-known/openid-configuration" -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel("Not Found"),
                                status = HttpStatusCode.NotFound,
                            )
                        }

                        else -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel(""),
                                status = HttpStatusCode.InternalServerError,
                            )
                        }
                    }
                }

            val daVinci =
                DaVinci {
                    httpClient = HttpClient(mockEngine)
                    // Oidc as module
                    module(Oidc) {
                        clientId = "test"
                        discoveryEndpoint =
                            "http://localhost/.well-known/openid-configuration"
                        scopes = mutableSetOf("openid", "email", "address")
                        redirectUri = "http://localhost:8080"
                        storage = MemoryStorage()
                        logger = Logger.STANDARD
                    }
                    module(Cookie) {
                        storage = MemoryStorage()
                        persist = mutableListOf("ST")
                    }
                }

            val node = daVinci.start() // Return first Node
            assertTrue { node is Error }
            assertTrue { (node as Error).cause is ApiException }
            assertTrue { ((node as Error).cause as ApiException).status == 404 }
            assertTrue { ((node as Error).cause as ApiException).content == "Not Found" }
        }

    @TestRailCase(21286)
    @Test
    fun `DaVinci authorize endpoint failed`() =
        runTest {

            mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/.well-known/openid-configuration" -> {
                            respond(openIdConfigurationResponse(), HttpStatusCode.OK, headers)
                        }

                        "/authorize" -> {
                            respond(content = ByteReadChannel("{\n" +
                                    "    \"id\": \"7bbe285f-c0e0-41ef-8925-c5c5bb370acc\",\n" +
                                    "    \"code\": \"INVALID_REQUEST\",\n" +
                                    "    \"message\": \"Invalid DV Flow Policy ID: Single_Factor\"\n" +
                                    "}")
                                , HttpStatusCode.BadRequest, headers)
                        }

                        else -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel(""),
                                status = HttpStatusCode.InternalServerError,
                            )
                        }
                    }
                }

            val daVinci =
                DaVinci {
                    httpClient = HttpClient(mockEngine)
                    // Oidc as module
                    module(Oidc) {
                        clientId = "test"
                        discoveryEndpoint =
                            "http://localhost/.well-known/openid-configuration"
                        scopes = mutableSetOf("openid", "email", "address")
                        redirectUri = "http://localhost:8080"
                        storage = MemoryStorage()
                        logger = Logger.STANDARD
                    }
                    module(Cookie) {
                        storage = MemoryStorage()
                        persist = mutableListOf("ST")
                    }
                }

            val node = daVinci.start() // Return first Node
            assertTrue { node is Failure }
            assertContains((node as Failure).input.toString(), "INVALID_REQUEST")
        }

    @TestRailCase(21287)
    @Test
    fun `DaVinci authorize endpoint failed with OK response but error during Transform`() =
        runTest {

            mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/.well-known/openid-configuration" -> {
                            respond(openIdConfigurationResponse(), HttpStatusCode.OK, headers)
                        }

                        "/authorize" -> {
                            respond(content = ByteReadChannel("{\n" +
                                    "    \"environment\": {\n" +
                                    "        \"id\": \"0c6851ed-0f12-4c9a-a174-9b1bf8b438ae\"\n" +
                                    "    },\n" +
                                    "    \"status\": \"FAILED\",\n" +
                                    "    \"error\": {\n" +
                                    "        \"code\": \"login_required\",\n" +
                                    "        \"message\": \"The request could not be completed. There was an issue processing the request\"\n" +
                                    "    }\n" +
                                    "}")
                                , HttpStatusCode.OK, headers)
                        }

                        else -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel(""),
                                status = HttpStatusCode.InternalServerError,
                            )
                        }
                    }
                }

            val daVinci =
                DaVinci {
                    httpClient = HttpClient(mockEngine)
                    // Oidc as module
                    module(Oidc) {
                        clientId = "test"
                        discoveryEndpoint =
                            "http://localhost/.well-known/openid-configuration"
                        scopes = mutableSetOf("openid", "email", "address")
                        redirectUri = "http://localhost:8080"
                        storage = MemoryStorage()
                        logger = Logger.STANDARD
                    }
                    module(Cookie) {
                        storage = MemoryStorage()
                        persist = mutableListOf("ST")
                    }
                }

            val node = daVinci.start() // Return first Node
            assertTrue { node is Error }
            assertContains((node as Error).cause.toString(), "login_required")
        }

    @TestRailCase(21288)
    @Test
    fun `DaVinci transform failed`() =
        runTest {

            mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/.well-known/openid-configuration" -> {
                            respond(openIdConfigurationResponse(), HttpStatusCode.OK, headers)
                        }

                        "/authorize" -> {
                            respond(content = ByteReadChannel("{ Not a Json }")
                                , HttpStatusCode.OK, authorizeResponseHeaders)
                        }


                        else -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel(""),
                                status = HttpStatusCode.InternalServerError,
                            )
                        }
                    }
                }

            val daVinci =
                DaVinci {
                    httpClient = HttpClient(mockEngine)
                    // Oidc as module
                    module(Oidc) {
                        clientId = "test"
                        discoveryEndpoint =
                            "http://localhost/.well-known/openid-configuration"
                        scopes = mutableSetOf("openid", "email", "address")
                        redirectUri = "http://localhost:8080"
                        storage = MemoryStorage()
                        logger = Logger.STANDARD
                    }
                    module(Cookie) {
                        storage = MemoryStorage()
                        persist = mutableListOf("ST")
                    }
                }

            val node = daVinci.start() // Return first Node
            assertTrue { node is Error }
        }

    @TestRailCase(21289)
    @Test
    fun `DaVinci invalid password`() =
        runTest {

            mockEngine =
                MockEngine { request ->
                    when (request.url.encodedPath) {
                        "/.well-known/openid-configuration" -> {
                            respond(openIdConfigurationResponse(), HttpStatusCode.OK, headers)
                        }

                        "/customHTMLTemplate" -> {
                            respond(customHTMLTemplateWithInvalidPassword(), HttpStatusCode.BadRequest, customHTMLTemplateHeaders)
                        }

                        "/authorize" -> {
                            respond(authorizeResponse(), HttpStatusCode.OK, authorizeResponseHeaders)
                        }

                        else -> {
                            return@MockEngine respond(
                                content =
                                ByteReadChannel(""),
                                status = HttpStatusCode.InternalServerError,
                            )
                        }
                    }
                }

            val daVinci =
                DaVinci {
                    httpClient = HttpClient(mockEngine)
                    // Oidc as module
                    module(Oidc) {
                        clientId = "test"
                        discoveryEndpoint =
                            "http://localhost/.well-known/openid-configuration"
                        scopes = mutableSetOf("openid", "email", "address")
                        redirectUri = "http://localhost:8080"
                        storage = MemoryStorage()
                        logger = Logger.STANDARD
                    }
                    module(Cookie) {
                        storage = MemoryStorage()
                        persist = mutableListOf("ST")
                    }
                }

            val node = daVinci.start() // Return first Node
            assertTrue(node is Connector)
            (node.collectors[0] as? TextCollector)?.value = "My First Name"
            (node.collectors[1] as? PasswordCollector)?.value = "My Password"
            (node.collectors[2] as? SubmitCollector)?.value = "click me"
            val next = node.next()

            //Make sure the password is cleared by close() interface
            assertEquals("", (node.collectors[1] as? PasswordCollector)?.value)

            assertTrue(next is Failure)
            assertEquals(" Invalid username and/or password", next.message)
            assertContains(next.input.toString(), "The provided password did not match provisioned password")

        }

}
