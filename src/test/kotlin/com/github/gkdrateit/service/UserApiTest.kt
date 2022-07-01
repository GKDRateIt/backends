package com.github.gkdrateit.service


import io.javalin.testtools.JavalinTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals


internal class UserApiTest {
    private val apiServer = ApiServer()

    @Test
    fun successCreate() = JavalinTest.test(apiServer.app) { server, client ->
        val allowedChars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randStr = (1..10)
            .map { allowedChars.random() }
            .joinToString("")
        val formBody = FormBody.Builder()
            .add("_action", "create")
            .add("email", "test_$randStr@ucas.ac.cn")
            .add("hashedPassword", "123456")
            .add("nickname", Base64.getEncoder().encodeToString("❤Aerith❤".toByteArray()))
            .add("startYear", "2020")
            .add("group", "default")
            .build()
        val req = Request.Builder()
            .url("http://localhost:${server.port()}/api/user")
            .post(formBody)
            .build()
        client.request(req).use {
            assertEquals(it.code, 200)
            val bodyStr = it.body!!.string()
            val body = Json.decodeFromString<ApiResponse<String>>(bodyStr)
            assertEquals(body.status, ResponseStatus.SUCCESS, bodyStr)
        }
        // TODO: query after create
    }
}