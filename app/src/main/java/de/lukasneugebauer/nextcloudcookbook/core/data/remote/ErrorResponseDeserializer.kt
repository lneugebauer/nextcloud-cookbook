package de.lukasneugebauer.nextcloudcookbook.core.data.remote

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import de.lukasneugebauer.nextcloudcookbook.core.data.remote.response.ErrorResponse
import timber.log.Timber
import java.io.StringReader
import java.lang.reflect.Type

class ErrorResponseDeserializer: JsonDeserializer<ErrorResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ErrorResponse {
        val gson = Gson()
        val reader = gson.newJsonReader(StringReader(json.toString()))
        return when(reader.peek()) {
            JsonToken.BEGIN_OBJECT -> gson.fromJson(json, typeOfT)
            JsonToken.STRING -> ErrorResponse(msg = gson.fromJson(json, String::class.java))
            else -> {
                Timber.e("1722283362: Could not deserialize error response!")
                ErrorResponse(msg = "")
            }
        }
    }
}