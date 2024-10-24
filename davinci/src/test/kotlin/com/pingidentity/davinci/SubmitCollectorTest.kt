/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

import com.pingidentity.davinci.collector.FieldCollector
import com.pingidentity.davinci.collector.SubmitCollector
import com.pingidentity.testrail.TestRailCase
import com.pingidentity.testrail.TestRailWatcher
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TestWatcher

class SubmitCollectorTest {

    @JvmField
    @Rule
    val watcher: TestWatcher = TestRailWatcher

    @TestRailCase(22560)
    @Test
    fun testInitialization() {
        val submitCollector = SubmitCollector()
        assertNotNull(submitCollector)
    }

    @TestRailCase(22561)
    @Test
    fun testInheritance() {
        val submitCollector = SubmitCollector()
        assertTrue(submitCollector is FieldCollector)
    }
}