package org.mgam.syncsave.google.oauth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.request.uri
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.p
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.util.UUID
import kotlin.collections.listOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set

val log = LoggerFactory.getLogger("MyLogger")
val server = embeddedServer(Netty, port = 8080, module = Application::main)
lateinit var serverJob: Job

fun authenticate (onResult: (Boolean) -> Unit) {
    runBlocking {
        serverJob = launch {
            server.start(wait = false)
            try {
                delay(Long.MAX_VALUE)
            } finally {
                log.info("Shutting down server...")
                server.stop(gracePeriodMillis = 1000, timeoutMillis = 1000)
                log.info("Server has been stopped.")
                onResult(true)
            }
        }
        delay(1000)
        java.awt.Desktop.getDesktop().browse(java.net.URI( "http://localhost:8080/login"))
    }
}

suspend fun stopServer() {
    log.info("Cancelling server...")
    serverJob.cancelAndJoin()
}

val applicationHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

private fun getClientSecret(): OAuthConfig {
    return Json.decodeFromString<OAuthConfig>(File("../client_secret.json").readText())
}

fun Application.main(httpClient: HttpClient = applicationHttpClient) {
    val config = getClientSecret()

    install(Sessions) {
        cookie<UserSession>("user_session")
    }
    val redirects = mutableMapOf<String, String>()
    install(Authentication) {
        oauth("auth-oauth-google") {
            // Configure oauth authentication
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = config.web.client_id,
                    clientSecret = config.web.client_secret,
                    defaultScopes = listOf(
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/drive.file"
                    ),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = httpClient
        }
    }
    routing {
        authenticate("auth-oauth-google") {
            get("/login") {
                log.info("PATH /login")
                // Redirects to 'authorizeUrl' automatically
            }

            get("/callback") {
                log.info("PATH /callback")
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                // redirects home if the url is not found before authorization
                currentPrincipal?.let { principal ->
                    principal.state?.let { state ->
                        call.sessions.set(UserSession(state, principal.accessToken))
                        redirects[state]?.let { redirect ->
                            call.respondRedirect(redirect)
                            return@get
                        }
                    }
                }
                call.respondRedirect("/home")
            }
        }
        get("/") {
            log.info("PATH /")
            call.respondHtml {
                body {
                    p {
                        a("/login") { +"Login with Google" }
                    }
                }
            }
        }
        get("/home") {
            log.info("PATH /home")
            val userSession: UserSession? = getSession(call)
            if (userSession != null) {
                val userInfo: UserInfo = getPersonalGreeting(httpClient, userSession)
                call.respondText("Hello, ${userInfo.name}! Welcome home!")

                val jsonString = Json.encodeToString(userSession)
                File("../syncsave-config.json").writeText(jsonString)
            }
            stopServer()
        }

//        get("/{path}") {
//            val userSession: UserSession? = getSession(call)
//            if (userSession != null) {
//                val userInfo: UserInfo = getPersonalGreeting(httpClient, userSession)
//                call.respondText("Hello, ${userInfo.name}!")
//            }
//        }
    }
}

private suspend fun getPersonalGreeting(
    httpClient: HttpClient,
    userSession: UserSession
): UserInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
    headers {
        append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
    }
}.body()

private suspend fun getSession(
    call: ApplicationCall
): UserSession? {
    val userSession: UserSession? = call.sessions.get()
    //if there is no session, redirect to login
    if (userSession == null) {
        val redirectUrl = URLBuilder("http://localhost:8080/login").run {
            parameters.append("redirectUrl", call.request.uri)
            build()
        }
        call.respondRedirect(redirectUrl)
        return null
    }
    return userSession
}
