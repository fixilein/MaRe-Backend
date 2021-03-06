package at.troebinger.mc

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream
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

        get("/status") {
            call.respondText("Up: Version 1.3, pandoc version 2.11.2-linux-amd64", contentType = ContentType.Text.Plain)
        }

        post("/upload/{id}/md") {
            val id = call.parameters["id"]
            val file = File("/storage/${id}/md.md")

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output -> input.copyToSuspend(output) }
                        }
                    }
                }
                part.dispose()
            }
            call.respond(HttpStatusCode.Created)
        }

        post("/upload/{id}/img") {
            val id = call.parameters["id"]
            val dir = File("/storage/${id}/img/")
            dir.mkdirs()

            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        part.streamProvider().use { input ->
                            val file = File(dir, part.originalFileName)
                            file.outputStream().buffered().use { output -> input.copyToSuspend(output) }
                        }
                    }
                }
                part.dispose()
            }
            call.respond(HttpStatusCode.Created)
        }

        get("/pdf/{id}") {
            val id = call.parameters["id"]
            generatePDF(id)
            val file = File("/storage/${id}/pdf.pdf")

            if (file.exists())
                call.respondFile(file)
            else
                call.respond(HttpStatusCode.NotFound) // 404 Error
        }

        get("/log/{id}") {
            val id = call.parameters["id"]
            val log = File("/storage/${id}/log")
            call.respondText(log.readText(), contentType = ContentType.Text.Plain)
        }

    }
}

private fun generatePDF(id: String?) {
    val exec = Runtime.getRuntime()
        .exec("/make.sh $id")
    exec.waitFor()
}

fun InputStream.toFile(path: String) {
    File(path).outputStream().use { this.copyTo(it) }
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}

// returns String
private fun getId() = Instant.now().toEpochMilli().toString()

