/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.logger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggerTest {
    @Test
    fun loggerSameInstance() {
        val log1 = Logger.STANDARD
        val log2 = Logger.STANDARD
        assertEquals(log1, log2)
    }

    @Test
    fun loggerSameInstanceWarn() {
        val log1 = Logger.WARN
        val log2 = Logger.WARN
        assertEquals(log1, log2)
    }

    @Test
    fun loggerSameInstanceNone() {
        val log1 = Logger.NONE
        val log2 = Logger.NONE
        assertEquals(log1, log2)
    }

    @Test
    fun logLogCat() {
        assertTrue(Logger.STANDARD is Standard)
    }
}
