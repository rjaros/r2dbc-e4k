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

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.Parameter
import org.springframework.r2dbc.core.RowsFetchSpec
import pl.treksoft.e4k.query.Query
import kotlin.reflect.KClass

fun DatabaseClient.GenericExecuteSpec.bindMap(parameters: Map<String, Any?>): DatabaseClient.GenericExecuteSpec {
    return parameters.entries.fold(this) { spec, entry ->
        if (entry.value == null) {
            spec.bindNull(entry.key, String::class.java)
        } else {
            spec.bind(entry.key, Parameter.fromOrEmpty(entry.value, entry.value!!::class.java))
        }
    }
}

fun DatabaseClient.GenericExecuteSpec.bindIndexedMap(parameters: Map<Int, Any?>): DatabaseClient.GenericExecuteSpec {
    return parameters.entries.fold(this) { spec, entry ->
        if (entry.value == null) {
            spec.bindNull(entry.key, String::class.java)
        } else {
            spec.bind(entry.key, Parameter.fromOrEmpty(entry.value, entry.value!!::class.java))
        }
    }
}

fun DbClient.execute(sql: String) = databaseClient.sql(sql)

inline fun <reified T : Any> DbClient.execute(
    sql: String
): BindSpec<T> {
    return BindSpecImpl(this, sql, T::class)
}

interface BindSpec<T : Any> {
    fun bind(index: Int, value: Any): BindSpec<T>
    fun bindNull(index: Int, type: Class<*>): BindSpec<T>
    fun bind(name: String, value: Any): BindSpec<T>
    fun bindNull(name: String, type: Class<*>): BindSpec<T>
    fun fetch(): RowsFetchSpec<T>
}

@PublishedApi
internal class BindSpecImpl<T : Any>(
    private val dbClient: DbClient,
    private val sql: String,
    private val type: KClass<T>
) :
    BindSpec<T> {
    private val indexedParameters = mutableMapOf<Int, Any?>()
    private val namedParameters = mutableMapOf<String, Any?>()

    override fun bind(index: Int, value: Any): BindSpec<T> {
        indexedParameters[index] = value
        return this
    }

    override fun bindNull(index: Int, type: Class<*>): BindSpec<T> {
        indexedParameters[index] = null
        return this
    }

    override fun bind(name: String, value: Any): BindSpec<T> {
        namedParameters[name] = value
        return this
    }

    override fun bindNull(name: String, type: Class<*>): BindSpec<T> {
        namedParameters[name] = null
        return this
    }

    override fun fetch(): RowsFetchSpec<T> {
        return dbClient.databaseClient.sql(sql).bindMap(namedParameters).bindIndexedMap(indexedParameters)
            .map { row, rowMetadata ->
                dbClient.mappingR2dbcConverter.read(type.java, row, rowMetadata)
            }
    }
}

inline fun <reified T : Any> DbClient.execute(
    sql: String,
    parameters: Map<String, Any?> = emptyMap()
): RowsFetchSpec<T> {
    return databaseClient.sql(sql).bindMap(parameters).map { row, rowMetadata ->
        mappingR2dbcConverter.read(T::class.java, row, rowMetadata)
    }
}

inline fun <reified T : Any> DbClient.execute(
    query: Query
): RowsFetchSpec<T> {
    return execute(query.sql, query.parameters)
}
