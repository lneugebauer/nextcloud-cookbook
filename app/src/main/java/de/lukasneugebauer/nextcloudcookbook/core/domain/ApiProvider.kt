package de.lukasneugebauer.nextcloudcookbook.core.domain

import kotlinx.coroutines.flow.StateFlow

interface ApiProvider<T> {
    val apiFlow: StateFlow<T>

    fun initApi()

    fun resetApi()

    fun getApi(): T
}
