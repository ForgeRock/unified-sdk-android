/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.oidc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PkceTest {
    @Test
    fun `generate should create different Pkce for each call`() {
        val pkce1 = Pkce.generate()
        val pkce2 = Pkce.generate()

        assertTrue(pkce1.codeVerifier != pkce2.codeVerifier)
        assertTrue(pkce1.codeChallenge != pkce2.codeChallenge)
    }

    @Test
    fun `generate should create valid Pkce`() {
        val pkce = Pkce.generate()
        assertTrue(pkce.codeVerifier.isNotEmpty())
        assertTrue(pkce.codeChallenge.isNotEmpty())
        assertEquals("S256", pkce.codeChallengeMethod)
    }

}
