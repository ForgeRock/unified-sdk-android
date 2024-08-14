/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.oidc

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.pingidentity.android.ContextProvider
import com.pingidentity.exception.ApiException
import com.pingidentity.logger.CONSOLE
import com.pingidentity.logger.Console
import com.pingidentity.logger.Logger
import com.pingidentity.logger.Standard
import com.pingidentity.storage.MemoryStorage
import com.pingidentity.storage.StorageDelegate
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class OidcClientConfigTest {
    private val context: Context by lazy { ApplicationProvider.getApplicationContext<Application>() }

    @BeforeTest
    fun setUp() {
        ContextProvider.init(context)
    }

    @Test
    fun `init should set openId when not initialized`() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            ByteReadChannel(
                                "{\n" +
                                    "  \"authorization_endpoint\" : \"https://auth.test-one-pingone.com/as/authorize\",\n" +
                                    "  \"token_endpoint\" : \"https://auth.test-one-pingone.com/as/token\",\n" +
                                    "  \"userinfo_endpoint\" : \"https://auth.test-one-pingone.com/as/userinfo\",\n" +
                                    "  \"end_session_endpoint\" : \"https://auth.test-one-pingone.com/as/signoff\",\n" +
                                    "  \"revocation_endpoint\" : \"https://auth.test-one-pingone.com/as/revoke\"\n" +
                                    "}",
                            ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val httpClient = HttpClient(mockEngine)
            val oidcClientConfig =
                OidcClientConfig().apply {
                    this.httpClient = httpClient
                    discoveryEndpoint = "http://localhost"
                    this.storage = MemoryStorage<Token>()
                }
            oidcClientConfig.init()

            assertEquals(
                "https://auth.test-one-pingone.com/as/authorize",
                oidcClientConfig.openId.authorizationEndpoint,
            )
            assertEquals(
                "https://auth.test-one-pingone.com/as/token",
                oidcClientConfig.openId.tokenEndpoint,
            )
            assertEquals(
                "https://auth.test-one-pingone.com/as/userinfo",
                oidcClientConfig.openId.userinfoEndpoint,
            )
            assertEquals(
                "https://auth.test-one-pingone.com/as/signoff",
                oidcClientConfig.openId.endSessionEndpoint,
            )
            assertEquals(
                "https://auth.test-one-pingone.com/as/revoke",
                oidcClientConfig.openId.revocationEndpoint,
            )
        }

    @Test
    fun `init should throw exception when discovery fails`(): Unit =
        runTest {
            val mockEngine =
                MockEngine {
                    respond(
                        content = ByteReadChannel("Error"),
                        status = HttpStatusCode.InternalServerError,
                    )
                }
            val httpClient = HttpClient(mockEngine)
            val oidcClientConfig =
                OidcClientConfig().apply {
                    this.httpClient = httpClient
                    discoveryEndpoint = "http://localhost"
                    this.storage = MemoryStorage<Token>()
                }

            assertFailsWith<ApiException> {
                oidcClientConfig.init()
            }
        }

    @Test
    fun `plusAssign should copy all properties from other config`() {
        val oidcClientConfig = OidcClientConfig()
        val otherConfig =
            OidcClientConfig().apply {
                openId = OpenIdConfiguration()
                agent = mockk()
                logger = mockk()
                storage = mockk<StorageDelegate<Token>>()
                discoveryEndpoint = "http://localhost"
                clientId = "clientId"
                scope("openid")
                redirectUri = "http://localhost/callback"
                loginHint = "loginHint"
                nonce = "nonce"
                display = "display"
                prompt = "prompt"
                uiLocales = "uiLocales"
                acrValues = "acrValues"
                additionalParameters = mapOf("param" to "value")
                httpClient = mockk()
            }

        oidcClientConfig += otherConfig

        assertEquals(otherConfig.openId, oidcClientConfig.openId)
        assertEquals(otherConfig.agent, oidcClientConfig.agent)
        assertEquals(otherConfig.logger, oidcClientConfig.logger)
        assertEquals(otherConfig.storage, oidcClientConfig.storage)
        assertEquals(otherConfig.discoveryEndpoint, oidcClientConfig.discoveryEndpoint)
        assertEquals(otherConfig.clientId, oidcClientConfig.clientId)
        assertEquals(otherConfig.scopes, oidcClientConfig.scopes)
        assertEquals(otherConfig.redirectUri, oidcClientConfig.redirectUri)
        assertEquals(otherConfig.loginHint, oidcClientConfig.loginHint)
        assertEquals(otherConfig.nonce, oidcClientConfig.nonce)
        assertEquals(otherConfig.display, oidcClientConfig.display)
        assertEquals(otherConfig.prompt, oidcClientConfig.prompt)
        assertEquals(otherConfig.uiLocales, oidcClientConfig.uiLocales)
        assertEquals(otherConfig.acrValues, oidcClientConfig.acrValues)
        assertEquals(otherConfig.additionalParameters, oidcClientConfig.additionalParameters)
        assertEquals(otherConfig.httpClient, oidcClientConfig.httpClient)
    }

    @Test
    fun `clone should create a new instance with same properties`() {
        val oidcClientConfig =
            OidcClientConfig().apply {
                openId = OpenIdConfiguration()
                agent = mockk()
                logger = mockk()
                storage = mockk<StorageDelegate<Token>>()
                discoveryEndpoint = "http://localhost"
                clientId = "clientId"
                scope("openid")
                redirectUri = "http://localhost/callback"
                loginHint = "loginHint"
                nonce = "nonce"
                display = "display"
                prompt = "prompt"
                uiLocales = "uiLocales"
                acrValues = "acrValues"
                additionalParameters = mapOf("param" to "value")
                httpClient = mockk()
            }

        val clonedConfig = oidcClientConfig.clone()

        assertEquals(oidcClientConfig.openId, clonedConfig.openId)
        assertEquals(oidcClientConfig.agent, clonedConfig.agent)
        assertEquals(oidcClientConfig.logger, clonedConfig.logger)
        assertEquals(oidcClientConfig.storage, clonedConfig.storage)
        assertEquals(oidcClientConfig.discoveryEndpoint, clonedConfig.discoveryEndpoint)
        assertEquals(oidcClientConfig.clientId, clonedConfig.clientId)
        assertEquals(oidcClientConfig.scopes, clonedConfig.scopes)
        assertEquals(oidcClientConfig.redirectUri, clonedConfig.redirectUri)
        assertEquals(oidcClientConfig.loginHint, clonedConfig.loginHint)
        assertEquals(oidcClientConfig.nonce, clonedConfig.nonce)
        assertEquals(oidcClientConfig.display, clonedConfig.display)
        assertEquals(oidcClientConfig.prompt, clonedConfig.prompt)
        assertEquals(oidcClientConfig.uiLocales, clonedConfig.uiLocales)
        assertEquals(oidcClientConfig.acrValues, clonedConfig.acrValues)
        assertEquals(oidcClientConfig.additionalParameters, clonedConfig.additionalParameters)
        assertEquals(oidcClientConfig.httpClient, clonedConfig.httpClient)
    }

    @Test
    fun `scope should add provided scope to scopes set`() {
        val oidcClientConfig = OidcClientConfig()
        val scope = "email"

        oidcClientConfig.scope(scope)

        assertTrue { oidcClientConfig.scopes.contains(scope) }
    }

    @Test
    fun `httpclient should be initialized`() = runTest {
        val oidcClientConfig = OidcClientConfig().apply {
            storage = mockk()
            openId = mockk()
            logger = Logger.CONSOLE
        }
        oidcClientConfig.init()
        //Access the httpclient and make sure it is initialized
        oidcClientConfig.httpClient
    }

}
