/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey

import com.pingidentity.android.ModuleInitializer
import com.pingidentity.journey.callback.ChoiceCallback
import com.pingidentity.journey.callback.NameCallback
import com.pingidentity.journey.callback.PasswordCallback
import com.pingidentity.journey.callback.PollingWaitCallback
import com.pingidentity.journey.plugin.CallbackFactory

/**
 * This class is responsible for registering callbacks in the application.
 * It extends the ModuleInitializer class, which means it is part of the initialization process of the application.
 */
class CallbackRegistry : ModuleInitializer() {

    /**
     * This function is called during the initialization process of the application.
     * It registers two callbacks: NameCallback and PasswordCallback.
     */
    override fun initialize() {
        // Register NameCallback with the CallbackFactory
        CallbackFactory.register("NameCallback", ::NameCallback)

        // Register PasswordCallback with the CallbackFactory
        CallbackFactory.register("PasswordCallback", ::PasswordCallback)
        CallbackFactory.register("PollingWaitCallback", ::PollingWaitCallback)
        CallbackFactory.register("ChoiceCallback", ::ChoiceCallback)
    }
}