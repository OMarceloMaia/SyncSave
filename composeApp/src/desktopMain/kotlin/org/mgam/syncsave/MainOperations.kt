package org.mgam.syncsave

import kotlinx.serialization.json.Json
import org.mgam.syncsave.files.drive.uploadFileToGoogleDrive
import org.mgam.syncsave.google.oauth.UserSession
import org.mgam.syncsave.google.oauth.authenticate
import org.mgam.syncsave.google.oauth.log
import java.io.File

fun backup() {
    val user = Json.decodeFromString<UserSession>(File("../syncsave-config.json").readText())

    val filePath = "/home/marcelomaia/projects/Captura.jpeg"
    val file = File(filePath)

    if (file.exists()) {
        val response = uploadFileToGoogleDrive(
            token = user.token,
            file = file,
            folderId = "1sF3uEuG_PBbvf2RAGYUdE8Q7PBimRvIF"
        )
    } else {
        log.info("The file does not exist.")
    }
}

fun restore() {

}

fun login(onResult: (Boolean) -> Unit) {
    var isAuthenticated = false

    authenticate { success ->
        isAuthenticated = success
        onResult(isAuthenticated)
    }
}