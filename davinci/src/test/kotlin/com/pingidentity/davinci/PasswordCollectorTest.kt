/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci

import com.pingidentity.davinci.collector.PasswordCollector
import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordCollectorTest {

    @Test
    fun `close should clear password when clearPassword is true`() {
        val passwordCollector = PasswordCollector()
        passwordCollector.value = "password"
        passwordCollector.clearPassword = true

        passwordCollector.close()

        assertEquals("", passwordCollector.value)
    }

    @Test
    fun `close should not clear password when clearPassword is false`() {
        val passwordCollector = PasswordCollector()
        passwordCollector.value = "password"
        passwordCollector.clearPassword = false

        passwordCollector.close()

        assertEquals("password", passwordCollector.value)
    }
}