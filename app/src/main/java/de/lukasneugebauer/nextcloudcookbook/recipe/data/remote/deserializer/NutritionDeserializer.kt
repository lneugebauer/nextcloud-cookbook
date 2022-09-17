package de.lukasneugebauer.nextcloudcookbook.recipe.data.remote.deserializer

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.stream.JsonToken
import de.lukasneugebauer.nextcloudcookbook.recipe.data.dto.NutritionDto
import java.io.StringReader
import java.lang.reflect.Type

class NutritionDeserializer : JsonDeserializer<NutritionDto?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NutritionDto? {
        val gson = Gson()
        val reader = gson.newJsonReader(StringReader(json.toString()))
        return if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            null
        } else {
            gson.fromJson<NutritionDto>(json, typeOfT)
        }
    }
}
