package org.mgam.syncsave.files.drive

import kotlinx.serialization.Serializable

@Serializable
data class DriveFile(
    val id: String? = null,
    val name: String? = null
)