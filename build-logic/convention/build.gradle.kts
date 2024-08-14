/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.dokka.gradlePlugin)
    //https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "com.pingidentity.convention.android.library"
            implementationClass = "com.pingidentity.convention.AndroidLibraryConventionPlugin"
        }
        register("centralPublish") {
            id = "com.pingidentity.convention.centralPublish"
            implementationClass = "com.pingidentity.convention.MavenCentralPublishConventionPlugin"
        }
    }
}