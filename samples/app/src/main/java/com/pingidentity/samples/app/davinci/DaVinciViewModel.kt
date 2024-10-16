/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app.davinci

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pingidentity.davinci.DaVinci
import com.pingidentity.davinci.module.Oidc
import com.pingidentity.logger.Logger
import com.pingidentity.logger.STANDARD
import com.pingidentity.orchestrate.ContinueNode
import com.pingidentity.samples.app.Mode
import com.pingidentity.samples.app.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Use DataStore to store the AccessToken

//TODO: Integration Point. STEP 1
val daVinci = DaVinci {
    logger = Logger.STANDARD

    // Oidc as module
    module(Oidc) {
        clientId = "<Client ID>"
        discoveryEndpoint = "<Discovery Endpoint>"
        scopes = mutableSetOf("<scope1>", "<scope2>", "...")
        redirectUri = "<Redirect URI>"
    }
}

class DaVinciViewModel : ViewModel() {
    var state = MutableStateFlow(DaVinciState())
        private set

    var loading = MutableStateFlow(false)
        private set

    init {
        start()
    }

    fun next(current: ContinueNode) {
        loading.update {
            true
        }
        viewModelScope.launch {
            //TODO: Integration Point. STEP 3
            // Continue the DaVinci flow, next node from the flow will be returned
            // Update the state with the next node
            /*
            val next = current.next()
            state.update {
                it.copy(prev = current, node = next)
            }
             */
            loading.update {
                false
            }
        }
    }

    fun start() {
        loading.update {
            true
        }
        viewModelScope.launch {
            User.current(Mode.DAVINCI)

            //TODO: Integration Point. STEP 2
            // Start the DaVinci flow, next node from the flow will be returned
            // Update the state with the next node
            /*
            val next = daVinci.start()

            state.update {
                it.copy(prev = next, node = next)
            }
             */
            loading.update {
                false
            }
        }
    }

    fun refresh() {
        state.update {
            it.copy(prev = it.prev, node = it.node)
        }
    }
}
