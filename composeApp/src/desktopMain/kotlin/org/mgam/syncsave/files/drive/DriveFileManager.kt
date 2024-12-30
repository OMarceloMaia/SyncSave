package org.mgam.syncsave.files.drive

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.append
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

fun getOrCreateFolder(httpClient: HttpClient = HttpClient(),
                      token: String,
                      folderName: String,
                      parentFolderId: String? = "root"): String? {
    return runBlocking {
        // Determine the parent folder ID or default to "root" if not provided
        val parentId = parentFolderId ?: "root"

        // Build the query to search for the folder by name within the specified parent folder
        val query = "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder' and '$parentId' in parents"

        // Search for an existing folder by name within the parent folder
        val searchResponse: HttpResponse = httpClient.request("https://www.googleapis.com/drive/v3/files") {
            method = HttpMethod.Get
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
            parameter("q", query)
            parameter("spaces", "drive")
            parameter("fields", "files(id, name)")
        }

        val responseText = searchResponse.bodyAsText()
        val files = parseDriveFiles(responseText) // Function to parse JSON and get the folder ID

        if (files.isNotEmpty()) {
            println("Folder '$folderName' found with ID: ${files[0].id}")
            files[0].id // Return the found folder ID
        } else {
            println("Folder '$folderName' not found in parent ID '$parentId'. Creating new folder.")

            // If the folder does not exist, create a new one within the specified parent folder
            val createResponse: HttpResponse = httpClient.request("https://www.googleapis.com/drive/v3/files") {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                }
                setBody(
                    """
                    {
                        "name": "$folderName",
                        "mimeType": "application/vnd.google-apps.folder",
                        "parents": ["$parentId"]
                    }
                    """.trimIndent()
                )
            }
            val createResponseText = createResponse.bodyAsText()
            val createdFolder = parseDriveFile(createResponseText) // Function to get the ID of the newly created folder

            if (createdFolder.id == null) {
                println("Error: Failed to create folder '$folderName'. Response: $createResponseText")
                return@runBlocking null
            } else {
                println("Folder '$folderName' created with ID: ${createdFolder.id}")
                createdFolder.id
            }
        }
    }
}



//fun getOrCreateFolder(httpClient: HttpClient, token: String, folderName: String): String? {
//    return runBlocking {
//        // Search for an existing folder by name
//        val searchResponse: HttpResponse = httpClient.request("https://www.googleapis.com/drive/v3/files") {
//            method = HttpMethod.Get
//            headers {
//                append(HttpHeaders.Authorization, "Bearer $token")
//            }
//            parameter("q", "name = '$folderName' and mimeType = 'application/vnd.google-apps.folder'")
//            parameter("spaces", "drive")
//            parameter("fields", "files(id, name)")
//        }
//
//        val responseText = searchResponse.bodyAsText()
//        val files = parseDriveFiles(responseText) // Function to parse JSON and get the folder ID
//
//        if (files.isNotEmpty()) {
//            files[0].id // Return the found folder ID
//        } else {
//            // If the folder does not exist, create a new one
//            val createResponse: HttpResponse = httpClient.request("https://www.googleapis.com/drive/v3/files") {
//                method = HttpMethod.Post
//                headers {
//                    append(HttpHeaders.Authorization, "Bearer $token")
//                    append(HttpHeaders.ContentType, ContentType.Application.Json)
//                }
//                setBody(
//                    """
//                    {
//                        "name": "$folderName",
//                        "mimeType": "application/vnd.google-apps.folder"
//                    }
//                    """.trimIndent()
//                )
//            }
//            val createResponseText = createResponse.bodyAsText()
//            val createdFolder = parseDriveFile(createResponseText) // Function to get the ID of the newly created folder
//            createdFolder.id
//        }
//    }
//}

fun parseDriveFiles(responseText: String): List<DriveFile> {
    val json = Json { ignoreUnknownKeys = true }
    val filesJsonArray = json.decodeFromString<JsonObject>(responseText)["files"]?.jsonArray

    return filesJsonArray?.mapNotNull { element ->
        json.decodeFromJsonElement<DriveFile>(element).takeIf { it.id != null && it.name != null }
    } ?: emptyList()
}

fun parseDriveFile(responseText: String): DriveFile {
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(responseText)
}