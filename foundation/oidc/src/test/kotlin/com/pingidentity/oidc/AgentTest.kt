/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.oidc

import com.pingidentity.oidc.exception.AuthorizeException
import com.pingidentity.storage.MemoryStorage
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class AgentTest {

    @Test
    fun `endSession should always return false`() = runTest {
        val oidcClientConfig = OidcClientConfig().apply {
            httpClient = mockk()
            discoveryEndpoint = "http://localhost/openid-configuration"
            redirectUri = "http://localhost/redirect"
            clientId = "test-client-id"
            storage = MemoryStorage()
            updateAgent(DefaultAgent)
        }
        val result = DefaultAgent.endSession(OidcConfig(Unit, oidcClientConfig), "dummy-id-token")
        assertFalse(result)
    }

    @Test
    fun `authorize should always throw AuthorizeException`() = runTest {
        val oidcClientConfig = OidcClientConfig().apply {
            httpClient = mockk()
            discoveryEndpoint = "http://localhost/openid-configuration"
            redirectUri = "http://localhost/redirect"
            clientId = "test-client-id"
            storage = MemoryStorage()
            updateAgent(DefaultAgent)
        }
        val exception = assertFailsWith<AuthorizeException> {
            DefaultAgent.authorize(OidcConfig(Unit, oidcClientConfig))
        }
        assertEquals("No AuthCode is available.", exception.message)
    }

}