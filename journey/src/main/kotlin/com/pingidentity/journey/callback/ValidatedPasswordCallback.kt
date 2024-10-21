package com.pingidentity.journey.callback

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

class ValidatedPasswordCallback : AbstractValidatedCallback() {
    var prompt: String = ""
        private set

    var echoOn = false
        private set

    var username: String = ""

    var password: CharArray = CharArray(0)

    override fun onAttribute(name: String, value: JsonElement) {
        super.onAttribute(name, value)
        when (name) {
            "prompt" -> this.prompt = value.jsonPrimitive.content
            "echoOn" -> this.echoOn = value.jsonPrimitive.boolean
        }
    }

    override fun asJson() = input(String(password), inputValidateOnly)


}