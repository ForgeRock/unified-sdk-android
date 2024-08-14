/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey.module

import com.pingidentity.journey.journeyConfig
import com.pingidentity.orchestrate.Module

val Start = Module.of {

    start {
        if (workflow.journeyConfig().hasJourneyName() && it.hasUrl.not()) {
            it.url("${workflow.journeyConfig().serverUrl}/json/realms/${workflow.journeyConfig().realm}/authenticate")
            it.parameter("authIndexType", "service")
            it.parameter("authIndexValue", workflow.journeyConfig().journeyName)
            if (workflow.journeyConfig().forceAuth) it.parameter("ForceAuth", "true")
            if (workflow.journeyConfig().noSession) it.parameter("noSession", "true")
            it.body()
        }
        it
    }

}