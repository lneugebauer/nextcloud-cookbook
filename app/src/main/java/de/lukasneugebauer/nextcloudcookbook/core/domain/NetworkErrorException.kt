package de.lukasneugebauer.nextcloudcookbook.core.domain

class NetworkErrorException(
    cause: Throwable,
) : Exception(cause)
