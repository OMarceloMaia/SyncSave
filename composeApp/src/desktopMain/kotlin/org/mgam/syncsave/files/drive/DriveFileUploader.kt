package org.mgam.syncsave.files.drive

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import org.mgam.syncsave.google.oauth.log
import java.io.File
import java.nio.file.Files
import java.util.UUID

fun uploadFileToGoogleDrive(
    httpClient: HttpClient = HttpClient(),
    token: String,
    file: File,
    folderId: String
): String {
    return runBlocking {
        // Detect the file's MIME type
        val fileType = Files.probeContentType(file.toPath()) ?: "application/octet-stream"

        // Metadata in JSON format
        val metadataJson = """
            {
                "name": "${file.name}",
                "mimeType": "$fileType",
                "parents": ["$folderId"]
            }
        """.trimIndent()

        // Generate the boundary for multipart
        val boundary = UUID.randomUUID().toString()

        // Prepare the multipart request
        val uploadResponse: HttpResponse = httpClient.request("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart") {
            method = HttpMethod.Post
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "multipart/related; boundary=$boundary")
            }

            // Request body
            setBody(
                buildString {
                    // Metadata
                    append("--$boundary\r\n")
                    append("Content-Type: application/json; charset=UTF-8\r\n")
                    append("Content-Disposition: form-data; name=\"metadata\"\r\n\r\n")
                    append(metadataJson)
                    append("\r\n")

                    // File
                    append("--$boundary\r\n")
                    append("Content-Type: $fileType\r\n")
                    append("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n\r\n")
                    append(file.readText())  // File converted to text
                    append("\r\n")

                    // End the multipart
                    append("--$boundary--\r\n")
                }
            )
        }

        // Check the response
        val statusCode = uploadResponse.status
        val responseText = uploadResponse.bodyAsText()

        if (statusCode.isSuccess()) {
            log.info("Upload successful.")
        } else {
            log.info("Upload error: $responseText")
        }
        responseText
    }
}