<div>
  <picture>
     <img src="https://www.pingidentity.com/content/dam/ping-6-2-assets/topnav-json-configs/Ping-Logo.svg" width="80" height="80"  alt=""/>
  </picture>
</div>

# Ping External IDP

## Overview

Ping External IDP is a library that allows you to authenticate with Ping Identity's External IDP, for example, Google, Facebook, Apple, etc...
This library act as a plugin to the `ping-davinci` and `ping-journey` library,
and it provides the necessary configuration to authenticate with the External IDP.

<img src="images/socialLogin.png" width="250">

## Add dependency to your project

```kotlin
dependencies {
    implementation(project(":ping-davinci"))
    implementation(project("::ping-external-idp"))
}
```

## Usage

To use the `ping-enternal-idp` with `IdpCollector`, you need to integrate with `DaVinci` module.
Setup `PingOne` with `External IDPs` and a `DaVinci` flow.

### PingOne External IDPs Setup

<img src="images/externalIdps.png" width="500">

### DaVinci Flow Setup

<img src="images/htmlTemplate.png" width="500">

### App Redirect Uri Setup

In the App `gradle.build.kts` file, add the following `manifestPlaceholders` to the `android.defaultConfig`:

```json
android {
    defaultConfig {
        manifestPlaceholders["appRedirectUriScheme"] = "myapp"
    }
}
```


Here's an example of how to use the `IdpCollector` instance:

```kotlin
var node = daVinci.start()

if (node is Connector) {
    node.collectors.forEach {
        when (it) {
            is IdpCollector -> {
                when (val result = idpCollector.authorize()) {
                    is Success -> {
                        //When success, move to next Node
                        node.next()
                    }
                    is Failure -> {
                        //Handle the failure
                    }
                }
            }
        }
    }
}
```

Simply call `idpCollector.authorize()` method to start the authentication flow with the External IDP,
the `authorize()` will launch a `CustomTab` to authenticate with the External IDP,
once the authentication is successful, it will return a `Success` result with `continueToken`,
otherwise, it will return a `Failure` Node with `Throwable`.

```kotlin
when (val result = idpCollector.authorize()) {
    is Success -> {
        result.value //This is the continueToken
    }
    is Failure -> {
        result.value //This is the Throwable
    }
}
```

The `continueToken` is a unique token that can be used to continue the flow with `DaVinci` module,
and the SDK will automatically handle the continueToken when you call `node.next()`.

### More IdpCollector Configuration

You can customize the `customTab` from `IdpCollector` by using the following methods:

```kotlin
idpCollector.authorize {
    setShowTitle(false)
    setColorScheme(CustomTabsIntent.COLOR_SCHEME_DARK)
    setUrlBarHidingEnabled(true)
}
```