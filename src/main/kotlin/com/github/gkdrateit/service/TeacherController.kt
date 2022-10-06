package com.github.gkdrateit.service

import com.github.gkdrateit.database.Teacher
import com.github.gkdrateit.database.TeacherModel
import com.github.gkdrateit.database.Teachers
import io.javalin.http.Context
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction


class TeacherController : CrudApiBase() {
    override val path: String
        get() = "/teacher"

    override fun handleCreate(ctx: Context): ApiResponse<String> {
        val param = ctx.paramJsonMap()
        if (param["name"] == null) {
            return missingParamError("name")
        }

        val nameDec = try {
            String(base64Decoder.decode(param["name"]!!))
        } catch (e: IllegalArgumentException) {
            return base64Error("name")
        }

        try {
            // Q: should I check if it repeats manually?
            transaction {
                Teacher.new {
                    name = nameDec
                    email = param["email"]
                }
            }
            return success()
        } catch (e: Throwable) {
            return databaseError(e.message ?: "")
        }
    }

    override fun handleRead(ctx: Context): ApiResponse<out Any> {
        val query = Teachers.selectAll()
        val param = ctx.paramJsonMap()
        param["teacherId"]?.let {
            query.andWhere { Teachers.id eq it.toInt() }
        }
        param["name"]?.let {
            query.andWhere { Teachers.name like "$it%" }
        }
        val totalCount = transaction { query.count() }
        val pagination = getPaginationInfoOrDefault(param)
        query.limit(pagination.limit, pagination.offset)
        transaction {
            query.map { Teacher.wrapRow(it).toModel() }
        }.let {
            return successReply(it, totalCount, pagination)
        }
    }

    override fun handleUpdate(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }

    override fun handleDelete(ctx: Context): ApiResponse<String> {
        return notImplementedError()
    }
}