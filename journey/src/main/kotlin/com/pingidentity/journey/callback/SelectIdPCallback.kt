package com.pingidentity.journey.callback

import com.pingidentity.journey.plugin.AbstractCallback
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Callback to collect an Identity Provider
 */
class SelectIdPCallback : AbstractCallback() {

    var providers = emptyList<IdPValue>()
    private set

    var value: String= ""

    override fun onAttribute(name: String, value: JsonElement) {
        when (name) {
            "providers" -> {
                providers = value.jsonArray.map {
                    IdPValue(it.jsonObject)
                }
            }
        }
    }

    override fun asJson(): JsonObject {
        return input(value)
    }

    class IdPValue(jsonObject: JsonObject) {
        private var provider: String = ""
        private var uiConfig: JsonObject?

        init {
            this.provider = jsonObject["provider"]?.jsonPrimitive?.content ?: ""
            this.uiConfig = jsonObject["uiConfig"]?.jsonObject
        }
    }

}