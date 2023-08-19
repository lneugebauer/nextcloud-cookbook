package de.lukasneugebauer.nextcloudcookbook.recipe.util

import de.lukasneugebauer.nextcloudcookbook.recipe.domain.model.DurationComponents
import java.time.Duration
import kotlin.time.toKotlinDuration

fun Duration.toDurationComponents(): DurationComponents = this.toKotlinDuration().toComponents { hours, minutes, _, _ ->
    return DurationComponents(hours.toString(), minutes.toString())
}
