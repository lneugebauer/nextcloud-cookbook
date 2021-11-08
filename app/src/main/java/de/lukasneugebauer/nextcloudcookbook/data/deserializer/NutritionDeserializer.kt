package de.lukasneugebauer.nextcloudcookbook.data.deserializer

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.stream.JsonToken
import de.lukasneugebauer.nextcloudcookbook.data.model.NutritionNw
import java.io.StringReader
import java.lang.reflect.Type

class NutritionDeserializer : JsonDeserializer<NutritionNw?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NutritionNw? {
        val gson = Gson()
        val reader = gson.newJsonReader(StringReader(json.toString()))
        return if (reader.peek() != JsonToken.BEGIN_OBJECT) {
            null
        } else {
            gson.fromJson<NutritionNw>(json, typeOfT)
        }
    }
}