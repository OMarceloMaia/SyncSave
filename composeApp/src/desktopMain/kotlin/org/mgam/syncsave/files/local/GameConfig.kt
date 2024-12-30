package org.mgam.syncsave.files.local

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val gameName: String,
    val windowsPath: String,
    val linuxPath: String
)