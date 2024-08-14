/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.idp

import androidx.browser.customtabs.CustomTabsIntent
import com.pingidentity.davinci.plugin.ContinueTokenCollector
import com.pingidentity.idp.browser.BrowserLauncherActivity
import com.pingidentity.idp.browser.ContinueToken
import com.pingidentity.idp.browser.CustomTabActivity
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL


class IdpCollector : ContinueTokenCollector {

    var idpEnabled = true
    lateinit var idpId: String
    lateinit var idpType: String
    lateinit var label: String
    lateinit var link: URL

    private var continueToken: ContinueToken? = null
    override fun continueToken() = continueToken

    override fun init(input: JsonObject) {
        idpEnabled = input["idpEnabled"]?.jsonPrimitive?.boolean ?: true
        idpId = input["idpId"]?.jsonPrimitive?.content ?: ""
        idpType = input["idpType"]?.jsonPrimitive?.content ?: ""
        label = input["label"]?.jsonPrimitive?.content ?: ""
        link = URL(
            input["links"]
                ?.jsonObject?.get("authenticate")
                ?.jsonObject?.get("href")?.jsonPrimitive?.content
                ?: ""
        )
    }

    suspend fun authorize(customizer: CustomTabsIntent.Builder.() -> Unit = {}):
            Result<ContinueToken> {
        CustomTabActivity.customTabsCustomizer = customizer
        val result = BrowserLauncherActivity.authorize(link)
        result.onSuccess { continueToken = it }
        return result
    }

}