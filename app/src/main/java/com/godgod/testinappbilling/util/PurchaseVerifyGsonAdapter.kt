package com.godgod.testinappbilling.util

import com.godgod.testinappbilling.model.PurchaseVerifyState
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PurchaseVerifyGsonAdapter : JsonDeserializer<PurchaseVerifyState> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PurchaseVerifyState {
        val jsonObject = json.asJsonObject

        val isSuccessful =
            if (jsonObject.has("isSuccessful")) jsonObject.get("isSuccessful").asBoolean else false

        return if (isSuccessful) {
            PurchaseVerifyState.Success("", "")
        } else {
            PurchaseVerifyState.VerifyError(
                errorCode = if (jsonObject.has("errorCode")) jsonObject["errorCode"].asInt else 404,
                errorMessage = if (jsonObject.has("errorMessage")) jsonObject["errorMessage"].asString else ""
            )
        }
    }
}