package org.mgam.syncsave.files.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun addGameToConfig(gameName: String, windowPath: String, linuxPath: String) {
    val json = File("../games-config.json")

    if (json.exists()) {
        val gamesConfig = Json.decodeFromString<MutableList<GameConfig>>(json.readText())
        gamesConfig.add(GameConfig(gameName,windowPath, linuxPath))
        json.writeText(Json.encodeToString(gamesConfig))
    } else {
        val game = listOf( GameConfig(gameName, windowPath, linuxPath))
        val gamesConfig = Json.encodeToString(game)
        File("../games-config.json").writeText(gamesConfig)
    }
}