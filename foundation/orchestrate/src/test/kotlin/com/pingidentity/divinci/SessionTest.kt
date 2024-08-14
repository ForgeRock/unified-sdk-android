/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.divinci

import com.pingidentity.orchestrate.EmptySession
import com.pingidentity.orchestrate.Session
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTest {

    @Test
    fun `EmptySession value should return empty string`() {
        assertEquals("", EmptySession.value())
    }

    @Test
    fun `Session value should return correct session value`() {
        val session = object : Session {
            override fun value(): String {
                return "session_value"
            }
        }
        assertEquals("session_value", session.value())
    }
}