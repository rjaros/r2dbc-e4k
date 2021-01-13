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

import org.springframework.data.r2dbc.core.ReactiveUpdateOperation
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.data.relational.core.query.Update
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.FetchSpec
import org.springframework.r2dbc.core.Parameter
import pl.treksoft.e4k.query.Query
import reactor.core.publisher.Mono

fun DbClient.update(): UpdateTableSpec {
    return UpdateTableSpecImpl(this)
}

interface UpdateTableSpec {
    fun table(table: String): UpdateValuesSpec
    fun <T> table(type: Class<T>): ReactiveUpdateOperation.ReactiveUpdate
}

inline fun <reified T : Any> UpdateTableSpec.table(): ReactiveUpdateOperation.ReactiveUpdate {
    return table(T::class.java)
}

inline fun <reified T : Any> ReactiveUpdateOperation.UpdateWithQuery.using(
    obj: T,
    dbClient: DbClient
): Mono<Int> {
    val dataAccessStrategy = dbClient.r2dbcEntityTemplate.dataAccessStrategy
    val idColumns = dbClient.r2dbcEntityTemplate.dataAccessStrategy.getIdentifierColumns(T::class.java)
    if (idColumns.isEmpty()) throw IllegalStateException("Identifier columns not declared")
    val firstIdColumn = idColumns.first()
    val outboundRow = dataAccessStrategy.getOutboundRow(obj)
    val where = where(firstIdColumn.reference).`is`(
        outboundRow[firstIdColumn]?.value
            ?: throw IllegalStateException("Identifier value not set (${firstIdColumn.reference})")
    )
    val criteria = idColumns.drop(1).fold(where) { criteria, idColumn ->
        criteria.and(idColumn.reference)
            .`is`(
                outboundRow[idColumn]?.value
                    ?: throw IllegalStateException("Identifier value not set (${idColumn.reference}")
            )
    }
    val columns = dbClient.r2dbcEntityTemplate.dataAccessStrategy.getAllColumns(T::class.java) - idColumns
    if (columns.isEmpty()) throw java.lang.IllegalStateException("There are no not-identifier columns to update")
    val firstColumn = columns.first()
    val firstUpdate = Update.update(firstColumn.reference, outboundRow[firstColumn]?.value)
    val update = columns.drop(1).fold(firstUpdate) { update, column ->
        update.set(column.reference, outboundRow[column]?.value)
    }
    return matching(query(criteria)).apply(update)
}

private class UpdateTableSpecImpl(private val dbClient: DbClient) : UpdateTableSpec {
    override fun table(table: String): UpdateValuesSpec {
        return UpdateValuesSpecImpl(dbClient, table)
    }

    override fun <T> table(type: Class<T>): ReactiveUpdateOperation.ReactiveUpdate {
        return dbClient.r2dbcEntityTemplate.update(type)
    }
}

interface SetterSpec {
    val Update: SetterSpec
    fun update(field: String, value: Any): UpdateValuesSpec
    fun update(field: String, value: Any?, type: Class<*>): UpdateValuesSpec
    fun set(field: String, value: Any): UpdateValuesSpec
    fun set(field: String, value: Any?, type: Class<*>): UpdateValuesSpec
    fun set(parameters: Map<String, Any?>): UpdateValuesSpec
}

inline fun <reified T : Any> SetterSpec.updateNullable(field: String, value: T? = null) =
    update(field, value, T::class.java)

inline fun <reified T : Any> SetterSpec.setNullable(field: String, value: T? = null) =
    set(field, value, T::class.java)

interface UpdateValuesSpec : SetterSpec {
    fun using(setters: SetterSpec.() -> Unit): UpdateValuesSpec
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

private class UpdateValuesSpecImpl(
    private val dbClient: DbClient,
    private val table: String
) : UpdateValuesSpec {
    val values = mutableMapOf<String, Any?>()

    override val Update: SetterSpec
        get() = this

    override fun update(field: String, value: Any): UpdateValuesSpec {
        return set(field, value)
    }

    override fun update(field: String, value: Any?, type: Class<*>): UpdateValuesSpec {
        return set(field, value, type)
    }

    override fun set(field: String, value: Any): UpdateValuesSpec {
        values[field] = value
        return this
    }

    override fun set(field: String, value: Any?, type: Class<*>): UpdateValuesSpec {
        values[field] = Parameter.fromOrEmpty(value, type)
        return this
    }

    override fun set(parameters: Map<String, Any?>): UpdateValuesSpec {
        values.putAll(parameters)
        return this
    }

    override fun using(setters: SetterSpec.() -> Unit): UpdateValuesSpec {
        this.setters()
        return this
    }

    override fun matching(where: String?, whereParameters: Map<String, Any?>?): DatabaseClient.GenericExecuteSpec {
        val updateParameters = values.map { (name, _) -> "$name = :$name" }.joinToString(", ")
        val sql = "UPDATE $table SET $updateParameters"
        val sqlToExecute = if (where != null) {
            "$sql WHERE $where"
        } else {
            sql
        }
        return dbClient.databaseClient.sql(sqlToExecute).bindMap(values + (whereParameters ?: emptyMap()))
    }
}
