package at.troebinger.mc

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.routing
import kotlinx.html.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.Instant

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {

        // get id for upload
        get("/upload/new") {
            val id = getId()
            File("/storage/$id").mkdirs()
            call.respondText(id, contentType = ContentType.Text.Plain)
        }

        // add md file to storage
        put("/upload/{id}/md") {
            val id = call.parameters["id"]

            // instantly unmarshal json to FileDetails object
            val details = call.receive<FileDetails>()

            val file = File("/storage/${id}/md.md")
            file.writeText(details.content)

            Runtime.getRuntime()
                .exec("/opt/pandoc /storage/${id}/md.md -f markdown -t latex -o /storage/${id}/pdf.pdf")
            call.respondText("Successfully added file '${details.name}'", contentType = ContentType.Text.Plain)
        }

        // TODO remove after testing
        // show a specific file
        get("/files/{name}") {
            // extract param
            val name = call.parameters["name"]

            // read file (or error msg)
            val file = File("/storage/$name")
            val text =
                if (file.exists())
                    file.readText()
                else
                    "File '$name' does not exist"

            call.respondText(text, contentType = ContentType.Text.Plain)
        }

        get("/pdf/{id}") {
            val id = call.parameters["id"]
            val file = File("/storage/${id}/pdf.pdf")

            if (file.exists())
                call.respondFile(file)
            else
                call.respondText("PDF does not exist.", contentType = ContentType.Text.Plain)
        }

    }
}

// returns String
private fun getId() = Instant.now().toEpochMilli().toString()

