/*
 * Copyright (c) 2020 Robert Jaros
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package pl.treksoft.e4k.core

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.ReactiveInsertOperation
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import org.springframework.r2dbc.core.Parameter
import org.springframework.r2dbc.core.RowsFetchSpec
import org.springframework.r2dbc.core.awaitOne
import reactor.core.publisher.Mono

fun DbClient.insert(): InsertIntoSpec {
    return InsertIntoSpecImpl(this)
}

interface InsertIntoSpec {
    fun into(table: String): InsertValuesSpec
    fun into(table: String, idColumn: String): InsertValuesKeySpec
    fun <T> into(type: Class<T>): ReactiveInsertOperation.ReactiveInsert<T>
}

inline fun <reified T : Any> InsertIntoSpec.into(): ReactiveInsertOperation.ReactiveInsert<T> {
    return into(T::class.java)
}

private class InsertIntoSpecImpl(private val dbClient: DbClient) : InsertIntoSpec {
    override fun into(table: String): InsertValuesSpec {
        return InsertValuesSpecImpl(dbClient, table)
    }

    override fun into(table: String, idColumn: String): InsertValuesKeySpec {
        return InsertValuesKeySpecImpl(dbClient, table, idColumn)
    }

    override fun <T> into(type: Class<T>): ReactiveInsertOperation.ReactiveInsert<T> {
        return dbClient.r2dbcEntityTemplate.insert(type)
    }
}

interface InsertValuesSpec {
    fun value(field: String, value: Any): InsertValuesSpec
    fun value(field: String, value: Any?, type: Class<*>): InsertValuesSpec
    fun nullValue(field: String): InsertValuesSpec
    fun nullValue(field: String, type: Class<*>): InsertValuesSpec
    fun fetch(): FetchSpec<out Any>
    fun then(): Mono<Void>
    suspend fun await()
}

inline fun <reified T : Any> InsertValuesSpec.valueNullable(field: String, value: T? = null) =
    value(field, value, T::class.java)

private class InsertValuesSpecImpl(
    private val dbClient: DbClient,
    private val table: String
) : InsertValuesSpec {
    val values = mutableMapOf<String, Any?>()

    override fun value(field: String, value: Any): InsertValuesSpec {
        values[field] = value
        return this
    }

    override fun value(field: String, value: Any?, type: Class<*>): InsertValuesSpec {
        values[field] = Parameter.fromOrEmpty(value, type)
        return this
    }

    override fun nullValue(field: String): InsertValuesSpec {
        values[field] = null
        return this
    }

    override fun nullValue(field: String, type: Class<*>): InsertValuesSpec {
        values[field] = Parameter.empty(type)
        return this
    }

    private fun execute(): DatabaseClient.GenericExecuteSpec {
        if (values.isEmpty()) throw IllegalStateException("No values specified")
        val names = values.keys.joinToString(", ")
        val namedArguments = values.keys.map { ":$it" }.joinToString(", ")
        val sql = "INSERT INTO $table ($names) VALUES ($namedArguments)"
        return dbClient.databaseClient.sql(sql).bindMap(values)
    }

    override fun fetch(): FetchSpec<out Any> {
        return execute().fetch()
    }

    override fun then(): Mono<Void> {
        return execute().then()
    }

    override suspend fun await() {
        execute().then().awaitFirstOrNull()
    }
}

interface InsertValuesKeySpec {
    fun value(field: String, value: Any): InsertValuesKeySpec
    fun value(field: String, value: Any?, type: Class<*>): InsertValuesKeySpec
    fun nullValue(field: String): InsertValuesKeySpec
    fun nullValue(field: String, type: Class<*>): InsertValuesKeySpec
    fun then(): Mono<Void>
    fun fetch(): RowsFetchSpec<Int>
    suspend fun awaitOne(): Int
    fun fetchLong(): RowsFetchSpec<Long>
    suspend fun awaitOneLong(): Long
}

inline fun <reified T : Any> InsertValuesKeySpec.valueNullable(field: String, value: T? = null) =
    value(field, value, T::class.java)


private class InsertValuesKeySpecImpl(
    private val dbClient: DbClient,
    private val table: String,
    private val idColumn: String
) : InsertValuesKeySpec {
    val values = mutableMapOf<String, Any?>()

    override fun value(field: String, value: Any): InsertValuesKeySpec {
        values[field] = value
        return this
    }

    override fun value(field: String, value: Any?, type: Class<*>): InsertValuesKeySpec {
        values[field] = Parameter.fromOrEmpty(value, type)
        return this
    }

    override fun nullValue(field: String): InsertValuesKeySpec {
        values[field] = null
        return this
    }

    override fun nullValue(field: String, type: Class<*>): InsertValuesKeySpec {
        values[field] = Parameter.empty(type)
        return this
    }

    override fun then(): Mono<Void> {
        return then()
    }

    override fun fetch(): RowsFetchSpec<Int> {
        if (values.isEmpty()) throw IllegalStateException("No values specified")
        val names = values.keys.joinToString(", ")
        val namedArguments = values.keys.map { ":$it" }.joinToString(", ")
        val sql = "INSERT INTO $table ($names) VALUES ($namedArguments)"
        val executeSpec = dbClient.databaseClient.sql(sql).bindMap(values)
        return executeSpec.filter { s -> s.returnGeneratedValues(idColumn) }.map { row ->
            row.get(idColumn, Integer::class.java)?.toInt()
        }
    }

    override fun fetchLong(): RowsFetchSpec<Long> {
        if (values.isEmpty()) throw IllegalStateException("No values specified")
        val names = values.keys.joinToString(", ")
        val namedArguments = values.keys.map { ":$it" }.joinToString(", ")
        val sql = "INSERT INTO $table ($names) VALUES ($namedArguments)"
        val executeSpec = dbClient.databaseClient.sql(sql).bindMap(values)
        return executeSpec.filter { s -> s.returnGeneratedValues(idColumn) }.map { row ->
            row.get(idColumn, java.lang.Long::class.java)?.toLong()
        }
    }

    override suspend fun awaitOne(): Int {
        return fetch().awaitOne()
    }

    override suspend fun awaitOneLong(): Long {
        return fetchLong().awaitOne()
    }
}
