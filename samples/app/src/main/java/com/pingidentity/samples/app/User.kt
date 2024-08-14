/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app

import com.pingidentity.davinci.davinciUser
import com.pingidentity.journey.journeyUser
import com.pingidentity.oidc.OidcUser
import com.pingidentity.oidc.User
import com.pingidentity.samples.app.centralize.oidcClient
import com.pingidentity.samples.app.davinci.daVinci
import com.pingidentity.samples.app.journey.journey

enum class Orchestrator {
    JOURNEY,
    DAVINCI,
    CENTRALIZE
}

var current = Orchestrator.DAVINCI

object User {
    suspend fun user() : User? {
        return when (current) {
            Orchestrator.DAVINCI -> {
                daVinci.davinciUser()
            }
            Orchestrator.JOURNEY -> {
                journey.journeyUser()
            }
            Orchestrator.CENTRALIZE -> {
                return OidcUser(oidcClient)
            }
        }
    }
}