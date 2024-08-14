/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.oidc

import com.pingidentity.exception.ApiException
import com.pingidentity.oidc.exception.AuthorizeException
import com.pingidentity.utils.Result.*
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OidcErrorTest {

    @Test
    fun `catch should return success result for successful block execution`() {
        val result = catch { "success" }
        assertTrue(result is Success)
        assertEquals("success", result.value)
    }

    @Test
    fun `catch should return ApiError for ApiException`() {
        val exception = ApiException(404, "Not Found")
        val result = catch { throw exception }
        assertTrue(result is Failure)
        assertTrue((result).value is OidcError.ApiError)
        assertEquals(404, (result.value as OidcError.ApiError).code)
        assertEquals("Not Found", (result.value as OidcError.ApiError).message)
    }

    @Test
    fun `catch should return AuthorizeError for AuthorizeException`() {
        val exception = AuthorizeException("Authorization failed")
        val result = catch { throw exception }
        assertTrue(result is Failure)
        assertTrue((result).value is OidcError.AuthorizeError)
        assertEquals(exception, (result.value as OidcError.AuthorizeError).cause)
    }

    @Test
    fun `catch should return NetworkError for IOException`() {
        val exception = IOException("Network error")
        val result = catch { throw exception }
        assertTrue(result is Failure)
        assertTrue((result).value is OidcError.NetworkError)
        assertEquals(exception, (result.value as OidcError.NetworkError).cause)
    }

    @Test
    fun `catch should return Unknown for other exceptions`() {
        val exception = RuntimeException("Unknown error")
        val result = catch { throw exception }
        assertTrue(result is Failure)
        assertTrue((result).value is OidcError.Unknown)
        assertEquals(exception, (result.value as OidcError.Unknown).cause)
    }
}