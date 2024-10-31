package org.mgam.syncsave.google.oauth

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val state: String,
    val token: String
)