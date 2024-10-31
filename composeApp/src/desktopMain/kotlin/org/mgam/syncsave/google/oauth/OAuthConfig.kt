package org.mgam.syncsave.google.oauth

import kotlinx.serialization.Serializable

@Serializable
data class OAuthConfig(
    val web: WebConfig
)

@Serializable
data class WebConfig(
    val client_id: String,
    val project_id: String,
    val auth_uri: String,
    val token_uri: String,
    val auth_provider_x509_cert_url: String,
    val client_secret: String,
    val redirect_uris: List<String>,
    val javascript_origins: List<String>
)
