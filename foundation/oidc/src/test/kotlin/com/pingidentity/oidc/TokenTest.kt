/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.oidc

import com.pingidentity.oidc.Token.Companion.now
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TokenTest {

   @Test
    fun `isExpired should return true when current time is after expireAt`() {
        val token = Token(expireAt = now() - 1)
        assertTrue(token.isExpired)
    }

    @Test
    fun `isExpired should return false when current time is before expireAt`() {
        val token = Token(expireAt = now() + 1)
        assertFalse(token.isExpired)
    }

    @Test
    fun `isExpired with threshold should return true when current time is after expireAt minus threshold`() {
        val token = Token(expireAt = now() + 1)
        assertTrue(token.isExpired(threshold = 2))
    }

    @Test
    fun `isExpired with threshold should return false when current time is before expireAt minus threshold`() {
        val token = Token(expireAt = now() + 3)
        assertFalse(token.isExpired(threshold = 2))
    }

    @Test
    fun `Token should deserialize from JSON correctly`() {
        val json = """{"access_token":"accessToken","token_type":"Bearer","scope":"openid","expires_in":3600,"refresh_token":"refreshToken","id_token":"idToken","expireAt":${now() + 3600}}"""
        val token = Json.decodeFromString<Token>(json)
        assertEquals("accessToken", token.accessToken)
        assertEquals("Bearer", token.tokenType)
        assertEquals("openid", token.scope)
        assertEquals(3600, token.expiresIn)
        assertEquals("refreshToken", token.refreshToken)
        assertEquals("idToken", token.idToken)
        assertEquals(now() + 3600, token.expireAt)
    }

    @Test
    fun `Token should handle missing optional fields during deserialization`() {
        val json = """{"access_token":"accessToken","expires_in":3600,"expireAt":${now() + 3600}}"""
        val token = Json.decodeFromString<Token>(json)
        assertEquals("accessToken", token.accessToken)
        assertEquals(null, token.tokenType)
        assertEquals(null, token.scope)
        assertEquals(3600, token.expiresIn)
        assertEquals(null, token.refreshToken)
        assertEquals(null, token.idToken)
        assertEquals(now() + 3600, token.expireAt)
    }

}
