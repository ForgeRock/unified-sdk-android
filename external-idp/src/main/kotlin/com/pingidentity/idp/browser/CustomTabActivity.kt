/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.idp.browser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent

/**
 * This activity is responsible for launching the CustomTabsIntent with the provided URL.
 */
class CustomTabActivity : ComponentActivity() {

    companion object {
        internal var customTabsCustomizer: CustomTabsIntent.Builder.() -> Unit = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            //Already launch the browser, don't do it again.
            intent.removeExtra(URL)
        }
    }

    override fun onResume() {
        super.onResume()
        val url = intent.extras?.getString(URL)
        if (intent?.data != null) {
            setResult(RESULT_OK, Intent().setData(intent?.data))
            finish()
        } else if (url == null) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            intent.removeExtra(URL)
            val builder = CustomTabsIntent.Builder()
            builder.customTabsCustomizer()
            val customTabsIntent = builder.build()
            //What if ActivityNotFound?
            customTabsIntent.launchUrl(this, Uri.parse(url))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}