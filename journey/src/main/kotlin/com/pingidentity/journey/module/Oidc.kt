/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey.module

import com.pingidentity.journey.Journey
import com.pingidentity.journey.journeyConfig
import com.pingidentity.journey.prepareUser
import com.pingidentity.journey.user
import com.pingidentity.oidc.OidcClient
import com.pingidentity.oidc.OidcClientConfig
import com.pingidentity.oidc.OidcUser
import com.pingidentity.orchestrate.Module
import com.pingidentity.orchestrate.SuccessNode
import kotlin.collections.set

private const val OIDC_CLIENT_CONFIG = "com.pingidentity.journey.OIDC_CLIENT_CONFIG"

val Oidc =
    Module.of(::OidcClientConfig) {

        init {
            // propagate the configuration from workflow to the module
            config.httpClient = workflow.config.httpClient
            config.logger = workflow.config.logger

            sharedContext[OIDC_CLIENT_CONFIG] = config
            config.init()
        }

        start { request ->

            // When user starting the flow again, revoke previous token if exists
            workflow.user()?.revoke()
            request
        }

        success { success ->
            SuccessNode(success.input,
                prepareUser(
                    workflow,
                    OidcUser(workflow.oidcClientConfig()),
                    success.session
                )
            )
        }

        signOff { request ->
            request.url(config.openId.endSessionEndpoint)
            OidcClient(config).endSession {
                request.parameter("id_token_hint", it)
                request.parameter("client_id", config.clientId)
                true
            }
            request
        }
    }

fun Journey.oidcClientConfig(): OidcClientConfig {
    sharedContext.getValue<OidcClientConfig>(OIDC_CLIENT_CONFIG)?.let {
        it.clone().also { clone ->
            clone.updateAgent(sessionAgent(journeyConfig().cookie) {
                session()
            })
            return clone
        }
    }
    throw IllegalStateException("Oidc module is not initialized")
}