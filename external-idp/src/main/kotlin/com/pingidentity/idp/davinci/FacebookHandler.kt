/*
 * Copyright (c) 2024 PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.idp.davinci

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.os.OperationCanceledException
import androidx.activity.ComponentActivity
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.pingidentity.android.ContextProvider
import com.pingidentity.davinci.plugin.DaVinci
import com.pingidentity.exception.ApiException
import com.pingidentity.idp.UnsupportedIdPException
import com.pingidentity.orchestrate.Request
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A handler class for managing Facebook Identity Provider (IdP) authorization.
 *
 * @property davinci The DaVinci instance used for making HTTP requests and handling configurations.
 */
class FacebookHandler(val davinci: DaVinci) : IdpHandler {

    /**
     * Authorizes a user by using the Facebook SDK.
     *
     * @param url The URL to which the authorization request is made.
     * @return A [Request] object that can be used to continue the DaVinci flow.
     * @throws UnsupportedIdPException if the Facebook SDK is not available.
     * @throws ApiException if the HTTP response status is not successful.
     */
    override suspend fun authorize(url: String): Request {

        try {
            Class.forName("com.facebook.login.LoginManager")
        } catch (e: ClassNotFoundException) {
            throw UnsupportedIdPException("Google SDK is not available.")
        }

        LoginManager.getInstance().logOut()

        // Make a request to the given URL to retrieve the Facebook Client login information
        val response = davinci.config.httpClient.get(url) {
            header("x-requested-with", "forgerock-sdk")
            header("Accept", "application/json")
        }

        if (response.status.isSuccess()) {
            with(response) {
                //although the response include the client id, we are not using it,
                // Facebook SDK requires the client id to be set in the String.xml
                val json = Json.parseToJsonElement(call.body()).jsonObject
                //The next link after authenticate with Facebook
                val next =
                    json["_links"]?.jsonObject?.get("next")?.jsonObject?.get("href")?.jsonPrimitive?.content
                        ?: throw IllegalStateException("Next URL not found")
                //Should include email and public_profile
                val scopes =
                    json["idp"]?.jsonObject?.get("scopes")?.jsonArray?.map { it.jsonPrimitive.content }
                        ?: emptyList()

                try {
                    val result = performFacebookLogin(scopes)
                    return Request().apply {
                        url(next)
                        header("Accept", "application/json")
                        body(buildJsonObject {
                            put("accessToken", result.accessToken.token)
                        })
                    }
                } finally {
                    FacebookCallbackManager.unregister()
                }
            }
        } else {
            throw ApiException(response.status.value, response.body())
        }

    }

    /**
     * Performs the Facebook login process.
     *
     * @param scopes The list of scopes required for the login.
     * @return A [LoginResult] object containing the login result.
     */
    private suspend fun performFacebookLogin(scopes: List<String>): LoginResult =
        withContext(Dispatchers.Default) {
            suspendCancellableCoroutine { continuation ->
                val callbackManager = create()
                FacebookCallbackManager.register(callbackManager)
                LoginManager.getInstance()
                    .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult) {
                            continuation.resume(result) // Resume coroutine on success
                        }

                        override fun onCancel() {
                            continuation.resumeWithException(OperationCanceledException("Login cancelled"))
                        }

                        override fun onError(error: FacebookException) {
                            continuation.resumeWithException(error) // Resume with exception on error
                        }
                    })
                FacebookActivity.login(scopes)
            }
        }
}

/**
 * An object for managing Facebook callback registration and unregistration.
 */
internal object FacebookCallbackManager {
    var callbackManager: CallbackManager? = null

    /**
     * Registers the given callback manager.
     *
     * @param callbackManager The callback manager to register.
     */
    fun register(callbackManager: CallbackManager) {
        FacebookCallbackManager.callbackManager = callbackManager
    }

    /**
     * Unregisters the current callback manager.
     */
    fun unregister() {
        callbackManager?.let {
            LoginManager.getInstance().unregisterCallback(it)
        }
        callbackManager = null
    }
}

/**
 * An activity for handling Facebook login.
 */
class FacebookActivity : ComponentActivity() {

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcher = registerForActivityResult(
            LoginManager.getInstance()
                .createLogInActivityResultContract(FacebookCallbackManager.callbackManager)
        ) {
            finish()
        }

        launcher.launch(listOf("public_profile", "email"))
    }

    /**
     * Unregisters the Facebook callback manager when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        FacebookCallbackManager.unregister()
    }

    companion object {

        private const val EXTRA_SCOPES = "EXTRA_SCOPES"

        /**
         * Starts the Facebook login activity with the given scopes.
         *
         * @param scopes The list of scopes required for the login.
         */
        fun login(scopes: List<String>) {
            val intent = Intent(ContextProvider.context, FacebookActivity::class.java).apply {
                flags = FLAG_ACTIVITY_NEW_TASK
                putStringArrayListExtra(EXTRA_SCOPES, ArrayList(scopes))
            }
            ContextProvider.context.startActivity(intent)
        }
    }
}