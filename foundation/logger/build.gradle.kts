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

//apply<AndroidBuildGradlePlugin>()

android {
    namespace = "com.pingidentity.logger"
}

//ext["ARTIFACT_ID"] = "logger"
//apply("../../gradle/publish-package.gradle")

dependencies {
    testImplementation(libs.kotlin.test)
}