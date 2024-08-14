/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pingidentity.utils.Result
import com.pingidentity.oidc.OidcError
import com.pingidentity.samples.app.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {
    var state = MutableStateFlow(UserProfileState())
        private set

    fun userinfo() {
        viewModelScope.launch {
            User.user()?.userinfo(false).also {
                when (it) {
                    is Result.Failure ->
                        state.update { s ->
                            s.copy(user = null, error = it.value)
                        }

                    is Result.Success ->
                        state.update { s ->
                            s.copy(user = it.value, error = null)
                        }

                    null ->
                        state.update { s ->
                            s.copy(user = null, error = OidcError.Unknown(Throwable("No user found.")))
                        }
                }
            }
        }
    }
}