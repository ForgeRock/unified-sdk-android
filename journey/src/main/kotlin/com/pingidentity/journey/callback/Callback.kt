/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.journey.callback

import com.pingidentity.journey.plugin.Callback
import com.pingidentity.orchestrate.ContinueNode

/*
sealed interface Callback : Action {
    fun init(jsonObject: JsonObject)

    //Callback is more self-contained, it created its own json without depending on other Callback
    fun asJson(): JsonObject
}
 */

val ContinueNode.callbacks: List<Callback>
    get() = this.actions.filterIsInstance<Callback>()
