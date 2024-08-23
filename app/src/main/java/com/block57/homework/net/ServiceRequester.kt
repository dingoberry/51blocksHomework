package com.block57.homework.net

import com.apollographql.apollo.ApolloClient
import com.block57.homework.BuildConfig


private lateinit var clientInstance: ApolloClient

val apolloSwapiClient: ApolloClient
    get() = if (::clientInstance.isInitialized) {
        clientInstance
    } else {
        synchronized(::clientInstance) {
            if (::clientInstance.isInitialized) {
                clientInstance
            } else {
                ApolloClient.Builder().serverUrl(BuildConfig.ENDPOINT_URL).build().apply {
                    clientInstance = this
                }
            }
        }
    }