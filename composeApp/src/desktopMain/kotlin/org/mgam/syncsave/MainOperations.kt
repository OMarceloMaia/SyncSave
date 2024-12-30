package org.mgam.syncsave

import kotlinx.serialization.json.Json
import org.mgam.syncsave.files.drive.getOrCreateFolder
import org.mgam.syncsave.files.drive.uploadFileToGoogleDrive
import org.mgam.syncsave.files.local.GameConfig
import org.mgam.syncsave.google.oauth.UserSession
import org.mgam.syncsave.google.oauth.authenticate
import org.mgam.syncsave.google.oauth.log
import java.io.File

fun backup() {
    val (user, gamesConfig) = configSetup()

    // Determine the OS type and set the path key
    val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

    for (game in gamesConfig) {
        // Choose the correct path based on the OS
        val filePath = if (isWindows) game.windowsPath else game.linuxPath
        val file = File(filePath)

        if (file.exists()) {
            val mainFolderId = getOrCreateFolder(token = user.token, folderName = "SyncSave")
            val folderId = getOrCreateFolder(token = user.token, folderName = game.gameName, parentFolderId = mainFolderId)

            if(folderId != null) {
                // Upload file to Google Drive
                val response = uploadFileToGoogleDrive(
                    token = user.token,
                    file = file,
                    folderId = folderId
                )
            }
            log.info("File ${file.name} uploaded successfully.")
        } else {
            log.info("The file ${file.name} does not exist at path: $filePath.")
        }
    }
}

fun restore() {

}

fun configSetup(): Pair<UserSession, MutableList<GameConfig>> {
    // Load user session and games configuration
    val user = Json.decodeFromString<UserSession>(File("../syncsave-config.json").readText())
    val gamesConfig = Json.decodeFromString<MutableList<GameConfig>>(File("../games-config.json").readText())

    return Pair(user, gamesConfig)
}

fun login(onResult: (Boolean) -> Unit) {
    var isAuthenticated = false

    authenticate { success ->
        isAuthenticated = success
        onResult(isAuthenticated)
    }
}