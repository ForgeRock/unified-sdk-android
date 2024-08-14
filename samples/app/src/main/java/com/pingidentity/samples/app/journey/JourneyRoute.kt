/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app.journey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pingidentity.samples.app.Orchestrator
import com.pingidentity.samples.app.PreferenceViewModel
import com.pingidentity.samples.app.current

@Composable
fun JourneyRoute(
    preferenceViewModel: PreferenceViewModel,
    onSubmit: (String) -> Unit
) {

    //Stateful Composable - state maintain when configuration change
    var journeyName by rememberSaveable {
        current = Orchestrator.JOURNEY
        mutableStateOf(preferenceViewModel.getLastJourney())
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = journeyName,
            onValueChange = { value -> journeyName = value },
            label = { Text("Journey Name") },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(modifier = Modifier.align(Alignment.End),
            onClick = {
                preferenceViewModel.saveJourney(journeyName)
                onSubmit(journeyName)
            }) {
            Text("Submit")
        }
    }
}