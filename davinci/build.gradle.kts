/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

plugins {
    id("com.pingidentity.convention.android.library")
    id("com.pingidentity.convention.centralPublish")
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
}

description = "DaVinci library"

android {
    namespace = "com.pingidentity.davinci"

    unitTestVariants.all {
        this.mergedFlavor.manifestPlaceholders["appAuthRedirectScheme"] = "com.pingidentity.demo"
    }

    testVariants.all {
        this.mergedFlavor.manifestPlaceholders["appAuthRedirectScheme"] = "com.pingidentity.demo"
    }
}

dependencies {
    api(project(":foundation:orchestrate"))
    api(project(":foundation:davinci-plugin"))
    api(project(":foundation:utils"))
    api(project(":foundation:oidc"))
    api(project(":foundation:logger"))
    implementation(project(":foundation:android"))
    implementation(project(":foundation:storage"))

    implementation(libs.ktor.client.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.test.runner)

    testImplementation(project(":foundation:testrail"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)

    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
