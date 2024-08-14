/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.samples.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pingidentity.samples.app.centralize.Centralize
import com.pingidentity.samples.app.centralize.CentralizeLoginViewModel
import com.pingidentity.samples.app.davinci.DaVinci
import com.pingidentity.samples.app.journey.Journey
import com.pingidentity.samples.app.journey.JourneyRoute
import com.pingidentity.samples.app.journey.JourneyViewModel
import com.pingidentity.samples.app.token.Token
import com.pingidentity.samples.app.userprofile.UserProfile
import com.pingidentity.samples.app.userprofile.UserProfileViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Destinations.TOKEN_ROUTE,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Destinations.DAVINCI) {
            DaVinci {
                navController.navigate(Destinations.USER_INFO)
            }
        }
        composable(Destinations.TOKEN_ROUTE) {
            Token()
        }
        composable(Destinations.USER_INFO) {
            val userProfileViewModel = viewModel<UserProfileViewModel>()
            UserProfile(userProfileViewModel)
        }
        composable(Destinations.CENTRALIZE_ROUTE) {
            val centralizeLoginViewModel = viewModel<CentralizeLoginViewModel>()
            Centralize(centralizeLoginViewModel)
        }
        composable(Destinations.LAUNCH_ROUTE) {
            val preferenceViewModel = viewModel<PreferenceViewModel>(
                factory = PreferenceViewModel.factory(LocalContext.current)
            )
            JourneyRoute(
                preferenceViewModel = preferenceViewModel,
                onSubmit = { journeyName ->
                    navController.navigate(Destinations.JOURNEY_ROUTE + "/$journeyName")
                })
        }
        composable(Destinations.JOURNEY_ROUTE + "/{name}", arguments = listOf(
            navArgument("name") { type = NavType.StringType }
        )) {
            it.arguments?.getString("name")?.apply {
                val journeyViewModel = viewModel<JourneyViewModel>(
                    factory = JourneyViewModel.factory(this)
                )
                Journey(journeyViewModel) {
                    navController.navigate(Destinations.USER_INFO)
                }
            }
        }
    }
}
