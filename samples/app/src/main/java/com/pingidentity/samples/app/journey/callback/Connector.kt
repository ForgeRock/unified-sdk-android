/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app.journey.callback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pingidentity.journey.callback.NameCallback
import com.pingidentity.journey.callback.PasswordCallback
import com.pingidentity.journey.callback.callbacks
import com.pingidentity.orchestrate.ContinueNode

@Composable
fun ContinueNode(
    continueNode: ContinueNode,
    onNodeUpdated: () -> Unit,
    onNext: () -> Unit,
) {
    /*
    Row(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Spacer(Modifier.width(8.dp))
        Text(
            text = connector.name,
            Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
                .weight(1f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
    }
    Row(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        Spacer(Modifier.width(8.dp))
        Text(
            text = connector.description,
            Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
                .weight(1f),
            style = MaterialTheme.typography.titleSmall,
        )
    }
     */

    Column(
        modifier =
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
    ) {
        var hasAction = false

        continueNode.callbacks.forEach {
            when (it) {
                is NameCallback -> {
                    NameCallback(it, onNodeUpdated)
                }

                is PasswordCallback -> {
                    PasswordCallback(it, onNodeUpdated)
                }
            }
        }
        if (!hasAction) {
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = onNext,
            ) {
                Text("Next")
            }
        }
    }
}

