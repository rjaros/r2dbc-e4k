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

import org.springframework.data.r2dbc.core.ReactiveDeleteOperation
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import pl.treksoft.e4k.query.Query
import reactor.core.publisher.Mono

fun DbClient.delete(): DeleteTableSpec {
    return DeleteTableSpecImpl(this)
}

interface DeleteTableSpec {
    fun from(table: String): DeleteValuesSpec
    fun <T> from(type: Class<T>): ReactiveDeleteOperation.ReactiveDelete
}

inline fun <reified T : Any> DeleteTableSpec.from(): ReactiveDeleteOperation.ReactiveDelete {
    return from(T::class.java)
}

private class DeleteTableSpecImpl(private val dbClient: DbClient) : DeleteTableSpec {
    override fun from(table: String): DeleteValuesSpec {
        return DeleteValuesSpecImpl(dbClient, table)
    }

    override fun <T> from(type: Class<T>): ReactiveDeleteOperation.ReactiveDelete {
        return dbClient.r2dbcEntityTemplate.delete(type)
    }
}

interface DeleteValuesSpec {
    fun matching(where: String? = null, whereParameters: Map<String, Any?>? = null): DatabaseClient.GenericExecuteSpec
    fun matching(query: Query): DatabaseClient.GenericExecuteSpec {
        return matching(query.sql, query.parameters)
    }

    fun fetch(): FetchSpec<MutableMap<String, Any>> {
        return matching().fetch()
    }

    fun then(): Mono<Void> {
        return matching().then()
    }
}

private class DeleteValuesSpecImpl(
    private val dbClient: DbClient,
    private val table: String
) : DeleteValuesSpec {

    override fun matching(where: String?, whereParameters: Map<String, Any?>?): DatabaseClient.GenericExecuteSpec {
        val sql = "DELETE FROM $table"
        val sqlToExecute = if (where != null) {
            "$sql WHERE $where"
        } else {
            sql
        }
        return dbClient.databaseClient.sql(sqlToExecute).bindMap(whereParameters ?: emptyMap())
    }
}
