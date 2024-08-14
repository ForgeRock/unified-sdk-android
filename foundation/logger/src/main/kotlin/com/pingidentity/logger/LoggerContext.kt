/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.logger

/**
 * LoggerContext Singleton Object which allows to propagate the logger instance across current thread.
 */
object LoggerContext {

    // ThreadLocal variable to hold the Logger instance for the current thread.
    private val l = ThreadLocal<Logger>()

    /**
     * Associates the given Logger instance with the current thread.
     *
     * @param logger The Logger instance to be associated with the current thread.
     */
    fun put(logger: Logger) {
        l.set(logger)
    }

    /**
     * Retrieves the Logger instance associated with the current thread.
     * If no Logger is associated, returns the default Logger.
     *
     * @return The Logger instance associated with the current thread, or the default Logger if none is set.
     */
    fun get(): Logger {
        // If nothing in the thread local, return the default logger
        return l.get() ?: Logger.logger
    }
}