/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci.plugin

/**
 * Interface representing a ContinueTokenCollector.
 * A ContinueTokenCollector is a type of Collector that can provide a continuation token.
 */
interface ContinueTokenCollector : Collector {
    /**
     * Retrieves the continuation token.
     *
     * @return The continuation token as a String, or null if no token is available.
     */
    fun continueToken(): String?
}