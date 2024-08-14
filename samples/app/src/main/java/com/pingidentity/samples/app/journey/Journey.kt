/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app.journey

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pingidentity.orchestrate.Connector
import com.pingidentity.orchestrate.Error
import com.pingidentity.orchestrate.Failure
import com.pingidentity.orchestrate.Success
import com.pingidentity.samples.app.R
import com.pingidentity.samples.app.journey.callback.Connector

@Composable
fun Journey(
    journeyViewModel: JourneyViewModel,
    onSuccess: (() -> Unit)? = null,
) {
    BackHandler {
        journeyViewModel.start()
    }

    val state by journeyViewModel.state.collectAsState()
    val loading by journeyViewModel.loading.collectAsState()
    val currentOnSuccess by rememberUpdatedState(onSuccess)

    Journey(
        state = state,
        loading = loading,
        onNodeUpdated = {
            journeyViewModel.refresh()
        },
        onNext = {
            journeyViewModel.next(it)
        },
        currentOnSuccess,
    )
}

@Composable
fun Journey(
    state: JourneyState,
    loading: Boolean,
    onNodeUpdated: () -> Unit,
    onNext: (Connector) -> Unit,
    onSuccess: (() -> Unit)?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (loading) {
            CircularProgressIndicator()
        }

        Column(
            modifier =
            Modifier
                .padding(8.dp)
                .fillMaxSize(),
        ) {
            Logo(modifier = Modifier)

            when (val node = state.node) {
                is Connector -> {
                    Render(node = node, onNodeUpdated) {
                        onNext(node)
                    }
                }

                is Error -> {
                    Log.e("Journey", node.cause.message, node.cause)
                    Render(node = node)
                }

                is Failure -> {
                    // TODO For Journey, we many not need to render the Failure node
                    Render(node)
                }

                is Success -> {
                    LaunchedEffect(true) {
                        onSuccess?.let { onSuccess() }
                    }
                }

                null -> {}
            }
        }
    }
}

@Composable
fun Render(node: Error) {
    Row(
        modifier =
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Card(
            elevation =
            CardDefaults.cardElevation(
                defaultElevation = 10.dp,
            ),
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Error, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${node.cause}",
                    Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun Render(node: Failure) {
    Row(
        modifier =
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Card(
            elevation =
            CardDefaults.cardElevation(
                defaultElevation = 10.dp,
            ),
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Error, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = node.message,
                    Modifier
                        .weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
fun Render(
    node: Connector,
    onNodeUpdated: () -> Unit,
    onNext: () -> Unit,
) {
    Connector(node, onNodeUpdated, onNext)
}

@Composable
private fun Logo(modifier: Modifier) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        Spacer(modifier = Modifier.weight(1f, true))
        Icon(
            painterResource(R.drawable.ping_logo),
            contentDescription = null,
            modifier =
            Modifier
                .height(100.dp)
                .padding(8.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .then(modifier),
            tint = Color.Unspecified,
        )
        Spacer(modifier = Modifier.weight(1f, true))
    }
}