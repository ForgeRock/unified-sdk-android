/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.orchestrate

/**
 * Interface for a Session. A Session represents a user's session in the application.
 */
interface Session {
    /**
     * Returns the value of the session.
     * @return The value of the session as a String.
     */
    fun value(): String
}

/**
 * Object for an EmptySession. An EmptySession represents a session with no value.
 */
object EmptySession : Session {
    /**
     * Returns the value of the empty session.
     * @return The value of the empty session as a String.
     */
    override fun value(): String {
        return ""
    }
}