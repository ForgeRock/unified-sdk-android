package com.pingidentity.journey

import com.pingidentity.orchestrate.Session
import kotlinx.serialization.Serializable

@Serializable
sealed interface SSOToken : Session {
    val successUrl: String
    val realm: String
}

@Serializable
internal data class SSOTokenImpl(
    override val value: String,
    override val successUrl: String,
    override val realm: String
) : SSOToken


internal data object EmptySSOToken : SSOToken {
    override val value: String = ""
    override val successUrl: String = ""
    override val realm: String = ""
}